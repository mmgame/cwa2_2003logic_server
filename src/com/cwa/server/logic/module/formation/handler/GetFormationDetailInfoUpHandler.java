package com.cwa.server.logic.module.formation.handler;

import java.util.ArrayList;
import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.data.entity.domain.HeroEntity;
import com.cwa.message.FormationMessage.FormationDetailInfoBean;
import com.cwa.message.FormationMessage.GetFormationDetailInfoDown;
import com.cwa.message.FormationMessage.GetFormationDetailInfoUp;
import com.cwa.message.LErrorMessage.InputErrorTypeEnum;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.gameEnum.FormationTypeEnum;
import com.cwa.server.logic.dataFunction.FormationDataFunction;
import com.cwa.server.logic.dataFunction.HeroDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 查看别人防守阵容
 * 
 * @author tzy
 * 
 */
public class GetFormationDetailInfoUpHandler extends IGameHandler<GetFormationDetailInfoUp> {
	@Override
	public void loginedHandler(GetFormationDetailInfoUp message, IPlayer player) {
		int formationType = message.getFormationType();

		if (FormationTypeEnum.getEnum(formationType - 1) == null) {
			// 没有对应类型
			logicContext.getSyncManager().sendInputError(player.getSession(), InputErrorTypeEnum.IE_FormationType, message);
			return;
		}

		FormationDataFunction fdFunction = (FormationDataFunction) player.getDataFunctionManager().getDataFunction(FormationEntity.class);
		HeroDataFunction hdFunction = (HeroDataFunction) player.getDataFunctionManager().getDataFunction(HeroEntity.class);

		if (!fdFunction.checkFormationExist(formationType)) {
			// 阵容不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Formation, message);
			return;
		}

		List<Integer> heroList = fdFunction.getHeroList(formationType);
		List<FormationDetailInfoBean> formationDetailInfoBeanList = new ArrayList<FormationDetailInfoBean>();
		for (int i = 0; i < GameConstant.CONSUMEITEM_COUNT; i++) {
			FormationDetailInfoBean.Builder bean = FormationDetailInfoBean.newBuilder();
			bean.setHeroInfoBean(hdFunction.createHeroInfoBean(heroList.get(i)));
			formationDetailInfoBeanList.add(bean.build());
		}
		sendGetFormationDetailInfoDownMessage(formationDetailInfoBeanList, player);
	}

	public void sendGetFormationDetailInfoDownMessage(List<FormationDetailInfoBean> formationDetailInfoBeanList, IPlayer player) {
		GetFormationDetailInfoDown.Builder b = GetFormationDetailInfoDown.newBuilder();
		b.addAllFormationDetailInfoBean(formationDetailInfoBeanList);
		player.getSession().send(b.build());
	}
}
