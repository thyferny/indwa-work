/**   
 * ClassName:WebWorkFlowRunner   
 * Author   kemp zhang   
 * Version  Ver 1.0   
 * Date     2011-4-22    
 * COPYRIGHT   2010 - 2011 Alpine Solutions. All Rights Reserved. 
 */
package com.alpine.miner.impls.web.resource;   

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.workflow.AlpineAnalyticEngine;
import com.alpine.datamining.workflow.AnalyticEngine;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;
import com.alpine.utility.db.DbConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.xml.XmlDocManager;


public class WebWorkFlowRunner {
    private static Logger itsLogger = Logger.getLogger(WebWorkFlowRunner.class);

    public static final String RUNTIME_DIR = System.getProperty("java.io.tmpdir") + File.separator + "RuntimeCache";
	
	protected String filePath = "";
	protected String flowName = "";
	protected RowInfo row;
	protected String uuid;
	protected String user;
	protected WebRunAnalyticProcessListener listener;
	
	protected ResourceType resourceType;
	protected String flow_file_Version;
	protected String flowFullName;
	protected Locale locale;

	protected String processID;


	public String getProcessID() {
		return processID;
	}

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public WebWorkFlowRunner(String filePath,  RowInfo row,String uuid,
			String flowFullName, Locale locale, FlowInfo flowInfo){
		
 
		this.filePath = filePath;
		this.flowName = flowInfo.getId();
		this.row = row;
		this.uuid = uuid;
		this.flow_file_Version=  flowInfo.getVersion();
		this.user = flowInfo.getModifiedUser();
		this.resourceType= flowInfo.getResourceType();
		this.flowFullName = flowFullName;
		this.locale=locale;
	}
	
	public void stopWorkFlow() throws Exception{
			AlpineAnalyticEngine.getInstance().stopAnalysisProcess(processID);
 
	}
 
	public static void copyFile(String filePath, ResourceType resourceType,String userName,String tmpFilePath,Locale locale,boolean addTablePrefix) throws Exception{
		OperatorWorkFlow ow;
			try{
				ow = readFlow(filePath, resourceType, userName, locale);
			}catch(Exception e){
				return;
			}

		String tempDir = tmpFilePath.substring(0, tmpFilePath.lastIndexOf(File.separator));
		File runtimeDir = new File(tempDir);
		if(!runtimeDir.exists()){
			runtimeDir.mkdirs();
		}
		
		for(UIOperatorModel subOperator : ow.getChildList()){
			if(subOperator.getOperator() instanceof SubFlowOperator){
				String subFlowName = (String) subOperator.getOperator().getOperatorParameter(OperatorParameter.NAME_subflowPath).getValue();
				String subFlowPath = filePath.substring(0,filePath.lastIndexOf(File.separator) + 1) + subFlowName + Resources.AFM;
				String tmpPath = tempDir + File.separator + subFlowName + Resources.AFM;
				 copyFile(subFlowPath, resourceType, userName, tmpPath, locale,addTablePrefix);
			}
		}
		XMLWorkFlowSaver saver=new XMLWorkFlowSaver();
		
		try {
			
			saver.doSave(tmpFilePath, ow,userName,addTablePrefix);
			
			Document xmlDoc = updateTempFile(tmpFilePath, userName, resourceType);
			File newfile = new File(tmpFilePath);
			XmlDocManager xmlDocManager = new XmlDocManager();
			BufferedWriter writer = null;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newfile),XmlDocManager.ENCODING_UTF8)); 
			writer.write(xmlDocManager.xmlToLocalString(xmlDoc));
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e) ;
			throw e;
		}
		
		return;
	}

	private static OperatorWorkFlow readFlow(String filePath,
			ResourceType resourceType, String userName, Locale locale)
			throws Exception {
		XMLWorkFlowReader reader=new XMLWorkFlowReader();
		OperatorWorkFlow ow=null;
	 
		 ow=reader.doRead(new XMLFileReaderParameters(filePath,userName,resourceType),locale);
		return ow;
	}
	public void runWorkFlow() throws Exception{
		List<AnalyticProcessListener> listeners =new ArrayList<AnalyticProcessListener>();
		listener = new WebRunAnalyticProcessListener(user,flowName, filePath,locale);
		listener.setRow(row);
		listener.setUUID(uuid);
		listener.setFlowFileVersion(flow_file_Version) ;
		listener.setFlowFullName(flowFullName);
		listeners.add(listener);
		try{
			String tempDir=RUNTIME_DIR + File.separator + user + File.separator + UUID.randomUUID();
			String tmpFilePath=tempDir+File.separator+UUID.randomUUID().toString()+flowName+Resources.AFM;
			listener.setFilePath(tmpFilePath);
			boolean addSuffixToOutput=Boolean.parseBoolean(ProfileReader.getInstance().getParameter(ProfileUtility.UI_ADD_PREFIX));
			copyFile(filePath,resourceType,user,tmpFilePath,locale,addSuffixToOutput);
			
			 processID = AnalyticEngine.instance.runAnalysisProcessFile( 
					 tmpFilePath, listeners,false,locale,user);
		
		}catch(Exception e){
			listener.processError(e) ;
			throw e;
		}
	}
//
 
	

	private static Document updateTempFile(String tmpFilePath, String user,  ResourceType dbResourcetype) throws Exception{
		XmlDocManager opTypeXmlManager = new XmlDocManager();

		try {
			opTypeXmlManager.parseXMLFile(tmpFilePath);
			
			Element root = (Element) opTypeXmlManager.getRootNode();
			NodeList nodes = root.getElementsByTagName("Operator");
			
			for(int i = 0 ; i < nodes.getLength() ; i ++ )
			{
				Element node = (Element)nodes.item(i);
				String operatorType = node.getAttribute("type");
				if(operatorType.endsWith(".DbTableOperator") || 
						operatorType.endsWith(".SQLExecuteOperator"))
				{
					NodeList paramentnodes = node.getElementsByTagName("Parameter");
					String connName = null;
					for (int j = 0; j < paramentnodes.getLength(); j++) {
						Element paramentnode = (Element) paramentnodes.item(j);
						String key = paramentnode.getAttribute("key");
						if (key.equals("dbConnectionName")) {
							connName = paramentnode.getAttribute("value");
							break;
						}
					}

					if(connName == null || connName.length() ==0)	{
						//MINERWEB-774 avoid the error in step run
						//nothing to do...
					}
					else{
					DbConnectionInfo result = null;
					for (DbConnectionInfo info : ResourceManager.getInstance().getDBConnectionList(user)) {
						if (info.getId().equals(connName)
								&& info.getResourceType().equals(dbResourcetype)) {
							result = info;
							break;
						}
					}
					//no connection, will go on ...
					if (result != null &&  result.getConnection() != null)
					{
 
						
						DbConnection dbconn = result.getConnection();					
						for(int j = 0 ; j < paramentnodes.getLength() ; j ++ )
						{
							Element paramentnode = (Element)paramentnodes.item(j);
							String key = paramentnode.getAttribute("key");
							if(key.equals("url"))
							{
								paramentnode.setAttribute("value", dbconn.getUrl());
							}
							if(key.equals("userName"))
							{
								paramentnode.setAttribute("value", dbconn.getDbuser());
							}
							if(key.equals("password"))
							{
								paramentnode.setAttribute("value", XmlDocManager.encryptedPassword(dbconn.getPassword()));
							}
							if(key.equals("system"))
							{
								paramentnode.setAttribute("value", dbconn.getDbType());
							}
						}
					}
				}
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e) ;
			throw e;
		}
		return opTypeXmlManager.getXmlDoc();
	}


	public WebRunAnalyticProcessListener getListener() {
		return listener;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setRow(RowInfo row) {
		this.row = row;
	}
	
	public String getFlowFileVersion() {
		return flow_file_Version;
	}

	public void setFlowFileVersion(String flow_file_Version) {
		this.flow_file_Version = flow_file_Version;
	}


}
   
