package com.cwa.server.logic.module.equipment.handler;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.gameEnum.DischargePlugTypeEnum;
import com.cwa.message.EquipmentMessage.DischargePlugUp;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.gameEnum.MoneyTypeEnum;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 一键卸下所有插件
 * 
 * @author tzy
 * 
 */
public class DischargePlugUpHandler extends IGameHandler<DischargePlugUp> {
	@Override
	public void loginedHandler(DischargePlugUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();
		int poisonId = message.getPosition();
		int type = message.getType();

		EquipmentDataFunction edFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);

		if (!edFunction.checkEquipmentExist(heroKeyId, poisonId)) {
			// 装备不存在，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}
		if (type == DischargePlugTypeEnum.DischargePlug_ByItem.value()) {
			ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);
			if (!idFunction.checkItemCount(GameConstant.DISCHARGE_ITEM_ID, GameConstant.CONSUMEITEM_COUNT)) {
				// 道具不足，数据错误
				logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
				return;
			}
			// 扣除道具
			idFunction.modifyItemCount(GameConstant.DISCHARGE_ITEM_ID, -GameConstant.CONSUMEITEM_COUNT);
		} else if (type == DischargePlugTypeEnum.DischargePlug_ByMoney.value()) {
			UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
					UsereconomyEntity.class);
			if (!udFunction.checkMoneyCount(MoneyTypeEnum.Money_Diamond, GameConstant.DISCHARGE_DIAMOND_COUNT)) {
				// 货币不足，数据错误
				logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
				return;
			}
			// 扣除货币
			udFunction.modifyMoney(MoneyTypeEnum.Money_Diamond, -GameConstant.DISCHARGE_DIAMOND_COUNT);
		} else {
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_ConsumeType, message);
			return;
		}
		// 卸下插件
		edFunction.dischargePlug(heroKeyId, poisonId);
	}
}
