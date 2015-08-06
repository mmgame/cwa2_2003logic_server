package com.cwa.server.logic.dataFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.constant.GameConstant;
import com.cwa.data.entity.IUsereconomyEntityDao;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.prototype.GameInitPrototype;
import com.cwa.prototype.gameEnum.IdsTypeEnum;
import com.cwa.prototype.gameEnum.MoneyTypeEnum;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.GameUtil;

/**
 * 经济数据封装
 * 
 * @author mausmars
 *
 */
public class UsereconomyDataFunction implements IDataFunction {
	// {货币类型：UsereconomyEntity}
	private Map<Integer, UsereconomyEntity> entityMap = new HashMap<Integer, UsereconomyEntity>();

	private IUsereconomyEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public UsereconomyDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IUsereconomyEntityDao) dbSession.getEntityDao(UsereconomyEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<UsereconomyEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (UsereconomyEntity e : entitys) {
			entityMap.put(e.moneyType, e);
		}

		for (MoneyTypeEnum moneyTypeEnum : MoneyTypeEnum.values()) {
			if (!entityMap.containsKey(moneyTypeEnum.value())) {
				newCreateEntity(moneyTypeEnum);

				IPrototypeClientService prototypeService = player.getLogicContext().getprototypeManager();
				List<GameInitPrototype> gameInitPrototypeList = prototypeService.getAllPrototype(GameInitPrototype.class);
				for (GameInitPrototype gameInitPrototype : gameInitPrototypeList) {
					if (gameInitPrototype.getInitType() == IdsTypeEnum.Ids_Money.value()
							&& gameInitPrototype.getInitSubType() == moneyTypeEnum.value()) {
						modifyMoney(gameInitPrototype.getInitSubType(), gameInitPrototype.getInitParamList().get(0));
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	private void newCreateEntity(MoneyTypeEnum moneyTypeEnum) {
		UsereconomyEntity entity = new UsereconomyEntity();
		entity.userId = player.getUserId();
		entity.moneyType = moneyTypeEnum.value();
		entity.moneyCount = 0;
		entityMap.put(entity.moneyType, entity);

		// 插入实体
		insertEntity(entity);
	}

	public UsereconomyEntity getEntity(int moneyType) {
		return entityMap.get(moneyType);
	}

	public Collection<UsereconomyEntity> getAllEntity() {
		return entityMap.values();
	}

	/**
	 * 验证货币数量
	 * 
	 * @param moneyType
	 * @param expectCount
	 * @return
	 */
	public boolean checkMoneyCount(int moneyType, int expectCount) {
		UsereconomyEntity entity = getEntity(moneyType);
		return entity != null && entity.moneyCount >= expectCount;
	}

	public boolean checkMoneyCount(MoneyTypeEnum moneyType, int expectCount) {
		return checkMoneyCount(moneyType.value(), expectCount);
	}

	/**
	 * 修改货币数量
	 * 
	 * @param moneyType
	 * @param changValue
	 */
	public void modifyMoney(int moneyType, int changValue) {
		UsereconomyEntity entity = getEntity(moneyType);
		long value = entity.moneyCount + changValue;
		entity.moneyCount = ((int) GameUtil.getAmongValue(0, value, GameConstant.GOLD_MAX));

		// 更新实体
		updateEntity(entity);

		if (changValue < 0) {

		} else {

		}
	}

	public void modifyMoney(MoneyTypeEnum moneyType, int count) {
		modifyMoney(moneyType.value(), count);
	}

	private void updateEntity(UsereconomyEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(UsereconomyEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
