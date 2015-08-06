package com.cwa.server.logic.module.room.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import serverice.room.CurrentBattleInfo;
import serverice.room.FightList;
import serverice.room.IRoomServicePrx;
import serverice.room.RoomInfo;
import serverice.room.RoomStateEnum;
import serverice.room.UserStateEnum;
import baseice.basedao.IEntity;

import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.BattleInfoBean;
import com.cwa.message.RoomMessage.GetUserRoomUp;
import com.cwa.message.RoomMessage.RoomDetailInfoBean;
import com.cwa.message.RoomMessage.RoomUserInfoBean;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

public class GetUserRoomUpHandler extends IGameHandler<GetUserRoomUp> {

	@Override
	public void loginedHandler(GetUserRoomUp message, IPlayer player) {
		long userId = player.getUserId();
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		int roomId = prx.getRoomByUser(userId);
		if (roomId == 0) {
			player.getLogicContext().getRoomManager().sendGetRoomUserDownMessage(player.getSession(), roomId, null);
			return;
		}
		RoomInfo roominfo = prx.findRoom(roomId);
		if (roominfo == null) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		RoomDetailInfoBean.Builder roomDetailInfoBean = RoomDetailInfoBean.newBuilder();
		roomDetailInfoBean.setRoomId(roominfo.rid);
		roomDetailInfoBean.setMasterId(String.valueOf(roominfo.masterId));
		int roomState = roominfo.roomState.value();
		roomDetailInfoBean.setState(roomState);
		if (roomState == RoomStateEnum.Fighting.value()) {
			CurrentBattleInfo info = roominfo.battleInfo;
			BattleInfoBean.Builder bean = BattleInfoBean.newBuilder();
			bean.setIp(info.ip);
			bean.setPort(info.port);
			bean.setBattleId(info.battleId);
			bean.setAttackRoomid(info.attRoom);
			bean.setDefineRoomid(info.defRoom);
			roomDetailInfoBean.setBattleInfoBean(bean.build());
		} else {
			Map<Long, FightList> fightListMap = roominfo.fightMap;
			Map<Long, UserStateEnum> userStateMap = roominfo.stateMap;
			Iterator<Entry<Long, FightList>> it = fightListMap.entrySet().iterator();
			List<RoomUserInfoBean> roomUserBeanList = new ArrayList<RoomUserInfoBean>();
			for (; it.hasNext();) {
				Entry<Long, FightList> entry = it.next();
				Long id = entry.getKey();
				RoomUserInfoBean.Builder bean = RoomUserInfoBean.newBuilder();
				bean.setUserId(String.valueOf(id));
				bean.setState(userStateMap.get(id).value());
				for (IEntity entity : entry.getValue().heroIds) {
					HeroEntity e = (HeroEntity) entity;
					if(e != null){
						bean.addHeroId(e.heroId);
					}
				}
				roomUserBeanList.add(bean.build());
			}
			roomDetailInfoBean.addAllRoomUserInfoBean(roomUserBeanList);
		}
		player.getLogicContext().getRoomManager().sendGetRoomUserDownMessage(player.getSession(), roomId, roomDetailInfoBean.build());
	}

}
