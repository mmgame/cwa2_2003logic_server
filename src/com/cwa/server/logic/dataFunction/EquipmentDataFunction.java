package com.cwa.server.logic.dataFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IEquipmentEntityDao;
import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.prototype.EquipmentGradePrototype;
import com.cwa.prototype.HeroPrototype;
import com.cwa.prototype.gameEnum.EquipmentPositionEnum;
import com.cwa.prototype.gameEnum.QualityEnum;
import com.cwa.server.logic.player.IPlayer;

/**
 * 装备数据封装
 * 
 * @author mausmars
 *
 */
public class EquipmentDataFunction implements IDataFunction {
	// {装备id：EquipmentEntity}
	private Map<Integer, EquipmentEntity> entityMap = new HashMap<Integer, EquipmentEntity>();

	private IEquipmentEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public EquipmentDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IEquipmentEntityDao) dbSession.getEntityDao(EquipmentEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<EquipmentEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (EquipmentEntity e : entitys) {
			entityMap.put(getEKey(e.heroId, e.positionId), e);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity() {

	}

	/**
	 * 验证英雄存在
	 * 
	 * @param heroKeyId
	 * @return
	 */
	public boolean checkEquipmentExist(int heroKeyId, int position) {
		return getEntity(heroKeyId, position) != null;
	}

	public EquipmentEntity getEntity(int heroKeyId, int position) {
		return entityMap.get(getEKey(heroKeyId, position));
	}

	private int getEKey(int heroKeyId, int position) {
		return heroKeyId * 100 + position;
	}

	public List<EquipmentEntity> getEntityByHeroId(int heroKeyId) {
		HeroPrototype heroPrototype = player.getLogicContext().getprototypeManager().getPrototype(HeroPrototype.class, heroKeyId);
		if (heroPrototype == null) {
			return null;
		}
		List<Integer> equipmentIds = heroPrototype.getEquipmentList();
		List<EquipmentEntity> entitys = new ArrayList<EquipmentEntity>(equipmentIds.size());
		for (int i = 1; i <= equipmentIds.size(); i++) {
			entitys.add(entityMap.get(getEKey(heroKeyId, i)));
		}
		return entitys;
	}

	/**
	 * 插槽是否可用
	 * 
	 * @param userId
	 * @param heroKeyId
	 * @param position
	 * @param quality
	 * @return
	 */
	public boolean checkSlotAvailable(int heroKeyId, int position, int quality) {
		EquipmentEntity entity = getEntity(heroKeyId, position);
		if (entity == null) {
			return false;
		}
		if (QualityEnum.getEnum(quality - 1) == null) {
			// 没有对应品质
			return false;
		}
		if (entity.equipmentQuality < quality) {
			// 未开启
			return false;
		}
		if (!entity.isEmpty(quality)) {
			// 不为空
			return false;
		}
		return true;
	}

	/**
	 * 卸下插件
	 */
	public void dischargePlug(int heroKeyId, int position) {
		EquipmentEntity entity = getEntity(heroKeyId, position);
		for (int i = 1; i <= entity.getPIds().length; i++) {
			entity.setPlugId(i, 0);
		}
		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 装备插件
	 */
	public void putonPlugin(int heroKeyId, int position, int quality, int pluginId) {
		EquipmentEntity entity = getEntity(heroKeyId, position);
		entity.setPlugId(quality, pluginId);

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 升级等级
	 */
	public void upgradeLevel(int heroKeyId, int position, int level) {
		EquipmentEntity entity = getEntity(heroKeyId, position);
		entity.equipmentLevel = level + 1;

		// 更新实体
		updateEntity(entity);
	}

	public int getLevel(int heroKeyId, int position) {
		EquipmentEntity entity = getEntity(heroKeyId, position);
		return entity.equipmentLevel;
	}

	/**
	 * 得到装备级别keyId（）
	 * 
	 * @param entity
	 * @return
	 */
	public int getEquipmentGradeKeyId(int equipmentKeyId, int quality) {
		return equipmentKeyId + quality;
	}

	/**
	 * 升级装备品质
	 */
	public void upgradeQuality(int heroKeyId, int position, int nextLevel) {
		EquipmentEntity entity = getEntity(heroKeyId, position);
		EquipmentGradePrototype equipmentGradePrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(EquipmentGradePrototype.class, nextLevel);
		entity.equipmentQuality = equipmentGradePrototype.getQuality();

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 创建英雄装备
	 */
	public void createEquipment(long userId, int heroKeyId) {
		for (EquipmentPositionEnum e : EquipmentPositionEnum.values()) {
			EquipmentEntity entity = new EquipmentEntity();
			entity.userId = userId;
			entity.heroId = heroKeyId;
			entity.positionId = e.value();
			entity.equipmentLevel = 1;
			entity.equipmentQuality = 1;
			entity.plugs = "";
			entityMap.put(getEKey(entity.heroId, entity.positionId), entity);

			insertEntity(entity);
		}
	}

	private void updateEntity(EquipmentEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(EquipmentEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}

}
