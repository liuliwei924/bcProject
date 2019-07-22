package org.bc.web.action.intercepter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.llw.com.web.session.DuoduoSession;
import org.llw.com.web.session.RequestUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;
/***
 * handler page Interceptor
 * 
 * @author liulw 2019-07-20
 * 
 */
@Slf4j
public class MyHandlerInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) {
		
		String url = request.getRequestURI();
	
		//获取请求参数
		String params = RequestUtil.getRequestParams(request, true);
		String ipAddress = DuoduoSession.getIpAddress(request);
		
		log.info("reuqest url:{},ip={},参数：{}",url,ipAddress,params);
		
		// 判断是否需要登录
		//boolean needLogin = needLogin(url);
		
		//设置请求信息
		DuoduoSession.web2Service(request);
		return true;
	}

		
}
