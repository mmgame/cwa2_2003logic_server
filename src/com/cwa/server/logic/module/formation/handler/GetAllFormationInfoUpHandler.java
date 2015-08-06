package com.cwa.server.logic.module.formation.handler;

/**
 * 得到用户的阵容信息
 * 
 * @author tzy
 * 
 */
import java.util.List;

import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.message.FormationMessage.FormationBean;
import com.cwa.message.FormationMessage.GetAllFormationInfoDown;
import com.cwa.message.FormationMessage.GetAllFormationInfoUp;
import com.cwa.server.logic.dataFunction.FormationDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 得到全部阵容
 * 
 * @author mausmars
 *
 */
public class GetAllFormationInfoUpHandler extends IGameHandler<GetAllFormationInfoUp> {
	@Override
	public void loginedHandler(GetAllFormationInfoUp message, IPlayer player) {
		FormationDataFunction fdFunction = (FormationDataFunction) player.getDataFunctionManager().getDataFunction(FormationEntity.class);

		List<FormationBean.Builder> beans = fdFunction.createFormationBean();
		GetAllFormationInfoDown.Builder b = GetAllFormationInfoDown.newBuilder();
		for (FormationBean.Builder bean : beans) {
			b.addFormationBean(bean);
		}
		player.getSession().send(b.build());
	}
}
