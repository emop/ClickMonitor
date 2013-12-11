package com.taodian.monitor.spout;

import java.io.File;

import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.TopologyContext;

public class FileDataSpout implements DataSpout {
	
	public void load(File f){
	}
	
	@Override
	public void prepare(TopologyContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataCell nextDataCell() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}



}
