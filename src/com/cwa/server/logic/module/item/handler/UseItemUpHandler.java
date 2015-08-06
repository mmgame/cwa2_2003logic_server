package com.cwa.server.logic.module.item.handler;

import java.util.ArrayList;
import java.util.List;

import com.cwa.constant.GameConstant;
import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.ItemMessage.ItemInfoBean;
import com.cwa.message.ItemMessage.UseItemDown;
import com.cwa.message.ItemMessage.UseItemUp;
import com.cwa.message.LErrorMessage.ModuleTypeEnum;
import com.cwa.prototype.ItemPrototype;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.dataFunction.UserinfoDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 开宝箱（使用一个道具）
 * 
 * @author tzy
 * 
 */
public class UseItemUpHandler extends IGameHandler<UseItemUp> {
	@Override
	public void loginedHandler(UseItemUp message, IPlayer player) {
		int itemKeyId = message.getItemId();

		UserinfoDataFunction udFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		ItemPrototype itemPrototype = logicContext.getprototypeManager().getPrototype(ItemPrototype.class, itemKeyId);

		if (!udFunction.checkUserLevel(itemPrototype.getRequireRoleLevel())) {
			// 玩家道具使用等级不足
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_User, message);
			return;
		}

		if (!idFunction.checkItemCount(itemKeyId, GameConstant.CONSUMEITEM_COUNT)) {
			// 道具不足，数据错误
			logicContext.getSyncManager().sendDataError(player.getSession(), ModuleTypeEnum.MT_Item, message);
			return;
		}

		List<Object> itemInfos = idFunction.useChests(itemKeyId);

		// 扣除道具
		idFunction.modifyItemCount(itemKeyId, GameConstant.CONSUMEITEM_COUNT);

		List<ItemInfoBean> itemInfoBeanList = new ArrayList<ItemInfoBean>();
		for (Object i : itemInfos) {
			ItemInfoBean.Builder useItemInfoBean = ItemInfoBean.newBuilder();
			int[] itemInfo = (int[]) i;
			useItemInfoBean.setId(itemInfo[0]);
			useItemInfoBean.setCount(itemInfo[1]);
			// 修改开出的道具数量
//			idFunction.modifyItemCount(itemInfo[0], itemInfo[1]);
			itemInfoBeanList.add(useItemInfoBean.build());
			logicContext.getUpdateManager().drop(itemInfo[0], itemInfo[1], player);
			
		}
		sendUseItemDownMessage(itemInfoBeanList, player);
	}

	private void sendUseItemDownMessage(List<ItemInfoBean> itemInfoBeanList, IPlayer player) {
		UseItemDown.Builder b = UseItemDown.newBuilder();
		b.addAllItemInfoBean(itemInfoBeanList);
		player.getSession().send(b.build());
	}
}
