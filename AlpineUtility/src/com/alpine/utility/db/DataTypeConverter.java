/**
 * ClassName DataTypeConverter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.HashMap;

/**
 * the Interface for design class to convert JDBC data type to database specific data type.
 * @author Richie Lo
 *
 */
public abstract class DataTypeConverter {

	/**
	 * Given the JDBC data type info, return the database specific data type.
	 * @param typeName
	 * @param jdbcSqlType
	 * @param colSize
	 * @param decimal
	 * @return
	 */
	public abstract String getClause(String typeName, int jdbcSqlType, int colSize, int decimal);

	private static HashMap<Integer,String> doubleTypeHt = new HashMap<Integer,String>();
	static {
		doubleTypeHt.put(java.sql.Types.FLOAT, "");
		doubleTypeHt.put(java.sql.Types.REAL, "");
		doubleTypeHt.put(java.sql.Types.DOUBLE, "");
		doubleTypeHt.put(java.sql.Types.DECIMAL, "");
		doubleTypeHt.put(java.sql.Types.NUMERIC, "");
		doubleTypeHt.put(DataTypeConverterUtil.ORACLE_BINARY_DOUBLE, "");
	};
	
	public static boolean isDoubleType(int sqlType) {
		if(doubleTypeHt.containsKey(sqlType)){
			return true;
		}else{
			return false;
		}
	}
	public static boolean isMoneyType(String money){
		if(money.equals("money"))
		{
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isNumberColumnType(String type, String dataSystem){
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		if (stype == null) {
			return DataTypeConverterUtil.isNumberType(type);
		} else {
			return stype.isNumberColumnType(type);
		}
	}
	
	public static boolean isArrayArrayColumnType(String type, String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.isArrayArrayColumnType(type);
	}
	
	public static boolean isArrayColumnType(String type, String dataSystem) {
		DataSourceType stype = DataSourceType.getDataSourceType(dataSystem);
		return stype.isArrayColumnType(type);
	}
}
