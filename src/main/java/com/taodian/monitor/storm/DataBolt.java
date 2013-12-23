package com.taodian.monitor.storm;

public interface DataBolt {

	/**
	 * 准备开始处理数据。需要做一些数据初始化操作。
	 */
	public void prepare(TopologyContext context);
	
	
	/**
	 * 处理一个数据元素。
	 * 
	 * @param data
	 */
	public void execute(DataCell data, OutputCollector output);

	/**
	 * 系统推出时调用。
	 */
	public void shutdown();

}
