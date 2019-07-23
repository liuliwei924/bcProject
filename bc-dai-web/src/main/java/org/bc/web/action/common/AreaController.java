package org.bc.web.action.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bc.admin.util.sys.AreaUtils;
import org.llw.com.context.AppResult;
import org.llw.com.exception.ExceptionUtil;
import org.llw.com.web.session.DuoduoSession;
import org.llw.common.web.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Controller()
@RequestMapping("/area/")
@Slf4j
public class AreaController extends BaseController {

	@RequestMapping("provinceList")
	@ResponseBody
	public List<Map<String,Object>> provinceList(){
		return AreaUtils.getProvice();
	}
	
	@RequestMapping("cityList")
	@ResponseBody
	public AppResult cityList(){
		AppResult result = new AppResult();
		List<Map<String,Object>> provinceList = new ArrayList<Map<String,Object>>();
		String provinceCode = request.getParameter("provinceCode");
		try {
			provinceList = AreaUtils.getProviceCity(provinceCode);
		} catch (Exception e) {
			log.error(" Execute error",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		result.putAttr("cityList", provinceList);
		return result;
	}
	
	/**
	 * 获取所有城市(剔除省份)
	 * @return
	 */
	@RequestMapping("allCity")
	@ResponseBody
	public AppResult allCity(){
		AppResult result = new AppResult();
		try {
			result.putAttr("allCity", AreaUtils.getAllCity());
		} catch (Exception e) {
			log.error(" Execute error",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 获取所有区域信息(包括省份)
	 * @return
	 */
	@RequestMapping("allAreaInfo")
	@ResponseBody
	public AppResult allAreaInfo(){
		AppResult result = new AppResult();
		try {
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
			result.putAttr("allArea", allArea);
		} catch (Exception e) {
			log.error(" Execute allAreaInfo error",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 根据城市名称查询区域
	 * @return
	 */
	@RequestMapping("cityArea/{cityName}")
	@ResponseBody
	public AppResult allCityArea(@PathVariable("cityName") String cityName){
		AppResult result = new AppResult();
		try {
			List<Map<String,Object>> disticts = AreaUtils.queryCityDistrict(cityName);
			result.putAttr("areas", disticts.get(0).get("areas"));
		} catch (Exception e) {
			log.error(" Execute error",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

}

