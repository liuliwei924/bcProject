package org.bc.admin.util.sys;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.bc.admin.util.constant.CustConstant;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.llw.com.context.AppProperties;
import org.llw.com.util.JsonUtil;
import org.llw.com.util.NumberUtil;
import org.llw.com.util.StringUtil;
import org.llw.com.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public class MapLocaltionUtil {

	private static final String getBaduAk(){
		return"D8zpsEKQlrGxUvuHMNZjkACLggxecoCU"; 
	}
	
	
	// 百度URL
	private static final String BaiDu_URL = "http://api.map.baidu.com/location/ip";

	// 定位状态 1-后台定位成功  2-前端定位成功
	public static final String BaiDu_STATUS = "BaiDu_STATUS";

	// 经度
	public static final String BaiDu_longitude = "BaiDu_longitude";

	// 纬度
	public static final String BaiDu_latitude = "BaiDu_latitude";

	// 省份
	public static final String BaiDu_province = "BaiDu_province";

	// 城市
	public static final String BaiDu_cityName = "BaiDu_cityName";

	// 选择的城市
	public static final String select_cityName = "select_cityName";

	// 区域
	public static final String BaiDu_area = "BaiDu_area";

	// 百度位置信息
	public static final String BaiDu_Location_info = "BaiDu_Location_info";
	// 百度位置信息
	public static final String BaiDu_Location_Errorinfo = "BaiDu_Location_Errorinfo";

	// 百度使用的IP
	public static final String BaiDu_SessionIp = "BaiDu_SessionIp";
	
	// app 使用的缓存key
	public static final String BaiDu_APP_CACHE = "BaiDu_APP_CACHE";
	
	// 缓存时间修改为30分钟
	public static final int CACHE_TIME = 30*60;
	
	/**
	 * 设置城市信息和经纬度(兼容老版本)
	 * @param ip
	 * @param request
	 */
	public static void sendBaiDuLocaltion(String ip) {
		sendBaiDuLocaltion(ip, DuoduoSession.getHttpRequest());
	}
	
	/**
	 * 设置城市信息和经纬度
	 * @param ip
	 * @param request
	 */
	public static void sendBaiDuLocaltion(String ip,HttpServletRequest request) {
		try {
			if(StringUtils.isEmpty(ip)){
				Map<String,Object >localMap = getInitMap();
				// 设置session信息
				setLocationInfo(localMap, request);
				return;
			}
			ip = ip.split(",")[0];
			Map<String, Object> baiduMap = getLocationInfo(request);
			if(baiduMap==null || baiduMap.isEmpty()){
				baiduMap = getBaiduAddress(ip);
				setLocationInfo(baiduMap, request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置城市信息和经纬度
	 * @param ip
	 * @param request
	 */
	public static Map<String, Object> getLocaltion(HttpServletRequest request) {
		try {
			String ip = DuoduoSession.getClientIp();
			ip = ip.split(",")[0];
			Map<String, Object> baiduMap = getBaiduAddress(ip);
			if (baiduMap == null) { //
				return new HashMap<String, Object>();
			}
			return baiduMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap<String, Object>();
	}

	
	public static String getLongitude(HttpServletRequest request) {
		Object value = getLocationInfo(request).get(BaiDu_longitude);
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	public static String getLatitude(HttpServletRequest request) {
		Object value = getLocationInfo(request).get(BaiDu_latitude);
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	public static String getCityName(HttpServletRequest request) {
		return (String) getLocationInfo(request).get(BaiDu_cityName);
	}
	
	public static String getProvice(HttpServletRequest request) {
		return (String) getLocationInfo(request).get(BaiDu_province);
	}

	public static String getSelectCityName(HttpServletRequest request) {
		return (String) getLocationInfo(request).get(select_cityName);
	}
	
	public static Map<String, Object> getLocationInfo(HttpServletRequest request) {
		Map<String, Object> locaMap =(Map<String, Object>) request.getSession().getAttribute(BaiDu_Location_info);
		if (locaMap == null || locaMap.isEmpty()) {
			String signId = (String) request.getAttribute(CustConstant.USER_SIGNID);
			String phoneUUID = request.getParameter("UUID");
			if(StringUtils.hasText(signId)){
				locaMap = (Map<String, Object>) RedisUtils.getRedisService().get(
						BaiDu_Location_info + signId);
			}else if(StringUtils.hasText(phoneUUID)){//兼容app老版本（没有signID）
				locaMap = (Map<String, Object>) RedisUtils.getRedisService().get(
						BaiDu_Location_info+phoneUUID);
			}
		}
		return locaMap == null ? new HashMap<String, Object>() : locaMap;
	}

	public static void setLocationInfo(Map<String, Object> values, HttpServletRequest request) {
		String signId = (String) request.getAttribute(CustConstant.USER_SIGNID);
		String phoneUUID = request.getParameter("UUID");
		if(values ==null){
			values = getInitMap();
			values.put("status", "-1");
		}
		if(StringUtils.hasText(signId)){
			RedisUtils.getRedisService().set(
					BaiDu_Location_info + signId, (Serializable) values,CACHE_TIME);
		}else if(StringUtils.hasText(phoneUUID)){//兼容app老版本（没有signID）
			RedisUtils.getRedisService().set(
					BaiDu_Location_info + phoneUUID, (Serializable) values,CACHE_TIME);
		}else{//兼容pc端老版本（没有signID）
			request.getSession().setAttribute(BaiDu_Location_info, values);
		}
	}

	private static Map<String, Object> getInitMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		if(AppProperties.isDebug()){
			map.put(BaiDu_longitude, "114.076");
			map.put(BaiDu_latitude, "22.569");
			map.put(BaiDu_province, "广东省");
			map.put(BaiDu_cityName, "深圳市");
			map.put(BaiDu_area, "福田区");
			map.put(BaiDu_STATUS, "-1");
		}
		return map;
	}
	
	
	/***
	 * 使用普通IP定位
	 * @param ip
	 * @param type
	 * @param selectCity
	 * @return
	 */
	public static Map<String,Object>  getBaiduAddress(String ip){
		 Map<String, Object> baiduMap =  (Map<String,Object>)RedisUtils.getRedisService().get(BaiDu_SessionIp+ip);
		if(baiduMap != null){
			return baiduMap;
		}
		String localUrl = BaiDu_URL + "?ak=" + getBaduAk() + "&ip=" + ip + "&coor=bd09ll";
		Connection conn = HttpConnection.connect(localUrl);
		conn.timeout(10000);
		conn.ignoreContentType(true);
		Document doc = null;
		Map<String,Object> localMap = new HashMap<String,Object>();
		try {
			doc = conn.get();
			Map<String, Object> all = JsonUtil.getInstance()
					.json2Object(
							URLDecoder.decode(doc.body().text(),
									"UTF-8"), Map.class);
			String status = all.get("status").toString();
			if ("0".equals(status)) {
				Map<String, Object> content = (Map<String, Object>) all.get("content");
				Map<String, Object> addressDetail = (Map<String, Object>) content.get("address_detail");
				Map<String, Object> point = (Map<String, Object>)content.get("point");
				localMap = new HashMap<String,Object>();
				if (point != null) {
					localMap.put(BaiDu_longitude, point.get("x"));// 经度
					localMap.put(BaiDu_latitude, point.get("y"));// 纬度
				}
				localMap.put(BaiDu_province,
						addressDetail.get("province"));// 省份
				localMap.put(BaiDu_cityName, addressDetail.get("city").toString().trim());//城市
				localMap.put(BaiDu_area, addressDetail.get("district").toString().trim());//区县 
				
				localMap.put(BaiDu_STATUS, "1");// 状态
				RedisUtils.getRedisService().set(BaiDu_SessionIp+ip, (Serializable)localMap, CACHE_TIME);
				return localMap;
			}
			//百度地图
			return getQQlocation(ip);
		} catch (Exception e) {
			log.error("getBaiduAddress error:" + ip,e);
			e.printStackTrace();
		}
		return null;
	}
	
	/***
	 * 使用高精度定位
	 * @param ip
	 * @param type
	 * @param selectCity
	 * @return
	 */
	public static Map<String,Object>  getGaoDeHighacciploc(String ip){
		String url = "http://restapi.amap.com/v3/ip?ip="+ip+"&output=JSON&key=b964c3b2f81f119c915012157dd2198f";
		Connection conn = HttpConnection.connect(url);
		conn.timeout(10000);
		conn.ignoreContentType(true);
		Document doc = null;
		Map<String,Object> localMap = new HashMap<String,Object>();
		try {
			doc = conn.get();
			Map<String, Object> all = JsonUtil.getInstance().json2Object(
					URLDecoder.decode(doc.body().text(),"UTF-8"), Map.class);
			Object status = all.get("status");
			if("1".equals(status.toString())){
				//localMap.put(BaiDu_longitude, all.get("lng"));// 经度
				String  rectangle = all.get("rectangle").toString();// 纬度
				System.out.println(rectangle);
				localMap.put(BaiDu_province,
						all.get("province"));// 省份
				localMap.put(BaiDu_cityName, all.get("city").toString().trim());//城市
				localMap.put(BaiDu_STATUS, "0");// 状态
				
				return localMap;
			}
		} catch (Exception e) {
			log.error("getGaoDeHighacciploc error:" + ip,e);
			e.printStackTrace();
		}
		return null;
	}
	

	/***
	 * 使用高精度定位
	 * @param ip
	 * @param type
	 * @param selectCity
	 * @return
	 */
	public static Map<String,Object>  getQQlocation(String ip){
		Connection conn = HttpConnection.connect("http://apis.map.qq.com/ws/location/v1/ip?ip=" + ip +"&key=VLBBZ-LDPCP-BSSDC-LOPNO-HLLRT-E6FCG" );
		conn.timeout(10000);
		conn.ignoreContentType(true);
		Map<String,Object> localMap = new HashMap<String,Object>();
		Document doc = null;
			try {
						
				doc = conn.get();
				Map<String, Object> all = JsonUtil.getInstance().json2Object(
								URLDecoder.decode(doc.body().text(),"UTF-8"), Map.class);
				Object status = all.get("status");
				if("0".equals(status.toString())){
					Map<String, Object> addressDetail = (Map<String, Object>) ((Map<String, Object>) all.get("result")).get("ad_info");
					Map<String, Object> point =(Map<String, Object>) ((Map<String, Object>) all.get("result")).get("location");
					if (point != null) {
						localMap.put(BaiDu_longitude, point.get("lng"));// 经度
						localMap.put(BaiDu_latitude, point.get("lat"));// 纬度
					}
					localMap.put(BaiDu_province,addressDetail.get("province"));// 省份
					localMap.put(BaiDu_cityName, addressDetail.get("city").toString().trim());//城市
					
					localMap.put(BaiDu_STATUS, "1");// 状态
					return localMap;
				}
			} catch (Exception e) {
				log.error("getQQlocation error:" + ip,e);
				e.printStackTrace();
			}
			return null;
	}
	

	/**
	 * 将 GCJ-02 坐标转换成 BD-09 坐标
	 * GoogleMap和高德map用的是同一个坐标系GCJ-02
	 * */
	public static double[] bd_encrypt(double gg_lon,double gg_lat) {
		double bd_lat = 0.0;
		double bd_lon = 0.0;
		double location[] = new double[2];
		double x = gg_lon, y = gg_lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		bd_lon = z * Math.cos(theta) + 0.0065;
		bd_lat = z * Math.sin(theta) + 0.006;
		location[0] = bd_lon;
		location[1] = bd_lat;
		return location;
	}

	/**
	 * 将 BD-09 坐标转换成 GCJ-02 坐标
	 * GoogleMap和高德map用的是同一个坐标系GCJ-02
	 * */
	public static double[] bd_decrypt(double bd_lon, double bd_lat) {
		double gg_lat = 0.0;
		double gg_lon = 0.0;
		double location[] = new double[2];
		double x = bd_lon - 0.0065, y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		gg_lon = z * Math.cos(theta);
		gg_lat = z * Math.sin(theta);
		location[0] = gg_lon;
		location[1] = gg_lat;
		return location;
	}
	private static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
	
	private static final String appKey = "c0ef6eb5389af8865610009e080fb78d";
	public static boolean matchPhone(String s){
		return Pattern.matches("^1\\d{10}$", s);
	}
	 //手机归属地查询
    @SuppressWarnings("rawtypes")
	public static String getTelephoneCity(String phone){
    	if(!matchPhone(phone)){
    		return null;
    	}
        String result =null;
        String url ="http://apis.juhe.cn/mobile/get";//请求接口地址
        Map<String, String> params = new HashMap<String, String>();//请求参数
        params.put("phone",phone);//需要查询的手机号码或手机号码前7位
        params.put("key",appKey);//应用APPKEY(应用详细页查询)
        params.put("dtype","json");//返回数据的格式,xml或json，默认json
        try {
            result =net(url, params, "GET");
            Map object = JsonUtil.getInstance().json2Object(result, Map.class);
            if((NumberUtil.getInt(object.get("error_code"), 1))==0){
            	if(object.get("result")!=null){
            		String city = ((Map)object.get("result")).get("city").toString();
            		if(!StringUtils.isEmpty(city) && (!city.endsWith("市"))){
                		city = city + "市";
                	}
            		return city;
            	}
            }else{
            	log.info("getRequestCity2 error phone:{}, errorCode:{}", phone, object.get("error_code"));
                return null;
            }
        } catch (Exception e) {
        	log.error( "getRequestCity2 error phone:{}",phone,e);
        }
        return null;
    }
    
    /**
    *
    * @param strUrl 请求地址
    * @param params 请求参数
    * @param method 请求方法
    * @return  网络请求字符串
    * @throws Exception
    */
   @SuppressWarnings({ "rawtypes"})
	public static String net(String strUrl, Map params,String method) throws Exception {
       HttpURLConnection conn = null;
       BufferedReader reader = null;
       String rs = null;
       try {
           StringBuffer sb = new StringBuffer();
           if(method==null || method.equals("GET")){
               strUrl = strUrl+"?"+urlencode(params);
           }
           URL url = new URL(strUrl);
           conn = (HttpURLConnection) url.openConnection();
           if(method==null || method.equals("GET")){
               conn.setRequestMethod("GET");
           }else{
               conn.setRequestMethod("POST");
               conn.setDoOutput(true);
           }
           conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
           conn.setUseCaches(false);
           conn.setConnectTimeout(30000);
           conn.setReadTimeout(30000);
           conn.setInstanceFollowRedirects(false);
           conn.connect();
           if (params!= null && method.equals("POST")) {
               try {
                   DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                   out.writeBytes(urlencode(params));
               } catch (Exception e) {
                   e.printStackTrace();
               }
                
           }
           InputStream is = conn.getInputStream();
           reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
           String strRead = null;
           while ((strRead = reader.readLine()) != null) {
               sb.append(strRead);
           }
           rs = sb.toString();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           if (reader != null) {
               reader.close();
           }
           if (conn != null) {
               conn.disconnect();
           }
       }
       return rs;
   }
   
   //将map型转为请求参数型
   @SuppressWarnings("rawtypes")
	public static String urlencode(Map<String,String> data) {
       StringBuilder sb = new StringBuilder();
       for (Map.Entry i : data.entrySet()) {
           try {
               sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
           } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           }
       }
       return sb.toString();
   }
   
   /**聚合appkey  1、手机固话来电显示  2、手机归属地查询**/
	private static final String[] appKeys = new String[] {"928091df185dc771f831ac1bcba8dda5", "461cf465d187dd0eb6df7ba792a17276"};

	
	//手机归属地查询
    @SuppressWarnings("rawtypes")
	public static String getTelCity(String phone){
    	if(!matchPhone(phone)){
    		return null;
    	}
        String result =null;
        String url ="http://apis.juhe.cn/mobile/get";//请求接口地址
        Map<String, String> params = new HashMap<String, String>();//请求参数
        params.put("phone",phone);//需要查询的手机号码或手机号码前7位
        params.put("key",appKeys[1]);//应用APPKEY(应用详细页查询)
        params.put("dtype","json");//返回数据的格式,xml或json，默认json
        try {
            result =net(url, params, "GET");
            Map object = JsonUtil.getInstance().json2Object(result, Map.class);
            if((NumberUtil.getInt(object.get("error_code"), 1))==0){
            	if(object.get("result")!=null){
            		Map data = (Map)object.get("result");
            		String city = null;
            		if (StringUtils.isEmpty(data.get("city"))) {
            			city = StringUtil.getString(data.get("province"));
					}else {
						city = StringUtil.getString(data.get("city"));
					}
                	city = AreaUtils.getLikeCityName(city);;
            		return city;
            	}
            }else{
            	log.info("getRequestCity2 error phone:{}, errorCode:{}", phone, object.get("error_code"));
                return null;
            }
        } catch (Exception e) {
        	log.error("getRequestCity2 error phone:{}",phone,e);
        }
        return null;
    }
    
    private static final String redis_tel_key = "is_request_getTelInfo";
    
    //手机固话来电显示
    @SuppressWarnings("rawtypes")
	public static Map<String, Object> getTelInfo(String phone){
    	if(!matchPhone(phone)){
    		return null;
    	}
    	Map<String, Object> resMap = new HashMap<String, Object>();
    	Object telValue = RedisUtils.getRedisService().get(redis_tel_key);//判断固话接口是否用完
    	if (!StringUtils.isEmpty(telValue)) {
    		resMap.put("cityName", getTelCity(phone));
    		return resMap;
		}
        String result =null;
        String url ="http://op.juhe.cn/onebox/phone/query";//请求接口地址
        Map<String, String> params = new HashMap<String, String>();//请求参数
        params.put("tel",phone);//需要查询的手机号码或手机号码前7位
        params.put("key",appKeys[0]);//应用APPKEY(应用详细页查询)
        params.put("dtype","json");//返回数据的格式,xml或json，默认json
        try {
            //如果当天这个接口请求完了需要直接请求getTelCity接口
        	result =net(url, params, "GET");
            Map object = JsonUtil.getInstance().json2Object(result, Map.class);
            int error_code = NumberUtil.getInt(object.get("error_code"), 1);
            if(error_code==0){
            	if(object.get("result")!=null){
            		Map data = (Map)object.get("result");
            		String city = null;
            		if (StringUtils.isEmpty(data.get("city"))) {
            			city = StringUtil.getString(data.get("province"));
					}else {
						city = StringUtil.getString(data.get("city"));
					}
                	city = AreaUtils.getLikeCityName(city);
                	resMap.put("cityName", city);
                	resMap.put("iszhapian", data.get("iszhapian"));
            	}
            }else if (error_code == 10012) {
        		long endTime = getLastTime(new Date()).getTime();
            	Long seconds = (endTime - System.currentTimeMillis()) / 1000;//获取当天剩余的秒
            	RedisUtils.getRedisService().set(redis_tel_key, "1", seconds.intValue());//存入key并传入剩余秒，下次进入根据这个是否为空判断需不需要调用这个接口
				resMap.put("cityName", getTelCity(phone));
			}else{
            	log.info("getRequestCity3 error phone:{},errorCode:{}" ,phone, object.get("error_code"));
            }
        } catch (Exception e) {
        	log.error( "getRequestCity3 error phone:{}" ,phone,e);
        }
        return resMap;
    }
    
    private static Date getLastTime (Date date){
    	Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
    }
}
