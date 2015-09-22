/**
 * ClassName  JoinTable.java
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
public class JoinTable implements XMLFragment {
 
	public static final String TAG_NAME="JoinTableModel";
	
	private static final String ATTR_OPERATOR_ID="operID";//uuid
 	private static final String ATTR_SCHEMA = 	"schema";
	private static final String ATTR_TABLE = "table";
	private static final String ATTR_ALIAS = "alias";
	
	String schema;
	String table;
	String alias;
	String operatorModelID;
	
	

	public JoinTable(String schema,String table,String alias,String operatorModelID){
		this.schema=schema;
		this.table=table;
		this.alias=alias;
		this.operatorModelID = operatorModelID;
	}
	
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
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
		element.setAttribute(ATTR_SCHEMA, getSchema());
		element.setAttribute(ATTR_TABLE, getTable());
		element.setAttribute(ATTR_ALIAS, getAlias());
		element.setAttribute(ATTR_OPERATOR_ID, getOperatorModelID());
		return element;
	}

	/**
	 * @param item
	 * @return
	 */
	public static JoinTable fromXMLElement(Element item) {
		String schema=	item.getAttribute(ATTR_SCHEMA);
		String table=item.getAttribute(ATTR_TABLE);
		String alias=item.getAttribute(ATTR_ALIAS);
		String uuid= item.getAttribute(ATTR_OPERATOR_ID);
		return new JoinTable(schema,table,alias,uuid);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new JoinTable(schema,table,alias,operatorModelID);
	}

	
	public   boolean equals( Object obj){
		if(obj==null||(obj instanceof JoinTable) == false){
			return false;
		}
		JoinTable joinTable=(JoinTable ) obj;
		return ParameterUtility.nullableEquales(joinTable.getSchema(), schema)
				&& ParameterUtility.nullableEquales(joinTable.getTable(), table)
				&& ParameterUtility.nullableEquales(joinTable.getAlias(), alias);
			//	&& ParameterUtility.nullableEquales(joinTable.getOperatorModelID(), operatorModelID);
	 
		
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