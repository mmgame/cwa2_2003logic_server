package com.cwa.server.logic.module.cd.impl;

import com.cwa.prototype.GameCDPrototype;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.TimeUtil;

/**
 * 从cd重置算起
 * 
 * @author tzy
 * 
 */
public class GeneralGameCd implements IGameCdHandler {
	@Override
	public boolean isFinishedCd(IPlayer player, int cdType, long resetTime) {
		if (resetTime > TimeUtil.currentSystemTime()) {
			// cd没到期
			return false;
		}
		// cd到了执行的操作
		return true;
	}

	@Override
	public long resetcd(IPlayer player, int cdType) {
		GameCDPrototype cdPrototype = player.getLogicContext().getprototypeManager().getPrototype(GameCDPrototype.class, cdType);
		if (cdPrototype == null) {
			return 0;
		}
		return TimeUtil.currentSystemTime() +cdPrototype.getCdTime() * 1000;
	}
}
