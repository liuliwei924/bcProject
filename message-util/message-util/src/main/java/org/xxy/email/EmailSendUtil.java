package org.xxy.email;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.math.RandomUtils;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.xxy.MessageConstants;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EmailSendUtil {
	
	@SuppressWarnings("unchecked")
	public static AppResult sendHtmlMail(AppParam params) {
		AppResult result = new AppResult();
		//配置的列表
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr(MessageConstants.ConfigList);
		if (list.size() == 0) {
			log.error("not have email config.");
			result.setSuccess(false);
			result.setMessage("not have email config.");
			return result;
		}
		Map<String, Object> map =  list.get(RandomUtils.nextInt(list.size()));
		map.put(MessageConstants.KEY_MESSAGE_TYPE, params.getAttr(MessageConstants.KEY_MESSAGE_TYPE));
		Session session = EmailUtils.getSession(map);
		try {
			// 昵称
			String nick  = javax.mail.internet.MimeUtility.encodeText(map.get("emailName").toString());
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(nick + " <" + map.get("emailAddress").toString() + ">"));
			String emailTo = (String)params.getAttr(MessageConstants.KEY_EMAIL_TO);
			
			msg.setRecipient(RecipientType.TO, new InternetAddress(emailTo));
			msg.setSubject(params.getAttr(MessageConstants.KEY_messageTitle).toString(), "UTF-8");
			msg.setSentDate(new Date());
			msg.setContent(params.getAttr(MessageConstants.KEY_emailContent),"text/html;charset=UTF-8");
			msg.saveChanges();
			Transport.send(msg);
			result.setSuccess(true);
			return result;
		} catch (Exception e) {
			log.error(" sendHtmlMail error:" + map.get("emailAddress") + " to " + 
					params.getAttr(MessageConstants.KEY_EMAIL_TO),e);
			result.setSuccess(false);
			result.setMessage("邮件发送失败，请稍后重试！");
			return result;
		}
	}
	
	
	/***
	 * 发送邮件处理
	 * @param mailTo
	 * @param title
	 * @param content
	 * @param messageType
	 * @return
	 */
	public static AppResult sendHtmlMail(String mailTo,String title,String content,String messageType) {
		AppResult result = new AppResult();
		Map<String, Object> map =  new HashMap<String,Object>();
		
		Session session = EmailUtils.getSession(map);
		try {
			// 昵称
			String nick  = javax.mail.internet.MimeUtility.encodeText(map.get("emailName").toString());
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(nick + " <" + map.get("emailAddress").toString() + ">"));
			
			msg.setRecipient(RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(title, "UTF-8");
			msg.setSentDate(new Date());
			msg.setContent(content,"text/html;charset=UTF-8");
			msg.saveChanges();
			Transport.send(msg);
			result.setSuccess(true);
			return result;
		} catch (Exception e) {
			log.error(" sendHtmlMail error:" + map.get("emailAddress") + " to " + 
					mailTo,e);
			result.setSuccess(false);
			result.setMessage("邮件发送失败，请稍后重试！");
			return result;
		}
	}
	
	public static void main(String args[]){
		AppParam param = new AppParam();
		param.addAttr("to", "734455416@qq.com");
		param.addAttr("messageTitle", "企查查Cookie失效");
		param.addAttr("emailContent", "http://app.xxjr.com/cpQuery/company/page/setCookie");
		sendHtmlMail(param);
	}
}
