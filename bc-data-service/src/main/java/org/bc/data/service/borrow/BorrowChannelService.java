package org.bc.data.service.borrow;

import java.util.Date;
import java.util.Map;

import org.bc.admin.util.borrow.BorrowChannelUtil;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.exception.AppException;
import org.llw.com.exception.DuoduoError;
import org.llw.com.exception.SysException;
import org.llw.com.util.StringUtil;
import org.llw.com.web.session.DuoduoSession;
import org.llw.common.core.service.ApiBaseServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Lazy
@Service
public class BorrowChannelService extends ApiBaseServiceImpl {
	private static final String NAMESPACE = "BORROWCHANNEL";

	public BorrowChannelService() {
		super.namespace = NAMESPACE;
	}
	
	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult exsitChannel(AppParam params) {
		return super.query(params, NAMESPACE,"exsitChannel");
	}
	

	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		Object channelCode = params.getAttr("channelCode");
		if(StringUtils.isEmpty(channelCode)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("channelCode", channelCode);
		AppResult queryResult = this.exsitChannel(queryParam);
		if(queryResult.getRows().size() > 0){
			Map<String,Object> channelMap = queryResult.getRow(0);
			if(channelMap != null && !StringUtils.isEmpty(channelMap.get("channelCodes"))){
				String channelCodes = StringUtil.getString(channelMap.get("channelCodes"));
				throw new SysException("跟渠道[" + channelCodes +"]存在包含关系，添加无效");
			}
		}
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(params);
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannel();
		}
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		Object channelCode = params.getAttr("channelCode");
		if(StringUtils.isEmpty(channelCode)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppResult result = this.update(params);
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannel();
		}
		return result;
	}
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("channelCode", id);
				
				result = super.delete(param);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("channelCode"))) {
			result = super.delete(params);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		BorrowChannelUtil.refreshBorrowChannel();
		return result;
	}
}
