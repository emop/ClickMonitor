package com.taodian.monitor.storm.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taodian.monitor.storm.DataBolt;
import com.taodian.monitor.storm.DataSpout;
import com.taodian.monitor.storm.ItemFactory;
import com.taodian.monitor.storm.Topology;
import com.taodian.monitor.storm.TopologyBuilder;

/**
 * 根据文本输入流配置拓扑。
 * @author deonwu
 */
public class SimpleTopologyBuilder implements TopologyBuilder{
	private Log log = LogFactory.getLog("storm.builder");  
	
	private BufferedReader reader = null;
	public SimpleTopologyBuilder(InputStream ins){
		try {
			reader = new BufferedReader(new InputStreamReader(ins, "utf8"));
		} catch (UnsupportedEncodingException e) {
			log.error(e, e);
		}
	}

	@Override
	public void buildToplogy(Topology topology, ItemFactory factory){
		try{
			log.info("-------Read topology configuration-------");
			for(String r = reader.readLine(); r != null; r = reader.readLine()){
				r = r.trim();
				if(r.startsWith("#") || r.length() == 0) continue;
				String[] tmp = r.split("=", 2);
				log.info(r);
				if(tmp.length == 2){
					String[] x = tmp[1].split(",", 2);
					Object obj = factory.create(x[0].trim());
					String p = x.length == 2 ? x[1] : "";
					if(obj instanceof DataSpout){
						topology.setSpout(tmp[0].trim(), (DataSpout)obj, p);
					}else if(obj instanceof DataBolt){
						topology.setBolt(tmp[0].trim(), (DataBolt)obj, p);
					}
				}
			}
		}catch(IOException  e){
			log.error(e, e);
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
