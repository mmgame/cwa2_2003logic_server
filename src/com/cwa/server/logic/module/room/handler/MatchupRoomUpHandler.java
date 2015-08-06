package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.MatchupRoomUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 开始匹配（只有房主可以）
 * 
 * @author tzy
 * 
 */
public class MatchupRoomUpHandler extends IGameHandler<MatchupRoomUp> {

	@Override
	public void loginedHandler(MatchupRoomUp message, IPlayer player) {
		long userId = player.getUserId();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		boolean isSusscess = prx.matchupRoom(userId);
		if (!isSusscess) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
	}

}
