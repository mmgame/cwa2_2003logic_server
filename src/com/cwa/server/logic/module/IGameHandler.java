/**
 * 
 */
package com.cwa.server.logic.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwa.ISession;
import com.cwa.handler.IMessageHandler;
import com.cwa.net.ISessionManager;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.player.IPlayer;

/**
 * @author tzy
 * 
 */
public abstract class IGameHandler<T> implements IMessageHandler<T> {
	protected static final Logger logger = LoggerFactory.getLogger(IGameHandler.class);

	protected ILogicContext logicContext;

	@Override
	public void handle(T message, ISession session) {
		IPlayer target = (IPlayer) session.getAttachment(ISessionManager.Target_Key);
		try {
			if (target == null) {// 用户注册或者登陆
				unloginHandler(message, session);
			} else {
				loginedHandler(message, target);
			}
		} catch (Exception e) {
			logger.error("GameHandler error! target=" + target, e);
		} finally {
		}
	}

	public void unloginHandler(T message, ISession session) {
		logger.error("Error user state!  login!" + message);
	}

	public void loginedHandler(T message, IPlayer player) {
		logger.error("Error user state! no login!" + message);
	}

	// -----------------------------------------------
	public void setLogicContext(ILogicContext logicContext) {
		this.logicContext = logicContext;
	}

}