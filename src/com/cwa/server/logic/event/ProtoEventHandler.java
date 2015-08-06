package com.cwa.server.logic.event;

import serverice.proto.ProtoEvent;
import baseice.event.IEvent;

import com.cwa.component.event.IEventHandler;
import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.server.logic.context.ILogicContext;

public class ProtoEventHandler implements IEventHandler {
	private ILogicContext logicContext;

	@Override
	public void eventHandler(IEvent event) {
		IPrototypeClientService s = logicContext.getprototypeManager();
		if (s != null) {
			s.protoInform((ProtoEvent) event);
		}
	}

	// -------------------------------------
	public void setLogicContext(ILogicContext logicContext) {
		this.logicContext = logicContext;
	}
}
