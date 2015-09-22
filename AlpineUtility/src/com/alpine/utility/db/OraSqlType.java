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

public class OraSqlType extends DataSourceType {

	// Number Type
	protected static final String NUMBER = "NUMBER"; 
	protected static final String DEC = "DEC"; // fixed-point
	protected static final String DECIMAL = "DECIMAL";
	protected static final String NUMERIC = "NUMERIC";
	protected static final String DOUBLE = "DOUBLE";//floating-point
	protected static final String PRECISION = "PRECISION";
	protected static final String DOUBLE_PRECISION = "DOUBLE PRECISION";
	protected static final String FLOAT = "FLOAT";
	protected static final String BINARY_FLOAT = "BINARY_FLOAT";
	protected static final String BINARY_DOUBLE = "BINARY_DOUBLE";
	protected static final String INTEGER = "INTEGER";//integers
	protected static final String BIGINT = "BIGINT";//integers
	protected static final String INT  = "INT";
	protected static final String SMALLINT  = "SMALLINT";
	protected static final String SIMPLE_INTEGER  = "simple_integer";
	protected static final String BOOLEAN = "BOOLEAN";
	protected static final String REAL  = "REAL";
	
	protected static final String BINARY_INTEGER ="BINARY_INTEGER";
	protected static final String NATURAL ="NATURAL";
	protected static final String NATURALN ="NATURALN";
	protected static final String POSITIVE ="POSITIVE";
	protected static final String POSITIVEN ="POSITIVEN";
	protected static final String SIGNTYPE ="SIGNTYPE";
	
	protected static final String TEXT = "VARCHAR2"; // change for Oracle
	protected static final String STRING = "STRING";
	protected static final String CHARACTER ="CHARACTER";
	protected static final String VARCHAR2 = "VARCHAR2";
	protected static final String CHAR = "CHAR";
	protected static final String NVARCHAR2 = "NVARCHAR2";
	protected static final String NCHAR = "NCHAR";
	protected static final String DATE = "DATE";
	protected static final String TIMESTAMP = "TIMESTAMP";
	protected static final String TIMESTAMP_WITH_LOCAL_TIMEZONE = "TIMESTAMP_WITH_LOCAL_TIME_ZONE";
	protected static final String TIMESTAMP_WITH_TIMEZONE = "TIMESTAMP_WITH_TIME_ZONE";
	protected static final String CLOB = "CLOB";
	protected static final String NLOB = "NLOB";

	protected static final String ROWID = "ROWID";
	protected static final String UROWID = "UROWID";
	
	protected static final String ARRAY = "VARRAY";
	protected static final String FLOATARRAY = "FLOATARRAY";
	protected static final String INTEGERARRAY = "INTEGERARRAY";
	protected static final String VARCHAR2ARRAY = "VARCHAR2ARRAY";
	public static final String FLOATARRAYARRAY = "FLOATARRAYARRAY";
	protected static final String INTEGERARRAYARRAY = "INTEGERARRAYARRAY";
	protected static final String VARCHAR2ARRAYARRAY = "VARCHAR2ARRAYARRAY";

	protected static final String[] Common_Types = new String[] {
			OraSqlType.CLOB, OraSqlType.NLOB, OraSqlType.CHAR, OraSqlType.DATE,
			OraSqlType.BINARY_DOUBLE, OraSqlType.INTEGER,OraSqlType.NUMBER, OraSqlType.TIMESTAMP,
			OraSqlType.VARCHAR2, OraSqlType.NVARCHAR2 };

	protected static final String[] Date_Types = new String[] {
			OraSqlType.DATE, OraSqlType.TIMESTAMP };

	protected static final String[] Time_Types = new String[] { TIMESTAMP,
			TIMESTAMP_WITH_TIMEZONE, TIMESTAMP_WITH_LOCAL_TIMEZONE };

	protected static final String[] DateTime_Types = new String[] { TIMESTAMP,
			TIMESTAMP_WITH_TIMEZONE, TIMESTAMP_WITH_LOCAL_TIMEZONE,OraSqlType.DATE };

	protected static String[] numberTypes = new String[] { 
			OraSqlType.NUMBER,
			OraSqlType.DEC,
			OraSqlType.DECIMAL,
			OraSqlType.NUMERIC,
			OraSqlType.DOUBLE,
			OraSqlType.PRECISION,
			OraSqlType.DOUBLE_PRECISION,
			OraSqlType.FLOAT,
			OraSqlType.INTEGER,
			OraSqlType.INT,
			OraSqlType.SMALLINT,
			OraSqlType.SIMPLE_INTEGER,
			OraSqlType.BIGINT,
			OraSqlType.REAL,
			OraSqlType.BINARY_DOUBLE, 
			OraSqlType.BINARY_FLOAT,
			OraSqlType.BINARY_INTEGER,
			OraSqlType.NATURAL,
			OraSqlType.NATURALN,
			OraSqlType.POSITIVE,
			OraSqlType.POSITIVEN,
			OraSqlType.SIGNTYPE};
	
	protected static String[] intTypes = new String[]{
		OraSqlType.INTEGER,
		OraSqlType.INT,
		OraSqlType.SMALLINT,
		OraSqlType.SIMPLE_INTEGER,OraSqlType.BIGINT};
	protected static final String[] arrayArray_Types = new String[] { FLOATARRAYARRAY,
		INTEGERARRAYARRAY, VARCHAR2ARRAYARRAY};
	
	protected static final String[] All_Types = new String[] {
		OraSqlType.INTEGER,
		OraSqlType.SMALLINT,
		OraSqlType.BIGINT,
		OraSqlType.REAL,
		OraSqlType.DOUBLE,
		OraSqlType.DEC,
		OraSqlType.CHAR,
		OraSqlType.NUMERIC,
		OraSqlType.DATE,
		OraSqlType.CLOB,
		OraSqlType.TIMESTAMP,
		OraSqlType.TIMESTAMP_WITH_LOCAL_TIMEZONE,
		OraSqlType.ROWID,
		OraSqlType.UROWID,
		OraSqlType.CLOB,
		OraSqlType.NLOB,
		OraSqlType.TEXT,
		OraSqlType.STRING,
		OraSqlType.CHARACTER,
		OraSqlType.VARCHAR2,
		OraSqlType.NVARCHAR2,
};
	
	
	protected static final String[] array_Types = new String[] { FLOATARRAY,INTEGERARRAY,VARCHAR2ARRAY};
	public static final DataSourceType INSTANCE = new OraSqlType();

	public boolean isNumberColumnType(String type) {
		// for aggregate, no type ,means true
		if (type == null) {
			return true;
		}

		for (String s : numberTypes) {
			if (type.equalsIgnoreCase(s))
				return true;
		}
		type=type.toUpperCase();

		if(type.startsWith("NUMBER(")&&type.endsWith(")")){
			return true;
		}
		return false;
	}
	public boolean isIntegerColumnType(String type){
		if(type==null){
			return true;
		}
		 
		for(String s:intTypes){
				if(type.equalsIgnoreCase(s))return true;
		} 
		type=type.toUpperCase();
		
		return false;
	}
	private OraSqlType() {

	}

	/**
	 * @param columnType
	 * @return
	 */
	public boolean isDateColumnType(String type) {
		// for aggregate, no type ,means true
		if (type == null) {
			return true;
		}
		type=type.toUpperCase();

		for (String s : Date_Types) {
			if (type.startsWith(s))
				return true;
		}
		return false;
	}

	public boolean isTimeColumnType(String type) {

		if (type == null) {
			return false;
		}

		for (String s : Time_Types) {
			if (type.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

	public boolean isDateTimeColumnType(String type) {

		if (type == null) {
			return false;
		}
		for (String s : DateTime_Types) {
			if (type.equalsIgnoreCase(s))
			{
				return true;
			}
			String[] temp=type.split("\\(");
			if(temp[0].equals(s))
			{
				return true;
			}	
		}
		return false;
	}

	public boolean isPureDateColumnType(String type) {
		if (type == null) {
			return false;
		}
		return false;
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
		return NUMBER;
	}
	@Override
	public boolean isArrayArrayColumnType(String type) {
		if (type == null) {
			return false;
		}

		for (String s : arrayArray_Types) {
			if (type.endsWith(s))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isArrayColumnType(String type) {
		if (type == null) {
			return false;
		}

		for (String s : array_Types) {
			if (type.endsWith(s))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDoubleType() {
		return DOUBLE;
	}
	@Override
	public String[] getAllTypes() {
		return All_Types;
	}
	@Override
	public List<String> getOneArgTypes() {
		return Arrays.asList(new String[]{
				NVARCHAR2,
				VARCHAR2,
				FLOAT,
		}) ;
	}
	@Override
	public List<String> getTwoArgTypes() {
		return Arrays.asList(new String[]{
				NUMBER,
				DECIMAL,
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
		if(type.startsWith("NUMBER(")&&!type.endsWith(",0)")){
			return true;
		}
		
		if (type.equalsIgnoreCase(DOUBLE_PRECISION)
				||type.equalsIgnoreCase(DOUBLE)
				||type.equalsIgnoreCase(NUMERIC)
				||type.equalsIgnoreCase(REAL)){
			return true;
		}
		else{
			return false;
		}
	}
	@Override
	public boolean isLongColumnType(String type) {
		if(type.startsWith("NUMBER(")&&type.endsWith(",0)")){
			return true;
		}
		return BIGINT.equals(type);
	}
}
