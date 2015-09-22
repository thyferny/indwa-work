/**
 * ClassName  HadoopJoinColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hadoopjoin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author Jeff Dong
 *
 */
public class HadoopJoinColumn implements  XMLFragment  {
 
	private static final String ATTR_COL_NAME = "columnName";

	private static final String ATTR_NEW_COL_NAME = "newColumnName";
	
	private static final String ATTR_COLUMN_TYPE = "columnType";
	
	private static final String ATTR_FILE_ID = "fileId";

	public  static final String TAG_NAME = "JoinColumnModel";

	String columnName;
	String newColumnName;
	String columnType;
	String fileId;
	
	public HadoopJoinColumn(String columnName, String newColumnName,String columnType,String fileId) {
		this.columnName = columnName;
		this.newColumnName = newColumnName;
		this.columnType=columnType;
		this.fileId = fileId;
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

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_COL_NAME, getColumnName());
		element.setAttribute(ATTR_NEW_COL_NAME, getNewColumnName());
		element.setAttribute(ATTR_COLUMN_TYPE, getColumnType());
		element.setAttribute(ATTR_FILE_ID, getFileId());
		return element;
	}
	/**
	 * @param item
	 * @return
	 */
	public static HadoopJoinColumn fromXMLElement(Element item) {
		String columnName=	item.getAttribute(ATTR_COL_NAME);
		String newColumnMame=	item.getAttribute(ATTR_NEW_COL_NAME);
		String columnType=	item.getAttribute(ATTR_COLUMN_TYPE);
		String fileId=	item.getAttribute(ATTR_FILE_ID);
		return new HadoopJoinColumn(columnName,newColumnMame,columnType,fileId);
	}

	public boolean equals(Object obj) {
		if(obj==null||(obj instanceof HadoopJoinColumn) ==false){
			return false;
		}
		HadoopJoinColumn joinColumn = (HadoopJoinColumn) obj; 
		return  ParameterUtility.nullableEquales(joinColumn.getColumnName(),columnName)
				&& ParameterUtility.nullableEquales(joinColumn.getNewColumnName(),newColumnName)
				&& ParameterUtility.nullableEquales(joinColumn.getColumnType(),columnType)
				&& ParameterUtility.nullableEquales(joinColumn.getFileId(),fileId);

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new HadoopJoinColumn(columnName, newColumnName,columnType,fileId);
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