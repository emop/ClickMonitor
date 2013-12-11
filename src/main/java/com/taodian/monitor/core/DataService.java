package com.taodian.monitor.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.taodian.monitor.Settings;
import com.taodian.monitor.model.ShortUrlModel;

/**
 * 数据操作服务。
 * 
 * @author deonwu
 */
public class DataService {
	private JedisPool connPool = null; //new JRedisClient(
	
	public boolean start(){
		
		String host = Settings.getString("redis.host", "127.0.0.1");
		connPool = new JedisPool(new JedisPoolConfig(), host);
		Jedis c = getJedis();
		String p = c.ping();
		releaseConn(c);
		return p != null && p.equals("PONG");
	}
	
	public ShortUrlModel getShortUrl(String key){
		return null;
	}
	
	public Jedis getJedis(){
		return connPool.getResource();
	}
	
	public void releaseConn(Jedis jedis){
		if(jedis != null){
			connPool.returnResource(jedis);
		}
	}
}
