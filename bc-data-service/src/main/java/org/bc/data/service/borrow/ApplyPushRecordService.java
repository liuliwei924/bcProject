package org.bc.data.service.borrow;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.common.core.service.ApiBaseServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class ApplyPushRecordService extends ApiBaseServiceImpl{

	private static final String NAMESPACE = "APPLYPUSHRECORD";
	
	public ApplyPushRecordService(){
		super.namespace = NAMESPACE;
	}
	
	@Override
	public AppResult insertRetId(AppParam params) {
		AppResult result = this.insert(params);
		
		result.putAttr("pushId", params.getAttr("pushId"));
		return result;
	}
	
	/**
	 * 查询可推送的数据
	 * @param params
	 * @return
	 */
	public AppResult queryPushList(AppParam params){
		return super.query(params, NAMESPACE, "queryPushList");
	}
}
