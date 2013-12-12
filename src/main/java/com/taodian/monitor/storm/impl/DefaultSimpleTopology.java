package com.taodian.monitor.storm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.DataQueue;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.OutputCollector;
import com.taodian.monitor.storm.Topology;
import com.taodian.monitor.storm.TopologyContext;

public class DefaultSimpleTopology implements Topology {
	private Log log = LogFactory.getLog("topology");  

	protected ThreadPoolExecutor executor = null;
	private Map<String, DataQueue> queues = new ConcurrentHashMap<String, DataQueue>();
	private Map<DataSpout, List<String>> spouts = new HashMap<DataSpout, List<String>>();
	private TopologyContext context = null;
	
	public DefaultSimpleTopology(ThreadPoolExecutor executor){
		this.executor = executor;
	}
	
	@Override
	public void setSpout(String name, DataSpout spout) {
		setSpout(name, spout, "");
	}

	@Override
	public void setSpout(String name, DataSpout spout, String output) {
		List<String> queue = spouts.get(spout);
		if(queue == null){
			queue = new ArrayList<String>();
			spouts.put(spout, queue);
		}
		queue.add(name);
	}
	
	
	@Override
	public void setBolt(String from, DataBolt bolt, String output) {
		DataQueue q = queues.get(from);
		if(q == null){
			q = new DataQueue(from);
			queues.put(from, q);
		}
		q.addDataBolt(bolt, output);
	}

	@Override
	public void start() {
		context = new TopologyContext();
		
		ArrayList<DataBolt> bolts = new ArrayList<DataBolt>();
		for(DataQueue q: queues.values()){
			for(DataBolt b : q.bolts){
				if(bolts.contains(b))continue;
				b.prepare(context);
			}
		}
		for(Entry<DataSpout, List<String>> entry: spouts.entrySet()){
			//s.prepare(context);
			DataSpout s = entry.getKey();
			s.prepare(context);
			
			List<String> out = entry.getValue();
			executor.execute(new SpoutWorker(s, out, new QueueOutputCollector(out)));
		}
		
	}
	
	class QueueOutputCollector implements OutputCollector{
		private List<String> declaredOut = new ArrayList<String>();
		
		public QueueOutputCollector(List<String> declaredOut){
			this.declaredOut = declaredOut;
		}
		
		@Override
		public void emit(String name, DataCell cell) {
			DataQueue q = queues.get(name);
			if(q != null){
				q.addDataCell(cell);
				if(q.status == DataQueue.NO_START){
					q.status = DataQueue.SCHEDULED;
					executor.execute(new BoltWorker(q, new QueueOutputCollector(q.declaredOutput)));
				}else if(q.status == DataQueue.WAITING){
					synchronized(q){
						q.notifyAll();
					}
				}
			}else {
				log.error("queue '" + name + "' is not decleared.");
			}
		}
		
	}

	
	
}
