package org.bc.admin.util.borrow;

import java.util.Date;
import java.util.Map;

import org.bc.admin.util.constant.BorrowConstant;
import org.bc.admin.util.sys.ServiceKey;
import org.bc.admin.util.sys.SysParamsUtil;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.util.NumberUtil;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 贷款申请
 * @author liulw 2017-07-20
 *
 */
@Slf4j
public class BorrowApplyUtils {

	/**
	 * 是否能申请
	 * @param telephone
	 * @return 
	 */
	public static AppResult isCanApply(String telephone){
		AppResult result = new AppResult();
	
		Map<String, Object> queryMap = queryApplyInfo(telephone);
		if(queryMap != null && !queryMap.isEmpty()) {
			
			int status = NumberUtil.getInt(queryMap.get("status"));
			//处理成功|处理中的不可重复申请
			if(status == BorrowConstant.ApplyStatus.Success ||
					status == BorrowConstant.ApplyStatus.Handling) {
				
				result.setErrorCode("100");
				result.setSuccess(false);
				result.setMessage("已经申请过了，不能重复申请");
			}
			
			result.putAttr("uid", queryMap.get("uid"));
			result.putAttr("status", queryMap.get("status"));
		}

		return result;
	}
	
	/**
	 * 是否能申请
	 * @param telephone
	 * @return 
	 */
	public static AppResult isCanApply(Object uid,int status){
		AppResult result = new AppResult();
	
		//处理成功|处理中的不可重复申请
		if(status == BorrowConstant.ApplyStatus.Success ||
				status == BorrowConstant.ApplyStatus.Handling) {
			
			result.setErrorCode("100");
			result.setSuccess(false);
			result.setMessage("已经申请过了，不能重复申请");
		}
		
		result.putAttr("uid", uid);
		result.putAttr("status", status);

		return result;
	}
	
	
	public static Map<String,Object> queryApplyInfo(String telephone) {
		AppParam queryAppParam = new AppParam("applyService","query");
		queryAppParam.addAttr("telephone", telephone);
		queryAppParam.addAttr("limitSize", 1);
		queryAppParam.setOrderBy("applyId");
		queryAppParam.setOrderValue("DESC");
		AppResult result = ServiceKey.doCallNoTx(queryAppParam, ServiceKey.Key_data);
		
		if(result != null && result.getRows().size() > 0)
			return result.getRow(0);
		else
			return null;
	}
	
	public static int insertPushRecord(Object applyId,Object telephone,Date applyTime) {
		int insertSize = 0;
		try{
			AppParam insertParam = new AppParam("applyPushRecordService","insert");
			insertParam.addAttr("telephone", telephone);
			insertParam.addAttr("applyId", applyId);
			insertParam.addAttr("applyTime", applyTime);
			AppResult result = ServiceKey.doCall(insertParam, ServiceKey.Key_data);
			
			insertSize = result.getInsertCount();
		}catch(Exception e){
			insertSize = 0;
			log.error("插入推送数据记录失败,applyId={},telephone={}",
					applyId,telephone,e);
		}
		
		return insertSize;
	}
	
	public static boolean isTestUser (String telephone) {
		String telephones = SysParamsUtil.getParamByKey("tgTestTelephone");
		if (!StringUtils.isEmpty(telephones)) {
			if (telephones.indexOf(telephone) > -1) {
				return true;
			}
		}
		return false;
	}
	
	
}
