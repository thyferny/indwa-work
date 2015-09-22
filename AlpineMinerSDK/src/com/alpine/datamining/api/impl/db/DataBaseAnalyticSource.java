/**
 * ClassName DataBaseAnalyticSource.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.impl.AbstractAnalyticSource;
import com.alpine.datamining.api.impl.DBTableSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.SQLAnalysisConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.common.VariableModelUtility;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.xml.XmlDocManager;

/**
 * @author John Zhao
 * 
 */
public class DataBaseAnalyticSource extends AbstractAnalyticSource {
    private static final Logger itsLogger = Logger.getLogger(DataBaseAnalyticSource.class);
    private static String UNKNOWN_DB_SOURCE = "UNKNOW_DB_SOURCE";
	DataBaseInfo dataBaseInfo;
	TableInfo tableInfo;
	Connection conenction;
	private String sourceName; 

	public Connection getConnection() {
		
		return conenction;
	}

	public void setConenction(Connection conenction) {
		this.conenction = conenction;
	}

	public DataBaseAnalyticSource(String system, String url, String userName,
			String password, String schema, String tableName,List<TableColumnMetaInfo> columns, String useSSL) {
		this.dataBaseInfo = new DataBaseInfo(system, url, userName, password,useSSL);
		this.tableInfo = new TableInfo(schema, tableName,columns);

	}
	
	public DataBaseAnalyticSource(String dbsystem, String url, String userName,
			String password, String schema, String tableName,String useSSL) {
		this(dbsystem,   url,   userName,  password,   schema,   tableName,null,useSSL);
	}
 

	/**
	 * 
	 */
	public DataBaseAnalyticSource() {
	}

	public DataBaseInfo getDataBaseInfo() {
		return dataBaseInfo;
	}

	public void setDataBaseInfo(DataBaseInfo dataBaseInfo) {
		this.dataBaseInfo = dataBaseInfo;
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	@Override
	public String toString() {
		AnalyticConfiguration config = getAnalyticConfig();
		Locale locale = config.getLocale();
		StringBuffer sb= new StringBuffer();
	    if(dataBaseInfo==null||tableInfo==null)//error
	    	{itsLogger.error("DataBaseAnalyticSource toString error db or table can not be null");
	    	return "";
	    }
		String blank="          ";
		sb.append(SDKLanguagePack.getMessage(SDKLanguagePack.Database_Connection,locale)+blank);
		sb.append(dataBaseInfo.getUrl()+"\n");
		
		sb.append(SDKLanguagePack.getMessage(SDKLanguagePack.Table_Name,locale)+blank);
		if(tableInfo.getSchema()==null||tableInfo.getSchema().trim().length()==0){
			sb.append(tableInfo.getTableName()+"\n");
		}else{
			sb.append(tableInfo.getSchema()+"."+tableInfo.getTableName()+"\n");
		}
		

		//selected columnes
		if(config.getColumnNames()!=null&&config.getColumnNames().trim().length()!=0){
			sb.append(SDKLanguagePack.getMessage(SDKLanguagePack.Table_Columns,locale)+blank+config.getColumnNames()+"\n");
		}else{//use db table columns
			sb.append(SDKLanguagePack.getMessage(SDKLanguagePack.Table_Columns,locale)+blank+tableInfo.getColumnNameString()+"\n");
		}
		return sb.toString();
	}
	
	public String getDataSourceType() {
		if (dataBaseInfo != null) {
			return dataBaseInfo.getSystem();
		} else {
			return DataBaseAnalyticSource.UNKNOWN_DB_SOURCE;
		}
	}


	static boolean dePasswordFlag = false;
	@Override
	public void setSourceInfoByNodeIndex(XmlDocManager opTypeXmlManager,
			Node opNode, int index,Map<String,String> variableMap) {
		ArrayList<Node> parameterNodeList;
		ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode,
				"InPutFieldList");
		if (nodes != null && nodes.size() > 0) {
			String url = null;
			String userName = null;
			String password = null;
			String system = null;
			String schema = null;
			String tableName = null;
			String tableType = null;

			Node inputNode = nodes.get(index);
			parameterNodeList = opTypeXmlManager.getNodeList(inputNode,
					PARAMETER);
			String useSSL ="false"; //default value 
			for (Iterator<Node> iterator = parameterNodeList.iterator(); iterator
					.hasNext();) {
				Node pnode = iterator.next();

				if (((Element) pnode).getAttribute(KEY).equals("url")) {
					url = ((Element) pnode).getAttribute(VALUE);
				} else if (((Element) pnode).getAttribute(KEY).equals(
						"system")) {
					system = ((Element) pnode).getAttribute(VALUE);
				} else if (((Element) pnode).getAttribute(KEY).equals(
						"username")) {
					userName = ((Element) pnode).getAttribute(VALUE);
				} else if (((Element) pnode).getAttribute(KEY).equals(
						"password")) {
					String enpassword = ((Element) pnode).getAttribute(VALUE);
					password = XmlDocManager.decryptedPassword(enpassword);
				} else if (((Element) pnode).getAttribute(KEY)
						.equals("table")) {
					tableName = ((Element) pnode).getAttribute(VALUE);

				} else if (((Element) pnode).getAttribute(KEY).equals(
						"schema")) {
					schema = ((Element) pnode).getAttribute(VALUE);

				}
				else if (((Element) pnode).getAttribute(KEY).equals(
						"tableType")) {
					tableType = ((Element) pnode).getAttribute(VALUE);

				}
				else if (((Element) pnode).getAttribute(KEY).equals(
						"useSSL")) {
					useSSL = ((Element) pnode).getAttribute(VALUE);

				}
			}
	
			schema=VariableModelUtility.getReplaceValue(variableMap, schema);
			tableName=VariableModelUtility.getReplaceValue(variableMap, tableName);
			
			List<TableColumnMetaInfo> columns = inputFieldInfo(
					opTypeXmlManager, inputNode);

			for(TableColumnMetaInfo column:columns){
				String columnName = column.getColumnName();
				columnName=VariableModelUtility.getReplaceValue(variableMap, columnName);
				column.setColumnName(columnName);
			}
			
			setDataBaseInfo(new DataBaseInfo(system, url, userName,
					password,useSSL));

			setTableInfo(new TableInfo(schema, tableName, columns,
					tableType));

			Object config = getAnalyticConfig();
			if (config instanceof SQLAnalysisConfig && !dePasswordFlag) {
				String pass = ((SQLAnalysisConfig) config).getPassword();
				if (!StringUtil.isEmpty(pass)) {
					((SQLAnalysisConfig)getAnalyticConfig()).setPassword(XmlDocManager
							.decryptedPassword(pass));
					dePasswordFlag = true;
				}
			}

		} else {
				Object config = getAnalyticConfig();
				if (config instanceof DBTableSelectorConfig) {
					String pass = ((DBTableSelectorConfig) config)
							.getPassword();
					((DBTableSelectorConfig)getAnalyticConfig()).setPassword(XmlDocManager
							.decryptedPassword(pass));
				}
				if (config instanceof SQLAnalysisConfig) {
					String pass = ((SQLAnalysisConfig) config).getPassword();
					((SQLAnalysisConfig)getAnalyticConfig()).setPassword(XmlDocManager
							.decryptedPassword(pass));
				}
		}
	}

	@Override
	public void setNameAlias(String sourceName) {
		this.sourceName=sourceName;
		
	}

	@Override
	public String getNameAlias() {
		return sourceName;
	}


}
