package com.taodian.monitor.bolt.alarm;

import java.io.File;
import java.io.PrintWriter;

import com.taodian.monitor.Settings;
import com.taodian.monitor.model.ClickAlarm;
import com.taodian.monitor.storm.OutputCollector;
import com.taodian.monitor.storm.TopologyContext;

/**
 * 把告警信息，写到一个文件里面，主要是方便做测试。
 * 
 * @author deonwu
 *
 */
public class WriteAlarmToFileBolt extends BaseAlarmBolt {
	
	private PrintWriter writer = null;
	@Override
	public void prepare(TopologyContext context) {
		super.prepare(context);
		
		String file = Settings.getString(Settings.ALARM_WRITE_TO_FILE, "");
		if(file != null && file.length() > 0){
			File f = new File(file);
			if(f.isFile()){
				f.delete();
			}
			try {
				writer = new PrintWriter(file, "utf8");
			} catch (Exception e) {
				log.warn("failed to create file:" + file, e);
			}
		}
	}
	
	@Override
	protected void alarm(ClickAlarm alarm, OutputCollector output) {
		if(writer != null){
			String f = String.format("%s %s %s", alarm.alaramID, alarm.userId, alarm.desc);
			writer.println(f);
			writer.flush();
		}
	}

}
