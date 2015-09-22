/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.aggregate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */

 
public class AnalysisAggregateFieldsModel{
	public static final String TAG_NAME="AggregateFieldsModel";
	
	public static final String GROUPBY_TAG_NAME="groupBy";

	public static final String ATTR_COLUMNNAME = "columnName";
	
	public static final String PARENT_TAG_NAME="parentFieldList";
	
	List<AnalysisAggregateField> aggregateFieldList=null;
	List<String> groupByFieldList=null;
	List<String> parentFieldList=null;
	
	public List<AnalysisAggregateField> getAggregateFieldList() {
		return aggregateFieldList;
	}
	
	public void setAggregateFieldList(List<AnalysisAggregateField> aggregateFieldList) {
		this.aggregateFieldList = aggregateFieldList;
	}
	
	public List<String> getGroupByFieldList() {
		return groupByFieldList;
	}
	
	public void setGroupByFieldList(List<String> groupByFieldList) {
		this.groupByFieldList = groupByFieldList;
	}
 	
	public AnalysisAggregateFieldsModel(List<AnalysisAggregateField> aggregateFieldList,List<String>groupByFieldList,List<String> parentFieldList){ 
		this.aggregateFieldList=aggregateFieldList;
		this.groupByFieldList=groupByFieldList;
		this.parentFieldList=parentFieldList;
	}
	
	public AnalysisAggregateFieldsModel( ){
		this.aggregateFieldList=new ArrayList<AnalysisAggregateField>();
		this.groupByFieldList=new ArrayList<String>();
	}
	
	 
	
	public List<String> getParentFieldList() {
		return parentFieldList;
	}

	public void setParentFieldList(List<String> parentFieldList) {
		this.parentFieldList = parentFieldList;
	}

	public boolean equals(Object obj) {
		if(obj instanceof AnalysisAggregateFieldsModel){
		 
				return  ListUtility.equalsIgnoreOrder(aggregateFieldList,
						((AnalysisAggregateFieldsModel)obj).getAggregateFieldList()) 
					&&ListUtility.equalsFocusOrder(groupByFieldList,
						((AnalysisAggregateFieldsModel)obj).getGroupByFieldList())
					&&ListUtility.equalsIgnoreOrder(parentFieldList,
						((AnalysisAggregateFieldsModel)obj).getParentFieldList()) ;
			 
		}else{
			return false;
		}
	
	}
  
	public void addAggregateField(AnalysisAggregateField item) {
		if(aggregateFieldList==null){
			aggregateFieldList= new ArrayList<AnalysisAggregateField>();
		}
		aggregateFieldList.add(item) ;
		
	}
	public void removeAggregateField(AnalysisAggregateField item) {
		if(aggregateFieldList!=null&&aggregateFieldList.contains(aggregateFieldList)){
			aggregateFieldList.remove(item) ;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(TAG_NAME).append(" = ").append("\n");
		for (Iterator<AnalysisAggregateField> iterator = getAggregateFieldList().iterator(); iterator.hasNext();) {
			AnalysisAggregateField item = iterator.next();
			 sb.append(item.toString());
		}
		sb.append("\n");
		sb.append(GROUPBY_TAG_NAME).append(" = ").append("\n");
		for (Iterator<String> iterator = getGroupByFieldList().iterator(); iterator.hasNext();) {
			 String item = iterator.next();
			 sb.append(ATTR_COLUMNNAME).append(":").append(item).append("\n"); 
		}
		sb.append("\n");
		sb.append(PARENT_TAG_NAME).append(" = ").append("\n");
		for (Iterator<String> iterator = getParentFieldList().iterator(); iterator.hasNext();) {
			 String item = iterator.next();
			 sb.append(ATTR_COLUMNNAME).append(":").append(item).append("\n"); 
		}
		return sb.toString();
	}
	
}
