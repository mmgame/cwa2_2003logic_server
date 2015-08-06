package com.cwa.server.logic.module.checkcondition.checkimpl;

import java.util.List;

import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.checkcondition.ICheckCondition;
import com.cwa.server.logic.player.IPlayer;

/**
 * 用户等级(等级),(当前等级大于等于)
 * 
 * @author tzy
 * 
 */
public class UserLevelConditionCheck implements ICheckCondition {
	@Override
	public boolean check(Object condition, Object attr, IPlayer player) {
		List<String> c = (List<String>) condition;
		int expectLevel = Integer.parseInt(c.get(0));

		UserinfoDataFunction udFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		return udFunction.checkUserLevel(expectLevel);
	}
}
