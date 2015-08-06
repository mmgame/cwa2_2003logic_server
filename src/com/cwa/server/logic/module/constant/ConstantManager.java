package com.cwa.server.logic.module.constant;


import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwa.component.prototype.IPrototypeClientService;
import com.cwa.constant.GameConstant;
import com.cwa.prototype.ConstantPrototype;
import com.cwa.service.IService;
import com.cwa.service.constant.ServiceConstant;
import com.cwa.service.context.IGloabalContext;

/**
 * 常亮管理
 * @author tzy
 *
 */
public class ConstantManager {
	private IGloabalContext gloabalContext;
	protected static final Logger logger = LoggerFactory.getLogger(ConstantManager.class);

	/**
	 * 初始化常量原型
	 */
	public void resetConstant() {
		IPrototypeClientService service=getprototypeManager();
		if (service==null) {
			logger.error("GameConstant init is error!PrototypeClientService is null");
			return;
		}
		// 常量数据不做缓存
		List<ConstantPrototype> constantPrototypeList = service.getAllPrototype(ConstantPrototype.class);
		try {
			Class<GameConstant> cls = GameConstant.class;
			for (ConstantPrototype constantPrototype : constantPrototypeList) {
				String attrName = constantPrototype.getAttributeName();

				Field field = cls.getField(attrName);
				if (field == null) {
					logger.error("GameConstant init is error! [" + attrName + "] field is null!");
				} else {
					// 赋值
					setFieldValue(field, constantPrototype.getValue());
				}
			}
		} catch (Exception e) {
			logger.error("GameConstant init is error!", e);
		}
	}

	public static void main(String args[]) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = GameConstant.class.getDeclaredFields();
		for (Field field : fields) {
			System.out.println(field.getName());
			setFieldValue(field, "11");
		}
		System.out.println(GameConstant.CONSUMEITEM_COUNT);
	}

	private static void setFieldValue(Field field, String value) throws IllegalArgumentException, IllegalAccessException {
		String fieldName = field.getName();
		Class fieldClass = field.getType();

		if (fieldClass == String.class) {
			field.set(field.getName(), value);
		} else if (fieldClass == Boolean.TYPE) { // boolean
			field.setBoolean(fieldName, Boolean.parseBoolean(value));
		} else if (fieldClass == Byte.TYPE) { // byte
			field.setByte(fieldName, Byte.parseByte(value));
		} else if (fieldClass == Short.TYPE) { // short
			field.setShort(fieldName, Short.parseShort(value));
		} else if (fieldClass == Integer.TYPE) { // int
			field.setInt(fieldName, Integer.parseInt(value));
		} else if (fieldClass == Long.TYPE) { // long
			field.setLong(fieldName, Long.parseLong(value));
		} else if (fieldClass == Float.TYPE) { // float
			field.setFloat(fieldName, Float.parseFloat(value));
		} else if (fieldClass == Double.TYPE) { // double
			field.setDouble(fieldName, Double.parseDouble(value));
		} else {
			logger.error("Type is not exist! fieldClass=" + fieldClass);
		}
	}

	public IPrototypeClientService getprototypeManager() {
		IService service = gloabalContext.getCurrentService(ServiceConstant.ProtoclientKey);
		return (IPrototypeClientService) service;
	}
	public void setGloabalContext(IGloabalContext gloabalContext) {
		this.gloabalContext = gloabalContext;
	}
}
