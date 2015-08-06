package com.cwa.server.logic.dataFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IMatchShopEntityDao;
import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.prototype.MatchGoodsPrototype;
import com.cwa.prototype.MatchShopPrototype;
import com.cwa.prototype.gameEnum.CDTypeEnum;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.RandomUtil;
import com.cwa.util.TimeUtil;

/**
 * 神秘商店数据封装
 * 
 * @author mausmars
 * 
 */
public class MatchShopDataFunction implements IDataFunction {
	// {商店类型：MatchShopEntity}
	private Map<Integer, MatchShopEntity> entityMap = new HashMap<Integer, MatchShopEntity>();

	private IMatchShopEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public MatchShopDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IMatchShopEntityDao) dbSession.getEntityDao(MatchShopEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<MatchShopEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (MatchShopEntity e : entitys) {
			entityMap.put(e.shopType, e);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity(IPlayer player, int shopId, int shopType) {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_SHOP, player.getLogicContext());
		if (entityMap.containsKey(shopType)) {
			MatchShopEntity entity = entityMap.get(shopType);
			entity.shopId = shopId;
			entity.creatTime = cdHandler.resetcd(player, CDTypeEnum.CD_SHOP.value());
			updateEntity(entity);
		} else {
			MatchShopEntity entity = new MatchShopEntity();
			entity.userId = player.getUserId();
			entity.shopType = shopType;
			entity.shopId = shopId;
			entity.creatTime = cdHandler.resetcd(player, CDTypeEnum.CD_SHOP.value());
			entity.resetTime = TimeUtil.currentSystemTime();
			entity.refreshCount = 0;
			entity.setGoodsIndexList(new ArrayList<Integer>());
			entityMap.put(shopType, entity);

			// 插入实体
			insertEntity(entity);
		}

	}

	public MatchShopEntity getEntity(int shopType) {
		return entityMap.get(shopType);
	}

	public Collection<MatchShopEntity> getAllEntity() {
		return entityMap.values();
	}

	// 检测是否存在该商店
	public boolean contains(int shopType) {
		if (!entityMap.containsKey(shopType)) {
			return false;
		}
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_SHOP, player.getLogicContext());
		MatchShopEntity entity = entityMap.get(shopType);
		boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_SHOP.value(), entity.resetTime);
		if (isFinishedCD) {
			return false;
		}

		return true;
	}

	// 检测是否可以创建商店
	public boolean checkNewShop(int shopType) {
		if (!entityMap.containsKey(shopType)) {
			return true;
		}
		MatchShopEntity entity = entityMap.get(shopType);
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_REFRESH_SHOP, player.getLogicContext());
		boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_REFRESH_SHOP.value(), entity.resetTime);
		if (isFinishedCD) {
			return true;
		}
		return false;

	}

	// 获取隐藏关卡剩余时间
	public long getExistTime(int shopType) {
		MatchShopEntity entity = entityMap.get(shopType);
		long t = entity.resetTime - TimeUtil.currentSystemTime();
		return t > 0 ? t : 0l;
	}

	/**
	 * 刷新商店次数cd
	 */
	public void cdRefreshShopReset() {
		ILogicContext logicContext = player.getLogicContext();
		IGameCdHandler cdHandler = logicContext.getCdManager().getGameCd(CDTypeEnum.CD_BATTLE, player.getLogicContext());
		for (MatchShopEntity entity : entityMap.values()) {
			boolean isFinishedCD = cdHandler.isFinishedCd(player, CDTypeEnum.CD_BATTLE.value(), entity.resetTime);
			if (isFinishedCD) {
				entity.refreshCount = 0;
				// 重置
				if (logicContext.getCdManager().isSaveDb(CDTypeEnum.CD_BATTLE, logicContext)) {
					entity.resetTime = TimeUtil.currentSystemTime();
				}
				updateEntity(entity);
			}
		}

	}

	// 检测是否出售该商品
	public boolean isSale(int shopType, int shelfId) {
		if (!entityMap.containsKey(shopType)) {
			return false;
		}
		MatchShopEntity entity = entityMap.get(shopType);

		List<Integer> goodsIndexList = entity.getGoodsIndexList();
		if (shelfId > goodsIndexList.size() || goodsIndexList.get(shelfId - 1) == -1) {
			return false;
		}
		return true;
	}

	/**
	 * 购买商品
	 * 
	 * @param shopType
	 * @param shelfId
	 */
	public boolean buyGoods(int shopType, int shelfId) {
		MatchShopEntity entity = entityMap.get(shopType);
		entity.getGoodsIndexList().set(shelfId - 1, -1);
		List<Integer> l = entity.getGoodsIndexList();
		updateEntity(entity);
		for (Integer integer : l) {
			if (integer != -1) {
				return false;
			}
		}
		return true;
	}

	// 刷新商店商品
	public List<Integer> refreshShop(int shopId, boolean buy) {
		MatchShopPrototype matchShopPrototype = player.getLogicContext().getprototypeManager()
				.getPrototype(MatchShopPrototype.class, shopId);
		int shopType = matchShopPrototype.getType();
		MatchShopEntity entity = entityMap.get(shopType);
		List<Integer> goodsList = new ArrayList<Integer>();
		List<Integer> shelfList = matchShopPrototype.getGoodList();
		for (Integer integer : shelfList) {
			MatchGoodsPrototype goodsPrototype = player.getLogicContext().getprototypeManager()
					.getPrototype(MatchGoodsPrototype.class, integer);
			int index = RandomUtil.getRatioRandom(goodsPrototype.getRatiosList());
			if (index <= -1) {
				continue;
			}
			goodsList.add(index);
		}
		entity.setGoodsIndexList(goodsList);
		if (buy) {
			entity.refreshCount += 1;
		}
		updateEntity(entity);
		return goodsList;

	}

	// 获取刷新次数
	public int getRefreshCount(int shopType) {
		MatchShopEntity entity = entityMap.get(shopType);
		return entity.refreshCount;
	}

	private void updateEntity(MatchShopEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(MatchShopEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
