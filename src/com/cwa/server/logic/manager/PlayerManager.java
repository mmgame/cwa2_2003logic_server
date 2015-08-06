package com.cwa.server.logic.manager;

import java.util.concurrent.ConcurrentHashMap;

import com.cwa.server.logic.player.IPlayer;

/**
 * 用户管理类
 * 
 * @author tzy
 * 
 */
public class PlayerManager implements IPlayerManager {
	// {用户id : player}
	private ConcurrentHashMap<Long, IPlayer> playerMap = new ConcurrentHashMap<Long, IPlayer>();

	@Override
	public void insert(IPlayer player) {
		playerMap.put(player.getUserId(), player);
	}

	@Override
	public IPlayer select(long userId) {
		return playerMap.get(userId);
	}

	@Override
	public IPlayer remove(long userId) {
		return playerMap.remove(userId);
	}

	@Override
	public void removerAllPlayer() {
		playerMap.clear();
	}
}
