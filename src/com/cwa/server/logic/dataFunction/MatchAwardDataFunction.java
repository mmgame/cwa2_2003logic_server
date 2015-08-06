package com.cwa.server.logic.dataFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IMatchAwardEntityDao;
import com.cwa.data.entity.domain.MatchAwardEntity;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.ByteOperationUtil;

/**
 * 章节领奖状态数据封装
 * 
 * @author mausmars
 * 
 */
public class MatchAwardDataFunction implements IDataFunction {
	// {章节：MatchAwardEntity}
	private Map<Integer, MatchAwardEntity> entityMap = new HashMap<Integer, MatchAwardEntity>();

	private IMatchAwardEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public MatchAwardDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IMatchAwardEntityDao) dbSession.getEntityDao(MatchAwardEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<MatchAwardEntity> entitys =  dao.selectEntityByUserId(player.getUserId(), createParams());
		for (MatchAwardEntity e : entitys) {
			entityMap.put(e.chapterId, e);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity(int chapterId) {
		MatchAwardEntity entity = new MatchAwardEntity();
		entity.userId = player.getUserId();
		entity.chapterId=chapterId;
		entity.awardStates=0;
		entityMap.put(chapterId, entity);
		// 插入实体
		insertEntity(entity);
	}

	public MatchAwardEntity getEntity(int shopType) {
		return entityMap.get(shopType);
	}

	public Collection<MatchAwardEntity> getAllEntity() {
		return entityMap.values();
	}

	// 检测是否能领奖
	public boolean canAward(int chapterId,int index) {
		if (!entityMap.containsKey(chapterId)) {
			return false;
		}
		MatchAwardEntity entity = entityMap.get(chapterId);
		if (ByteOperationUtil.checkIntNByte(entity.awardStates, index - 1)) {
			return false;
		}
		return true;
	}
	
	// 领奖
	public void award(int chapterId,int index) {
		if (!entityMap.containsKey(chapterId)) {
			newCreateEntity(chapterId);
		}
		MatchAwardEntity entity = entityMap.get(chapterId);
		int awardState=entity.awardStates;
		awardState = ByteOperationUtil.setIntNByte(awardState, index - 1);
		entity.awardStates=awardState;
		updateEntity(entity);
	}


	private void updateEntity(MatchAwardEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(MatchAwardEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
