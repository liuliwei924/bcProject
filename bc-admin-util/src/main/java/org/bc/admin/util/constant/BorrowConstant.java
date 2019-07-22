package org.bc.admin.util.constant;

/**
 * 贷款申请的常量类
 * @author liulw by 2019-07-20 
 *
 */
public class BorrowConstant {

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
		/***企业*/
		public static final int qy = 2;

	}
}
