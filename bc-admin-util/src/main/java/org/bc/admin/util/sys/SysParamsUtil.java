package org.bc.admin.util.sys;

import java.io.Serializable;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.core.SpringAppContext;
import org.llw.com.core.service.RemoteInvoke;
import org.llw.com.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
/***
 * 参数配置
 * @author Administrator
 *
 */
public class SysParamsUtil {
	public final static String StartParamKey="BC_Param_";

	/**
	 * 获取参数
	 * @param key
	 */
	public static String refreshValue(String key, String value){
		if (value == null) {
			AppParam queryParams = new AppParam();
			queryParams.setService("sysParamsService");
			queryParams.setMethod("query");
			queryParams.addAttr("paramCode", key);
			
			//若没有相应的对象，使用远程调用 
			if (SpringAppContext.getBean("sysParamsService") == null) {
				AppResult paramsResult = RemoteInvoke.getInstance().call(queryParams);
				if (paramsResult.getRows().size() > 0) {
					value = (String)paramsResult.getRow(0).get("paramValue");
				}
			}else{
				AppResult paramsResult = SoaManager.getInstance().invoke(queryParams);
				if (paramsResult.getRows().size() > 0) {
					value = (String)paramsResult.getRow(0).get("paramValue");
				}
			}
			RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, (Serializable) value);
		}else{
			RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, (Serializable) value);
		}
		return value;
	}
	/**
	 * 获取参数
	 * @param key
	 */
	public static String getParamByKey(String key){
		Object value =null;
		try{
			value = RedisUtils.getRedisService().get(SysParamsUtil.StartParamKey + key);
		}catch(Exception e){
			return null;
		}
		if (value == null) {
			value = refreshValue(key, null);
		}
		return value ==null ? null: value.toString();
	}
	
	/**
	 * 获取参数，如果获取不到，直接返回key
	 * @param key
	 * @param retunKey
	 * @return
	 */
	public static String getParamByKey(String key, boolean retunKey){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, key);
			return key;
		}
		return value;
	}
	
	/**
	 * 获取参数，如果获取不到，直接返回defaultValue
	 * @param key 
	 * @param defaultValue
	 * @return
	 */
	public static int getIntParamByKey(String key, int defaultValue){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			try{
				RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, String.valueOf(defaultValue));
			}catch(Exception e){
				
			}
			return defaultValue;
		}
		return Integer.valueOf(value);
	}
	
	/**
	 * 获取参数，如果获取不到，直接返回defaultValue
	 * @param key 
	 * @param defaultValue
	 * @return
	 */
	public static String getStringParamByKey(String key, String defaultValue){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			try{
				RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, defaultValue);
			}catch(Exception e){
				
			}
			return defaultValue;
		}
		return value;
	}
	
	/**
	 * 系统测试
	 * @param key 
	 * @param defaultValue
	 * @return
	 */
	public static boolean getBoleanByKey(String key, boolean defaultValue){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			try{
				RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, String.valueOf(defaultValue));
			}catch(Exception e){
				
			}
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}
	
}
