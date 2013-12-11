package com.taodian.monitor.storm.impl;

import java.io.InputStream;

import com.taodian.monitor.storm.ItemFactory;
import com.taodian.monitor.storm.Topology;
import com.taodian.monitor.storm.TopologyBuilder;

/**
 * 根据文本输入流配置拓扑。
 * @author deonwu
 */
public class SimpleTopologyBuilder implements TopologyBuilder{
	public SimpleTopologyBuilder(InputStream ins){
		
	}

	@Override
	public void buildToplogy(Topology topology, ItemFactory factory) {
		
	}

}
