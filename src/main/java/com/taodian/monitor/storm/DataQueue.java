package com.taodian.monitor.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 一个消息处理队列。
 * @author deonwu
 */
public class DataQueue {
	private Log log = LogFactory.getLog("lm.data.queue");  
	
	/**
	 * 消息队列正在运行中。
	 */
	public static final int RUNNING = 1;
	/**
	 * 消息队列在等待新消息。
	 */
	public static final int WAITING = 2;
	
	/**
	 * 没有在运行。
	 */
	public static final int NO_START = 3;	
	public static final int	SCHEDULED = 4;	
	
	public String name = "";
	public Lock lock = new ReentrantLock();
	public int status = NO_START;
	
	public ArrayBlockingQueue<DataCell> databackend = new ArrayBlockingQueue<DataCell>(100);	
	public ArrayList<DataBolt> bolts = new ArrayList<DataBolt>();
	public List<String> declaredOutput = new ArrayList<String>();
	
	public DataQueue(String n){
		name = n;
	}
	
	public void addDataCell(DataCell c){
		for(int i = 0; i < 3; i++){
			if(databackend.remainingCapacity() > 0){
				databackend.add(c);
				break;
			}else {
				log.warn(String.format("The queue '%s' is full, sleep 1s waiting to avaliable.", name));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public void addDataBolt(DataBolt b){
		addDataBolt(b, "");
	}
	
	public void addDataBolt(DataBolt b, String declaredOutput){
		bolts.add(b);
		for(String out : declaredOutput.split(",")){
			if(out.trim().length() > 0){
				this.declaredOutput.add(out.trim());
			}
		}
	}
}
