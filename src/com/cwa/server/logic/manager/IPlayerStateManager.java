package com.cwa.server.logic.manager;

import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.player.IPlayerState;

/**
 * player状态管理
 * 
 * @author mausmars
 * 
 */
public interface IPlayerStateManager {
	/**
	 * 改变状态
	 * 
	 * @param state
	 * @return
	 */
	boolean changeState(int state);

	/**
	 * 对应的Player
	 * 
	 * @return
	 */
	IPlayer getPlayer();

	/**
	 * 当前状态
	 * 
	 * @return
	 */
	IPlayerState getCurrentState();
}
