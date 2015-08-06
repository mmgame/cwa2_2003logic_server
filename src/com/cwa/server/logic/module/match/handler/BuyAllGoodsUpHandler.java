package com.cwa.server.logic.module.match.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.BuyAllGoodsDown;
import com.cwa.message.MatchMessage.BuyAllGoodsUp;
import com.cwa.prototype.MatchGoodsPrototype;
import com.cwa.prototype.MatchShopPrototype;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 购买全部商品
 * 
 * @author tzy
 * 
 */
public class BuyAllGoodsUpHandler extends IGameHandler<BuyAllGoodsUp> {

	@Override
	public void loginedHandler(BuyAllGoodsUp message, IPlayer player) {
		int shopType = message.getShopType();// 商店类型

		MatchShopDataFunction msFunction = (MatchShopDataFunction) player.getDataFunctionManager().getDataFunction(MatchShopEntity.class);
		UsereconomyDataFunction ueFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(UsereconomyEntity.class);

		if (!msFunction.contains(shopType)) {
			// 商店不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		MatchShopPrototype matchShopPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchShopPrototype.class, shopType);

		MatchShopEntity entity = msFunction.getEntity(shopType);
		List<Integer> goodsIndexList = entity.getGoodsIndexList();
		Map<Integer, Integer> moneyTypeMap = new HashMap<Integer, Integer>();
		List<Integer> itemList = new ArrayList<Integer>();
		List<Integer> itemCountList = new ArrayList<Integer>();
		for (int i = 0; i < goodsIndexList.size(); i++) {
			int index = goodsIndexList.get(i);
			if (index != -1) {
				MatchGoodsPrototype matchGoodsPrototype = player.getLogicContext().getprototypeManager()
						.getPrototype(MatchGoodsPrototype.class, matchShopPrototype.getGoodList().get(i));

				int moneyType = matchGoodsPrototype.getMoneyTypeList().get(index);
				int count = matchGoodsPrototype.getMoneyCountList().get(index);
				if (!moneyTypeMap.containsKey(moneyType)) {
					moneyTypeMap.put(moneyType, 0);
				}
				moneyTypeMap.put(moneyType, moneyTypeMap.get(moneyType) + count);

				itemList.add(matchGoodsPrototype.getGoodList().get(index));
				itemCountList.add(matchGoodsPrototype.getCountList().get(index));
			}
		}

		Iterator<Entry<Integer, Integer>> it = moneyTypeMap.entrySet().iterator();
		for (; it.hasNext();) {
			Entry<Integer, Integer> e = it.next();
			if (!ueFunction.checkMoneyCount(e.getKey(), e.getValue())) {
				// 货币不足，数据错误
				logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
				return;
			}
		}

		for (; it.hasNext();) {
			Entry<Integer, Integer> e = it.next();
			ueFunction.modifyMoney(e.getKey(), -(int) (e.getValue() * GameConstant.BUY_ALL_GOODS));
		}
		List<Integer> goodList = msFunction.refreshShop(entity.shopId, false);
		// 掉落商品
		player.getLogicContext().getUpdateManager().drop(itemList, itemCountList, player);
		sendBuyAllGoodsDownMessage(goodList, player);
	}

	private void sendBuyAllGoodsDownMessage(List<Integer> goodList, IPlayer player) {
		BuyAllGoodsDown.Builder b = BuyAllGoodsDown.newBuilder();
		b.addAllGoodsIndex(goodList);
		player.getSession().send(b.build());
	}

}
