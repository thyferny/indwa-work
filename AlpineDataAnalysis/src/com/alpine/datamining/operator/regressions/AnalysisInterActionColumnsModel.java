/**
 * ClassName InterActionModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-23
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.Resources;

/**
 * @author Eason
 *
 */
public class AnalysisInterActionColumnsModel {
	
	private List<AnalysisInterActionItem> analysisInterActionItems;

	public AnalysisInterActionColumnsModel(List<AnalysisInterActionItem> analysisInterActionItems) {
		this.analysisInterActionItems = analysisInterActionItems;
	}
	
	public AnalysisInterActionColumnsModel() {
		this.analysisInterActionItems = new ArrayList<AnalysisInterActionItem>();
	}

	public List<AnalysisInterActionItem> getInterActionItems() {
		return analysisInterActionItems;
	}

	public void setInterActionItems(List<AnalysisInterActionItem> analysisInterActionItems) {
		this.analysisInterActionItems = analysisInterActionItems;
	}
	public void addInterActionItem(AnalysisInterActionItem item){
		analysisInterActionItems.add(item);
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		int i=0;
		 for (Iterator<AnalysisInterActionItem> iterator = getInterActionItems().iterator(); iterator.hasNext();) {
			 AnalysisInterActionItem item = iterator.next();
			 sb.append(item.toString());
			 if(i!=getInterActionItems().size()-1){
				 sb.append(Resources.FieldSeparator);
			 }
			 i++;
		}
		return sb.toString();
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisInterActionColumnsModel){
		 
				return  ListUtility.equalsIgnoreOrder(analysisInterActionItems,
						((AnalysisInterActionColumnsModel)obj).getInterActionItems()) ;
			 
		}else{
			return false;
		}
	
	}
	
}
