/**
 * ClassName  JoinFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hadoopunion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author Jeff Dong
 *
 */
public class HadoopUnionSourceColumn implements XMLFragment {
 
	public static final String TAG_NAME="unionSourceColumn";
	
	private static final String ATTR_OPERATOR_ID="operID";//uuid
	private static final String ATTR_Column_NAME = "column";
	 
	
	String operatorModelID;
	String columnName;
	String columnType; // this is only for UI, so need not persistent
	

	public String getColumnType() {
		return columnType;
	}



	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}



	public String getColumnName() {
		return columnName;
	}



	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}



	public HadoopUnionSourceColumn(String columnName,String operatorModelID ,String columnType){
		this.columnName=columnName;
		this.operatorModelID = operatorModelID;
		this.columnType = columnType;
	}
	
 
	
	public String getOperatorModelID() {
		return operatorModelID;
	}

	public void setOperatorModelID(String operatorModelID) {
		this.operatorModelID = operatorModelID;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_Column_NAME, getColumnName());
		element.setAttribute(ATTR_OPERATOR_ID, getOperatorModelID());
		return element;
	}

	/**
	 * @param item
	 * @return
	 */
	public static HadoopUnionSourceColumn fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_Column_NAME);
		String uuid= item.getAttribute(ATTR_OPERATOR_ID);
		return new HadoopUnionSourceColumn(columnName,uuid,"");//columnType is only for design time use...
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new HadoopUnionSourceColumn(columnName,operatorModelID,columnType);
	}

	
	public   boolean equals( Object obj){
		if(obj==null||(obj instanceof HadoopUnionSourceColumn) == false){
			return false;
		}
		HadoopUnionSourceColumn that=(HadoopUnionSourceColumn ) obj;
		return ParameterUtility.nullableEquales(that.getColumnName(), columnName);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	
}