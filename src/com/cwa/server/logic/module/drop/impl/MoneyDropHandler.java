package com.cwa.server.logic.module.drop.impl;

import java.util.List;

import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.drop.IDropAttr;
import com.cwa.server.logic.module.drop.IDropHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 货币(类型，数量)
 * 
 * @author Administrator
 * 
 */
public class MoneyDropHandler implements IDropHandler {
	@Override
	public void drop(Object parm, IDropAttr attr, IPlayer player) {
		List<String> p = (List<String>) parm;
		int moneyType = Integer.parseInt(p.get(0));
		int changValue = Integer.parseInt(p.get(1));

		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		udFunction.modifyMoney(moneyType, changValue);
	}
}
