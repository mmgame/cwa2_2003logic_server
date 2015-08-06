package com.cwa.server.logic.player;

public enum PlayerStateTypeEnum {
	PS_Idle(1), // 空闲状态
	PS_Room(2), // 房间状态
	;
	private int value;

	PlayerStateTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
