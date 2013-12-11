package com.taodian.monitor.storm;


/**
 * 根据配置文件，组装一个消息处理拓扑结构。
 * @author deonwu
 */
public interface TopologyBuilder {
	public void buildToplogy(Topology topology, ItemFactory factory);
}
