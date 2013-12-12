package com.taodian.monitor.bolt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.core.DataService;
import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.TopologyContext;

public abstract class AbstractClickMonitorBolt implements DataBolt{
	//public static 
	//private Log log = LogFactory.getLog("click.mointor");  
	protected Log log = LogFactory.getLog("lm.mointor.bolt");  
	
	protected DataService dsPool = null;
	
	public void setDataService(DataService s){
		this.dsPool = s;
	}
	
	@Override
	public void prepare(TopologyContext context) {
		
	}


}
