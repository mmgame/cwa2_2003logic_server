package com.cwa.server.logic;

import baseice.service.FunctionAddress;

import com.cwa.service.IModuleServer;

public interface ILogicService extends IModuleServer {
	/**
	 * 获得在线人数
	 * 
	 * @return
	 */
	int getOnlineCount();

	/**
	 * 获得服务器地址
	 * 
	 * @return
	 */
	FunctionAddress getAddress();
}
