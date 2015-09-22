/**
 * ClassName WOENumbernicNode.java
 *
 * Version information: 1.00
 *
 * Data: 28 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.workflow.operator.parameter.woe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Shawn
 * 
 */
public class WOENumericNode extends WOENode {
	public static final String TAG_NAME = "WOENumbernicNode";
	private static final String ATTR_UPPER = "upper";
	private static final String ATTR_BOTTOM = "bottom";
	private String upper;
	private String bottom;

	public String getUpper() {
		return upper;
	}

	public void setUpper(String upper) {
		this.upper = upper;
	}

	public String getBottom() {
		return bottom;
	}

	public void setBottom(String bottom) {
		this.bottom = bottom;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(bottom).append("<=  ");
		result.append("X");
		result.append(" <").append(upper);
		result.append(" : ");
		result.append(groupInfo);
		result.append("\n");
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WOENumericNode)) {
			return false;
		} else {
			WOENumericNode woeNumbernicNode = (WOENumericNode) obj;
			if (upper.equals(woeNumbernicNode.getUpper())
					&& bottom.equals(woeNumbernicNode.getBottom())) {
				return true;
			}
			return false;
		}
	}
	
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_GROUPINFO, getGroupInfo());
		element.setAttribute(ATTR_WOEVALUE, String.valueOf(getWOEValue()));
		element.setAttribute(ATTR_UPPER, String.valueOf(getUpper()));
		element.setAttribute(ATTR_BOTTOM, String.valueOf(getBottom()));
		return element;
	}

	public static WOENumericNode fromXMLElement(Element item) {
		String groupInfo=item.getAttribute(ATTR_GROUPINFO);
		double woeValue=Double.parseDouble(item.getAttribute(ATTR_WOEVALUE));
		String upper=item.getAttribute(ATTR_UPPER);
		String bottom=item.getAttribute(ATTR_BOTTOM);
		
		WOENumericNode woeNumericField=new WOENumericNode();

		woeNumericField.setGroupInfo(groupInfo);
		woeNumericField.setWOEValue(woeValue);
		woeNumericField.setUpper(upper);
		woeNumericField.setBottom(bottom);
		return woeNumericField;
	}
}
