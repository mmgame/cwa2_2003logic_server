package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.ExitRoomUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 退出房间
 * 
 * @author tzy
 * 
 */
public class ExitRoomUpHandler extends IGameHandler<ExitRoomUp> {
	@Override
	public void loginedHandler(ExitRoomUp message, IPlayer player) {
		long userId = player.getUserId();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		boolean isSussecss = prx.exitRoom(userId);
		if (!isSussecss) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
	}
}
