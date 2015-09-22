package com.alpine.utility.db;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.alpine.utility.file.StringUtil;

public class DB2SqlType extends DataSourceType {

	protected static final String INT = "INT";
	protected static final String INTEGER = "INTEGER";
	protected static final String SMALLINT = "SMALLINT";
	protected static final String BIGINT = "BIGINT";
	
	protected static final String DOUBLE = "DOUBLE";
	protected static final String DECIMAL = "DECIMAL";
	protected static final String DEC = "DEC";
	protected static final String REAL = "REAL";
	protected static final String NUMERIC = "NUMERIC";
	protected static final String NUM = "NUM";
	protected static final String DECFLOAT = "DECFLOAT";
	
	
	protected static final String CHAR = "CHAR";
	protected static final String VARCHAR = "VARCHAR";
	protected static final String LONGVARCHAR = "LONG VARCHAR";
	protected static final String CLOB = "CLOB";
	protected static final String GRAPHIC = "GRAPHIC";
	protected static final String VARGRAPHIC = "VARGRAPHIC";
	protected static final String LONGVARGRAPHIC = "LONG VARGRAPHIC";
	protected static final String DBCLOB = "DBCLOB";
	
	protected static final String BLOB = "BLOB";
	
	protected static final String DATE = "DATE";
	protected static final String TIME = "TIME";
	protected static final String TIMESTAMP = "TIMESTAMP";
	public static final DataSourceType INSTANCE = new DB2SqlType();
	
	private static List<String> numberTypes = Arrays.asList(new String[] {
			DOUBLE, DECIMAL, DEC,REAL, NUMERIC,NUM,DECFLOAT,INT, INTEGER, SMALLINT,BIGINT,});
	
	private static List<String> intTypes = Arrays.asList(new String[] {
			INT, INTEGER, SMALLINT,BIGINT,});
	
	private static List<String> common_Types = Arrays.asList(new String[] {
			INTEGER,BIGINT,CHAR,DOUBLE,NUMERIC,DATE,VARCHAR, 
			LONGVARCHAR,CLOB,GRAPHIC,VARGRAPHIC,});
	
	private static List<String> date_Types = Arrays.asList(new String[] {
			DATE, TIMESTAMP,});
	
	private static List<String> time_Types = Arrays.asList(new String[] {
			TIME, TIMESTAMP,});
	
	private static List<String> dateTime_Types = Arrays.asList(new String[] {
			TIMESTAMP,});
	
	protected static final String[] All_Types = new String[] {
		DB2SqlType.INTEGER,
		DB2SqlType.SMALLINT,
		DB2SqlType.BIGINT,
		DB2SqlType.REAL,
		DB2SqlType.DOUBLE,
		DB2SqlType.DEC,
		DB2SqlType.DECFLOAT,
		DB2SqlType.CHAR,
		DB2SqlType.NUMERIC,
		DB2SqlType.DATE,
		DB2SqlType.VARCHAR,
		DB2SqlType.LONGVARCHAR,
		DB2SqlType.CLOB,
		DB2SqlType.GRAPHIC ,
		DB2SqlType.VARGRAPHIC,
		DB2SqlType.LONGVARGRAPHIC,
		DB2SqlType.DBCLOB,
		DB2SqlType.BLOB,
		DB2SqlType.TIMESTAMP,
		DB2SqlType.TIME,
};
	
	private DB2SqlType(){
		
	}
	@Override
	public String[] getCommonTypes() {
		return common_Types.toArray(new String[common_Types.size()]);
	}

	@Override
	public String getIdType() {
		return BIGINT;
	}

	@Override
	public String getIntegerType() {
		return INTEGER;
	}

	@Override
	public String getTextType() {
		return VARCHAR;
	}

	@Override
	public boolean isArrayArrayColumnType(String type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isArrayColumnType(String type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDateColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		if(date_Types.contains(type)){
			return true;
		}
		return false;
	}

	@Override
	public boolean isDateTimeColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		if(dateTime_Types.contains(type)){
			return true;
		}
		return false;
	}

	@Override
	public boolean isIntegerColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);

		for(String s:intTypes){
			if (type.equalsIgnoreCase(s))
				return true;
		}
		type=type.toUpperCase();
		return false;
	}

	@Override
	public boolean isNumberColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		for(String s:numberTypes){
			if (type.equalsIgnoreCase(s))
				return true;
		}
		type=type.toUpperCase();

		if(type.startsWith("DECIMAL(")&&type.endsWith(")")){
			return true;
		}
		return false;
	}

	@Override
	public boolean isPureDateColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		if(!StringUtil.isEmpty(type)&&type.equalsIgnoreCase(DATE)){
			return true;
		}
		return false;
	}

	@Override
	public boolean isTimeColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		if(time_Types.contains(type)){
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
				CHAR,
				VARCHAR,
				LONGVARCHAR,
		}) ;
	}
	@Override
	public List<String> getTwoArgTypes() {
		return Arrays.asList(new String[]{
				DECIMAL,
				DEC,
				DECFLOAT,
				NUMERIC,
				NUM,
		}) ;
	}
	@Override
	public boolean isFloatColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);

		 
		if (type.equalsIgnoreCase(DECFLOAT)){
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

		if(type.startsWith("DECIMAL(")&&!type.endsWith(",0)")){
			return true;
		}
		 
		if (type.equalsIgnoreCase(DOUBLE)
			||type.equalsIgnoreCase(NUMERIC)	
			||type.equalsIgnoreCase(NUM)		
			||type.equalsIgnoreCase(REAL)		
			||type.equalsIgnoreCase(DECIMAL)		
			||type.equalsIgnoreCase(DEC)		
				){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public boolean isLongColumnType(String type) {
		if(type.startsWith("DECIMAL(")&&type.endsWith(",0)")){
			return true;
		}
		return BIGINT.equals(type);
	}
}

