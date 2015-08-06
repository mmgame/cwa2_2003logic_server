package com.cwa.server.logic.player;

/**
 * player状态
 * 
 * @author mausmars
 * 
 */
public interface IPlayerState {
	/**
	 * 类型
	 */
	int getType();

	/**
	 * 进入状态
	 */
	void enter(IPlayer player);

	/**
	 * 退出状态
	 */
	void exit(IPlayer player);

	/**
	 * 持续状态
	 */
	void update(IPlayer player);
}
