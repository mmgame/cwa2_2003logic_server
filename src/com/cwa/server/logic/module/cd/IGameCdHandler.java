package com.cwa.server.logic.module.cd;

import com.cwa.server.logic.player.IPlayer;

/**
 * @author tzy
 * 
 */
public interface IGameCdHandler {
	boolean isFinishedCd(IPlayer player, int cdType, long resetTime);
	
	long resetcd(IPlayer player, int cdType);
}
