/**
 * ClassName WOENomalnicNode.java
 *
 * Version information: 1.00
 *
 * Data: 28 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.workflow.operator.parameter.woe;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;

/**
 * @author Shawn
 * 
 */
public class WOENominalNode extends WOENode {
	private static final String CHOOSELIST_TAG_NAME = "choosedList";
	private static final String ATTR_COLUMNNAME = "columnName";
	public static final String TAG_NAME = "WOENominalNode";
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
		if (!(obj instanceof WOENominalNode)) {
			return false;
		} else {
			WOENominalNode woeNominalNode = (WOENominalNode) obj;
			if (ListUtility.equalsIgnoreOrder(choosedList, woeNominalNode
					.getChoosedList())) {
				return true;
			}
			return false;
		}
	}
	
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_GROUPINFO, getGroupInfo());
		element.setAttribute(ATTR_WOEVALUE, String.valueOf(getWOEValue()));
		if(getChoosedList()!=null){
			for(String s:getChoosedList()){
				Element parentEle=xmlDoc.createElement(CHOOSELIST_TAG_NAME);
				parentEle.setAttribute(ATTR_COLUMNNAME, s);
				element.appendChild(parentEle);
			}		
		}
		return element;
	}

	public static WOENominalNode fromXMLElement(Element item) {
		String groupInfo=item.getAttribute(ATTR_GROUPINFO);
		double woeValue=Double.parseDouble(item.getAttribute(ATTR_WOEVALUE));
		NodeList chooseNodeList = item.getElementsByTagName(CHOOSELIST_TAG_NAME);
		List<String> chooseList=new ArrayList<String>();
		for (int i = 0; i < chooseNodeList.getLength(); i++) {
			if (chooseNodeList.item(i) instanceof Element ) {
				String choose=((Element)chooseNodeList.item(i)).getAttribute(ATTR_COLUMNNAME);
				chooseList.add(choose);
			}
		}
		
		WOENominalNode woeNominalField=new WOENominalNode();
		woeNominalField.setChoosedList(chooseList);
		woeNominalField.setGroupInfo(groupInfo);
		woeNominalField.setWOEValue(woeValue);
		return woeNominalField;
	}
}
