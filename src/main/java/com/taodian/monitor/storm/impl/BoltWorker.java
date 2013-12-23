package com.taodian.monitor.storm.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.DataQueue;
import com.taodian.monitor.storm.OutputCollector;

public class BoltWorker implements Runnable {
	private Log log = LogFactory.getLog("bolt.worker");  

	private DataQueue queue = null;
	private OutputCollector collector = null;
	
	public BoltWorker(DataQueue queue, OutputCollector collector){
		this.queue = queue;
		this.collector = collector;
	}
	
	@Override
	public void run() {
		if(queue.lock.tryLock()){
			queue.status = DataQueue.RUNNING;
			try{
				DataCell cell = null;
				do{
					cell = queue.databackend.poll(100, TimeUnit.MILLISECONDS);
					if(cell != null){
						processDataCell(cell);
					}
				}while(cell != null);
				synchronized(queue){
					queue.notifyAll();
				}
			}catch(Throwable e){
				log.error(e.toString(), e);
			}finally{
				queue.status = DataQueue.NO_START;
				queue.lock.unlock();
			}
		}
	}
	
	protected void processDataCell(DataCell data){
		for(DataBolt bolt: queue.bolts){
			try{
				bolt.execute(data, collector);
			}catch(Throwable e){
				log.warn(e.toString(), e);				
			}
		}
	}

}
