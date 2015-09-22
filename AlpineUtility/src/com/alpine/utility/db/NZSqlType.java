package com.alpine.utility.db;

import java.util.Arrays;
import java.util.List;

public class NZSqlType extends GPSqlType {
	
	public static final DataSourceType INSTANCE = new NZSqlType(); 
	
	protected static final String DOUBLE = "DOUBLE";
	protected static final String NVARCHAR = "NVARCHAR";
	protected static final String NCHAR = "NCHAR";
	
	protected static final String[] All_Types = new String[] {
		GPSqlType.INTEGER,
		GPSqlType.SMALLINT,
		GPSqlType.BIGINT,
		GPSqlType.REAL,
		GPSqlType.FLOAT,
		NZSqlType.DOUBLE,
		GPSqlType.DOUBLE_PRECISION,
		GPSqlType.CHAR,
		NZSqlType.NVARCHAR,
		NZSqlType.NCHAR,
		GPSqlType.BOOLEAN,
		GPSqlType.NUMERIC,
		GPSqlType.DATE,
		GPSqlType.VARCHAR,
		GPSqlType.TIMESTAMP,
		GPSqlType.INTERVAL,
		GPSqlType.TIME,
		GPSqlType.TIME_WITH_TIME_ZONE,
		GPSqlType.CHARACTER_VARYING,
};
	
	public String getTextType() {
		return NZSqlType.NVARCHAR;
	}
	protected static String[] numberTypes = new String[]{
		GPSqlType.INT2,
		GPSqlType.INT4,
		GPSqlType.INT8,
		GPSqlType.FLOAT4,
		GPSqlType.FLOAT8,
		GPSqlType.SMALLINT,
		GPSqlType.INTEGER,
		GPSqlType.BIGINT,
		GPSqlType.REAL,
		GPSqlType.DOUBLE_PRECISION,			
		GPSqlType.NUMERIC,
		NZSqlType.DOUBLE};	
	
	public boolean isNumberColumnType(String type){
		//for aggregate, no type ,means true
		if(type==null){
			return true;
		}
		type=type.toUpperCase();
		 
		for(String s:numberTypes){
				if(type.startsWith(s))return true;
		} 
		return false;
	}
	
	public boolean isIntegerColumnType(String type){
		if(type==null){
			return true;
		}
		type=type.toUpperCase();

		for(String s:intTypes){
				if(type.startsWith(s))return true;
		} 
		return false;
	}
	
	protected NZSqlType() {
		
	}
	
	@Override
	public String[] getAllTypes() {
		return All_Types;
	}
	
	@Override
	public List<String> getOneArgTypes() {
		return Arrays.asList(new String[]{
				CHAR,
				VARCHAR,
				NVARCHAR,
				NCHAR,
				NUMERIC,
				FLOAT,
				DOUBLE,
		}) ;
	}
	@Override
	public List<String> getTwoArgTypes() {
		return Arrays.asList(new String[]{
				NUMERIC,
		}) ;
	}
}
