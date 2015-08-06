package com.cwa.server.logic.module.hero.handler;

import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.gameEnum.HeroTrainStateEnum;
import com.cwa.message.HeroMessage.SaveTrainPatienceUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 是否保存培养耐性
 * 
 * @author tzy
 * 
 */
public class SaveTrainPatienceUpHandler extends IGameHandler<SaveTrainPatienceUp> {
	@Override
	public void loginedHandler(SaveTrainPatienceUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();

		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);

		if (!hdFunction.checkHeroTrainState(heroKeyId, HeroTrainStateEnum.Train_NoSave.value())) {
			// 已经保存，这里包含了没有英雄的判断了
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Hero, message);
			return;
		}
		hdFunction.saveTrain(heroKeyId);
	}
}
