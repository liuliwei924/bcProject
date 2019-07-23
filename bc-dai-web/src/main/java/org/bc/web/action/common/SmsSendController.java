package org.bc.web.action.common;

import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bc.admin.util.borrow.BorrowApplyUtils;
import org.bc.admin.util.constant.SysParamConstont;
import org.bc.admin.util.sys.SysParamsUtil;
import org.bc.admin.util.sys.ValidUtils;
import org.bc.web.action.util.Key_SMS;
import org.llw.com.context.AppResult;
import org.llw.com.util.JsonUtil;
import org.llw.com.util.StringUtil;
import org.llw.common.web.base.BaseController;
import org.llw.common.web.identify.ImageIdentify;
import org.llw.common.web.util.IdentifyUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxy.sms.SmsConfig;
import org.xxy.sms.SmsJuheSendUtil;
import org.xxy.sms.SmsLMobileUtil;
import org.xxy.sms.SmsTenXunSendUtil;

import lombok.extern.slf4j.Slf4j;


/***
 * 短信相关
 * @author liulw 
 *
 */

@Controller
@RequestMapping("/smsAction/")
@Slf4j
public class SmsSendController extends BaseController {
	
	/**
	 * 不需要用户登录的短信发送
	 * @param request
	 * @return
	 */
	@RequestMapping("nologin/{type}")
	@ResponseBody
	public AppResult sendNotLoginSms(@PathVariable String type){
		AppResult context = new AppResult();
		String telephone = request.getParameter("telephone");
		String page = request.getParameter("page");
		try{
			String key = null;
			if(StringUtils.isEmpty(telephone)){
				context.setSuccess(Boolean.FALSE);
				context.setMessage("请先输入手机号码！");
				return context;
			}
	
			boolean isCheckImgCode = SysParamsUtil.getBoleanByKey(SysParamConstont.IS_CHECK_IMG_CODE, false);
			// 验证图形验证码
			if(isCheckImgCode && !ValidUtils.validImageCode(request, page)) {
				context.setSuccess(Boolean.FALSE);
				context.setMessage("图形验证码不正确！");
				return context;
			}
			
			if(type.equals("borrowApply")){
				AppResult applyResult = BorrowApplyUtils.isCanApply(telephone);
				Object applyId = applyResult.getAttr("applyId");
				if(applyResult.isSuccess()){
					context.putAttr("applyId", applyId);
					key = Key_SMS.Key_SMS_BORROW_APPLY +telephone;
				}else{
					context.setSuccess(Boolean.FALSE);
					context.setErrorCode(applyResult.getErrorCode());
					context.setMessage(applyResult.getMessage());
					return context;
				}
			}
			AppResult result = sendSms(key, type, telephone,null);
			if(result.isSuccess()){
				context.setMessage("手机动态码已经发送到:"+StringUtil.getHideTelphone(telephone));
			}
		}catch(Throwable e){
			log.error("send sms error!,type={},tel={}",type,telephone,e);
		}
		return context;
	}
	

	/***
	 * 发送短信处理
	 * @param key
	 * @param type
	 * @param telephone
	 */
	private AppResult sendSms(String key,String type,String telephone,String isVoice){
		AppResult result = new AppResult();
 		if (!SysParamsUtil.getBoleanByKey(Key_SMS.SEND_SMS_STATUS, false)) { // 不发送短信验证码
			RedisUtils.getRedisService().set(key,"4321",300);
			return result;
		}
 		
 		String smsConfigStr = SysParamsUtil.getStringParamByKey("SMS_CONFIG","");
 		
 		if(StringUtils.isEmpty(smsConfigStr)) {
 			result.setSuccess(false);
 			result.setMessage("短信配置为空！");
 			return result;
 		}
 		
 	    SmsConfig smsConfig = JsonUtil.getInstance().json2Object(smsConfigStr, SmsConfig.class);
		
 	    String random = (String)RedisUtils.getRedisService().get(key);
		if (random == null) {
			random = IdentifyUtil.getRandNum(6);
		}
		
		int smsType = smsConfig.getSmsType();
		
		if(smsType == SysParamConstont.SmsType.TengXun) {
			
			ArrayList<String> params = new ArrayList<String>();
	    	params.add(random);
			result =  SmsTenXunSendUtil.sendTplSms(smsConfig, telephone,params);
		
		}else if(smsType == SysParamConstont.SmsType.Juhe) {
			
			result = SmsJuheSendUtil.sendSms(telephone, random, smsConfig);
		
		}else if(smsType == SysParamConstont.SmsType.LMobile) {
			
			result = SmsLMobileUtil.sendCode(telephone, random, smsConfig);
		}else {
			result.setMessage("暂不支持发送短信");
			result.setSuccess(false);
		}
        
		if(result.isSuccess()) {
			RedisUtils.getRedisService().set(key, random,300);
		}
		
		return result; 
	}
	
	/**
	 * 生成图形验证码
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("tgImageCode")
	public String tgImageCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ServletOutputStream responseOutputStream = response.getOutputStream();
		//图片页面
		String page = request.getParameter("page");
		ImageIdentify identify = ValidUtils.getImageCode(request, page);
		// 输出图象到页面
		ImageIO.write(identify.getImage(), "JPG", responseOutputStream);
		// 以下关闭输入流！
		responseOutputStream.flush();
		responseOutputStream.close();
		return null; 
	}
}
