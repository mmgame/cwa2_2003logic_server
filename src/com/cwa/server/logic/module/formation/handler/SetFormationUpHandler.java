package com.cwa.server.logic.module.formation.handler;

import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.message.FormationMessage.FormationInfoBean;
import com.cwa.message.FormationMessage.SetFormationUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.server.logic.dataFunction.FormationDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 设置出站阵容
 * 
 * @author tzy
 * 
 */
public class SetFormationUpHandler extends IGameHandler<SetFormationUp> {
	@Override
	public void loginedHandler(SetFormationUp message, IPlayer player) {
		int formationType = message.getFormationType();

		List<FormationInfoBean> beans = message.getFormationInfoBeanList();

		if (beans.size() != GameConstant.FIGHT_GROOVE_MAX_COUNT) {
			// 出战位不对
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Formation, message);
			return;
		}

		FormationDataFunction fdFunction = (FormationDataFunction) player.getDataFunctionManager().getDataFunction(FormationEntity.class);
		if (!fdFunction.checkFormationExist(formationType)) {
			// 阵容不存在
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Formation, message);
			return;
		}
		if (!fdFunction.checkFormationInfo(beans)) {
			// 验证阵容是否正确
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Formation, message);
			return;
		}
		// 重置阵容
		fdFunction.resetFormation(formationType, beans);
	}
}
