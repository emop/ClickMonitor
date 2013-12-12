package com.taodian.monitor.bolt;

import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 用户点击来源分析，
 * 1. 是否有连续的相同地址。
 * 
 * @author deonwu
 *
 */
public class ClickSourceMonitor extends AbstractClickMonitorBolt {

	@Override
	public void execute(DataCell data, OutputCollector output) {
		Object m = data.get(ClickMonitor.DATA_SHORT_URL);
		if(m == null) return;
		
		ShortUrlModel obj = (ShortUrlModel)m;
		
		log.info("access user:" + obj.uid + ", ip:" + obj.ip + ", shop id:" + obj.shopId + ", num id:" + obj.numIid);
	}

}
