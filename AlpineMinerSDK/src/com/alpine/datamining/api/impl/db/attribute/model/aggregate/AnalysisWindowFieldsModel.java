/**
 * ClassName  WindowFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.aggregate;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */

public class AnalysisWindowFieldsModel{
	public static final String TAG_NAME="WindowFieldsModel";
 
	List<AnalysisWindowField> windowFieldList=null;  
 
 
	public List<AnalysisWindowField> getWindowFieldList() {
		return windowFieldList;
	}

	public void setWindowFieldList(List<AnalysisWindowField> windowFieldList) {
		this.windowFieldList = windowFieldList;
	}

	public AnalysisWindowFieldsModel(List<AnalysisWindowField> windowFieldList){ 
		this.windowFieldList=windowFieldList;
	}
	
	public AnalysisWindowFieldsModel( ){
		this.windowFieldList=new ArrayList<AnalysisWindowField>();
	}
	
	 
	
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisWindowFieldsModel){
				return  ListUtility.equalsIgnoreOrder(windowFieldList,
						((AnalysisWindowFieldsModel)obj).getWindowFieldList()) ;
		}else{
			return false;
		}
	
	}
  
	public void addDerivedFieldItem(AnalysisWindowField item) {
		if(windowFieldList==null){
			windowFieldList= new ArrayList<AnalysisWindowField>();
		}
		windowFieldList.add(item) ;
		
	}
	public void removeDerivedFieldItem(AnalysisWindowField item) {
		if(windowFieldList!=null&&windowFieldList.contains(windowFieldList)){
			windowFieldList.remove(item) ;
		}
	}

}