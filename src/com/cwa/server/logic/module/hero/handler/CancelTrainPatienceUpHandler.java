package com.cwa.server.logic.module.hero.handler;

import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.gameEnum.HeroTrainStateEnum;
import com.cwa.message.HeroMessage.CancelTrainPatienceUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 取消保存
 * 
 * @author tzy
 *
 */
public class CancelTrainPatienceUpHandler extends IGameHandler<CancelTrainPatienceUp> {
	@Override
	public void loginedHandler(CancelTrainPatienceUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();

		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);

		if (!hdFunction.checkHeroTrainState(heroKeyId, HeroTrainStateEnum.Train_Save.value())) {
			// 没有保存，这里包含了没有英雄的判断了
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Hero, message);
			return;
		}
		// 取消训练
		hdFunction.cancelTrain(heroKeyId);
	}
}
