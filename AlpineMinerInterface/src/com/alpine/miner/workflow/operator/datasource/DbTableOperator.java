/**
 * ClassName DbTableOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.AlpineConncetionException;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhaoyong
 * 
 */
public class DbTableOperator extends AbstractOperator {
    private static final Logger itsLogger=Logger.getLogger(DbTableOperator.class);

    public static final List<String> parameterNames = Arrays
			.asList(new String[] { OperatorParameter.NAME_dBConnectionName,
					OperatorParameter.NAME_schemaName,
					OperatorParameter.NAME_tableName });

	public DbTableOperator() {
		super(parameterNames);
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.DBTABLE_OPERATOR,locale);
	}

	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameterList = super.fromXML(opTypeXmlManager, opNode);
		
		attachConn(opTypeXmlManager, opNode, operatorParameterList);
		
		return operatorParameterList;
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		String connName = null;
		String schemaName = null;
		String tableName = null;
		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			String paraValue = (String) para.getValue();
			if (paraName.equals(OperatorParameter.NAME_dBConnectionName)) {
				connName = paraValue;
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_schemaName)) {
				schemaName = paraValue;
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_tableName)) {
				tableName = paraValue;
				validateNull(invalidParameterList, paraName, paraValue);
			}
		}
		if (!StringUtil.isEmpty(connName) && !StringUtil.isEmpty(schemaName)
				&& !StringUtil.isEmpty(tableName)) {
			DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE
					.getManager();
			DbConnectionInfo connInfo = null;
			try {
				connInfo = dbManager.getDBConnection(userName, connName,
						resourceType);
			} catch (Exception e1) {
				itsLogger.error(e1.getMessage(),e1);
				if (!invalidParameterList
						.contains(OperatorParameter.NAME_dBConnectionName)) {
					invalidParameterList
							.add(OperatorParameter.NAME_dBConnectionName);
				}
			}

			try {
				if (connInfo != null) {
					String[] schemaList = dbManager.getSchemaList(userName,
							connName, resourceType);
					boolean isSchemaContain = false;
					for (String s : schemaList) {
						if (s.equals(schemaName)) {
							isSchemaContain = true;
							break;
						}
					}
					if (isSchemaContain) {
						String[] tableList = dbManager.getTableList(userName,
								connName, schemaName, resourceType);
						boolean isTableContain = false;
						for (String s : tableList) {
							if (s.equals(tableName)) {
								isTableContain = true;
								break;
							}
						}
						if (!isTableContain) {
							if (!invalidParameterList
									.contains(OperatorParameter.NAME_tableName)) {
								invalidParameterList
										.add(OperatorParameter.NAME_tableName);
							}
						}
					} else {
						if (!invalidParameterList
								.contains(OperatorParameter.NAME_schemaName)) {
							invalidParameterList
									.add(OperatorParameter.NAME_schemaName);
						}
						if (!invalidParameterList
								.contains(OperatorParameter.NAME_tableName)) {
							invalidParameterList
									.add(OperatorParameter.NAME_tableName);
						}
					}
				} else {
					if (!invalidParameterList
							.contains(OperatorParameter.NAME_dBConnectionName)) {
						invalidParameterList
								.add(OperatorParameter.NAME_dBConnectionName);
					}
					if (!invalidParameterList
							.contains(OperatorParameter.NAME_schemaName)) {
						invalidParameterList
								.add(OperatorParameter.NAME_schemaName);
					}
					if (!invalidParameterList
							.contains(OperatorParameter.NAME_tableName)) {
						invalidParameterList
								.add(OperatorParameter.NAME_tableName);
					}
				}
			} catch (Exception e) {
				if (!invalidParameterList
						.contains(OperatorParameter.NAME_dBConnectionName)) {
					invalidParameterList
							.add(OperatorParameter.NAME_dBConnectionName);
				}
				if (!invalidParameterList
						.contains(OperatorParameter.NAME_schemaName)) {
					invalidParameterList.add(OperatorParameter.NAME_schemaName);
				}
				if (!invalidParameterList
						.contains(OperatorParameter.NAME_tableName)) {
					invalidParameterList.add(OperatorParameter.NAME_tableName);
				}
				itsLogger.error(e.getMessage(),e);
			}
		}

		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Object> getOperatorInputList() {
		return getOperatorOutputList();
	}

	@Override
	public List<Object> getOperatorOutputList() {
		DBResourceManagerIfc dbManager = DBResourceManagerFactory.INSTANCE
				.getManager();
		String connName = (String) getOperatorParameter(
				OperatorParameter.NAME_dBConnectionName).getValue();
		List<Object> outputObjectList = new ArrayList<Object>();
		if(StringUtil.isEmpty(connName)){
			return outputObjectList;
		}
		DbConnectionInfo dbInfo;
		try {
			dbInfo = dbManager.getDBConnection(userName, connName,
					resourceType);
			DbConnection conn = dbInfo.getConnection();
			
			OperatorInputTableInfo tableInfo = new OperatorInputTableInfo();
			tableInfo.setOperatorUUID(getOperModel().getUUID()) ;
			tableInfo.setUrl(conn.getUrl());
			tableInfo.setUseSSL(conn.getUseSSL()) ;
			tableInfo.setPassword(conn.getPassword());
			tableInfo.setUsername(conn.getDbuser());

			tableInfo.setTable((String) getOperatorParameter(
					OperatorParameter.NAME_tableName).getValue());
			tableInfo.setSchema((String) getOperatorParameter(
					OperatorParameter.NAME_schemaName).getValue());
			tableInfo.setConnectionName(connName);
			tableInfo.setSystem(conn.getDbType());
			tableInfo.setTableType(Resources.TableType);
			
			// allTableList = dbUtil.getAllTableList(tableInfo.getSchema());
			// if(allTableList.contains(tableInfo.getTable())){
			// tableInfo.setTableType(DBMetaDataUtil.TABLE_TYPE_TABLE);
			// }else{
			// tableInfo.setTableType(DBMetaDataUtil.TABLE_TYPE_VIEW);
			// }
			List<String[]> fieldColumns = new ArrayList<String[]>();
			List<TableColumnMetaInfo> columnList = dbManager.loadColumnList(
					userName, connName, tableInfo.getSchema(), tableInfo
							.getTable(), resourceType);
			if(columnList!=null){
				for (TableColumnMetaInfo column : columnList) {
					String[] fieldColumn = new String[] { column.getColumnName(),
							column.getColumnsType() };
					fieldColumns.add(fieldColumn);
				}
			}
			tableInfo.setFieldColumns(fieldColumns);
			outputObjectList.add(tableInfo);
			return outputObjectList;
		}  catch (AlpineConncetionException e) {
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException("1019", e);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		}

		return null;
	}

	@Override
	public boolean isInputObjectsReady() {
		List<UIOperatorModel> childList = OperatorUtility.getChildList(this
				.getOperModel());
		if ((childList == null) || (childList.size() == 0)) {
			return false;
		} else {
			return true;
		}
	}

	public Map<String, String> refreshTableInfo() {
		Map<String, String> paraMap = OperatorUtility.refreshTableInfo(this,userName,resourceType);
		return paraMap;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);

		Map<String, String> paraMap = OperatorUtility.refreshTableInfo(this,userName,resourceType);
		if (paraMap == null)
			return;
		createSimpleElements(xmlDoc, element, paraMap.get("userName"),
				"userName");
		createSimpleElements(xmlDoc, element, paraMap.get("url"), "url");
		createSimpleElements(xmlDoc, element, paraMap.get("password"),
				"password");
		createSimpleElements(xmlDoc, element, paraMap.get("system"), "system");
		createSimpleElements(xmlDoc, element, paraMap.get("useSSL"), "useSSL");
	}

	@Override
	public void saveInputFieldList(Document xmlDoc, Element operatorElement,
			boolean addSuffixToOutput) {
		// do nothing
	}

	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;

	}
}
