package com.taodian.monitor.storm.utils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SimpleCacheApi implements CacheApi {
	private Map<String, CacheItem> cache = new HashMap<String, CacheItem>();
	private Lock l = new ReentrantLock();
	private long lastCleanUp = System.currentTimeMillis();
	private CacheStat stat = new CacheStat();

	public void set(String key, Object obj, int expired) {
		CacheItem item = new CacheItem();
		stat.item_set_count++;
		
		item.ref = new SoftReference<Object>(obj);
		item.expiredTime = expired * 1000;
		cache.put(key, item);
	}

	@Override
	public Object get(String key) {
		return get(key, false);
	}

	public Object get(String key, boolean update) {
		stat.get_count++;

		if (System.currentTimeMillis() - lastCleanUp > 1000 * 30) {
			lastCleanUp = System.currentTimeMillis();
			new Thread(){
				public void run(){
					if(l.tryLock()){
						try{
							cleanObject();
						}finally{
							l.unlock();
						}
					}
				}
			}.start();
		}

		CacheItem item = cache.get(key);
		if (item != null) {
			if (System.currentTimeMillis() - item.lastAccess < item.expiredTime) {
				if (update) {
					item.lastAccess = System.currentTimeMillis();
				}
				stat.hit_count++;
				return item.ref.get();
			} else {
				cache.remove(key);
			}
		}
		stat.hit_failed_count++;
		return null;
	}

	@Override
	public boolean remove(String key) {
		if (cache.containsKey(key)) {
			stat.item_rmeove_count++;
			cache.remove(key);
			return true;
		}
		return false;
	}

	@Override
	public boolean add(String key, Object data, int expired) {
		stat.item_add_count++;
		if (cache.containsKey(key)) {
			return false;
		} else {
			this.set(key, data, expired);
		}
		return true;
	}

	private synchronized void cleanObject() {
		Vector<String> keys = new Vector<String>();
		keys.addAll(cache.keySet());
		for (String key : keys) {
			CacheItem item = cache.get(key);
			if (item != null
					&& System.currentTimeMillis() - item.lastAccess > item.expiredTime) {
				cache.remove(key);
			}
		}
	}

	class CacheItem {
		SoftReference<Object> ref = null;
		long lastAccess = System.currentTimeMillis();
		long expiredTime = 0;
	}

	@Override
	public boolean cleanAll() {
		cache.clear();
		return true;
	}

	@Override
	public Map<String, Object> stat() {
		Map<String, Object> st = new HashMap<String, Object>();
		
		st.put("get_count", stat.get_count);
		st.put("hit_count", stat.hit_count);
		st.put("hit_failed_count", stat.hit_failed_count);
		st.put("item_count", cache.size());
		st.put("item_set_count", stat.item_set_count);
		st.put("item_add_count", stat.item_add_count);
		st.put("item_rmeove_count", stat.item_rmeove_count);
		
		return st;
	}
	
	/**
	 * 缓存统计报表。
	 * @author deonwu
	 *
	 */
	class CacheStat{
		public long get_count = 0;
		public long hit_count = 0;
		public long hit_failed_count = 0;
		public long item_count = 0;
		public long item_set_count = 0;
		public long item_add_count = 0;
		public long item_rmeove_count = 0;
	}

	@Override
	public List<String> keys() {
		List<String> keys = new ArrayList<String>();
		keys.addAll(cache.keySet());
		
		return keys;
	}
}
