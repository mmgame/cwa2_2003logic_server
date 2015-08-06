package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.JoinRoomUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 进入房间
 * 
 * @author tzy
 * 
 */
public class JoinRoomUpHandler extends IGameHandler<JoinRoomUp> {

	@Override
	public void loginedHandler(JoinRoomUp message, IPlayer player) {
		long userId = player.getUserId();
		int roomId = message.getRoomId();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		boolean isSusscess = prx.joinRoom(roomId, userId);
		if (!isSusscess) {
			player.getLogicContext().getRoomManager().sendJoinRoomDownMessage(player.getSession(), isSusscess, null);
			return;
		}
		RoomInfo info = prx.findRoom(roomId);
		if (info == null) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		player.getLogicContext().getRoomManager().sendJoinRoomDownMessage(player.getSession(), isSusscess, info);
	}

}
