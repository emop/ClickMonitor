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
	public static final String WRITE_LOG_THREAD_COUNT = "write_log_thread_count";
	public static final String GET_SHORT_URL_THREAD_COUNT = "get_short_url_thread_count";

	
	public static final String WRITE_ACCESS_LOG = "write_access_log";

	public static final String TAOKE_SOURCE_DOMAIN = "taoke_source_domain";

	public static final String CACHE_URL_TIMEOUT = "cache_url_timeout";

	
	private static Log log = LogFactory.getLog("click.settings");
	protected static Properties settings = new Properties(); //System.getProperties();	
	private static String confName = "short_url.conf";
	
	//private String[] masterSettings = new String[]{};
	//private String[] routeSettings = new String[]{};
	
	public static void loadSettings(){
		try {
			InputStream is = Settings.class.getClassLoader().getResourceAsStream(confName);
			if(is != null){
				settings.load(is);
			}
		} catch (IOException e) {
			log.error(e, e.getCause());
		}
		
		File f = new File(confName);
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
