package com.cwa.server.logic.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import serverice.config.NewRegionDBEvent;
import baseice.event.IEvent;

import com.cwa.component.data.IDBService;
import com.cwa.component.data.IDBSession;
import com.cwa.component.event.IEventHandler;
import com.cwa.data.config.IDatabaseInfoConfigDao;
import com.cwa.data.config.domain.DatabaseInfoConfig;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.service.constant.ServiceConstant;
import com.cwa.service.init.servicefactory.IDBServiceFactory;

/**
 * 新区数据配置处理
 * 
 * @author mausmars
 *
 */
public class NewRegionDBEventHandler implements IEventHandler {
	protected static final Logger logger = LoggerFactory.getLogger(IEventHandler.class);

	private ILogicContext logicContext;
	private IDBServiceFactory dbServiceFactory;

	@Override
	public void eventHandler(IEvent event) {
		NewRegionDBEvent e = new NewRegionDBEvent();
		if (e.gid != logicContext.getGid()) {
			return;
		}
		try {
			IDBService dbService = logicContext.getGloabalContext().getCurrentDBService();
			IDBSession dbSession = dbService.getDBSessionByKey(e.dbid);
			if (dbSession == null) {
				IDBService configDBService = (IDBService) logicContext.getGloabalContext().getService(ServiceConstant.General_Gid,
						ServiceConstant.DatabaseKey);// 配置服数据库session
				IDBSession configDBSession = configDBService.getDBSession(ServiceConstant.General_Rid);
				IDatabaseInfoConfigDao infoDao = (IDatabaseInfoConfigDao) configDBSession.getEntityDao(DatabaseInfoConfig.class);
				DatabaseInfoConfig databaseInfoConfig = (DatabaseInfoConfig) infoDao.selectEntityByGidAndDbid(logicContext.getGid(),
						e.dbid, configDBSession.getParams(ServiceConstant.General_Rid));

				dbSession = dbServiceFactory.createDBSession(dbService.getServiceConfig(), databaseInfoConfig);
			}
			if (dbSession == null) {
				return;
			}
			for (int rid : e.rids) {
				dbService.insertDBSession(dbSession.getKey(), rid, dbSession);
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
	}

	// -------------------------------------
	public void setLogicContext(ILogicContext logicContext) {
		this.logicContext = logicContext;
	}
}
