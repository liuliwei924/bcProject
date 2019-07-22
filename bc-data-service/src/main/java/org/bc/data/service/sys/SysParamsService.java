package org.bc.data.service.sys;

import java.util.Date;

import org.bc.admin.util.sys.SysParamsUtil;
import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class SysParamsService extends BaseService {
	private static final String NAMESPACE = "SYSPARAMS";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT,context.getAttr(), context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.insert(context, NAMESPACE);
		
		SysParamsUtil.refreshValue(context.getAttr("paramCode").toString(),context.getAttr("paramValue").toString());
		return result;
	}
	public AppResult update(AppParam context) {
		context.addAttr("updateTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.update(context, NAMESPACE);
		SysParamsUtil.refreshValue(context.getAttr("paramCode").toString(),context.getAttr("paramValue").toString());
		return result;
	}
}
