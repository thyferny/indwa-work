/**
 * ClassName  HadoopJoinCondition.java
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
public class HadoopJoinCondition implements XMLFragment {

	public static final String TAG_NAME = "JoinConditionModel";
	
	public static final String KEYCOLUMN = "keyColumn";
	
	public static final String KEYCOLUMN_ALIAS = "keyColumnAlias";
	
	public static final String ATTR_FILE_ID = "fileId";

	String fileId;	
	String keyColumn;
	String keyColumnAlias;

	public HadoopJoinCondition(String keyColumn, String fileId,String keyColumnAlias) {
		this.keyColumn = keyColumn;
		this.fileId = fileId;
		this.keyColumnAlias=keyColumnAlias;
	}

	public String getFileId() {
		return fileId;
	}


	public void setFileId(String fileId) {
		this.fileId = fileId;
	}


	public String getKeyColumn() {
		return keyColumn;
	}
	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}
	
	
	public String getKeyColumnAlias() {
		return keyColumnAlias;
	}

	public void setKeyColumnAlias(String keyColumnAlias) {
		this.keyColumnAlias = keyColumnAlias;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_FILE_ID, getFileId());
		element.setAttribute(KEYCOLUMN, getKeyColumn());
		element.setAttribute(KEYCOLUMN_ALIAS, getKeyColumnAlias());
		return element;
	}
	/**
	 * @param item
	 * @return
	 */
	public static HadoopJoinCondition fromXMLElement(Element item) {
		String fileId = item.getAttribute(ATTR_FILE_ID);
		String keyColumn=item.getAttribute(KEYCOLUMN);
		String keyColumnAlias=item.getAttribute(KEYCOLUMN_ALIAS);
		HadoopJoinCondition model=new HadoopJoinCondition(keyColumn,fileId,keyColumnAlias);
		return model;
	}

	 public boolean equals(Object obj) {
		 if(obj==null||obj instanceof HadoopJoinCondition ==false){
			 return false;
		 }
		 HadoopJoinCondition target=(HadoopJoinCondition) obj ;
		 //avoid the the null point
		 
		 return ParameterUtility.nullableEquales( target.getFileId(),getFileId())
				 	&&ParameterUtility.nullableEquales( target.getKeyColumn(),getKeyColumn());
	 }
	 
		@Override
		public Object clone() throws CloneNotSupportedException {
			HadoopJoinCondition model=new HadoopJoinCondition(keyColumn,fileId,keyColumnAlias);
			return model;
		}
		
		
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		this.setFileId(element.getAttribute(ATTR_FILE_ID));
		String leftColumn=element.getAttribute(KEYCOLUMN);
		setKeyColumn(leftColumn);
		
	}
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public  String getXMLTagName() {
		return TAG_NAME;
	}
} 
