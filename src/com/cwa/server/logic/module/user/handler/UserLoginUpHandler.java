package com.cwa.server.logic.module.user.handler;

import serverice.account.IAccountServicePrx;
import baseice.constant.ErrorValue;
import baseice.service.FunctionTypeEnum;

import com.cwa.ISession;
import com.cwa.component.datatimeout.IDataTimeoutManager;
import com.cwa.component.functionmanage.IFunctionCluster;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.message.UserMessage.UserLoginDown;
import com.cwa.message.UserMessage.UserLoginUp;
import com.cwa.net.ISessionManager;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.service.context.IGloabalContext;

public class UserLoginUpHandler extends IGameHandler<UserLoginUp> {
	@Override
	public void unloginHandler(UserLoginUp message, ISession session) {
		String token = message.getToken();
		int rid = message.getRid();
		long userId = Long.parseLong(message.getUserIdStr());// 这里可能是新创建账号，可能是已存在的账号
		int asid = message.getAsid();

		IGloabalContext gloabalContext = logicContext.getGloabalContext();

		// 验证服验证用户登陆
		userId = checkLogin(token, rid, userId, asid, gloabalContext);
		if (userId <= ErrorValue.value) {
			// 非法登陆或验证服不可用
			removeChecker(session);
			session.close(true);
			return;
		}

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
		sendUserLoginDownMessage(session, 0, 1);
	}

	private void removeChecker(ISession session) {
		IDataTimeoutManager dataTimeoutManager = (IDataTimeoutManager) session.getAttachment(IDataTimeoutManager.DataTimeoutManagerKey);
		if (dataTimeoutManager != null) {
			dataTimeoutManager.removeTimeoutCheck(String.valueOf(session.getSessionId()));
		}
	}

	private long checkLogin(String token, int rid, long userId, int asid, IGloabalContext gloabalContext) {
		// 选取逻辑服
		IFunctionService functionService = gloabalContext.getCurrentFunctionService();
		IFunctionCluster functionCluster = functionService.getFunctionCluster(gloabalContext.getGid(), FunctionTypeEnum.Account);
		if (functionCluster == null) {
			return ErrorValue.value;
		}
		// 指定id的服务
		IAccountServicePrx servicePrx = functionCluster.getSlaveService(asid, IAccountServicePrx.class);
		if (servicePrx == null) {
			return ErrorValue.value;
		}
		return servicePrx.checkLogin(token, rid, userId);
	}

	private void sendUserLoginDownMessage(ISession session, int state, long time) {
		UserLoginDown.Builder bb = UserLoginDown.newBuilder();
		bb.setLogoutTime(String.valueOf(System.currentTimeMillis()));
		session.send(bb.build());
	}
}