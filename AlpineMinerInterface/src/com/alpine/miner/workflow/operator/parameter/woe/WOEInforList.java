/**
 * ClassName WOEInfroList.java
 *
 * Version information: 1.00
 *
 * Data: 31 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.workflow.operator.parameter.woe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;

/**
 * @author Jeff
 * 
 */
public class WOEInforList {
	public static final String TAG_NAME = "WOEInforList";
	private static final String ATTR_COLUMNNAME = "columnName";
	private static final String ATTR_GINI = "gini";
	private static final String ATTR_INFO_VALUE = "inforValue";
	private List<WOENode> InforList = new ArrayList<WOENode>();
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
		for (WOENode tempNode : InforList) {
			String key = tempNode.getGroupInfo();
			if (key.equalsIgnoreCase(groupInfo)) {
				tempNode.setWOEValue(woeValue);
				break;
			}

		}
	}

	public double getWOEValue(String groupInfo) {
		for (WOENode tempNode : InforList) {
			String key = tempNode.getGroupInfo();
			if (key.equalsIgnoreCase(groupInfo)) {
				return tempNode.getWOEValue();

			}

		}
		return 0;
	}

	public List<WOENode> getInforList() {
		return InforList;
	}

	public void setInforList(List<WOENode> inforList) {
		InforList = inforList;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("\n ColumnName :").append(columnName);
		result.append("\n InforValue :").append(inforValue);
		result.append("\n Gini :").append(gini).append("\n");
		for (WOENode tempNode : InforList) {
			String key = tempNode.getGroupInfo();
			result.append(tempNode.toString());
			result.append(key).append(" woe: ");

			result.append(tempNode.getWOEValue());

			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WOEInforList)) {
			return false;
		} else {
			WOEInforList woeInfoList = (WOEInforList) obj;
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

	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getColumnName());
		element.setAttribute(ATTR_GINI, String.valueOf(getGini()));
		element.setAttribute(ATTR_INFO_VALUE, String.valueOf(getInforValue()));
		if(getInforList()!=null){
			for (Iterator<WOENode> iterator = getInforList().iterator(); iterator.hasNext();) {
				WOENode item = iterator.next();
				if(item!=null){
					if(item instanceof WOENominalNode){
						Element itemElement=((WOENominalNode)item).toXMLElement(xmlDoc);
						element.appendChild(itemElement); 
					}else if(item instanceof WOENumericNode){
						Element itemElement=((WOENumericNode)item).toXMLElement(xmlDoc);
						element.appendChild(itemElement); 
					}
				}			
			}
		}
		return element;
	}

	public static WOEInforList fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_COLUMNNAME);
		double gini=Double.parseDouble(item.getAttribute(ATTR_GINI));
		double infoValue=Double.parseDouble(item.getAttribute(ATTR_INFO_VALUE));
		
		List<WOENode> woeNodeList=new ArrayList<WOENode>();
		
		NodeList nominalWOENodeList = item.getElementsByTagName(WOENominalNode.TAG_NAME);
		for (int i = 0; i < nominalWOENodeList.getLength(); i++) {
			if (nominalWOENodeList.item(i) instanceof Element ) {
				WOENominalNode woeFieldItem=WOENominalNode.fromXMLElement((Element)nominalWOENodeList.item(i));
				woeNodeList.add(woeFieldItem);
			}
		}
		
		NodeList numericWOENodeList = item.getElementsByTagName(WOENumericNode.TAG_NAME);
		for (int i = 0; i < numericWOENodeList.getLength(); i++) {
			if (numericWOENodeList.item(i) instanceof Element ) {
				WOENumericNode woeFieldItem=WOENumericNode.fromXMLElement((Element)numericWOENodeList.item(i));
				woeNodeList.add(woeFieldItem);
			}
		}
	
		
		WOEInforList woeInfoField=new WOEInforList();
		woeInfoField.setColumnName(columnName);
		woeInfoField.setGini(gini);
		woeInfoField.setInforValue(infoValue);

		woeInfoField.setInforList(woeNodeList);

		return woeInfoField;
	}

}
