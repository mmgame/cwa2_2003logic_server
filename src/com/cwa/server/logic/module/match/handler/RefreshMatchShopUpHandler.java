package com.cwa.server.logic.module.match.handler;

import java.util.List;

import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.data.entity.domain.VipEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.RefreshMatchShopDown;
import com.cwa.message.MatchMessage.RefreshMatchShopUp;
import com.cwa.prototype.MatchShopPrototype;
import com.cwa.prototype.VipFunctionPrototype;
import com.cwa.prototype.gameEnum.VipFunctionEnum;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.dataFunction.VipDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 刷新商店商品
 * 
 * @author tzy
 * 
 */
public class RefreshMatchShopUpHandler extends IGameHandler<RefreshMatchShopUp> {

	@Override
	public void loginedHandler(RefreshMatchShopUp message, IPlayer player) {
		int shopType = message.getShopType();// 商店类型

		VipDataFunction vipFunction = (VipDataFunction) player.getDataFunctionManager().getDataFunction(VipEntity.class);
		UsereconomyDataFunction ueFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(UsereconomyEntity.class);
		MatchShopDataFunction msFunction = (MatchShopDataFunction) player.getDataFunctionManager().getDataFunction(MatchShopEntity.class);
		VipFunctionPrototype vipFunctionPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(VipFunctionPrototype.class, VipFunctionEnum.VipFunction_BuyPower.value());

		// 考虑cd
		msFunction.cdRefreshShopReset();

		if (!msFunction.contains(shopType)) {
			// 商店不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}
		int buyCount = msFunction.getRefreshCount(shopType);
		if (!vipFunction.isEnoughCount(vipFunctionPrototype, buyCount)) {
			// 玩家购买次数不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_User, message);
			return;
		}

		MatchShopPrototype matchShopPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchShopPrototype.class, msFunction.getEntity(shopType).shopId);
		int moneyType = matchShopPrototype.getRequireMoneyType();
		List<Integer> moneyList = matchShopPrototype.getRequireMoneyCountList();

		if (!ueFunction.checkMoneyCount(moneyType, moneyList.get(buyCount))) {
			// 货币不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}

		// 扣除货币
		ueFunction.modifyMoney(moneyType, -moneyList.get(buyCount));

		// 重置商品
		List<Integer> goodsIndexList = msFunction.refreshShop(msFunction.getEntity(shopType).shopId, true);
		sendRefreshMatchShopDownMessage(goodsIndexList, player);
	}

	private void sendRefreshMatchShopDownMessage(List<Integer> goodsIndexList, IPlayer player) {
		RefreshMatchShopDown.Builder b = RefreshMatchShopDown.newBuilder();
		b.addAllGoodsIndex(goodsIndexList);
		player.getSession().send(b.build());
	}

}
