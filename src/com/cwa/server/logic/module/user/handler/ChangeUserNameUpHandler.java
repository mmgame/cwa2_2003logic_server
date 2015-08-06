package com.cwa.server.logic.module.user.handler;

import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.UserMessage.ChangeUserNameUp;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 修改用户名
 * 
 * @author tzy
 * 
 */
public class ChangeUserNameUpHandler extends IGameHandler<ChangeUserNameUp> {
	@Override
	public void loginedHandler(ChangeUserNameUp message, IPlayer player) {
		String name = message.getUserName();

		UserinfoDataFunction udFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);

		if (udFunction.checkName(name)) {
			// 名字长度不正确
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_UserName, message);
			return;
		}
		udFunction.changeName(name);
	}
}
