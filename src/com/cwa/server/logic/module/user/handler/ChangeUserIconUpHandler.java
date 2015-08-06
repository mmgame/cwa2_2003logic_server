package com.cwa.server.logic.module.user.handler;

import java.util.List;

import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.UserMessage.ChangeUserIconUp;
import com.cwa.prototype.UserIconPrototype;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 修改用户头像
 * 
 * @author tzy
 * 
 */
public class ChangeUserIconUpHandler extends IGameHandler<ChangeUserIconUp> {
	@Override
	public void loginedHandler(ChangeUserIconUp message, IPlayer player) {
		int icon = message.getIcon();

		UserIconPrototype userIconPrototype = logicContext.getprototypeManager().getPrototype(UserIconPrototype.class, icon);
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);
		UserinfoDataFunction uidFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);

		int conditiionKeyId = userIconPrototype.getCondition();
		List<String> conditionParams = userIconPrototype.getConditionParamList();
		if (uidFunction.checkIcon(conditiionKeyId, conditionParams)) {
			// 头像不存在
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_IconType, message);
			return;
		}

		int moneyType = userIconPrototype.getRequireMoneyType();
		int expectCount = userIconPrototype.getRequireMoneyCount();
		if (!udFunction.checkMoneyCount(moneyType, expectCount)) {
			// 货币不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Economy, message);
			return;
		}
		// 扣除货币
		udFunction.modifyMoney(moneyType, -expectCount);

		// 变更头像
		uidFunction.changeIcon(icon);
	}
}
