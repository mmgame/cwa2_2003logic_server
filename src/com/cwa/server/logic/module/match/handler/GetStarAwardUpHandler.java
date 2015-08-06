package com.cwa.server.logic.module.match.handler;

import java.util.List;

import com.cwa.data.entity.domain.MatchAwardEntity;
import com.cwa.data.entity.domain.MatchStarEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.GetStarAwardUp;
import com.cwa.prototype.MatchChapterPrototype;
import com.cwa.server.logic.dataFunction.MatchAwardDataFunction;
import com.cwa.server.logic.dataFunction.MatchStarDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 领取章节星级奖励
 * 
 * @author tzy
 * 
 */
public class GetStarAwardUpHandler extends IGameHandler<GetStarAwardUp> {

	@Override
	public void loginedHandler(GetStarAwardUp message, IPlayer player) {
		int chapterId = message.getChapterId();// 章节id
		int index = message.getIndex();// 奖励索引

		MatchAwardDataFunction maFunction = (MatchAwardDataFunction) player.getDataFunctionManager().getDataFunction(MatchAwardEntity.class);
		MatchStarDataFunction msFunction = (MatchStarDataFunction) player.getDataFunctionManager().getDataFunction(MatchStarEntity.class);
		if (!maFunction.canAward(chapterId, index)) {
			// 不能领奖
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		MatchChapterPrototype matchChapterPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchChapterPrototype.class, chapterId);

		List<List<Integer>> awardList = matchChapterPrototype.gotStartAwardListsList();
		List<Integer> goodsList = awardList.get(index - 1);
		if (goodsList == null) {
			// 不能领奖
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		int starCount = msFunction.getTotalStar(chapterId);
		if (starCount < goodsList.get(0)) {
			// 不能领奖
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		// 掉落奖品
		maFunction.award(chapterId, index);
		player.getLogicContext().getUpdateManager().drop(goodsList.get(1), goodsList.get(2), player);
	}
}
