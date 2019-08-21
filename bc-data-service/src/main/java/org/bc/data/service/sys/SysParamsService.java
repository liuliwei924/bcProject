package org.bc.data.service.sys;

import java.util.Date;

import org.bc.admin.util.sys.SysParamsUtil;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.web.session.DuoduoSession;
import org.llw.common.core.service.ApiBaseServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class SysParamsService extends ApiBaseServiceImpl {
	private static final String NAMESPACE = "SYSPARAMS";

	public SysParamsService() {
		super.namespace = NAMESPACE;
	}
	

	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.insert(context);
		
		SysParamsUtil.refreshValue(context.getAttr("paramCode").toString(),context.getAttr("paramValue").toString());
		return result;
	}
	public AppResult update(AppParam context) {
		context.addAttr("updateTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.update(context);
		SysParamsUtil.refreshValue(context.getAttr("paramCode").toString(),context.getAttr("paramValue").toString());
		return result;
	}
	
	@Override
	public AppResult insertRetId(AppParam params) {
		AppResult result = this.insert(params);
		
		result.putAttr("paramCode", params.getAttr("paramCode"));
		return result;
	}
}
