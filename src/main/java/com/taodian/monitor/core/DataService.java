package com.taodian.monitor.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.taodian.api.TaodianApi;
import com.taodian.emop.http.HTTPResult;
import com.taodian.monitor.Settings;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.model.WeiboVisitor;
import com.taodian.monitor.storm.utils.CacheApi;
import com.taodian.monitor.storm.utils.SimpleCacheApi;

/**
 * 数据操作服务。
 * 
 * @author deonwu
 */
public class DataService {
	public static final int DS_COMMON_DATA = 1;

	public static final int DS_CPC_MONITOR = 2;
	public static final int DS_LOG_REPORT = 3;
	public static final int DS_USER_LOG = 4;
	
	
	private Log log = LogFactory.getLog("lm.data");  

	private JedisPool connPool = null; //new JRedisClient(
	protected ThreadPoolExecutor executor = null;
	protected TaodianApi api = null;
	private CacheApi cache = new SimpleCacheApi();
	public CopyOnWriteArraySet<String> pendingShortKey = new CopyOnWriteArraySet<String>();
	public BlockingQueue<Runnable> pendingShortQueue = null;
	
	public boolean start(ThreadPoolExecutor executor, TaodianApi api){
		this.executor = executor;
		pendingShortQueue = executor.getQueue();
		this.api = api;
		
		String host = Settings.getString("redis.host", "127.0.0.1");
		connPool = new JedisPool(new JedisPoolConfig(), host);
		Jedis c = getJedis();
		String p = c.ping();
		releaseConn(c);
		return p != null && p.equals("PONG");
	}
	
	public ShortUrlModel getShortUrl(String key){
		return getShortUrl(key, false);
	}
	
	public Jedis getJedis(){
		return connPool.getResource();
	}
	
	public Jedis getJedis(int db){
		Jedis d = connPool.getResource();
		d.select(db);
		return d;
	}	
	
	public void releaseConn(Jedis jedis){
		if(jedis != null){
			connPool.returnResource(jedis);
		}
	}
	
	public TaodianApi getTaodianApi(){
		return api;
	}
	
	/**
	 * 新生成的用户，不立即保存，等到第一次由访问记录的时候保存。避免因为爬虫生成无效的用户信息。
	 * @param v
	 */
	public void newWeiboVisitor(WeiboVisitor v){
		String ck = "user_" + v.uid;	
		v.noSave = true;
		cache.set(ck, v, 20);
	}
	
	public WeiboVisitor getWeiboVisitor(long uid){
		String ck = "user_" + uid;
		Object obj = cache.get(ck);
		if(obj == null){
			obj = loadFromDb(uid);
			if(obj == null){
				log.debug("not found uid:" + uid);
				WeiboVisitor t = new WeiboVisitor();
				t.uid = uid;
				t.created = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
				t.host = "na";
				t.agent = "na";
				t.ip = "na";
				
				obj = t;
			}else {
				log.debug("load uid from redis:" + uid);
			}
			cache.set(ck, obj, 30 * 60);
		}
		return (WeiboVisitor)obj;
	}	
	
	public void saveWeiboVisitor(WeiboVisitor v){
		DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		v.noSave = false;
		Jedis j = getJedis(DS_COMMON_DATA);
		
		String data = "%s$%s$%s$%s$%s$%s";
		data = String.format(data, v.uid, v.isMobile, v.ip, v.host, timeFormate.format(v.created), v.agent);		
		j.set("user_" + v.uid, data);
		j.expire("user_" + v.uid, 60 * 60 * 24 * 30);
		
		releaseConn(j);
	}
	
	protected WeiboVisitor loadFromDb(long uid){
		DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		WeiboVisitor v = null;
		Jedis j = getJedis(DS_COMMON_DATA);
		
		String data = j.get("user_" + uid);
		if(data != null && data.length() > 10){
			v = new WeiboVisitor();
			v.noSave = false;
			v.isFirst = false;
			
			String tmp[] = data.split("\\$");
			switch(tmp.length){
				case 6: v.agent = tmp[5];
				case 5: try {
					v.created = timeFormate.parse(tmp[4]);
				} catch (ParseException e) {
				}
				case 4: v.host = tmp[3];
				case 3: v.ip = tmp[2];
				case 2: v.isMobile = Boolean.parseBoolean(tmp[1]);
				v.uid = Long.parseLong(tmp[0]);
			}
		}
		releaseConn(j);
		
		return v;
	}
	
	
	
	
	public ShortUrlModel getShortUrl(final String key, boolean noCache){
		Object tmp = null;
		
		for(int i = 0; i < 2 && tmp == null; i++){
			tmp = cache.get(key, true);
			if(tmp == null || noCache){
				if(pendingShortQueue.remainingCapacity() > 1){
					if(!pendingShortKey.contains(key)){
						pendingShortKey.add(key);
						executor.execute(new Runnable(){
							public void run(){
								try{
									getLongUrlFromRemote(key, key);
								}finally{
									pendingShortKey.remove(key);
									synchronized(key){
										key.notifyAll();
									}
								}
							}
						});
					}else {
						log.warn("short key in pending:" + key);
					}
					synchronized(key){
						try {
							key.wait(1000 * 4);
							tmp = cache.get(key, true);
						} catch (InterruptedException e) {
						}
					}
				}else {
					log.error("Have too many pending short url, queue size:" + pendingShortQueue.size());
				}	
			}			
		}
		
		if(tmp != null && tmp instanceof ShortUrlModel){
			/**
			 * 删除错误的转换结果。
			 */
			ShortUrlModel m = (ShortUrlModel)tmp;
			if(m.longUrl == null || m.longUrl.length() < 5){
				cache.remove(key);
			}
			return m;
		}
		return null;
	}
	
	/*
	protected void getLongUrlFromRemote(String uri, String shortKey){
	}
	*/
	
	protected void getLongUrlFromRemote(String uri, String shortKey){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("short_key", shortKey);
		param.put("auto_mobile", "y");

		String errorMsg = "";
		Jedis j = getJedis();
		j.select(DS_COMMON_DATA);

		for(int i = 0; i < 2; i++){
			String shortUrlData = j.get(uri);
			
			HTTPResult r = new HTTPResult();
			boolean fromJedis = false;
			if(shortUrlData != null){
				r.json = (JSONObject)JSONValue.parse(shortUrlData);
				r.isOK = true;
				fromJedis = true;
				log.debug(String.format("load url '" + uri + "' data from redis"));
			}else {
				r = api.call("tool_convert_long_url", param);
			}
			
			ShortUrlModel m = new ShortUrlModel();
			if(r.isOK){
				if(!fromJedis) {
					shortUrlData = JSONValue.toJSONString(r.json);
					j.set(uri, shortUrlData);
				}
				if(i > 0){
					log.warn(String.format("The short url '%s' is get with retry %s times", shortKey, i));
				}
				m.shortKey = shortKey;
				m.longUrl = r.getString("data.long_url");
				m.mobileLongUrl = r.getString("data.mobile_long_url");
				m.shortKeySource = r.getString("data.create_source");
				m.platform = r.getString("data.plat_form");
				m.outId = r.getString("data.out_id");
				
				String tmp = r.getString("data.user_id");
				if(tmp != null && tmp.length() > 0){
					m.userId = Integer.parseInt(tmp);
				}
				tmp = r.getString("data.num_iid");
				if(tmp != null && tmp.length() > 0){
					m.numIid = Long.parseLong(tmp);
				}
				tmp = r.getString("data.shop_id");
				if(tmp != null && tmp.length() > 0){
					m.shopId = Long.parseLong(tmp);
				}
				
				cache.set(uri, m, 5 * 60);
				
				break;
			} //已经明确的返回错误了，就不用重试了。		
			else if(r.errorCode != null && r.errorCode.equals("not_found")){
				m.shortKey = shortKey;
				m.longUrl = "/";
				m.mobileLongUrl = "/";
				cache.set(uri, m, 5 * 60);
				
				break;
			}
			errorMsg = "code:" + r.errorCode + ", msg:" + r.errorMsg;
			log.error(String.format("The short url '%s' is not found, error:%s", shortKey, errorMsg));
			try{
				Thread.sleep(1000 * (i + 1));
			}catch(Exception e){}
		}
		
		releaseConn(j);
	}
}
