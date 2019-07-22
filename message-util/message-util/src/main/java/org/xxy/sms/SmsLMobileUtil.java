package org.xxy.sms;

import java.util.LinkedHashMap;
import java.util.Map;

import org.llw.com.context.AppResult;
import org.llw.com.http.HttpClientUtil;
import org.llw.com.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 乐信短信工具类
 * @author sty
 *
 */
@Slf4j
public class SmsLMobileUtil {
	
	public static String url_submit = "http://api.51welink.com/json/sms/g_Submit";// 提交短信地址
	 
	
	public static AppResult sendCode(String telephone, String random,SmsConfig smsConfig) {
		String content = "您的短信验证码是：" + random + "，5分钟内有效，请不要把验证码泄露给其他人。【" + smsConfig.getSignName() + "】";
		return sendJson(telephone,content,smsConfig);
	}
	/**
	 * 发送短信
	 * @param phones
	 * @param content
	 * @return
	 */
	public static AppResult sendJson(String telephone, String content,SmsConfig smsConfig) {
		AppResult result = new AppResult();
       try {		
			Map<String, Object> params = new LinkedHashMap<String, Object>();
			params.put("sname", smsConfig.getAppid());
			params.put("spwd", smsConfig.getAppkey());
			params.put("sprdid", smsConfig.getTempId());
			params.put("sdst", telephone);
			params.put("smsg", content);
			String resp = HttpClientUtil.getInstance().sendHttpPost(url_submit, params);
			//String resp = "{\"State\": \"0\",\"MsgID\": \"x71819213746576\",\"MsgState\": \"提交成功\",\"Reserve\":\"0\"}"
			@SuppressWarnings("unchecked")
			Map<String,Object> resultObj = JsonUtil.getInstance().json2Object(resp, Map.class);
      
			String stateCode =  resultObj.get("State").toString();
			if (stateCode.equals("0")) {
				return result;
			}
			
			result.setSuccess(Boolean.FALSE);
			result.setMessage(resultObj.get("MsgState").toString());
			
        }catch (Exception e) {
        	log.error("乐信发送短信发生异常",e);
        	result.setSuccess(Boolean.FALSE);
			result.setMessage("发送短信失败");
        }
		
		return result;
	}
}
