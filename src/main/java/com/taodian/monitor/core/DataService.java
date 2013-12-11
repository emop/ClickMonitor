package com.taodian.monitor.core;

import redis.clients.jedis.Jedis;

import com.taodian.monitor.Settings;
import com.taodian.monitor.model.ShortUrlModel;

/**
 * 数据操作服务。
 * 
 * @author deonwu
 */
public class DataService {
	private Jedis jredis = null; //new JRedisClient(
	
	public void start(){
		jredis = new Jedis(Settings.getString("redis.host", "127.0.0.1"));
	}
	
	public ShortUrlModel getShortUrl(String key){
		return null;
	}
}
