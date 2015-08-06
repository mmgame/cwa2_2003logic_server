package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.KickOutUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

public class KickOutUpHandler extends IGameHandler<KickOutUp> {

	@Override
	public void loginedHandler(KickOutUp message, IPlayer player) {
		long userId = player.getUserId();
		long kickoutId = Long.parseLong(message.getKickOutUserId());
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		boolean isSusscess = prx.kickOut(userId, kickoutId);
		if (!isSusscess) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
	}
}
