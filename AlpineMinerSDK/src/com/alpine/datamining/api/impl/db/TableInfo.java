/**
 * ClassName  TableInfo.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;
/**
 * @author John Zhao
 *
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.utility.db.Resources;
import com.alpine.utility.db.TableColumnMetaInfo;

/**
 * @author John Zha0
 *
 */
public class TableInfo {
	@Override
	public String toString() {
		return "TableInfo [schema=" + schema + ", tableName=" + tableName
				+ ", tableType=" + tableType + "]";
	}
	public static final String TYPE_TABLE=Resources.TableType;
	public static final String TYPE_VIEW=Resources.ViewType;
	private String tableName;
	private String schema;
	private  List<TableColumnMetaInfo> columns;
	
	
    private String tableType;


	// sometime you are not expected to analysis all the columns
	public TableInfo(String schema,String tableName, List<TableColumnMetaInfo> columns){
		this.schema=schema;
		this.tableName=tableName;
		this.columns=columns;
		//default value
		this.tableType=TYPE_TABLE;
	}
	public TableInfo(String schema,String tableName, List<TableColumnMetaInfo> columns,String tableType){
		this.schema=schema;
		this.tableName=tableName;
		this.columns=columns;
		//default value
		if(tableType==null||tableType.trim().length()==0){
			this.tableType=TYPE_TABLE;
		}else{
			this.tableType=tableType;
		}
	}
	
	
	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public  List<TableColumnMetaInfo>  getColumns() {
		return columns;
	}

	public void setColumns( List<TableColumnMetaInfo>  columns) {
		this.columns = columns;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * @return
	 */
	public List<String> getColumnNames() {
		List<String> res= new ArrayList<String>();
		if(columns!=null&&columns.size()>0){
			for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
				TableColumnMetaInfo colInfo = (TableColumnMetaInfo) iterator.next();
				res.add(colInfo.getColumnName());
			}
		}
		return res;
	}
	/**
	 * @return
	 */
	public String getColumnNameString() {
		StringBuffer sb=new StringBuffer();
		if(columns!=null&&columns.size()>0){
			int i=0;
			for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
				TableColumnMetaInfo colInfo = (TableColumnMetaInfo) iterator.next();
				if(i!=0){
					sb.append(",");
				}
				if(colInfo!=null){	
					sb.append(colInfo.getColumnName());
				}
				i++;
			}
		}
		return sb.toString();
	}
	


}
