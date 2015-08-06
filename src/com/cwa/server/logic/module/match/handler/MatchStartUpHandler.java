package com.cwa.server.logic.module.match.handler;

import java.util.List;
import java.util.Map;

import com.cwa.prototype.MatchChapterPrototype;
import com.cwa.prototype.MatchPrototype;
import com.cwa.prototype.gameEnum.MatchTypeEnum;
import com.cwa.prototype.gameEnum.PasscardTypeEnum;
import com.cwa.server.logic.dataFunction.FormationDataFunction;
import com.cwa.server.logic.dataFunction.MatchDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.module.match.cache.MatchCache;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.util.FormulaUtil;
import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.data.entity.domain.MatchEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.FormationInfoBean;
import com.cwa.message.MatchMessage.MatchStartUp;

/**
 * 比赛开始
 * 
 * @author tzy
 * 
 */
public class MatchStartUpHandler extends IGameHandler<MatchStartUp> {

	@Override
	public void loginedHandler(MatchStartUp message, IPlayer player) {
		int type = message.getType();
		int passcardId = message.getPasscardId();
		List<FormationInfoBean> formationInfoList = message.getFormationInfoBeanList();

		// 数据
		MatchDataFunction mdFunction = (MatchDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		UserinfoDataFunction uiFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		FormationDataFunction fFunction = (FormationDataFunction) player.getDataFunctionManager().getDataFunction(FormationEntity.class);

		MatchCache matchCache = mdFunction.getMatchCache();
//		if (matchCache != null && matchCache.getSession() != player.getSession()) {
//			// 已经存在比赛
//			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
//			return;
//		}
		MatchEntity matchEntity = mdFunction.getEntity(type);
		if (matchEntity == null) {
			// 没有对应类型
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_MatchType, message);
			return;
		}
		MatchPrototype matchPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchPrototype.class, passcardId);
		if (matchPrototype == null) {
			// 没有对应关卡
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_PasscardId, message);
			return;
		}

		if (matchEntity.matchKeyId < passcardId) {
			// 不可以打未开启的关卡
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		if (matchEntity.matchKeyId > passcardId && matchPrototype.getType() == PasscardTypeEnum.Passcard_Once.value()) {
			// 不能重复挑战一次性关卡
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_PasscardId, message);
			return;
		}

		// 玩家等级限制检测
		MatchChapterPrototype matchChapterPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchChapterPrototype.class, matchPrototype.getChapter());
		if (!uiFunction.checkUserLevel(matchChapterPrototype.getLimitLevel())) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_User, message);
			return;
		}
		// TODO 检测各种cd刷新以及隐藏关卡检测
		mdFunction.cdBattleReset();
		uiFunction.cdPowerReset();

		// 精英副本挑战次数检测
		if (type == MatchTypeEnum.Match_Elite.value()) {
			Map<String, Integer> battleKeyIdMap = matchEntity.getBattleKeyIdsMap();
			if (battleKeyIdMap.containsKey(String.valueOf(passcardId)) && battleKeyIdMap.get(String.valueOf(passcardId)) >= GameConstant.ELITE_COUNT) {
				// 挑战次数不足
				logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
				return;
			}
		}
		// 检测体力
		if (!uiFunction.checkPower(FormulaUtil.getMatchPower(matchPrototype.getRequirePower(), type))) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_User, message);
			return;
		}
		// 检测整容
		if (!mdFunction.checkMatchFormationInfo(formationInfoList)) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Hero, message);
			return;
		}

		// 创建比赛缓存
		mdFunction.createMatchCache(type, passcardId);
	}

}
