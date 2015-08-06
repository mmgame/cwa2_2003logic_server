package com.cwa.server.logic.module.hero.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.data.entity.domain.UserattrEntity;
import com.cwa.gameEnum.HeroTrainStateEnum;
import com.cwa.message.HeroMessage.EquipmentInfoBean;
import com.cwa.message.HeroMessage.GetHeroInfoDown;
import com.cwa.message.HeroMessage.GetHeroInfoUp;
import com.cwa.message.HeroMessage.HeroInfoBean;
import com.cwa.message.HeroMessage.SlotInfoBean;
import com.cwa.prototype.gameEnum.UserAttrKeyEnum;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.dataFunction.UserattrDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 获取用户全部英雄信息
 * 
 * @author tzy
 * 
 */
public class GetHeroInfoUpHandler extends IGameHandler<GetHeroInfoUp> {
	@Override
	public void loginedHandler(GetHeroInfoUp message, IPlayer player) {
		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);

		Collection<HeroEntity> entityList = hdFunction.getAllEntity();
		
		UserattrDataFunction uadFunction = (UserattrDataFunction) player.getDataFunctionManager().getDataFunction(UserattrEntity.class);
		
		// 发送消息
		sendGetAllHeroInfoDownMessage(entityList,(int)uadFunction.getEntity(UserAttrKeyEnum.Hero_Exp).attrValue, player);
	}

	private void sendGetAllHeroInfoDownMessage(Collection<HeroEntity> list,int heroExp, IPlayer player) {
		EquipmentDataFunction edFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);

		GetHeroInfoDown.Builder b = GetHeroInfoDown.newBuilder();
		for (HeroEntity heroEntity : list) {
			HeroInfoBean.Builder heroInfoBean = HeroInfoBean.newBuilder();
			heroInfoBean.setId(heroEntity.heroId);
			heroInfoBean.setLevel(heroEntity.level);
			heroInfoBean.setExperience(heroEntity.experience);
			heroInfoBean.setQuality(heroEntity.quality);
			heroInfoBean.setStar(heroEntity.starLevel);
			if (heroEntity.trainState == HeroTrainStateEnum.Train_NoSave.value()) {
				heroInfoBean.setIsSave(false);
			} else {
				heroInfoBean.setIsSave(true);
			}
			heroInfoBean.setTrainType(heroEntity.trainType);
			heroInfoBean.addAllCurrentPatience(heroEntity.getPatienceTrainList());
			heroInfoBean.addAllWaitPatience(heroEntity.getWaitPatienceTrainList());

			// 装备
			List<EquipmentEntity> equipmentEntityList = edFunction.getEntityByHeroId(heroEntity.heroId);
			List<EquipmentInfoBean> infoList = new ArrayList<EquipmentInfoBean>();
			for (EquipmentEntity equipmentEntity : equipmentEntityList) {
				EquipmentInfoBean.Builder bean = EquipmentInfoBean.newBuilder();
				bean.setPositionId(equipmentEntity.positionId);
				bean.setLevel(equipmentEntity.equipmentLevel);
				bean.setQuality(equipmentEntity.equipmentQuality);
				int[] plug = equipmentEntity.getPIds();
				List<SlotInfoBean> slotList = new ArrayList<SlotInfoBean>();
				for (int i = 1; i <= plug.length; i++) {
					SlotInfoBean.Builder slotbean = SlotInfoBean.newBuilder();
					slotbean.setId(i);
					slotbean.setPlugId(plug[i - 1]);
					slotList.add(slotbean.build());
				}
				bean.addAllSlotInfoBean(slotList);
				infoList.add(bean.build());
			}
			heroInfoBean.addAllEquipmentInfoBean(infoList);
			b.addHeroInfoBean(heroInfoBean);
		}
		b.setUserId(String.valueOf(player.getUserId()));
		b.setHeroExp(heroExp);
		player.getSession().send(b.build());
	}
}
