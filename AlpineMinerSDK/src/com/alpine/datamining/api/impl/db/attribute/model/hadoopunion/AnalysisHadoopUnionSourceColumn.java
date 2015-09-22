/**
 * ClassName  AnalysisHadoopUnionSourceColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopunion;


/**
 * @author john zhao
 *
 */
public class AnalysisHadoopUnionSourceColumn  {
	
	String operatorModelID;
	String columnName;
	
	public String getColumnName() {
		return columnName;
	}



	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}



	public AnalysisHadoopUnionSourceColumn(String columnName,String operatorModelID){
		this.columnName=columnName;
		this.operatorModelID = operatorModelID;
	}
	
 
	
	public String getOperatorModelID() {
		return operatorModelID;
	}

	public void setOperatorModelID(String operatorModelID) {
		this.operatorModelID = operatorModelID;
	}

	
	public   boolean equals( Object obj){
		if(obj==null||(obj instanceof AnalysisHadoopUnionSourceColumn) == false){
			return false;
		}
		AnalysisHadoopUnionSourceColumn that=(AnalysisHadoopUnionSourceColumn ) obj;
		return nullableEquales(that.getColumnName(), columnName);
	}



	    boolean nullableEquales(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		} else if (obj1 != null) {
			return obj1.equals(obj2);
		} else {
			return false;
		}
	}
 
 
	
}