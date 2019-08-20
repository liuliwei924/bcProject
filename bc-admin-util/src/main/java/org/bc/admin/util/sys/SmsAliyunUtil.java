package org.bc.admin.util.sys;

import java.util.Map;

import org.llw.com.context.AppResult;
import org.llw.com.util.JsonUtil;
import org.xxy.sms.SmsConfig;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
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
	        request.putQueryParameter("TemplateCode", "SMS_171090204");
	    	
	        request.putQueryParameter("TemplateParam", "{\"code\":\"" + randomCode + "\"}");
	        
	        AppResult result = new AppResult();
	        try {
	        	 CommonResponse response = client.getCommonResponse(request);
	        	 Map<String,Object> retData = JsonUtil.getInstance().json2Object(
	        			 response.getData(), Map.class);
	        	 
	             System.out.println(response.getData());
	        } catch (Exception e) {
	        	log.error("阿里云发送短信验证码异常",e);
	        	result.setSuccess(Boolean.FALSE);
				result.setMessage("发送短信验证码异常");
	        }
	        
	        return result;
	}
	
	public static void sendSms1(){
		 DefaultProfile profile = DefaultProfile.getProfile("default", "LTAIQechIC0bj1hz", "");
	        IAcsClient client = new DefaultAcsClient(profile);

	        CommonRequest request = new CommonRequest();
	        request.setMethod(MethodType.POST);
	        request.setDomain("dysmsapi.aliyuncs.com");
	        request.setVersion("2017-05-25");
	        request.setAction("SendSms");
	        request.putQueryParameter("RegionId", "default");
	        request.putQueryParameter("PhoneNumbers", "18670787211");
	        request.putQueryParameter("SignName", "恒盈");
	        request.putQueryParameter("TemplateCode", "SMS_171090204");
	        request.putQueryParameter("TemplateParam", "{\"code\":\"1111\"}");
	        try {
	            CommonResponse response = client.getCommonResponse(request);
	            System.out.println(response.getData());
	        } catch (ServerException e) {
	            e.printStackTrace();
	        } catch (ClientException e) {
	            e.printStackTrace();
	        }
	    }

	
	public static void main(String[] args) {
        DefaultProfile profile = DefaultProfile.getProfile("default", "LTAIo4oBm4dasEm6", "U5hhe4Qg2PwV5RLPVxHtkBMO1jp8pa");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "default");
        request.putQueryParameter("PhoneNumbers", "18670787211");
        request.putQueryParameter("SignName", "恒盈");
        request.putQueryParameter("TemplateCode", "SMS_171090204");
        request.putQueryParameter("TemplateParam", "{\"code\":\"1111\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
