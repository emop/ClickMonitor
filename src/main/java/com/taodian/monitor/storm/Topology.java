package com.taodian.monitor.storm;

import java.util.List;


public interface Topology {
	
	/**
	 * 设置一个输入数据源，
	 * 
	 * @param name 输入数据源的名字。
	 * @param spout， 数据源
	 */
	public void setSpout(String name, DataSpout spout, String output);

	public void setSpout(String name, DataSpout spout);

	
	/**
	 * 设置一个输入数据源，
	 * 
	 * @param name 数据来源
	 * @param bolt，
	 * @param output -- 输出的消息队列。
	 */
	public void setBolt(String from, DataBolt bolt, String output);
	
	/**
	 * 开始运行
	 */
	public void start();
	
	public List<DataQueue> getAllDataQueue();
}
