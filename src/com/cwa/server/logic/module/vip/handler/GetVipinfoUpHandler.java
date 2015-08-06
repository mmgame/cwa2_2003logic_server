package com.cwa.server.logic.module.vip.handler;

import com.cwa.data.entity.domain.VipEntity;
import com.cwa.message.VipMessage.GetVipinfoDown;
import com.cwa.message.VipMessage.GetVipinfoUp;
import com.cwa.server.logic.dataFunction.VipDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取VIP信息
 * 
 * @author tzy
 * 
 */
public class GetVipinfoUpHandler extends IGameHandler<GetVipinfoUp> {
	@Override
	public void loginedHandler(GetVipinfoUp message, IPlayer player) {
		VipDataFunction vdFunction = (VipDataFunction) player.getDataFunctionManager().getDataFunction(VipEntity.class);
		sendGetVipinfoDownMessage(vdFunction.getEntity(), player);
	}

	private void sendGetVipinfoDownMessage(VipEntity vipEntity, IPlayer player) {
		GetVipinfoDown.Builder b = GetVipinfoDown.newBuilder();
		b.setLevel(vipEntity.vipLevel);
		b.setExperience(vipEntity.vipExp);
		b.setAwardState(vipEntity.rewardState);
		player.getSession().send(b.build());
	}
}
