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

import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.ifc.HadoopConnectionManagerIfc;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelFactory;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.db.AlpineConncetionException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

public class HadoopFileOperator extends HadoopOperator {
    private static final Logger itsLogger=Logger.getLogger(HadoopFileOperator.class);

    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_connetionName,
			OperatorParameter.NAME_HD_fileName,
			OperatorParameter.NAME_HD_format,
			OperatorParameter.NAME_HD_fileStructure,
	});

	public HadoopFileOperator() {
		super(parameterNames);
		addOutputClass(OperatorInputFileInfo.class.getName());
	}
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(
				opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		FileStructureModel fileStructureModel=FileStructureModelFactory.
				createFileStructureModelByXML(opTypeXmlManager,opNode);
		getOperatorParameter(OperatorParameter.NAME_HD_fileStructure)
		.setValue(fileStructureModel);

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
		return getOperatorOutputList();
	}
	
	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter fileStructureModelParameter = ParameterUtility
		.getParameterByName(this,
				OperatorParameter.NAME_HD_fileStructure);
		
		setFileStructureModel(xmlDoc, element, fileStructureModelParameter);
		//handle special paramters 
		
		Map<String, String> paraMap = OperatorUtility.refreshHadoopFileInfo((HadoopOperator)this,userName,resourceType);
		if (paraMap == null)
			return;
		
  

		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_hdfsHostname), HadoopFileSelectorConfig.NAME_HD_hdfsHostname  );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_hdfsPort), HadoopFileSelectorConfig.NAME_HD_hdfsPort   );
	 
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_jobHostname ), HadoopFileSelectorConfig.NAME_HD_jobHostname  );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_jobPort),HadoopFileSelectorConfig.NAME_HD_jobPort   );
		
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_version ), HadoopFileSelectorConfig.NAME_HD_version  );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_user), HadoopFileSelectorConfig.NAME_HD_user );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_group), HadoopFileSelectorConfig.NAME_HD_group );
	 
		
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_securityMode), HadoopFileSelectorConfig.NAME_HD_securityMode );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_hdfsPrincipal), HadoopFileSelectorConfig.NAME_HD_hdfsPrincipal );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_hdfsKeyTab), HadoopFileSelectorConfig.NAME_HD_hdfsKeyTab );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_mapredPrincipal), HadoopFileSelectorConfig.NAME_HD_mapredPrincipal );
		createSimpleElements(xmlDoc, element, paraMap.get( HadoopFileSelectorConfig.NAME_HD_mapredKeyTab), HadoopFileSelectorConfig.NAME_HD_mapredKeyTab );

		
	}
	
	private void setFileStructureModel(Document xmlDoc, Element element,
			OperatorParameter fileStructureModelParameter) {
		Object value = fileStructureModelParameter.getValue();
		if (!(value instanceof FileStructureModel)) {
			return;
		}
		FileStructureModel fileStructureModel = (FileStructureModel) value;
		element.appendChild(fileStructureModel.toXMLElement(xmlDoc));	
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
		String filePath = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_fileName).getValue();
		
		String fileFormat =  (String) getOperatorParameter(
				OperatorParameter.NAME_HD_format ).getValue();
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
			fileInfo.setHadoopFileName(filePath) ;
			fileInfo.setHadoopFileFormat(fileFormat);
			fileInfo.setIsDir (false);
			fileInfo.setVersion( hadoopConn.getVersion() );
			fileInfo.setUser(hadoopConn.getUserName());
			fileInfo.setGroup(hadoopConn.getGroupName());
			
			 
			fileInfo.setSecurityMode(hadoopConn.getSecurityMode()) ;
			fileInfo.setHdfsPrincipal(hadoopConn.getHdfsPrincipal() ) ;
			fileInfo.setHdfsKeyTab(hadoopConn.getHdfsKeyTab() ) ;
			fileInfo.setMapredPrincipal(hadoopConn.getMapredPrincipal() );
			fileInfo.setMapredKeyTab(hadoopConn.getMapredKeyTab()) ;
			
			Object obj = getOperatorParameter(OperatorParameter.NAME_HD_fileStructure).getValue();
			if(obj!=null){
				FileStructureModel sModel=(FileStructureModel)obj;
				fileInfo.setColumnInfo(sModel);
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

	
	protected HadoopFileOperator(List<String> parameterNames) {
		super(parameterNames);
	}

	@Override
	public String getToolTipTypeName() {	
		return LanguagePack.getMessage(LanguagePack.HP_FILE_OPERATOR,locale);
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
		} catch (Exception e) {
			invalidParameterList.add(OperatorParameter.NAME_HD_connetionName);
			itsLogger.error(e.getMessage(),e) ;
		}
		// you need to check the file structure
		if(ParameterUtility.getParameterValue(this,OperatorParameter.NAME_HD_fileStructure )==null){
			invalidParameterList.add(OperatorParameter.NAME_HD_fileStructure);
		}
		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
