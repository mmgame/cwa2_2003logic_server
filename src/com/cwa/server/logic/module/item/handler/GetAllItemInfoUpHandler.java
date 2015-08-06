package com.cwa.server.logic.module.item.handler;

import java.util.Collection;

import com.cwa.data.entity.domain.ItemEntity;
import com.cwa.message.ItemMessage.GetAllItemInfoDown;
import com.cwa.message.ItemMessage.GetAllItemInfoUp;
import com.cwa.message.ItemMessage.ItemInfoBean;
import com.cwa.server.logic.dataFunction.ItemDataFunction;
import com.cwa.server.logic.module.IGameHandler;
import com.cwa.server.logic.player.IPlayer;

/**
 * 得到玩家所有道具信息
 * 
 * @author tzy
 *
 */
public class GetAllItemInfoUpHandler extends IGameHandler<GetAllItemInfoUp> {
	@Override
	public void loginedHandler(GetAllItemInfoUp message, IPlayer player) {
		ItemDataFunction idFunction = (ItemDataFunction) player.getDataFunctionManager().getDataFunction(ItemEntity.class);

		sendGetAllItemInfoDownMessage(idFunction.getAllEntity(), player);
	}

	private void sendGetAllItemInfoDownMessage(Collection<ItemEntity> list, IPlayer player) {
		GetAllItemInfoDown.Builder b = GetAllItemInfoDown.newBuilder();
		for (ItemEntity itemEntity : list) {
			if (itemEntity.count <= 0) {
				continue;
			}
			ItemInfoBean.Builder itemInfoBean = ItemInfoBean.newBuilder();
			itemInfoBean.setCount(itemEntity.count);
			itemInfoBean.setId(itemEntity.itemId);
			b.addItemInfoBean(itemInfoBean);
		}
		player.getSession().send(b.build());
	}
}
