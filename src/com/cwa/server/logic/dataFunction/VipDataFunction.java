package com.cwa.server.logic.dataFunction;

import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IVipEntityDao;
import com.cwa.data.entity.domain.VipEntity;
import com.cwa.prototype.VipFunctionPrototype;
import com.cwa.server.logic.player.IPlayer;

/**
 * vip数据封装
 * 
 * @author mausmars
 * 
 */
public class VipDataFunction implements IDataFunction {
	private VipEntity entity;

	private IVipEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public VipDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IVipEntityDao) dbSession.getEntityDao(VipEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		entity = dao.selectEntityByUserId(player.getUserId(), createParams());
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
		entity = new VipEntity();
		entity.userId = player.getUserId();
		entity.vipLevel = 0;
		entity.vipExp = 0;
		entity.rewardState = 0;

		// 插入实体
		insertEntity(entity);
	}

	public VipEntity getEntity() {
		return entity;
	}

	/**
	 * 购买次数是否足够
	 * 
	 * @param functionType
	 * @param currentCount
	 * @return
	 */
	public boolean isEnoughCount(VipFunctionPrototype vipFunctionPrototype, int currentCount) {
		List<Integer> buyCountList = vipFunctionPrototype.getVipBuynumList();
		int buyMaxCount = buyCountList.get(entity.vipLevel);
		return currentCount < buyMaxCount;
	}

	private void updateEntity(VipEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(VipEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
