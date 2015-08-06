package com.cwa.server.logic.module.room.event;

import java.util.List;

import serverice.room.MatchRoomOutTimeEvent;
import baseice.event.IEvent;

import com.cwa.message.RoomMessage.MatchupOutTimeDown;
import com.cwa.server.logic.module.AEvent2MessageHandler;

/**
 * 匹配超时事件
 * 
 * @author tzy
 *
 */
public class MatchRoomOutTimeEventHandler extends AEvent2MessageHandler {
	@Override
	public void eventHandler(IEvent event) {
		MatchRoomOutTimeEvent e = (MatchRoomOutTimeEvent) event;
		List<Long> targetIds = e.uids;
		MatchupOutTimeDown.Builder b = MatchupOutTimeDown.newBuilder();
		// 发送消息
		sendMsg(b.build(), targetIds);
	}
}
