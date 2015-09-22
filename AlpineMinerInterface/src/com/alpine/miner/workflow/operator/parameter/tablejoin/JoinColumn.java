/**
 * ClassName  JoinColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.tablejoin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author John Zhao
 *
 */
public class JoinColumn implements  XMLFragment  {
 

	private static final String ATTR_TABLE_ALIAS = "tableAlias";

	private static final String ATTR_COL_NAME = "columnName";

	private static final String ATTR_NEW_COL_NAME = "newColumnName";

	private static final String ATTR_COL_TYPE = "columnType";

	public  static final String TAG_NAME = "JoinColumnModel";

	String tableAlias;
	String columnName;
	String newColumnName;
	String columnType;
	
	
	/**
	 * @param alias
	 * @param columnName
	 * @param newColumnNam
	 * @param columnType
	 */
	public JoinColumn(String alias, String columnName, String newColumnNam,
			String columnType) {

		this.tableAlias = alias;
		this.columnName = columnName;
		this.newColumnName = newColumnNam;
		this.columnType = columnType;
	}
	public String getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getNewColumnName() {
		return newColumnName;
	}
	public void setNewColumnName(String newColumnNam) {
		this.newColumnName = newColumnNam;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_TABLE_ALIAS, getTableAlias());
		element.setAttribute(ATTR_COL_NAME, getColumnName());
		element.setAttribute(ATTR_NEW_COL_NAME, getNewColumnName());
		element.setAttribute(ATTR_COL_TYPE, getColumnType());
		
		return element;
	}
	/**
	 * @param item
	 * @return
	 */
	public static JoinColumn fromXMLElement(Element item) {
		String tableAlias=	item.getAttribute(ATTR_TABLE_ALIAS);
		String columnName=	item.getAttribute(ATTR_COL_NAME);
		String newColumnMame=	item.getAttribute(ATTR_NEW_COL_NAME);
		String columnType=	item.getAttribute(ATTR_COL_TYPE);
 
		 
		return new JoinColumn(tableAlias,columnName,newColumnMame,columnType);
	}

	public boolean equals(Object obj) {
		if(obj==null||(obj instanceof JoinColumn) ==false){
			return false;
		}
		JoinColumn joinColumn = (JoinColumn) obj; 
		return  (ParameterUtility.nullableEquales(joinColumn.getColumnName(),columnName)
				&& ParameterUtility.nullableEquales(joinColumn.getNewColumnName(),newColumnName)
				&& ParameterUtility.nullableEquales(joinColumn.getTableAlias(),tableAlias)) ;

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new JoinColumn(tableAlias, columnName, newColumnName, columnType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement
	 * (org.w3c.dom.Element)
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