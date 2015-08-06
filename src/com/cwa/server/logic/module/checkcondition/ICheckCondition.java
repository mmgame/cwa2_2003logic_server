package com.cwa.server.logic.module.checkcondition;

import com.cwa.server.logic.player.IPlayer;

public interface ICheckCondition {
	boolean check(Object condition, Object attr, IPlayer player);
}
