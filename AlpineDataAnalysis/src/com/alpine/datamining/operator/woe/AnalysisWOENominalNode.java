/**
 * ClassName WOENomalnicNode.java
 *
 * Version information: 1.00
 *
 * Data: 28 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.woe;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.common.ListUtility;

/**
 * @author Shawn
 * 
 */
public class AnalysisWOENominalNode extends AnalysisWOENode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6668122397542787189L;
	private List<String> choosedList;

	public List<String> getChoosedList() {
		return choosedList;
	}

	public void setChoosedList(List<String> choosedList) {
		this.choosedList = choosedList;
	}

	public void addOneValue(String column) {
		choosedList.add(column);
	}

	public void removeOneValue(String column) {
		if (choosedList.contains(column))
			choosedList.remove(column);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(" in {");
		for (String tempString : choosedList) {
			result.append(tempString);
			result.append(",");
		}
		result.deleteCharAt(result.length() - 1);
		result.append("} : ");
		result.append(groupInfo);
		result.append("\n");
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==null||!(obj instanceof AnalysisWOENominalNode)) {
			return false;
		} else {
			AnalysisWOENominalNode woeNominalNode = (AnalysisWOENominalNode) obj;
			if (ListUtility.equalsIgnoreOrder(choosedList, woeNominalNode
					.getChoosedList())) {
				return true;
			}
			return false;
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		AnalysisWOENominalNode newNode=new AnalysisWOENominalNode();
		newNode.setWOEValue(WOEValue);
		newNode.setGroupInfror(groupInfo);
		List<String> newChoosedList=new ArrayList<String>();
		newChoosedList.addAll(choosedList);
		newNode.setChoosedList(newChoosedList);
		return newNode;
	}
	
	
}
