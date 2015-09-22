/**
 * ClassName  AnalysisHadoopUnionModelItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.hadoopunion;

import java.util.List;

/**
 * @author john zhao
 *
 */
public class AnalysisHadoopUnionModelItem       {

	 



	List<AnalysisHadoopUnionSourceColumn> mappingColumns;
	public List<AnalysisHadoopUnionSourceColumn> getMappingColumns() {
		return mappingColumns;
	}

	public void setMappingColumns(List<AnalysisHadoopUnionSourceColumn> mappingColumns) {
		this.mappingColumns = mappingColumns;
	}
	String columnName ="";
	String columnType ="";
 	
	public AnalysisHadoopUnionModelItem(String columnName, String columnType ,List<AnalysisHadoopUnionSourceColumn> mappingColumns) {
		this.columnName = columnName;
		this.columnType=columnType;
		this.mappingColumns =mappingColumns;
	}

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
 
	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public boolean equals(Object obj) {
		if(obj==null||(obj instanceof AnalysisHadoopUnionModelItem) ==false){
			return false;
		}
		AnalysisHadoopUnionModelItem joinColumn = (AnalysisHadoopUnionModelItem) obj; 
		return  nullableEquales(joinColumn.getColumnName(),columnName)
				&& nullableEquales(joinColumn.getColumnType(),columnType);

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