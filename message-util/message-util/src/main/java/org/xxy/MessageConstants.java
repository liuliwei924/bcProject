package org.xxy;



public class MessageConstants {
	
	/** 消息状态1 未查看 **/
	public static final int messageFlag_1 = 1;
	/** 消息状态1 已查看 **/
	public static final int messageFlag_2 = 2;
	/** 消息状态1 已删除 **/
	public static final int messageFlag_3 = 3;
	
	
	/** 发送消息配置的List ***/
	public static final String ConfigList="list";
	
	
	/** Email的队列 ***/
	public static final String KEY_QUEUE_EMAIL="queueWebEmail";
	/** SMS的队列 ***/
	public static final String KEY_QUEUE_SMS="queueWebSms";
	
	
	
	/** SMS的配置 ***/
	public static final String Key_configMessageKey ="configMessageKey";
	
	/** 微信模版消息配置 ***/
	public static final String Key_WXTemplateConfig = "WXTemplateConfigKey";
	
	/** 发送语音验证码标识 **/
	public static final String VALUE_MESSAGE_VOICE = "voice";

	/**是否发送语音验证码*/
	public static final String KEY_IS_VOICE = "isVoice";
	
	
	
	/**消息类型的关键字*/
	public static final String KEY_MESSAGE_TYPE = "messageType";
	
	/**发送SMS给电话*/
	public static final String KEY_CELL_PHONE = "cellPhone";
	
	/**发邮件的 主题*/
	public static final String KEY_messageTitle = "messageTitle";
	/**发邮件给 */
	public static final String KEY_EMAIL_TO = "to";
	
	
	/**发邮件的内容*/
	public static final String KEY_emailContent = "emailContent";
	
	/**短信的内容*/
	public static final String KEY_smsContent = "smsContent";
	
	/**站内信息的内容*/
	public static final String KEY_messageContent = "messageContent";
	
	
	public static final int SEND_FLAG_TRUE=1;
	public static final int SEND_FLAG_FALSE=2;
	
	
	
	

	/**	邮箱认证*/
	public static String Key_emailIdentify = "emailIdentify";
	/**	邮箱发送验证码*/
	public static String Key_codeEmail = "codeEmail";
	/**充值成功*/
	public static String Key_rechargeSuccess = "rechargeSuccess";

	
	/**提现申请*/
	public static String Key_withdraw = "withdraw";
	/**提现成功*/
	public static String Key_withdrawSuccess = "withdrawSuccess";
	/**提现取消*/
	public static String Key_withdrawCancel = "withdrawCancel";
	
	
	/*** 提成 ***/
	public static final String Key_rewardOrder = "rewardOrder";
	/*** 师傅提成 ***/
	public static final String Key_rewardOrderParent = "rewardOrderParent";
	
	/**经纪佣金*/
	public static String Key_reward = "reward";
	/**收到红包*/
	public static String Key_accessLucky = "accessLucky";
	/**激活红包*/
	public static String Key_activateLucky = "activateLucky";
	/**扣除过期代金券*/
	public static String Key_overdueCash = "overdueCash";
	
	
	/**新订单处理*/
	public static String Key_adminNewOrder = "adminNewOrder";
	/**订单补充资料*/
	public static String Key_adminResendOrder = "adminResendOrder";
	
	//推送订单
	public static final String jpush_Type_1 = "1";
	
	//简易的推送信息
	public static final String jpush_Type_2 = "2";
	
}
