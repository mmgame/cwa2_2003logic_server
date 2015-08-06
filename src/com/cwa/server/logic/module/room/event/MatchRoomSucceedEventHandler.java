package com.cwa.server.logic.module.room.event;

import java.util.List;

import serverice.room.MatchRoomSucceedEvent;
import baseice.event.IEvent;

import com.cwa.message.RoomMessage.BattleInfoBean;
import com.cwa.message.RoomMessage.MatchupSuccessDown;
import com.cwa.server.logic.module.AEvent2MessageHandler;

/**
 * 匹配成功
 * 
 * @author tzy
 * 
 */
public class MatchRoomSucceedEventHandler extends AEvent2MessageHandler {
	@Override
	public void eventHandler(IEvent event) {
		MatchRoomSucceedEvent e = (MatchRoomSucceedEvent) event;
		List<Long> targetIds = e.uids;
		MatchupSuccessDown.Builder b = MatchupSuccessDown.newBuilder();

		BattleInfoBean.Builder bean = BattleInfoBean.newBuilder();
		bean.setIp(e.ip);
		bean.setPort(e.port);
		bean.setBattleId(e.battleId);
		bean.setAttackRoomid(e.attRid);
		bean.setDefineRoomid(e.deRid);
		b.setBattleInfoBean(bean.build());
		// 发送消息
		sendMsg(b.build(), targetIds);
	}
}
