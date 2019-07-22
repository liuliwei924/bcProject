package org.bc.admin.util.sys;

import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppProperties;
import org.llw.com.context.AppResult;
import org.llw.com.core.SpringAppContext;
import org.llw.com.core.service.RemoteInvoke;
import org.llw.com.core.service.SoaManager;
import org.llw.com.util.StringUtil;



/***
 * 系统服务 关健字
 * @author qinxcb
 *
 */
public class ServiceKey {

	/**业务服务相关*/
	public static final String Key_data="data";
	/**系统服务相关*/
	public static final String Key_sys="sys";
	
	
	/**
	 * 用事务调用
	 * @param params
	 * @param rpcKey
	 */
	public static AppResult doCall(AppParam params, String rpcKey){
		params.setRpcServiceName(AppProperties
				.getProperties(DuoduoConstant.RPC_SERVICE_START + rpcKey));
		
		String service = StringUtil.getString(params.getService());
		AppResult result = null;
		
		if (SpringAppContext.getBean(service) == null) {
			result = RemoteInvoke.getInstance().call(params);
		}else{
			result = SoaManager.getInstance().invoke(params);
		}
		
		return result ;
		
	}
	
	/**无事务调用
	 * 
	 * @param params
	 * @param rpcKey
	 */
	public static AppResult doCallNoTx(AppParam params, String rpcKey){
		params.setRpcServiceName(AppProperties
				.getProperties(DuoduoConstant.RPC_SERVICE_START + rpcKey));
		
		String service = StringUtil.getString(params.getService());
		AppResult result = null;
		
		if (SpringAppContext.getBean(service) == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else{
			result = SoaManager.getInstance().callNoTx(params);
		}
		
		return result ;
		
	}
}
