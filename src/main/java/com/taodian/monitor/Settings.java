package com.taodian.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Settings {	
	
	public static final String LOG_LEVEL = "log_level";
	public static final String MAX_LOG_DAYS = "max_log_days";
	public static final String HTTP_PORT = "http_port";

	public static final String TAODIAN_APPID = "taodian.api_id";
	public static final String TAODIAN_APPSECRET = "taodian.api_secret";
	public static final String TAODIAN_APPROUTE = "taodian.api_route";
	
	public static final String WRITE_LOG_QUEUE_SIZE = "write_log_queue_size";
	public static final String CORE_WORKER_SIZE = "core_worker_size";
	//public static final String GET_SHORT_URL_THREAD_COUNT = "get_short_url_thread_count";
	
	public static final String TOPOLOGY = "topology";
	
	/**
	 * 消息处理结构的，输入流。
	 */
	public static final String INPUT_SPOUT = "click_gate";
	
	public static final String CLICK_GATE_LOG_URL = "click_gate_url";

	
	private static Log log = LogFactory.getLog("click.settings");
	protected static Properties settings = new Properties(); //System.getProperties();	
	//private static String confName = "short_url.conf";
	
	//private String[] masterSettings = new String[]{};
	//private String[] routeSettings = new String[]{};
	
	public static void loadSettings(String name){
		try {
			InputStream is = Settings.class.getClassLoader().getResourceAsStream("click_monitor.conf");
			if(is != null){
				settings.load(is);
			}
		} catch (IOException e) {
			log.error(e, e.getCause());
		}
		
		File f = new File(name);
		InputStream ins = null;
		if(f.isFile()){
			try {
				ins = new FileInputStream(f);
				settings.load(ins);
			} catch (FileNotFoundException e) {
				log.error(e, e.getCause());
			} catch (IOException e) {
				log.error(e, e.getCause());
			} finally{
				if(ins != null) {
					try {
						ins.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	public static String getString(String name, String def){
		return settings.getProperty(name, def);
	}
	
	public static int getInt(String name, int def){
		String val = settings.getProperty(name);
		int intVal = def;
		try{
			if(val != null) intVal = Integer.parseInt(val);
		}catch(Exception e){
		}
		
		return intVal;
	}	
	
	public static void dumpSetting(PrintWriter out) throws IOException{
		
		Properties tmp = new Properties();
		tmp.putAll(settings);
		tmp.put("taodian.api_secret", "****");
		tmp.store(out, "");
	}
		
}
