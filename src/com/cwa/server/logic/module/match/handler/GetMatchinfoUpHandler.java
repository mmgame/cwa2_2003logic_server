package com.cwa.server.logic.module.match.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cwa.data.entity.domain.MatchAwardEntity;
import com.cwa.data.entity.domain.MatchConcealEntity;
import com.cwa.data.entity.domain.MatchEntity;
import com.cwa.data.entity.domain.MatchMopupEntity;
import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.data.entity.domain.MatchStarEntity;
import com.cwa.message.MatchMessage.BattleInfoBean;
import com.cwa.message.MatchMessage.ChapterAwardInfoBean;
import com.cwa.message.MatchMessage.ChapterStarInfoBean;
import com.cwa.message.MatchMessage.ConcealInfoBean;
import com.cwa.message.MatchMessage.GetMatchinfoDown;
import com.cwa.message.MatchMessage.GetMatchinfoUp;
import com.cwa.message.MatchMessage.MatchInfoBean;
import com.cwa.message.MatchMessage.ResetInfoBean;
import com.cwa.message.MatchMessage.ShopInfoBean;
import com.cwa.message.MatchMessage.StarInfoBean;
import com.cwa.server.logic.dataFunction.MatchAwardDataFunction;
import com.cwa.server.logic.dataFunction.MatchConcealDataFunction;
import com.cwa.server.logic.dataFunction.MatchDataFunction;
import com.cwa.server.logic.dataFunction.MatchMopupDataFunction;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.MatchStarDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取副本相关信息
 * 
 * @author tzy
 * 
 */
public class GetMatchinfoUpHandler extends IGameHandler<GetMatchinfoUp> {

	@Override
	public void loginedHandler(GetMatchinfoUp message, IPlayer player) {
		// 数据
		MatchDataFunction mdFunction = (MatchDataFunction) player.getDataFunctionManager().getDataFunction(MatchEntity.class);
		MatchMopupDataFunction mmFunction = (MatchMopupDataFunction) player.getDataFunctionManager().getDataFunction(MatchMopupEntity.class);
		MatchConcealDataFunction mcFunction = (MatchConcealDataFunction) player.getDataFunctionManager().getDataFunction(MatchConcealEntity.class);
		MatchShopDataFunction msFunction = (MatchShopDataFunction) player.getDataFunctionManager().getDataFunction(MatchShopEntity.class);
		MatchStarDataFunction mtFunction = (MatchStarDataFunction) player.getDataFunctionManager().getDataFunction(MatchStarEntity.class);
		MatchAwardDataFunction maFunction = (MatchAwardDataFunction) player.getDataFunctionManager().getDataFunction(MatchAwardEntity.class);

		// 考虑各种cd
		mdFunction.cdBattleReset();
		mmFunction.cdMopupReset();
		msFunction.cdRefreshShopReset();

		// 副本信息
		Collection<MatchEntity> matchEntityList =  mdFunction.getAllEntity();
		List<MatchInfoBean> matchInfoList = new ArrayList<MatchInfoBean>();
		for (MatchEntity matchEntity : matchEntityList) {
			int matchType = matchEntity.matchType;

			// 章节信息
			List<ChapterStarInfoBean> chapterInfoList = new ArrayList<ChapterStarInfoBean>();
			List<MatchStarEntity> matchStarEntityList = mtFunction.getEntityByMatchType(matchType);
			for (MatchStarEntity matchStarEntity : matchStarEntityList) {
				List<StarInfoBean> starInfoList = new ArrayList<StarInfoBean>();
				Map<String, Integer> starMap = matchStarEntity.getChapterStarsMap();
				for (Entry<String, Integer> entry : starMap.entrySet()) {
					StarInfoBean.Builder starInfoBean = StarInfoBean.newBuilder();
					starInfoBean.setPasscardId(Integer.parseInt(entry.getKey()));
					starInfoBean.setStar(entry.getValue());
					starInfoList.add(starInfoBean.build());
				}
				ChapterStarInfoBean.Builder chapterInfoBean = ChapterStarInfoBean.newBuilder();
				chapterInfoBean.addAllStar(starInfoList);
				chapterInfoBean.setChapterId(matchStarEntity.chapterId);
				chapterInfoList.add(chapterInfoBean.build());
			}
			
			
			// 打过的副本
			List<BattleInfoBean> battleInfoList = new ArrayList<BattleInfoBean>();
			Map<String, Integer> battleMap = matchEntity.getBattleKeyIdsMap();
			for (Entry<String, Integer> e : battleMap.entrySet()) {
				BattleInfoBean.Builder battleInfoBean = BattleInfoBean.newBuilder();
				battleInfoBean.setPasscardId(Integer.parseInt(e.getKey()));
				battleInfoBean.setCount(e.getValue());
				battleInfoList.add(battleInfoBean.build());
			}
			// 重置信息
			List<ResetInfoBean> resetInfoList = new ArrayList<ResetInfoBean>();
			Map<String, Integer> resetMap = matchEntity.getResetKeyIdsMap();
			for (Entry<String, Integer> e : resetMap.entrySet()) {
				ResetInfoBean.Builder resetInfoBean = ResetInfoBean.newBuilder();
				resetInfoBean.setPasscardId(Integer.parseInt(e.getKey()));
				resetInfoBean.setCount(e.getValue());
				resetInfoList.add(resetInfoBean.build());
			}
			MatchInfoBean.Builder matchInfoBean = MatchInfoBean.newBuilder();
			matchInfoBean.setType(matchType);
			matchInfoBean.setPasscard(matchEntity.matchKeyId);
			matchInfoBean.addAllChapterStarInfoBean(chapterInfoList);
			matchInfoBean.addAllBattleInfoBean(battleInfoList);
			matchInfoBean.addAllResetInfoBean(resetInfoList);
			matchInfoList.add(matchInfoBean.build());
		}
		// 领奖状态
		Collection<MatchAwardEntity> matchAwardList = maFunction.getAllEntity();
		List<ChapterAwardInfoBean> chapterAwardInfoList = new ArrayList<ChapterAwardInfoBean>();
		for (MatchAwardEntity matchAwardEntity : matchAwardList) {
			ChapterAwardInfoBean.Builder chapterAwardInfoBean = ChapterAwardInfoBean.newBuilder();
			chapterAwardInfoBean.setChapterId(matchAwardEntity.chapterId);
			chapterAwardInfoBean.setAwardStates(matchAwardEntity.awardStates);
			chapterAwardInfoList.add(chapterAwardInfoBean.build());
		}

		// 隐藏关卡信息
		Collection<MatchConcealEntity> matchConcealEntityList =mcFunction.getAllEntity();
		List<ConcealInfoBean> concealInfoList = new ArrayList<ConcealInfoBean>();
		for (MatchConcealEntity matchConcealEntity : matchConcealEntityList) {
			long time = mcFunction.getExistTime(matchConcealEntity.concealId);
			if (time == 0) {
				continue;
			}
			ConcealInfoBean.Builder concealInfoBean = ConcealInfoBean.newBuilder();
			concealInfoBean.setPasscardId(matchConcealEntity.concealId);
			concealInfoBean.setTime(time);
			concealInfoList.add(concealInfoBean.build());
		}

		// 神秘商店信息
		Collection<MatchShopEntity> matchShopEntityList = msFunction.getAllEntity();
		List<ShopInfoBean> shopInfoList = new ArrayList<ShopInfoBean>();
		for (MatchShopEntity matchShopEntity : matchShopEntityList) {
			long time = msFunction.getExistTime(matchShopEntity.shopType);
			if (time == 0) {
				continue;
			}
			List<Integer> goodsList = matchShopEntity.getGoodsIndexList();
			ShopInfoBean.Builder shopInfoBean = ShopInfoBean.newBuilder();
			shopInfoBean.setType(matchShopEntity.shopType);
			shopInfoBean.setShopIp(matchShopEntity.shopId);
			shopInfoBean.setRefreshCount(matchShopEntity.refreshCount);
			shopInfoBean.setTime(time);
			shopInfoBean.addAllGoodsIndex(goodsList);
			shopInfoList.add(shopInfoBean.build());
		}

		GetMatchinfoDown.Builder b = GetMatchinfoDown.newBuilder();
		b.addAllMatchInfoBean(matchInfoList);
		b.addAllConcealInfoBean(concealInfoList);
		b.addAllShopInfoBean(shopInfoList);
		b.addAllChapterAwardInfoBean(chapterAwardInfoList);
		b.setMopupCount(mmFunction.getEntity().mopupCount);
		player.getSession().send(b.build());

	}
}
