package com.cwa.server.logic.dataFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IMatchStarEntityDao;
import com.cwa.data.entity.domain.MatchStarEntity;
import com.cwa.prototype.gameEnum.MatchTypeEnum;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.util.FormulaUtil;

/**
 * 副本星级数据封装
 * 
 * @author mausmars
 * 
 */
public class MatchStarDataFunction implements IDataFunction {
	// {副本类型+关卡id：MatchStarEntity}
	private Map<Integer, MatchStarEntity> entityMap = new HashMap<Integer, MatchStarEntity>();

	private IMatchStarEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public MatchStarDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IMatchStarEntityDao) dbSession.getEntityDao(MatchStarEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<MatchStarEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (MatchStarEntity e : entitys) {
			entityMap.put(getSKey(e.matchType, e.chapterId), e);
		}
		if (newRegister) {
			for (MatchTypeEnum matchTypeEnum : MatchTypeEnum.values()) {
				newCreateEntity(matchTypeEnum.value(), 1);
			}
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public MatchStarEntity newCreateEntity(int matchType, int chapterId) {
		MatchStarEntity entity = new MatchStarEntity();
		entity.userId = player.getUserId();
		entity.matchType = matchType;
		entity.chapterId = chapterId;
		entity.setChapterScoresMap(new HashMap<String, Integer>());
		entity.setChapterStarsMap(new HashMap<String, Integer>());
		entityMap.put(getSKey(matchType, chapterId), entity);
		// 插入实体
		insertEntity(entity);
		return entity;
	}

	// 获得星数
	public int getStar(int matchType, int passcardId, int chapterId) {
		return getEntity(matchType, chapterId).getChapterStarsMap().get(passcardId);
	}

	/**
	 * 副本获胜
	 * 
	 * @param matchType
	 * @param passcardId
	 * @param chapterId
	 * @param star
	 * @param grade
	 */
	public void winMatch(int matchType, int passcardId, int chapterId, int star, int grade) {
		MatchStarEntity matchStarEntity = getEntity(matchType, chapterId);
		if (matchStarEntity == null) {
			newCreateEntity(matchType, chapterId);
		}
		Map<String, Integer> starMap = matchStarEntity.getChapterStarsMap();
		if (!starMap.containsKey(String.valueOf(passcardId))) {
			starMap.put(String.valueOf(passcardId), 0);
		}
		// 加星
		if (star > starMap.get(String.valueOf(passcardId))) {
			starMap.put(String.valueOf(passcardId), star);
		}

		// 加积分
		Map<String, Integer> gradeMap = matchStarEntity.getChapterScoresMap();
		if (!gradeMap.containsKey(String.valueOf(passcardId))) {
			gradeMap.put(String.valueOf(passcardId), 0);
		}
		grade = gradeMap.get(String.valueOf(passcardId)) + FormulaUtil.getStarGradePower(grade, star);
		gradeMap.put(String.valueOf(passcardId), grade);

		updateEntity(matchStarEntity);
	}

	// 检测积分
	public boolean checkGrade(int matchType, int passcardId, int chapterId, int needGrade) {
		MatchStarEntity matchStarEntity = getEntity(matchType, chapterId);
		int grade = matchStarEntity.getChapterScoresMap().get(String.valueOf(passcardId));
		if (grade >= needGrade) {
			// 如果满足直接扣除所需积分
			matchStarEntity.getChapterScoresMap().put(String.valueOf(passcardId), grade - needGrade);
			updateEntity(matchStarEntity);
			return true;
		}
		return false;
	}

	// 获取章节总星数
	public int getTotalStar(int chapterId) {
		int total = 0;
		for (MatchTypeEnum matchTypeEnum : MatchTypeEnum.values()) {
			MatchStarEntity entity = getEntity(matchTypeEnum.value(), chapterId);
			if (entity != null) {
				Map<String, Integer> starMap = entity.getChapterStarsMap();
				for (Integer i : starMap.values()) {
					total += i;
				}
			}
		}
		return total;

	}

	public MatchStarEntity getEntity(int matchType, int chapterId) {
		return entityMap.get(getSKey(matchType, chapterId));
	}

	private int getSKey(int matchType, int chapterId) {
		return matchType * 10000 + chapterId;
	}

	public Collection<MatchStarEntity> getAllEntity() {
		return entityMap.values();
	}

	public List<MatchStarEntity> getEntityByMatchType(int matchType) {
		List<MatchStarEntity> entitys = new ArrayList<MatchStarEntity>();
		for (int i = 1;; i++) {
			MatchStarEntity e = entityMap.get(getSKey(matchType, i));
			if (e == null) {
				break;
			}
			entitys.add(e);
		}
		return entitys;
	}

	private void updateEntity(MatchStarEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(MatchStarEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
