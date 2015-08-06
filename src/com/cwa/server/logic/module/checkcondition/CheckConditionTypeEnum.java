package com.cwa.server.logic.module.checkcondition;

public enum CheckConditionTypeEnum {
	Check_UserLevel(101), // 用户等级
	Check_UserPower(12), // 用户体力
	Check_Item(2), // 道具
	Check_Money(10), // 钱
	;

	int value;

	CheckConditionTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
