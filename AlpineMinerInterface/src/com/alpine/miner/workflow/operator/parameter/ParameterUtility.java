/**
 * ClassName :ParameterUtility.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.miner.workflow.operator.Operator;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.db.DataTypeConverterUtil;

/**
 * @author zhaoyong
 * 
 */
public class ParameterUtility {

	public static boolean nullableEquales(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		} else if (obj1 != null) {
			return obj1.equals(obj2);
		} else {
			return false;
		}
	}

	public static List cloneObjectList(List sourceList)
			throws CloneNotSupportedException {
		List<Object> targetList = null;
		if (sourceList != null) {
			targetList = new ArrayList();
			for (Iterator iterator = sourceList.iterator(); iterator.hasNext();) {
				Object item = (Object) iterator.next();
				if (item != null && item instanceof XMLFragment) {
					targetList.add(((XMLFragment) item).clone());
				}else if (item != null&&(item instanceof String||item instanceof Integer||item instanceof Float||item instanceof Double)){
					targetList.add( item);
				} 
				else {
					throw new CloneNotSupportedException();
				}

			}

		}
		return targetList;
	}

	// TODO:the folowing code is from UI and will move to dbmetadatautility
	// later
	public static boolean isDateColumnType(String type, String dataSystem) {

		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);

		return stype.isDateColumnType(type);

	}

	public static boolean isTimeColumnType(String type, String dataSystem) {

		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);

		return stype.isTimeColumnType(type);

	}

	public static boolean isDateTimeColumnType(String type, String dataSystem) {

		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);

		return stype.isDateTimeColumnType(type);

	}

	public static boolean isPureDateColumnType(String type, String dataSystem) {

		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);

		return stype.isPureDateColumnType(type);

	}

	public static boolean isNumberColumnType(String type, String dataSystem) {

		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);

		if (stype == null) {

			return DataTypeConverterUtil.isNumberType(type);

		} else {

			return stype.isNumberColumnType(type);

		}

	}

	public static boolean isIntegerColumnType(String type, String dataSystem) {

		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);

		return stype.isIntegerColumnType(type);

	}

	public static boolean isArrayType(String type, String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.isArrayColumnType(type);
	}
	
	public static boolean isArrayArrayType(String type, String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.isArrayArrayColumnType(type);
	}
	
	/**
	 * @param thisType
	 * @param dataSystem
	 * @return
	 */
	public static boolean isCategoryColumnType(String thisType,
			String dataSystem) {
		
		return(!isNumberColumnType(thisType, dataSystem)		 
				&&!isDateColumnType(thisType, dataSystem)		 
				&&!isDateTimeColumnType(thisType, dataSystem)		 
				&&!isPureDateColumnType(thisType, dataSystem)		 
				&&!isTimeColumnType(thisType, dataSystem)	
				&&!isArrayArrayType(thisType, dataSystem)
				&&!isArrayType(thisType, dataSystem));
		}
	public static String getIdType(String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.getIdType();
	}
	
	public static String getTextType(String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.getTextType();
	}
	
	public static String getIntegerType(String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.getIntegerType();
	}
	
	public static String getDoubleType(String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.getDoubleType();
	}
	
	public static OperatorParameter getParameterByName(Operator operator,
			String name) {
		List<OperatorParameter> params = operator.getOperatorParameterList();
		if (params != null) {
			for (Iterator iterator = params.iterator(); iterator.hasNext();) {
				OperatorParameter operatorParameter = (OperatorParameter) iterator
						.next();
				if (operatorParameter.getName().equals(name)) {
					return operatorParameter;
				}

			}
		}
		return null;
	}

	public static Object getParameterValue(Operator operator,String parameterName) {
		OperatorParameter parameter = getParameterByName(  operator, parameterName) ;
		if(parameter!=null){
			return parameter.getValue();
		}else{
			return null;
		}
		
	}
	public static int indexOf(String[] items, String firstTable) {
		if(items!=null){
			for(int i = 0 ;i<items.length;i++){
				if(items[i].equals(firstTable)){
					return i;
				}
			}
		} 
		return -1;
	}
}

