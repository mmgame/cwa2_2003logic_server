package com.cwa.server.logic.module.cd;

public enum CDHandlerTypeEnum {
	CD_General(1), // 普通cd
	CD_Average(2), // 平均cd
	;

	int value;

	CDHandlerTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
