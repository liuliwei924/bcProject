package org.bc.data.service.sys;


import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;
import org.llw.common.core.service.ApiBaseServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;;

@Lazy
@Service
public class AreaService extends ApiBaseServiceImpl {
	private static final String NAMESPACE = "AREA";

	public AreaService() {
		super.namespace = NAMESPACE;
	}
	
	public AppResult queryAreaShow(AppParam context) {
		return super.query(context, NAMESPACE, "queryAreaShow");
	}
	
	/***
	 * 根据树形结构查寻
	 * @param context
	 * @return
	 */
	public AppResult querySelectTree(AppParam context) {
		return super.query(context, NAMESPACE, "querySelectTree");
	}
	
	/***
	 * 分组查寻省份信息
	 * @param context
	 * @return
	 */
	public AppResult queryProvice(AppParam context) {
		return super.query(context, NAMESPACE, "queryProvice");
	}
	
	/**
	 * 查询所有信息(包括省份、城市、区域)
	 * @param context
	 * @return
	 */
	public AppResult queryAllInfo(AppParam context) {
		return super.query(context, NAMESPACE, "queryAllInfo");
	}
	
	/***
	 * 分组查寻城市信息
	 * @param context
	 * @return
	 */
	public AppResult queryGroupCity(AppParam context) {
		return super.query(context, NAMESPACE, "queryGroupCity");
	}
	
	/***
	 * 分组查寻城市的区域信息
	 * @param context
	 * @return
	 */
	public AppResult queryGroupDistrict(AppParam context) {
		return super.query(context, NAMESPACE, "queryGroupDistrict");
	}

	public AppResult queryProvinceByCity (AppParam params){
		AppResult result = new AppResult();
		AppResult queryResult = this.query(params, NAMESPACE, "queryProvinceByCity");
		if (queryResult.getRows().size() > 0) {
			result.putAttr("province", queryResult.getRow(0).get("nameCn"));
		}
		return result;
	}
}
