package com.cwa.server.logic.module.item.handler;

import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.ItemMessage.CombineItemUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.ItemCombinePrototype;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 道具合成
 * 
 * @author tzy
 * 
 */
public class CombineItemUpHandler extends IGameHandler<CombineItemUp> {

	@Override
	public void loginedHandler(CombineItemUp message, IPlayer player) {
		int itemKeyId = message.getItemId(); // 要合成的道具id
		int count = message.getCount();// 合成的数量

		if (count <= 0 || count > GameConstant.BUY_MAX_COUNT) {
			// 购买数量错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		ItemCombinePrototype itemCombinePrototype = logicContext.getprototypeManager().getPrototype(ItemCombinePrototype.class, itemKeyId);

		List<Integer> itemKeyIdList = itemCombinePrototype.getSpendGoodsList();
		List<Integer> itemCountList = itemCombinePrototype.getSpendCountList();

		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);
		if (!idFunction.checkItemCount(itemKeyIdList, itemCountList)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}

		// 检查货币
		int expectCount = itemCombinePrototype.getRequireMoneyCount() * count;
		int moneyType = itemCombinePrototype.getRequireMoneyId();

		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		if (!udFunction.checkMoneyCount(moneyType, expectCount)) {
			// 货币不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}

		// 扣除道具
		idFunction.modifyItemCount(itemKeyIdList, itemCountList, false);
		// 扣除货币
		udFunction.modifyMoney(moneyType, -expectCount);

		// 合成道具
		idFunction.modifyItemCount(itemKeyId, count);
	}

}
