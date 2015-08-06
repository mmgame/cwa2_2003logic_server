package com.cwa.server.logic.module.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwa.ISession;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.LErrorMessage.LErrorDown;
import com.cwa.message.LErrorMessage.LErrorTypeEnum;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.google.protobuf.GeneratedMessage;

/**
 * 消息同步管理
 * 
 * @author mausmars
 *
 */
public class SyncManager {
	protected static final Logger logger = LoggerFactory.getLogger(SyncManager.class);

	public void sendInputError(ISession session, InputErrorTypeEnum inputErrorType, GeneratedMessage message) {
		LErrorDown.Builder b = LErrorDown.newBuilder();
		b.setErrorType(LErrorTypeEnum.InputError);
		b.setInputErrorType(inputErrorType);
		b.setErrorMessage(message.getClass().getSimpleName());
		session.send(b.build());
		if (logger.isInfoEnabled()) {
			logger.info("InputErrorMessage=" + message.getClass().getSimpleName() + " InputErrorType=" + inputErrorType);
		}
	}

	public void sendDataError(ISession session, ModuleTypeEnum moduleType, GeneratedMessage message) {
		LErrorDown.Builder b = LErrorDown.newBuilder();
		b.setErrorType(LErrorTypeEnum.DataError);
		b.setModuleType(moduleType);
		b.setErrorMessage(message.getClass().getSimpleName());
		session.send(b.build());
		if (logger.isInfoEnabled()) {
			logger.info("DataErrorMessage=" + message.getClass().getSimpleName() + " ModuleType=" + moduleType);
		}
	}

	public void sendSystemError(ISession session, ModuleTypeEnum moduleType, GeneratedMessage message) {
		LErrorDown.Builder b = LErrorDown.newBuilder();
		b.setErrorType(LErrorTypeEnum.SystemError);
		b.setModuleType(moduleType);
		b.setErrorMessage(message.getClass().getSimpleName());
		session.send(b.build());
		if (logger.isInfoEnabled()) {
			logger.info("DataErrorMessage=" + message.getClass().getSimpleName() + " ModuleType=" + moduleType);
		}
	}
}
