package com.taodian.monitor.storm.impl;

import java.util.concurrent.ThreadPoolExecutor;

import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.Topology;

public class DefaultSimpleTopology implements Topology {
	
	protected ThreadPoolExecutor executor = null;
	
	public DefaultSimpleTopology(ThreadPoolExecutor executor){
		this.executor = executor;
	}
	
	@Override
	public void setSpout(String name, DataSpout spout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBolt(String from, DataBolt bolt, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
