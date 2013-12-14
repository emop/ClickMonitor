package com.taodian.monitor.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 点击的短网址相关的信息。 用在做点网址点击统计，里面包含了所有统计需要用到的信息。
 * <br/>
 * 字段分为2大类：<br/>
 * 	a. 短网址的静态信息，例如：短网址的淘客用户ID，商品ID，冒泡库ID<br/>
 *  b. 本次点击的动态数据，例如：浏览器agent, 访客ID, 客户端IP 等。 <br/>
 * 
 * 
 * @author deonwu
 */
public class ShortUrlModel implements Serializable, Cloneable{
	public static final int DEVICE_PC = 9;
	public static final int DEVICE_IPAD = 1;
	public static final int DEVICE_IPHONE = 2;
	public static final int DEVICE_ANDROID = 3;
	public static final int DEVICE_OTHER_MOBILE = 4;	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6956443049935100082L;
	
	/**
	 * 点击事件ID，用来分区一次点击操作。根据时间戳和访客ID生成。在日志详情记录里面可以做唯一主键。
	 */
	public long clickId = 0;
	
	/**
	 * 访客ID 的数字型
	 */
	public long numUid = 0;
	
	/**
	 * 字符型访客ID， 访客ID是为每一个访问用户分配的ID。保存在访问者的浏览器Cookie里面。用来
	 * 区分跟踪一个用户。
	 * 
	 * ID本身是一个long 数字。为了一些统计方便保存了一个字符型字段。
	 */
	public String uid = null;
	
	/**
	 * 点击的浏览器，User-agent, 包含浏览器类型等信息。
	 */
	public String agent = null;
	
	/**
	 * 淘宝店铺ID，普通微博推广商品时，店铺ID可能为空。
	 */
	public long shopId = 0;
	
	/**
	 * 淘宝商品ID，如果是店铺推广链接的话，这个字段为0.
	 */
	public long numIid = 0;
	
	/**
	 * 生成短链接的应用ID， 例如：冒泡网-2, 45min-12. 如果要跟踪第三方开放客户端的点击，可以根据
	 * 这个ID 统计。
	 * 
	 */
	public int appId = 0;
	
	/**
	 * 淘客的userId，这个ID 是每个appId内唯一。
	 */
	public int userId = 0;
	
	/**
	 * 短网址如果是活动的话，代表活动主题ID
	 */
	public int topicId = 0;

	/**
	 * 点击访问的浏览器类型，1:iPad， 2:iPhone, 3:Android, 4:其他手机, 9:PC， 0：未知
	 */
	public int deviceType = 0;
	public String deviceName = "";
	public String browserName = "";
	
	public String agentHash = "";
	public String referHash = "";
	
	/**
	 * 冒泡网的，文案库ID。
	 */
	public int libId = 0;
	/**
	 * 微博ID
	 */
	public String outId = null;
	public String platform = null;
	
	/**
	 * 点击的客户端IP，例如：104.11.20.11
	 */
	public String ip = null;
	
	
	/**
	 * 点击的短网址，14位以内的字符串。
	 */
	public String shortKey = null;

	/**
	 * 点击的来源，例如：http://t.qq.com/q879779218。 如果点击经过多次跳转，就不准确了，
	 * 例如新浪微博的来源都是：http://h2w.iask.cn/jump.php?url=http://wap.emop.cn/c/s7ce147b
	 */
	public String refer = null;
	
	public String shortKeySource = null;
	
	public boolean isMobile=false;
	
	/**
	 * 长地址
	 */
	public String longUrl = null;

	/**
	 * 移动版长地址
	 */
	public String mobileLongUrl = null;
	
	
	public Date clickTime = new Date(System.currentTimeMillis());

	public int hashCode(){
		return shortKey != null ? (int)shortKey.hashCode() : 0;
	}
	
	public boolean equals(ShortUrlModel o){
		if(o instanceof ShortUrlModel){
			return o.hashCode() == this.hashCode();
		}else {
			return false;
		}
	}
	
	public ShortUrlModel copy(){
		ShortUrlModel m = null;
		try {
			m = (ShortUrlModel) this.clone();
		} catch (CloneNotSupportedException e) {
		}
		
		return m;
	}
}
