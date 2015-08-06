package com.cwa.server.logic.module.room.handler;

import java.util.ArrayList;
import java.util.List;

import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;

import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.GetRoomsUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取当前房间列表
 * 
 * @author tzy
 * 
 */
public class GetRoomsUpHandler extends IGameHandler<GetRoomsUp> {
	@Override
	public void loginedHandler(GetRoomsUp message, IPlayer player) {
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		List<RoomInfo> roominfoList = prx.getRoomList();
		if (roominfoList == null) {
			roominfoList = new ArrayList<RoomInfo>();
		}
		player.getLogicContext().getRoomManager().sendGetRoomsDownMessage(player.getSession(), roominfoList);
	}
}
