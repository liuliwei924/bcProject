package org.bc.admin.util.borrow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.bc.admin.util.constant.BorrowConstant;
import org.bc.admin.util.sys.SysParamsUtil;
import org.llw.com.util.DateTimeUtil;
import org.llw.com.util.NumberUtil;
import org.llw.com.util.StringUtil;
import org.springframework.util.StringUtils;

/**
 * 优质配置
 * @author 2017-02-15 By liulw
 *
 */
public class SeniorCfgUtils {





	/************** 新的优质单配置 **************/

	/**
	 * 新的的优质单判断
	 * @param borrowMap
	 * @return
	 */
	public static int getApplyType(Map<String,Object> borrowMap){
		//1-优质单 2-普通单 3-无资料 4-垃圾单 5 不押车贷6 微店单
		String workType = StringUtil.getString(borrowMap.get("workType"));
		int houseType = NumberUtil.getInt(borrowMap.get("houseType"), 2);
		int carType = NumberUtil.getInt(borrowMap.get("carType"),0);
		int insurType = NumberUtil.getInt(borrowMap.get("insurType"),0);
		int socialType = NumberUtil.getInt(borrowMap.get("socialType"), 2);
		int fundType = NumberUtil.getInt(borrowMap.get("fundType"), 2);
		double loanAmount = NumberUtil.getDouble(borrowMap.get("loanAmount"), 0);
		double income = NumberUtil.getDouble(borrowMap.get("income"), 0);
		double pubManageLine = NumberUtil.getDouble(borrowMap.get("pubManageLine"), 0);

		int applyType = BorrowConstant.ApplyType.comm_order;
		
		//不满足贷款金额的直接退出
		if(!judgeLoanAmount(2, loanAmount)){
			return  BorrowConstant.ApplyType.comm_order; 
		}
		// 3-商品住宅 4-商住两用房
		if(houseType == 3|| houseType == 4 ){
			return  BorrowConstant.ApplyType.high_order; //准优质单
		}
		if(judgeAsset(3, houseType, carType,insurType)){
			return BorrowConstant.ApplyType.senior_order; 
		}
		if (judgeSocialFund(4, socialType,fundType) ) {
			return BorrowConstant.ApplyType.senior_order; 
		}
		if(((judgeIncome(5000, income) ))
				|| (judgeManageLine(workType, 20000, pubManageLine))){
		
			return BorrowConstant.ApplyType.senior_order; 
		}
		
		return applyType;
	}

	
	/**
	 * 判断收入
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	private static boolean judgeIncome(double incomeCfg, double income){
		if(incomeCfg <= 0){
			return true;
		}
		if(income >= incomeCfg){
			return true;
		}
		return false;
	}
	
	/**1无固定职业 2企业主 3个体户 4上班族  5-学生
	 * 判断收入
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	public static double randomIncome(int workType, double income){
		switch (workType) {
		case 1://无固定职业 
			double jobless = 3000 + Math.random() * 4000;//产生5000-6000的随机数
			jobless =  income < 1000 ? jobless : income;
			return new BigDecimal(jobless).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 2://企业主 
			double business = 8000 + Math.random() * 40000;//产生10000-50000的随机数
			business = income < 1000 ? business : income;
			return new BigDecimal(business).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 3://个体户
			double person = 5000 + Math.random() * 5000;//产生6000-10000的随机数
			person = income < 1000 ? person : income;
			return new BigDecimal(person).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 4://上班族
			double worker = 3000 + Math.random() * 6000;//产生4000-10000的随机数
			worker = income < 1000 ? worker : income;
			return new BigDecimal(worker).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 5://学生
			double student = 1000 + Math.random() * 1000;//产生1000-3000的随机数
			student = income < 1000 ? student : income;
			return new BigDecimal(student).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

		default:
			return 4000;
		}
	}
	
	
	/**转换对公流水
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	public static double tranPubLine(double pubManageLine){
		if(pubManageLine > 0 && pubManageLine < 1) return 0.9;
		if(pubManageLine >= 1 && pubManageLine < 3) return 2;
		if(pubManageLine >=3 && pubManageLine < 5) return 4;
		if(pubManageLine >=5 && pubManageLine < 10) return 8;
		if(pubManageLine >= 10 && pubManageLine < 30) return 20;
		if(pubManageLine >= 30 && pubManageLine < 1) return 31;
		else return 0;
	}
	
	/**转换对公流水
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	public static double tranTotalLine(double totalLine){
		if(totalLine > 0 && totalLine < 1) return 0.9;
		if(totalLine >= 1 && totalLine < 3) return 2;
		if(totalLine >=3 && totalLine < 5) return 4;
		if(totalLine >=5 && totalLine < 10) return 8;
		if(totalLine >= 10 && totalLine < 30) return 20;
		if(totalLine >= 30 && totalLine < 1) return 31;
		else return 0;
	}
	
    /**
     * 判断单的抢单类型 1-免费 2-积分抢  3-现金抢
     * @param loanAmount 金额
     * @param isSeniorCust 是否优质客户 1-是 0-否
     * @param grade 等级
     * @param isAutoSale 是否自动转 1-自动 0-手工
     * @return
     */
	public static int judgeRobType(double loanAmount,int isSeniorCust,String grade,int isAutoSale){
		if(isSeniorCust == 1 || isAutoSale == 0) return 3;//
		
		int loanAmtRobType = SysParamsUtil.getIntParamByKey("loanAmtRobType", 2);
		if(("E".equals(grade) || "F".equals(grade)) && loanAmount <= loanAmtRobType) 
			return 1;
		else 
			return 3;
	}
	
	
	/**
	 * 判断公司流水
	 * @param workType
	 * @param pubManageLineCfg
	 * @param pubManageLine
	 * @return
	 */
	private static boolean judgeManageLine(String workType ,double pubManageLineCfg, double pubManageLine){
		//职业（1无固定职业 2企业主 3个体户 4上班族  5-学生）
		if(BorrowConstant.workType.qy==(NumberUtil.getInt(workType, 1))){
			if(pubManageLineCfg <= 0){
				return true;
			}
			if(pubManageLine >= pubManageLineCfg){
				return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * 判断借款金额
	 * @param loanAmountCfg
	 * @param loanAmount
	 * @return
	 */
	private static boolean judgeLoanAmount(double loanAmountCfg, double loanAmount){
		if(loanAmountCfg <= 0){
			return true;
		}
		if(loanAmount >= loanAmountCfg){
			return true;
		}
		return false;
	}

	/**
	 * 判断房产  0无需房产 1需要房产 2-有车有房 3车，房，保单三者其一
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	private static boolean judgeAsset(int haveHouseCfg, int haveHouse,int carType,int insurType){
		switch (haveHouseCfg) {
		case 0://无需房产
			return true;
		case 1://需要房产
			
			return judgeHouse(haveHouseCfg, haveHouse);
		case 2://有车有房 
			return judgeHouse(haveHouseCfg, haveHouse) && judgeCar(haveHouseCfg,carType);
		case 3://车，房，保单三者其一
			return (judgeHouse(haveHouseCfg, haveHouse) || judgeCar(haveHouseCfg,carType) || insurType !=0);

		default:
			return false;
		}
	}

	/**
	 * 判断房产
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	private static boolean judgeHouse(int haveHouseCfg, int haveHouse){
		if(haveHouseCfg == 0){
			return true;
		}
		return (haveHouse >0 && haveHouse !=2);
	}

	
	/**
	 * 判断房产
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	private static boolean judgeCar(int haveHouseCfg, int carType){
		if(haveHouseCfg == 0){
			return true;
		}
		return !(carType<=0 || carType==2);
	}
	
	/**
	 * 判断社保公积金
	 * @param socialFundCfg
	 * @param socialType
	 * @param fundType
	 * @return
	 */
	private static boolean judgeSocialFund(int socialFundCfg, int socialType, int fundType){
		switch (socialFundCfg) {
		case 0://无要求
			return true;
		case 1://有社保
			return socialType == 1;
		case 2://有公积金
			return fundType == 1;
		case 3://有公积金，有社保
			return (fundType == 1 && socialType == 1);
		case 4://有公积金或有社保
			return (fundType == 1 || socialType == 1);
		default:
			return false;
		}
	}
	
	/**
	 * 房、车 、保单、社保公积金 都没有就算未填信息
	 * noHouseType 和 noCarType 无车和无房的类型不统一，需要自己传类型,默认都是2
	 */
	public static void haveDetail(Map<String, Object> param, String noHouseType, String noCarType) {
		if (StringUtils.isEmpty(noHouseType)) {
			noHouseType = "2";
		}
		if (StringUtils.isEmpty(noCarType)) {
			noCarType = "2";
		}
		
		double income = NumberUtil.getDouble(param.get("income"), 0);
		int wagesType = NumberUtil.getInt(param.get("wagesType"), 0);
		if(wagesType ==1 && income >= 3000){
			return ;
		}
		
		if ((!"1".equals(StringUtil.getString(param.get("fundType")))) && (!"1".equals(StringUtil.getString(param.get("socialType"))))
			&& (StringUtils.isEmpty(param.get("insurType")) || "0".equals(StringUtil.getString(param.get("insurType"))))
			&& (StringUtils.isEmpty(param.get("houseType")) || noHouseType.equals(StringUtil.getString(param.get("houseType"))))
			&& (StringUtils.isEmpty(param.get("carType")) || noCarType.equals(StringUtil.getString(param.get("carType"))))) {
			param.put("haveDetail", 0);
		}
	}
	
	/**
	 * 房、车 、保单、社保公积金 都没有就算未填信息
	 * noHouseType 和 noCarType 无车和无房的类型不统一，需要自己传类型,默认都是2
	 */
	public static int haveDetail(Map<String, Object> param) {
	    int haveDetail = 1;
	    
		double loanAmount = 0;
        if(StringUtils.isEmpty(param.get("loanAmount"))){
       	 loanAmount = NumberUtil.getDouble(param.get("applyAmount"),0);
        }else{
       	 loanAmount = NumberUtil.getDouble(param.get("loanAmount"),0);
        }
        
	    if(loanAmount <= 0){
	    	return 0;
	    }
	    
	   if(StringUtils.isEmpty(param.get("cityName"))){
		   return 0;
	   }
	    
		double income = NumberUtil.getDouble(param.get("income"), 0);
		int wagesType = NumberUtil.getInt(param.get("wagesType"), 0);
		if(wagesType ==1 && income >= 3000){
			return haveDetail;
		}
		
		int fundType = NumberUtil.getInt(param.get("fundType"), 0);
		int socialType = NumberUtil.getInt(param.get("socialType"), 0);
		int insurType = NumberUtil.getInt(param.get("insurType"), 0);
		int houseType = NumberUtil.getInt(param.get("houseType"), 2);
		int carType = NumberUtil.getInt(param.get("carType"), 2);
		
		if(!judgeCar(1, carType) && !judgeHouse(1, houseType) 
		&&!judgeAsset(3, houseType, carType, insurType) 
		&& !judgeSocialFund(4, socialType, fundType)){
			haveDetail = 0;
		}
	
		return haveDetail ;
	}

	/**
	 * 获取转单时间
	 * @param paramsMap
	 * @return
	 */
	public static Date getTranferTime(int haveDetail) {
		Date d1 = new Date();
		if(haveDetail ==1) 
			return DateTimeUtil.getNextMinutes(d1, 1);
		else
			return DateTimeUtil.getNextMinutes(d1, 5);
	}
	
	
}
