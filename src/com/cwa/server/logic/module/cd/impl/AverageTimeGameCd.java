package com.cwa.server.logic.module.cd.impl;

import com.cwa.prototype.GameCDPrototype;
import com.cwa.server.logic.module.cd.IGameCdHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.util.TimeUtil;

/**
 * 从指定间隔时间算
 * 
 * @author tzy
 *
 */
public class AverageTimeGameCd implements IGameCdHandler {
	@Override
	public boolean isFinishedCd(IPlayer player, int cdType, long resetTime) {
		GameCDPrototype cdPrototype = player.getLogicContext().getprototypeManager().getPrototype(GameCDPrototype.class, cdType);
		if (cdPrototype == null) {
			return true;
		}

		int intervalNum = TimeUtil.getIntervalNum(cdPrototype.getCdTime(),cdPrototype.getStartTime());
		int resetlNum = TimeUtil.getIntervalNum(cdPrototype.getCdTime(), resetTime,cdPrototype.getStartTime());
		if (resetlNum >= intervalNum) {
			// cd没到期
			return false;
		}
		return true;
	}


	@Override
	public long resetcd(IPlayer player, int cdType) {
		return TimeUtil.currentSystemTime();
	}

}
