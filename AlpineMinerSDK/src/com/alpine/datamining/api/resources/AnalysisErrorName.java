/**
 * ClassName LanguagePack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.resources;

/**
 * 
 * @author John Zhao
 *
 */
public interface AnalysisErrorName {
 	
	public static final String ID_EMPTY = "ID_EMPTY";
	public static final String K_LESS_THEN_2 =  "K_EXCEEDS";
	public static final String DependentColumn_Empty = "DEPENDENT_EMPTY";
	public static final String Empty_TABLE_JOIN_DEFINITION = "NO_TABLE_JOIN";
	
	public static final String JDBC_DRIVER_NOT_FOUND = "JDBC_LOST";
	public static final String Database_connection_error= "DB_CONN_ERROR";
	public static final String Unsupported_database_type= "DB_NOT_SUPPORT";
	
	public static final String Non_numeric= "NON_NUM_COL";
	public static final String Not_null= "NULL_COLUMN";
	
	public static final String Histogram_not_same= "NUMBER_DIFF";
	public static final String N2t_notTable= "VIEW_MODIFIED";
	
	public static final String Illegal_parameter= "ILLEGAL_PARA";
	
	public static final String Drop_if_Exist= "DROP_TABLE_ERROR";
	
	public static final String Illegal_column= "ILLEGAL_COLUMN";
	
	public static final String Exceed_MAX_Value_Numbers= "VALUE_NUM_EXCEED";
	public static final String Exceed_MAX_Bin_NUMBER= "VALUE_BIN_EXCEED";
	public static final String Empty_table= "EMPTY_TABLE";
	public static final String Exceed_MAX_Bar_Numbers = "TOO_MANY_BARS";
	public static final String Table_doesnot_exists = "TABLE_NOT_EXIST";
	
	
	public static final String Exceed_MAX_TYPE_SIZE= "TYPE_NUM_EXCEED";
	
	public static final String Not_numeric= "NUM_COL";
	
	public static final String Too_Many_Distinct_value= "TOO_MANY_DIST_VALUES";
	
	public static final String Too_Many_SampleRowSize= "SAMPLE_ROW_SIZE_EXCEED";
	public static final String REPLACE_NULL_NOT_SAME= "REPLACE_NUM_DIFF";
	
	public static final String JOIN_DIFFERENT_TYPE= "JOIN_DIFFERENT_TYPE";
	public static final String JOIN_MORE_THAN_ONCE = "JOIN_MORE_THAN_ONCE";
	
	public static final String TABLE_NOT_EXIST= "TABLE_NOT_EXIST";
	
	public static final String Invalid_Identifier="INVALID_INDENTIFIER";
	
	public static final String HISTOGRAM_COUNT_NULL="HISTOGRAM_COUNT_NULL";
	
	public static final String VALUE_BOX_EXCEED= "VALUE_BOX_EXCEED";
	
	public static final String COPYTODB_DROPERROR= "COPYTODB_DROPERROR";
}

