/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.histogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.Resources;

/**
 * @author zhaoyong
 *
 */

 

public class AnalysisColumnBinsModel{
	public static final String TAG_NAME="ColumnBinsModel";
 
	List<AnalysisColumnBin> columnBins=null;
	 
	 
	public AnalysisColumnBinsModel(List<AnalysisColumnBin> columnBins){ 
		this.columnBins=columnBins;
	}
	
	public AnalysisColumnBinsModel( ){
		this.columnBins=new ArrayList<AnalysisColumnBin>();
	}
	
	public List<AnalysisColumnBin> getColumnBins() {
		return columnBins;
	}

	public void setColumnBins(List<AnalysisColumnBin> columnBins) {
		this.columnBins = columnBins;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisColumnBinsModel){
		 
				return  ListUtility.equalsIgnoreOrder(columnBins,
						((AnalysisColumnBinsModel)obj).getColumnBins()) 				  ;
		}else{
			return false;
		}
	
	}
  
	public void addColumnBin(AnalysisColumnBin columnBin) {
		if(columnBins==null){
			columnBins= new ArrayList<AnalysisColumnBin>();
		}
		columnBins.add(columnBin) ;
		
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		int i=0;
		 for (Iterator<AnalysisColumnBin> iterator = getColumnBins().iterator(); iterator.hasNext();) {
			 AnalysisColumnBin item = iterator.next();
			 sb.append(item.toString());
			 if(i!=getColumnBins().size()-1){
				 sb.append(Resources.FieldSeparator);
			 }
			 i++;
		}
		return sb.toString();
	}
	
}
