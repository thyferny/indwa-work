/**
 * ClassName  AbstractSqlGeneratorMultiDBGPPG.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

public abstract class AbstractSqlGeneratorMultiDBGPPG implements ISqlGeneratorMultiDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8418959692679062461L;
	@Override
	public String castToText(String columnName) {
		return columnName+"::text";
	}
	@Override
	public String castToDouble(String columnName){
		return "(1.0*("+columnName+"))";
	}

	@Override
	public String setSeed(String seed, Integer para, Integer index) {
		String sql;
		if(para==null||index==null){
			sql = "SELECT setseed("+seed+");";
		}else{
			sql = "SELECT setseed("+(Double.parseDouble(seed)/(para+index))+");";
		}	
		return sql;
	}
	
	@Override
	public String cascade() {
		return "CASCADE";
	}
	
	@Override
	public String dropTableIfExists(String tableName) {
		StringBuilder sb=new StringBuilder();
		sb.append("drop table if exists ").append(tableName);
		return sb.toString();
	}
	@Override
	public String dropViewIfExists(String tableName) {
		StringBuilder sb=new StringBuilder();
		sb.append("drop view if exists ").append(tableName);
		return sb.toString();
	}
	@Override
	public String generateTempTableString() {
		
		return "create temp table ";
	}
	@Override
	public String rownumberOverByNull() {
		return " row_number() over () ";
	}
	@Override
	public String to_date(String date) {
		
		return "'"+date+"'";
	}
	
	@Override
	public String countTable(String tableName,String number) {
		String sql = "select count (*) from ( select * from " + tableName + " limit " + number + " ) a ";
		return sql;
	}
	@Override
	public String textArray(){
		return "text[]";
	}

	@Override
	public String getTableSetType(String type) {
		return type;
	}
	
	@Override
	public String getCastDataType(String sql,String dataType) {
		if(dataType.equals(GPSqlType.VARCHAR)){
			return castToText(sql);
		}
		return sql;
	}
}
