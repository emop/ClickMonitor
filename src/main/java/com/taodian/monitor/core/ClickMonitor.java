package com.taodian.monitor.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.api.TaodianApi;
import com.taodian.monitor.Settings;
import com.taodian.monitor.bolt.AbstractClickMonitorBolt;
import com.taodian.monitor.spout.FileDataSpout;
import com.taodian.monitor.spout.HTTPURLSpout;
import com.taodian.monitor.storm.ItemFactory;
import com.taodian.monitor.storm.Topology;
import com.taodian.monitor.storm.TopologyBuilder;
import com.taodian.monitor.storm.impl.DefaultSimpleTopology;
import com.taodian.monitor.storm.impl.SimpleTopologyBuilder;

/**
 * 监控服务
 * @author deonwu
 *
 */

public class ClickMonitor {
	/**
	 * 从点击网关过来得原始数据队列
	 */
	public static final String MQ_CLICK_GATE = "click_gate";

	/**
	 * 消息点击日志。
	 */
	public static final String MQ_CLICK_LOG = "click_log";
	public static final String MQ_CPC_LOG = "cpc_log";

	/**
	 * 短网址处理队列。
	 */
	public static final String MQ_SHORT_URL = "short_url";
	/**
	 * 新用户访问队列。
	 */
	public static final String MQ_NEW_VISITOR = "new_visitor";
	/**
	 * 告警队列。
	 */
	public static final String MQ_ALARM = "alarm";
	
	
	/**
	 * 告警队列。
	 */
	public static final String DATA_RAW = "raw";
	
	/**
	 * 短网址数据。
	 */
	public static final String DATA_SHORT_URL = "short_url";
	
	
	private Log log = LogFactory.getLog("click.mointor");  
	private static ClickMonitor ins = null;
	private Topology topology = null;

	/**
	 * 如果从命令行指定了，日志文件名。将创建一个fileSpout。 否则默认
	 * 从ClickGate通过网络加载消息处理。
	 */
	private FileDataSpout fileSpout = null;
	
	private ThreadPoolExecutor workPool = null;
	private DataService ds = null;
	private LinkedBlockingDeque<Runnable> taskQueue = null;
	private TaodianApi api = null;
	//private String inputFile = null;
	
	public static synchronized ClickMonitor getInstance(){
		if(ins == null){
			ins = new ClickMonitor();
		}
		return ins;
	}
	
	/**
	 * 设置单个文件处理。
	 * @param file
	 */
	public void setInputFile(String file){
		fileSpout = new FileDataSpout();
		fileSpout.load(new File(file));		
	}

	/**
	 * 服务开始启动，
	 * 0. 初始化Redis数据库连接。
	 * 1. 加载所有blot，初始化状态数据。
	 * 2. 加载spout 开始处理数据
	 */
	public void start(){
		int coreWrokerSize = Settings.getInt(Settings.CORE_WORKER_SIZE, 10);
		int queueSize = Settings.getInt(Settings.WRITE_LOG_QUEUE_SIZE, 1024);
		
		taskQueue = new LinkedBlockingDeque<Runnable>(queueSize);

		log.debug("start message process thread pool, core size:" + coreWrokerSize + ", queue size:" + queueSize);
		workPool = new ThreadPoolExecutor(
				coreWrokerSize,
				coreWrokerSize * 2,
				10, 
				TimeUnit.SECONDS, 
				taskQueue
				);

		String appKey = Settings.getString(Settings.TAODIAN_APPID, null); // System.getProperty("");
		String appSecret = Settings.getString(Settings.TAODIAN_APPSECRET, null);
		String appRoute = Settings.getString(Settings.TAODIAN_APPROUTE, "http://api.zaol.cn/api/route");
		boolean inSAE = Settings.getString("in_sae", "n").equals("y");
		
		if(appKey != null && appSecret != null){
			api = new TaodianApi(appKey, appSecret, appRoute, inSAE ? "simple" : "apache");		
		}else {
			log.info("The taodian.api_id and taodian.api_secret Java properties are required.");
			System.exit(255);
		}
		
		ds = new DataService();
		if(!ds.start(workPool, api)){
			log.error("Data service start failed");
			System.exit(255);
		}
		
		
		topology = new DefaultSimpleTopology(workPool);			
		TopologyBuilder builder = null; //new SimpleTopologyBuilder();
		String topolgyName = Settings.getString(Settings.TOPOLOGY, "default_topology.cfg");
		
		File f = new File(topolgyName);
		InputStream ins = null;
		if(f.isFile()){
			log.info("Load topology from file, " + f.getAbsolutePath());
			try {
				ins = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				log.error(e, e);
			}
		}else {
			ins = this.getClass().getClassLoader().getResourceAsStream(topolgyName);
		}
		if(ins != null){
			builder = new SimpleTopologyBuilder(ins);
		}
		
		builder.buildToplogy(topology, new ObjectFactory());
		
		if(fileSpout != null){
			topology.setSpout(Settings.INPUT_SPOUT, fileSpout);
		}else {
			String clickGate = Settings.getString(Settings.CLICK_GATE_LOG_URL, "");
			HTTPURLSpout spout = new HTTPURLSpout();
			if(spout.connect(clickGate)){
				topology.setSpout(Settings.INPUT_SPOUT, spout);				
			}
		}
		
		topology.start();		
	}
	
	/**
	 * 
	 */
	public void shutdown(){
		workPool.shutdownNow();
	}
	
	public boolean isCommandLineMode(){
		return fileSpout != null;
	}
	
	/**
	 * 等待所有的Spout结束，用在分析单个文件的时候。等待所有日志处理完成。
	 */
	public void waitAllSpoutDone(){
		while(fileSpout != null && !fileSpout.isClosed()){
			synchronized(fileSpout){
				try {
					fileSpout.wait(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	class ObjectFactory implements ItemFactory{
		private Map<String, Object> maps = new HashMap<String, Object>();
		
		@Override
		public Object create(String name) {
			Object tmp = maps.get(name);
			if(tmp == null){
				try {
					Class cls = Class.forName(name);
					tmp = cls.newInstance();
					if(tmp instanceof AbstractClickMonitorBolt){
						AbstractClickMonitorBolt b = (AbstractClickMonitorBolt)tmp;
						b.setDataService(ds);
					}
					maps.put(name, tmp);
				} catch (Exception e) {
					log.error("Failed to create object, name:" + name, e);
				}
			}
			
			return tmp;
		}
		
	}
	

}
