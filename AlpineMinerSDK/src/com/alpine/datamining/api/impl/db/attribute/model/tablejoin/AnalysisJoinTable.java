/**
 * ClassName  JoinTable.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.tablejoin;


/**
 * @author John Zhao
 *
 */
public class AnalysisJoinTable{
 
	public static final String TAG_NAME="JoinTableModel";
	
	String schema;
	String table;
	String alias;
	String operatorModelID;
	
	

	public AnalysisJoinTable(String schema,String table,String alias,String operatorModelID){
		this.schema=schema;
		this.table=table;
		this.alias=alias;
		this.operatorModelID = operatorModelID;
	}
	
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String getOperatorModelID() {
		return operatorModelID;
	}

	public void setOperatorModelID(String operatorModelID) {
		this.operatorModelID = operatorModelID;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisJoinTable(schema,table,alias,operatorModelID);
	}

	
	public   boolean equals( Object obj){
		AnalysisJoinTable joinTable=(AnalysisJoinTable ) obj;
		if(joinTable.getSchema().equals(schema)
				&&joinTable.getTable().equals(table)
				&&joinTable.getAlias().equals(alias)){
			return true;
		}else{
			return false;
		}
		
	}
}