package com.taodian.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;


public class ClickMonitorApp {

    public static void main( String[] args ) throws Exception
    {
    	System.setProperty("user.timezone","Asia/Shanghai");
    	//Settings.loadSettings();
    	//updateLog4jLevel("short_url");
    	//startCleanLog("short_url");
    	
    	//ShortUrlService.getInstance();
    	
    	Log log = LogFactory.getLog("click.gate");    	
    	
    	int port = Settings.getInt(Settings.HTTP_PORT, 8082);
        Server server = new Server(port);
        
        log.info("Starting click gate http server at port:" + port);
        
        ServletHandler context = new ServletHandler();
        server.setHandler(context);
 

 
        server.start();
        server.join();
    }

}
