package com.taodian.monitor.bolt.user;

import redis.clients.jedis.Jedis;

import com.taodian.monitor.bolt.AbstractClickMonitorBolt;
import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.core.DataService;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;

public abstract class UserBaseBlot extends AbstractClickMonitorBolt {

	@Override
	public void execute(DataCell data, OutputCollector output) {
		Object m = data.get(ClickMonitor.DATA_SHORT_URL);
		if(m == null) return;
		
		ShortUrlModel obj = (ShortUrlModel)m;
		Jedis d = this.dsPool.getJedis(DataService.DS_USER_LOG);
		try{
			check(obj, d, output);
		}finally{
			if(d != null){
				dsPool.releaseConn(d);
			}
		}
	}
	
	protected abstract void check(ShortUrlModel obj, Jedis ds, OutputCollector output);

}
