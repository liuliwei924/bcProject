package org.bc.admin.util.sys;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppParam;
import org.llw.com.context.AppProperties;
import org.llw.com.context.AppResult;
import org.llw.com.core.service.RemoteInvoke;
import org.llw.com.util.StringUtil;
import org.llw.com.web.page.PageUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

public class AreaUtils {
	
	public final static int timeOut = 60*60*8;
	private final static  String KEY_PROVICE = "ProviceList_";
	private final static  String KEY_All_CITY = "AllCity_";
	private final static  String KEY_All_CITY_AREA = "AllCityArea_";
	private final static  String KEY_All_AREA_INFO = "AllAreaInfo_";
	private final static  String KEY_All_PROVICE_AREA_INFO = "AllProviceAreaInfo_";
	
	
	
	/****
	 * 返回所有的城市 根据首字母区分
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getAllCity(){
		List<Map<String,Object>> provice  = ((List<Map<String,Object>>) RedisUtils.getRedisService().get(KEY_All_CITY));
		if (provice == null || provice.isEmpty()) {
			provice = refreshAllCity();
		}
		return provice;
	}
	
	/****
	 * 返回所有的城市 根据首字母区分
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getAllInfo(){
		List<Map<String,Object>> provice  = ((List<Map<String,Object>>) RedisUtils.getRedisService().get(KEY_All_AREA_INFO));
		if (provice == null || provice.isEmpty()) {
			provice = refreshAllInfo();
		}
		return provice;
	}
	/****
	 * 返回所有的城市 根据首字母区分
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getAllCityArea(){
		List<Map<String,Object>> cityAreas  = ((List<Map<String,Object>>) RedisUtils.getRedisService().get(KEY_All_CITY_AREA));
		if (cityAreas == null || cityAreas.isEmpty()) {
			cityAreas = queryCityDistrict(null);
		}
		return cityAreas;
	}
	
	/****
	 * 返回所有的省份
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getProvice(){
		List<Map<String,Object>> provice  = ((List<Map<String,Object>>) RedisUtils.getRedisService().get(KEY_PROVICE));
		if (provice == null || provice.isEmpty()) {
			provice = refreshProvice();
		}
		return provice;
	}
	
	/****
	 * 返回相应省份下的城市
	 * @param proviceCode 省份代码
	 * @return
	 */
	public static List<Map<String,Object>> getProviceCity(String proviceCode){
		AppParam query = new AppParam();
		query.setService("areaService");
		query.setMethod("query");
		query.setRpcServiceName(AppProperties.
				getProperties(DuoduoConstant.RPC_SERVICE_START+ ServiceKey.Key_sys));
		query.addAttr("level", "2");
		query.addAttr("tempPcode", proviceCode);
		query.setOrderBy("displayOrder");
		query.setOrderValue(PageUtil.ORDER_ASC);
		AppResult result = RemoteInvoke.getInstance().call(query);
		List<Map<String, Object>> rows = result.getRows();
		List<Map<String,Object>>  properties = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map: rows){
			Map<String,Object>  p = new HashMap<String,Object> ();
			 p.put("value", map.get("code"));
			 p.put("lable", map.get("nameCn"));
			 p.put("param", map.get("nameEn"));
			 p.put("config", map.get("shortName"));
			properties.add(p);
		}
		
		return properties;
	}
	
	/**
	 * 根据城市名称获取代号
	 * @param cityName
	 * @return
	 */
	public static String getCityCode(String cityName){
		AppParam param = new AppParam();
		param.setService("areaService");
		param.setMethod("query");
		param.addAttr("nameCn", cityName);
		param.addAttr("level", "2");
		AppResult result = RemoteInvoke.getInstance().call(param);
		if(result.getRows().size() > 0){
			return result.getRow(0).get("code").toString();
		}
		return null;
	}
	
	/***
	 * 刷新省份
	 * @return
	 */
	private static List<Map<String,Object>> refreshProvice() {
		AppParam query = new AppParam();
		query.setService("areaService");
		query.setMethod("query");
		query.addAttr("level", "1");
		query.setOrderBy("displayOrder");
		query.setOrderValue(PageUtil.ORDER_ASC);
		AppResult result = RemoteInvoke.getInstance().call(query);
		List<Map<String,Object>> rows = result.getRows();
		List<Map<String,Object>>  properties = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map: rows){
			Map<String,Object>  p = new HashMap<String,Object> ();
			 p.put("value", map.get("code"));
			 p.put("lable", map.get("nameCn"));
			 p.put("param", map.get("nameEn"));
			 p.put("config", map.get("shortName"));
			properties.add(p);
		}
		
		RedisUtils.getRedisService().set(KEY_PROVICE, properties, timeOut);
		return properties;
	}
	
	
	/***
	 * 刷新所有省份、城市、区域信息
	 * @return
	 */
	private static List<Map<String,Object>> refreshAllInfo() {
		AppParam query = new AppParam();
		query.setService("areaService");
		query.setMethod("queryAllInfo");
		AppResult result = RemoteInvoke.getInstance().call(query);
		RedisUtils.getRedisService().set(KEY_All_AREA_INFO, (Serializable) result.getRows(), timeOut);
		return result.getRows();
	}	
	
	
	/***
	 * 刷新所有城市
	 * @return
	 */
	private static List<Map<String,Object>> refreshAllCity() {
		AppParam query = new AppParam();
		query.setService("areaService");
		query.setMethod("queryGroupCity");
		AppResult result = RemoteInvoke.getInstance().call(query);
		for(Map<String,Object> row: result.getRows()){
			Object cityName = row.remove("cityName");
			row.put("cityNames", cityName.toString().split(","));
		}
		RedisUtils.getRedisService().set(KEY_All_CITY, (Serializable) result.getRows(), timeOut);
		return result.getRows();
	}	
	
	
	/***
	 * 获取城市下的区域信息，不传cityName则返回所有的
	 * @param cityName
	 * @return
	 */
	public static List<Map<String,Object>> queryCityDistrict(String cityName) {
		AppParam query = new AppParam();
		query.setService("areaService");
		query.setMethod("queryGroupDistrict");
		query.addAttr("cityName", cityName);
		AppResult result = RemoteInvoke.getInstance().call(query);
		for(Map<String,Object> row: result.getRows()){
			Object areas = row.remove("areas");
			row.put("areas", areas.toString().split(","));
		}
		if(StringUtils.isEmpty(cityName)){
			RedisUtils.getRedisService().set(KEY_All_CITY_AREA, (Serializable) result.getRows(), timeOut);
		}
		return result.getRows();
	}	
	
	/**
	 * 根据部分城市名称查询完整的城市名
	 * @return
	 */
	public static String getLikeCityName (String cityName) {
		String result = null;
		AppParam query = new AppParam();
		query.setService("areaService");
		query.setMethod("query");
		query.addAttr("level", "2");
		query.addAttr("nameCn", cityName);
		AppResult queryResult = RemoteInvoke.getInstance().call(query);
		if (queryResult.getRows().size() > 0) {
			result = StringUtil.getString(queryResult.getRow(0).get("nameCn"));
		}
		return result;
	}
	
	/****
	 * 根据代号获取省份代码
	 * @param codeProvice
	 * @return
	 */
	public static String getProviceCode(String proviceName){
		List<Map<String,Object>> provice  = getProvice();;
		if(StringUtils.isEmpty(proviceName)){
			return "";
		}
		for(Map<String,Object> prov: provice){
			if(StringUtil.getString(prov.get("nameCn")).indexOf(proviceName)>=0){
				return StringUtil.getString(prov.get("code"));
			}
		}
		return "";
	}
	/****
	 * 根据代号获取省份代码
	 * @param codeProvice
	 * @return
	 */
	public static Map<String,Object> getProviceMo(String proviceName){
		List<Map<String,Object>> provice  = getProvice();;
		if(StringUtils.isEmpty(proviceName)){
			return null;
		}
		for(Map<String,Object> prov: provice){
			if(StringUtil.getString(prov.get("nameCn")).indexOf(proviceName)>=0){
				return prov;
			}
		}
		return null;
	}
	/****
	 * 返回省份分组的城市列表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getAllCityAndProvince(){
		List<Map<String,Object>> areas  = ((List<Map<String,Object>>) RedisUtils.getRedisService().get(KEY_All_PROVICE_AREA_INFO));
		if (areas == null || areas.isEmpty()) {
			areas = refreshAllCityAndProvince();
		}
		return areas;
	}
	/***
	 * 刷新省份分组的城市列表
	 */
	public static List<Map<String,Object>> refreshAllCityAndProvince(){
		List<Map<String,Object>> allArea = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> provinceList = AreaUtils.getAllInfo();
		Map<String,List<Map<String,Object>>> citysMap = new HashMap<String, List<Map<String,Object>>>();
		for(Map<String,Object> row : provinceList){
			String provinceName = row.remove("provinceName").toString();
			String[] districts = row.remove("districtNames").toString().split(",");
			row.put("districts", districts);
			if(citysMap.containsKey(provinceName)){
				citysMap.get(provinceName).add(row);
			}else{
				List<Map<String,Object>> temp = new ArrayList<Map<String,Object>>();
				temp.add(row);
				citysMap.put(provinceName, temp);
			}
		}
		for (String key : citysMap.keySet()) {
			Map<String,Object> item = new HashMap<String, Object>();
			item.put("provinceName", key);
			item.put("citys", citysMap.get(key));
			allArea.add(item);
		}
		
		RedisUtils.getRedisService().set(KEY_All_PROVICE_AREA_INFO, (Serializable) allArea, timeOut);
		
		return allArea;
	}
}
