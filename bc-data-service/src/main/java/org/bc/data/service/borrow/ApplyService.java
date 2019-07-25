package org.bc.data.service.borrow;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bc.admin.util.borrow.BorrowApplyUtils;
import org.bc.admin.util.borrow.SeniorCfgUtils;
import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.exception.AppException;
import org.llw.com.exception.DuoduoError;
import org.llw.com.security.MD5Util;
import org.llw.com.util.DateTimeUtil;
import org.llw.com.util.NumberUtil;
import org.llw.com.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
public class ApplyService extends BaseService {
	private static final String NAMESPACE = "APPLY";

	/**
	 * 查寻数据
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 分页查寻数据
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * 查寻分页统计数据
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * 添加数据处理
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("applyTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * 修改数据处理
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		return super.update(params, NAMESPACE);
	}
	
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("applyId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * queryApplyStatus
	 * @param params
	 * @return
	 */
	public AppResult queryApplyStatus(AppParam params) {
		return super.query(params, NAMESPACE, "queryApplyStatus");
	}
	
	private void emptyToNull(Map<String, Object> row) {
		if (StringUtils.isEmpty(row.get("telephone"))) {
			row.remove("telephone");
		}
		
		if (StringUtils.isEmpty(row.get("applyName"))) {
			row.remove("applyName");
		}
		
		if (!StringUtils.isEmpty(row.get("workType"))) {
			int workType = NumberUtil.getInt(row.get("workType"), 1);
			if (workType == 5) {
				row.put("workType", 1);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("houseType"))) {
			int houseType = NumberUtil.getInt(row.get("houseType"), 2);
			if (houseType > 4) {
				row.put("houseType", 1);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("carType"))) {
			int carType = NumberUtil.getInt(row.get("carType"), 2);
			if (carType == 1) {
				row.put("carType", 3);
			}else if (carType == 5) {
				row.put("carType", 2);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("creditType"))) {
			int creditType = NumberUtil.getInt(row.get("creditType"), 2);
			if (creditType == 3) {
				row.put("creditType", 5);
			}
		}
		
		
		String applyIp = StringUtil.getString(row.get("applyIp"));
		if (!StringUtils.isEmpty(applyIp)) {
			int index = applyIp.indexOf(",");//多次反向代理后会有多个ip值，第一个ip才是真实ip
	        if(index != -1){
	        	row.put("applyIp", applyIp.substring(0,index));
	        }
		}
	}
	
	/**申请第一步**/
	public AppResult loanApply1(AppParam params) {
		
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		
		AppResult canResult = BorrowApplyUtils.isCanApply(telephone);
		
		if(!canResult.isSuccess()) return canResult;
		
		AppResult result = new AppResult();
	
		Object uid = canResult.getAttr("uid");
		
		double applyAmount = NumberUtil.getDouble(params.getAttr("applyAmount"), 0);
		String channelDetail = StringUtil.getString(params.getAttr("channelDetail"));
		
		if (applyAmount >= 1000) {
			params.addAttr("applyAmount", applyAmount / 1000);
		}
		
		if(StringUtils.hasText(channelDetail) && channelDetail.indexOf("?") > -1){
			params.addAttr("channelDetail", channelDetail.substring(0, channelDetail.indexOf("?")));
		}

		
		if (BorrowApplyUtils.isTestUser(telephone) && StringUtils.isEmpty(params.getAttr("applyName"))) {
			params.addAttr("applyName", "内部测试");
		}
		
		emptyToNull(params.getAttr());
		
		int haveDetail = SeniorCfgUtils.haveDetail(params.getAttr());
		
		if(!StringUtils.isEmpty(uid)) {// 更新操作
			params.addAttr("uid", uid);

			this.update(params);
			
		}else {
			
			uid = getUid();
			params.addAttr("uid", uid);
			//测试号码直接相关信息，并修改状态
			if(!BorrowApplyUtils.isTestUser(telephone)) {
				////设置转单的时间
				params.addAttr("tranferTime", SeniorCfgUtils.getTranferTime(haveDetail));
			}
			result = this.insert(params);
		}
		
		result.putAttr("uid", uid);
		return result;
	}
	
	/**申请第二步**/
	public AppResult loanApply2(AppParam params) {
		AppResult result = new AppResult();
		Object uid = params.getAttr("uid");
		
		AppParam queryParam = new AppParam();
		queryParam.addAttr("uid", uid);
		AppResult queryResult = this.query(queryParam);
		int size = queryResult.getRows().size();//判断uid是否正确
		if (size <= 0) {
			result.setSuccess(false);
			result.setMessage("数据保存错误，请稍后再试!");
			return result;
		}
		
		Map<String, Object> newApplyInfo = queryResult.getRow(0);
		int status = NumberUtil.getInt(newApplyInfo.get("status"));
		
		AppResult canResult = BorrowApplyUtils.isCanApply(uid, status);
		
		if(!canResult.isSuccess()) return canResult;

		double applyAmount = NumberUtil.getDouble(params.getAttr("applyAmount"), 0);
		if (applyAmount >= 1000) {
			params.addAttr("applyAmount", applyAmount / 10000);
		}
		
		emptyToNull(params.getAttr());
		
		int haveDetail = NumberUtil.getInt(params.getAttr("haveDetail"), 0);
		
		if (haveDetail == 0) {//如果haveDetail没传，默认为1
			haveDetail = SeniorCfgUtils.haveDetail(params.getAttr());
			
		}
		
		String telephone = StringUtil.getString(newApplyInfo.get("telephone"));
	
		//测试号码直接相关信息，并修改状态
		if(!BorrowApplyUtils.isTestUser(telephone)) {
			////设置转单的时间
			params.addAttr("tranferTime", SeniorCfgUtils.getTranferTime(haveDetail));
		}
		
		params.addAttr("haveDetail", haveDetail);
		
		result = this.update(params);
	
		result.putAttr("uid", uid);
		return result;
	}
	
	public Map<String,Object> queryBaseByUid(AppParam param) {
		Map<String,Object> map = new HashMap<String,Object>();
		AppResult queryResult = this.query(param, NAMESPACE, "queryBaseByUid");
		if (queryResult.getRows().size() > 0) {
			map = queryResult.getRow(0);
		}
		return map;
	}
	
	public AppResult queryBaseInfo (AppParam param) {
		AppResult result = new AppResult();
		result.putAttr("baseInfo", queryBaseByUid(param));
		return result;
	}
	
	private static String getUid () {
		String uid = DateTimeUtil.toStringByParttern(new Date(), DateTimeUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS)
				+ StringUtil.getUUID();
		return MD5Util.encrypt(uid);
	}
}
