package com.cwa.server.logic.module.room.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import serverice.room.IRoomServicePrx;
import baseice.basedao.IEntity;

import com.cwa.data.entity.domain.EquipmentEntity;
import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.message.RoomMessage.FightHeroUp;
import com.cwa.message.RoomMessage.FormationInfoBean;
import com.cwa.server.logic.dataFunction.EquipmentDataFunction;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

public class FightHeroUpHandler extends IGameHandler<FightHeroUp> {

	@Override
	public void loginedHandler(FightHeroUp message, IPlayer player) {
		List<FormationInfoBean> formationInfoBeanList = message.getFormationInfoBeanList();
		List<IEntity> heroEntitys = new ArrayList<IEntity>();
		FormationEntity formationEntity = new FormationEntity();
		Map<Integer, List<IEntity>> equipmentEntityMap = new HashMap<Integer, List<IEntity>>();
		StringBuilder heroId = new StringBuilder();
		StringBuilder retinueId = new StringBuilder();
		long userId = player.getUserId();
		formationEntity.userId = userId;
		IRoomServicePrx prx = player.getLogicContext().getRoomManager().getRoomPrx(player.getLogicContext());
		if (prx == null) {
			logicContext.getSyncManager().sendSystemError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
		
		for(FormationInfoBean bean : formationInfoBeanList){
			heroId.append(bean.getHeroId());
			heroId.append(",");
			retinueId.append(bean.getRetinueId());
			retinueId.append(",");
			heroEntitys.add(getHeroEntity(bean.getHeroId(),player));
			heroEntitys.add(getHeroEntity(bean.getRetinueId(),player));
			equipmentEntityMap.put(bean.getHeroId(), getHeroEquipment(bean.getHeroId(),player));
			equipmentEntityMap.put(bean.getRetinueId(), getHeroEquipment(bean.getRetinueId(),player));
		}
		heroId.deleteCharAt(heroId.lastIndexOf(","));
		retinueId.deleteCharAt(retinueId.lastIndexOf(","));
		formationEntity.heros = heroId.toString();
		formationEntity.retinues = retinueId.toString();
		boolean isSusscess = prx.replaceHero(userId, heroEntitys, formationEntity, equipmentEntityMap);
		if (!isSusscess) {
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Room, message);
			return;
		}
	}
	
	private IEntity getHeroEntity(int heroId, IPlayer player){
		if(heroId == 0){
			return null;
		}
		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);
		return hdFunction.getEntity(heroId);
	}
	
	private List<IEntity> getHeroEquipment(int heroId, IPlayer player){
		if(heroId == 0){
			return null;
		}
		EquipmentDataFunction equipmentDataFunction = (EquipmentDataFunction) player.getDataFunctionManager().getDataFunction(EquipmentEntity.class);
		List<IEntity> equipmentEntityList = new ArrayList<IEntity>();
		for(EquipmentEntity entity : equipmentDataFunction.getEntityByHeroId(heroId)){
			equipmentEntityList.add(entity);
		}
		return equipmentEntityList;
	}
}
