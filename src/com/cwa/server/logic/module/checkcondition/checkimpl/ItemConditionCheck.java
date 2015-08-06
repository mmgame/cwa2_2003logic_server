package com.cwa.server.logic.module.checkcondition.checkimpl;

import java.util.List;

import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.checkcondition.ICheckCondition;
import com.cwa.server.logic.player.IPlayer;

/**
 * 道具个数(道具类型，道具数量)
 * 
 * @author Administrator
 * 
 */
public class ItemConditionCheck implements ICheckCondition {
	@Override
	public boolean check(Object condition, Object attr, IPlayer player) {
		List<String> c = (List<String>) condition;
		int itemKeyId = Integer.parseInt(c.get(0));
		int expectCount = Integer.parseInt(c.get(1));
		if (expectCount < 0) {
			return false;
		}
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);
		return idFunction.checkItemCount(itemKeyId, expectCount);
	}
}
