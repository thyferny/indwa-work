/**
 * ClassName  SqlGeneratorMultiDBOracle.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import com.alpine.utility.tools.StringHandler;

public class SqlGeneratorMultiDBOracle implements ISqlGeneratorMultiDB {

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
		return "(1.0*("+columnName+"))";
	}

	@Override
	public String setSeed(String seed, Integer para, Integer index) {
		StringBuilder sb = new StringBuilder("call dbms_random.seed(");
		sb.append(seed).append("+").append(index).append(")");
		return sb.toString();
	}
	
	@Override
	public String setCreateTableEndingSql(String distributeColumns) {
		return "";
	}



	@Override
	public String cascade() {
		return "CASCADE constraints";
	}
	
	@Override
	public String dropTableIfExists(String tableName) {
		if(tableName.contains(".")){
			String[] temp=tableName.split("\\.");
			tableName=StringHandler.removeDoubleQ(temp[1]);
		}
		StringBuilder sb=new StringBuilder();
		sb.append("call proc_droptableifexists('").append(tableName).append("')");
		return sb.toString();
	}
	
	@Override
	public String dropViewIfExists(String tableName) {
		if(tableName.contains(".")){
			String[] temp=tableName.split("\\.");
			tableName=StringHandler.removeDoubleQ(temp[1]);
		}
		StringBuilder sb=new StringBuilder();
		sb.append("call proc_dropviewifexists('").append(tableName).append("')");
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
		String sql = "select count (*) from ( select * from " + tableName + " where rownum< " + number+ "+1 ) a ";
		return sql;
	}
	
	@Override
	public String textArray(){
		return "varchar2array";
	}
	@Override
	public String insertTable(String resTable, String decTable) {
		return "";
	}

	@Override
	public String getTableSetType(String type) {
		if(type.equalsIgnoreCase("EXCEPT")){
			return "MINUS";
		}else{
			return type;
		}
	}
	@Override
	public String getStorageString(boolean isAppendOnly, boolean isColumnarStorage, boolean isCompression, int compressionLevel){
		return "";
	}
	@Override
	public String getCastDataType(String sql,String dataType) {
		if(dataType.equals(OraSqlType.VARCHAR2)
				||dataType.equals(OraSqlType.NVARCHAR2)){
			return castToText(sql);
		}
		return sql;
	}
}
