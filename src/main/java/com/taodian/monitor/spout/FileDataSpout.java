package com.taodian.monitor.spout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.TopologyContext;

public class FileDataSpout implements DataSpout {
	protected Log log = LogFactory.getLog("lm.mointor.spout");  
	
	private File path = null;
	private BufferedReader reader = null;
	
	public void load(File f){
		path = f;
	}
	
	@Override
	public void prepare(TopologyContext context) {
		try {
			InputStream ins = new FileInputStream(path);
			reader = new BufferedReader(new InputStreamReader(ins, "utf8"));
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	@Override
	public DataCell nextDataCell() {
		String line = null;
		DataCell d = null;
		try {
			line = reader.readLine();
			if(line != null){
				d = new DataCell();
				d.set(ClickMonitor.DATA_RAW, line.trim());
			}else {
				reader.close();
				reader = null;
			}			
		} catch (IOException e) {
			log.error(e, e);	
		}

		return d;
	}

	@Override
	public boolean isClosed() {
		return reader ==  null;
	}

	public String toString(){
		return "File spout, path:" + path.getAbsolutePath();
	}


}
