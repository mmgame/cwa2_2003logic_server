package com.cwa.server.logic.module.drop;

import com.cwa.server.logic.player.IPlayer;

/**
 * 掉落接口
 * @author tzy
 *
 */
public interface IDropHandler {
	void drop(Object parm, IDropAttr attr, IPlayer player);
}
