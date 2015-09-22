/**
 * ClassName  ColumnMap.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-19
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.tableset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;

/**
 * @author zhaoyong
 *
 */
public class ColumnMap  extends AbstractParameterObject {
 
	//we keep  this tag name to compaitable with old version's flow file
	public static final String TAG_NAME = "ColumnMap"; 
	public static final String ATTR_TABLENAME = "TableName";
	public static final String ATTR_SCHEMANAME = "SchemaName";
	public static final String TAG_COLUMNNAME = "ColumnName";
	public static final String ATTR_NAME = "name";
	private static final String ATTR_TABLE_OPERATOR_UUID="operUUID";//uuid
	private String tableName ="";
	private String schemaName ="";
	String operatorUUID ="";
	public String getOperatorUUID() {
		return operatorUUID;
	}

	public void setOperatorUUID(String operatorUUID) {
		this.operatorUUID = operatorUUID;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}


	private List<String> tableColumns = new ArrayList<String> (); 
  
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getTableColumns() {
		return tableColumns;
	}

	public void setTableColumns(List<String> tableColumns) {
		this.tableColumns = tableColumns;
	}

	public ColumnMap(String schemaName,String tableName, List<String> tableColumns,String operatorUUID) {
		super();
		this.schemaName=schemaName;
		this.tableName = tableName;
		this.tableColumns = tableColumns;
		this.operatorUUID = operatorUUID;
	}

	@Override
	public ColumnMap clone() throws CloneNotSupportedException {
		ColumnMap clone = new ColumnMap( );
		clone.setTableName(getTableName()) ;
		clone.setSchemaName(getSchemaName()) ;
		clone.setOperatorUUID(getOperatorUUID()) ;
		List<String> newTableColumns = new ArrayList<String>();
		for(String column:tableColumns){
			newTableColumns.add(column);
		}
		clone.setTableColumns(newTableColumns) ;
		return clone;
	 
	}

	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		out.append("schemaName =" +schemaName+"\n");
		out.append("tableName =" +tableName+"\n");
		out.append("operatorUUID =" +operatorUUID+"\n");
		out.append("tableColumns =" +tableColumns.toArray()+"\n");
	 
		return out.toString();
	}
	 

	/**
	 * 
	 */
	private ColumnMap() {
		 
	} 

	public Element toXMLElement(Document xmlDoc){
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_TABLENAME, getTableName()); 
		element.setAttribute(ATTR_SCHEMANAME, getSchemaName());
		element.setAttribute(ATTR_TABLE_OPERATOR_UUID, getOperatorUUID());
		if(getTableColumns()!=null){
			for (Iterator iterator = getTableColumns().iterator(); iterator.hasNext();) {
				String columnName = (String) iterator.next();
				if(columnName!=null){
					Element groupByEle=xmlDoc.createElement(TAG_COLUMNNAME );
					groupByEle.setAttribute(ATTR_NAME, columnName);
					element.appendChild(groupByEle);
				 
				}
				
			}
		}
		
		return element;
		
	}
	
	public static ColumnMap fromXMLElement(Element element) {
		List<String> tableColumns = new ArrayList<String>();
 
		NodeList columnNames = element.getElementsByTagName(TAG_COLUMNNAME);
		String tableName = element.getAttribute(ATTR_TABLENAME);
		String schemaName = element.getAttribute(ATTR_SCHEMANAME);
		String uuid = element.getAttribute(ATTR_TABLE_OPERATOR_UUID);
		for (int i = 0; i < columnNames.getLength(); i++) {
			if (columnNames.item(i) instanceof Element ) {
				String columnName=((Element)columnNames.item(i)).getAttribute(ATTR_NAME);
				
				tableColumns.add(columnName);

			}
		}
	 

		ColumnMap columnMap = new ColumnMap(schemaName,tableName, tableColumns,uuid);
		return columnMap;

	}
	  
	 public boolean equals(Object obj) {
		
		 if(obj==null||(obj instanceof ColumnMap)==false){
			 return false;
		 }
		 
		 ColumnMap target=(ColumnMap )obj;
		 return ParameterUtility.nullableEquales(tableName, target.getTableName())
	 		&& ParameterUtility.nullableEquales(schemaName, target.getSchemaName())
	 		&& equalsWithOrder(tableColumns, target.getTableColumns());
		  
		 
	 }
 

	/**
	 * @param tableColumns2
	 * @param tableColumns3
	 * @return
	 */
	private boolean equalsWithOrder(List<?> source,
			List<?> target) {
			if(source==target){
				return true;
			}	 
			else if (source==null&&target!=null){
				return false;
			}
			else if (source!=null&&target==null){
				return false;
			}
			else if (source.size()!=target.size()){
				return false;
			}
			else{
				for(int i=0;i<source.size();i++){
					if(source.get(i)!=null&&source.get(i).equals(target.get(i))==false){
						return false;
					}
				}
				 
				return true;
			} 
 
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		
		return TAG_NAME;
	}

}