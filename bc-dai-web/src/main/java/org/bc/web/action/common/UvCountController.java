package org.bc.web.action.common;

import javax.servlet.http.HttpServletRequest;

import org.llw.com.context.AppProperties;
import org.llw.com.context.AppResult;
import org.llw.common.web.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * uv统计
 * TODO 等后期实现 
 *
 */
@Controller
@RequestMapping("/uv/")
public class UvCountController extends BaseController {
	
	/**
	 * uv统计，每天的
	 * @param request
	 * @return
	 */
	@RequestMapping("uvRecordCount")
	@ResponseBody
	public String uvRecordCount (HttpServletRequest request) {
		//AppResult result = new AppResult();
		
		String port = AppProperties.getProperties("server.port");
		
		String sessionId = request.getSession().getId();
		String str = "port=" + port + "----session:" +sessionId;
		
		System.out.println("port=" + port + "----session:" +sessionId );
		
		//request.getSession().setAttribute("message",request.getQueryString());
		return str;
	}
	
	/**
	 * 页面统计(包含uv,pv)，每天的
	 * @param request
	 * @return
	 */
	@RequestMapping("pageRecordCount")
	@ResponseBody
	public AppResult pageRecordCount (HttpServletRequest request) {
		AppResult result = new AppResult();
		return result;
	}
	
	/**
	 * 按钮统计，每天的
	 * @param request
	 * @return
	 */
	@RequestMapping("btnRecordCount")
	@ResponseBody
	public AppResult btnRecordCount (HttpServletRequest request) {
		AppResult result = new AppResult();
		
		return result;
	}
	
	
	/**
	 * 短链接统计(包含uv,pv)，每天的
	 * @param request
	 * @return
	 */
	@RequestMapping("shortUrlCount")
	@ResponseBody
	public AppResult shortUrlCount (HttpServletRequest request) {
		AppResult result = new AppResult();
	
		return result;
	}
	/**
	 * 小小贷款点击记录
	 * @param request
	 * @return
	 */
	@RequestMapping("xxdkBtnRecordCount")
	@ResponseBody
	public AppResult xxdkBtnRecordCount (HttpServletRequest request) {
		AppResult result = new AppResult();
		
		return result;
	}
}
