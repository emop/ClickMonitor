package com.taodian.monitor.bolt;

import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;
import com.taodian.monitor.storm.TopologyContext;

public class SimpleCount implements DataBolt {



	@Override
	public void execute(DataCell data, OutputCollector output) {
		
	}

	@Override
	public void prepare(TopologyContext context) {
		// TODO Auto-generated method stub
		
	}


}
