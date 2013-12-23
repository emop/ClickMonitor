package com.taodian.monitor.bolt.alarm;

import java.net.MalformedURLException;
import java.net.URL;
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
 * 修改ClickGate的路由规则，忽略掉和推广者相关的点击。如果推广者出现告警信息。
 * 
 * @author deonwu
 *
 */
public class UpdateRouteTableBolt extends BaseAlarmBolt {
	
	//private PrintWriter writer = null;
	protected TaodianApi gateApi = null;
	private CacheApi cache = new SimpleCacheApi();
	@Override
	public void prepare(TopologyContext context) {
		super.prepare(context);
		
		if(Settings.getString("ignore_alarm_user", "n").equals("y")){
			String appKey = Settings.getString(Settings.TAODIAN_APPID, null); // System.getProperty("");
			String appSecret = Settings.getString(Settings.TAODIAN_APPSECRET, null);
			String appRoute = Settings.getString(Settings.CLICK_GATE_LOG_URL, "http://127.0.0.1:8082");			
			if(appKey != null && appSecret != null){
				try {
					URL u = new URL(appRoute);
					appRoute = "http://" + u.getHost();
					if(u.getPort() > 0 && u.getPort() != 80){
						appRoute += ":" + u.getPort();
					}
					appRoute += "/api";
				} catch (MalformedURLException e) {
				}
				log.debug("Connect click gate api route:" + appRoute);
				gateApi = new TaodianApi(appKey, appSecret, appRoute, "simple");		
			}
		}
		
	}
	
	@Override
	protected void alarm(ClickAlarm alarm, OutputCollector output) {
		
		log.debug("Connect click gate api route:" + gateApi);
		
		if(gateApi == null){
			return;
		}
		String ck = alarm.userId + "_" + alarm.alaramID;
		
		//查看缓存中是否已经写过相同的告警，1分钟内告警不重复。
		if(cache.get(ck) == null){
			String route = "route -A cpc -user_id " + alarm.userId + " -j ignore -expire 15mins";
			log.debug("update gate route:" + route);
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("route", route);
			
			HTTPResult r = gateApi.call("updateGateRoute", param);
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
