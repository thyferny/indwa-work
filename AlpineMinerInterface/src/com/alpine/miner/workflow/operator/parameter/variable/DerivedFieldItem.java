/**
 * ClassName  DerivedFieldItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.variable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author zhaoyong
 *
 */
public class DerivedFieldItem  implements XMLFragment{
	
	public static final String TAG_NAME="DerivedFieldItem";

	private static final String ATTR_COLUMNNAME = "columnName";

	private static final String ATTR_DATATYPE = "dataType";

	private static final String ATTR_EXPRESSION = "expression";
	
	String resultColumnName = null; 
	String dataType = null;
	String sqlExpression = null;
	
	
 
	/**
	 * @param resultColumnName
	 * @param dataType
	 * @param sqlExpression
	 */
	public DerivedFieldItem(String resultColumnName, String dataType,
			String sqlExpression) {
	 
		this.resultColumnName = resultColumnName;
		this.dataType = dataType;
		this.sqlExpression = sqlExpression;
	}

	public String getResultColumnName() {
		return resultColumnName;
	}

	public void setResultColumnName(String resultColumnName) {
		this.resultColumnName = resultColumnName;
	}

	public String getDataType() {
		return dataType;
	}



	public void setDataType(String dataType) {
		this.dataType = dataType;
	}



	public String getSqlExpression() {
		return sqlExpression;
	}



	public void setSqlExpression(String sqlExpression) {
		this.sqlExpression = sqlExpression;
	}
	
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COLUMNNAME, getResultColumnName());
		element.setAttribute(ATTR_DATATYPE, getDataType());
		element.setAttribute(ATTR_EXPRESSION, getSqlExpression());
		return element;
	}	
	
	public boolean equals(Object obj) {
		if(obj==this){
			return true;
		}
		else if(obj instanceof DerivedFieldItem){
			DerivedFieldItem item = (DerivedFieldItem)obj;
			
			return  ParameterUtility.nullableEquales(item.getDataType(),this.getDataType())
				 && ParameterUtility.nullableEquales(item.getResultColumnName(),this.getResultColumnName()) 
			    && ParameterUtility.nullableEquales(item.getSqlExpression(),this.getSqlExpression()) ;
				
		}else{
			return false;
		}
	
	}
	
	/**
	 * @return
	 */
	public DerivedFieldItem clone() {
		DerivedFieldItem item = new DerivedFieldItem(resultColumnName,dataType,sqlExpression);
		return item;
	}

	public static DerivedFieldItem fromXMLElement(Element item) {
		String columnName=item.getAttribute(ATTR_COLUMNNAME);
		String expression=item.getAttribute(ATTR_EXPRESSION);
		String dataType=item.getAttribute(ATTR_DATATYPE);
		DerivedFieldItem aggregateField=new DerivedFieldItem(columnName,dataType,expression);
		return aggregateField;
	}

	@Override
	public String getXMLTagName() {
 
		return TAG_NAME;
	}

	@Override
	public void initFromXmlElement(Element element) {
		if(element!=null){
			this.resultColumnName=element.getAttribute(ATTR_COLUMNNAME);
			this.sqlExpression=element.getAttribute(ATTR_EXPRESSION);
			this.dataType=element.getAttribute(ATTR_DATATYPE);
		}
	}
	
	 
}
