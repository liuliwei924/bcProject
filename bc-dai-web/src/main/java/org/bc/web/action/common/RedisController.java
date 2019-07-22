package org.bc.web.action.common;

import org.llw.com.context.AppResult;
import org.llw.com.exception.ExceptionUtil;
import org.llw.com.web.session.DuoduoSession;
import org.llw.common.web.base.BaseController;
import org.llw.model.cache.RedisUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/account/config/")
@Slf4j
public class RedisController extends BaseController{
	
	/**
	 * 查询缓存
	 * @param request
	 * @return
	 */
	@RequestMapping("redis/query")
	public AppResult query(){
		AppResult result = new AppResult();
		try {	
			String key = request.getParameter("key");
			Object value = RedisUtils.getRedisService().get(key);
			
			result.putAttr("objectType", "String");
			result.putAttr("values", value);
		} catch (Exception e) {
			log.error("查询缓存错误",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除缓存
	 * @param request
	 * @return
	 */
	@RequestMapping("redis/delete")
	public AppResult delete(){
		AppResult result = new AppResult();
		try {	
			String key = request.getParameter("key");
			boolean del = RedisUtils.getRedisService().del(key);
			result.setSuccess(del);
		} catch (Exception e) {
			log.error("删除缓存错误",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 设置缓存
	 * @param request
	 * @return
	 */
	@RequestMapping("redis/update")
	public AppResult update(){
		AppResult result = new AppResult();
		try {	
			String key = request.getParameter("key");
			String value = request.getParameter("values");
			
			RedisUtils.getRedisService().set(key, value);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error("设置缓存错误",e);
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
