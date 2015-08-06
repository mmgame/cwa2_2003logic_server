package com.cwa.server.logic.player;

import com.cwa.ISession;
import com.cwa.component.data.function.IDataFunctionManager;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.manager.IPlayerStateManager;

/**
 * player接口
 * 
 * @author mausmars
 *
 */
public interface IPlayer {
	/**
	 * player的id
	 * 
	 * @return
	 */
	long getUserId();

	/**
	 * 获取区id
	 * 
	 * @return
	 */
	int getRid();

	/**
	 * 逻辑模块上下文
	 * 
	 * @return
	 */
	ILogicContext getLogicContext();

	/**
	 * player状态管理
	 * 
	 * @return
	 */
	IPlayerStateManager getStateManager();

	/**
	 * player功能管理
	 * 
	 * @return
	 */
	IDataFunctionManager getDataFunctionManager();

	/**
	 * player的session 如果session为null则是临时的数据
	 * 
	 * @return
	 */
	ISession getSession();

	/**
	 * 替换session
	 * 
	 * @return
	 */
	void replaceSession(ISession session);

	/**
	 * 清楚session
	 */
	void clearSession();

	/**
	 * 直接发送消息
	 * 
	 * @param key
	 * @return
	 */
	void send(Object msg);
}
