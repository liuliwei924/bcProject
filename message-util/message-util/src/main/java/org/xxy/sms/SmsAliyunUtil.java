package org.xxy.sms;

import java.util.Map;

import org.llw.com.context.AppResult;
import org.llw.com.util.JsonUtil;
import org.llw.com.util.StringUtil;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云短信接口
 * @author liulw 2019-08-20
 *
 */
@Slf4j
public class SmsAliyunUtil {

	private static final String SMS_DOMAIN = "dysmsapi.aliyuncs.com";
	
	private static final String SMS_VERSION = "2017-05-25";
	
	private static final String SMS_ACTION = "SendSms";
	
	private static final String SUCCESS = "OK";
	
    
	@SuppressWarnings("unchecked")
	public static AppResult sendSms(String telephone,String randomCode,SmsConfig smsConfig){
		   DefaultProfile profile = DefaultProfile.getProfile("default", smsConfig.getAppkey(), smsConfig.getAppid());
		   IAcsClient client = new DefaultAcsClient(profile);

		   CommonRequest request = new CommonRequest();
	        
	        request.setSysMethod(MethodType.POST);
	        request.setSysAction(SMS_ACTION);
	        request.setSysDomain(SMS_DOMAIN);
	        request.setSysVersion(SMS_VERSION);
	        request.putQueryParameter("PhoneNumbers", telephone);
	        request.putQueryParameter("SignName", smsConfig.getSignName());
	        request.putQueryParameter("TemplateCode", smsConfig.getTempId());
	    	
	        request.putQueryParameter("TemplateParam", "{\"code\":\"" + randomCode + "\"}");
	        
	        AppResult result = new AppResult();
	        try {
	        	 CommonResponse response = client.getCommonResponse(request);
	        	
	        	 //{"Message":"OK","RequestId":"654A9A84-782D-4711-89B1-7B8091D6F1BD","BizId":"852822066341294798^0","Code":"OK"}
	        	 Map<String,Object> retData = JsonUtil.getInstance().json2Object(
	        			 response.getData(), Map.class);
	        	 
	        	 System.out.println(String.format("%s[Code=%s]", retData.get("Message"),retData.get("Code")));
	        	 if(!SUCCESS.equalsIgnoreCase(StringUtil.getString(retData.get("Code")))) {
	        		 result.setSuccess(false);
	        		 result.setMessage(String.format("%s[Code=%s]", retData.get("Message"),retData.get("Code")));
	        		 
	        	 }
	        } catch (Exception e) {
	        	log.error("阿里云发送短信验证码异常",e);
	        	result.setSuccess(Boolean.FALSE);
				result.setMessage("发送短信验证码异常");
	        }
	        
	        return result;
	}

	
	public static void main(String[] args) {
		SmsConfig smsConfig = new SmsConfig();
		smsConfig.setAppkey("<accessKeyId>");
		smsConfig.setAppid("<secret>");
		smsConfig.setSignName("恒盈");
		smsConfig.setTempId("SMS_171090204");
		
		AppResult result = sendSms("18670787211", "123321", smsConfig);
		
		System.out.println(result.toJson());

    }
}
