package org.bc.web.action.schedule.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bc.admin.util.constant.BorrowConstant;
import org.bc.admin.util.constant.PushType;
import org.bc.admin.util.sys.ServiceKey;
import org.bc.admin.util.sys.SysParamsUtil;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.http.HttpClientUtil;
import org.llw.com.security.BASE64;
import org.llw.com.security.MD5Util;
import org.llw.com.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 推送福米数据得job
 * @author HP
 *
 */
@Component
@Slf4j
public class FumiTaskJob {
	private static final String Req_BASE_URL = "http://www.fumi88.com:8011/channel";
	
	private static final String SUCCESS = "000";
	
	@SuppressWarnings("unchecked")
	public void execJob(){
		long startTime = System.currentTimeMillis();
		
		 log.info("***************转福任务 start*********************");
		 if(!SysParamsUtil.getBoleanByKey("TRANFER_FUMI_JOB_ENABLE", false)) {
			 log.info("***************转福米单任务未开启*********************");
			 return;
		 }
		 
		 List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		 getPushData(dataList);
		 int total = 0;
		 int successTotal = 0;
		 int failTotal = 0;
		 
		 if(dataList != null && dataList.size() > 0){
			 total = dataList.size();
			 String md5Key = SysParamsUtil.getStringParamByKey("TRANFER_FUMI_MD5_KEY", "fumiImport");
			 String dataJson = null;
			 Map<String,String> retMap = null;
			 String base64DataStr;
			
			Map<String,String> paramMap = new HashMap<String,String>();
			 paramMap.put("data", MD5Util.encrypt(md5Key));
			 
			 AppParam updatePushParams = new AppParam("applyPushRecordService", "update");
			 
			for(Map<String,Object> dataMap : dataList){
				try {
					 Object pushId = dataMap.remove("pushId");
					 dataJson = JsonUtil.getInstance().object2JSON(dataMap);
					 base64DataStr = BASE64.getEncoder().encodeToString(dataJson.getBytes("UTF-8"));
					 paramMap.put("list", base64DataStr);
					 try{
						 String jsonStr = HttpClientUtil.getInstance().sendHttpPost(Req_BASE_URL, paramMap);
						 retMap = JsonUtil.getInstance().json2Object(jsonStr, Map.class);
					 }catch (Exception e) {
						 log.error("福米入库失败,返回信息={}",retMap,e);
						
						 retMap = new HashMap<String,String>();
						 retMap.put("returnCode", "0999");
						 retMap.put("returnMsg", "福米请求接口异常");
					 }
					 
					 if(SUCCESS.equals(retMap.get("returnCode"))){
						 updatePushParams.addAttr("status", BorrowConstant.PushStatus.PUSH_SUCCESS);
						 successTotal++;
						
					 }else{
						 updatePushParams.addAttr("status", BorrowConstant.PushStatus.PUSH_FAIL); 
						 updatePushParams.addAttr("errMsg", retMap.get("returnMsg"));
						 failTotal++;
					 }
					 
					 updatePushParams.addAttr("pushId", pushId);
					 updatePushParams.addAttr("addPushCount", 1);
					 updatePushParams.addAttr("pushType", PushType.FUMI.getPushType());
					 updatePushParams.addAttr("pushTypeName", PushType.FUMI.getPushTypeName());
					 
					 ServiceKey.doCall(updatePushParams, ServiceKey.Key_data);

				} catch (Exception e) {
					log.error("福米更新转单数据失败,返回信息={}",retMap,e);
					continue;
				}
				
			}

		 }
		 
		 log.info("此批次总单量：{},成功笔数：{}，失败笔数：{}，耗时：{} 毫秒",
				 total,successTotal,failTotal,(System.currentTimeMillis()-startTime));
	}
	
	public void getPushData(List<Map<String,Object>> dataList){
		AppParam pushParams = new AppParam("applyPushRecordService", "queryPushList");
		//未处理
		pushParams.addAttr("status", BorrowConstant.PushStatus.NO_PUSH+"");
		pushParams.addAttr("isRepeat", "0");// 0-不重复的 1-重复
		pushParams.addAttr("limitMin", SysParamsUtil.getIntParamByKey("FUMI_TRA_LIMIT_MIN", 5));
		pushParams.addAttr("limitSize", 80);
		AppResult result = ServiceKey.doCallNoTx(pushParams, ServiceKey.Key_data);
		
		int size = result.getRows().size();
		if(size > 0){
			
			Map<String,Object> fumiMap = null;
			AppResult updateResult = null;
			AppParam updateParam = new AppParam("applyPushRecordService","update");
			
			for(Map<String,Object> dataMap : result.getRows()){
				
				if(StringUtils.isEmpty(dataMap.get("telephone")) || 
						StringUtils.isEmpty(dataMap.get("applyName")) ||
						StringUtils.isEmpty(dataMap.get("applyAmount"))){
					continue;
				}
				
				updateParam.addAttr("pushId", dataMap.get("pushId"));
				updateParam.addAttr("fromStatus", BorrowConstant.PushStatus.NO_PUSH+"");
				updateParam.addAttr("status", BorrowConstant.PushStatus.PUSH_ING);
				updateResult = ServiceKey.doCall(updateParam, ServiceKey.Key_data);
				
				if(updateResult.getUpdateCount() > 0){//开始封装数据
					fumiMap = new HashMap<String,Object>();
					
					fumiMap.put("user_name", dataMap.get("applyName"));
					fumiMap.put("user_phone", dataMap.get("telephone"));
					fumiMap.put("amount", dataMap.get("applyAmount"));
					//公积金 1-有 0否
					fumiMap.put("is_reserved", changeDataType0(dataMap.get("fundType")));
					//社保 1-有 0否
					fumiMap.put("is_security", changeDataType0(dataMap.get("socialType")));
					//微粒贷额度
					fumiMap.put("weilidai_limit", dataMap.get("haveWeiLi"));
					//芝麻信用
					fumiMap.put("sesame_points", dataMap.get("zimaScore"));
					//房产
					ChangeDataBean houseBean = changeType(dataMap.get("houseType"), "house");
					fumiMap.put("is_house", houseBean.getIsHave());
					fumiMap.put("house_type", houseBean.getHaveName());
					
					//车
					ChangeDataBean carBean =changeType(dataMap.get("carType"),"car");
					fumiMap.put("is_car", carBean.getIsHave());
					fumiMap.put("car_type", carBean.getHaveName());
					
					ChangeDataBean insBean = changeType(dataMap.get("insurType"),"insurance");
					fumiMap.put("is_insurance", insBean.getIsHave());
					fumiMap.put("insurance_type", insBean.getHaveName());
					
					fumiMap.put("birth", dataMap.get("birthday"));
					fumiMap.put("area", dataMap.get("cityName"));
					fumiMap.put("pushId", dataMap.get("pushId"));
					dataList.add(fumiMap);
				}
			}
		}
		
	}
	
	
	static class ChangeDataBean{
		private int isHave;
		
		private String haveName;
		
		public ChangeDataBean(){}
		public ChangeDataBean(int isHave,String haveName){
			this.isHave = isHave;
			this.haveName = haveName;
		}

		public int getIsHave() {
			return isHave;
		}

		public void setIsHave(int isHave) {
			this.isHave = isHave;
		}

		public String getHaveName() {
			return haveName;
		}

		public void setHaveName(String haveName) {
			this.haveName = haveName;
		}
		
	}
	
	
	private static ChangeDataBean changeType(Object data,String typeStr){

		ChangeDataBean bean = new ChangeDataBean();
		if("car".equalsIgnoreCase(typeStr)){
			if(!StringUtils.isEmpty(data)){
				int dataInt = Integer.parseInt(data.toString());
				
				if(dataInt ==3){
					bean.setIsHave(0);
					bean.setHaveName("贷款车");
				}else if(dataInt ==4){
					bean.setIsHave(0);
					bean.setHaveName("全款车");
				}else if(dataInt>=5){
					bean.setIsHave(0);
					bean.setHaveName("其他房");
				}
			}
			
			if(StringUtils.isEmpty(bean.getHaveName())){
				bean.setIsHave(1);
				bean.setHaveName("无车");
			}
			
		}else if("house".equalsIgnoreCase(typeStr)){
			if(!StringUtils.isEmpty(data)){
				int dataInt = Integer.parseInt(data.toString());
				
				if(dataInt ==0 ||dataInt ==2){
					bean.setIsHave(0);
					bean.setHaveName("无房");
				}
				if(dataInt ==3){
					bean.setIsHave(1);
					bean.setHaveName("商品房（有贷款）");
				}
				
				if(dataInt ==4){
					bean.setIsHave(1);
					bean.setHaveName("商品房（无贷款）");
				}
				if(dataInt>=5){
					bean.setIsHave(1);
					bean.setHaveName("其他房");
				}
			}
			
			if(StringUtils.isEmpty(bean.getHaveName())){
				bean.setIsHave(0);
				bean.setHaveName("无房");
			}
		}else if("insurance".equalsIgnoreCase(typeStr)){
			if(!StringUtils.isEmpty(data)){
				int dataInt = Integer.parseInt(data.toString());
				if(dataInt ==1){
					bean.setIsHave(0);
					bean.setHaveName("寿险 ");
				}else if(dataInt ==2){
					bean.setIsHave(0);
					bean.setHaveName("财险");
				}
				
			}
			
			if(StringUtils.isEmpty(bean.getHaveName())){
				bean.setIsHave(1);
				bean.setHaveName("无保险");
			}
		}
		
		return bean;
	}
	
	public static int changeDataType0(Object data){
		if(!StringUtils.isEmpty(data) && 1 == Integer.parseInt(data.toString()))
			return 1;
		else
			return 0;
	}
}
