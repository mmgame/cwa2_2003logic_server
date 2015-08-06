package com.cwa.server.logic.module.drop.impl;

import java.util.List;

import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.drop.IDropAttr;
import com.cwa.server.logic.module.drop.IDropHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 体力
 * 
 * @author tzy
 * 
 */
public class UserPowerDropHandler implements IDropHandler {
	@Override
	public void drop(Object parm, IDropAttr attr, IPlayer player) {
		List<String> p = (List<String>) parm;
		int power = Integer.parseInt(p.get(1));

		UserinfoDataFunction uidFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		uidFunction.changePower(power,false);
	}
}
