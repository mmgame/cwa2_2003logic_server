package com.cwa.server.logic;

import baseice.service.FunctionAddress;
import baseice.service.NetProtocolEnum;

import com.cwa.component.data.IDBService;
import com.cwa.component.data.IDBSession;
import com.cwa.component.datatimeout.IDataTimeoutManager;
import com.cwa.component.datatimeout.IDataTimeoutService;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.net.ISessionManager;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.manager.IPlayerFactory;
import com.cwa.server.logic.manager.IPlayerManager;
import com.cwa.server.logic.manager.PlayerFactory;
import com.cwa.server.logic.manager.PlayerManager;
import com.cwa.server.logic.module.cd.CDManager;
import com.cwa.server.logic.module.checkcondition.CheckManager;
import com.cwa.server.logic.module.constant.ConstantManager;
import com.cwa.server.logic.module.drop.UpdateManager;
import com.cwa.server.logic.module.room.RoomManager;
import com.cwa.server.logic.module.sync.SyncManager;
import com.cwa.service.IService;
import com.cwa.service.constant.ServiceConstant;
import com.cwa.service.context.IGloabalContext;
import com.cwa.service.init.services.NettyService;

public class LogicService implements ILogicService, ILogicContext {
	private IGloabalContext gloabalContext;

	private PlayerFactory playerFactory;
	private PlayerManager playerManager;

	private RoomManager roomManager;
	private SyncManager syncManager;
	private CheckManager checkManager;
	private UpdateManager updateManager;
	private CDManager cdManager;

	private IDataTimeoutManager dataTimeoutManager;
	private ConstantManager constantManager;

	@Override
	public FunctionAddress getAddress() {
		NettyService service = (NettyService) gloabalContext.getCurrentService(ServiceConstant.NettyServerClientKey);

		FunctionAddress address = new FunctionAddress();
		address.ip = gloabalContext.getOutsideNetIp();
		address.port = service.getServer().getPort();
		address.protocol = NetProtocolEnum.tcp;
		return address;
	}

	@Override
	public int getOnlineCount() {
		return 0;
	}

	// -------------------------------
	@Override
	public void startup(IGloabalContext gloabalContext) {
		this.gloabalContext = gloabalContext;
		// 重置常量类
		constantManager.resetConstant();
		// player管理
		playerManager = new PlayerManager();
		// player工厂
		playerFactory = new PlayerFactory();

		cdManager = new CDManager();
		checkManager = new CheckManager();
		roomManager = new RoomManager();
		syncManager = new SyncManager();
		updateManager = new UpdateManager();
		playerFactory.setLogicContext(this);

		// 创建dataTimeoutManager管理
		IDataTimeoutService dataTimeoutService = (IDataTimeoutService) gloabalContext.getCurrentService(ServiceConstant.DataTimeoutKey);
		if (dataTimeoutService != null) {
			dataTimeoutManager = dataTimeoutService.createTask("data_timeoutcheck_" + gloabalContext.getGid());
		}
	}

	@Override
	public void shutdown() throws Exception {
	}

	public RoomManager getRoomManager() {
		return roomManager;
	}

	public IPlayerManager getPlayerManager() {
		return playerManager;
	}

	public SyncManager getSyncManager() {
		return syncManager;
	}

	@Override
	public IPlayerFactory getPlayerFactory() {
		return playerFactory;
	}

	@Override
	public IPrototypeClientService getprototypeManager() {
		IService service = gloabalContext.getCurrentService(ServiceConstant.ProtoclientKey);
		return (IPrototypeClientService) service;
	}

	@Override
	public IDBSession getDbSession(int rid) {
		IDBService service = (IDBService) gloabalContext.getCurrentService(ServiceConstant.DatabaseKey);
		if (service == null) {
			return null;
		}
		return service.getDBSession(rid);
	}

	@Override
	public ISessionManager getSessionManager() {
		return ((NettyService) gloabalContext.getCurrentService(ServiceConstant.NettyServerClientKey)).getServer().getSessionManager();
	}

	@Override
	public IFunctionService getFunctionService() {
		return gloabalContext.getCurrentFunctionService();
	}

	@Override
	public IGloabalContext getGloabalContext() {
		return gloabalContext;
	}

	@Override
	public int getGid() {
		return gloabalContext.getGid();
	}

	@Override
	public CheckManager getCheckManager() {
		return checkManager;
	}

	@Override
	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	@Override
	public CDManager getCdManager() {
		return cdManager;
	}

	@Override
	public IDataTimeoutManager getDataTimeoutManager() {
		return dataTimeoutManager;
	}

	// -------------------------------------
	public void setConstantManager(ConstantManager constantManager) {
		this.constantManager = constantManager;
	}
}
