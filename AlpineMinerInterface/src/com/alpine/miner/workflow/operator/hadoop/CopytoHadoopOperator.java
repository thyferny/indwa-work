/**
 * ClassName :SecurityFilter.java
 *
 * Version information: 2.8
 *
 * Date: 2012-08-22
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.hadoop.CopytoHadoopAnalyzer;
import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.ifc.HadoopConnectionManagerIfc;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.CSVFileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.db.AlpineConncetionException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

public class CopytoHadoopOperator extends HadoopOperator {
    private static final Logger itsLogger=Logger.getLogger(CopytoHadoopOperator.class);

    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_connetionName,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_copyToFileName,
			OperatorParameter.NAME_HD_ifFileExists
	});
	

	public static final String[] ifFileExistsOption = new String[]{
		CopytoHadoopConfig.OPTION_DROP, CopytoHadoopConfig.OPTION_APPEND, 
		CopytoHadoopConfig.OPTION_SKIP, CopytoHadoopConfig.OPTION_ERROR
	};

	public CopytoHadoopOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputFileInfo.class.getName());
	}
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(
				opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		String fileName = (String)ParameterUtility.getParameterValue(this, OperatorParameter.NAME_HD_fileName);
		
		
		if (fileName != null ){
			String dir = fileName.substring(0,fileName.lastIndexOf(HadoopFile.SEPARATOR));
			String name = fileName.substring(fileName.lastIndexOf(HadoopFile.SEPARATOR)+1,fileName.length());
			getOperatorParameter(OperatorParameter.NAME_HD_copyToFileName)
			.setValue(name);
			getOperatorParameter(OperatorParameter.NAME_HD_ResultsLocation)
					.setValue(dir);
		}
 
		createHadoopConnectionIfNotExsits(operatorParameters,opTypeXmlManager,opNode); 
		
		return operatorParameters;
	}
	
	private void createHadoopConnectionIfNotExsits(List<OperatorParameter> operatorParameterList, XmlDocManager opTypeXmlManager, Node opNode) {
	
		
		
		OperatorParameter operatorParameter=null;
		for (OperatorParameter opParameter : operatorParameterList) {
			if (opParameter.getName().equals(OperatorParameter.NAME_HD_connetionName )) {
				operatorParameter=opParameter;
			}
		}
		if(operatorParameter==null)return;
		Object obj = operatorParameter.getValue();
		if(obj!=null){
			String connName=(String)obj;
			  HadoopConnectionManagerIfc hadoopManager = HadoopConnectionManagerFactory.INSTANCE.getManager();
			 
			try {
 
				HadoopConnection connInfo=null;
				try {
					connInfo = hadoopManager.readHadoopConnection(connName, userName);
				} catch (Exception e) {
					itsLogger.error(e.getMessage(),e);
				}
				if(connInfo==null ){
					
					ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
							opNode, "Parameter");
					
					Properties props = new Properties(); 
					for (Node parameterNode : parameterNodeList) {
						String paraName = ((Element) parameterNode).getAttribute("key");
						String paraValue = null;
						paraValue = ((Element) parameterNode).getAttribute("value");
						 
						props.put(paraName, paraValue);
					}
					
					hadoopManager.saveHadoopConnection(props,userName) ;
					//refresh
				}
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				return;
			}
		}
		
	}
	
	@Override
	public List<Object> getOperatorInputList() {
		List<Object> list= new ArrayList();
		List<Operator> parent = getParentOperators();
		if(parent!=null){
			for (Operator operator : parent) {
				list.addAll(operator.getOperatorOutputList()); 
			}
		}
		return list;
	}
	
	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
 
		//handle special paramters 
		if( getOperatorOutputList()!=null&& getOperatorOutputList().size()>0){
			 OperatorInputFileInfo fileInfo = (OperatorInputFileInfo)getOperatorOutputList().get(0);
			setFileStructureModel(xmlDoc, element,fileInfo);
			String fileName = fileInfo.getHadoopFileName();
			createSimpleElements(xmlDoc, element, fileName, HadoopFileSelectorConfig.NAME_HD_fileName );

		}
		
		
		Map<String, String> paraMap = OperatorUtility.refreshHadoopFileInfo(this,userName,resourceType);
		if (paraMap == null)
			return;
		
  

		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_hdfsHostname), HadoopFileSelectorConfig.NAME_HD_hdfsHostname  );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_hdfsPort), HadoopFileSelectorConfig.NAME_HD_hdfsPort   );
	 
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_jobHostname ), HadoopFileSelectorConfig.NAME_HD_jobHostname  );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_jobPort),HadoopFileSelectorConfig.NAME_HD_jobPort   );
		
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_version ), HadoopFileSelectorConfig.NAME_HD_version  );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_user), HadoopFileSelectorConfig.NAME_HD_user );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_group), HadoopFileSelectorConfig.NAME_HD_group );
	}
	
 

	 

		
	private void setFileStructureModel(Document xmlDoc, Element element,
			OperatorInputFileInfo operatorInputFileInfo) {
		FileStructureModel fileStructureModel = operatorInputFileInfo.getColumnInfo();
		if(fileStructureModel!=null){
			 element.appendChild(fileStructureModel.toXMLElement(xmlDoc));
		}	

		
	}
	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputFileInfo());
		return list;
	}
	
	
	
	@Override
	public List<Object> getOperatorOutputList() {
		HadoopConnectionManagerIfc hdManager = HadoopConnectionManagerFactory.INSTANCE.getManager();

		String connName = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_connetionName).getValue();
		String fileDir = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_ResultsLocation).getValue();
		
		String fileName =  (String) getOperatorParameter(
				OperatorParameter.NAME_HD_copyToFileName).getValue();
		List<Object> outputObjectList = new ArrayList<Object>();
		if(StringUtil.isEmpty(connName)){
			return outputObjectList;
		}
		HadoopConnection hadoopConn;
		try {
			hadoopConn = hdManager.readHadoopConnection(connName, userName);
			
			OperatorInputFileInfo fileInfo = new OperatorInputFileInfo();
			
			fileInfo.setConnectionName(connName);
			fileInfo.setHdfsHostname(hadoopConn.getHdfsHostName());
			fileInfo.setHdfsPort(String.valueOf(hadoopConn.getHdfsPort())) ;
			fileInfo.setJobHostname(hadoopConn.getJobHostName()) ;
			fileInfo.setJobPort(String.valueOf(hadoopConn.getJobPort())) ;
            fileInfo.setOperatorUUID(this.getOperModel().getUUID());
			fileInfo.setHadoopFileName(fileDir+HadoopFile.SEPARATOR+fileName) ;
			fileInfo.setHadoopFileFormat("Text Field");
			fileInfo.setIsDir (false);
			fileInfo.setVersion( hadoopConn.getVersion() );
			fileInfo.setUser(hadoopConn.getUserName());
			fileInfo.setGroup(hadoopConn.getGroupName());
			 
			fileInfo.setSecurityMode(hadoopConn.getSecurityMode()) ;
			fileInfo.setHdfsPrincipal(hadoopConn.getHdfsPrincipal() ) ;
			fileInfo.setHdfsKeyTab(hadoopConn.getHdfsKeyTab() ) ;
			fileInfo.setMapredPrincipal(hadoopConn.getMapredPrincipal() );
			fileInfo.setMapredKeyTab(hadoopConn.getMapredKeyTab()) ;
			
			
			List<OperatorInputTableInfo> parentTables = getParentDBTableSet();
			if(parentTables!=null&&parentTables.size()!=0){
				CSVFileStructureModel columnInfo = new CSVFileStructureModel(); 
				OperatorInputTableInfo table = parentTables.get(0);
				List<String[]> columns = table.getFieldColumns();
				List<String> columnNameList = new ArrayList<String>(); 
				List<String> columnTypeList = new ArrayList<String>(); 
				for (String[] strings : columns) {
					columnNameList.add(StringUtil.filterInvalidChar4PigName(strings[0])) ;  
					columnTypeList.add(guessHadoopType(table.getSystem(),strings[1])) ;
				}
				//columnInfo.setIncludeHeader("True");
				columnInfo.setIncludeHeader("False");
				columnInfo.setColumnNameList(columnNameList) ;
				columnInfo.setColumnTypeList(columnTypeList);
				columnInfo.setDelimiter(CSVFileStructureModel.DELIMITER[1]) ;
				
				columnInfo.setQuoteChar(CopytoHadoopAnalyzer.QUOTE_CHAR) ;
				columnInfo.setEscapChar(CopytoHadoopAnalyzer.ESC_CHAR) ;
				
				fileInfo.setColumnInfo(columnInfo) ;
			}

  
			outputObjectList.add(fileInfo);
			return outputObjectList;
		}  catch (AlpineConncetionException e) {
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException("1019", e);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		}

		return null;
	}

	
	private String guessHadoopType(String dbSystem, String dbColumnType) {
		return HadoopDataType.guessHadoopDataType(dbSystem, dbColumnType) ;
		 
		
	}
	protected CopytoHadoopOperator(List<String> parameterNames) {
		super(parameterNames);
	}

	@Override
	public String getToolTipTypeName() {	
		return LanguagePack.getMessage(LanguagePack.HP_COPYTO_OPERATOR,locale);
	}
	
	@Override
	public boolean isInputObjectsReady() {
		List<OperatorInputTableInfo> parentDBTables = this.getParentDBTableSet();
		if(parentDBTables==null||parentDBTables.size()==0){		
		 	return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
	
		List<String> invalidParameterList = new ArrayList<String>();
		try {
			List<HadoopConnection> allHadoopConns = HadoopConnectionManagerFactory.INSTANCE
					.getManager().getAllHadoopConnection(getUserName(),
							ResourceType.Personal);
			if (allHadoopConns != null) {
				boolean found = false;
				for (Iterator iterator = allHadoopConns.iterator(); iterator
						.hasNext();) {
					HadoopConnection hadoopConnection = (HadoopConnection) iterator
							.next();
					if (hadoopConnection.getConnName().equals(
							ParameterUtility.getParameterValue(this,
									OperatorParameter.NAME_HD_connetionName))) {
						found = true;
						break;
					}
				}
				if (found == false) {
					invalidParameterList
							.add(OperatorParameter.NAME_HD_connetionName);
				}
			} else {
				invalidParameterList
						.add(OperatorParameter.NAME_HD_connetionName);
			}
			
			List<OperatorParameter> paraList=getOperatorParameterList();
			String paraName;
			for (Iterator iterator = paraList.iterator(); iterator.hasNext();) {
				OperatorParameter operatorParameter = (OperatorParameter) iterator
						.next();
				paraName = operatorParameter.getName();
				if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
				 
						validateNull(invalidParameterList, paraName, (String)operatorParameter.getValue());
				 
				}else if(paraName.equals(OperatorParameter.NAME_HD_copyToFileName)){
					 //TODO
						validateNull(invalidParameterList, paraName, (String)operatorParameter.getValue());
				 
				}	else if(paraName.equals(OperatorParameter.NAME_HD_ifFileExists)){
						validateNull(invalidParameterList, paraName, (String)operatorParameter.getValue());
				 
				}	
			}
			 
		   
			
			
		} catch (Exception e) {
			invalidParameterList.add(OperatorParameter.NAME_HD_connetionName);
			itsLogger.error(e.getMessage(),e) ;
		}
		// you need to check the file structure
	 
	 
		
		
		
		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public void saveInputFieldList(Document xmlDoc, Element operator_element,
			boolean addSuffixToOutput) {
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo info = (OperatorInputTableInfo) obj;
				Element ele = xmlDoc.createElement("InPutFieldList");
				setAttribute(xmlDoc, ele, "url", info.getUrl());
				setAttribute(xmlDoc, ele, "schema", info.getSchema());
				if (addSuffixToOutput && !ifFromDBTableOperator(info.getTable())) {
					String newTableName = StringHandler.addPrefix(info
							.getTable(), userName);
					setAttribute(xmlDoc, ele, "table", newTableName); 
				} else {
					setAttribute(xmlDoc, ele, "table", info.getTable());
				}

				setAttribute(xmlDoc, ele, "tableType", info.getTableType());
				setAttribute(xmlDoc, ele, "username", info.getUsername());
				// any encrypt?
				setAttribute(xmlDoc, ele, "password", XmlDocManager
						.encryptedPassword(info.getPassword()));
				setAttribute(xmlDoc, ele, "system", info.getSystem());

				List<String[]> fieldColumns = info.getFieldColumns();

				if (fieldColumns != null && fieldColumns.isEmpty() == false) {
					Element fieldList = xmlDoc.createElement("Fields");
					ele.appendChild(fieldList);
					for (String[] fieldColumn : fieldColumns) {
						Element field = xmlDoc.createElement("Field");
						field.setAttribute("name", fieldColumn[0]);
						field.setAttribute("type", fieldColumn[1]);
						fieldList.appendChild(field);
					}
				}
				operator_element.appendChild(ele);
			}
		}
	}
	
}
