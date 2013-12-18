package com.taodian.monitor.bolt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taodian.api.TaodianApi;
import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.model.WeiboVisitor;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 把一行行的日志字符传分解，为点击统计的ShortUrlModel，或者是新用户事件，就创建用户对象。
 *
 */
public class RawTextConvertBolt extends AbstractClickMonitorBolt {
	private Pattern isMobile = Pattern.compile("Mobile|iPhone|Android|WAP|NetFront|JAVA|OperasMini|UCWEB|WindowssCE|Symbian|Series|webOS|SonyEricsson|Sony|BlackBerry|Cellphone|dopod|Nokia|samsung|PalmSource|Xphone|Xda|Smartphone|PIEPlus|MEIZU|MIDP|CLDC", Pattern.CASE_INSENSITIVE);

	private Pattern clickLog = Pattern.compile("([0-9\\-]+\\s[0-9:]+)\\s(\\w+)\\s(\\d+)\\s([\\d\\.]+)\\s\\[([^\\]]+)\\](.*)");

	//12-12 12:53:09 8MMr3 39893389498 115.194.90.124 [Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1]
	//private static DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Pattern newUser = Pattern.compile("new_uid:([0-9]+),mobile:(\\w+),ip:([\\d\\.]+),host:([\\.\\w]+),agent:\\[([^\\]]+)\\],created:([0-9\\-]+\\s[0-9:]+)");
	//new_uid:25320970001,mobile:false,ip:127.0.0.1,host:127.0.0.1,agent:[Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36],created:2013-12-17 23:02:12
	
	@Override
	public void execute(DataCell data, OutputCollector output) {
		// TODO Auto-generated method stub
		String raw = data.get(ClickMonitor.DATA_RAW) + "";
		
		//log.warn("click raw:" + raw);		
		Matcher ma =clickLog.matcher(raw);
		DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(ma != null && ma.find()){
			ShortUrlModel model = dsPool.getShortUrl(ma.group(2));
			if(model != null){
				model.shortKey = ma.group(2);
				model.uid = ma.group(3);
				model.ip = ma.group(4);
				model.agent = ma.group(5);
				model.refer = ma.group(6);
				
				String time = ma.group(1);
				if(time.length() < 19){
					time = "2013-" + time;
				}
				try {
					model.clickTime = timeFormate.parse(time);
				} catch (ParseException e) {
					log.warn("parse error:" + time + ", exception:" + e, e);
				}
				
				if(model.refer != null){
					model.referHash = TaodianApi.MD5(model.refer);
				}
				if(model.agent != null){
					model.agentHash = TaodianApi.MD5(model.agent);
				}
				
				model.visitor = dsPool.getWeiboVisitor(Long.parseLong(model.uid));
				if(model.visitor != null && model.visitor.noSave){
					model.visitor.isFirst = true;
					dsPool.saveWeiboVisitor(model.visitor);
					
					data.set(ClickMonitor.MQ_NEW_VISITOR, model.visitor);
					output.emit(ClickMonitor.MQ_NEW_VISITOR, data);
				}else if(model.visitor != null){
					model.visitor.isFirst = false;
				}
				
				data.set(ClickMonitor.MQ_SHORT_URL, model);
				
				insepctorAccessInfo(model);
				output.emit(ClickMonitor.MQ_CLICK_LOG, data);
				if(model.shortKeySource != null && model.shortKeySource.equals("cpc")){
					output.emit(ClickMonitor.MQ_CPC_LOG, data);					
				}
			}else {
				log.warn("Not found short url:" + ma.group(2));				
			}
		}else if(raw.startsWith("new_uid")) {
			ma = newUser.matcher(raw);
			if(ma != null && ma.find()){
				WeiboVisitor visitor = new WeiboVisitor();
				
				visitor.uid = Long.parseLong(ma.group(1));
				visitor.isMobile = Boolean.parseBoolean(ma.group(2));
				visitor.ip = ma.group(3);
				visitor.host = ma.group(4);
				visitor.agent = ma.group(5);
				try {
					visitor.created = timeFormate.parse(ma.group(6));
				} catch (ParseException e) {
				}
				
				log.debug("create new user:" + visitor.uid);
				dsPool.newWeiboVisitor(visitor);
				
				data.set(ClickMonitor.MQ_NEW_VISITOR, visitor);
				output.emit(ClickMonitor.MQ_NEW_VISITOR, data);					
			}else {
				log.warn("Failed to parse user log:" + raw);
			}
		}

	}

	private void insepctorAccessInfo(ShortUrlModel model){
		String agent = model.agent;
		if(agent == null || agent.trim().length() == 0){
		}
		
		if(agent.contains("iPad")){
			model.deviceType = 1;
			model.deviceName = "iPad";
		}else if(agent.contains("iPhone")){
			model.deviceType = 2;
			model.deviceName = "iPhone";
		}else if(agent.contains("Android")){
			model.deviceType = 3;
			model.deviceName = "Android";
		}else if(isMobile.matcher(agent).find()){
			model.deviceType = 4;
			model.deviceName = "OtherMobile";
		}else {
			model.deviceType = 9;
			model.deviceName = "PC";			
		}
		
		String[] names = new String[]{
		"firefox", "msie", "opera", "chrome", "safari",
        "mozilla", "seamonkey",    "konqueror", "netscape",
        "gecko", "navigator", "mosaic", "lynx", "amaya",
        "omniweb", "avant", "camino", "flock", "aol"};
		
		for(String n : names) {
			String reg = "(" + n +"[/ ]?([0-9.]*)?)";
			Pattern pa = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
			Matcher ma = pa.matcher(agent);
			if(ma.find()){
				model.browserName = ma.group(1);
				break;
			}
		}
	}
}
