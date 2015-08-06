package com.cwa.server.logic.module.match.handler;

import java.util.ArrayList;
import java.util.List;

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
import com.cwa.message.MatchMessage.MatchOverDown;
import com.cwa.message.MatchMessage.MatchOverUp;
import com.cwa.message.MatchMessage.ShopInfoBean;
import com.cwa.prototype.MatchChapterPrototype;
import com.cwa.prototype.MatchDropPrototype;
import com.cwa.prototype.MatchPrototype;
import com.cwa.prototype.MatchShopPrototype;
import com.cwa.prototype.gameEnum.PasscardTypeEnum;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.MatchConcealDataFunction;
import com.cwa.server.logic.dataFunction.MatchDataFunction;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.MatchStarDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.module.match.cache.MatchCache;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.util.FormulaUtil;
import com.cwa.util.RandomUtil;

/**
 * 比赛结束
 * 
 * @author tzy
 * 
 */
public class MatchOverUpHandler extends IGameHandler<MatchOverUp> {

	@Override
	public void loginedHandler(MatchOverUp message, IPlayer player) {
		int resultType = message.getResultType();// 1胜利，0失败

		MatchDataFunction mFunction = (MatchDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		UserinfoDataFunction uiFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		MatchCache matchCache = mFunction.getMatchCache();
		if (matchCache == null) {
			// 比赛不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Match, message);
			return;
		}
		int matchType = matchCache.getMatchType();
		int matchKeyId = matchCache.getMatchKeyId();

		MatchPrototype matchPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchPrototype.class, matchKeyId);
		int needPower = FormulaUtil.getMatchPower(matchPrototype.getRequirePower(), matchType);
		List<DropInfoBean> dropList = new ArrayList<DropInfoBean>();
		if (resultType == 1) {// 胜利
			int star = message.getStar();
			if (star > GameConstant.MATCH_MAX_STAR || star < GameConstant.MATCH_MIN_STAR) {
				// 星级错误
				logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_Star, message);
				// 移除缓存
				mFunction.cleanCache();
				return;
			}

			int dropKeyId = matchPrototype.getDropList().get(matchType - 1);
			MatchDropPrototype matchDropPrototype = player.getLogicContext().getprototypeManager().getPrototype(MatchDropPrototype.class, dropKeyId);
			ItemDataFunction iFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);
			// 设置下一关以及精英副本比赛次数加1
			mFunction.winMatch(matchType, matchKeyId, matchDropPrototype.getNextKeyId());
			// 扣除体力
			uiFunction.changePower(-needPower, false);

			// 普通关卡修改星级和增加积分
			if (matchPrototype.getType() == PasscardTypeEnum.Passcard_Noraml.value()) {
				MatchStarDataFunction msFunction = (MatchStarDataFunction) player.getDataFunctionManager().getDataFunction(MatchStarEntity.class);
				msFunction.winMatch(matchType, matchKeyId, matchPrototype.getChapter(), star, matchDropPrototype.getGrade());

				// 检测是否掉落特产
				if (msFunction.checkGrade(matchType, matchKeyId, matchPrototype.getChapter(), matchDropPrototype.getRequireGrade())) {
					// 特产掉落
					List<Object> itemInfos = iFunction.useChests(matchDropPrototype.getGradeDrop());
					for (Object i : itemInfos) {
						DropInfoBean.Builder dropInfoBean = DropInfoBean.newBuilder();
						int[] itemInfo = (int[]) i;
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
				for (int i = index; i < randomList.size(); i++) {
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

			ConcealInfoBean.Builder concealInfoBean = ConcealInfoBean.newBuilder();
			boolean concealOpen = false;

			MatchConcealDataFunction mcFunction = (MatchConcealDataFunction) player.getDataFunctionManager().getDataFunction(MatchConcealEntity.class);
			// 如果所打关卡为隐藏关卡，胜利后移除
			if (matchPrototype.getType() == PasscardTypeEnum.Passcard_Conceal.value()) {
				mcFunction.removeConceal(matchKeyId);
			}

			if (!mcFunction.contains(matchPrototype.getConcealPasscard())) {
				if (mcFunction.checkConceal(matchPrototype.getConcealCondition())) {
					if (RandomUtil.getSuccessRandom(needPower, matchPrototype.getConcealRatios()) == 1) {
						mcFunction.newCreateEntity(player, matchKeyId);
						concealOpen = true;
						concealInfoBean.setPasscardId(matchKeyId);
						// 剩余时间
						concealInfoBean.setTime(mcFunction.getExistTime(matchKeyId));
					}
				}
			}

			// 检测神秘商店开启
			ShopInfoBean.Builder shopInfoBean = ShopInfoBean.newBuilder();
			boolean shopOpen = false;

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
				//如果不存在该商店
				if (!msFunction.contains(shopType) && msFunction.checkNewShop(shopType)) {
					// 新建商店
					msFunction.newCreateEntity(player, shopId, shopType);
					// 刷新商品
					List<Integer> goodsIndexList = msFunction.refreshShop(shopId, false);
					shopInfoBean.addAllGoodsIndex(goodsIndexList);
					shopInfoBean.setType(shopType);
					shopInfoBean.setShopIp(shopId);
					shopInfoBean.setRefreshCount(msFunction.getRefreshCount(shopType));
					// 剩余时间
					shopInfoBean.setTime(msFunction.getExistTime(shopType));
					shopOpen = true;
				}
			}
			sendMatchOverDownMessage(dropList, concealOpen, shopOpen, concealInfoBean, shopInfoBean, player);

		} else if (resultType == 0) {// 失败
			// 扣除体力
			uiFunction.changePower(-FormulaUtil.getFailMatchPower(needPower), false);
			// 掉落家园经验
			uiFunction.upgradeLevel(needPower);
			DropInfoBean.Builder dropInfoBean = DropInfoBean.newBuilder();
			dropInfoBean.setGoodsId(GameConstant.USER_EXP_KEYID);
			dropInfoBean.setCount(FormulaUtil.getFailMatchPower(needPower));
			dropList.add(dropInfoBean.build());
			sendMatchOverDownMessage(dropList, false, false, null, null, player);
		}
		// 移除缓存
		mFunction.cleanCache();
	}

	private void sendMatchOverDownMessage(List<DropInfoBean> dropInfoList, boolean cancealOpen, boolean shopOpen, ConcealInfoBean.Builder concealInfoBean, ShopInfoBean.Builder shopInfoBean,
			IPlayer player) {
		MatchOverDown.Builder b = MatchOverDown.newBuilder();
		b.addAllDropInfoBean(dropInfoList);
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
