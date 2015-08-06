package com.cwa.server.logic.module.user.handler;

import com.cwa.ISession;
import com.cwa.message.UserMessage.UserLoginDown;
import com.cwa.message.UserMessage.UserLoginUp;
import com.cwa.net.ISessionManager;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

public class UserLoginUpHandler2 extends IGameHandler<UserLoginUp> {
	@Override
	public void unloginHandler(UserLoginUp message, ISession session) {
		String token = message.getToken();
		int rid = message.getRid();
		long userId = Long.parseLong(message.getUserIdStr());// 这里可能是新创建账号，可能是已存在的账号
		int asid = message.getAsid();

		IPlayer player = logicContext.getPlayerManager().select(userId);
		if (player == null) {
			player = logicContext.getPlayerFactory().createPlayer(userId, rid, session);
			// 登陆初始化数据
			player.getDataFunctionManager().initData();
		} else {
			// 替换session
			player.replaceSession(session);
		}
		// session绑定player
		session.setAttachment(ISessionManager.Target_Key, player);
		session.setAttachment(ISessionManager.UserId_Key, player.getUserId());
		sendUserLoginDownMessage(player.getSession(), 1, 0);
	}





	private void sendUserLoginDownMessage(ISession session, int state, long time) {
		UserLoginDown.Builder bb = UserLoginDown.newBuilder();
		bb.setLogoutTime(String.valueOf(System.currentTimeMillis()));
		bb.setLoginState(state);
		session.send(bb.build());
	}
}