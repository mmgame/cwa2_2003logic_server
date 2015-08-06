package com.cwa.server.logic.context;

import com.cwa.component.data.IDBSession;
import com.cwa.component.datatimeout.IDataTimeoutManager;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.net.ISessionManager;
import com.cwa.server.logic.manager.IPlayerFactory;
import com.cwa.server.logic.manager.IPlayerManager;
import com.cwa.server.logic.module.cd.CDManager;
import com.cwa.server.logic.module.checkcondition.CheckManager;
import com.cwa.server.logic.module.drop.UpdateManager;
import com.cwa.server.logic.module.room.RoomManager;
import com.cwa.server.logic.module.sync.SyncManager;
import com.cwa.service.context.IGloabalContext;

/**
 * 逻辑服务上下文
 * 
 * @author tzy
 * 
 */
public interface ILogicContext {
	/**
	 * 
	 * @param rid
	 * @return
	 */
	IDBSession getDbSession(int rid);

	/**
	 * 
	 * @return
	 */
	IGloabalContext getGloabalContext();

	/**
	 * 
	 * @return
	 */
	IFunctionService getFunctionService();

	/**
	 * 
	 * @return
	 */
	int getGid();

	/**
	 * 
	 * @return
	 */
	ISessionManager getSessionManager();

	/**
	 * 
	 * @return
	 */
	IPrototypeClientService getprototypeManager();

	/**
	 * 
	 * @return
	 */
	RoomManager getRoomManager();

	/**
	 * 
	 * @return
	 */
	IPlayerManager getPlayerManager();

	IPlayerFactory getPlayerFactory();

	/**
	 * 
	 * @return
	 */
	SyncManager getSyncManager();

	/**
	 * 
	 * @return
	 */
	CheckManager getCheckManager();

	/**
	 * 
	 * @return
	 */
	UpdateManager getUpdateManager();

	/**
	 * 
	 * @return
	 */
	CDManager getCdManager();

	/**
	 * 数据吃超时管理
	 * 
	 * @return
	 */
	IDataTimeoutManager getDataTimeoutManager();
}
