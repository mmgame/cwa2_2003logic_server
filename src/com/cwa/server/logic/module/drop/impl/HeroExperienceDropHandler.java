package com.cwa.server.logic.module.drop.impl;

import java.util.List;

import com.cwa.data.entity.domain.UserattrEntity;
import com.cwa.server.logic.dataFunction.UserattrDataFunction;
import com.cwa.server.logic.module.drop.IDropAttr;
import com.cwa.server.logic.module.drop.IDropHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 角色经验(数量)
 * 
 * @author Administrator
 * 
 */
public class HeroExperienceDropHandler implements IDropHandler {
	@Override
	public void drop(Object parm, IDropAttr attr, IPlayer player) {
		List<String> p = (List<String>) parm;
		int exp = Integer.parseInt(p.get(1));

		UserattrDataFunction uidFunction = (UserattrDataFunction) player.getDataFunctionManager().getDataFunction(UserattrEntity.class);
		uidFunction.changeHeroExp(exp);
	}
}
