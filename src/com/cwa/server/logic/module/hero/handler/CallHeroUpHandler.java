package com.cwa.server.logic.module.hero.handler;

import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.message.HeroMessage.CallHeroUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.HeroPrototype;
import com.cwa.prototype.gameEnum.QualityEnum;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 召唤英雄
 * 
 * @author tzy
 * 
 */
public class CallHeroUpHandler extends IGameHandler<CallHeroUp> {
	@Override
	public void loginedHandler(CallHeroUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();
		long userId = player.getUserId();

		HeroPrototype heroPrototype = logicContext.getprototypeManager().getPrototype(HeroPrototype.class, heroKeyId);
		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		if (hdFunction.checkHeroExist(heroKeyId)) {
			// 英雄已经存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Hero, message);
			return;
		}

		int itemKeyId = heroPrototype.getRequireItem();
		int expectCount = heroPrototype.getItemCount();
		if (!idFunction.checkItemCount(itemKeyId, expectCount)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
		}
		// 扣道具处理
		idFunction.modifyItemCount(itemKeyId, -expectCount);
		// 生成英雄
		hdFunction.createHero(userId, heroKeyId, QualityEnum.Quality_Green.value());

		// TODO 这里成功不回复消息
	}
}
