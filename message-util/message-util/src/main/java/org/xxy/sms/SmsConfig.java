package org.xxy.sms;

import java.io.Serializable;

public class SmsConfig implements Serializable {

	private static final long serialVersionUID = -6299863613290672674L;
	
	private String appid;
	
	private String appkey;
	
	private String signName;
	
	private String tempId;
	
	private int smsType;


	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getSignName() {
		return signName;
	}

	public void setSignName(String signName) {
		this.signName = signName;
	}

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}
	
	public int getSmsType() {
		return smsType;
	}

	public void setSmsType(int smsType) {
		this.smsType = smsType;
	}
}
