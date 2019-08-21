package org.bc.admin.util.constant;

public class SysParamConstont {
    /***是否检查图形验证码*/
	public static final String IS_CHECK_IMG_CODE = "IS_CHECK_IMG_CODE";
	
   /***转单的定时是否开启*/
	public static final String TRANFER_ORDER_JOB_ENABLE = "TRANFER_ORDER_JOB_ENABLE";
	
	 /***短信类型*/
	public static class SmsType {
		 /***腾讯*/
		public static final int TengXun = 1;
		 /***聚合*/
		public static final int Juhe = 2;
		 /***微网通联*/
		public static final int LMobile = 3;
		
		 /***阿里云*/
		public static final int aliyun = 4;
	}
}
