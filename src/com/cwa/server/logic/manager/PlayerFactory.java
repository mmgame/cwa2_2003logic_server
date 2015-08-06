package com.cwa.server.logic.manager;

import java.util.HashMap;
import java.util.Map;

import com.cwa.ISession;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.player.IPlayer;
import com.cwa.server.logic.player.IPlayerState;
import com.cwa.server.logic.player.Player;
import com.cwa.server.logic.player.PlayerStateTypeEnum;

/**
 * player工厂
 * 
 * @author mausmars
 * 
 */
public class PlayerFactory implements IPlayerFactory {
	// 逻辑服上下文
	protected ILogicContext logicContext;

	// 战场默认状态机
	protected Map<Integer, IPlayerState> defaultStateMap = new HashMap<Integer, IPlayerState>();

	@Override
	public IPlayer createPlayer(long userId, int rid, Object params) {
		// 初始化player状态管理
		PlayerStateManager stateManager = new PlayerStateManager();

		stateManager.setCurrentState(defaultStateMap.get(PlayerStateTypeEnum.PS_Idle.value()));
		stateManager.setDefaultStateMap(defaultStateMap);

		// 初始化player数据管理
		PlayerFunctionManager playerFunctionManager = new PlayerFunctionManager();

		Player player = new Player();
		player.setUserId(userId);
		player.setStateManager(stateManager);
		player.setRid(rid);
		player.setLogicContext(logicContext);
		player.setPlayerFunctionManager(playerFunctionManager);
		player.setSession((ISession) params);

		stateManager.setPlayer(player);
		playerFunctionManager.init(player);

		// 插入player
		logicContext.getPlayerManager().insert(player);

		return player;
	}

	// -------------------------------------
	public void setLogicContext(ILogicContext logicContext) {
		this.logicContext = logicContext;
	}
}
