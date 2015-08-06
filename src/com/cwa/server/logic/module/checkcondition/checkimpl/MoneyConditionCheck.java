package com.cwa.server.logic.module.checkcondition.checkimpl;

import java.util.List;

import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.checkcondition.ICheckCondition;
import com.cwa.server.logic.player.IPlayer;

/**
 * 金钱(金币类型，金币数量)
 * 
 * @author Administrator
 * 
 */
public class MoneyConditionCheck implements ICheckCondition {
	@Override
	public boolean check(Object condition, Object attr, IPlayer player) {
		List<String> c = (List<String>) condition;
		int moneyType = Integer.parseInt(c.get(0));
		int expectCount = Integer.parseInt(c.get(1));
		if (expectCount < 0) {
			return false;
		}

		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		return udFunction.checkMoneyCount(moneyType, expectCount);
	}
}
