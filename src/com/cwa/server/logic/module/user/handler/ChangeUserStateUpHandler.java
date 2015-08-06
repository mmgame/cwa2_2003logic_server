package com.cwa.server.logic.module.user.handler;

import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.UserMessage.ChangeUserStateUp;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 更改用户状态
 * 
 * @author tzy
 * 
 */
public class ChangeUserStateUpHandler extends IGameHandler<ChangeUserStateUp> {
	@Override
	public void loginedHandler(ChangeUserStateUp message, IPlayer player) {
		String functionState = message.getFunctionState();
		String eventState = message.getEventState();

		UserinfoDataFunction udFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);

		udFunction.changeState(eventState, functionState);
	}
}
