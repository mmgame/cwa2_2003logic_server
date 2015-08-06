package com.cwa.server.logic.player.state;

import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.player.IPlayerState;
import com.cwa.server.logic.player.PlayerStateTypeEnum;

/**
 * 房间状态
 * 
 * @author mausmars
 *
 */
public class RoomPlayerState implements IPlayerState {
	@Override
	public int getType() {
		return PlayerStateTypeEnum.PS_Room.value();
	}

	@Override
	public void enter(IPlayer player) {

	}

	@Override
	public void exit(IPlayer player) {

	}

	@Override
	public void update(IPlayer player) {

	}

}
