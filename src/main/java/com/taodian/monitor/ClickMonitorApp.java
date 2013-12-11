package com.taodian.monitor;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.core.ClickMonitor;


public class ClickMonitorApp {
	
	public static final String VERSION = "version";
	public static final String INPUTFILE = "file";
	public static final String CONFIG = "cfg";

	public static final String RESET_REPORT = "reset";
		
    public static void main( String[] args ) throws Exception
    {
    	System.setProperty("user.timezone","Asia/Shanghai");
    	updateLog4jLevel("click_monitor");
    	startCleanLog("click_monitor");
    	
    	Log log = LogFactory.getLog("click.mointor");    	
    	
		Options options = new Options();
		options.addOption(VERSION, false, "show version.");
		options.addOption(CONFIG, true, "the config file, default is monitor.conf, None does not read config file.");
		options.addOption(INPUTFILE, true, "read logs from a file.");
		options.addOption(RESET_REPORT, true, "reset the monitor data from a date");
		
		CommandLine cmd = null;
		
		try{
			CommandLineParser parser = new PosixParser();
			cmd = parser.parse(options, args);			
		}catch(ParseException e){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ClickMonitor", options);
			System.exit(-1);
		}
		
		if(cmd.hasOption(VERSION)){
			System.out.println("ClickMonitor " + Version.getVersion());
			return;
		}
		
		String cfg = cmd.getOptionValue(CONFIG, "monitor.conf");
		Settings.loadSettings(cfg);
		
		ClickMonitor cm = ClickMonitor.getInstance();
		
		if(cmd.hasOption(INPUTFILE)){
			cm.setInputFile(cmd.getOptionValue(INPUTFILE));
		}
		
		cm.start();		
		if(cm.isCommandLineMode()){
			cm.waitAllSpoutDone();
			cm.shutdown();
			log.info("done");
		}else {
			synchronized (ClickMonitorApp.class) {
				ClickMonitorApp.class.wait();
		    }
		}
    }
    
	private static void startCleanLog( final String name){
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
				try{
					updateLog4jLevel(name);
				}catch(Throwable e){
					root.info(e.toString());
				}
			}
		}, 100, 1000 * 3600 * 12);
	}	
	
	private static void updateLog4jLevel(String name){
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        String level = Settings.getString(Settings.LOG_LEVEL, "debug").toLowerCase().trim();
        if(level.equals("trace")){
                root.setLevel(org.apache.log4j.Level.TRACE);
        }else if(level.equals("debug")){
                root.setLevel(org.apache.log4j.Level.DEBUG);
        }else if(level.equals("info")){
                root.setLevel(org.apache.log4j.Level.INFO);
        }else if(level.equals("warn")){
                root.setLevel(org.apache.log4j.Level.WARN);
        }
        File r = new File("logs");
        
        int max_log_days = Settings.getInt(Settings.MAX_LOG_DAYS, 10);                
        Date d = new Date(System.currentTimeMillis() - 1000 * 3600 * 24 * max_log_days);                
        DateFormat format= new SimpleDateFormat("yy-MM-dd");            
        for(File log : r.listFiles()){
            String[] p = log.getName().split("\\.");
            String logDate = p[p.length -1];
            if(logDate.indexOf("-") > 0){
                try {
                    if(format.parse(logDate).getTime() < d.getTime()){
                            root.info("remove old log file:" + log.getName());
                            log.delete();
                    }
                } catch (Exception e) {
                        root.info(e.toString());
                }
            }
        }
	}   

}
