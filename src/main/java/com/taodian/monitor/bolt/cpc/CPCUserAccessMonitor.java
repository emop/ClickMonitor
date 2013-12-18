package com.taodian.monitor.bolt.cpc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import redis.clients.jedis.Jedis;

import com.taodian.monitor.bolt.BaseClickMonitor;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 统计CPC 点击里面， 新用户，首次访问用户，老用户的点击比例。
 * 
 */
public class CPCUserAccessMonitor extends BaseClickMonitor {
	//过期时间为1个月。
	private static int EXPIRED_TIME = 60 * 60 * 24 * 30;
	
	@Override
	protected void check(ShortUrlModel obj, Jedis ds, OutputCollector output) {
		DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd");
		String date = timeFormate.format(obj.clickTime);
		
		if(obj.visitor == null) return;
		
		String ck = "";
		if(obj.visitor.isFirst){
			ck = String.format("user_first_%s_%s", obj.userId, date);
		}else if(obj.visitor.isNewUser()){
			ck = String.format("user_new_%s_%s", obj.userId, date);
		}else {
			ck = String.format("user_old_%s_%s", obj.userId, date);
		}
		
		/*
		String firstUser = String.format("user_first_%s_%s", obj.userId, date);
		String newtUser = String.format("user_new_%s_%s", obj.userId, date);
		String oldUser = String.format("user_old_%s_%s", obj.userId, date);
		*/		
		long result = ds.incr(ck);
		if(result <= 1){
			ds.expire(ck, EXPIRED_TIME);
		}
		if(log.isDebugEnabled()){
			log.debug("user access counter:" + ck + ", counter:" + result);
		}
	}

}
