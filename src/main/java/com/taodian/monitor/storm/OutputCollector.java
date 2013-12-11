package com.taodian.monitor.storm;

/**
 *
 * @author deonwu
 *
 */
public interface OutputCollector {
	
	/**
	 * 提交一个数据，到处理队列。
	 * @param name -- 队列名字
	 * @param cell -- 需要处理的数据
	 */
	public void emit(String name, DataCell cell);
}
