package com.cwa.server.logic.module.equipment.handler;

import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.EquipmentMessage.UpgradeEquipmentLevelUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.EquipmentPrototype;
import com.cwa.prototype.HeroPrototype;
import com.cwa.prototype.gameEnum.MoneyTypeEnum;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.util.FormulaUtil;

/**
 * 装备升级
 * 
 * @author tzy
 * 
 */
public class UpgradeEquipmentLevelUpHandler extends IGameHandler<UpgradeEquipmentLevelUp> {
	@Override
	public void loginedHandler(UpgradeEquipmentLevelUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();
		int position = message.getPosition();

		EquipmentDataFunction edFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);

		HeroPrototype heroPrototype = logicContext.getprototypeManager().getPrototype(HeroPrototype.class, heroKeyId);
		int equipmentKeyId = heroPrototype.getEquipmentList().get(position - 1);
		EquipmentPrototype equipmentPrototype = logicContext.getprototypeManager().getPrototype(EquipmentPrototype.class, equipmentKeyId);

		int level = edFunction.getLevel(heroKeyId, position);
		int goldValue = FormulaUtil.getUpdateNeeDGold(level, equipmentPrototype.getUpdateGoldRatio());

		if (!udFunction.checkMoneyCount(MoneyTypeEnum.Money_Gold, goldValue)) {
			// 货币不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}

		if (!hdFunction.checkHeroLevel(heroKeyId, level, false)) {
			// 等级不能超过英雄等级
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Equipment, message);
			return;
		}
		// 扣除货币
		udFunction.modifyMoney(MoneyTypeEnum.Money_Gold, -goldValue);
		// 升级
		edFunction.upgradeLevel(heroKeyId, position, level);
	}
}
