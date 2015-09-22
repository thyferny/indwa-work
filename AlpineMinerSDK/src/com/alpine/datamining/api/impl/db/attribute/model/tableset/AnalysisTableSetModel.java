/**
 * ClassName  SampleDataDefinition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-19
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.tableset;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;
import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong,Jeff Dong
 *
 */
public class AnalysisTableSetModel  {
	
	public static final String[] TABLE_SET_TYPE = new String[]{"UNION","UNION ALL","INTERSECT","EXCEPT"};
		 	
	String setType ="";

	String  firstTable ="";
	List<AnalysisColumnMap> columnMapList = new ArrayList<AnalysisColumnMap> (); 
	
	public AnalysisTableSetModel(String setType, String firstTable,
			List<AnalysisColumnMap> columnMapList) {
		super();
		this.setType = setType;
		this.firstTable = firstTable;
		this.columnMapList = columnMapList;
	}
	
	
	public AnalysisTableSetModel() {
	 
	}
	
	public AnalysisColumnMap getColumnMap(String schemaName,String tableName){
		AnalysisColumnMap result = null ;
		if(columnMapList!=null){
			for( AnalysisColumnMap columnMap:columnMapList){
				if(tableName!=null&&schemaName!=null&&columnMap!=null
						&&columnMap.getTableName().equals(tableName)
						&&columnMap.getSchemaName().equals(schemaName)){
					result = columnMap;
					break;
				}
			}
		}
		return result;
	}


	@Override
	public AnalysisTableSetModel clone() throws CloneNotSupportedException {
		AnalysisTableSetModel clone = new AnalysisTableSetModel( );
		clone.setSetType(getSetType()) ;
		clone.setFirstTable(getFirstTable()) ; 
		List<AnalysisColumnMap> columnMaps = new ArrayList<AnalysisColumnMap>();
		for( AnalysisColumnMap columnMap:columnMapList){
			columnMaps.add(columnMap.clone());
		}
		clone.setColumnMapList(columnMaps) ;
		return clone;
	 
	}

	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		out.append("setType =" +getSetType()+"\n");
		out.append("firstTable =" +getFirstTable()+"\n");
		out.append("columnMaps =" +columnMapList.toArray()+"\n");
	 
		return out.toString();
	}
	  
	 public boolean equals(Object obj) {
		 if((obj instanceof AnalysisTableSetModel)==false){
			 return false;
		 }
		 AnalysisTableSetModel target=(AnalysisTableSetModel )obj;
		 
		 return ModelUtility.nullableEquales(setType, target.getSetType())
	 		&& ModelUtility.nullableEquales(firstTable, target.getFirstTable())
	 		&& ListUtility.equalsIgnoreOrder(columnMapList,target.getColumnMapList());	 
	 }
 	
	public String getSetType() {
		return setType;
	}


	public void setSetType(String setType) {
		this.setType = setType;
	}


	public String getFirstTable() {
		return firstTable;
	}


	public void setFirstTable(String firstTable) {
		this.firstTable = firstTable;
	}


	public List<AnalysisColumnMap> getColumnMapList() {
		return columnMapList;
	}


	public void setColumnMapList(List<AnalysisColumnMap> columnMapList) {
		this.columnMapList = columnMapList;
	}


}