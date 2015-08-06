package com.cwa.server.logic.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import baseice.basedao.IEntity;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.component.data.function.IDataFunctionManager;
import com.cwa.component.datatimeout.IDataTimeoutCallBlack;
import com.cwa.component.datatimeout.IDataTimeoutManager;
import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.MatchAwardEntity;
import com.cwa.data.entity.domain.MatchConcealEntity;
import com.cwa.data.entity.domain.MatchEntity;
import com.cwa.data.entity.domain.MatchMopupEntity;
import com.cwa.data.entity.domain.MatchShopEntity;
import com.cwa.data.entity.domain.MatchStarEntity;
import com.cwa.data.entity.domain.UserattrEntity;
import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.data.entity.domain.VipEntity;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.FormationDataFunction;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.MatchAwardDataFunction;
import com.cwa.server.logic.dataFunction.MatchConcealDataFunction;
import com.cwa.server.logic.dataFunction.MatchDataFunction;
import com.cwa.server.logic.dataFunction.MatchMopupDataFunction;
import com.cwa.server.logic.dataFunction.MatchShopDataFunction;
import com.cwa.server.logic.dataFunction.MatchStarDataFunction;
import com.cwa.server.logic.dataFunction.UserattrDataFunction;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.dataFunction.VipDataFunction;
import com.cwa.server.logic.player.IPlayer;

/**
 * 用户功能管理
 * 
 * @author mausmars
 *
 */
public class PlayerFunctionManager implements IDataFunctionManager {
	private IPlayer player;
	// 数据session
	private IDBSession dbSession;
	// 数据功能map
	private Map<String, IDataFunction> dataFunctionMap = new HashMap<String, IDataFunction>();

	public void init(IPlayer player) {
		this.player = player;
		dbSession = player.getLogicContext().getDbSession(player.getRid());

		dataFunctionMap.put(UserinfoEntity.class.getSimpleName(), new UserinfoDataFunction(player));
		dataFunctionMap.put(UserattrEntity.class.getSimpleName(), new UserattrDataFunction(player));
		dataFunctionMap.put(UsereconomyEntity.class.getSimpleName(), new UsereconomyDataFunction(player));
		dataFunctionMap.put(VipEntity.class.getSimpleName(), new VipDataFunction(player));

		dataFunctionMap.put(HeroEntity.class.getSimpleName(), new HeroDataFunction(player));
		dataFunctionMap.put(EquipmentEntity.class.getSimpleName(), new EquipmentDataFunction(player));
		dataFunctionMap.put(ItemEntity.class.getSimpleName(), new ItemDataFunction(player));
		dataFunctionMap.put(FormationEntity.class.getSimpleName(), new FormationDataFunction(player));
		
		dataFunctionMap.put(MatchAwardEntity.class.getSimpleName(), new MatchAwardDataFunction(player));
		dataFunctionMap.put(MatchConcealEntity.class.getSimpleName(), new MatchConcealDataFunction(player));
		dataFunctionMap.put(MatchEntity.class.getSimpleName(), new MatchDataFunction(player));
		dataFunctionMap.put(MatchMopupEntity.class.getSimpleName(), new MatchMopupDataFunction(player));
		dataFunctionMap.put(MatchShopEntity.class.getSimpleName(), new MatchShopDataFunction(player));
		dataFunctionMap.put(MatchStarEntity.class.getSimpleName(), new MatchStarDataFunction(player));
	 
	}

	@Override
	public void initData() {
		IDataFunction df = getDataFunction(UserinfoEntity.class);
		boolean newRegister = df.initData(false);

		for (Entry<String, IDataFunction> entry : dataFunctionMap.entrySet()) {
			if (entry.getKey().equals(UserinfoEntity.class.getSimpleName())) {
				continue;
			}
			entry.getValue().initData(newRegister);
		}
	}

	@Override
	public void initData(Class<? extends IEntity> cla) {
		IDataFunction dataFunction = dataFunctionMap.get(cla.getSimpleName());
		if (dataFunction != null) {
			dataFunction.initData(false);
		}
	}

	@Override
	public IDataFunction getDataFunction(Class<? extends IEntity> cla) {
		return dataFunctionMap.get(cla.getSimpleName());
	}

	@Override
	public IDBSession getDbSession() {
		return dbSession;
	}

	@Override
	public void insertDataTimeout() {
		IDataTimeoutManager dataTimeoutManager = player.getLogicContext().getDataTimeoutManager();
		if (dataTimeoutManager == null) {
			return;
		}
		dataTimeoutManager.insertTimeoutCheck(String.valueOf(player.getUserId()), player, new IDataTimeoutCallBlack() {
			@Override
			public void callblack(Object obj) {
				IPlayer p = (IPlayer) obj;
				// 移除player
				p.getLogicContext().getPlayerManager().remove(p.getUserId());
			}
		});
	}

	@Override
	public void removeDataTimeout() {
		IDataTimeoutManager dataTimeoutManager = player.getLogicContext().getDataTimeoutManager();
		if (dataTimeoutManager == null) {
			return;
		}
		dataTimeoutManager.removeTimeoutCheck(String.valueOf(player.getUserId()));
	}

	@Override
	public void resetDataTimeout() {
		IDataTimeoutManager dataTimeoutManager = player.getLogicContext().getDataTimeoutManager();
		if (dataTimeoutManager == null) {
			return;
		}
		dataTimeoutManager.resetTime(String.valueOf(player.getUserId()));
	}
}
