/**
 * ClassName WOETable.java
 *
 * Version information: 1.00
 *
 * Data: 1 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.workflow.operator.parameter.woe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;

/**
 * @author Jeff
 * 
 */
public class WOETable {
	public static final String TAG_NAME = "WOETable";
	private List<WOEInforList> DataTableWOE = new ArrayList<WOEInforList>();

	public List<WOEInforList> getDataTableWOE() {
		return DataTableWOE;
	}

	public void setDataTableWOE(List<WOEInforList> dataTableWOE) {
		DataTableWOE = dataTableWOE;
	}

	public WOEInforList getOneColumnWOE(String column) {
		Iterator<WOEInforList> keys = DataTableWOE.iterator();
		while (keys.hasNext()) {
			WOEInforList key = keys.next();
			if (key.getColumnName().equalsIgnoreCase(column)) {
				return key;
			}

		}
		return null;
	}

	public void addOneColumnWOE(WOEInforList tempWOE) {

		DataTableWOE.add(tempWOE);
	}

	public void removeOneColumnWOE(WOEInforList tempWOE) {
		DataTableWOE.remove(tempWOE);

	}

	public void removeOneColumnWOE(String column) {
		Iterator<WOEInforList> keys = DataTableWOE.iterator();
		while (keys.hasNext()) {
			WOEInforList key = keys.next();
			if (key.getColumnName().equalsIgnoreCase(column)) {
				DataTableWOE.remove(key);
				break;
			}

		}

	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		Iterator<WOEInforList> keys = DataTableWOE.iterator();
		while (keys.hasNext()) {
			WOEInforList key = keys.next();
			result.append("\n").append(key).append("   \n");
			// ListUtility.equalsIgnoreOrder
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WOETable)) {
			return false;
		} else {
			WOETable woeTable = (WOETable) obj;
			if (ListUtility.equalsIgnoreOrder(DataTableWOE, woeTable
					.getDataTableWOE())) {
				return true;
			}
			return false;

		}
	}

	public Node toXMLElement(Document xmlDoc) {
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getDataTableWOE()!=null){
			for (Iterator<WOEInforList> iterator = getDataTableWOE().iterator(); iterator.hasNext();) {
				WOEInforList item = iterator.next();
				if(item!=null){
					Element itemElement=item.toXMLElement(xmlDoc);
					element.appendChild(itemElement); 
				}			
			}
		}
		return element;
	}
	
	public static WOETable fromXMLElement(Element element) {
		List<WOEInforList> WOEInforItems = new ArrayList<WOEInforList>();
		NodeList woeInforList = element.getElementsByTagName(WOEInforList.TAG_NAME);
		for (int i = 0; i < woeInforList.getLength(); i++) {
			if (woeInforList.item(i) instanceof Element ) {
				WOEInforList woeFieldItem=WOEInforList.fromXMLElement((Element)woeInforList.item(i));
				WOEInforItems.add(woeFieldItem);
			}
		}
		WOETable woeTable=new WOETable();
		woeTable.setDataTableWOE(WOEInforItems);
		return woeTable;
	}
}
