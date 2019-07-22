package org.xxy.email;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.xxy.MessageConstants;

import com.sun.mail.util.MailSSLSocketFactory;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EmailUtils {
	/**Email设置*/
	public final static String Key_configEmail="configEmail";
	
	public static Session getSession(Map<String,Object> map){
		Properties props = new Properties();
		// 启用ssl
		String enableSsl =  map.get("enableSsl")+"";
		if (enableSsl != null && enableSsl.equals("1")) {
			String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
			props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
			props.put("mail.smtp.socketFactory.port", map.get("port")
					.toString());
			props.put("mail.smtp.socketFactory.fallback", "false");
			MailSSLSocketFactory sf;
			try {
				sf = new MailSSLSocketFactory();
		        sf.setTrustAllHosts(true); 
		        props.put("mail.smtp.ssl.socketFactory", sf);
			} catch (GeneralSecurityException e) {
				log.error("sms sll error:", e);
			}
		}
		//测试时，需要设置超时时长
		if("testSendEmail".equals(map.get(MessageConstants.KEY_MESSAGE_TYPE))){
			props.put("mail.smtp.timeout", "5000");
		}
		props.put("mail.smtp.port", map.get("port").toString());
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.host", map.get("emailUrl").toString());
		
		// 身份验证
		Authenticator auth = new SimpleAuthenticator(map
				.get("emailAddress").toString(), map.get("emailPwd")
				.toString());
		Session session = Session.getInstance(props, auth);
		return session;
	}
	// 身份验证
	static class SimpleAuthenticator extends Authenticator {
		private String user;
		private String pwd;

		public SimpleAuthenticator(String user, String pwd) {
			this.user = user;
			this.pwd = pwd;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pwd);
		}
	}
	
	
	/**
	 * 发送验证邮件
	 * @param desEmail 目标邮箱地址
	 * @param userName 用户名
	 * @throws Exception 
	 */
	public static AppResult sendTestEmail(String desEmail, String userName) throws Exception{
		AppParam context = new AppParam();
		context.addAttr("userName", userName);
		context.addAttr(MessageConstants.KEY_EMAIL_TO, desEmail);
		context.addAttr(MessageConstants.KEY_emailContent, "这是测试邮件，无需回复");
		context.addAttr(MessageConstants.KEY_messageTitle, "测试邮件");
		context.addAttr(MessageConstants.KEY_MESSAGE_TYPE, "testSendEmail");
		return EmailSendUtil.sendHtmlMail(context);
	}
	
	/**
	 * 常用邮箱登录地址
	 * @param emailAddress
	 * @return
	 */
	public static String getEmailLoginUrl(String emailAddress){
		String suffix = emailAddress.split("@")[1].toLowerCase();
		if ("163.com".equals(suffix)) {
            return "http://mail.163.com";
        } else if ("vip.163.com".equals(suffix)) {
            return "http://vip.163.com";
        } else if ("126.com".equals(suffix)) {
            return "http://mail.126.com";
        } else if ("qq.com".equals(suffix) || "vip.qq.com".equals(suffix) || "foxmail.com".equals(suffix)) {
            return "http://mail.qq.com";
        } else if ("sohu.com".equals(suffix)) {
            return "http://mail.sohu.com";
        } else if ("tom.com".equals(suffix)) {
            return "http://mail.tom.com";
        } else if ("vip.sina.com".equals(suffix)) {
            return "http://vip.sina.com";
        } else if ("sina.com.cn".equals(suffix) || "sina.com".equals(suffix)) {
            return "http://mail.sina.com.cn";
        } else if ("yeah.net".equals(suffix)) {
            return "http://www.yeah.net";
        } else if ("21cn.com".equals(suffix)) {
            return "http://mail.21cn.com";
        } else if ("hotmail.com".equals(suffix)) {
            return "http://www.hotmail.com";
        } else if ("sogou.com".equals(suffix)) {
            return "http://mail.sogou.com";
        } else if ("188.com".equals(suffix)) {
            return "http://www.188.com";
        } else if ("139.com".equals(suffix)) {
            return "http://mail.10086.cn";
        } else if ("189.cn".equals(suffix)) {
            return "http://webmail15.189.cn/webmail";
        } else if ("wo.com.cn".equals(suffix)) {
            return "http://mail.wo.com.cn/smsmail";
        } else if ("139.com".equals(suffix)) {
            return "http://mail.10086.cn";
        } else {
            return null;
        }
	}
	
	public static List<Map<String, Object>> refreshEmailService(){
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("emailUrl", "smtp.exmail.qq.com");
		map.put("port", "465");
		map.put("enableAuth", "1");
		map.put("emailAddress", "vip1@ddqian.com");
		map.put("emailPwd", "ac123456");
		map.put("emailName", "多多智富");
		map.put("enableSsl", "1");
		list.add(map);
		return list;
		/*
		AppParam param = new AppParam();
		param.setService("sysEmailService");
		param.setMethod("query");
		param.addAttr("enable", "1");
		AppResult emailConf = SoaManager.getInstance().invoke(param);
		RedisUtils.getRedisService().set(Key_configEmail,(Serializable)emailConf.getRows() );
		return emailConf.getRows();*/
		
	}
	public static void main(String[] args){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("enableSsl", "1");
		map.put("port", "465");
		map.put("emailUrl", "smtp.qq.com");
		map.put("emailAddress", "qinxcb@qq.com");
		map.put("emailPwd", "ulbqzlkzgrjpbhjh");
		map.put(MessageConstants.KEY_MESSAGE_TYPE, "testSendEmail");
		Session session = EmailUtils.getSession(map);
		try {
			// 昵称
			String nick  = javax.mail.internet.MimeUtility.encodeText("小小金融");
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(nick + " <" + map.get("emailAddress").toString() + ">"));
			String emailTo = "qinxcb@qq.com";
			
			msg.setRecipient(RecipientType.TO, new InternetAddress(emailTo));
			msg.setSubject("测试邮件", "UTF-8");
			msg.setSentDate(new Date());
			msg.setContent("测试邮件111","text/html;charset=UTF-8");
			msg.saveChanges();
			Transport.send(msg);
		} catch (Exception e) {
			log.error(" sendHtmlMail error:",e);
			
		}
	}
}
