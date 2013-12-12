package com.taodian.monitor.bolt.cpc;

import redis.clients.jedis.Jedis;

import com.taodian.monitor.bolt.AbstractClickMonitorBolt;
import com.taodian.monitor.bolt.BaseClickMonitor;
import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 最近访问行为分析：
 * 
 * 1. 最近访问的浏览器。
 * 2. 最近访问的IP
 * 3. 最近访问的设备，
 * 
 * @author deonwu
 *
 */
public class LastAccessMonitor extends BaseClickMonitor {

	@Override
	protected void check(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		this.checkBrowser(obj, ds, output);
		this.checkAccessIp(obj, ds, output);
		this.checkAccessDevice(obj, ds, output);
	}
	
	protected void checkBrowser(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		String ck = "last_browser_" + obj.userId;
		
		log.info("ck:" + ck  + ", n:" + obj.browserName);
	}

	protected void checkAccessIp(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		String ck = "last_ip_" + obj.userId;
		
	}

	protected void checkAccessDevice(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		
	}	
		
}
