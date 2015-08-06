package com.cwa.server.logic.module.equipment.handler;

import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.message.EquipmentMessage.PutonPluginUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.EquipmentPrototype;
import com.cwa.prototype.HeroPrototype;
import com.cwa.prototype.PlugItemPrototype;
import com.cwa.prototype.SlotTypePrototype;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 装备插件
 * 
 * @author tzy
 * 
 */
public class PutonPluginUpHandler extends IGameHandler<PutonPluginUp> {
	@Override
	public void loginedHandler(PutonPluginUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();
		int position = message.getPosition();
		int quality = message.getSlotId();
		int pluginId = message.getPluginId();

		EquipmentDataFunction edFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		HeroPrototype heroPrototype = logicContext.getprototypeManager().getPrototype(HeroPrototype.class, heroKeyId);
		int equipmentKeyId = heroPrototype.getEquipmentList().get(position - 1);
		EquipmentPrototype equipmentPrototype = logicContext.getprototypeManager().getPrototype(EquipmentPrototype.class, equipmentKeyId);
		PlugItemPrototype plugItemPrototype = logicContext.getprototypeManager().getPrototype(PlugItemPrototype.class, pluginId);

		int slot = equipmentPrototype.getSlotTypeList().get(quality - 1);
		SlotTypePrototype slotTypePrototype = logicContext.getprototypeManager().getPrototype(SlotTypePrototype.class, slot);
		List<Integer> plugTypeList = slotTypePrototype.getPluginTypeList();
		if (!plugTypeList.contains(plugItemPrototype.getType())) {
			// 属性槽错误，属于用户 错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}
		if (!edFunction.checkSlotAvailable(heroKeyId, position, quality)) {
			// 装备插槽验证，属于数据错误类型
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}

		if (!idFunction.checkItemCount(pluginId, GameConstant.CONSUMEITEM_COUNT)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		// 装备插件
		edFunction.putonPlugin(heroKeyId, position, quality, pluginId);
	}
}
