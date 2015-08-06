package com.cwa.server.logic.module.checkcondition;

import java.util.HashMap;
import java.util.Map;

import com.cwa.prototype.gameEnum.ConditionEnum;
import com.cwa.server.logic.module.checkcondition.checkimpl.ItemConditionCheck;
import com.cwa.server.logic.module.checkcondition.checkimpl.MoneyConditionCheck;
import com.cwa.server.logic.module.checkcondition.checkimpl.UserLevelConditionCheck;
import com.cwa.server.logic.module.checkcondition.checkimpl.UserPowerConditionCheck;
import com.cwa.server.logic.player.IPlayer;

/**
 * 检测管理
 * 
 * @author tzy
 * 
 */
public class CheckManager {
	private Map<Integer, ICheckCondition> checkerMap = new HashMap<Integer, ICheckCondition>();

	public CheckManager() {
		checkerMap.put(CheckConditionTypeEnum.Check_Item.value(), new ItemConditionCheck());
		checkerMap.put(CheckConditionTypeEnum.Check_Money.value(), new MoneyConditionCheck());
		checkerMap.put(CheckConditionTypeEnum.Check_UserLevel.value(), new UserLevelConditionCheck());
		checkerMap.put(CheckConditionTypeEnum.Check_UserPower.value(), new UserPowerConditionCheck());
	}

	public boolean check(ConditionEnum conditionEnum, Object condition, IPlayer player) {
		return check(conditionEnum.value(), condition, null, player);
	}

	public boolean check(ConditionEnum conditionEnum, Object condition, Object attr, IPlayer player) {
		return check(conditionEnum.value(), condition, attr, player);
	}

	public boolean check(int conditionType, Object condition, IPlayer player) {
		return check(conditionType, condition, null, player);
	}

	public boolean check(int conditionType, Object condition, Object attr, IPlayer player) {
		ICheckCondition checkCondition = checkerMap.get(conditionType);
		if (checkCondition == null) {
			return true;
		}
		return checkCondition.check(condition, attr, player);
	}
}
