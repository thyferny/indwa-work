/**
 * ClassName XMLDataParser.java
 *
 * Version information: 1.00
 *
 * Date: Oct 24, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.hadoop.ext;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLRecordParser extends AbstractRecordParser {

	private List<String> xpathList = new ArrayList<String>();
	private XPath xpathObj = null;
	private String sturctureType;
	private String containerPath;
	private XPathTree rootXpathTree;
	private HashMap<Integer , XPathItem>  xPathItemMap = new HashMap<Integer , XPathItem> ();
	DocumentBuilder db = null;
	private static Logger itsLogger = Logger.getLogger(XMLRecordParser.class);

	public XMLRecordParser(List<String> xpathList, String sturctureType,
			String containerPath) {

		if (xpathList != null) {
			this.xpathList = xpathList;
		}
		this.sturctureType = sturctureType;
		this.containerPath = containerPath;
		XPathFactory factory = XPathFactory.newInstance();

		xpathObj = factory.newXPath();
		rootXpathTree = new XPathTree("root", null, null);
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			itsLogger.error(e.getMessage(), e);
		}

	}

 

	@Override
	public List<String[]> parse(String aRecordContent) throws Exception {

		Element root =null;
		try{
			Document xmlDoc = db.parse(new ByteArrayInputStream(aRecordContent.getBytes("utf-8")));
			  root = xmlDoc.getDocumentElement();
			}catch(Exception e){
			itsLogger.error("Can not parse xml element:" +aRecordContent )  ;
			itsLogger.error(e.getMessage(),e) ;
 			return new ArrayList<String[]> ();
		}
		if(root!=null){
			//root is just a name , no use 
			rootXpathTree.setParentXpath("//") ;
			rootXpathTree.setParentXPathTree(null);
	
			
			fillXpathTree(rootXpathTree,cloneXpathList(xpathList)); 
	 
			XPathRow rootRow = createXPathRow(root, rootXpathTree);
			rootRow.countMaXDepth();
			List<String[]> result = new ArrayList<String[]>(rootRow.getMaxDepth());// this is all data
			initResultData(result, rootRow.getMaxDepth());
			rootRow.fillResultData(result);
			rootXpathTree.clear();
			return result;
		}else{
 			return new ArrayList<String[]> ();
		}

	}

	private List<String> cloneXpathList(List<String> xpathList) {
		List<String> clone = new ArrayList<String>();
		for (Iterator<String> iterator = xpathList.iterator(); iterator.hasNext();) {
			String xpath = iterator.next();
			clone.add(xpath);
		}
		return clone;
	}

	private void fillXpathTree(XPathTree parentTree,List<String> thisLevelXpath) {
		String parentXpath = parentTree.getParentXpath();
		List<String> shouldRemove = new ArrayList<String>();
		for (int i = 0; i < thisLevelXpath.size() ; i++) {
			String xpath = thisLevelXpath.get(i) ;
			if(xpath.startsWith(parentXpath+"@")){
				String thisLevelxpath = getThisLevelXpath(xpath,parentXpath);
				int columnIndex = xpathList.indexOf(buildFullXpath(parentTree,thisLevelxpath)) ;

				parentTree.addXPathItem( getXPathItem(columnIndex,thisLevelxpath )) ;
				shouldRemove.add(xpath);
			}else if (isDirectXpath(xpath,parentXpath)){//first level
				String thisLevelxpath = getThisLevelXpath(xpath,parentXpath);

				int columnIndex = xpathList.indexOf(buildFullXpath(parentTree,thisLevelxpath)) ;

				parentTree.addXPathItem(   getXPathItem(columnIndex, thisLevelxpath)) ;
				shouldRemove.add(xpath);
			} 
			
		}
		thisLevelXpath.removeAll(shouldRemove) ;
		if(thisLevelXpath.size()>0){
			HashMap <String,List<String>> groupdedThisLevelXpath = gruopXpathByTagName(thisLevelXpath,parentXpath);
			
			for (Iterator<String> iterator = groupdedThisLevelXpath.keySet().iterator(); iterator.hasNext();) {
				String tagName = iterator.next();
				XPathTree newChildXPathTree = new XPathTree(tagName, null, null);
				newChildXPathTree.setParentXpath( tagName+"/") ;
				newChildXPathTree.setParentXPathTree(parentTree);
				fillXpathTree(newChildXPathTree, groupdedThisLevelXpath.get(tagName));
				parentTree.addChildXPathTree( newChildXPathTree);
			}
		}
	}

	private XPathItem getXPathItem(int columnIndex, String thisLevelxpath) {
		 
		if(xPathItemMap.containsKey(columnIndex)==false){
			xPathItemMap.put(columnIndex, 		  new XPathItem(columnIndex,thisLevelxpath )) ;
		}
		return xPathItemMap.get(columnIndex);
	}



	private String buildFullXpath(XPathTree parentTree,
		   String thisLevelXpath) {
		return getParetnFulPath(parentTree)+thisLevelXpath;
		
	 
	}

	private String getParetnFulPath(XPathTree parentTree) {
		//not the root
		if(parentTree.getParentXPathTree()!=null ){
			return getParetnFulPath(parentTree.getParentXPathTree())+parentTree.getTagName()+"/";
			
		}//root
		else{
			return "//" ;
		}
		 
	 
	}

	private HashMap<String, List<String>> gruopXpathByTagName(
			List<String> xpaths, String parentXpath) {
		HashMap<String, List<String>> resultMap = new HashMap<String, List<String>> (); 
		for (Iterator<String> iterator = xpaths.iterator(); iterator.hasNext();) {
			String xpath = iterator.next();
			String thisLevelXpath = getThisLevelXpath(xpath, parentXpath) ;
			String tagName = thisLevelXpath.split("/")[0]; 
			if(resultMap.get(tagName)==null){
				resultMap.put(tagName, new ArrayList<String> ()) ;
			}
			resultMap.get(tagName).add(thisLevelXpath);
			
		}
		return resultMap;
	}

	private String getThisLevelXpath(String xpath, String parentXpath) {
		//from //@xx -> @xxx   from //localtion/@length -> @length
		String result = xpath.substring(parentXpath.length(),xpath.length());
		
		return result;
	}

	private boolean isDirectXpath(String xpath, String parentXpath) {
		String purePath =getThisLevelXpath(xpath, parentXpath);
		boolean result;
		if(purePath.indexOf("/") ==purePath.lastIndexOf("/")&&purePath.endsWith("/text()")){
			result = true;
		}else{
			result=  false;	
		}
		return result;
	}

	private void initResultData(List<String[]> result, int number) {
		for (int i = 0; i < number; i++) {
			result.add(new String[xpathList.size()]);
		}
	}

	private XPathRow createXPathRow(Element element, XPathTree xpathTree)
			throws XPathExpressionException {
		XPathRow result = new XPathRow();
		if (xpathTree.getXpathElement() != null) {

			for (XPathItem xPathElement : xpathTree.getXpathElement()) {

				XPathExpression expr = xpathObj.compile(xPathElement
						.getThisLevelXPath());

				int columnIndex = xPathElement.getColumnIndex();

				if (xPathElement.getThisLevelXPath().startsWith("@")) {
					String aValue = element.getAttribute(xPathElement
							.getThisLevelXPath().substring(1,
									xPathElement.getThisLevelXPath().length()));
					result.putAttributeValue(columnIndex, aValue);
				} else if (xPathElement.getThisLevelXPath().startsWith("//@")) {
					String aValue = element.getAttribute(xPathElement
							.getThisLevelXPath().substring(3,
									xPathElement.getThisLevelXPath().length()));
					result.putAttributeValue(columnIndex, aValue);
				} else {
					NodeList xpathResult = (NodeList) expr.evaluate(element,
							XPathConstants.NODESET);
					if (xpathResult == null || xpathResult.getLength() == 0) {
						continue;
					} else {
						for (int k = 0; k < xpathResult.getLength(); k++) {
							String oneValue = getColumnValue(xpathResult
									.item(k));

							XPathRow createdXPathRow = new XPathRow();
							createdXPathRow.getRowDatas().put(columnIndex,
									oneValue);
							result.addChildXPathRow(createdXPathRow,
									"tagName_alp_" + columnIndex);
						}
					}
				}

			}
		}

		if (xpathTree.getChildXPathTree() != null) {

			for (XPathTree subTree : xpathTree.getChildXPathTree()) {
				String tagName = subTree.getTagName();
				NodeList childNodes = element.getElementsByTagName(tagName);
				for (int k = 0; k < childNodes.getLength(); k++) {
					Node node = childNodes.item(k);
					if (node instanceof Element) {
						result.addChildXPathRow(
								createXPathRow(((Element) node), subTree),
								tagName);

					}
				}
			}
		}
		return result;
	}

	private String getColumnValue(Node item) {
		if (item.getNodeType() == Node.ATTRIBUTE_NODE) {
			return item.getTextContent();
		} else if (item.getNodeType() == Node.TEXT_NODE) {
			return item.getTextContent();
		} else {
			return "";
		}
	}

 

	@Override
	public String[] parseLine(String string) throws Exception {
		// nothing to do here, n
		throw new UnsupportedOperationException();
	}
}