package com.cwa.server.logic.module;

import java.util.List;

import com.cwa.component.event.IEventHandler;
import com.cwa.server.logic.context.ILogicContext;
import com.cwa.server.logic.player.IPlayer;
import com.google.protobuf.GeneratedMessage;

public abstract class AEvent2MessageHandler implements IEventHandler {
	protected ILogicContext logicContext;

	protected void sendMsg(GeneratedMessage msg, List<Long> targetIds) {
		for (long targetId : targetIds) {
			IPlayer player = logicContext.getPlayerManager().select(targetId);
			if (player != null) {
				player.send(msg);
			}
		}
	}

	// -----------------------------------
	public void setLogicContext(ILogicContext logicContext) {
		this.logicContext = logicContext;
	}

}
