package com.cwa.server.logic.module.equipment.handler;

import java.util.List;

import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.EquipmentMessage.UpgradeEquipmentQualityUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.EquipmentGradePrototype;
import com.cwa.prototype.HeroPrototype;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 装备进阶
 * 
 * @author tzy
 * 
 */
public class UpgradeEquipmentQualityUpHandler extends IGameHandler<UpgradeEquipmentQualityUp> {
	@Override
	public void loginedHandler(UpgradeEquipmentQualityUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();
		int position = message.getPosition();

		EquipmentDataFunction edFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		HeroPrototype heroPrototype = logicContext.getprototypeManager().getPrototype(HeroPrototype.class, heroKeyId);
		int equipmentKeyId = heroPrototype.getEquipmentList().get(position - 1);

		if (!edFunction.checkEquipmentExist(heroKeyId, position)) {
			// 装备不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}
		int equipmentGradeKeyId = edFunction.getEquipmentGradeKeyId(equipmentKeyId, position);
		EquipmentGradePrototype equipmentGradePrototype = logicContext.getprototypeManager().getPrototype(EquipmentGradePrototype.class,
				equipmentGradeKeyId);
		// 下一个品质id
		int nextLevelId = equipmentGradePrototype.getNextLevel();
		if (nextLevelId == 0) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}
		List<Integer> itemIds = equipmentGradePrototype.getRequireItemList();
		List<Integer> itemCounts = equipmentGradePrototype.getRequireCountList();
		int moneyType = equipmentGradePrototype.getRequireMoneyId();
		int moneyValue = equipmentGradePrototype.getRequireMoneyCount();

		if (!idFunction.checkItemCount(itemIds, itemCounts)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}
		if (!udFunction.checkMoneyCount(moneyType, moneyValue)) {
			// 货币不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}
		int level = edFunction.getLevel(heroKeyId, position);
		if (level < equipmentGradePrototype.getLevelMax()) {
			// 装备等级不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}
		// 扣除道具
		idFunction.modifyItemCount(itemIds, itemCounts, false);
		// 扣除货币
		udFunction.modifyMoney(moneyType, -moneyValue);
		// 提品
		edFunction.upgradeQuality(heroKeyId, position, nextLevelId);
	}
}
