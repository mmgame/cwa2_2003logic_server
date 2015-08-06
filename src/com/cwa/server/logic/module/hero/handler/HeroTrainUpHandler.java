package com.cwa.server.logic.module.hero.handler;

import java.util.List;

import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.HeroMessage.HeroTrainDown;
import com.cwa.message.HeroMessage.HeroTrainUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.HeroTrainPrototype;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 英雄培训
 * 
 * @author tzy
 * 
 */
public class HeroTrainUpHandler extends IGameHandler<HeroTrainUp> {
	@Override
	public void loginedHandler(HeroTrainUp message, IPlayer player) {
		int trainType = message.getTrainType();
		int heroKeyId = message.getHeroKeyId();

		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);

		if (!hdFunction.checkHeroExist(heroKeyId)) {
			// 英雄不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Hero, message);
			return;
		}

		HeroTrainPrototype heroTrainPrototype = logicContext.getprototypeManager().getPrototype(HeroTrainPrototype.class, trainType);
		int moneyType = heroTrainPrototype.getMoneyType();
		int expectCount = heroTrainPrototype.getMoneyCount();

		if (!udFunction.checkMoneyCount(moneyType, expectCount)) {
			// 货币不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}

		// 扣除货币
		udFunction.modifyMoney(moneyType, -expectCount);
		// 训练
		List<Integer> waitPatienceTrain = hdFunction.train(heroKeyId, trainType);

		// 发送消息
		sendTrainHeroDownMessage(heroKeyId, waitPatienceTrain, player);
	}

	public void sendTrainHeroDownMessage(int heroKeyId, List<Integer> waitPatienceTrain, IPlayer player) {
		HeroTrainDown.Builder b = HeroTrainDown.newBuilder();
		b.setHeroKeyId(heroKeyId);
		b.addAllPatiences(waitPatienceTrain);
		player.getSession().send(b.build());
	}
}
