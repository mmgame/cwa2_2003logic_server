package com.cwa.server.logic.dataFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.constant.GameConstant;
import com.cwa.data.entity.IItemEntityDao;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.prototype.BoxItemPrototype;
import com.cwa.prototype.BoxItemRatePrototype;
import com.cwa.prototype.GameInitPrototype;
import com.cwa.prototype.ItemPrototype;
import com.cwa.prototype.gameEnum.IdsTypeEnum;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.GameUtil;
import com.cwa.util.RandomUtil;

/**
 * 道具数据封装
 * 
 * @author mausmars
 *
 */
public class ItemDataFunction implements IDataFunction {
	// {道具keyid：ItemEntity}
	private Map<Integer, ItemEntity> entityMap = new HashMap<Integer, ItemEntity>();

	private IItemEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public ItemDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IItemEntityDao) dbSession.getEntityDao(ItemEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<ItemEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (ItemEntity e : entitys) {
			entityMap.put(e.itemId, e);
		}
		if (newRegister) {
			IPrototypeClientService prototypeService = player.getLogicContext().getprototypeManager();
			List<GameInitPrototype> gameInitPrototypeList = prototypeService.getAllPrototype(GameInitPrototype.class);
			for (GameInitPrototype gameInitPrototype : gameInitPrototypeList) {
				if (gameInitPrototype.getInitType() == IdsTypeEnum.Ids_Item.value()) {
					modifyItemCount(gameInitPrototype.getInitSubType(), gameInitPrototype.getInitParamList().get(0));
				}
			}
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity() {

	}

	public ItemEntity getEntity(int itemId) {
		return entityMap.get(itemId);
	}

	public Collection<ItemEntity> getAllEntity() {
		return entityMap.values();
	}

	/**
	 * 验证道具数量
	 * 
	 * @return
	 */
	public boolean checkItemCount(int itemKeyId, int expectCount) {
		ItemEntity entity = getEntity(itemKeyId);
		return entity != null && entity.count >= expectCount;
	}

	/**
	 * 验证道具数量
	 * 
	 * @return
	 */
	public boolean checkItemCount(List<Integer> itemKeyIds, List<Integer> expectCounts) {
		if (itemKeyIds.size() != expectCounts.size()) {
			return false;
		}
		Iterator<Integer> itemKeyIdIt = itemKeyIds.iterator();
		Iterator<Integer> expectCountIt = expectCounts.iterator();
		boolean isSuccess = true;
		for (; itemKeyIdIt.hasNext();) {
			ItemEntity entity = getEntity(itemKeyIdIt.next());
			if (entity == null || entity.count < expectCountIt.next()) {
				isSuccess = false;
				break;
			}
		}
		return isSuccess;
	}

	/**
	 * 修改道具数量
	 * 
	 * @param moneyType
	 * @param count
	 */
	public void modifyItemCount(int itemKeyId, int changerCount) {
		ItemPrototype itemPrototype = player.getLogicContext().getprototypeManager().getPrototype(ItemPrototype.class, itemKeyId);

		ItemEntity entity = getEntity(itemKeyId);
		if (entity == null) {
			entity = new ItemEntity();
			entity.itemId = itemKeyId;
			entity.userId = player.getUserId();
			entity.count = ((int) GameUtil.getAmongValue(0, changerCount, itemPrototype.getHoldMaxCount()));

			entityMap.put(entity.itemId, entity);

			// 插入实体
			insertEntity(entity);
		} else {
			int count = entity.count + changerCount;
			entity.count = ((int) GameUtil.getAmongValue(0, count, itemPrototype.getHoldMaxCount()));

			// 更新实体
			updateEntity(entity);
		}

		if (changerCount < 0) {
			// TODO 消耗日志
		} else {
			// TODO 获得日志
		}
	}

	public void modifyItemCount(List<Integer> itemKeyIds, List<Integer> expectCounts, boolean isIncrease) {
		Iterator<Integer> itemKeyIdIt = itemKeyIds.iterator();
		Iterator<Integer> expectCountIt = expectCounts.iterator();
		if (isIncrease) {
			for (; itemKeyIdIt.hasNext();) {
				modifyItemCount(itemKeyIdIt.next(), expectCountIt.next());
			}
		} else {
			for (; itemKeyIdIt.hasNext();) {
				modifyItemCount(itemKeyIdIt.next(), -expectCountIt.next());
			}
		}
	}

	/**
	 * 使用宝箱
	 */
	public List<Object> useChests(int itemKeyId) {
		List<Object> itemInfoList = new ArrayList<Object>();

		BoxItemPrototype boxItemPrototype = player.getLogicContext().getprototypeManager().getPrototype(BoxItemPrototype.class, itemKeyId);

		// 顺序随机
		int index = RandomUtil.getOrderRandom(boxItemPrototype.getCountRatiosList(),
				boxItemPrototype.getCountRatiosList().get(boxItemPrototype.getCountRatiosList().size() - 1));
		if (index <= -1) {
			return itemInfoList;
		}
		// 开出几种道具
		int count = boxItemPrototype.getCountList().get(index);

		List<Integer> itemBooksCatchList = new ArrayList<Integer>();
		List<Integer> itemBooksRatiosCatchList = new ArrayList<Integer>();
		itemBooksCatchList.addAll(boxItemPrototype.getItemBooksList());
		itemBooksRatiosCatchList.addAll(boxItemPrototype.getItemBooksRatiosList());

		for (int i = 0; i < count; i++) {
			// 随机大类道具
			if (itemBooksRatiosCatchList.get(0) == GameConstant.MUST_RATE) {
				index = GameConstant.MUST_RATE;
			} else {
				// 按比例随机
				index = RandomUtil.getRatioRandom(itemBooksRatiosCatchList);
				if (index <= -1) {
					return itemInfoList;
				}
			}
			itemBooksRatiosCatchList.remove(index);
			int itemBookId = itemBooksCatchList.remove(index);

			BoxItemRatePrototype itemBookRatePrototype = player.getLogicContext().getprototypeManager()
					.getPrototype(BoxItemRatePrototype.class, itemBookId);
			if (itemBookRatePrototype == null) {
				return itemInfoList;
			}

			// 随机道具和数量
			index = RandomUtil.getRatioRandom(itemBookRatePrototype.getItemRatiosList());
			if (index <= -1) {
				// 需修改
				return itemInfoList;
			}
			int goodsKeyId = itemBookRatePrototype.getItemKeyIdsList().get(index);
			int goodsCount = itemBookRatePrototype.getCountList().get(index);

			// 随机是否翻倍
			index = RandomUtil.getRatioRandom(itemBookRatePrototype.getCountRatiosList());
			if (index <= -1) {
				// 需修改
				return itemInfoList;
			}
			// 个数翻倍
			goodsCount = goodsCount * itemBookRatePrototype.getCountMultiply().get(index);

			int[] itemInfo = new int[2];
			itemInfo[0] = goodsKeyId;
			itemInfo[1] = goodsCount;

			itemInfoList.add(itemInfo);
		}
		return itemInfoList;
	}

	private void updateEntity(ItemEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(ItemEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
