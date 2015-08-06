package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.GetRoomInfoUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取指定房间详细信息
 * 
 * @author tzy
 * 
 */
public class GetRoomInfoUpHandler extends IGameHandler<GetRoomInfoUp> {
	@Override
	public void loginedHandler(GetRoomInfoUp message, IPlayer player) {
		int roomId = message.getRid();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		RoomInfo info = prx.findRoom(roomId);
		if (info == null) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		player.getLogicContext().getRoomManager().sendGetRoomInfoDownMessage(player.getSession(), info);
	}
}
