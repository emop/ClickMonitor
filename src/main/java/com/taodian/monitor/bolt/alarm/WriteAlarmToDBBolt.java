package com.taodian.monitor.bolt.alarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.taodian.api.TaodianApi;
import com.taodian.emop.http.HTTPResult;
import com.taodian.monitor.Settings;
import com.taodian.monitor.model.ClickAlarm;
import com.taodian.monitor.storm.OutputCollector;
import com.taodian.monitor.storm.TopologyContext;
import com.taodian.monitor.storm.utils.CacheApi;
import com.taodian.monitor.storm.utils.SimpleCacheApi;

/**
 * 把告警信息，写到一个文件里面，主要是方便做测试。
 * 
 * @author deonwu
 *
 */
public class WriteAlarmToDBBolt extends BaseAlarmBolt {
	
	//private PrintWriter writer = null;
	protected TaodianApi api = null;
	private CacheApi cache = new SimpleCacheApi();
	@Override
	public void prepare(TopologyContext context) {
		super.prepare(context);
		
		if(Settings.getString("save_to_db", "n").equals("y")){
			api = dsPool.getTaodianApi();
		}
		/*
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
		*/
	}
	
	@Override
	protected void alarm(ClickAlarm alarm, OutputCollector output) {
		//String f = String.format("%s %s %s", alarm.alaramID, alarm.userId, alarm.desc);
		//log.info(f);
		if(api == null){
			return;
		}
		String ck = alarm.userId + "_" + alarm.alaramID;
		DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd");
		
		//查看缓存中是否已经写过相同的告警，1分钟内告警不重复。
		if(cache.get(ck) == null){
			String sql = "insert into cpc_alarm_user_event(user_id, alarm_id, alarm_date, alarm_times, comment, update_time, create_time)" +
					"values(%s, %s, '%s', %s, '%s', now(), now())" +
					"ON DUPLICATE KEY UPDATE alarm_times=alarm_times+1, update_time=values(update_time)";
			sql = String.format(sql, alarm.userId, alarm.alaramID, timeFormate.format(alarm.created), 1, alarm.desc);
			log.debug("save alarm:" + sql);
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("db_name", "click_report");
			param.put("sql", sql);
			
			HTTPResult r = api.call("data_run_sql", param);
			if(r.isOK){
				cache.set(ck, ck, 60);
			}else {
				log.warn("save alarm error:" + r.errorMsg);
			}
		}else {
			log.debug("the alarm is save in 1 min, ck:" + ck);
		}
	}

}
