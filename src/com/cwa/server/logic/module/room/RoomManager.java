package com.cwa.server.logic.module.room;

import java.util.ArrayList;
import java.util.List;

import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;
import baseice.service.FunctionTypeEnum;

import com.cwa.ISession;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.message.RoomMessage.CreateRoomDown;
import com.cwa.message.RoomMessage.GetRoomInfoDown;
import com.cwa.message.RoomMessage.GetRoomsDown;
import com.cwa.message.RoomMessage.GetUserRoomDown;
import com.cwa.message.RoomMessage.JoinRoomDown;
import com.cwa.message.RoomMessage.RoomBaseInfoBean;
import com.cwa.message.RoomMessage.RoomDetailInfoBean;
import com.cwa.server.logic.context.ILogicContext;

public class RoomManager {

	public IRoomServicePrx getRoomPrx(ILogicContext logicContext) {
		IFunctionService functionService=logicContext.getFunctionService();
		IRoomServicePrx	iRoomServicePrx = (IRoomServicePrx) functionService.getIcePrx(logicContext.getGid(), FunctionTypeEnum.Room, IRoomServicePrx.class);
		return iRoomServicePrx;
	}

	public void sendCreatRoomDownMessage(ISession session, RoomInfo roomInfo) {
		CreateRoomDown.Builder b = CreateRoomDown.newBuilder();
		b.setRoomId(roomInfo.rid);
		session.send(b.build());
	}

	public void sendGetRoomsDownMessage(ISession session, List<RoomInfo> roominfoList) {
		GetRoomsDown.Builder b = GetRoomsDown.newBuilder();
		List<RoomBaseInfoBean> roominfoBeanList = new ArrayList<RoomBaseInfoBean>();
		for (RoomInfo roomInfo : roominfoList) {
			RoomBaseInfoBean.Builder bean = RoomBaseInfoBean.newBuilder();
			bean.setRoomId(roomInfo.rid);
			roominfoBeanList.add(bean.build());
		}
		b.addAllRoomBaseInfoBean(roominfoBeanList);
		session.send(b.build());
	}

	public void sendGetRoomInfoDownMessage(ISession session, RoomInfo roominfo) {
		GetRoomInfoDown.Builder b = GetRoomInfoDown.newBuilder();
		RoomDetailInfoBean.Builder bean = RoomDetailInfoBean.newBuilder();
		bean.setRoomId(roominfo.rid);
		bean.setMasterId(String.valueOf(roominfo.masterId));
		b.setRoomDetailInfoBean(bean.build());
		session.send(b.build());
	}
	
	public void sendJoinRoomDownMessage(ISession session, Boolean isSusscess,RoomInfo roominfo) {
		JoinRoomDown.Builder b = JoinRoomDown.newBuilder();
		b.setIsSuccess(isSusscess);
		if (isSusscess) {
			RoomDetailInfoBean.Builder bean = RoomDetailInfoBean.newBuilder();
			bean.setRoomId(roominfo.rid);
			bean.setMasterId(String.valueOf(roominfo.masterId));
			b.setRoomDetailInfoBean(bean.build());
		}
		session.send(b.build());
	}
	
	public void sendGetRoomUserDownMessage(ISession session,int roomId,RoomDetailInfoBean roomDetailInfoBean) {
		GetUserRoomDown.Builder b = GetUserRoomDown.newBuilder();
		if (roomId==0) {
			b.setIsRoom(false);
			session.send(b.build());
			return;
		}
		b.setIsRoom(true);
		b.setRoomDetailInfoBean(roomDetailInfoBean);
		session.send(b.build());
	}

	// --------------------------------------------------------
	public static void main(String[] args) {

	}
}
