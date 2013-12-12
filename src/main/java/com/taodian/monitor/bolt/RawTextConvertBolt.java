package com.taodian.monitor.bolt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taodian.monitor.core.ClickMonitor;
import com.taodian.monitor.model.ShortUrlModel;
import com.taodian.monitor.storm.DataCell;
import com.taodian.monitor.storm.OutputCollector;

/**
 * 把一行行的日志字符传分解，为点击统计的ShortUrlModel，或者是新用户事件，就创建用户对象。
 *
 */
public class RawTextConvertBolt extends AbstractClickMonitorBolt {
	private Pattern clickLog = Pattern.compile("([0-9\\-]+\\s[0-9:]+)\\s(\\w+)\\s(\\d+)\\s([\\d\\.]+)\\s\\[([^\\]]+)\\](.*)");
	//12-12 12:53:09 8MMr3 39893389498 115.194.90.124 [Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1]
	//private static DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void execute(DataCell data, OutputCollector output) {
		// TODO Auto-generated method stub
		String raw = data.get(ClickMonitor.DATA_RAW) + "";
		
		//log.warn("click raw:" + raw);		
		Matcher ma =clickLog.matcher(raw);
		if(ma != null && ma.find()){
			ShortUrlModel model = ds.getShortUrl(ma.group(2));
			if(model != null){
				model.shortKey = ma.group(2);
				model.uid = ma.group(3);
				model.ip = ma.group(4);
				model.agent = ma.group(5);
				model.refer = ma.group(6);
				
				DateFormat timeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = ma.group(1);
				if(time.length() < 19){
					time = "2013-" + time;
				}
				try {
					model.clickTime = timeFormate.parse(time);
				} catch (ParseException e) {
					log.warn("parse error:" + time + ", exception:" + e, e);
				}
				
				data.set(ClickMonitor.MQ_SHORT_URL, model);
				output.emit(ClickMonitor.MQ_CLICK_LOG, data);
			}else {
				log.warn("Not found short url:" + ma.group(2));				
			}
		}		

	}


}
