package com.taodian.monitor.storm;

/**
 * 消息处理过程中，被处理的最小数据单元。
 * 
 * @author deonwu
 */
public class DataCell {
	
	public Object get(String field){
		return field;
	}
	
	public void set(String field, Object obj){
		
	}
}
