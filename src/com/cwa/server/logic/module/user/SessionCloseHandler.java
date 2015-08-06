package com.cwa.server.logic.module.user;

import com.cwa.ISession;
import com.cwa.handler.IClosedSessionHandler;
import com.cwa.net.ISessionManager;
import com.cwa.server.logic.player.IPlayer;

public class SessionCloseHandler implements IClosedSessionHandler {
	/**
	 * 关闭session时的后去操纵
	 */
	@Override
	public void execute(ISession session) {
		IPlayer player = (IPlayer) session.getAttachment(ISessionManager.Target_Key);
		if (player == null) {
			return;
		}
		Boolean isReplace = (Boolean) session.getAttachment("is_replace");
		if (isReplace != null && isReplace) {
			// 是替换的
			return;
		}
		// 清除session
		player.clearSession();
	}
}
