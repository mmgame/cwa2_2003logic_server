package com.cwa.server.logic.module.room.handler;

import serverice.room.IRoomServicePrx;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.CancelReadyUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 取消准备
 * 
 * @author tzy
 * 
 */
public class CancelReadyUpHandler extends IGameHandler<CancelReadyUp> {
	@Override
	public void loginedHandler(CancelReadyUp message, IPlayer player) {
		long userId = player.getUserId();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		boolean isSusscess = prx.cancelReady(userId);
		if (!isSusscess) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
	}
}
