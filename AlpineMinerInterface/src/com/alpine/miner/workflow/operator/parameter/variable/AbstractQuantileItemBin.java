/**
 * ClassName  AbstractQuantileItemBin.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.variable;

import org.w3c.dom.Element;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * @author John Zhao
 *
 */
public abstract class AbstractQuantileItemBin implements QuantileItemBin {

	public static final String ATTR_VALUES="values";
	
	private int binIndex;
	private int binType;

	public void setBinIndex(int binIndex) {
		this.binIndex = binIndex;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.gef.runoperator.field.varible.QuantileItemBin#getBinIndex()
	 */
	@Override
	public int getBinIndex() {
		return binIndex;
	}
 	
	public static QuantileItemBin fromXMLElement(Element element) {
		QuantileItemBin  itemBin=null; 
		if(element.getTagName().equals(QuantileItemBinCategory.TAG_NAME)){
			QuantileItemBinCategory itemBinCategory = new QuantileItemBinCategory();
			
			itemBinCategory.setValues(StringUtil.stringToList(
					element.getAttribute(ATTR_VALUES), ","));

			itemBin=itemBinCategory;
			
		}else if(element.getTagName().equals(QuantileItemBinNumeric.TAG_NAME)){
			QuantileItemBinNumeric itemBinNumeric = new QuantileItemBinNumeric();
		 
			String startFromValue=element.getAttribute(QuantileItemBinNumeric.ATTR_START_FROM);
			if(isNullValue(startFromValue)){
				itemBinNumeric.setStartFrom(startFromValue); 
			}
			
			String endToValue =element.getAttribute(QuantileItemBinNumeric.ATTR_END_TO);
			if(isNullValue(endToValue)){
				itemBinNumeric.setEndTo(endToValue);
			}
			if(element.getAttribute(ATTR_VALUES)!=null){
				itemBinNumeric.setValues(ListUtility.stringToDoubleList(element.getAttribute(ATTR_VALUES),
						COLLECTION_SEPARATOR));
			}
			
			itemBin=itemBinNumeric;
			 
		}
		else if(element.getTagName().equals(QuantileItemBinDateTime.TAG_NAME)){
			QuantileItemBinDateTime itemBinDateTime = new QuantileItemBinDateTime();
			if(isNullValue(element.getAttribute(QuantileItemBinDateTime.ATTR_DATE_START_FROM))){
				itemBinDateTime.setStartDate( element.getAttribute(QuantileItemBinDateTime.ATTR_DATE_START_FROM)); 
			}
			if(isNullValue(element.getAttribute(QuantileItemBinDateTime.ATTR_DATE_END_TO))){
				itemBinDateTime.setEndDate( element.getAttribute(QuantileItemBinDateTime.ATTR_DATE_END_TO));
			}
			if(isNullValue(element.getAttribute(QuantileItemBinDateTime.ATTR_TIME_START_FROM))){
				itemBinDateTime.setStartTime( element.getAttribute(QuantileItemBinDateTime.ATTR_TIME_START_FROM)); 
			}
			if(isNullValue(element.getAttribute(QuantileItemBinDateTime.ATTR_TIME_END_TO))){
				itemBinDateTime.setEndTime( element.getAttribute(QuantileItemBinDateTime.ATTR_TIME_END_TO));
			}
			if(element.getAttribute(ATTR_VALUES)!=null){
				itemBinDateTime.setValues(StringUtil.stringToList(element.getAttribute(ATTR_VALUES),
						COLLECTION_SEPARATOR));
			}
			
			itemBin=itemBinDateTime;
			 
		}
		itemBin.setBinIndex(Integer.parseInt(element.getAttribute(QuantileItemBin.ATTR_BIN_INDEX))) ;
		itemBin.setBinType(Integer.parseInt(element.getAttribute(QuantileItemBin.ATTR_BIN_TYPE))) ;
		return itemBin;
	}
 
	/**
	 * @param value
	 * @return
	 */
	private static boolean isNullValue(String value) {
		return value!=null&&value.equals("null")==false&&value.trim().length()>0;
	}
	
	public int getBinType(){
		return binType;
	}
	public void setBinType(int binType){
		this.binType=binType;
	}

	public abstract QuantileItemBin clone();
	
}
