package com.taodian.monitor.storm;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理过程中，被处理的最小数据单元。
 * 
 * @author deonwu
 */
public class DataCell {
	private Map<String, Object> data = new HashMap<String, Object>();
	public Object get(String field){
		return data.get(field);
	}
	
	public void set(String field, Object obj){
		data.put(field, obj);
	}
}
