package com.cwa.server.logic.module.match.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.MatchConcealEntity;
import com.cwa.data.entity.domain.MatchEntity;
import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.data.entity.domain.MatchStarEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.MatchMessage.ConcealInfoBean;
import com.cwa.message.MatchMessage.DropInfoBean;
import com.cwa.message.MatchMessage.MatchMopupDown;
import com.cwa.message.MatchMessage.MatchMopupUp;
import com.cwa.message.MatchMessage.MopupDropInfoBean;
import com.cwa.message.MatchMessage.ShopInfoBean;
import com.cwa.prototype.MatchChapterPrototype;
import com.cwa.prototype.MatchDropPrototype;
import com.cwa.prototype.MatchPrototype;
import com.cwa.prototype.MatchShopPrototype;
import com.cwa.prototype.gameEnum.MatchTypeEnum;
import com.cwa.prototype.gameEnum.PasscardTypeEnum;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.MatchConcealDataFunction;
import com.cwa.server.logic.dataFunction.MatchDataFunction;
import com.cwa.server.logic.dataFunction.MatchMopupDataFunction;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.MatchStarDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.util.FormulaUtil;
import com.cwa.util.RandomUtil;

/**
 * 扫荡
 * 
 * @author tzy
 * 
 */
public class MatchMopupUpHandler extends IGameHandler<MatchMopupUp> {

	@Override
	public void loginedHandler(MatchMopupUp message, IPlayer player) {
		int matchType = message.getMatchType(); // 1:普通副本2:精英副本
		int passcardId = message.getPasscardId();// 关卡id
		boolean isMore = message.getMore(); // 是否多次扫荡
		int count = 1;
		int needPower = 0;
		// 数据
		MatchDataFunction mdFunction = (MatchDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		MatchMopupDataFunction mmFunction= (MatchMopupDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		UserinfoDataFunction uiFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		MatchEntity matchEntity = mdFunction.getEntity(matchType);
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

		if (matchEntity.matchKeyId <= passcardId) {
			// 不可以打未通过关卡
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		if (passcardId != PasscardTypeEnum.Passcard_Noraml.value()) {
			// 只能掃蕩普通關卡
			// 没有对应关卡
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_PasscardId, message);
			return;
		}
		// 批量扫荡
		if (isMore) {
			if (matchType == MatchTypeEnum.Match_Noraml.value()) {
				count = GameConstant.MOPUP_NORMAL_COUNT;

			} else if (matchType == MatchTypeEnum.Match_Elite.value()) {
				count = GameConstant.MOPUP_ELITE_COUNT;
			}
		}
		needPower = FormulaUtil.getMatchPower(matchPrototype.getRequirePower(), matchType) * count;

		//检测各种cd刷新以及隐藏关卡检测
		 mdFunction.cdBattleReset();
		 mmFunction.cdMopupReset();
		 uiFunction.cdPowerReset();

		// 精英副本挑战次数检测
		if (matchType == MatchTypeEnum.Match_Elite.value()) {
			Map<String, Integer> battleKeyIdMap = matchEntity.getBattleKeyIdsMap();
			if (battleKeyIdMap.get(String.valueOf(passcardId)) + count > GameConstant.ELITE_COUNT) {
				// 挑战次数不足
				logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
				return;
			}
		}
		// 检测体力
		if (!uiFunction.checkPower(needPower)) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_User, message);
			return;
		}
		
		//检测扫荡次数
		if (!mmFunction.checkMopupCount(count)) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}

		MatchStarDataFunction matchStarDataFunction = (MatchStarDataFunction) player.getDataFunctionManager().getDataFunction(MatchStarEntity.class);
		MatchDataFunction mFunction = (MatchDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		int star = matchStarDataFunction.getStar(matchType, passcardId, matchPrototype.getChapter());
		List<MopupDropInfoBean> mopupDropList = new ArrayList<MopupDropInfoBean>();
		// 增加比赛次数
		mFunction.modifyBattleCount(matchType, passcardId, count);
		// 扣除体力
		uiFunction.changePower(-needPower,false);
		
		//扫荡
		mmFunction.mopup(count);

		// 检测隐藏关卡开启
		ConcealInfoBean.Builder concealInfoBean = ConcealInfoBean.newBuilder();
		boolean concealOpen = false;
		boolean checkconceal = false;
		MatchConcealDataFunction mcFunction = (MatchConcealDataFunction) player.getDataFunctionManager().getDataFunction(MatchConcealEntity.class);
		if (!mcFunction.contains(matchPrototype.getConcealPasscard())) {
			if (mcFunction.checkConceal(passcardId)) {
				checkconceal = true;
			}
		}

		// 检测神秘商店开启
		ShopInfoBean.Builder shopInfoBean = ShopInfoBean.newBuilder();
		boolean shopOpen = false;

		for (int i = 0; i < count; i++) {
			List<DropInfoBean> dropList = new ArrayList<DropInfoBean>();
			int dropKeyId = matchPrototype.getDropList().get(matchType - 1);
			MatchDropPrototype matchDropPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchDropPrototype.class, dropKeyId);
			ItemDataFunction iFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

			// 普通关卡修改星级和增加积分
			if (matchPrototype.getType() == PasscardTypeEnum.Passcard_Noraml.value()) {
				MatchStarDataFunction msFunction = (MatchStarDataFunction) player.getDataFunctionManager().getDataFunction(MatchStarEntity.class);
				msFunction.winMatch(matchType, passcardId, matchPrototype.getChapter(), star, matchDropPrototype.getGrade());

				// 检测是否掉落特产
				if (msFunction.checkGrade(matchType, passcardId, matchPrototype.getChapter(), matchDropPrototype.getRequireGrade())) {
					// 特产掉落
					List<Object> itemInfos = iFunction.useChests(matchDropPrototype.getGradeDrop());
					for (Object o : itemInfos) {
						DropInfoBean.Builder dropInfoBean = DropInfoBean.newBuilder();
						int[] itemInfo = (int[]) o;
						dropInfoBean.setGoodsId(itemInfo[0]);
						dropInfoBean.setCount(itemInfo[1]);
						dropList.add(dropInfoBean.build());
						logicContext.getUpdateManager().drop(itemInfo[0], itemInfo[1], player);
					}
				}
			}

			// 顺序随机
			List<Integer> ratiosList = matchDropPrototype.getCountRatiosList();
			int index = RandomUtil.getOrderRandom(matchDropPrototype.getCountRatiosList(), ratiosList.get(ratiosList.size() - 1));
			if (index >= 0) {
				List<Integer> randomList = matchDropPrototype.getRandomDropList();
				for (int j = index; j < randomList.size(); j++) {
					List<Object> itemInfos = iFunction.useChests(randomList.get(i));
					for (Object o : itemInfos) {
						DropInfoBean.Builder dropInfoBean = DropInfoBean.newBuilder();
						int[] itemInfo = (int[]) o;
						dropInfoBean.setGoodsId(itemInfo[0]);
						dropInfoBean.setCount(itemInfo[1]);
						dropList.add(dropInfoBean.build());
						logicContext.getUpdateManager().drop(itemInfo[0], itemInfo[1], player);
					}
				}
			}

			// 固定掉落
			logicContext.getUpdateManager().drop(matchDropPrototype.getDropGoodList(), matchDropPrototype.getDropCountList(), player);

			// 检测隐藏关卡开启
			if (checkconceal) {
				if (RandomUtil.getSuccessRandom(needPower, matchPrototype.getConcealRatios()) == 1) {
					mcFunction.newCreateEntity(player, passcardId);
					concealOpen = true;
					checkconceal = false;
					concealInfoBean.setPasscardId(passcardId);
					//剩余时间
					concealInfoBean.setTime(mcFunction.getExistTime(passcardId));
				}
			}

			// 检测神秘商店开启
			MatchChapterPrototype matchChapterPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchChapterPrototype.class, matchPrototype.getChapter());
			List<Integer> shopRatiosList = matchChapterPrototype.getBusinessmanSatioList();
			List<Integer> shopList = matchChapterPrototype.getBusinessmanList();
			int shopIndex = 0;
			int shopId = 0;

			for (Integer integer : shopRatiosList) {
				// 随机神秘商店
				if (RandomUtil.getSuccessRandom(needPower, integer) == 1) {
					shopId = shopList.get(shopIndex);
					break;
				}
				shopIndex++;
			}
			if (shopId != 0) {
				MatchShopDataFunction msFunction = (MatchShopDataFunction) player.getDataFunctionManager().getDataFunction(MatchShopEntity.class);
				MatchShopPrototype matchShopPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchShopPrototype.class, shopId);
				int shopType = matchShopPrototype.getType();
				if (!msFunction.contains(shopType)) {
					// 新建商店
					msFunction.newCreateEntity(player,shopId, shopType);
					// 刷新商品
					List<Integer> goodsIndexList = msFunction.refreshShop(shopId,false);
					shopInfoBean.addAllGoodsIndex(goodsIndexList);
					shopInfoBean.setType(shopType);
					shopInfoBean.setShopIp(shopId);
					shopInfoBean.setRefreshCount(msFunction.getRefreshCount(shopType));
					//  剩余时间
					shopInfoBean.setTime(msFunction.getExistTime(shopType));
					shopOpen = true;
				}
			}
			MopupDropInfoBean.Builder mopupDropInfoBean = MopupDropInfoBean.newBuilder();
			mopupDropInfoBean.addAllDropInfoBean(dropList);
			mopupDropList.add(mopupDropInfoBean.build());
		}
		sendMatchMopupDownMessage(mopupDropList, concealOpen, shopOpen, concealInfoBean, shopInfoBean, player);
	}

	private void sendMatchMopupDownMessage(List<MopupDropInfoBean> mopupDropList, boolean cancealOpen, boolean shopOpen, ConcealInfoBean.Builder  concealInfoBean,
			ShopInfoBean.Builder shopInfoBean, IPlayer player) {
		MatchMopupDown.Builder b = MatchMopupDown.newBuilder();
		b.addAllMopupDropInfoBean(mopupDropList);
		b.setCancealOpen(cancealOpen);
		b.setShopOpen(shopOpen);
		if (shopOpen) {
			b.setShopInfoBean(shopInfoBean.build());
		}
		if (cancealOpen) {
			b.setConcealInfoBean(concealInfoBean.build());
		}
		player.getSession().send(b.build());
	}
}