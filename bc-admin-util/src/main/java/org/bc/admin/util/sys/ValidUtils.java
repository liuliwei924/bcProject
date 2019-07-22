package org.bc.admin.util.sys;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.llw.common.web.identify.ImageIdentify;
import org.llw.common.web.util.IdentifyUtil;
import org.llw.model.cache.RedisUtils;


public class ValidUtils {
	
	public static Map<String,String> defaultPartens = new HashMap<String,String>();
	
	
	/**邮箱的校验**/
	public static final String Valide_email = "email";
	/**电话号码 **/
	public static final String Valide_telephone = "telephone";
	/**保留两位小数的数字型 **/
	public static final String Valide_double2 = "double";
	/**数字类型校验 **/
	public static final String Valide_integer = "integer";
	static{
		defaultPartens.put("notNull", ".+");
		defaultPartens.put("email","^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
		defaultPartens.put("telephone" ,"^(1(([1-9][0-9])|(47)|[8][0123456789]))\\d{8}$");
		defaultPartens.put("account","^[a-zA-Z][a-zA-Z0-9_]{4,30}$");
		defaultPartens.put("subnetMask","^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])(\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])){3}$");
		defaultPartens.put("ip","^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$");
		defaultPartens.put("name","^[\\da-zA-Z_\u4e00-\u9fa5]{2,}$");
		defaultPartens.put("color","^[0-9a-fA-F]{3,6}$");
		defaultPartens.put("double","\\d+(?:\\.\\d{0,2}|)");
		defaultPartens.put("integer","\\d+");
	}
	

	/***
	 * 数据验证
	 * @param validType
	 * @param value
	 * @return
	 */
	public static boolean validValue(String validType, String value) {
		if (value == null) {
			value ="";
		}
		Pattern format = Pattern.compile(defaultPartens.get(validType));
		Matcher matcher = format.matcher(value);
		if (!matcher.matches()) {
			return false;
		}
		return true;
	}
	
	/***
	 * 手机格式验证
	 * 
	 * @param telephone
	 * @return
	 */
	public static Boolean validateTelephone(String telephone) {
		return validValue(Valide_telephone, telephone);
	}
	
	/***
	 * 密码校验
	 * @param password
	 * @return
	 */
	public static Boolean validatePwd(String password) {
		if (password == null || password.trim().length()<6) {
			return false;
		} 
		return true;
	}
	
	/**
	 * 短信验证码验证
	 * @param clientNum
	 * @param cacheKey
	 * @param phone
	 * @return
	 */
	public static Boolean validateRandomNo(String clientNum,String cacheKey,String phone){
		String key = SysParamsUtil.getParamByKey(cacheKey, true) + phone;
		boolean sendStatus = SysParamsUtil.getBoleanByKey("sendStatus", false);
		//不验证短信验证码
		if (!sendStatus) {
			if("4321".equals(clientNum)){
				return true; 
			}
			return false;
		}
		String random = (String) RedisUtils.getRedisService().get(key);
		if(random != null && random.equals(clientNum)){
			return true;
		}
		return false;
	}
	
	/**
	 * 删除缓存中的验证码
	 * @param key
	 */
	public static void removeCache(String key){
		RedisUtils.getRedisService().del(key);
	}
	/**
	 * 验证邮箱格式
	 * @param email
	 * @return
	 */
	public static Boolean validateEmail(String email){
		if(StringUtils.isEmpty(email)){
			return false;
		}
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(email);
		if(!matcher.matches()){
			return false;
		}
		return true;
	}
	
	
	/***
	 * 创建ImageCode
	 * @param request
	 * @param page
	 * @return
	 */
	public static ImageIdentify getImageCode(HttpServletRequest request,String page){
		//获取image信息
		ImageIdentify identify = IdentifyUtil.getIdentifyNum(4);
		String pageId =  request.getSession().getId()+page;
		RedisUtils.getRedisService().set(pageId, identify.getRandCode(),300);
		return identify;
	}
	
	/**
	 * 创建随机字母加数字图片
	 * @param imageCodeKey
	 * @param size  	随机码长度【4-6】
	 * @param width		图片宽度
	 * @param height	图片高度
	 * @return
	 */
	public static ImageIdentify getImageCode(String imageCodeKey,int size,int width,int height){
		//获取image信息
		ImageIdentify identify = IdentifyUtil.getIdentifyNum(size);
		RedisUtils.getRedisService().set(imageCodeKey, identify.getRandCode(), 80);
		return identify;
	}
	
	/**
	 * 验证imageCode
	 * @param imageCode 	验证码
	 * @param imageCodeKey
	 * @return
	 */
	public static boolean validImageCode(String imageCode,String imageCodeKey){
		//获取image信息
		String code = (String)RedisUtils.getRedisService().get(imageCodeKey);
		if (imageCode == null || !imageCode.equalsIgnoreCase(code)) {
			//删除验证码
			RedisUtils.getRedisService().del(imageCodeKey);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	/***
	 * 验证imageCode
	 * @param request
	 * @param page
	 * @return
	 */
	public static boolean validImageCode(HttpServletRequest request,String page){
		//获取image信息
		String imageCode= request.getParameter("imageCode");
		String pageId = request.getSession().getId()+page;
		String code = (String)RedisUtils.getRedisService().get(pageId);
		if (imageCode == null || !imageCode.equalsIgnoreCase(code)) {
			//删除验证码
			RedisUtils.getRedisService().del(pageId);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	/***
	 * 验证 referer
	 * @param request
	 * @return
	 */
	public static boolean validReferer(HttpServletRequest request){
		//Referer
		String referer= request.getHeader("Referer");
		String host = request.getHeader("HOST");
		if (StringUtils.isEmpty(referer) || referer.indexOf(host) <= 0) {
			return false;
		}
		return true;
	}
}
