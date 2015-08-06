package com.cwa.server.logic.module.economy.handler;

import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.EconomyMessage.ExchangeMoneyUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.gameEnum.MoneyTypeEnum;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 钻石兑换金券
 * 
 * @author tzy
 * 
 */
public class ExchangeMoneyUpHandler extends IGameHandler<ExchangeMoneyUp> {

	@Override
	public void loginedHandler(ExchangeMoneyUp message, IPlayer player) {
		int count = message.getExchangeCount();
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(UsereconomyEntity.class);
		if (!udFunction.checkMoneyCount(MoneyTypeEnum.Money_Diamond, count)) {
			// 货币不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}

		// 扣除货币
		udFunction.modifyMoney(MoneyTypeEnum.Money_Diamond, -count);
		// 兑换货币
		udFunction.modifyMoney(MoneyTypeEnum.Money_Coupon, count);
	}

}
