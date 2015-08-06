package com.cwa.server.logic.module.user.handler;

import com.cwa.message.UserMessage.UserLogoutUp;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

public class UserLogoutUpHandler extends IGameHandler<UserLogoutUp> {
	@Override
	public void loginedHandler(UserLogoutUp message, IPlayer player) {
		if (logger.isInfoEnabled()) {
			logger.info("关闭session，用户登出！！！");
		}
		player.getSession().close(true);
	}
}