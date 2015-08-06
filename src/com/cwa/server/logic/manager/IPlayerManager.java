package com.cwa.server.logic.manager;

import com.cwa.server.logic.player.IPlayer;

/**
 * player对象管理（ 管理全部的对象）
 * 
 * @author mausmars
 * 
 */
public interface IPlayerManager {
	/**
	 * 添加
	 * 
	 * @param player
	 */
	void insert(IPlayer player);

	/**
	 * 查询
	 * 
	 * @param id
	 * @return
	 */
	IPlayer select(long id);

	/**
	 * 移除
	 * 
	 * @param id
	 * @return
	 */
	IPlayer remove(long id);

	/**
	 * 移除全部player
	 * 
	 * @param id
	 * @return
	 */
	void removerAllPlayer();
}
