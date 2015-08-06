package com.cwa.server.logic.util;

/**
 * 公式
 * 
 * @author Administrator
 * 
 */
public class FormulaUtil {

	/**
	 * 插件精炼成功率=系数+当前幸运值/幸运值上限*100  * 得到的值小于0时按0计算
	 */
	public static int getRefinePlugRate(int ratio, int lucky, int luckyMax) {
		int rate = ratio - lucky / luckyMax * 100;
		if (rate < 0) {
			return 0;
		} else
			return rate;
	}

	/**
	 * 装备升级金币花费=等级*系数
	 */
	public static int getUpdateNeeDGold(int level, int ratio) {
		return level * ratio;
	}
	/**
	 * 挑战副本体力消耗:所需体力*副本类型
	 */
	public static int getMatchPower(int power,int matchType) {
		return power * matchType;
	}
	
	/**
	 * 挑战副本失败消耗体力=消耗体力/6
	 */
	public static int getFailMatchPower(int power) {
		return (int) (power/6.0);
	}
	
	/**
	 * 挑战副本掉落积分=原型积分+星级*3
	 */
	public static int getStarGradePower(int grade,int star) {
		return grade+star*3;
	}
}
