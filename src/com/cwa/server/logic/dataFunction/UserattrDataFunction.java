package com.cwa.server.logic.dataFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.constant.GameConstant;
import com.cwa.data.entity.IUserattrEntityDao;
import com.cwa.data.entity.domain.UserattrEntity;
import com.cwa.prototype.PlugItemPrototype;
import com.cwa.prototype.gameEnum.UserAttrKeyEnum;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.util.FormulaUtil;
import com.cwa.util.GameUtil;
import com.cwa.util.RandomUtil;

/**
 * 用户附加数据封装
 * 
 * @author mausmars
 *
 */
public class UserattrDataFunction implements IDataFunction {
	// {类型：UserattrEntity}
	private Map<Integer, UserattrEntity> entityMap = new HashMap<Integer, UserattrEntity>();

	private IUserattrEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public UserattrDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IUserattrEntityDao) dbSession.getEntityDao(UserattrEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<UserattrEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (UserattrEntity e : entitys) {
			entityMap.put(e.attrType, e);
		}
		for (UserAttrKeyEnum userAttrKeyEnum : UserAttrKeyEnum.values()) {
			if (!entityMap.containsKey(userAttrKeyEnum.value())) {
				newCreateEntity(userAttrKeyEnum);
			}
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity(UserAttrKeyEnum userAttrKeyEnum) {
		UserattrEntity entity = new UserattrEntity();
		entity.attrType = userAttrKeyEnum.value();
		entity.attrValue = 0;
		entity.userId = player.getUserId();
		entityMap.put(entity.attrType, entity);

		// 插入实体
		insertEntity(entity);
	}

	public UserattrEntity getEntity(int attrType) {
		return entityMap.get(attrType);
	}

	public UserattrEntity getEntity(UserAttrKeyEnum attrType) {
		return getEntity(attrType.value());
	}

	/**
	 * lucky属性修改
	 * 
	 * @param pluginId
	 * @return
	 */
	public boolean luckyAttr(int pluginId) {
		UserattrEntity entity = getEntity(UserAttrKeyEnum.Lucky_Type);

		PlugItemPrototype plugItemPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(PlugItemPrototype.class, pluginId);

		boolean isSuccess = false;
		int lucky = (int) entity.attrValue;
		int luckyMax = plugItemPrototype.getLuckyMax();
		if (lucky >= plugItemPrototype.getLuckyMax()) {
			isSuccess = true;
		} else {
			int success = RandomUtil.getSuccessRandom(FormulaUtil.getRefinePlugRate(plugItemPrototype.getUpdataSuccess(), lucky, luckyMax),
					GameConstant.PERCENT);
			if (success == 1) {
				isSuccess = true;
			}
		}
		if (isSuccess) {
			lucky = lucky - luckyMax;
			entity.attrValue = lucky > 0 ? lucky : 0;
		} else {
			lucky = lucky + plugItemPrototype.getLuckyAdd();
			entity.attrValue = lucky > GameConstant.LUCKY_MAX ? GameConstant.LUCKY_MAX : lucky;
		}
		// 更新实体
		updateEntity(entity);
		return isSuccess;
	}

	/**
	 * 改变英雄经验
	 * 
	 * @param power
	 */
	public void changeHeroExp(int changeValue) {
		UserattrEntity entity = getEntity(UserAttrKeyEnum.Hero_Exp);
		int currentExp = (int) entity.attrValue;
		entity.attrValue = GameUtil.getAmongValue(0, currentExp + changeValue, GameConstant.HERO_EXP_MAX);
		// 更新实体
		updateEntity(entity);
	}

	private void updateEntity(UserattrEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(UserattrEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
