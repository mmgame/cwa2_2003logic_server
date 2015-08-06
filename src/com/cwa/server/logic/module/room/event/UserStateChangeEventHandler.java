package com.cwa.server.logic.module.room.event;

import java.util.List;

import serverice.room.UserStateChangeEvent;
import baseice.event.IEvent;

import com.cwa.message.RoomMessage.UserStateChangeDown;
import com.cwa.server.logic.module.AEvent2MessageHandler;

/**
 * 用户行为状态改变
 * 
 * @author tzy
 *
 */
public class UserStateChangeEventHandler extends AEvent2MessageHandler {

	@Override
	public void eventHandler(IEvent event) {
		UserStateChangeEvent e = (UserStateChangeEvent) event;
		List<Long> targetIds = e.uids;
		UserStateChangeDown.Builder b = UserStateChangeDown.newBuilder();
		b.setUserId(String.valueOf(e.uInfo.uid));
		b.setState(e.uInfo.userAction.value());
		// 发送消息
		sendMsg(b.build(), targetIds);
	}
}
