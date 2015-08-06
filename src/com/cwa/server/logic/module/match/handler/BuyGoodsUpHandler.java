package com.cwa.server.logic.module.match.handler;

import java.util.ArrayList;
import java.util.List;

import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.BuyGoodsDown;
import com.cwa.message.MatchMessage.BuyGoodsUp;
import com.cwa.prototype.MatchGoodsPrototype;
import com.cwa.prototype.MatchShopPrototype;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 购买商品
 * 
 * @author tzy
 * 
 */
public class BuyGoodsUpHandler extends IGameHandler<BuyGoodsUp> {

	@Override
	public void loginedHandler(BuyGoodsUp message, IPlayer player) {
		int shopType = message.getShopType();// 商店类型
		int shelfId = message.getShelfId();// 商品格位

		boolean refresh = false;// 是否刷新
		List<Integer> goodList = new ArrayList<Integer>();
		
		MatchShopDataFunction msFunction = (MatchShopDataFunction) player.getDataFunctionManager().getDataFunction(MatchShopEntity.class);
		UsereconomyDataFunction ueFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(UsereconomyEntity.class);

		
		if (!msFunction.contains(shopType)) {
			// 商店不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		if (msFunction.isSale(shopType, shelfId)) {
			// 商品不出售
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		MatchShopPrototype matchShopPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchShopPrototype.class, shopType);
		MatchGoodsPrototype matchGoodsPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(MatchGoodsPrototype.class, matchShopPrototype.getGoodList().get(shelfId - 1));

		MatchShopEntity entity = msFunction.getEntity(shopType);
		int index = entity.getGoodsIndexList().get(shelfId - 1);
		int needMoneyType = matchGoodsPrototype.getMoneyTypeList().get(index);
		int needMoneyCount = matchGoodsPrototype.getMoneyCountList().get(index);

		if (!ueFunction.checkMoneyCount(needMoneyType, needMoneyCount)) {
			// 货币不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}

		int itemKeyId = matchGoodsPrototype.getGoodList().get(index);
		int itemCount = matchGoodsPrototype.getMoneyCountList().get(index);

		// 扣除货币
		ueFunction.modifyMoney(needMoneyType, -needMoneyCount);
		if (msFunction.buyGoods(shopType, shelfId)) {
			refresh = true;
			goodList = msFunction.refreshShop(entity.shopId, false);
		}
		// 掉落商品
		player.getLogicContext().getUpdateManager().drop(itemKeyId, itemCount, player);

		sendBuyGoodsDownMessage(refresh, goodList, player);
	}

	private void sendBuyGoodsDownMessage(boolean refresh, List<Integer> goodList, IPlayer player) {
		BuyGoodsDown.Builder b = BuyGoodsDown.newBuilder();
		b.setRefresh(refresh);
		if (refresh) {
			b.addAllGoodsIndex(goodList);
		}
		player.getSession().send(b.build());
	}
}
