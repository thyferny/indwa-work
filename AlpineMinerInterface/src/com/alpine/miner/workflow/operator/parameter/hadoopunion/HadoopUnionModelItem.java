/**
 * ClassName  HadoopJoinColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hadoopunion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;
import com.alpine.utility.common.ListUtility;

/**
 * @author Jeff Dong
 *
 */
public class HadoopUnionModelItem implements  XMLFragment  {

	public  static final String TAG_NAME = "unionColumnModel";
	
	private static final String ATTR_COL_NAME = "columnName";

	private static final String ATTR_COLUMN_TYPE = "columnType";
	
	

	List<HadoopUnionSourceColumn> mappingColumns;
	public List<HadoopUnionSourceColumn> getMappingColumns() {
		return mappingColumns;
	}

	public void setMappingColumns(List<HadoopUnionSourceColumn> mappingColumns) {
		this.mappingColumns = mappingColumns;
	}
	String columnName ="";
	String columnType ="";
 	
	public HadoopUnionModelItem(String columnName, String columnType ,List<HadoopUnionSourceColumn> mappingColumns) {
		this.columnName = columnName;
		this.columnType=columnType;
		this.mappingColumns =mappingColumns;
	}

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
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
		element.setAttribute(ATTR_COL_NAME, getColumnName());
		element.setAttribute(ATTR_COLUMN_TYPE, getColumnType());
		if(mappingColumns!=null&&mappingColumns.size()>0){
			for (HadoopUnionSourceColumn column : mappingColumns) {
				element.appendChild(column.toXMLElement(xmlDoc)) ;
			}
		}
		
		
		return element;
	}
	/**
	 * @param item
	 * @return
	 */
	public static HadoopUnionModelItem fromXMLElement(Element item) {
		String columnName=	item.getAttribute(ATTR_COL_NAME);
		String columnType=	item.getAttribute(ATTR_COLUMN_TYPE);
		NodeList columnNodeds = item.getElementsByTagName(HadoopUnionSourceColumn.TAG_NAME);
		List<HadoopUnionSourceColumn> mapColumns = new ArrayList<HadoopUnionSourceColumn>(); 
		for (int i = 0; i < columnNodeds.getLength(); i++) {
			if (columnNodeds.item(i) instanceof Element) {
				HadoopUnionSourceColumn joinTable = HadoopUnionSourceColumn
						.fromXMLElement((Element) columnNodeds.item(i));
				mapColumns.add(joinTable);

			}
		}
 
		return new HadoopUnionModelItem(columnName,columnType,mapColumns);
	}

	public boolean equals(Object obj) {
		if(obj==null||(obj instanceof HadoopUnionModelItem) ==false){
			return false;
		}
		HadoopUnionModelItem joinColumn = (HadoopUnionModelItem) obj; 
		return  ParameterUtility.nullableEquales(joinColumn.getColumnName(),columnName)
				&& ParameterUtility.nullableEquales(joinColumn.getColumnType(),columnType)
				&&ListUtility.equalsFocusOrder(joinColumn.getMappingColumns(), mappingColumns);

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		List<HadoopUnionSourceColumn> newMappingColumns = new ArrayList<HadoopUnionSourceColumn> (); 
		if(mappingColumns!=null){
			for (HadoopUnionSourceColumn hadoopUnionSourceColumn : mappingColumns) {
				newMappingColumns.add((HadoopUnionSourceColumn)hadoopUnionSourceColumn.clone()) ;
			}
		}
		return new HadoopUnionModelItem(columnName, columnType,newMappingColumns);
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

	}
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
}