/**
 * ClassName WOETable.java
 *
 * Version information: 1.00
 *
 * Data: 1 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.woe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author Shawn
 * 
 */
public class AnalysisWOETable implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3826655007319336538L;
	private List<AnalysisWOEColumnInfo> DataTableWOE = new ArrayList<AnalysisWOEColumnInfo>();

	public List<AnalysisWOEColumnInfo> getDataTableWOE() {
		return DataTableWOE;
	}

	public void setDataTableWOE(List<AnalysisWOEColumnInfo> dataTableWOE) {
		DataTableWOE = dataTableWOE;
	}

	public AnalysisWOEColumnInfo getOneColumnWOE(String column) {
		Iterator<AnalysisWOEColumnInfo> keys = DataTableWOE.iterator();
		while (keys.hasNext()) {
			AnalysisWOEColumnInfo key = keys.next();
			if (key.getColumnName().equalsIgnoreCase(column)) {
				return key;
			}

		}
		return null;
	}

	public void addOneColumnWOE(AnalysisWOEColumnInfo tempWOE) {

		DataTableWOE.add(tempWOE);
	}

	public void removeOneColumnWOE(AnalysisWOEColumnInfo tempWOE) {
		DataTableWOE.remove(tempWOE);

	}

	public void removeOneColumnWOE(String column) {
		Iterator<AnalysisWOEColumnInfo> keys = DataTableWOE.iterator();
		while (keys.hasNext()) {
			AnalysisWOEColumnInfo key = keys.next();
			if (key.getColumnName().equalsIgnoreCase(column)) {
				DataTableWOE.remove(key);
				break;
			}

		}

	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		Iterator<AnalysisWOEColumnInfo> keys = DataTableWOE.iterator();
		while (keys.hasNext()) {
			AnalysisWOEColumnInfo key = keys.next();
			result.append("\n").append(key).append("   \n");
			// ListUtility.equalsIgnoreOrder
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==null||!(obj instanceof AnalysisWOETable)) {
			return false;
		} else {
			AnalysisWOETable woeTable = (AnalysisWOETable) obj;
			if (ListUtility.equalsIgnoreOrder(DataTableWOE, woeTable
					.getDataTableWOE())) {
				return true;
			}
			return false;

		}
	}
	
	@Override
	public AnalysisWOETable clone()  throws CloneNotSupportedException {
		AnalysisWOETable model= new AnalysisWOETable();
		if(this.getDataTableWOE()!=null){
			for(AnalysisWOEColumnInfo woeColumnInfo:DataTableWOE){
				model.addOneColumnWOE((AnalysisWOEColumnInfo)woeColumnInfo.clone());
			}
		}	 
	 return model;
	}
}
