/**
 * ClassName  QuantileItem.java
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

import com.alpine.miner.workflow.operator.parameter.XMLFragment;

 

/**
 * @author John Zhao
 *
 */
public interface QuantileItemBin  extends XMLFragment{
	public static final String COLLECTION_SEPARATOR = ",";
	public static final String RANGE_SEPARATOR = "-";
	
	public static final String ATTR_BIN_INDEX="binIndex" ;
	public static final String ATTR_BIN_TYPE="binType" ;
	

	
	public static final String BIN_TYPE_RANGE_LABEL=  "Range";
	public static final String BIN_TYPE_COLLECTION_LABEL= "Collection";
	public static final String BIN_TYPE_REST_LABEL= "Rest Values ";
	
	public static final int BIN_TYPE_RANGE=  0;
	public static final int BIN_TYPE_COLLECTION= 1;
	public static final int BIN_TYPE_REST_VALUES= 2;//all the other value
	
//	public static final String TYPE_AVG_DESC_LABEL="Average Descend";
 
	
	
	public int getBinIndex();
	public void setBinIndex(int binIndex);
	
	public int getBinType();
	public void setBinType(int binType);
	/**
	 * @return
	 */
	public Element toXMLElement(Document doc);
	
	/**   could be double list or string list
	 * @return
	 */
	public List getValues();
	/**
	 * @return
	 */
	public boolean isValid();
	/**
	 * @return
	 */
	public   QuantileItemBin clone();
 
	
}
