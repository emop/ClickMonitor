package com.taodian.monitor.bolt.alarm;

import com.taodian.monitor.bolt.AbstractClickMonitorBolt;
import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.model.ClickAlarm;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 告警流程的处理。
 *
 */
public abstract class BaseAlarmBolt extends AbstractClickMonitorBolt {

	@Override
	public void execute(DataCell data, OutputCollector output) {
		Object m = data.get(ClickMonitor.DATA_ALARM);
		if(m == null) return;
		
		ClickAlarm alarm = (ClickAlarm)m;
		String f = String.format("%s %s %s", alarm.alaramID, alarm.userId, alarm.desc);
		log.info(f);
		alarm(alarm, output);
	}
	
	protected abstract void alarm(ClickAlarm alarm, OutputCollector output);

}
