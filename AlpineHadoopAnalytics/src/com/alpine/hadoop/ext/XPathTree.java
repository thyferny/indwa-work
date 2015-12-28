package com.alpine.hadoop.ext;

import java.util.ArrayList;
import java.util.List;

public class XPathTree {

	String tagName;// tagName ==null means this is a root
	List<XPathItem> xpathElement = new ArrayList<XPathItem>();
	List<XPathTree> childXPathTree = new ArrayList<XPathTree>();

	private String parentXparth;
	private XPathTree parentXPathTree;

	@Override
	public String toString() {
		return "XPathTree [tagName=" + tagName + ", parentXparth=" + parentXparth
				+ ", xpathElement="
				+ xpathElement + ", childXPathTree=" + childXPathTree
				 + "]";
	}

	public XPathTree(String tagName, List<XPathItem> xpathElement,
			List<XPathTree> childXPathTree) {
		super();
		this.tagName = tagName;
		if(xpathElement!=null){
			this.xpathElement = xpathElement;
		}
		if(childXPathTree!=null){
			this.childXPathTree = childXPathTree;
		}
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<XPathItem> getXpathElement() {
		return xpathElement;
	}
	
	public void addXPathItem(XPathItem e){
		xpathElement.add(e) ;
	}
	public void addChildXPathTree(XPathTree e){
		childXPathTree.add(e) ;
	}
	
	public void setXpathElement(List<XPathItem> xpathElement) {
		this.xpathElement = xpathElement;
	}

	public List<XPathTree> getChildXPathTree() {
		return childXPathTree;
	}

	public void setChildXPathTree(List<XPathTree> childXPathTree) {
		this.childXPathTree = childXPathTree;
	}

	// belowing is for test use ....

	// @id
	// track/@length
	// track/location/text()
	// track/writers/writer/name/text()
	// track/writers/writer/age/text()
	

	public XPathTree lightlyColne() {
		XPathTree clone = new XPathTree(tagName, xpathElement, childXPathTree);
		return clone;
	}

	public void setParentXpath(String parentXparth) {
		this.parentXparth = parentXparth;

	}

	public String getParentXpath() {
		return this.parentXparth;

	}

	public XPathTree getParentXPathTree() {
		return parentXPathTree;
	}

	public void setParentXPathTree(XPathTree parentXPathTree) {
		this.parentXPathTree = parentXPathTree;
	}

	//make sure to save time , need not create the object any more 
	public void clear() {
		xpathElement.clear();
		childXPathTree.clear();		
	}

}
