/**
 * ClassName  QuantileModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */
public class AnalysisDerivedFieldsModel {
	public static final String TAG_NAME="DerivedFieldsModel";
 
	public static final String SELECTED_COLUMN_TAG_NAME="selectedColumnName";

	private static final String ATTR_COLUMNNAME = "columnName";
	
	List<AnalysisDerivedFieldItem> derivedFieldsList=null;
	
	List<String> selectedFieldList=null;
	
	public AnalysisDerivedFieldsModel(List<AnalysisDerivedFieldItem> derivedFieldsList){ 
		this.derivedFieldsList=derivedFieldsList;
	}
	public AnalysisDerivedFieldsModel( ){
		this.derivedFieldsList=new ArrayList<AnalysisDerivedFieldItem>();
	}
	
	 public List<AnalysisDerivedFieldItem> getDerivedFieldsList() {
		return derivedFieldsList;
	}
	public void setDerivedFieldsList(List<AnalysisDerivedFieldItem> derivedFieldsList) {
		this.derivedFieldsList = derivedFieldsList;
	}
	
	public List<String> getSelectedFieldList() {
		return selectedFieldList;
	}
	public void setSelectedFieldList(List<String> selectedFieldList) {
		this.selectedFieldList = selectedFieldList;
	}
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisDerivedFieldsModel){
				return ListUtility.equalsIgnoreOrder(derivedFieldsList,
						((AnalysisDerivedFieldsModel)obj).getDerivedFieldsList())
						&&ListUtility.equalsIgnoreOrder(selectedFieldList,
								((AnalysisDerivedFieldsModel)obj).getSelectedFieldList());
				
		}else{
			return false;
		}
	
	}
  
	public void addDerivedFieldItem(AnalysisDerivedFieldItem item) {
		if(derivedFieldsList==null){
			derivedFieldsList= new ArrayList<AnalysisDerivedFieldItem>();
		}
		derivedFieldsList.add(item) ;
		
	}
	public void removeDerivedFieldItem(AnalysisDerivedFieldItem item) {
		if(derivedFieldsList!=null&&derivedFieldsList.contains(derivedFieldsList)){
			derivedFieldsList.remove(item) ;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(TAG_NAME).append(" = ").append("\n");
		for (Iterator<AnalysisDerivedFieldItem> iterator = getDerivedFieldsList().iterator(); iterator.hasNext();) {
			 AnalysisDerivedFieldItem item = iterator.next();
			 sb.append(item.toString());
		}
		sb.append("\n");
		sb.append(SELECTED_COLUMN_TAG_NAME).append(" = ").append("\n");
		for (Iterator<String> iterator = getSelectedFieldList().iterator(); iterator.hasNext();) {
			 String item = iterator.next();
			 sb.append(ATTR_COLUMNNAME).append(":").append(item).append("\n"); 
		}
		return sb.toString();
	}
}
