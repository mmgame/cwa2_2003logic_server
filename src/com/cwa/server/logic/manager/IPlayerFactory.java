package com.cwa.server.logic.manager;

import com.cwa.server.logic.player.IPlayer;

/**
 * player工厂
 * 
 * @author mausmars
 * 
 */
public interface IPlayerFactory {
	/**
	 * 创建player
	 * 
	 * @param id
	 * @param keyId
	 * @return
	 */
	IPlayer createPlayer(long userId, int rid, Object params);
}
