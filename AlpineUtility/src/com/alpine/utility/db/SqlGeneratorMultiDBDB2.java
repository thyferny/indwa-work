/**
 * ClassName  SqlGeneratorMultiDBDB2.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;



public class SqlGeneratorMultiDBDB2 implements ISqlGeneratorMultiDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5714009684635753432L;

	@Override
	public String castToText(String columnName) {
		return "to_char("+columnName+")";
	}
	@Override
	public String castToDouble(String columnName){
		return "double("+columnName+")";
	}

	@Override
	public String setSeed(String seed, Integer para, Integer index) {
		StringBuilder sb = new StringBuilder("call dbms_random.seed(");
		sb.append(seed).append("+").append(index).append(")");
		return sb.toString();
	}
	
	@Override
	public String setCreateTableEndingSql(String distributeColumns) {	
		return "  DEFINITION ONLY ";
	}

	@Override
	public String insertTable(String sql,String decTable) {
		String str="insert into "+decTable+" ( "+sql+" )";
		return str;
	}


	@Override
	public String cascade() {
		return " ";
	}
	
	@Override
	public String dropTableIfExists(String tableName) {
		String schemaName = null;
		if(tableName.contains(".")){
			String[] temp=tableName.split("\\.",2);
			schemaName=temp[0];	
			tableName=temp[1];	
		}
		StringBuilder sb=new StringBuilder();
		sb.append("call PROC_DROPSCHTABLEIFEXISTS('").append(schemaName).append("','").append(tableName).append("')");
		return sb.toString();
	}
	
	@Override
	public String dropViewIfExists(String tableName) {
		String schemaName = null;
		if(tableName.contains(".")){
			String[] temp=tableName.split("\\.");
			schemaName=temp[0];	
			tableName=temp[1];	
		}
		StringBuilder sb=new StringBuilder();
		sb.append("call proc_dropviewifexists('").append(schemaName).append("','").append(tableName).append("')");
		return sb.toString();
	}
	@Override
	public String generateTempTableString() {
		return "create table ";
	}
	@Override
	public String rownumberOverByNull() {
		return " row_number() over (order by 1) ";
	}
	
	@Override
	public String to_date(String date) {

		return "to_date('"+date+"','MM/DD/YYYY HH24:MI:SS')";
	}
	
	@Override
	public String countTable(String tableName, String number) {
		String sql = "select count (*) from ( select * from " + tableName + " fetch first " + number+ " row only ) a ";
		return sql;
	}
	
	@Override
	public String textArray(){
		return "varchar2array";
	}
	
	@Override
	public String getTableSetType(String type) {
		return type;
	}
	@Override
	public String getStorageString(boolean isAppendOnly, boolean isColumnarStorage, boolean isCompression, int compressionLevel){
		return "";
	}
	
	@Override
	public String getCastDataType(String sql,String dataType) {
		if(dataType.equals(DB2SqlType.VARCHAR)
				||dataType.equals(DB2SqlType.LONGVARCHAR)
				||dataType.equals(DB2SqlType.CHAR)){
			return castToText(sql);
		}
		return sql;
	}
}
