package org.xxy.sms;

import java.util.ArrayList;

import org.llw.com.context.AppResult;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;

import lombok.extern.slf4j.Slf4j;


/***
 * 腾讯发送短信
 * 
 * @author Administrator
 *
 */
@Slf4j
public class SmsTenXunSendUtil {


	/**
	 * 
	 * @param smsConfig 短信配置
	 * @param telephone 手机
	 * @param params 内容需要参数
	 * @return
	 */
	public static AppResult sendTplSms(SmsConfig smsConfig,String telephone, ArrayList<String> params) {
		AppResult result = new AppResult();

		try {
			// 初始化单发
			int appid = Integer.parseInt(smsConfig.getAppid()) ;
			SmsSingleSender singleSender = new SmsSingleSender(appid, smsConfig.getAppkey());
			// 指定模板单发
			// 假设短信模板内容为：测试短信，{1}，{2}，{3}，上学。
			SmsSingleSenderResult singleSenderResult = singleSender.sendWithParam("86", telephone, Integer.parseInt(smsConfig.getTempId()), params,
					smsConfig.getSignName(), "", "");
			int ret = singleSenderResult.result;
			if (ret == 0) {
				return result;
			}
			result.setSuccess(Boolean.FALSE);
			result.setMessage(singleSenderResult.errMsg);
			// 异常记录错误码和错误信息
			log.error("Send SMS error: telephone=" + telephone + " errorCode="
					+ singleSenderResult.result + " messsage:" + singleSenderResult.errMsg);
		} catch (Exception e) {
			log.error("Send SMS error: telephone=" + telephone,e);
			result.setSuccess(Boolean.FALSE);
			result.setMessage("验证码发送失败，请重试");
		}
		return result;
	}
}
