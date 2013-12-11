package com.taodian.monitor.model;

import java.io.Serializable;

public class ShopAccount implements Serializable{

	public String status = "";
	public long shopId;
	public float banlance = 0;
	public long lastRefreshTime = 0;
	public long created = System.currentTimeMillis();
	
	public int hashCode(){
		return (int)shopId;
	}
	
	public boolean equals(Object o){
		if(o instanceof ShopAccount){
			return o.hashCode() == this.hashCode();
		}else {
			return false;
		}
	}	
}
