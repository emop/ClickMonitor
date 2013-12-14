package com.taodian.monitor.bolt.cpc;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.taodian.monitor.bolt.BaseClickMonitor;
import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.model.ClickAlarm;
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
		Transaction t = ds.multi();
		
		//保存所有浏览器。
		String lb = "last_browser_" + obj.userId;	
		t.lpush(lb, obj.browserName);
		t.ltrim(lb, 0, 500);

		//最近访问IP
		String lip = "last_ip_" + obj.userId;	
		t.lpush(lip, obj.ip);
		t.ltrim(lip, 0, 500);

		//访问设备
		String ldevice = "last_device_" + obj.userId;	
		t.lpush(ldevice, obj.deviceName);
		t.ltrim(ldevice, 0, 500);

		//访问者Agent
		String lagent = "last_agent_" + obj.userId;	
		t.lpush(lagent, obj.agentHash);
		t.ltrim(lagent, 0, 500);
		
		
		Response<List<String>> browsers = t.lrange(lb, 0, 100);
		Response<List<String>> ips = t.lrange(lip, 0, 100);
		Response<List<String>> devices = t.lrange(ldevice, 0, 100);		
		Response<List<String>> agents = t.lrange(lagent, 0, 100);		

		t.exec(); 
		
		ST sBrowsers = statistics(obj.browserName, browsers.get());
		if(sBrowsers.count > 10){
			ST sIps = statistics(obj.ip, ips.get());
			ST sDevices = statistics(obj.deviceName, devices.get());
			ST sAgents = statistics(obj.agentHash, devices.get());
			
			alarmMonitor(output, obj, sBrowsers, sIps, sDevices, sAgents);
		}
	}
	
	protected void raiseAlarm(OutputCollector output, int id, int userId, String desc){
		DataCell cell = new DataCell();
		cell.set(ClickMonitor.DATA_ALARM, new ClickAlarm(id, userId, desc));
		output.emit(ClickMonitor.MQ_ALARM, cell);		
	}
	
	private void alarmMonitor(OutputCollector output, ShortUrlModel obj, ST browser, ST ip, ST devices, ST agents){
		List<ClickAlarm> alarm = null;
		if(devices.count > 10){
			if(devices.cons / devices.count > 0.50){
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "连续相同设备，超过最近访问的50%");
			}else if(devices.hit / devices.count > 0.90) {
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "单一设备，访问超过90%");
			}
		}
		if(ip.count > 10){
			if(ip.cons / ip.count > 0.30){
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "连续相同IP，超过最近访问的30%");
			}else if(ip.hit / ip.count > 0.30) {
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "相同IP来源，访问超过0.3%");				
			}
		}

		if(agents.count > 10){
			if(agents.cons / agents.count > 0.30){
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "连续相同Agent，超过最近访问的30%");
			}else if(agents.hit / agents.count > 0.30) {
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "相同Agent，访问超过0.3%");				
			}
		}
		
		if(browser.count > 10 && obj.deviceType == ShortUrlModel.DEVICE_PC){
			if(browser.cons / browser.count > 0.30){
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "连续相同浏览器，超过最近访问的30%");
			}else if(browser.hit / browser.count > 0.80) {
				raiseAlarm(output, ClickAlarm.SAME_CLIENT, obj.userId, "相同浏览器，访问超过80%");				
			}
		}
	}
	
	private ST statistics(String sample, List<String> data){
		ST s = new ST();
		s.count = data.size();
		boolean stoped = false;
		for(String l : data){
			if(l.equals(sample)){
				s.hit++;
				if(!stoped){
					s.cons++;
				}
			}else {
				stoped = true;
			}
		}
		
		return s;
	}
	
	
	class ST{
		//连续多少个相同的访问。
		public int cons;
		//总共相同
		public int hit;
		//所有统计数据。
		public int count;
	}
	
}
