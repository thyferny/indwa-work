/**
 * ClassName  QuantileFieldsModel.java
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
public class AnalysisQuantileFieldsModel{
	public static final String TAG_NAME="QuantileModel";
 
	List<AnalysisQuantileItem> quantileItems=null;
	
	public AnalysisQuantileFieldsModel(List<AnalysisQuantileItem> quantileItems){
		this.quantileItems=quantileItems;
	}
	public AnalysisQuantileFieldsModel( ){
		this.quantileItems=new ArrayList<AnalysisQuantileItem>();
	}
	
	 public List<AnalysisQuantileItem> getQuantileItems() {
		return quantileItems;
	}
	public void setQuantileItems(List<AnalysisQuantileItem> quantileItems) {
		this.quantileItems = quantileItems;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisQuantileFieldsModel){
		 
				return ListUtility.equalsIgnoreOrder(quantileItems,
						((AnalysisQuantileFieldsModel)obj).getQuantileItems()) ;
			 
				
		}else{
			return false;
		}
	
	}
	
	/**
	 * @param item
	 */
	public void addQuantileItem(AnalysisQuantileItem item) {
		if(quantileItems==null){
			quantileItems= new ArrayList<AnalysisQuantileItem>();
		}
		quantileItems.add(item) ;
		
	}
	public void removeQuantileItem(AnalysisQuantileItem item) {
		if(quantileItems!=null&&quantileItems.contains(quantileItems)){
			quantileItems.remove(item) ;
		}
	}


	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		 for (Iterator<AnalysisQuantileItem> iterator = getQuantileItems().iterator(); iterator.hasNext();) {
			 AnalysisQuantileItem item =iterator.next();
			out.append(item.toString());		
		}
		return out.toString();
	}

}
