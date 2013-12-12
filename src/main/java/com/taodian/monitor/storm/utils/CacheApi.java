package com.taodian.monitor.storm.utils;

import java.util.List;
import java.util.Map;

public interface CacheApi {
	public void set(String key, Object data, int expired);
	public Object get(String key);
    public Object get(String key, boolean update);

	public boolean remove(String key);
	public boolean cleanAll();
	public Map<String, Object> stat();
	
	public List<String> keys();
	
	public boolean add(String key, Object data, int expired);
	
}
