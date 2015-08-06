package com.cwa.server.logic.dataFunction;

import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.constant.GameConstant;
import com.cwa.data.entity.IUserinfoEntityDao;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.prototype.GameCDPrototype;
import com.cwa.prototype.UserLevelPrototype;
import com.cwa.prototype.gameEnum.CDTypeEnum;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.GameUtil;
import com.cwa.util.StringUtil;
import com.cwa.util.TimeUtil;

/**
 * 用户数据封装
 * 
 * @author mausmars
 * 
 */
public class UserinfoDataFunction implements IDataFunction {
	private UserinfoEntity entity;

	private IUserinfoEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public UserinfoDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IUserinfoEntityDao) dbSession.getEntityDao(UserinfoEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		entity = dao.selectEntityByUserId(player.getUserId(), createParams());
		if (entity == null) {
			// 如果不存在就创建
			newCreateEntity();
			// 新创建的
			return true;
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	// 初始化用户信息
	public void newCreateEntity() {
		entity = new UserinfoEntity();
		entity.userId = player.getUserId();
		entity.level = 1;
		entity.name = "游客" + player.getUserId();
		entity.icon = 1;
		entity.eventState = "";
		entity.functionState = "";
		UserLevelPrototype userLevelPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(UserLevelPrototype.class, entity.level);
		entity.power = userLevelPrototype.getPowerMax();
		entity.power = 10000;
		entity.buyCount = 0;
		entity.resetTime = TimeUtil.currentSystemTime();
		// 插入实体
		insertEntity(entity);
	}

	public UserinfoEntity getEntity() {
		return entity;
	}

	/**
	 * 更改名字
	 */
	public void changeName(String name) {
		entity.name = name;
	}

	public boolean checkUserLevel(int expectLevel) {
		return entity.level >= expectLevel;
	}

	public boolean checkName(String name) {
		if (name == null || name.length() <= 0) {
			return false;
		}
		int length = StringUtil.getStringUTF8Leng(name);
		if (length > GameConstant.USERNAME_MAX_LENGHT) {
			return false;
		}
		return true;
	}

	public boolean checkPower(int expectPower) {
		return entity.power >= expectPower;
	}

	public boolean checkIcon(int conditiionKeyId, List<String> conditionParams) {
		if (conditiionKeyId == 0) {
			return false;
		}
		if (!player.getLogicContext().getCheckManager().check(conditiionKeyId, conditionParams, player)) {
			return false;
		}
		return true;
	}

	/**
	 * 用户升级
	 */
	public void upgradeLevel(int exp) {
		for (int i = entity.level; i < GameConstant.USER_LEVEL_MAX; i++) {
			UserLevelPrototype userLevelPrototype = player.getLogicContext().getprototypeManager()
					.getPrototype(UserLevelPrototype.class, entity.level);
			int requireExperience = userLevelPrototype.getExperience();// 需要的经验

			entity.experience += exp;
			if (entity.experience >= requireExperience) {
				// 如果当前经验够升到下一级
				if ((entity.level + 1) == GameConstant.USER_LEVEL_MAX) {
					entity.experience = 0;
				} else {
					entity.experience -= requireExperience;
				}
				entity.level += 1;

				// TODO 发送升级事件

				// TODO 日志升级
				continue;
			}
			break;
		}
		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 变更名字
	 * 
	 * @param name
	 */
	public void changeUser(String name) {
		entity.name = name;

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 更改客户端用到的一些信息
	 * 
	 * @param name
	 */
	public void changeState(String eventState, String functionState) {
		entity.eventState = eventState;
		entity.functionState = functionState;

		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 更改头像
	 * 
	 * @param name
	 */
	public void changeIcon(int icon) {
		entity.icon = icon;
	}

	public int getLevel() {
		return entity.level;
	}

	/**
	 * 恢复体力
	 */
	public void cdPowerReset() {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_POWER, player.getLogicContext());
		boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_POWER.value(), entity.resetTime);
		if (isFinishedCD) {
			// 恢复体力
			recoverPower();
		}
	}

	// 恢复体力
	private void recoverPower() {
		ILogicContext logicContext = player.getLogicContext();

		int count = 0;
		GameCDPrototype cdPrototype = logicContext.getprototypeManager().getPrototype(GameCDPrototype.class, CDTypeEnum.CD_POWER.value());
		int intervalNum = TimeUtil.getIntervalNum(cdPrototype.getCdTime(), cdPrototype.getStartTime());
		int resetlNum = TimeUtil.getIntervalNum(cdPrototype.getCdTime(), entity.resetTime, cdPrototype.getStartTime());
		count = intervalNum - resetlNum;

		UserLevelPrototype userLevelPrototype = logicContext.getprototypeManager().getPrototype(UserLevelPrototype.class, entity.level);

		if (entity.power < userLevelPrototype.getPowerMax()) {
			// 体力不足
			int availableCount = entity.power + GameConstant.POWER_COUNT * count;
			if (availableCount <= userLevelPrototype.getPowerMax()) {
				entity.power = availableCount;
			} else {
				entity.power = userLevelPrototype.getPowerMax();
			}
		}
		// 重置
		if (logicContext.getCdManager().isSaveDb(CDTypeEnum.CD_POWER, logicContext)) {
			entity.resetTime = TimeUtil.currentSystemTime();
		}
		// 更新实体
		updateEntity(entity);
	}

	/**
	 * 购买体力次数cd检测
	 */
	public void cdBuyPowerReset() {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_BUY_POWER, player.getLogicContext());
		boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_BUY_POWER.value(), entity.resetTime);
		if (isFinishedCD) {
			entity.buyCount = 0;
			// 重置
			if (logicContext.getCdManager().isSaveDb(CDTypeEnum.CD_BUY_POWER, logicContext)) {
				entity.resetTime = TimeUtil.currentSystemTime();
			}
			updateEntity(entity);
		}
	}

	/**
	 * 改变体力
	 * 
	 * @param power
	 */
	public void changePower(int power, boolean buy) {
		int currentPower = entity.power;
		entity.power = ((int) GameUtil.getAmongValue(0, currentPower + power, GameConstant.POWER_MAX));
		entity.buyCount += 1;
		if (buy) {
			entity.buyCount += 1;
		}
		// 更新实体
		updateEntity(entity);
	}

	private void updateEntity(UserinfoEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(UserinfoEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
