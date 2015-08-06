package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.CreateRoomUp;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 创建房间
 * 
 * @author tzy
 * 
 */
public class CreateRoomUpHandler extends IGameHandler<CreateRoomUp> {
	@Override
	public void loginedHandler(CreateRoomUp message, IPlayer player) {
		long userId = player.getUserId();
		int battleKeyId = message.getBattleKeyId();

		// 获得上下文
		ILogicContext logicContext = player.getLogicContext();
		IRoomServicePrx prx = logicContext.getRoomManager().getRoomPrx(logicContext);
		if (prx == null) {
			// 房间服务未开启
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}

		RoomInfo roomInfo = prx.createRoom(userId, battleKeyId);
		if (roomInfo == null) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		logicContext.getRoomManager().sendCreatRoomDownMessage(player.getSession(), roomInfo);
	}
}
