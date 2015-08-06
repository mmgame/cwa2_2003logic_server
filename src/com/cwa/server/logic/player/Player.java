package com.cwa.server.logic.player;

import com.cwa.ISession;
import com.cwa.component.data.function.IDataFunctionManager;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.manager.IPlayerStateManager;

/**
 * 角色封装类
 * 
 * @author tzy
 * 
 */
public class Player implements IPlayer {
	private long userId; // 用户id
	private int rid; // 区id
	private ISession session; // 连接session

	private ILogicContext logicContext; // 逻辑服上下文
	private IPlayerStateManager stateManager; // 状态机管理
	private IDataFunctionManager playerFunctionManager;// 用户数据管理

	@Override
	public IPlayerStateManager getStateManager() {
		return stateManager;
	}

	@Override
	public ILogicContext getLogicContext() {
		return logicContext;
	}

	@Override
	public ISession getSession() {
		return session;
	}

	@Override
	public long getUserId() {
		return userId;
	}

	@Override
	public int getRid() {
		return rid;
	}

	@Override
	public IDataFunctionManager getDataFunctionManager() {
		if (session == null) {
			// 重置缓存时间，不过可能会太过频繁
			playerFunctionManager.resetDataTimeout();
		}
		return playerFunctionManager;
	}

	@Override
	public void replaceSession(ISession session) {
		if (this.session == null && session != null) {
			// 移除数据超时
			playerFunctionManager.removeDataTimeout();
		}
		if (this.session != session) {
			if (this.session!=null) {
				this.session.close(true);
			}
			this.session = session;
			this.session.setAttachment("is_replace", true);// session为替换的
		}
	}

	@Override
	public void clearSession() {
		this.session = null;
		// 当前player数据设置超时
		playerFunctionManager.insertDataTimeout();
	}

	@Override
	public void send(Object msg) {
		if (session != null) {
			session.send(msg);
		}
	}

	// --------------------------------------------
	public void setSession(ISession session) {
		this.session = session;
	}

	public void setLogicContext(ILogicContext logicContext) {
		this.logicContext = logicContext;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public void setStateManager(IPlayerStateManager stateManager) {
		this.stateManager = stateManager;
	}

	public void setPlayerFunctionManager(IDataFunctionManager playerFunctionManager) {
		this.playerFunctionManager = playerFunctionManager;
	}
}
