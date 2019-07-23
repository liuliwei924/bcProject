package org.bc.web.action.timer;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bc.admin.util.borrow.BorrowChannelUtil;
import org.bc.admin.util.constant.BorrowConstant;
import org.bc.admin.util.constant.SysParamConstont;
import org.bc.admin.util.sys.ServiceKey;
import org.bc.admin.util.sys.SysParamsUtil;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.com.http.HttpClientUtil;
import org.llw.com.security.MD5Util;
import org.llw.com.util.DateTimeUtil;
import org.llw.com.util.JsonUtil;
import org.llw.com.util.StringUtil;
import org.llw.model.cache.RedisLock;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 传输数据的定时器,15秒跑一次
 * @author liulw 2019-07-21
 *
 */
@Slf4j
public class TranferDataTimer implements Runnable {

	private static final String Req_BASE_URL = "https://kefu.bona09.com/kf/cooper/org/thirdData/";
	
	private static final String SUCCESS = "000";
	
	private static final int SLEEP_TIME = 15000;
	
	private static final String timer_lock_key = "timer_lock_key";
	
	private RedisLock redisLock = new RedisLock();
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		log.info("***************************开始数据传输**************");
		
		while (true) {
			boolean isLock = false;
			try{
				
				isLock = redisLock.lock(timer_lock_key, 1);
				if(!isLock) continue;
				
				log.info("***************获取到锁*********************");
				
				 if(!SysParamsUtil.getBoleanByKey(SysParamConstont.TRANFER_ORDER_JOB_ENABLE, false)) {
					 log.info("***************转单任务未开启*********************");
					
					 try {
							Thread.sleep(56000);//睡15秒，相当于15秒跑一次
						} catch (InterruptedException e) {
							e.printStackTrace();
							//Thread.currentThread().interrupt();
						}
					 
					 continue;
				 }
				
				AppParam traferParams = new AppParam("applyService", "query");
				//未处理和传输失败的
				traferParams.addAttr("inStatus", BorrowConstant.ApplyStatus.NO_Handle +","
				+ BorrowConstant.ApplyStatus.Fail);
				traferParams.addAttr("isRepeat", 0);// 0-不重复的 1-重复
				traferParams.addAttr("beginTranferTime", new Date());
				traferParams.addAttr("limitSize", 100);
				traferParams.setOrderBy("applyTime");
				traferParams.setOrderValue("DESC");
				
				AppResult result = ServiceKey.doCallNoTx(traferParams, ServiceKey.Key_data);
				
				if(result.isSuccess() && 
						result.getRows() != null && result.getRows().size() > 0) {
					
					int successTotal  = 0;
					int failTotal  = 0;
					int total  = result.getRows().size();
					long startTime = System.currentTimeMillis();
					Map<String,Object> paramMap = null;
					Map<String,Object> retMap = null;
					
					for(Map<String,Object> tranferMap : result.getRows()) {
						Object applyId = tranferMap.get("applyId");
						
						paramMap = new LinkedHashMap<String,Object>();
						String channelCode = StringUtil.getString(tranferMap.get("channelCode"));
						if(StringUtils.isEmpty(channelCode)) {
							paramMap.put("applyId", applyId);
							paramMap.put("returnCode", "099");
							paramMap.put("errorMessage", "大渠道为空");
							
							packRetResult(paramMap);
							failTotal++;
							continue;
						}
						
						
						try {
							String req_url = Req_BASE_URL + channelCode;
							
							Map<String,Object> channelMap = BorrowChannelUtil.getChannelByCode(channelCode);
							
							String merchId = StringUtil.getString(channelMap.get("merchId"));
							String telephone = tranferMap.get("telephone").toString();
							String time = DateTimeUtil.toStringByParttern(new Date(), 
									DateTimeUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS);
							//数据签名
							String sign = MD5Util.getEncryptByKey(telephone + "&" + time, merchId);
							
							paramMap.put("time", time);
							paramMap.put("telephone", telephone);
							paramMap.put("sign", sign);
							paramMap.put("applyName", tranferMap.get("applyName"));
							paramMap.put("loanAmount", tranferMap.get("applyAmount"));
							paramMap.put("sex", tranferMap.get("sex"));
							paramMap.put("birthday", tranferMap.get("birthday"));
							paramMap.put("cityName", tranferMap.get("cityName"));
							paramMap.put("workType", tranferMap.get("workType"));
							paramMap.put("socialType", tranferMap.get("socialType"));
							paramMap.put("fundType", tranferMap.get("fundType"));
							paramMap.put("houseType", tranferMap.get("houseType"));
							paramMap.put("carType", tranferMap.get("carType"));
							paramMap.put("creditType", tranferMap.get("creditType"));
							paramMap.put("income", tranferMap.get("income"));
							paramMap.put("jobMonth", tranferMap.get("jobMonth"));
							paramMap.put("cashMonth", tranferMap.get("cashMonth"));
							paramMap.put("wagesType", tranferMap.get("wagesType"));
							paramMap.put("insurType", tranferMap.get("insurType"));
							paramMap.put("age", tranferMap.get("age"));
							paramMap.put("applyIp", tranferMap.get("applyIp"));
							paramMap.put("channelDetail", tranferMap.get("channelDetail"));
							paramMap.put("haveWeiLi", tranferMap.get("haveWeiLi"));
							paramMap.put("identifyNo", tranferMap.get("identifyNo"));
							
							String jsonStr = HttpClientUtil.getInstance().sendHttpPost(req_url, paramMap);
							
							retMap = JsonUtil.getInstance().json2Object(jsonStr, Map.class);
						}catch (Exception e) {
							retMap = new HashMap<String,Object>();
							retMap.put("applyId", applyId);
							retMap.put("returnCode", "098");
							retMap.put("errorMessage", "转单报错");
							
							log.error("转单报错：",e);
							
						}finally {
							boolean suc = false;
							try {
								suc = packRetResult(retMap);
							}catch (Exception e) {
								log.error("更新转单状态报错，applyId=[{}]：",applyId,e);
							}
							
							if(suc) successTotal++; else failTotal++;
						}
						
					}
					
					log.info("此批次总单量：{}笔,成功转单：{}笔,失败转单：{}笔，耗时：{} 毫秒", 
							total,successTotal,failTotal,(System.currentTimeMillis()-startTime));
				}	
				
				
			}finally {
				
				if(isLock){
					
					redisLock.unlock(timer_lock_key, 1);
				} else{
					log.info("***************为获取到锁，sleep*********************");
				}
				try {
					Thread.sleep(SLEEP_TIME);//睡15秒，相当于15秒跑一次
				} catch (InterruptedException e) {
					e.printStackTrace();
					//Thread.currentThread().interrupt();
				}
				
			}
			
		}
		
	}
	
	/**
	 * 处理结果
	 * @param retMap
	 * @return
	 */
	private boolean packRetResult(Map<String,Object> retMap) {
		Object applyId = retMap.get("applyId");
		
		String returnCode = StringUtil.getString(retMap.get("returnCode"));
		String errorMessage = StringUtil.getString(retMap.get("errorMessage"));
		
		boolean isSuccess = SUCCESS.equals(returnCode);
		
		int updateStatus = isSuccess? 
				BorrowConstant.ApplyStatus.Success :BorrowConstant.ApplyStatus.Fail;
		
		AppParam appParam = new AppParam("applyService", "update");
		appParam.addAttr("applyId", applyId);
		appParam.addAttr("status", updateStatus);
		appParam.addAttr("errMsg", errorMessage+"[" + returnCode + "]");
		appParam.addAttr("tranTime", new Date());
		
		if("003".equals(returnCode)) {
			appParam.addAttr("isRepeat", 1);// 申请重复
		}
		
		ServiceKey.doCall(appParam, ServiceKey.Key_data);
		
		return isSuccess;
				
	}

}
