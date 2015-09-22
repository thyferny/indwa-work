/**
 * ClassName HadoopDataType.java
 *
 * Version information:1.00
 *
 * Date:Jun 11, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.hadoop;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.alpine.utility.db.DataSourceType;


public class HadoopDataType  extends DataSourceType {

	
	public static final String HADOOP = "Hadoop";
	
	//this is old definition --never used, but need support the old flow
	public static final String INTEGER_OLD = "Int";
	public static final String FLOAT_OLD = "Float";
	public static final String NUMERIC_OLD  = "Numeric";
	public static final String TEXT_OLD = "Text";

	//following is real hadoop data types
	public static final String INT = "int";
	public static final String LONG = "long" ;
	public static final String FLOAT = "float" ;

	public static final String DOUBLE = "double" ;
	public static final String CHARARRAY = "chararray";	
	public static final String BYTEARRAY = "bytearray";
	
	public static final String[] ALL_TYPES = new String[]{
		INT , LONG, FLOAT , DOUBLE , CHARARRAY ,BYTEARRAY
	};

	
  	public static final DataSourceType INSTANCE = new HadoopDataType();
	
	private static List<String> numberTypesInUpperCase = Arrays.asList(new String[] {
			FLOAT_OLD.toUpperCase() , 
			NUMERIC_OLD.toUpperCase(),  
			INTEGER_OLD,INT.toUpperCase(),
			LONG.toUpperCase(),
			FLOAT.toUpperCase(),
			DOUBLE.toUpperCase()
			});
	
	private static List<String> intTypes = Arrays.asList(new String[] {
			INTEGER_OLD,INT,LONG});
 
	
	private static List<String> date_Types = Arrays.asList(new String[] {
		});
	
	private static List<String> time_Types = Arrays.asList(new String[] {
			});
	
	private static List<String> dateTime_Types = Arrays.asList(new String[] {
			  });
	
 
 
	
	private HadoopDataType(){
		
	}
	@Override
	public String[] getCommonTypes() {
		return ALL_TYPES;
	}

	@Override
	public String getIdType() {
		return INTEGER_OLD;
	}

	@Override
	public String getIntegerType() {
		return INTEGER_OLD;
	}

	@Override
	public String getTextType() {
		return TEXT_OLD;
	}

	@Override
	public boolean isArrayArrayColumnType(String type) {
		return false;
	}

	@Override
	public boolean isArrayColumnType(String type) {
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

		if(type.startsWith("DECIMAL(")&&type.endsWith(",0)")){
			return true;
		}
		return false;
	}

	@Override
	public boolean isNumberColumnType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		if(numberTypesInUpperCase.contains(type)){
				return true;
		}
		
		if(type.startsWith("DECIMAL(")&&type.endsWith(")")){
			return true;
		}
		return false;
	}

	@Override
	public boolean isPureDateColumnType(String type) {
		//pig has no date type ...
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
		return ALL_TYPES;
	}
	@Override
	public List<String> getOneArgTypes() {
		return Arrays.asList(new String[]{
				 
		}) ;
	}
	@Override
	public List<String> getTwoArgTypes() {
		return Arrays.asList(new String[]{
		 
		}) ;
	}
	 

 
	
	public static String[] getDataTypes(){
		return ALL_TYPES;
	}
	
	public static String getTransferDataType(String originDataType){
		 String formatDataType="";
		 if(originDataType.equalsIgnoreCase(INTEGER_OLD)){
		  formatDataType = INT;
		 }else if(originDataType.equalsIgnoreCase(TEXT_OLD)){
		  formatDataType = CHARARRAY ;
		 }else if(originDataType.equalsIgnoreCase(FLOAT_OLD)){
		  formatDataType = DOUBLE;
		 }else if(originDataType.equalsIgnoreCase(NUMERIC_OLD)){
		  formatDataType = DOUBLE;
		 }else{
			 formatDataType=originDataType;
		 }
		 return formatDataType;
		}
	
	//can be join and union
	public static boolean isSimilarType(String sourcdeType,String targetType){
		if(isNumberType(sourcdeType) ==true 
				&&isNumberType(targetType) ==true)	{
			return true;
		}else if(sourcdeType!=null)		{
			return sourcdeType.equalsIgnoreCase(targetType) ;
		}
		return false;
		
	}
	
 
	public static boolean isNumberType(String type) {
		if(type==null){
			return false;
		}
		type=type.toUpperCase(Locale.ENGLISH);
		
		if(numberTypesInUpperCase.contains(type)){
			return true;
		}

		if(type.startsWith("DECIMAL(")&&type.endsWith(")")){
			return true;
		}
		return false;
	}

	public  static boolean isSimilarType4List(List<String> inputColumnTypeList) { 
		if(inputColumnTypeList!=null&&inputColumnTypeList.size()>0){ 
			String baseType = inputColumnTypeList.get(0);
			//boolean isNumber = HadoopDataType.isNumberType(baseType)||HadoopDataType.BYTEARRAY.equals(baseType);
			
			for (String columnType : inputColumnTypeList) {
				if(HadoopDataType.isNumberType(baseType)==true&&HadoopDataType.CHARARRAY.equals(columnType)==true){
					return false;
				}else if (HadoopDataType.CHARARRAY.equals(baseType)==true&&HadoopDataType.isNumberType(columnType)==true){
					return false;
				}
				baseType = columnType;
			}
		}
		else{
			return false;
		}
		return true;
	 
	}
	
	public  static String guessHadoopDataType(String dbSystem,String dbColumnType) {
		DataSourceType dataType = DataSourceType.getDataSourceType(dbSystem);
		if(dataType!=null){
			if(dataType.isLongColumnType(dbColumnType)){
				return HadoopDataType.LONG;
			}
			else if(dataType.isIntegerColumnType(dbColumnType)){
				return HadoopDataType.INT;
			}else if(dataType.isNumberColumnType(dbColumnType)){
				return HadoopDataType.DOUBLE;
			}
 		}
		return HadoopDataType.CHARARRAY;
	}

	public static String findMaxHadoopDataType(List<String> columnTypeList){
		if(columnTypeList!=null){
			if(columnTypeList.contains(DOUBLE)){
				if(columnTypeList.contains(CHARARRAY)){
					return null;
				}
				return HadoopDataType.DOUBLE;
			}else if(columnTypeList.contains(FLOAT)){
				if(columnTypeList.contains(CHARARRAY)){
					return null;
				}
				return HadoopDataType.FLOAT;
			}else if(columnTypeList.contains(LONG)){
				if(columnTypeList.contains(CHARARRAY)){
					return null;
				}
				return HadoopDataType.LONG;
			}else if(columnTypeList.contains(INT)){
				if(columnTypeList.contains(CHARARRAY)){
					return null;
				}
				return HadoopDataType.INT;
			}else if(columnTypeList.contains(BYTEARRAY)){
				return HadoopDataType.BYTEARRAY;
			}
			
			if(columnTypeList.contains(CHARARRAY)){
				return HadoopDataType.CHARARRAY;
			}else if(columnTypeList.contains(BYTEARRAY)){
				return HadoopDataType.BYTEARRAY;
			}
		}
		return null;
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
		 
		if ( type.equalsIgnoreCase(DOUBLE) ){
			return true;
		}
		else{
			return false;
		}
	}
	@Override
	public boolean isLongColumnType(String type) {
		 
		return LONG.equalsIgnoreCase(type);
	}
}
