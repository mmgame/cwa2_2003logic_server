package com.cwa.server.logic.module.drop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cwa.constant.GameConstant;
import com.cwa.server.logic.module.drop.impl.HeroExperienceDropHandler;
import com.cwa.server.logic.module.drop.impl.ItemDropHandler;
import com.cwa.server.logic.module.drop.impl.MoneyDropHandler;
import com.cwa.server.logic.module.drop.impl.UserExperienceDropHandler;
import com.cwa.server.logic.module.drop.impl.UserPowerDropHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 掉落管理类
 * 
 * @author tzy
 * 
 */
public class UpdateManager {
	private Map<Integer, IDropHandler> dropMap = new HashMap<Integer, IDropHandler>();

	public UpdateManager() {
		dropMap.put(DropTypeEnum.Drop_Item.value(), new ItemDropHandler());
		dropMap.put(DropTypeEnum.Drop_Money.value(), new MoneyDropHandler());
		dropMap.put(DropTypeEnum.Drop_UserExperience.value(), new UserExperienceDropHandler());
		dropMap.put(DropTypeEnum.Drop_UserPower.value(), new UserPowerDropHandler());
		dropMap.put(DropTypeEnum.Drop_HeroExperience.value(), new HeroExperienceDropHandler());
	}

	public void drop(int dropType, Object parm, IDropAttr attr, IPlayer player) {
		IDropHandler dropHandler = dropMap.get(dropType);
		if (dropHandler != null) {
			dropHandler.drop(parm, attr, player);
		}
	}

	public void drop(int goodsKey, int goodsCount, IPlayer player) {
		int dropType = goodsKey / GameConstant.GOODS_TYPE;
		List<String> params = new ArrayList<String>(2);
		params.add(String.valueOf(goodsKey));
		params.add(String.valueOf(goodsCount));
		drop(dropType, params, null, player);
	}

	/**
	 * 掉落list
	 * 
	 * @param userId
	 * @param goodsKeyList
	 * @param goodsCountList
	 * @param logicContext
	 */
	public void drop(List<Integer> goodsKeyList, List<Integer> goodsCountList, IPlayer player) {
		Iterator<Integer> keyIdIt = goodsKeyList.iterator();
		Iterator<Integer> countIt = goodsCountList.iterator();
		for (; keyIdIt.hasNext();) {
			int keyId = keyIdIt.next();
			int count = countIt.next();
			drop(keyId, count, player);
		}
	}

}
