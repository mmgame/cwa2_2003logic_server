package com.cwa.server.logic.dataFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IHeroEntityDao;
import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.gameEnum.HeroTrainStateEnum;
import com.cwa.message.FormationMessage.HeroInfoBean;
import com.cwa.prototype.HeroGradePrototype;
import com.cwa.prototype.HeroTrainPrototype;
import com.cwa.prototype.gameEnum.ElementEnum;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.GameUtil;
import com.cwa.util.RandomUtil;

/**
 * 英雄数据封装
 * 
 * @author mausmars
 * 
 */
public class HeroDataFunction implements IDataFunction {
	// {英雄keyid：HeroEntity}
	private Map<Integer, HeroEntity> entityMap = new HashMap<Integer, HeroEntity>();

	private IHeroEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public HeroDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IHeroEntityDao) dbSession.getEntityDao(HeroEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<HeroEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (HeroEntity e : entitys) {
			entityMap.put(e.heroId, e);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity() {

	}

	public HeroEntity getEntity(int heroKeyId) {
		return entityMap.get(heroKeyId);
	}

	public Collection<HeroEntity> getAllEntity() {
		return entityMap.values();
	}

	/**
	 * 验证英雄存在
	 * 
	 * @param heroKeyId
	 * @return
	 */
	public boolean checkHeroExist(int heroKeyId) {
		return getEntity(heroKeyId) != null;
	}

	/**
	 * 验证英雄培训状态
	 * 
	 * @param heroKeyId
	 * @return
	 */
	public boolean checkHeroTrainState(int heroKeyId, int expectState) {
		HeroEntity entity = getEntity(heroKeyId);
		return entity != null && entity.trainState == expectState;
	}

	/**
	 * 验证英雄进阶等级
	 * 
	 * @param heroKeyId
	 * @return
	 */
	public boolean checkHeroLevelEvolution(int heroKeyId) {
		HeroEntity entity = getEntity(heroKeyId);
		if (entity == null) {
			// 英雄不存在
			return false;
		}
		HeroGradePrototype herogradePrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(HeroGradePrototype.class, getHeroGradeKeyId(entity));
		int nextHeroGradeId = herogradePrototype.getNextLevel();
		if (nextHeroGradeId == 0) {
			// 不存在下个阶段
			return false;
		}
		if (entity.level < herogradePrototype.getNeedLevel()) {
			// 英雄等级不满足
			return false;
		}
		return true;
	}

	/**
	 * 检查英雄等级
	 * 
	 * @param heroKeyId
	 * @param expectLevel
	 *            当前等级要小于期待等级
	 * @return
	 */
	public boolean checkHeroLevel(int heroKeyId, int expectLevel, boolean isExpectGTR) {
		HeroEntity entity = getEntity(heroKeyId);
		if (isExpectGTR) {
			return entity != null && entity.level > expectLevel;
		} else {
			return entity != null && entity.level < expectLevel;
		}
	}

	/**
	 * 检查品质
	 * 
	 * @param heroKeyId
	 * @return
	 */
	public boolean checkHeroQuality(int heroKeyId, int quality) {
		HeroEntity entity = getEntity(heroKeyId);
		return entity.quality >= quality;
	}

	/**
	 * 训练
	 * 
	 * @param heroKeyId
	 * @param trainType
	 */
	public List<Integer> train(int heroKeyId, int trainType) {
		HeroEntity entity = getEntity(heroKeyId);

		HeroTrainPrototype heroTrainPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(HeroTrainPrototype.class, trainType);
		HeroGradePrototype herogradePrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(HeroGradePrototype.class, getHeroGradeKeyId(entity));

		int trainMax = herogradePrototype.getPatiencesMax();
		List<Integer> patienceTrainList = entity.getPatienceTrainList();
		List<Integer> waitPatienceTrainList = new ArrayList<Integer>();

		for (ElementEnum e : ElementEnum.values()) {
			int value = getTrainValue(patienceTrainList.get(e.value() - 1), heroTrainPrototype);
			value = (int) GameUtil.getAmongValue(0, value, trainMax);
			waitPatienceTrainList.add(value);
		}
		entity.setWaitPatienceTrainList(waitPatienceTrainList);
		entity.trainState = HeroTrainStateEnum.Train_NoSave.value();

		// 更新实体
		updateEntity(entity);
		return waitPatienceTrainList;
	}

	/**
	 * 保存训练
	 * 
	 * @param heroKeyId
	 */
	public void saveTrain(int heroKeyId) {
		HeroEntity entity = getEntity(heroKeyId);

		entity.setPatienceTrainList(entity.getWaitPatienceTrainList());
		entity.trainState = HeroTrainStateEnum.Train_Save.value();

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 
	 * @param heroKeyId
	 * @param quality
	 */
	public void modifyQuality(int heroKeyId, int quality) {
		HeroEntity entity = getEntity(heroKeyId);
		entity.quality = quality;

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 取消训练
	 * 
	 * @param heroKeyId
	 */
	public void cancelTrain(int heroKeyId) {
		HeroEntity entity = getEntity(heroKeyId);
		entity.trainState = HeroTrainStateEnum.Train_Save.value();

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 进化
	 * 
	 * @param heroKeyId
	 */
	public void evolution(int heroKeyId, int nextHeroGradeId) {
		HeroEntity entity = getEntity(heroKeyId);

		HeroGradePrototype herogradePrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(HeroGradePrototype.class, nextHeroGradeId);
		// 升级
		entity.quality = herogradePrototype.getQuality();
		entity.starLevel = herogradePrototype.getStartLevel();

		// 更新实体
		updateEntity(entity);
	}

	public int getHeroGradeKeyId(int heroKeyId) {
		HeroEntity entity = getEntity(heroKeyId);
		return getHeroGradeKeyId(entity);
	}

	private int getHeroGradeKeyId(HeroEntity entity) {
		return entity.heroId + entity.quality * 10 + entity.starLevel;
	}

	/**
	 * 创建
	 * 
	 * @param userId
	 * @param heroKeyId
	 * @param quality
	 * @param player
	 * @return
	 */
	public HeroEntity createHero(long userId, int heroKeyId, int quality) {
		// 创建英雄表
		HeroEntity entity = new HeroEntity();
		entity.userId = userId;
		entity.heroId = heroKeyId;
		entity.experience = 0;
		entity.level = 1;
		entity.quality = quality;
		entity.starLevel = 1;

		entity.trainState = HeroTrainStateEnum.Train_Save.value();
		entity.trainType = 1;

		int size = ElementEnum.values().length;
		List<Integer> patienceTrainList = new ArrayList<Integer>(size);
		List<Integer> waitPatienceTrainList = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			waitPatienceTrainList.add(0);
			patienceTrainList.add(0);
		}
		entity.setWaitPatienceTrainList(waitPatienceTrainList);
		entity.setPatienceTrainList(patienceTrainList);

		// 初始化英雄武器
		EquipmentDataFunction function = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);
		function.createEquipment(userId, heroKeyId);

		entityMap.put(entity.heroId, entity);
		insertEntity(entity);
		return entity;
	}

	private int getTrainValue(int patience, HeroTrainPrototype heroTrainPrototype) {
		int value = 0;
		if (patience <= heroTrainPrototype.getScale()) {
			value = patience + RandomUtil.generalRrandom(heroTrainPrototype.getMaxraise());
		} else {
			int random = RandomUtil.generalRrandom(patience + heroTrainPrototype.getMaxraise());
			value = random - patience;
			int maxreduce = heroTrainPrototype.getMaxreduce();
			if (value < maxreduce) {
				value = RandomUtil.regionRandom(maxreduce - heroTrainPrototype.getRandom(), maxreduce + heroTrainPrototype.getRandom());
			}
			value += patience;
		}
		return value;
	}

	public HeroInfoBean.Builder createHeroInfoBean(int heroKeyId) {
		HeroEntity entity = getEntity(heroKeyId);

		HeroInfoBean.Builder b = HeroInfoBean.newBuilder();
		b.setHeroId(entity.heroId);
		b.setLevel(entity.level);
		b.setQuality(entity.quality);
		b.setStar(entity.starLevel);
		return b;
	}

	private void updateEntity(HeroEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(HeroEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}

}
