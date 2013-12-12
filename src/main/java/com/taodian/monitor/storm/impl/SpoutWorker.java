package com.taodian.monitor.storm.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.OutputCollector;

public class SpoutWorker implements Runnable {
	private Log log = LogFactory.getLog("spout.worker");  
	
	private DataSpout spout = null;
	private List<String> outQueue = null;
	private OutputCollector collector = null;
	public SpoutWorker(DataSpout spout, List<String> outQueue, OutputCollector collector){
		this.spout = spout;
		this.outQueue = outQueue;
		this.collector = collector;
	}
	
	@Override
	public void run() {
		try{
			while(!spout.isClosed()){
				DataCell cell = spout.nextDataCell();				
				for(String queue: outQueue){
					collector.emit(queue, cell);
				}
			}
		}catch(Throwable e){
			log.error(e, e);
		}
	}

}
