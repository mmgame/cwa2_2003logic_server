package com.cwa.server.logic.module.match.handler;

import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.data.entity.domain.VipEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.BuyPowerUp;
import com.cwa.prototype.VipFunctionPrototype;
import com.cwa.prototype.gameEnum.VipFunctionEnum;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.dataFunction.VipDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 购买体力
 * 
 * @author tzy
 * 
 */
public class BuyPowerUpHandler extends IGameHandler<BuyPowerUp> {

	@Override
	public void loginedHandler(BuyPowerUp message, IPlayer player) {
		
		
		UserinfoDataFunction uiFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		VipDataFunction vipFunction = (VipDataFunction) player.getDataFunctionManager().getDataFunction(VipEntity.class);
		UsereconomyDataFunction ueFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(UsereconomyEntity.class);
		VipFunctionPrototype vipFunctionPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(VipFunctionPrototype.class, VipFunctionEnum.VipFunction_BuyPower.value());
		//考虑cd
		uiFunction.cdBuyPowerReset();
		
		int  buyCount = uiFunction.getEntity().buyCount;
		if (!vipFunction.isEnoughCount(vipFunctionPrototype, buyCount)) {
			// 玩家购买次数不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_User, message);
			return;
		}

		int moneyType = vipFunctionPrototype.getRequireMoneyType();
		List<Integer> moneyList = vipFunctionPrototype.getRequireMoneyCountList();

		if (!ueFunction.checkMoneyCount(moneyType, moneyList.get(buyCount))) {
			// 货币不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}
		
		// 扣除货币
		ueFunction.modifyMoney(moneyType, -moneyList.get(buyCount));
		
		//购买体力
		uiFunction.changePower(GameConstant.BUY_POWER,true);
	}

}
