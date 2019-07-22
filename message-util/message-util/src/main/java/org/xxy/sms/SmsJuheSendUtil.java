package org.xxy.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.llw.com.context.AppResult;
import org.llw.com.util.StringUtil;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsJuheSendUtil {

	public static final String DEF_CHATSET = "UTF-8";
	public static final int DEF_CONN_TIMEOUT = 10000;
	public static final int DEF_READ_TIMEOUT = 10000;
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

	/**
	 * 用户发验证码的
	 * 
	 * @param telephone
	 *            接收手机号码
	 * @param random
	 *            随机数
	 * @param tplId
	 *            发送模板
	 * @param key
	 *            发送key
	 * @return
	 */
	public static AppResult sendSms(String telephone, String random, SmsConfig smsConfig) {
		random = "#code#=" + random;
		return sendSmsText(telephone, random, smsConfig);

	}

	/**
	 * 用于发自定义内容
	 * 
	 * @param telephone
	 *            接收手机号码
	 * @param random
	 *            随机数
	 * @param tplId
	 *            发送模板
	 * @param key
	 *            发送key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult sendSmsText(String telephone, String context, SmsConfig smsConfig) {
		AppResult result = new AppResult();
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		String strUrl = "http://v.juhe.cn/sms/send?";// 请求接口地址

		try {
			StringBuffer sb = new StringBuffer(strUrl);
			sb.append("mobile=" + telephone);
			sb.append("&tpl_id=" + smsConfig.getTempId());
			sb.append("&tpl_value=" + URLEncoder.encode(juheEncodeStr(context), "utf-8"));
			sb.append("&key=" + smsConfig.getAppkey());
			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setRequestProperty("User-agent", userAgent);
			conn.setUseCaches(false);
			conn.setConnectTimeout(DEF_CONN_TIMEOUT);
			conn.setReadTimeout(DEF_READ_TIMEOUT);
			conn.setInstanceFollowRedirects(false);
			conn.connect();
			InputStream is = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
			String strRead = null;
			sb = new StringBuffer();
			while ((strRead = reader.readLine()) != null) {
				sb.append(strRead);
			}
			Map<String, Object> resultObj = JSON.parseObject(sb.toString(), Map.class);
			String error_code = resultObj.get("error_code").toString();
			if (error_code.equals("0")) {
				return result;
			}
			result.setSuccess(Boolean.FALSE);
			result.setMessage(resultObj.get("reason").toString());
		} catch (IOException e) {
			log.error("send sms error:" + smsConfig.getTempId() + ":" + smsConfig.getAppkey(), e);
			result.setSuccess(Boolean.FALSE);
			result.setMessage(e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	private static String juheEncodeStr(String context) {
		// #code#=1234&#company#=聚合数据
		StringBuffer sb = new StringBuffer();
		String[] arr1 = context.split("&");
		for (String s : arr1) {
			String[] arr2 = s.split("=");
			sb.append(arr2[0]).append("=").append(StringUtil.encodeStr(arr2[1])).append("&");
		}

		String context2 = sb.toString();
		return context2.substring(0, context2.length() - 1);
	}
}
