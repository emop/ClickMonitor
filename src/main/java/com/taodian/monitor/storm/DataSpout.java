package com.taodian.monitor.storm;

/**
 * 数据输入流
 * 
 * @author deonwu
 */
public interface DataSpout {
	
	public void prepare(TopologyContext context);
	public DataCell nextDataCell();
	
	public boolean isClosed();
}
