package com.taodian.monitor.storm;

/**
 * Spout&Blot 工厂，根据一个名字，创建一个Spout 或者Blot。
 * @author deonwu
 *
 */
public interface ItemFactory {
	
	public Object create(String name);
}
