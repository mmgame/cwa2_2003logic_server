package com.cwa.server.logic.service;

import serverice.logic._ILogicServiceDisp;
import Ice.Current;
import baseice.service.FunctionAddress;

import com.cwa.server.logic.ILogicService;

public class LogicServiceI extends _ILogicServiceDisp {
	private static final long serialVersionUID = 1L;
	private ILogicService logicService;

	@Override
	public int getOnlineCount(Current __current) {
		return logicService.getOnlineCount();
	}

	@Override
	public FunctionAddress getAddress(Current __current) {
		return logicService.getAddress();
	}

	// -----------------------------------------------------
	public void setLogicService(ILogicService logicService) {
		this.logicService = logicService;
	}

}
