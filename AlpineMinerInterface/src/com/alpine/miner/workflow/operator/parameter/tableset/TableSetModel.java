/**
 * ClassName  TableSetModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-10
 *
 * COPYRIGHT (C) 2010,2012 Alpine Solutions. All Rights Reserved.
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
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 *
 */
public class TableSetModel  extends AbstractParameterObject {
	
	public static final String[] TABLE_SET_TYPE = new String[]{"UNION","UNION ALL","INTERSECT","EXCEPT"};//
	 
	//we keep  this tag name to compaitable with old version's flow file
	public static final String TAG_NAME = "TableSetModel"; 
	public static final String TAG_NAME_COLUMNMAP_LIST = "ColumnMapList";
	
	public static final String ATTR_NAME_SETTYPE = "setType";
	public static final String ATTR_NAME_FIRSTTABLE = "firstTable";
	
	

	String setType ="";


	String  firstTable ="";
	List<ColumnMap> columnMapList = new ArrayList<ColumnMap> (); 
	
	public TableSetModel(String setType, String firstTable,
			List<ColumnMap> columnMapList) {
		super();
		this.setType = setType;
		this.firstTable = firstTable;
		this.columnMapList = columnMapList;
	}
	
	
	public TableSetModel() {
	 
	}
	
	public ColumnMap getColumnMap(	ArrayList<ColumnMap> usedColumnMaps,String schemaName, String tableName){
		if(columnMapList!=null){
			for( ColumnMap columnMap:columnMapList){
				if(tableName!=null&&schemaName!=null&&columnMap!=null
						&&columnMap.getTableName().equals(tableName)
						&&columnMap.getSchemaName().equals(schemaName)){
					if(usedColumnMaps==null||absoluteHasObjectInList(usedColumnMaps,columnMap)==false){
						return columnMap;
					}
				}
			}
		}
		return null;
	}


	private boolean absoluteHasObjectInList(
			ArrayList<ColumnMap> usedColumnMaps, ColumnMap columnMap) {
		if(usedColumnMaps!=null){
			for(int i=0 ; i <usedColumnMaps.size();i++){
				if(usedColumnMaps.get(i) == columnMap){
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public TableSetModel clone() throws CloneNotSupportedException {
		TableSetModel clone = new TableSetModel( );
		clone.setSetType(getSetType()) ;
		clone.setFirstTable(getFirstTable()) ; 
		List<ColumnMap> columnMaps = new ArrayList<ColumnMap>();
		for( ColumnMap columnMap:columnMapList){
			columnMaps.add(columnMap.clone());
		}
		clone.setColumnMapList(columnMaps) ;
		return clone;
	 
	}

	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		out.append("setType =" +getSetType()+"\n");
		out.append("firstTable =" +getFirstTable()+"\n");
		out.append("columnMaps =" +columnMapList.toArray()+"\n");
	 
		return out.toString();
	}
	 

 

	public Element toXMLElement(Document xmlDoc){
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_NAME_SETTYPE, getSetType());
		element.setAttribute(ATTR_NAME_FIRSTTABLE, getFirstTable());
		
		if(getColumnMapList()!=null){
			for (Iterator<ColumnMap> iterator = getColumnMapList().iterator(); iterator.hasNext();) {
				ColumnMap columnMap = iterator.next();
				if(columnMap!=null){
					element.appendChild(columnMap.toXMLElement(xmlDoc));
				 
				}
				
			}
		}
		
		return element;
		
	}
	
	public static TableSetModel fromXMLElement(Element element) {
		List<ColumnMap> columnMapList = new ArrayList<ColumnMap>();
 
		NodeList columnMaps = element.getElementsByTagName(ColumnMap.TAG_NAME);
		String setType = element.getAttribute(ATTR_NAME_SETTYPE);		
		String firstTable = element.getAttribute(ATTR_NAME_FIRSTTABLE);
		for (int i = 0; i < columnMaps.getLength(); i++) {
			if (columnMaps.item(i) instanceof Element ) {
				columnMapList.add(ColumnMap.fromXMLElement(((Element)columnMaps.item(i))) );

			}
		}
		TableSetModel tableSetModel = new TableSetModel(setType, firstTable, columnMapList) ;
		return tableSetModel;

	}
	  
	 public boolean equals(Object obj) {
		 if(obj==null||(obj instanceof TableSetModel)==false){
			 return false;
		 }
		 TableSetModel target=(TableSetModel )obj;
		 return ParameterUtility.nullableEquales(setType, target.getSetType())
		 		&& ParameterUtility.nullableEquales(firstTable, target.getFirstTable())
		 		&& ListUtility.equalsIgnoreOrder(columnMapList,target.getColumnMapList());
 
		 
	 }
 

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		
		return TAG_NAME;
	}
	
	public String getSetType() {
		return setType;
	}


	public void setSetType(String setType) {
		this.setType = setType;
	}


	public String getFirstTable() {
		return firstTable;
	}


	public void setFirstTable(String firstTable) {
		this.firstTable = firstTable;
	}


	public List<ColumnMap> getColumnMapList() {
		return columnMapList;
	}


	public void setColumnMapList(List<ColumnMap> columnMapList) {
		this.columnMapList = columnMapList;
	}

	public boolean isValid() {

		if (StringUtil.isEmpty(this.setType)
				|| (setType.equals(TABLE_SET_TYPE[3])&&
						StringUtil.isEmpty(this.firstTable))) {
			return false;
		}
		List<ColumnMap> mapList = this.getColumnMapList();
		if (mapList == null) {
			return false;
		}
		for (int i = 0; i < mapList.size(); i++) {
			if (mapList.get(i) == null
					|| mapList.get(i).getTableColumns() == null
					|| mapList.get(i).getTableColumns().size() == 0) {
				return false;
			}
			List<String> columns = mapList.get(i).getTableColumns();
			for (int j = 0; j < columns.size(); j++) {
				if (StringUtil.isEmpty(columns.get(j)) == true) {
					return false;
				}
			}
		}

		return true;
	}

}