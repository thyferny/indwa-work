/**
 * ClassName GpDataTypeConverer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.HashMap;

public class DataTypeConverterUtil  {
	
	public static final int ORACLE_BINARY_DOUBLE = 101;
	
	public static DataTypeConverter createDataTypeConverter(String dbEngine) {
		if (dbEngine.equals(DataSourceInfoPostgres.dBType) || dbEngine.equals(DataSourceInfoGreenplum.dBType)) {
			return (new GpDataTypeConverter());
		} else if (dbEngine.equals(DataSourceInfoOracle.dBType)) {
			return (new OraDataTypeConverter());
		}else if (dbEngine.equals(DataSourceInfoDB2.dBType)) {
			return (new Db2DataTypeConverter());
		}else if (dbEngine.equals(DataSourceInfoNZ.dBType)) {
			return (new GpDataTypeConverter());
		} else {
			return null;
		}
		
	}

	private static HashMap<Integer,String> numberTypeHt = new HashMap<Integer,String>();
	static {
		numberTypeHt.put(java.sql.Types.INTEGER, "");
		numberTypeHt.put(java.sql.Types.TINYINT, "");
		numberTypeHt.put(java.sql.Types.SMALLINT, "");
		numberTypeHt.put(java.sql.Types.INTEGER, "");
		numberTypeHt.put(java.sql.Types.BIGINT, "");
		numberTypeHt.put(java.sql.Types.FLOAT, "");
		numberTypeHt.put(java.sql.Types.REAL, "");
		numberTypeHt.put(java.sql.Types.DOUBLE, "");
		numberTypeHt.put(java.sql.Types.DECIMAL, "");
		numberTypeHt.put(java.sql.Types.NUMERIC, "");
		numberTypeHt.put(ORACLE_BINARY_DOUBLE, "");
		// java.sql.Types.DATE
		// java.sql.Types.TIME,
		// java.sql.Types.TIMESTAMP
	};
	
	public static String getColumnType(int sqlType){
		if(numberTypeHt.containsKey(sqlType)){
			return numberType;
		}else{
			return textType;
		}
	}
	
	public final static String numberType = "INTEGER";
	public final static String textType = "TEXT";
	
	public static boolean isNumberType(String type) {
		return numberType.equals(type);
	}
}

