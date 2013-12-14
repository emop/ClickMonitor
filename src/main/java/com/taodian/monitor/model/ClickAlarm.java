package com.taodian.monitor.model;

public class ClickAlarm {
	public static final int SAME_CLIENT = 10001;	
	
	public int alaramID;
	public String name;
	
	public int userId;
	public String desc;

	public ClickAlarm(int id, int userId, String desc){
		this.alaramID = id;
		this.userId = userId;
		this.desc = desc;
	}
}
