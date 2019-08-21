package org.xxy.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.llw.com.context.AppProperties;
import org.llw.com.context.AppResult;
import org.llw.com.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/***
 * 九维短信处理
 * @author Administrator
 *
 */
@Slf4j
public class  SendSmsByUrl{
	
	
	/**
	 * 通过URL发送短信
	 * @param smsUrl
	 * @param content
	 * @param phone
	 * @param testPhone
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static AppResult sendSMSInfo(String smsUrl, String content, String phone, String testPhone) {
		 AppResult appresult = new AppResult();
		 BufferedReader in =null;
		 try {
			StringBuffer sb = new StringBuffer();
			sb.append(smsUrl);
			//判断是否测试环境，测试环境使用默认的电话
			if (AppProperties.isDebug()) {
				phone = testPhone == null ? "13817862400" : testPhone;
			}
			sb.append("&phones=" + phone);
			sb.append("&msg=" + URLEncoder.encode(StringUtil.encodeBase64(content), "utf-8"));
			sb.toString();
			
			URL url = new URL(sb.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    in = new BufferedReader(new InputStreamReader(url.openStream()));
		    
		    String result = in.readLine();
		    if(!result.startsWith("1")){
		    	appresult.setSuccess(Boolean.FALSE);
		    	appresult.setMessage("短信发送失败：" + result);
		    }
	    }catch(Exception e) {
	    	log.error("sendSMSInfo erro:", e);
	    	appresult.setSuccess(Boolean.FALSE);
	    	appresult.setMessage("短信发送失败：" + e.getMessage());
	    }finally {
	    	if(in!=null) {
	    		try {
					in.close();
				} catch (IOException e) {
					log.error("sendSMSInfo close BufferedReader erro:", e);
				}
	    	}
	    }
	    return appresult;
	}
	
	
	/***
	 * 语音处理处理
	 * @param message
	 */
	public static void main(String[] args) {

		
		AppResult result = SendSmsByUrl.sendSMSInfo("http://123.206.19.78:9001/sendXSms.do?username=qddtz&password=124588&productid=621215&dstime=&xh=11", "测试短信验证码", 
				"13817862400", "13817862400");
		
		try {
			System.out.println(result.isSuccess());
		} catch (Exception e) {
		}
	
	}
}
