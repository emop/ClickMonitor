package com.taodian.monitor.bolt.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.taodian.monitor.bolt.AbstractClickMonitorBolt;
import com.taodian.monitor.bolt.BaseClickMonitor;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;


/**
 * 记录用户的点击详情，
 * 
 * 1. visitor_user --> [推广者,]
 * 2. visitor_short --> [短网址,]
 * 3. visitor_ip --> [短网址,]
 * 4. visitor_time --> [访问时间,]
 */
public class UserAccessLogBlot extends UserBaseBlot {
	//过期时间为6个月。
	private static int EXPIRED_TIME = 60 * 60 * 24 * 30 * 6;

	@Override
	protected void check(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Transaction t = ds.multi();
		//最后访问用户列表。
		String lvl = "last_visitor_list";	
		t.lpush(lvl, obj.uid + "");
		t.ltrim(lvl, 0, 10000);
		
		//用户的推广者
		String vu = "visitor_user_" + obj.uid;	
		Response<Long> isNew =  t.lpush(vu, obj.userId + "");
		t.ltrim(vu, 0, 500);

		//最近访问IP
		String vs = "visitor_short_" + obj.uid;	
		t.lpush(vs, obj.shortKey);
		t.ltrim(vs, 0, 500);

		//访问设备
		String vip = "visitor_ip_" + obj.uid;	
		t.lpush(vip, obj.ip);
		t.ltrim(vip, 0, 500);

		//访问者Agent
		String vt = "visitor_time_" + obj.uid;	
		t.lpush(vt, timeFormate.format(obj.clickTime));
		t.ltrim(vt, 0, 500);
		t.exec(); 
		
		if(isNew != null && isNew.get() <= 1){
			Transaction t2 = ds.multi();
			
			t2.expire(vu, EXPIRED_TIME);
			t2.expire(vs, EXPIRED_TIME);
			t2.expire(vip, EXPIRED_TIME);
			t2.expire(vt, EXPIRED_TIME);			
			t2.exec();
		}
	}

}
