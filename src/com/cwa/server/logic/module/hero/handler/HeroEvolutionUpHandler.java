package com.cwa.server.logic.module.hero.handler;

import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.message.HeroMessage.HeroEvolutionUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.HeroGradePrototype;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 英雄提品和进化
 * 
 * @author tzy
 * 
 */
public class HeroEvolutionUpHandler extends IGameHandler<HeroEvolutionUp> {
	@Override
	public void loginedHandler(HeroEvolutionUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();

		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		if (!hdFunction.checkHeroLevelEvolution(heroKeyId)) {
			// 英雄等级不足，或满级，或原型有问题
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Hero, message);
			return;
		}
		int heroGradeId = hdFunction.getHeroGradeKeyId(heroKeyId);
		HeroGradePrototype herogradePrototype = logicContext.getprototypeManager().getPrototype(HeroGradePrototype.class, heroGradeId);
		if (!idFunction.checkItemCount(herogradePrototype.getNeedItemList(), herogradePrototype.getNeedCountList())) {
			// 道具不足，或原型有问题
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		// 扣除道具
		idFunction.modifyItemCount(herogradePrototype.getNeedItemList(), herogradePrototype.getNeedCountList(), false);

		// 升级
		int nextHeroGradeId = herogradePrototype.getNextLevel();
		hdFunction.evolution(heroKeyId, nextHeroGradeId);
	}
}
