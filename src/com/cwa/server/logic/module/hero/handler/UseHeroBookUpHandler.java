package com.cwa.server.logic.module.hero.handler;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.message.HeroMessage.ChangeToStoneKeyIdBean;
import com.cwa.message.HeroMessage.ChangeToStoneKeyIdBean.Builder;
import com.cwa.message.HeroMessage.UseHeroBookDown;
import com.cwa.message.HeroMessage.UseHeroBookUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.HeroItemPrototype;
import com.cwa.prototype.gameEnum.QualityEnum;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 开典籍召唤英雄
 * 
 * @author tzy
 * 
 */
public class UseHeroBookUpHandler extends IGameHandler<UseHeroBookUp> {

	@Override
	public void loginedHandler(UseHeroBookUp message, IPlayer player) {
		int itemKeyId = message.getItemId();
		long userId = player.getUserId();

		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		HeroItemPrototype heroItemPrototype = logicContext.getprototypeManager().getPrototype(HeroItemPrototype.class, itemKeyId);
		int heroKeyId = heroItemPrototype.getHeroId();

		if (!idFunction.checkItemCount(itemKeyId, -GameConstant.CONSUMEITEM_COUNT)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		// 扣道具处理
		idFunction.modifyItemCount(itemKeyId, -GameConstant.CONSUMEITEM_COUNT);

		boolean isHave = false;
		ChangeToStoneKeyIdBean.Builder changeToStoneKeyIdBean = ChangeToStoneKeyIdBean.newBuilder();
		if (!hdFunction.checkHeroExist(heroKeyId)) {
			// 如果没有英雄就创建英雄
			hdFunction.createHero(userId, heroKeyId, QualityEnum.Quality_Green.value());
		} else {
			int quality = heroItemPrototype.getQuality();
			if (hdFunction.checkHeroQuality(heroKeyId, quality)) {
				isHave = true;
				int dropItemKeyId = heroItemPrototype.getDropItem();
				int changerCount = heroItemPrototype.getDropCount();
				// 转化成宝石
				idFunction.modifyItemCount(dropItemKeyId, changerCount);

				changeToStoneKeyIdBean.setItemKeyId(dropItemKeyId);
				changeToStoneKeyIdBean.setCount(changerCount);
			} else {
				hdFunction.modifyQuality(heroKeyId, quality);
			}
		}
		int heroGradeId = hdFunction.getHeroGradeKeyId(heroKeyId);
		sendUseHeroBookDownMessage(heroGradeId, isHave, changeToStoneKeyIdBean, player);
	}

	public void sendUseHeroBookDownMessage(int heroGradeId, boolean isHave, Builder builder, IPlayer player) {
		UseHeroBookDown.Builder b = UseHeroBookDown.newBuilder();
		b.setHeroKeyId(heroGradeId);
		b.setIsHaveHero(isHave);
		if (isHave) {
			b.setChangeToStoneKeyIdBean(builder.build());
		}
		player.getSession().send(b.build());
	}
}
