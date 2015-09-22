/**
 * ClassName DataSourceType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.List;

import com.alpine.utility.hadoop.HadoopDataType;


public abstract class DataSourceType {
	public static final String SQL_SOURCE = "SQL";
	public static final String TEXT_SOURCE = "CSV";
	public abstract boolean isNumberColumnType(String type);
	public abstract boolean isDateColumnType(String type); 
	public abstract boolean isTimeColumnType(String type); 
	public abstract boolean isDateTimeColumnType(String type); 
	public abstract boolean isPureDateColumnType(String type); 
	public abstract boolean isIntegerColumnType(String type); 
	public abstract boolean isLongColumnType(String type); 
	public abstract boolean isArrayColumnType(String type); 
	public abstract boolean isArrayArrayColumnType(String type); 
	public abstract String getTextType();
	public abstract String getIntegerType();
	public abstract String[] getCommonTypes();
	public abstract String getIdType();
	public abstract String getDoubleType();
	public abstract String[] getAllTypes();
	public abstract List<String> getOneArgTypes();
	public abstract List<String> getTwoArgTypes();
	
	public static DataSourceType getDataSourceType(String source) {
		if (DataSourceInfoPostgres.dBType.equals(source) || DataSourceInfoGreenplum.dBType.equals(source)) {
			return (GPSqlType.INSTANCE);
		} else if (DataSourceInfoOracle.dBType.equals(source)) {
			return (OraSqlType.INSTANCE);
		} else if (DataSourceInfoDB2.dBType.equals(source)) {
			return (DB2SqlType.INSTANCE);
		}else if (DataSourceInfoNZ.dBType.equals(source)) {
			return (NZSqlType.INSTANCE);
		}else if (HadoopDataType.HADOOP.equalsIgnoreCase(source)) {
			return (HadoopDataType.INSTANCE);
		}else {
			return null;
		}
	}
	public abstract boolean isFloatColumnType(String columnsType);
	public abstract boolean isDoubleColumnType(String columnsType) ;
	

}
