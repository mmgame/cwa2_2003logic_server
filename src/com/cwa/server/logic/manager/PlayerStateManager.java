package com.cwa.server.logic.manager;

import java.util.Map;

import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.player.IPlayerState;

/**
 * player状态机
 * 
 * @author mausmars
 * 
 */
public class PlayerStateManager implements IPlayerStateManager {
	private Map<Integer, IPlayerState> defaultStateMap;
	// 战场
	protected IPlayer player;
	// 战场状态
	protected IPlayerState currentState;

	// 改变状态
	public boolean changeState(int state) {
		IPlayerState nextState = defaultStateMap.get(state);
		if (nextState == null) {
			return false;
		}
		currentState.exit(player);
		currentState = nextState;
		currentState.enter(player);
		return true;
	}

	public IPlayer getPlayer() {
		return player;
	}

	public IPlayerState getCurrentState() {
		return currentState;
	}

	// ------------------------------
	public void setDefaultStateMap(Map<Integer, IPlayerState> defaultStateMap) {
		this.defaultStateMap = defaultStateMap;
	}

	public void setPlayer(IPlayer player) {
		this.player = player;
	}

	public void setCurrentState(IPlayerState currentState) {
		this.currentState = currentState;
	}
}
