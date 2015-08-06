package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.QuickJoinRoomUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

public class QuickJoinRoomUpHandler extends IGameHandler<QuickJoinRoomUp> {

	@Override
	public void loginedHandler(QuickJoinRoomUp message, IPlayer player) {
		long userId = player.getUserId();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		RoomInfo info = prx.aKeyToJoin(userId);
		if (info == null) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
	}
}
