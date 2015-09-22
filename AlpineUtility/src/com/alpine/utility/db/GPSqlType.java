/**
 * ClassName GPSqlType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;



public class GPSqlType extends DataSourceType {

	protected static final String INT2 = "INT2";
	protected static final String INT4 = "INT4";
	protected static final String INT8 = "INT8";
	protected static final String INTEGER = "INTEGER";
	protected static final String SMALLINT = "SMALLINT";
	protected static final String BIGINT = "BIGINT";
	protected static final String FLOAT = "FLOAT";
	protected static final String FLOAT4 = "FLOAT4";
	protected static final String FLOAT8 = "FLOAT8";
	protected static final String REAL = "REAL";
	protected static final String DOUBLE_PRECISION = "DOUBLE PRECISION";
	protected static final String BPCHAR = "BPCHAR";
	protected static final String CHAR = "CHAR";
	protected static final String BOOL = "BOOL";
	protected static final String BOOLEAN = "BOOLEAN";
	protected static final String NUMERIC = "NUMERIC";
	protected static final String SERIAL = "SERIAL";
	protected static final String BIGSERIAL = "BIGSERIAL";
	protected static final String MONEY = "MONEY";
	protected static final String TEXT = "TEXT";
	protected static final String BYTEA = "BYTEA";
	protected static final String DATE = "DATE";
	protected static final String POINT = "POINT";
	protected static final String LINE = "LINE";
	protected static final String LSEG = "LSEG";
	protected static final String BOX = "BOX";
	protected static final String PATH = "PATH";
	protected static final String POLYGON = "POLYGON";
	protected static final String CIRCLE = "CIRCLE";
	protected static final String CIDR = "CIDR";
	protected static final String INET = "INET";
	protected static final String MACADDR = "MACADDR";
	protected static final String VARCHAR = "VARCHAR";
	protected static final String TIMESTAMP = "TIMESTAMP";
	protected static final String INTERVAL = "INTERVAL";
	protected static final String TIMESTAMPTZ = "TIMESTAMPTZ";
	protected static final String TIMESTAMP_WITH_TIME_ZONE = "TIMESTAMP WITH TIME ZONE";
	protected static final String TIMESTAMP_P_WITH_TIME_ZONE = "TIMESTAMP(%d) WITH TIME ZONE";
	protected static final String TIME = "TIME";
	protected static final String TIMETZ = "TIMETZ";
	protected static final String TIME_WITH_TIME_ZONE = "TIME WITH TIME ZONE";
	protected static final String TIME_P_WITH_TIME_ZONE = "TIME(%d) WITH TIME ZONE";
	protected static final String BIT = "BIT";
	protected static final String VARBIT = "VARBIT";
	protected static final String BIT_VARYING = "BIT VARYING";
	protected static final String CHARACTER = "CHARACTER";
	protected static final String CHARACTER_VARYING = "CHARACTER VARYING";
	public static final String ARRAY = "array";
	
	protected static final String[] All_Types = new String[] {
		GPSqlType.INTEGER,
		GPSqlType.SMALLINT,
		GPSqlType.BIGINT,
		GPSqlType.FLOAT,
		GPSqlType.FLOAT4,
		GPSqlType.FLOAT8,
		GPSqlType.REAL,
		GPSqlType.DOUBLE_PRECISION,
		GPSqlType.BPCHAR,
		GPSqlType.CHAR,
		GPSqlType.BOOLEAN,
		GPSqlType.NUMERIC,
		GPSqlType.BIGSERIAL,
		GPSqlType.MONEY,
		GPSqlType.TEXT,
		GPSqlType.BYTEA,
		GPSqlType.DATE,
		GPSqlType.POINT,
		GPSqlType.LINE,
		GPSqlType.LSEG,
		GPSqlType.BOX,
		GPSqlType.PATH,
		GPSqlType.POLYGON,
		GPSqlType.CIRCLE,
		GPSqlType.CIDR,
		GPSqlType.INET,
		GPSqlType.MACADDR,
		GPSqlType.VARCHAR,
		GPSqlType.TIMESTAMP,
		GPSqlType.INTERVAL,
		GPSqlType.TIMESTAMP_WITH_TIME_ZONE,
		GPSqlType.TIME,
		GPSqlType.TIME_WITH_TIME_ZONE,
		GPSqlType.BIT,
		GPSqlType.VARBIT,
		GPSqlType.BIT_VARYING,
		GPSqlType.CHARACTER_VARYING
};
	
	protected static final String[] Common_Types = new String[] {
			GPSqlType.BIGINT,
			GPSqlType.BOOLEAN,
			GPSqlType.BIT,
			GPSqlType.BIT_VARYING,
			GPSqlType.CHAR,
			GPSqlType.DATE,
			GPSqlType.FLOAT,
			GPSqlType.DOUBLE_PRECISION,
			GPSqlType.NUMERIC,
			GPSqlType.INTEGER,
			GPSqlType.VARCHAR
	};
	
	protected static final String[] Date_Types = new String[] {
		
		 DATE,
		
		TIME,
		TIMETZ,
		TIME_WITH_TIME_ZONE,
		TIME_P_WITH_TIME_ZONE,
		 
		TIMESTAMP,
		TIMESTAMPTZ,
		TIMESTAMP_WITH_TIME_ZONE,
		TIMESTAMP_P_WITH_TIME_ZONE
 
};

	protected static final String[] Time_Types = new String[] {
		TIME,
		TIMETZ,
		TIME_WITH_TIME_ZONE,
		TIME_P_WITH_TIME_ZONE,
 
};
	protected static final String[] DateTime_Types = new String[] {
		TIMESTAMP,
		TIMESTAMPTZ,
		TIMESTAMP_WITH_TIME_ZONE,
		TIMESTAMP_P_WITH_TIME_ZONE
 
};
	public static final DataSourceType INSTANCE = new GPSqlType(); 
	protected static String[] numberTypes = new String[]{
		GPSqlType.INT2,
		GPSqlType.INT4,
		GPSqlType.INT8,
		GPSqlType.FLOAT,
		GPSqlType.FLOAT4,
		GPSqlType.FLOAT8,
		GPSqlType.SMALLINT,
		GPSqlType.INTEGER,
		GPSqlType.BIGINT,
		GPSqlType.REAL,
		GPSqlType.DOUBLE_PRECISION,			
		GPSqlType.SERIAL,
		GPSqlType.BIGSERIAL,
		GPSqlType.MONEY,
		GPSqlType.NUMERIC};
	
	protected static String[] intTypes = new String[]{
		GPSqlType.INT2,
		GPSqlType.INT4,
		GPSqlType.INT8,
		GPSqlType.SMALLINT,
		GPSqlType.INTEGER,
		GPSqlType.BIGINT};
	
	
	public static String getTIMESTAMP_P_WITH_TIME_ZONE(int time){
		return "TIMESTAMP("+time+") WITH TIME ZONE";
	}
	public static String getTIME_P_WITH_TIME_ZONE(int time){
		return "TIME("+time+") WITH TIME ZONE";
	}
	public boolean isNumberColumnType(String type){
		//for aggregate, no type ,means true
		if(type==null){
			return true;
		}
		type=type.toUpperCase();
		 
		for(String s:numberTypes){
			if(type.startsWith(s))	{
				return true;
			}
		} 
		return false;
	}
	
	public boolean isIntegerColumnType(String type){
		if(type==null){
			return true;
		}
		type=type.toUpperCase();

		for(String s:intTypes){
				if(type.startsWith(s))	{
					return true;
					}
		} 
		return false;
	}
	protected GPSqlType() {
		
	}
	/**
	 * @param columnType
	 * @return
	 */
	public boolean isDateColumnType(String type) {
		//for aggregate, no type ,means true
		if(type==null){
			return true;
		}
		type=type.toUpperCase();

		for(String s:Date_Types){
				if(type.startsWith(s))return true;
		} 
		return false;
	}
	
	public boolean isTimeColumnType(String type) {
 
		if(type==null){
			return false;
		}
 
		for(String s:Time_Types){
				if(type.equalsIgnoreCase(s))return true;
		} 
		return false;
	}
	public boolean isDateTimeColumnType(String type) {
 
		if(type==null){
			return false;
		}

		for(String s:DateTime_Types){
				if(type.equalsIgnoreCase(s))return true;
		} 
		return false;
	}
	public boolean isPureDateColumnType(String type) {
		if(type==null){
			return false;
		}
		
		return GPSqlType.DATE.equalsIgnoreCase(type);
	}
	
	public String[] getCommonTypes() {
		return Common_Types;
	}
	
	public String getTextType() {
		return TEXT;
	}
	
	public String getIntegerType() {
		return INTEGER;
	}
	
	public String getIdType() {
		return BIGINT;
	}
	@Override
	public boolean isArrayArrayColumnType(String type) {
		return false;
	}
	@Override
	public boolean isArrayColumnType(String type) {
		if(type.equals(ARRAY)){
			return true;
		}else{
			return false;
		}
		
	}
	@Override
	public String getDoubleType() {
		return DOUBLE_PRECISION;
	}
	@Override
	public String[] getAllTypes() {
		return All_Types;
	}
	@Override
	public List<String> getOneArgTypes() {
		return Arrays.asList(new String[]{
				BIT,
				CHARACTER,
				CHARACTER_VARYING,
				NUMERIC,
				FLOAT,
		}) ;
	}
	@Override
	public List<String> getTwoArgTypes() {
		return Arrays.asList(new String[]{
				NUMERIC,
		}) ;
	}
	
	@Override
	public boolean isFloatColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);

		 
		if (type.startsWith(FLOAT)){
			return true;
		}
		else{
			return false;
		}
	}
	@Override
	public boolean isDoubleColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);

		 
		if (type.equalsIgnoreCase(DOUBLE_PRECISION)
				||type.equalsIgnoreCase(REAL)
				||type.equalsIgnoreCase(NUMERIC)){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public boolean isLongColumnType(String type) {
		return BIGINT.equals(type);
	}
}
