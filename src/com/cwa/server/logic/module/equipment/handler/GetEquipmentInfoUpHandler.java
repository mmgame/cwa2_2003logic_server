package com.cwa.server.logic.module.equipment.handler;

import java.util.ArrayList;
import java.util.List;

import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.message.EquipmentMessage.EquipmentInfoBean;
import com.cwa.message.EquipmentMessage.GetEquipmentInfoDown;
import com.cwa.message.EquipmentMessage.GetEquipmentInfoUp;
import com.cwa.message.EquipmentMessage.SlotInfoBean;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取相关英雄装备信息
 * 
 * @author tzy
 * 
 */
public class GetEquipmentInfoUpHandler extends IGameHandler<GetEquipmentInfoUp> {

	@Override
	public void loginedHandler(GetEquipmentInfoUp message, IPlayer player) {
		int heroKeyId = message.getHeroKeyId();

		EquipmentDataFunction edFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);

		List<EquipmentEntity> equipmentEntityList = edFunction.getEntityByHeroId(heroKeyId);

		List<EquipmentInfoBean> infoList = new ArrayList<EquipmentInfoBean>();
		for (EquipmentEntity equipmentEntity : equipmentEntityList) {
			EquipmentInfoBean.Builder bean = EquipmentInfoBean.newBuilder();
			bean.setPositionId(equipmentEntity.positionId);
			bean.setLevel(equipmentEntity.equipmentLevel);
			bean.setQuality(equipmentEntity.equipmentQuality);
			int[] plug = equipmentEntity.getPIds();
			List<SlotInfoBean> slotList = new ArrayList<SlotInfoBean>();
			for (int i = 1; i <= plug.length; i++) {
				SlotInfoBean.Builder b = SlotInfoBean.newBuilder();
				b.setId(i);
				b.setPlugId(plug[i - 1]);
				slotList.add(b.build());
			}
			bean.addAllSlotInfoBean(slotList);
			infoList.add(bean.build());
		}
		sendGetEquipmentInfoDownMessage(heroKeyId, infoList, player);
	}

	/**
	 * 发送获取装备信息
	 * 
	 * @param session
	 * @param userId
	 * @param heroId
	 * @param beans
	 */
	private void sendGetEquipmentInfoDownMessage(int heroId, List<EquipmentInfoBean> beans, IPlayer player) {
		GetEquipmentInfoDown.Builder b = GetEquipmentInfoDown.newBuilder();
		b.setUserId(String.valueOf(player.getUserId()));
		b.setHeroKeyId(heroId);
		b.addAllEquipmentInfoBean(beans);
		player.getSession().send(b.build());
	}
}
