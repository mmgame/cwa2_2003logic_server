package com.cwa.server.logic.module.cd;

import java.util.HashMap;
import java.util.Map;

import com.cwa.prototype.GameCDPrototype;
import com.cwa.prototype.gameEnum.CDTypeEnum;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.module.cd.impl.AverageTimeGameCd;
import com.cwa.server.logic.module.cd.impl.GeneralGameCd;

/**
 * @author tzy
 * 
 */
public class CDManager {
	private Map<Integer, IGameCdHandler> gameCdMap = new HashMap<Integer, IGameCdHandler>();

	public CDManager() {
		gameCdMap.put(CDHandlerTypeEnum.CD_General.value(), new GeneralGameCd());
		gameCdMap.put(CDHandlerTypeEnum.CD_Average.value(), new AverageTimeGameCd());
	}

	// 是否完成
	public IGameCdHandler getGameCd(CDTypeEnum cdType, ILogicContext logicContext) {
		GameCDPrototype cdPrototype = logicContext.getprototypeManager().getPrototype(GameCDPrototype.class, cdType.value());
		if (cdPrototype == null) {
			return null;
		}
		return gameCdMap.get(cdPrototype.getCdHandlerType());
	}

	/**
	 * 是否保存数据库
	 * 
	 * @param cdType
	 * @param logicContext
	 * @return
	 */
	public boolean isSaveDb(CDTypeEnum cdType, ILogicContext logicContext) {
		GameCDPrototype cdPrototype = logicContext.getprototypeManager().getPrototype(GameCDPrototype.class, cdType.value());
		return cdPrototype.getIsSave() == 1;
	}
}