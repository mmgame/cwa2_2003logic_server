package com.cwa.server.logic.dataFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IFormationEntityDao;
import com.cwa.data.entity.domain.FormationEntity;
import com.cwa.data.entity.domain.UserinfoEntity;
import com.cwa.message.FormationMessage.FormationBean;
import com.cwa.message.FormationMessage.FormationInfoBean;
import com.cwa.prototype.FunctionPrototype;
import com.cwa.prototype.gameEnum.FunctionTypeEnum;
import com.cwa.server.logic.player.IPlayer;

/**
 * 阵容数据封装
 * 
 * @author mausmars
 *
 */
public class FormationDataFunction implements IDataFunction {
	// {阵容类型：HeroEntity}
	private Map<Integer, FormationEntity> entityMap = new HashMap<Integer, FormationEntity>();

	private IFormationEntityDao dao;
	private IPlayer player;
	private boolean isInited;

	public FormationDataFunction(IPlayer player) {
		this.player = player;
		IDBSession dbSession = player.getDataFunctionManager().getDbSession();
		dao = (IFormationEntityDao) dbSession.getEntityDao(FormationEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<FormationEntity> entitys = dao.selectEntityByUserId(player.getUserId(), createParams());
		for (FormationEntity e : entitys) {
			entityMap.put(e.type, e);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public void newCreateEntity() {

	}

	public FormationEntity getEntity(int formationType) {
		return entityMap.get(formationType);
	}

	public Collection<FormationEntity> getAllEntity() {
		return entityMap.values();
	}

	public List<Integer> getHeroList(int formationType) {
		FormationEntity entity = getEntity(formationType);
		return entity.getHerosList();
	}

	/**
	 * 验证阵容存在
	 * 
	 * @param formationType
	 * @return
	 */
	public boolean checkFormationExist(int formationType) {
		return getEntity(formationType) != null;
	}

	public boolean checkFormationInfo(List<FormationInfoBean> beans) {
		Set<Integer> heroIds = new HashSet<Integer>();

		boolean isRetinue = false;
		Iterator<FormationInfoBean> beanIt = beans.iterator();
		for (; beanIt.hasNext();) {
			FormationInfoBean bean = beanIt.next();

			int heroId = bean.getHeroId();
			if (heroId > 0) {
				if (heroIds.contains(heroId)) {
					return false;
				}
				heroIds.add(heroId);
			}
			int retinueId = bean.getRetinueId();
			if (retinueId > 0) {
				if (heroIds.contains(retinueId)) {
					return false;
				}
				heroIds.add(retinueId);
				isRetinue = true;
			}
		}
		if (isRetinue) {
			UserinfoDataFunction fdFunction = (UserinfoDataFunction) player.getDataFunctionManager().getDataFunction(UserinfoEntity.class);
			FunctionPrototype functionPrototype = player.getLogicContext().getprototypeManager()
					.getPrototype(FunctionPrototype.class, FunctionTypeEnum.Function_Retinue.value());
			int userLevel = fdFunction.getLevel();
			if (userLevel < functionPrototype.getLevelMin()) {
				// 等级不足开启侍从
				return false;
			}
		}
		return true;
	}

	public List<FormationBean.Builder> createFormationBean() {
		List<FormationBean.Builder> beans = new LinkedList<FormationBean.Builder>();
		for (FormationEntity entity : entityMap.values()) {
			FormationBean.Builder fbean = FormationBean.newBuilder();

			fbean.setFormationType(entity.type);

			Iterator<Integer> heroIt = entity.getHerosList().iterator();
			Iterator<Integer> retinueIt = entity.getRetinuesList().iterator();
			for (; heroIt.hasNext() && retinueIt.hasNext();) {
				FormationInfoBean.Builder fibean = FormationInfoBean.newBuilder();
				fibean.setHeroId(heroIt.next());
				fibean.setRetinueId(retinueIt.next());
				fbean.addFormationInfoBean(fibean);
			}
			beans.add(fbean);
		}
		return beans;
	}

	/**
	 * 重新设置阵容
	 * 
	 * @param formationType
	 * @param beans
	 */
	public void resetFormation(int formationType, List<FormationInfoBean> beans) {
		FormationEntity entity = getEntity(formationType);

		List<Integer> heroIdList = new ArrayList<Integer>(beans.size());
		List<Integer> retinueList = new ArrayList<Integer>(beans.size());
		for (FormationInfoBean bean : beans) {
			heroIdList.add(bean.getHeroId());
			retinueList.add(bean.getRetinueId());
		}
		entity.setHerosList(heroIdList);
		entity.setRetinuesList(retinueList);
	}

	private void updateEntity(FormationEntity entity) {
		dao.updateEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private void insertEntity(FormationEntity entity) {
		dao.insertEntity(entity, player.getDataFunctionManager().getDbSession().getParams(player.getRid()));
	}

	private Map<String, Object> createParams() {
		return player.getDataFunctionManager().getDbSession().getParams(player.getRid());
	}
}
