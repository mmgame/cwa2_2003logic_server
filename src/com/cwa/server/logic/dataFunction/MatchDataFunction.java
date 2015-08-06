package com.cwa.server.logic.dataFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.data.entity.IMatchEntityDao;
import com.cwa.data.entity.domain.MatchEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.MatchMessage.FormationInfoBean;
import com.cwa.prototype.FunctionPrototype;
import com.cwa.prototype.GameInitPrototype;
import com.cwa.prototype.gameEnum.CDTypeEnum;
import com.cwa.prototype.gameEnum.FunctionTypeEnum;
import com.cwa.prototype.gameEnum.IdsTypeEnum;
import com.cwa.prototype.gameEnum.MatchTypeEnum;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.module.match.cache.MatchCache;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.TimeUtil;

/**
 * 副本数据封装
 * 
 * @author mausmars
 * 
 */
public class MatchDataFunction implements IDataFunction {
	// {副本类型：MatchEntity}
	private Map<Integer, MatchEntity> entityMap = new HashMap<Integer, MatchEntity>();

	private MatchCache matchCache;
	private IMatchEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public MatchDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IMatchEntityDao) dbSession.getEntityDao(MatchEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<MatchEntity> entitys =   dao.selectEntityByUserId(player.getUserId(), createParams());
		for (MatchEntity e : entitys) {
			entityMap.put(e.matchType, e);
		}

		if (newRegister) {
			IPrototypeClientService prototypeService = player.getLogicContext().getprototypeManager();
			List<GameInitPrototype> gameInitPrototypeList = prototypeService.getAllPrototype(GameInitPrototype.class);
			for (GameInitPrototype gameInitPrototype : gameInitPrototypeList) {
				if (gameInitPrototype.getInitType() == IdsTypeEnum.Ids_Match.value()) {
					newCreateEntity(gameInitPrototype.getInitSubType(), gameInitPrototype.getInitParamList().get(0));
				}
			}
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity(int matchType, int passcardId) {
		MatchEntity entity = new MatchEntity();
		entity.userId = player.getUserId();
		entity.matchType = matchType;
		entity.matchKeyId = passcardId;
		entity.resetTime = TimeUtil.currentSystemTime();
		entity.setBattleKeyIdsMap(new HashMap<String, Integer>());
		entity.setResetKeyIdsMap(new HashMap<String, Integer>());
		entityMap.put(matchType, entity);

		// 插入实体
		insertEntity(entity);
	}

	public MatchEntity getEntity(int matchType) {
		return entityMap.get(matchType);
	}

	public MatchCache getMatchCache() {
		return matchCache;
	}

	// 创建副本战斗缓存
	public void createMatchCache(int matchType, int passcardId) {
		matchCache = new MatchCache();
		matchCache.setMatchType(matchType);
		matchCache.setMatchKeyId(passcardId);
		matchCache.setStartTime(TimeUtil.currentSystemTime());
		matchCache.setCheck(true);
		matchCache.setSession(player.getSession());
	}

	// 移除緩存
	public void cleanCache() {
		matchCache = null;
	}

	public Collection<MatchEntity> getAllEntity() {
		return entityMap.values();
	}

	/**
	 * 赢得比赛
	 * 
	 * @param matchType
	 * @param passcardId
	 * @param next
	 */
	public void winMatch(int matchType, int passcardId, int next) {
		MatchEntity matchEntity = entityMap.get(matchType);
		if (matchEntity.matchKeyId == passcardId) {
			// 设置下一关
			matchEntity.matchKeyId = next;
		}
		modifyBattleCount(matchType, passcardId, 1);
		updateEntity(matchEntity);
	}

	public void modifyBattleCount(int matchType, int passcardId, int count) {
		// 精英副本比赛次数加
		if (matchType == MatchTypeEnum.Match_Elite.value()) {
			MatchEntity matchEntity = entityMap.get(matchType);
			Map<String, Integer> battleKeyIdMap = matchEntity.getBattleKeyIdsMap();
			if (!battleKeyIdMap.containsKey(String.valueOf(passcardId))) {
				battleKeyIdMap.put(String.valueOf(passcardId), 0);
			}
			battleKeyIdMap.put(String.valueOf(passcardId), battleKeyIdMap.get(battleKeyIdMap) + count);
			updateEntity(matchEntity);
		}
	}

	// 精英副本次数重置
	public void refreshBattleCount(int passcardId) {
		MatchEntity matchEntity = entityMap.get(MatchTypeEnum.Match_Elite.value());
		Map<String, Integer> battleKeyIdMap = matchEntity.getBattleKeyIdsMap();
		if (battleKeyIdMap.containsKey(String.valueOf(passcardId))) {
			battleKeyIdMap.put(String.valueOf(passcardId), 0);
			updateEntity(matchEntity);
		}
	}

	// 获取刷新次数
	public int getRefreshCount(int passcardId) {
		MatchEntity matchEntity = entityMap.get(MatchTypeEnum.Match_Elite.value());
		Map<String, Integer> battleKeyIdMap = matchEntity.getBattleKeyIdsMap();
		if (battleKeyIdMap.containsKey(String.valueOf(passcardId))) {
			return battleKeyIdMap.get(String.valueOf(passcardId));
		}
		return 0;
	}
	
	
	/**
	 * 副本次数和重置次数cd检测
	 */
	public void cdBattleReset() {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_BATTLE, player.getLogicContext());
		for (MatchEntity matchEntity : entityMap.values()) {
			boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_BATTLE.value(), matchEntity.resetTime);
			if (isFinishedCD) {
				matchEntity.getBattleKeyIdsMap().clear();
				matchEntity.getResetKeyIdsMap().clear();
				// 重置
				if (logicContext.getCdManager().isSaveDb(CDTypeEnum.CD_BATTLE, logicContext)) {
					matchEntity.resetTime = TimeUtil.currentSystemTime();
				}
				updateEntity(matchEntity);
			}
		}
		
	}
	
	
	public boolean checkMatchFormationInfo(List<FormationInfoBean> beans) {
		Set<Integer> heroIds = new HashSet<Integer>();

		boolean isRetinue = false;
		Iterator<FormationInfoBean> beanIt = beans.iterator();
		for (; beanIt.hasNext();) {
			FormationInfoBean bean = beanIt.next();

			int heroId = bean.getHeroId();
			if (heroId > 0) {
				if (heroIds.contains(heroId)) {
					return false;
				}
				heroIds.add(heroId);
			}
			int retinueId = bean.getRetinueId();
			if (retinueId > 0) {
				if (heroIds.contains(retinueId)) {
					return false;
				}
				heroIds.add(retinueId);
				isRetinue = true;
			}
		}
		if (isRetinue) {
			UserinfoDataFunction fdFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
			FunctionPrototype functionPrototype = player.getLogicContext().getprototypeManager()
					.getPrototype(FunctionPrototype.class, FunctionTypeEnum.Function_Retinue.value());
			int userLevel = fdFunction.getLevel();
			if (userLevel < functionPrototype.getLevelMin()) {
				// 等级不足开启侍从
				return false;
			}
		}
		return true;
	}

	private void updateEntity(MatchEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(MatchEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
