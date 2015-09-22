/**
 * ClassName :AggregateField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.aggregate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author zhaoyong
 * 
 */
public class AggregateField implements XMLFragment {

	public static final String TAG_NAME="AggregateField";

	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_EXPRESSION = "expression";
	
	private static final String ATTR_TYPE = "dataType";
	
	String alias = null;
	
	String aggregateExpression = null;
	
	String dataType = null;
	
	

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAggregateExpression() {
		return aggregateExpression;
	}

	public void setAggregateExpression(String aggregateExpression) {
		this.aggregateExpression = aggregateExpression;
	}

	/**
	 * @param alias2
	 * @param aggregateExpression2
	 */
	public AggregateField(String alias, String aggregateExpression,String dataType) {
		this.aggregateExpression = aggregateExpression;
		this.alias = alias;
		this.dataType=dataType;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getAlias());
		element.setAttribute(ATTR_EXPRESSION, getAggregateExpression());
		element.setAttribute(ATTR_TYPE, getDataType());
		return element;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AggregateField(alias, aggregateExpression,dataType);
	}
	
	 public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj instanceof AggregateField){
			 AggregateField aggField = (AggregateField) obj;
			 return ParameterUtility.nullableEquales(alias ,aggField.getAlias())
			 && ParameterUtility.nullableEquales(aggregateExpression ,aggField.getAggregateExpression())
			 &&ParameterUtility.nullableEquales(dataType ,aggField.getDataType());
 
		 }else{
			 return false;
		 }
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

	public static AggregateField fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_COLUMNNAME);
		String expression=item.getAttribute(ATTR_EXPRESSION);
		String dataType=item.getAttribute(ATTR_TYPE);
		AggregateField aggregateField=new AggregateField(columnName,expression,dataType);
		return aggregateField;
	}
}