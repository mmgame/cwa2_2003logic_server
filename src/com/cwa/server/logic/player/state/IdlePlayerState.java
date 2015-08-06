package com.cwa.server.logic.player.state;

import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.player.IPlayerState;
import com.cwa.server.logic.player.PlayerStateTypeEnum;

/**
 * 空闲状态
 * 
 * @author mausmars
 *
 */
public class IdlePlayerState implements IPlayerState {

	@Override
	public int getType() {
		return PlayerStateTypeEnum.PS_Idle.value();
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
