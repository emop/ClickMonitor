package com.taodian.monitor.bolt;

import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;
import com.taodian.monitor.storm.TopologyContext;

/**
 * 1.  最近日志分析
 * 2.  历史结合分析
 * 3.  规则参数分析
 */
public class SimpleCount implements DataBolt {



	@Override
	public void execute(DataCell data, OutputCollector output) {
		
	}

	@Override
	public void prepare(TopologyContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}


}
