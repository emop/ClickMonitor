package com.taodian.monitor.core;

/**
 * 监控服务
 * @author deonwu
 *
 */

public class ClickMonitor {
	private static ClickMonitor ins = null;
	
	public static synchronized ClickMonitor getInstance(){
		if(ins == null){
			ins = new ClickMonitor();
		}
		return ins;
	}

	/**
	 * 服务开始启动，
	 * 0. 初始化Redis数据库连接。
	 * 1. 加载所有blot，初始化状态数据。
	 * 2. 加载spout 开始处理数据
	 */
	public void start(){
		
	}
	
	/**
	 * 
	 */
	public void shutdown(){
		
	}
	
	public boolean isCommandLineMode(){
		return true;
	}
	
	/**
	 * 等待所有的Spout结束，用在分析单个文件的时候。等待所有日志处理完成。
	 */
	public void waitAllSpoutDone(){
		
	}

}
