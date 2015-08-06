package com.cwa.server.logic.dataFunction;

import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.constant.GameConstant;
import com.cwa.data.entity.IMatchMopupEntityDao;
import com.cwa.data.entity.domain.MatchMopupEntity;
import com.cwa.prototype.gameEnum.CDTypeEnum;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.TimeUtil;

/**
 * 扫荡数据封装
 * 
 * @author mausmars
 * 
 */
public class MatchMopupDataFunction implements IDataFunction {
	private MatchMopupEntity entity;

	private IMatchMopupEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public MatchMopupDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IMatchMopupEntityDao) dbSession.getEntityDao(MatchMopupEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		entity = (MatchMopupEntity) dao.selectEntityByUserId(player.getUserId(), createParams());
		if (entity == null) {
			newCreateEntity();
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity() {
		entity = new MatchMopupEntity();
		entity.userId = player.getUserId();
		entity.mopupCount = 0;
		entity.resetTime = TimeUtil.currentSystemTime();
		entity.mopupCount = GameConstant.MOPUP_MAX_COUNT;
		// 插入实体
		insertEntity(entity);
	}

	public MatchMopupEntity getEntity() {
		return entity;
	}

	/**
	 * 扫荡次数cd检测
	 */
	public void cdMopupReset() {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_BATTLE, player.getLogicContext());
		boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_BATTLE.value(), entity.resetTime);
		if (isFinishedCD) {
			entity.mopupCount = 0;
			// 重置
			if (logicContext.getCdManager().isSaveDb(CDTypeEnum.CD_BATTLE, logicContext)) {
				entity.resetTime = TimeUtil.currentSystemTime();
			}
			updateEntity(entity);
		}
	}

	// 检测扫荡次数
	public boolean checkMopupCount(int count) {
		return entity.mopupCount + count <= GameConstant.MOPUP_MAX_COUNT;
	}

	// 检测扫荡次数
	public void mopup(int count) {
		entity.mopupCount += count;
		updateEntity(entity);
	}

	private void updateEntity(MatchMopupEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(MatchMopupEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
