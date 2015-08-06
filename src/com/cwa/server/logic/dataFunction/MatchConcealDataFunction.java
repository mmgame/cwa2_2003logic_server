package com.cwa.server.logic.dataFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IMatchConcealEntityDao;
import com.cwa.data.entity.domain.MatchConcealEntity;
import com.cwa.prototype.ConcealConditionPrototype;
import com.cwa.prototype.gameEnum.CDTypeEnum;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.TimeUtil;

/**
 * 隐藏关卡数据封装
 * 
 * @author mausmars
 * 
 */
public class MatchConcealDataFunction implements IDataFunction {
	// {关卡：MatchConcealEntity}
	private Map<Integer, MatchConcealEntity> entityMap = new HashMap<Integer, MatchConcealEntity>();

	private IMatchConcealEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public MatchConcealDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IMatchConcealEntityDao) dbSession.getEntityDao(MatchConcealEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<MatchConcealEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (MatchConcealEntity e : entitys) {
			entityMap.put(e.concealId, e);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	// 出现新的隐藏关卡
	public void newCreateEntity(IPlayer player, int passcardId) {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_CONCEAL, player.getLogicContext());
		if (entityMap.containsKey(passcardId)) {
			MatchConcealEntity entity = entityMap.get(passcardId);
			entity.resetTime = cdHandler.resetcd(player, CDTypeEnum.CD_CONCEAL.value());
			updateEntity(entity);
		} else {
			MatchConcealEntity entity = new MatchConcealEntity();
			entity.userId = player.getUserId();
			entity.concealId = passcardId;
			entity.resetTime = cdHandler.resetcd(player, CDTypeEnum.CD_CONCEAL.value());
			entityMap.put(passcardId, entity);
			// 插入实体
			insertEntity(entity);
		}

	}

	public MatchConcealEntity getEntity(int matchType) {
		return entityMap.get(matchType);
	}

	public Collection<MatchConcealEntity> getAllEntity() {
		return entityMap.values();
	}

	// 检测是否存在该隐藏关卡
	public boolean contains(int passcardId) {
		if (!entityMap.containsKey(passcardId)) {
			return false;
		}
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_CONCEAL, player.getLogicContext());
		MatchConcealEntity entity = entityMap.get(passcardId);
		boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_CONCEAL.value(), entity.resetTime);
		if (isFinishedCD) {
			return false;
		}
		return true;
	}

	// 获取隐藏关卡剩余时间
	public long getExistTime(int passcardId) {
		MatchConcealEntity entity = entityMap.get(passcardId);
		long t = entity.resetTime - TimeUtil.currentSystemTime();
		return t > 0 ? t : 0l;
	}

	// 检测开启条件
	public boolean checkConceal(int conditionId) {
		if (conditionId == 0) {
			return true;
		}
		ConcealConditionPrototype concealConditionPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(ConcealConditionPrototype.class, conditionId);
		int conditiionKeyId = concealConditionPrototype.getCondition();
		List<String> params = concealConditionPrototype.getConditionParamList();
		if (!player.getLogicContext().getCheckManager().check(conditiionKeyId, params, player)) {
			return false;
		}
		return true;
	}

	// 移除隐藏关卡
	public void removeConceal(int passcardId) {
		MatchConcealEntity entity = entityMap.get(passcardId);
		entity.resetTime = 0;
		updateEntity(entity);
	}

	private void updateEntity(MatchConcealEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(MatchConcealEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
