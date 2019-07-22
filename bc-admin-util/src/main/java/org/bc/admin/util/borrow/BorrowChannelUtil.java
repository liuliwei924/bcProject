package org.bc.admin.util.borrow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bc.admin.util.sys.ServiceKey;
import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppProperties;
import org.llw.com.context.AppResult;
import org.llw.com.core.SpringAppContext;
import org.llw.com.core.service.RemoteInvoke;
import org.llw.com.core.service.SoaManager;
import org.llw.com.util.StringUtil;
import org.llw.model.cache.RedisUtils;

/**
 * 贷款渠道工具类
 * @author Administrator
 *
 */
public class BorrowChannelUtil {
	
	/** 所有贷款渠道  **/
	private final static String KEY_BORROW_CHANNEL = "key_borrow_channel";
	
	/**
	 * 查询所有贷款渠道
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAllChannel(){
		List<Map<String, Object>> info = (List<Map<String, Object>>) RedisUtils
				.getRedisService().get(KEY_BORROW_CHANNEL);
		if(info == null || info.size()==0){
			info = refreshBorrowChannel();
		}
		return info;
	}
	
	
	/**
	 * 根据渠道代号查询商户信息
	 */
	public static Map<String,Object> getChannelByCode(String channelCode){
		List<Map<String, Object>> list = getAllChannel();
		for (int i = 0; i < list.size(); i++) {
			Map<String,Object> map = list.get(i);
			if(channelCode.equals(map.get("channelCode").toString())){
				return map;
			}
		}
		return new HashMap<String,Object>();
	}
	
	/**
	 * 根据渠道代号查询商户信息
	 */
	public static String getChannelByStartCode(String channelCode){
		if(channelCode==null|| channelCode.trim().length()==0){
			return null;
		}
		List<Map<String, Object>> list = getAllChannel();
		for (int i = 0; i < list.size(); i++) {
			Map<String,Object> map = list.get(i);
			if(channelCode.toLowerCase().startsWith(map.get("channelCode").toString().toLowerCase())){
				return StringUtil.getString(map.get("channelCode"));
			}
		}
		return null;
	}
	
	
	
	/**
	 * 刷新渠道
	 * @return
	 */
	public static List<Map<String, Object>> refreshBorrowChannel(){
		AppParam param = new AppParam();
		param.setService("borrowChannelService");
		param.setMethod("query");
		param.setRpcServiceName(AppProperties
				.getProperties(DuoduoConstant.RPC_SERVICE_START + ServiceKey.Key_data));
		//若没有相应的对象，使用远程调用 
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("borrowChannelService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size() > 0){
			List<Map<String, Object>> info = result.getRows();
			RedisUtils.getRedisService().set(KEY_BORROW_CHANNEL, (Serializable)info, 3600 * 24 *7);
		}
		return null;
	}

}
