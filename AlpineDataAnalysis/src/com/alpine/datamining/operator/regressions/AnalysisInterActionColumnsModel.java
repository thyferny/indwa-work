
package com.alpine.datamining.operator.regressions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.Resources;


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
