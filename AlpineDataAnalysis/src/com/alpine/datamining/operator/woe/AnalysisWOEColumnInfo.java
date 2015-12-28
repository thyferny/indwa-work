

package com.alpine.datamining.operator.woe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.common.ListUtility;


public class AnalysisWOEColumnInfo implements Serializable{
	
	private static final long serialVersionUID = -5948250648008756404L;
	private List<AnalysisWOENode> InforList = new ArrayList<AnalysisWOENode>();
	// Map <String,Double> WOEValue =new HashMap<String,Double>();
	String columnName;
	boolean isChanged = false;

	double inforValue;
	double gini;

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public double getInforValue() {
		return inforValue;
	}

	public void setInforValue(double inforValue) {
		this.inforValue = inforValue;
	}

	public double getGini() {
		return gini;
	}

	public void setGini(double gini) {
		this.gini = gini;
	}

	public void setWOEValue(String groupInfo, double woeValue) {
		for (AnalysisWOENode tempNode : InforList) {
			String key = tempNode.getGroupInfror();
			if (key.equalsIgnoreCase(groupInfo)) {
				tempNode.setWOEValue(woeValue);
				break;
			}

		}
	}

	public double getWOEValue(String groupInfo) {
		for (AnalysisWOENode tempNode : InforList) {
			String key = tempNode.getGroupInfror();
			if (key.equalsIgnoreCase(groupInfo)) {
				return tempNode.getWOEValue();

			}

		}
		return 0;
	}

	// public Map<String, Double> getWOEValue() {
	// return WOEValue;
	// }
	//
	// public void setWOEValue(Map<String, Double> wOEValue) {
	// WOEValue = wOEValue;
	// }

	public List<AnalysisWOENode> getInforList() {
		return InforList;
	}

	public void setInforList(List<AnalysisWOENode> inforList) {
		InforList = inforList;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("\n ColumnName :").append(columnName);
		result.append("\n InforValue :").append(inforValue);
		result.append("\n Gini :").append(gini).append("\n");
		for (AnalysisWOENode tempNode : InforList) {
			String key = tempNode.getGroupInfror();
			result.append(tempNode.toString());
			result.append(key).append(" woe: ");

			result.append(tempNode.getWOEValue());

			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==null||!(obj instanceof AnalysisWOEColumnInfo)) {
			return false;
		} else {
			AnalysisWOEColumnInfo woeInfoList = (AnalysisWOEColumnInfo) obj;
			if (columnName.equalsIgnoreCase(woeInfoList.getColumnName())) {
				if (ListUtility.equalsIgnoreOrder(InforList, woeInfoList
						.getInforList())) {
					return true;
				}
				return false;
			}
		}
		return false;

	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		ArrayList<AnalysisWOENode> newList = new ArrayList<AnalysisWOENode>();
		if(InforList!=null){
			for(AnalysisWOENode analysisWOENode:InforList){
				newList.add((AnalysisWOENode)analysisWOENode.clone());
			}
		}
		AnalysisWOEColumnInfo woeInfo = new AnalysisWOEColumnInfo();
		woeInfo.setColumnName(columnName);
		woeInfo.setInforList(newList);
		woeInfo.setGini(gini);
		woeInfo.setInforValue(inforValue);
		return woeInfo;
	}

}
