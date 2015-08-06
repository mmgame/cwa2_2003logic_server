package com.cwa.server.logic.module.drop;

public enum DropTypeEnum {
	Drop_UserExperience(13), // 用户经验
	Drop_UserPower(12), // 用户体力
	Drop_Item(2), // 道具
	Drop_Money(10), // 钱
	Drop_HeroExperience(14), // 英雄经验
	;

	int value;

	DropTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
