package com.taodian.monitor.bolt;

import com.taodian.monitor.core.DataService;
import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.TopologyContext;

public abstract class AbstractClickMonitorBolt implements DataBolt{
	protected DataService ds = null;
	
	public void setDataService(DataService s){
		this.ds = s;
	}
	
	@Override
	public void prepare(TopologyContext context) {
		
	}


}
