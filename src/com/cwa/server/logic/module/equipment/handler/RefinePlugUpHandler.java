package com.cwa.server.logic.module.equipment.handler;

import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.UserattrEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.EquipmentMessage.RefinePlugDown;
import com.cwa.message.EquipmentMessage.RefinePlugUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.PlugItemPrototype;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.UserattrDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 精炼插件
 * 
 * @author tzy
 * 
 */
public class RefinePlugUpHandler extends IGameHandler<RefinePlugUp> {
	@Override
	public void loginedHandler(RefinePlugUp message, IPlayer player) {
		int pluginId = message.getPlugId();

		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		UserattrDataFunction uadFunction = (UserattrDataFunction) player.getDataFunctionManager().getDataFunction(UserattrEntity.class);

		PlugItemPrototype plugItemPrototype = logicContext.getprototypeManager().getPrototype(PlugItemPrototype.class, pluginId);
		List<Integer> itemIds = plugItemPrototype.getRequireKeyIdsList();
		List<Integer> itemCounts = plugItemPrototype.getRequireCountList();
		int moneyType = plugItemPrototype.getRequireMoneyId();
		int expectCount = plugItemPrototype.getRequireMoneyCount();
		int nextId = plugItemPrototype.getUpdataKeyId();

		if (nextId == 0) {
			// 插件等级已经最高
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		// 如果需求物品中包含精炼物品本身需要考虑本身个数的需求
		int needCount = 1;
		for (int i = 0; i < itemIds.size(); i++) {
			if (pluginId == itemIds.get(i)) {
				needCount += itemCounts.get(i);
			}
		}
		if (!idFunction.checkItemCount(pluginId, needCount)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		if (!udFunction.checkMoneyCount(moneyType, expectCount)) {
			// 货币不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}
		// 扣除道具
		idFunction.modifyItemCount(itemIds, itemCounts, false);
		// 扣除货币
		udFunction.modifyMoney(moneyType, -expectCount);

		boolean isSuccess = uadFunction.luckyAttr(pluginId);
		if (isSuccess) {
			idFunction.modifyItemCount(pluginId, -GameConstant.CONSUMEITEM_COUNT);
			idFunction.modifyItemCount(nextId, GameConstant.CONSUMEITEM_COUNT);
		}
		sendRefinePlugDownMessage(isSuccess, pluginId, player);
	}

	/**
	 * 发送插件精炼
	 * 
	 * @param session
	 * @param isSuccess
	 */
	private void sendRefinePlugDownMessage(boolean isSuccess, int pluginId, IPlayer player) {
		RefinePlugDown.Builder b = RefinePlugDown.newBuilder();
		b.setSuccess(isSuccess);
		b.setPlugId(pluginId);
		player.getSession().send(b.build());
	}
}
