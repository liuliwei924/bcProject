package org.bc.web.action.common;

import java.util.Date;
import java.util.Map;

import org.bc.admin.util.constant.BorrowConstant;
import org.bc.admin.util.constant.CustConstant;
import org.bc.admin.util.sys.MapLocaltionUtil;
import org.bc.admin.util.sys.ServiceKey;
import org.bc.admin.util.sys.ValidUtils;
import org.bc.web.action.util.Key_SMS;
import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppProperties;
import org.llw.com.context.AppResult;
import org.llw.com.core.service.RemoteInvoke;
import org.llw.com.exception.ExceptionUtil;
import org.llw.com.util.NumberUtil;
import org.llw.com.util.StringUtil;
import org.llw.com.web.session.DuoduoSession;
import org.llw.com.web.session.RequestUtil;
import org.llw.common.web.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

/**
 * 贷款申请，老的接口太多，拆分一下
 * 
 * @author liulw 2019-07-20
 *
 */
@Controller
@RequestMapping("/phone/loan/")
@Slf4j
public class PhoneLoanController extends BaseController {
	
	@RequestMapping("saveSimpleInfo")
	@ResponseBody
	public AppResult saveSimpleInfo () {
		AppResult result = new AppResult();
		try {
			String defaultCity = null;//保存是传入的城市
			String cityName = request.getParameter("cityName");
			String telephone = request.getParameter("telephone");
			String applyName = request.getParameter("applyName");
			String randomNo = request.getParameter("randomNo");
			
			if (StringUtils.isEmpty(telephone)) {
				return result.retErrorResult("请输入手机号");
			}
			// 验证手机号
			if (!ValidUtils.validateTelephone(telephone)) {
				return result.retErrorResult("手机号码验证错误代码");
			}
			// 验证短信验证码
			if (!ValidUtils.validateRandomNo(randomNo,
					Key_SMS.Key_SMS_BORROW_APPLY, telephone)) {
				return result.retErrorResult("短信验证码错误");
			}
			
			//渠道号处理
			String channelDetail = request.getParameter(BorrowConstant.RegSourceType);
			if (StringUtils.isEmpty(channelDetail)) {
				channelDetail = request.getParameter("channelDetail");
			}
			
			channelDetail = StringUtils.isEmpty(channelDetail) ? CustConstant.CUST_SOURCETYPE_DEFAULT
					: channelDetail;
			
			String pageReferer = request.getParameter("pageReferer");
			if (StringUtils.isEmpty(pageReferer)) {
				pageReferer = request.getHeader("referer");
				if (!StringUtils.isEmpty(pageReferer) && pageReferer.lastIndexOf("/") != -1) {
					pageReferer = pageReferer.substring(pageReferer.lastIndexOf("/") + 1, pageReferer.length());
					if (pageReferer.indexOf("?") != -1) {
						pageReferer = pageReferer.substring(0, pageReferer.indexOf("?"));
					}
				}
			}
			
			//城市定位处理
			if (StringUtils.isEmpty(cityName)) {
				cityName = (String) MapLocaltionUtil.getLocaltion(request).get(MapLocaltionUtil.BaiDu_cityName);
			}
			defaultCity = cityName;//先使用定位城市
			Map<String, Object> telInfo = MapLocaltionUtil.getTelInfo(telephone);
			String telCity = StringUtil.getString(telInfo.get("cityName"));//获取手机归属地
			if (StringUtils.isEmpty(defaultCity)) {
				defaultCity = telCity;//如果定位城市失败，使用手机归属地
			}
			
			AppParam applyParam = new AppParam("applyService", "loanApply1");
			RequestUtil.setAttr(applyParam, request);
			applyParam.setRpcServiceName(AppProperties.getProperties(DuoduoConstant.RPC_SERVICE_START+ServiceKey.Key_data));
			applyParam.addAttr("applyName", applyName);
			applyParam.addAttr("telephone", telephone);
			applyParam.addAttr("iszhapian", telInfo.get("iszhapian"));
			applyParam.addAttr("channelDetail", channelDetail);
			applyParam.addAttr("cityName", defaultCity);
			applyParam.addAttr("userAgent", request.getHeader("user-Agent"));
			if (StringUtils.isEmpty(request.getParameter("cityArea"))) {
				applyParam.addAttr("nullArea", 1);
			}
			applyParam.addAttr("applyTime", new Date());
			applyParam.addAttr("applyIp", DuoduoSession.getIpAddress(request));
			
			applyParam.addAttr("pageReferer", pageReferer);
			result = RemoteInvoke.getInstance().call(applyParam);//保存申请基本信息
			if (result.isSuccess()) {
				int cityStatus = 0;
				int netCity = 0;
				if (StringUtils.isEmpty(cityName) || StringUtils.isEmpty(telCity)) {//判断定位城市和手机归属地是否一致
					cityStatus = 2;
					result.setMessage("手机归属地或定位城市定位不成功!");
				}else if (!telCity.equals(cityName)) {
					cityStatus = 1;
					result.setMessage("手机归属地与定位城市不符合!");
				}
				result.putAttr("telCity", StringUtils.isEmpty(telCity) ? "" : telCity);
				result.putAttr("cityName", StringUtils.isEmpty(cityName) ? "" : cityName);
				result.putAttr("cityStatus", cityStatus);

				result.putAttr("netCity", netCity);
			}
		} catch (Exception e) {
			log.error("saveSimpleInfo error",e);
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("saveOtherInfo")
	@ResponseBody
	public AppResult saveOtherInfo () {
		AppResult result = new AppResult();
		try {
			String uid = request.getParameter("uid");
			if (StringUtils.isEmpty(uid)) {
				return result.retErrorResult("uid不能为空!");
			}
			double applyAmount = NumberUtil.getDouble(request.getParameter("applyAmount"), 0);
			if (applyAmount > 999) {
				return result.retErrorResult("贷款金额不正确，注意金额以万为单位!");
			}
			AppParam param = new AppParam("applyService", "loanApply2");
			param.setRpcServiceName(AppProperties.getProperties(DuoduoConstant.RPC_SERVICE_START+ServiceKey.Key_data));
			RequestUtil.setAttr(param, request);
			result = RemoteInvoke.getInstance().call(param);

		} catch (Exception e) {
			log.error("saveSimpleInfo error",e);
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 修改城市
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("updateCity")
	@ResponseBody
	public AppResult updateCity() {
		AppResult result = new AppResult();
		try {
			String uid = request.getParameter("uid");
			String cityName = request.getParameter("cityName");
			String cityArea = request.getParameter("cityArea");
			
			if (StringUtils.isEmpty(cityName) || StringUtils.isEmpty(uid)) {
				return result.retErrorResult("缺少重要参数!");
			}
			
			AppParam updateParam = new AppParam("applyService", "update");
			updateParam.setRpcServiceName(AppProperties.getProperties(DuoduoConstant.RPC_SERVICE_START + ServiceKey.Key_data));
			updateParam.addAttr("cityName", cityName);
			if ("".equals(cityArea)) {
				updateParam.addAttr("nullArea", 1);
			}else {
				updateParam.addAttr("cityArea", cityArea);
			}
			updateParam.addAttr("uid", uid);
			RemoteInvoke.getInstance().call(updateParam);//修改申请表的城市

		} catch (Exception e) {
			log.error("checkCityName error",e);
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询申请信息
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBaseInfo")
	@ResponseBody
	public AppResult queryBaseInfo () {
		AppResult result = new AppResult();
		try {
			String uid = request.getParameter("uid");
			if (StringUtils.isEmpty(uid)) {
				return result.retErrorResult("缺少重要参数!");
			}
			AppParam queryParam = new AppParam("applyService", "queryBaseInfo");
			queryParam.setRpcServiceName(AppProperties.getProperties(DuoduoConstant.RPC_SERVICE_START + ServiceKey.Key_data));
			queryParam.addAttr("uid", uid);
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		} catch (Exception e) {
			log.error("queryBaseInfo error",e);
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 保存申请信息，不需要发送短信验证码
	 * @param request
	 * @return
	 */
	@RequestMapping("saveInfoNotMsg")
	@ResponseBody
	public AppResult saveInfoNotMsg() {
		AppResult result = new AppResult();
		try {
			String defaultCity = null;//保存是传入的城市
			String cityName = request.getParameter("cityName");//这个页面必传城市
			String telephone = request.getParameter("telephone");
			String applyName = request.getParameter("applyName");
			
			if (StringUtils.isEmpty(telephone)) {
				return result.retErrorResult("请输入手机号");
			}
			// 验证手机号
			if (!ValidUtils.validateTelephone(telephone)) {
				return result.retErrorResult("手机号格式不正确");
			}
			
			if (StringUtils.isEmpty(cityName)) {
				cityName = (String)MapLocaltionUtil.getLocaltion(request).get(MapLocaltionUtil.BaiDu_cityName);
			}
			defaultCity = cityName;//先使用定位城市
			Map<String, Object> telInfo = MapLocaltionUtil.getTelInfo(telephone);
			String telCity = StringUtil.getString(telInfo.get("cityName"));//获取手机归属地
			if (StringUtils.isEmpty(defaultCity)) {
				defaultCity = telCity;//如果定位城市失败，使用手机归属地
			}

			Object channelDetail = request.getParameter(CustConstant.RegSourceType);
			if (StringUtils.isEmpty(channelDetail)) {
				channelDetail = request.getParameter("channelDetail");
			}
			channelDetail = StringUtils.isEmpty(channelDetail) ? CustConstant.CUST_SOURCETYPE_DEFAULT
					: channelDetail;

			String pageReferer = request.getParameter("pageReferer");
			
			AppParam applyParam = new AppParam("applyService", "loanApply1");
			RequestUtil.setAttr(applyParam, request);
			applyParam.setRpcServiceName(
					AppProperties.getProperties(DuoduoConstant.RPC_SERVICE_START+ServiceKey.Key_data));
			applyParam.addAttr("applyName", applyName);
			applyParam.addAttr("telephone", telephone);
			applyParam.addAttr("channelDetail", channelDetail);
			applyParam.addAttr("cityName", cityName);
			if (StringUtils.isEmpty(request.getParameter("cityArea"))) {
				applyParam.addAttr("nullArea", 1);
			}
			applyParam.addAttr("applyTime", new Date());
			applyParam.addAttr("applyIp", DuoduoSession.getIpAddress(request));
			if (StringUtils.isEmpty(pageReferer)) {
				pageReferer = request.getHeader("referer");
				if (!StringUtils.isEmpty(pageReferer) && pageReferer.lastIndexOf("/") != -1) {
					pageReferer = pageReferer.substring(pageReferer.lastIndexOf("/") + 1, pageReferer.length());
					if (pageReferer.indexOf("?") != -1) {
						pageReferer = pageReferer.substring(0, pageReferer.indexOf("?"));
					}
				}
			}
			applyParam.addAttr("pageReferer", pageReferer);
			result = RemoteInvoke.getInstance().call(applyParam);//保存申请基本信息
			if (result.isSuccess()) {
				int cityStatus = 0;
				if (StringUtils.isEmpty(cityName) || StringUtils.isEmpty(telCity)) {//判断定位城市和手机归属地是否一致
					cityStatus = 2;
					result.setMessage("手机归属地或定位城市定位不成功!");
				}else if (!telCity.equals(cityName)) {
					cityStatus = 1;
					result.setMessage("手机归属地与定位城市不符合!");
				}
				result.putAttr("cityStatus", cityStatus);
				result.putAttr("telCity", StringUtils.isEmpty(telCity) ? "" : telCity);
				result.putAttr("cityName", StringUtils.isEmpty(cityName) ? "" : cityName);
			}
		} catch (Exception e) {
			log.error("saveInfoNotMsg error",e);
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}


}
