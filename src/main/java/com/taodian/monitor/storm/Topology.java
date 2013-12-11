package com.taodian.monitor.storm;


public interface Topology {
	
	/**
	 * 设置一个输入数据源，
	 * 
	 * @param name 输入数据源的名字。
	 * @param spout， 数据源
	 */
	public void setSpout(String name, DataSpout spout);

	
	/**
	 * 设置一个输入数据源，
	 * 
	 * @param name 数据来源
	 * @param bolt，
	 */
	public void setBolt(String from, DataBolt bolt);
}
