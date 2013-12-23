package com.taodian.monitor.bolt.cpc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.taodian.monitor.bolt.BaseClickMonitor;
import com.taodian.monitor.bolt.cpc.LastAccessMonitor.ST;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 
 * @author lili
 * 每半个小时内点击分布情况
 * 1.不同设备点击分布
 */ 
public class CPCUserHourAccessMonitor extends BaseClickMonitor{
	
//	private static int EXPIRED_TIME = 60 * 60 * 24 * 30 * 6;

	@Override
	protected void check(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		// TODO Auto-generated method stub
		DateFormat MinFormate = new SimpleDateFormat("yyMMddHH");
		DateFormat dayFormate = new SimpleDateFormat("yyMMdd");
		
		String time=MinFormate.format(obj.clickTime);
		
		int minutes = obj.clickTime.getMinutes();
		if(minutes < 30){
			time += "00";
		}else {
			time += "30";
		}

		Transaction t = ds.multi();
		
		String day=dayFormate.format(obj.clickTime);
		String outKey = "";
		outKey = String.format("user_%s_%s", obj.userId, day);

		String innerKey = time+"_"+obj.deviceName;
		t.incr(innerKey);
		
		t.lpush(outKey, innerKey);
		t.ltrim(outKey, 0, 500);
		t.exec(); 
//		t.expire(outKey, EXPIRED_TIME);


	}
	


}
