package com.cwa.server.logic.module.match.handler;

import java.util.List;

import com.cwa.data.entity.domain.MatchEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.data.entity.domain.VipEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.ResetMatchCountUp;
import com.cwa.prototype.VipFunctionPrototype;
import com.cwa.prototype.gameEnum.VipFunctionEnum;
import com.cwa.server.logic.dataFunction.MatchDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.dataFunction.VipDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 重置精英副本次数
 * 
 * @author tzy
 * 
 */
public class ResetMatchCountUpHandler extends IGameHandler<ResetMatchCountUp> {

	@Override
	public void loginedHandler(ResetMatchCountUp message, IPlayer player) {
		int passcardId = message.getPasscardId();// 重置关卡

		VipDataFunction vipFunction = (VipDataFunction) player.getDataFunctionManager().getDataFunction(VipEntity.class);
		UsereconomyDataFunction ueFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(UsereconomyEntity.class);
		MatchDataFunction mFunction = (MatchDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		VipFunctionPrototype vipFunctionPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(VipFunctionPrototype.class, VipFunctionEnum.VipFunction_BuyPower.value());

		//cd 
		mFunction.cdBattleReset();
		
		int buyCount = mFunction.getRefreshCount(passcardId);

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

		// 重置次数
		mFunction.refreshBattleCount(passcardId);
	}

}
