package com.cwa.server.logic.module.user.handler;

import com.cwa.data.entity.domain.UserattrEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.UserMessage.GetUserinfoDown;
import com.cwa.message.UserMessage.GetUserinfoUp;
import com.cwa.message.UserMessage.UserInfoBean;
import com.cwa.prototype.gameEnum.UserAttrKeyEnum;
import com.cwa.server.logic.dataFunction.UserattrDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取用户信息
 * 
 * @author tzy
 * 
 */
public class GetUserinfoUpHandler extends IGameHandler<GetUserinfoUp> {
	@Override
	public void loginedHandler(GetUserinfoUp message, IPlayer player) {
		UserinfoDataFunction udFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		UserattrDataFunction uadFunction = (UserattrDataFunction) player.getDataFunctionManager().getDataFunction(UserattrEntity.class);

		// 恢复体力
		udFunction.cdPowerReset();
		udFunction.cdBuyPowerReset();

		UserattrEntity luckyInfo = uadFunction.getEntity(UserAttrKeyEnum.Lucky_Type);
		UserattrEntity stateInfo = uadFunction.getEntity(UserAttrKeyEnum.Current_State);

		UserinfoEntity userinfoEntity = udFunction.getEntity();
		GetUserinfoDown.Builder b = GetUserinfoDown.newBuilder();
		if (userinfoEntity != null) {
			UserInfoBean bean = userinfoEntity2UserInfoBean(userinfoEntity, luckyInfo.attrValue, stateInfo.attrValue);
			b.setUserInfoBean(bean);
		}
		b.setUserId(String.valueOf(player.getUserId()));
		player.getSession().send(b.build());
	}

	private UserInfoBean userinfoEntity2UserInfoBean(UserinfoEntity userinfoEntity, long lucky, long currentState) {
		UserInfoBean.Builder bean = UserInfoBean.newBuilder();
		bean.setName(userinfoEntity.name);
		bean.setLevel(userinfoEntity.level);
		bean.setExperience(userinfoEntity.experience);
		bean.setIcon(userinfoEntity.icon);
		bean.setEventState(userinfoEntity.eventState);
		bean.setFunctionState(userinfoEntity.functionState);
		bean.setModifyNameCount(userinfoEntity.modifyNameCount);
		bean.setPower(userinfoEntity.power);
		bean.setLucky((int) lucky);
		bean.setCurrentState((int) currentState);
		bean.setBuyPowerCount(userinfoEntity.buyCount);
		return bean.build();
	}
}
