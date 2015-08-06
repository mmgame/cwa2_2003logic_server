package com.cwa.server.logic.module.economy.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cwa.data.entity.domain.UsereconomyEntity;
import com.cwa.message.EconomyMessage.GetUserEconomyDown;
import com.cwa.message.EconomyMessage.GetUserEconomyUp;
import com.cwa.message.EconomyMessage.UserEconomyBean;
import com.cwa.server.logic.dataFunction.UsereconomyDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取用户经济信息
 * 
 * @author tzy
 * 
 */
public class GetUserEconomyUpHandler extends IGameHandler<GetUserEconomyUp> {
	@Override
	public void loginedHandler(GetUserEconomyUp message, IPlayer player) {
		UsereconomyDataFunction udFunction = (UsereconomyDataFunction) player.getDataFunctionManager().getDataFunction(
				UsereconomyEntity.class);

		Collection<UsereconomyEntity> entitys = udFunction.getAllEntity();
		List<UserEconomyBean> userEconomyList = new ArrayList<UserEconomyBean>();
		for (UsereconomyEntity entity : entitys) {
			UserEconomyBean.Builder bean = UserEconomyBean.newBuilder();
			bean.setMoneyType(entity.moneyType);
			bean.setMoneyCount(entity.moneyCount);
			userEconomyList.add(bean.build());
		}
		sendGetUserEconomyDownMessage(userEconomyList, player);
	}

	/**
	 * 发送获取用户经济信息
	 * 
	 * @param session
	 * @param list
	 */
	private void sendGetUserEconomyDownMessage(List<UserEconomyBean> list, IPlayer player) {
		GetUserEconomyDown.Builder b = GetUserEconomyDown.newBuilder();
		b.addAllUserEconomyBean(list);
		player.getSession().send(b.build());
	}
}
