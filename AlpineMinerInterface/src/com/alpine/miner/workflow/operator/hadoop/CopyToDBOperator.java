
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.AlpineConncetionException;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.xml.XmlDocManager;


public class CopyToDBOperator extends DataOperationOperator {

    private static final Logger itsLogger=Logger.getLogger(CopyToDBOperator.class);
	
	public static final List<String> parameterNames = Arrays
			.asList(new String[] { 
					OperatorParameter.NAME_dBConnectionName,
					OperatorParameter.NAME_schemaName,
					OperatorParameter.NAME_HD_copyToTableName,
					OperatorParameter.NAME_HD_ifDataExists});
	
	public CopyToDBOperator() {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
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
			} else if (paraName.equals(OperatorParameter.NAME_HD_copyToTableName)) {
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
					if (isSchemaContain==false) {
						if (!invalidParameterList
								.contains(OperatorParameter.NAME_schemaName)) {
							invalidParameterList
									.add(OperatorParameter.NAME_schemaName);
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
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator,false);
		if(StringUtil.isEmpty(message) ==true){ 
			message = super.validateStoreResult(precedingOperator);
		}
		return message;
	}
	

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameterList = super.fromXML(opTypeXmlManager, opNode);
		
		attachConn(opTypeXmlManager, opNode, operatorParameterList);
		
		return operatorParameterList;
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
	public void saveInputFieldList(Document xmlDoc, Element operator_element,
			boolean addSuffixToOutput) {
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("xmlDocument is:\n"+(null==xmlDoc?null:xmlDoc.toString()));
		}
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputFileInfo) {
				OperatorInputFileInfo info = (OperatorInputFileInfo) obj;
				Element ele = xmlDoc.createElement("InPutFieldList");
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_connetionName, info.getConnectionName());
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsHostname , info.getHdfsHostname() );
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsPort , info.getHdfsPort() );
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_version, info.getVersion());
	
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_jobHostname , info.getJobHostname() );
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_jobPort , info.getJobPort() );
				
				 
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_user , info.getUser());
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_group  , info.getGroup());
				
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_format , info.getHadoopFileFormat());
				setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_fileName , info.getHadoopFileName());
				
				if(info.getColumnInfo()!=null){
					ele.appendChild(info.getColumnInfo().toXMLElement(xmlDoc));
				}
				
				operator_element.appendChild(ele);
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("Element is ["+ele+"]");
				}
				
			}
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("obj["+obj+"]is not an instanceof OperatorInputFileInfo");
			}
		}
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
					OperatorParameter.NAME_HD_copyToTableName).getValue());
			tableInfo.setSchema((String) getOperatorParameter(
					OperatorParameter.NAME_schemaName).getValue());
			tableInfo.setConnectionName(connName);
			tableInfo.setSystem(conn.getDbType());
			tableInfo.setTableType(Resources.TableType);
			
			
			OperatorInputFileInfo parentFileInfo = null;

			List<Object> inputList = getOperatorInputList();
			if(inputList==null){
				return outputObjectList;
			}
			for (Object obj : inputList) {
				if (obj instanceof OperatorInputFileInfo) {
					parentFileInfo=(OperatorInputFileInfo)obj;
				}
			}
			String dbType = tableInfo.getSystem();
			List<String[]> fieldColumns = new ArrayList<String[]>();
			if(parentFileInfo.getColumnInfo()!=null){
				FileStructureModel columnInfo = parentFileInfo.getColumnInfo();
				List<String> columnNameList = columnInfo.getColumnNameList();
				List<String> columnTypeList = columnInfo.getColumnTypeList();
				if(columnNameList!=null&&columnTypeList!=null){
					for(int i=0;i<columnNameList.size();i++){
						String columnName = columnNameList.get(i);
						String columnType = columnTypeList.get(i);
						String newColumnType = AlpineUtil.guessDBDataType(dbType, columnType);
						fieldColumns.add(new String[]{columnName,newColumnType});
					}
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
		List<Object> list = getParentOutputClassList();
		List<OperatorInputFileInfo> dbList = new ArrayList<OperatorInputFileInfo>();
		if (list != null) {
			for (Object obj : list) {
				if (obj instanceof OperatorInputFileInfo) {
					dbList.add((OperatorInputFileInfo) obj);
				}
			}
		}
		if (dbList.size() == 1) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_HD_ifDataExists)){
			return "Drop";
		}
		return super.getOperatorParameterDefaultValue(paraName);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.HP_COPYTODB_OPERATOR,locale);
	}

	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
	
	protected String validateMultipleInput(Operator precedingOperator) {
	 
		if (getParentOperators()!=null&&getParentOperators().size()!=0) {
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.CANT_LINK_MUTIL_DATASOURCE,locale),
					this.getToolTipTypeName());
		}else{
			return "";
		}
	}
}
