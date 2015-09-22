/**
 * ClassName Resources.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;


public class Resources {

	public static final String minerEdition="2.8";
	public static final String coptRight="Copyright Alpine Data Labs 2010-2012";
	public static final String[] DBEngineList = {
		DataSourceInfoPostgres.dBType, 
		DataSourceInfoOracle.dBType,
		DataSourceInfoDB2.dBType,
		DataSourceInfoNZ.dBType};//
	public static final String TableType = "TABLE";
	public static final String ViewType = "VIEW";
	public static final String TempTableType = "TEMP TABLE";
	public static final String UIType = "UI";
	public static final String SqlType = "SQL";
	public static final String YesOpt = "Yes";
	public static final String NoOpt = "No";
	public static final String TrueOpt = "true";
	public static final String FalseOpt = "false";
	public static final String RowType = "Row";
	public static final String PercentageType = "Percentage";
	public static final String SLASH = "/";
	
	public static final String[] YesNoOpts = new String[]{YesOpt,NoOpt};
	public static final String[] FilterTypes = new String[]{SqlType};
	public static final String[] OutputTypes = new String[]{TableType,ViewType};
	public static final String FieldSeparator = "&&";
	public static final String[] SampleSizeTypes = new String[]{RowType,PercentageType};
	

	public static final String[] JoinTypeList = {"JOIN","LEFT JOIN","RIGHT JOIN","FULL OUTER JOIN"};
	
}
