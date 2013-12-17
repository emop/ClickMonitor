package com.taodian.monitor.spout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.TopologyContext;

public class HTTPURLSpout implements DataSpout {
	protected Log log = LogFactory.getLog("lm.mointor.spout");  
	protected URL logGate = null;
	private BufferedReader reader = null;
	protected HttpURLConnection connection = null;

	public HTTPURLSpout(){
		
	}
	
	public boolean connect(String url){
		try{
			logGate = new URL(url);			
		}catch(Exception e){
			return false;
		}		
		return openLogUrl();
	}
	
	@Override
	public DataCell nextDataCell() {
		String line = null;
		DataCell d = null;
		try {
			line = reader.readLine();
			log.debug("log:" + line);
			if(line != null){
				d = new DataCell();
				d.set(ClickMonitor.DATA_RAW, line.trim());
			}else {
				openLogUrl();
				//reader.close();
				//reader = null;
			}
		} catch (IOException e) {
			log.error(e, e);
			openLogUrl();
		}

		return d;
	}

	@Override
	public void prepare(TopologyContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return reader == null;
	}

	
	private boolean openLogUrl(){
		if(logGate == null){
			return false;
		}
		if(reader != null){
			try {
				reader.close();
			} catch (IOException e) {
			}
			reader = null;
		}
		if(connection != null){
			connection.disconnect();
		}
		
		log.debug("connecting to " + logGate.toString());
		try {
			connection = (HttpURLConnection )logGate.openConnection();
			connection.setReadTimeout(1000 * 60 * 30);
			connection.setConnectTimeout(1000 * 5);
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.connect();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf8"));
			//if(reader.)
			String c = reader.readLine();
			if(c.trim().startsWith("CONNECTED")){
				return true;
			}else {
				log.info("Failed to connect to click gate, msg:" + c);
				return false;
			}
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
		
		return false;
	}


}
