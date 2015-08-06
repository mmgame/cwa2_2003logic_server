package com.cwa.server.logic.module.drop.impl;

import java.util.List;

import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.drop.IDropAttr;
import com.cwa.server.logic.module.drop.IDropHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 道具(装备keyId，数量)
 * 
 * @author Administrator
 * 
 */
public class ItemDropHandler implements IDropHandler {
	@Override
	public void drop(Object parm, IDropAttr attr, IPlayer player) {
		List<String> p = (List<String>) parm;
		int itemKeyId = Integer.parseInt(p.get(0));
		int changerCount = Integer.parseInt(p.get(1));

		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);
		idFunction.modifyItemCount(itemKeyId, changerCount);
	}
}
