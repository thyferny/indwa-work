/**
 * ClassName  QuantileItemBinNumeric.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.variable;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.utility.common.DoubleUtil;
import com.alpine.utility.common.ListUtility;


/**
 * @author John Zhao
 *
 */
public class QuantileItemBinNumeric extends AbstractQuantileItemBin {
	public static final String TAG_NAME="QuantileItemBinNumeric";
	public static final String ATTR_START_FROM="startFrom";
	public static final String ATTR_END_TO="endTo";
 
	//>=
	private String startFrom =null;
	private String endTo = null;
	
	//222,222,33, in xml...
	private List <String> values=null;

	//for step run
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}else if (obj instanceof QuantileItemBinNumeric==false){
			return false;
		}else{
			QuantileItemBinNumeric qItem= (QuantileItemBinNumeric) obj;
			if(getBinType()==BIN_TYPE_RANGE){
				return startFrom.equals(qItem.getStartFrom())
				&&endTo.equals(qItem.getEndTo())
						&&getBinIndex()==qItem.getBinIndex()
						&&getBinType()==qItem.getBinType();
			}else {
			return 
				 getBinIndex()==qItem.getBinIndex()
				&&getBinType()==qItem.getBinType()
				&&ListUtility.equalsIgnoreOrder(values, qItem.getValues());
			} 
		}
		 
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		
		if(getBinType()==BIN_TYPE_COLLECTION){
			sb.append(ListUtility.listToString(values,COLLECTION_SEPARATOR));
		}else {
			if(startFrom!=null&&endTo!=null)
		 	sb.append(String.valueOf(startFrom)).append("-").append(endTo) ;
		}
		return sb.toString();
		
	}
	
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute( ATTR_BIN_INDEX,String.valueOf(getBinIndex()));
		element.setAttribute( ATTR_BIN_TYPE,String.valueOf(getBinType()));	
		if(getStartFrom()!=null){
			element.setAttribute( ATTR_START_FROM,String.valueOf(getStartFrom()));
		}
		if(getEndTo()!=null){
			element.setAttribute( ATTR_END_TO,String.valueOf(getEndTo()));
		}
		if(values!=null){
			element.setAttribute( ATTR_VALUES,ListUtility.listToString(values,COLLECTION_SEPARATOR));
			
		}
 		
		return element;
	}	

	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	public String getStartFrom() {
		return startFrom;
	}
	public void setStartFrom(String startFrom) {
		this.startFrom = startFrom;
	}
	public String getEndTo() {
		return endTo ;
	}
	public void setEndTo(String endTo) {
		this.endTo = endTo;
	}

	public boolean isValid() {

		if(getBinType()==BIN_TYPE_COLLECTION){
			return values!=null&&values.size()>0;
			
		}else if(getBinType()==BIN_TYPE_RANGE){
			return ( startFrom!=null ) 	&&( endTo!=null )
						 ;
		}else{
			return true;
		}
	}
	public QuantileItemBinNumeric clone(){
		QuantileItemBinNumeric clone=new QuantileItemBinNumeric();
		clone.setBinIndex(getBinIndex());
		clone.setBinType(getBinType());
		if(getBinType()==BIN_TYPE_RANGE){
			clone.setStartFrom(startFrom);
			clone.setEndTo(endTo) ;
		}else{
			clone.setValues(ListUtility.cloneStringList(getValues())) ;
		
		}
			
		return clone;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	} 
}
