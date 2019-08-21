package org.bc.admin.util.constant;

/**
 * 贷款申请的常量类
 * @author liulw by 2019-07-20 
 *
 */
public class BorrowConstant {
	/**注册类型**/
	public static final String RegSourceType = "regSourceType";
	
	public static class ApplyStatus{
		/***未处理*/
		public static final int NO_Handle = 0;
		/***处理中*/
		public static final int Handling = 1;
		/***处理成功*/
		public static final int Success = 2;
		/***处理失败*/
		public static final int Fail =3;
	}
	
	//1-优质单 2-普通单 3-无资料 4-垃圾单 5 不押车贷6 微店单
	public static class ApplyType{
		/***优质单*/
		public static final int senior_order = 1;
		/***普通单*/
		public static final int comm_order = 2;
		/***准优质*/
		public static final int high_order = 6;

	}
	
	public static class workType{
		/***自由职业*/
		public static final int free_work= 1;
		
		/***企业*/
		public static final int qy = 2;
		
		/***个体*/
		public static final int gt= 3;
		
		/***上班*/
		public static final int worker= 4;

	}
	
	/**t_apply_push_record
	 * 推送记录表中status状态
	 * @author HP
	 *
	 */
	public static class PushStatus{
		/***未推*/
		public static final int NO_PUSH = 0;
		/***推送成功*/
		public static final int PUSH_SUCCESS = 1;
		/***推送失败*/
		public static final int PUSH_FAIL = 2;
		/***推送中*/
		public static final int PUSH_ING = 9;
	}
}
