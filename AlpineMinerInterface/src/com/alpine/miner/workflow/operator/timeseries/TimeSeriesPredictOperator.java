package com.alpine.miner.workflow.operator.timeseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.AlpineConncetionException;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class TimeSeriesPredictOperator extends DataOperationOperator {
    private static final Logger itsLogger=Logger.getLogger(TimeSeriesPredictOperator.class);

    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dBConnectionName,
			OperatorParameter.NAME_Ahead_Number,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
	});
 	

	public TimeSeriesPredictOperator() {
		super(parameterNames);
		addInputClass(EngineModel.MPDE_TYPE_TIMESERIES);	
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TIMESERIES_PREDICTION_OPERATOR,locale);
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
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		
		String connName=null;
		String schemaName=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			 
			if(paraName.equals(OperatorParameter.NAME_dBConnectionName)){
				connName=paraValue;
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				schemaName=paraValue;
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}
		}
		schemaName=VariableModelUtility.getReplaceValue(variableModel, schemaName);
		if(!StringUtil.isEmpty(connName)
				&&!StringUtil.isEmpty(schemaName)){
			DBResourceManagerIfc dbManager=DBResourceManagerFactory.INSTANCE.getManager();
			DbConnectionInfo connInfo=null;
			try {
				connInfo=dbManager.getDBConnection(userName, connName, resourceType);
			} catch (Exception e1) {
				itsLogger.error(e1.getMessage(),e1);
				if(!invalidParameterList.contains(OperatorParameter.NAME_dBConnectionName)){
					invalidParameterList.add(OperatorParameter.NAME_dBConnectionName);
				}
			}
						
			try {
				if(connInfo!=null){
					String[] schemaList = dbManager.getSchemaList(userName, connName, resourceType);
					boolean isSchemaContain=false;
					for(String s:schemaList){
						if(s.equals(schemaName)){
							isSchemaContain=true;
							break;
						}
					}
					if(!isSchemaContain){
						if(!invalidParameterList.contains(OperatorParameter.NAME_outputSchema)){
							invalidParameterList.add(OperatorParameter.NAME_outputSchema);
						}
					}
				}else{
					if(!invalidParameterList.contains(OperatorParameter.NAME_dBConnectionName)){
						invalidParameterList.add(OperatorParameter.NAME_dBConnectionName);
					}
					if(!invalidParameterList.contains(OperatorParameter.NAME_outputSchema)){
						invalidParameterList.add(OperatorParameter.NAME_outputSchema);
					}
				}
			} catch (Exception e) {
				if(!invalidParameterList.contains(OperatorParameter.NAME_dBConnectionName)){
					invalidParameterList.add(OperatorParameter.NAME_dBConnectionName);
				}
				if(!invalidParameterList.contains(OperatorParameter.NAME_outputSchema)){
					invalidParameterList.add(OperatorParameter.NAME_outputSchema);
				}
				itsLogger.error(e.getMessage(),e);
			}
		}	
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_Ahead_Number)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,10000,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}
		}
			invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
			if(invalidParameterList.size()==0){
				return true;
			}else{
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
			tableInfo.setUrl(conn.getUrl());
			tableInfo.setPassword(conn.getPassword());
			tableInfo.setUsername(conn.getDbuser());

			tableInfo.setConnectionName(connName);
			tableInfo.setSystem(conn.getDbType());
			tableInfo.setOperatorUUID(getOperModel().getUUID()) ;
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
	public void saveInputFieldList(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		String system = null;
		String url = null;
		String schema = null;
		String table = null;
	 
		String connUserName = null;
		String password = null;
		DBResourceManagerIfc dbManager=DBResourceManagerFactory.INSTANCE.getManager();
		List<DbConnectionInfo> dbInfos = dbManager.getDBConnectionList(userName);
		
		for(DbConnectionInfo dbInfo:dbInfos){
			if(dbInfo.getResourceType().equals(resourceType)
					&&dbInfo.getModifiedUser().equals(userName)){
				DbConnection conn=dbInfo.getConnection();
				String connName=(String)getOperatorParameter(OperatorParameter.NAME_dBConnectionName).getValue();
				if(!conn.getConnName().equals(connName)){
					continue;
				}
				system=conn.getDbType();
				url=conn.getUrl();
				connUserName=conn.getDbuser();
				password=conn.getPassword();
				break;
			}
		}
	
		schema = (String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue();

		table = (String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue();

		Element ele = xmlDoc.createElement("InPutFieldList");
 
		Element parameter1 = xmlDoc.createElement("Parameter");
		parameter1.setAttribute("key", OperatorParameter.NAME_URL);
		parameter1.setAttribute("value", url);
		ele.appendChild(parameter1);
		Element parameter2 = xmlDoc.createElement("Parameter");
		parameter2.setAttribute("key", "schema");
		parameter2.setAttribute("value", schema);
		ele.appendChild(parameter2);
		Element parameter3 = xmlDoc.createElement("Parameter");
		if(addSuffixToOutput){
			String newTableName=StringHandler.addPrefix(table, connUserName);
			parameter3.setAttribute("key", "table");
			parameter3.setAttribute("value", newTableName);
			ele.appendChild(parameter3);
		}else{
			parameter3.setAttribute("key", "table");
			parameter3.setAttribute("value", table);
			ele.appendChild(parameter3);
		}
		
 
		Element parameter4 = xmlDoc.createElement("Parameter");
		parameter4.setAttribute("key", "username");
		parameter4.setAttribute("value", connUserName);
		ele.appendChild(parameter4);
		Element parameter5 = xmlDoc.createElement("Parameter");
		parameter5.setAttribute("key", OperatorParameter.NAME_Password);
		parameter5.setAttribute("value",
				XmlDocManager.encryptedPassword(password));
		ele.appendChild(parameter5);
		
		Element parameter7 = xmlDoc.createElement("Parameter");
		parameter7.setAttribute("key", OperatorParameter.NAME_System);
		parameter7.setAttribute("value", system);
		ele.appendChild(parameter7);
		
		element.appendChild(ele);
	}

	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof EngineModel){
					modelList.add((EngineModel)obj);
				}
			}
		}
		if(modelList.size()==1){
			return true;
		}else{
			return false;
		}
	}
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_Ahead_Number)){
			return "10";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
