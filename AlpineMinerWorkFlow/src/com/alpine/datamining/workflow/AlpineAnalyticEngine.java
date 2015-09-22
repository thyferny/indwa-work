/**
 * ClassName AlpineAnalyticEngine.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.AlpineMinerSDK;
import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.datamining.workflow.util.MiningUtil;
import com.alpine.license.validator.illuminator.ILicenseValidator.ValidationResult;
import com.alpine.license.validator.illuminator.IlluminatorLicenseValidator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelFactory;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.miner.workflow.operator.parameter.hadoopjoin.HadoopJoinModel;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.miner.workflow.operator.parameter.pigexe.PigExecutableModel;
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.JoinTable;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.tableset.TableSetModel;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.FileUtility;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.log.LogPoster;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author John Zhao
 * 
 */
public class AlpineAnalyticEngine implements AnalyticEngine {
	//this is very importatnt to init log4j in command line
    private static final Logger itsLogger =Logger.getLogger(AlpineAnalyticEngine.class);



    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	
	public static final String ARG_User = "User";
	public static final String ARG_Visual = "Visual";
	public static final String ARG_NewThread = "NewThread";
	public static final String ARG_ModeSaveFolder = "ModeFolder";
	public static final String ARG_HadoopLocalMode  = "HadoopLocalMode";

	public static final String MODEL_FOLDER = "model"; 
	
	private static AnalyticEngine instance = null;
	 
	public static String modelSaveDir = "";
	
	private AnalyticThreadPool analyticThreadPool;

	public static AnalyticEngine getInstance() {
		if (instance == null) {
			instance = new AlpineAnalyticEngine();
		}
		return instance;
	}

	private AlpineAnalyticEngine() {
		AlpineMinerSDK.init(); 
		
		analyticThreadPool=new AnalyticThreadPool(
				EngineConfig.instance.getMax_process_instance(),
				EngineConfig.instance.getCheck_thread_period());

	}
	private AlpineAnalyticEngine(int max_process_instance,int check_thread_period) {
		AlpineMinerSDK.init(); 
		
		analyticThreadPool=new AnalyticThreadPool(
				max_process_instance,
				check_thread_period);

	}
 
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.AnalyticEngine#stopAnalysisProcess(java.lang
	 * .String)
	 */
	@Override
	public void stopAnalysisProcess(String processID)
			throws AnalysisException {
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("AlpineAnalyticEngine.stop process id="+processID);
		}
//		AnalyticResult result= new AnalyticResultImpl(processID);
//		//even stop it,can show the result till now
//		AnalyticFlowMetaInfo flowMetaInfo=this.analyticThreadPool.getFlowMetaInfo(processID);
//		List<AnalyticOutPut> outs=this.analyticThreadPool.getOutPuts(processID);
//		
		//before stop.. release the connection...
//		AnalyticResult result=	
				analyticThreadPool.stop(processID,false);
		
//		result.setAnalyticMetaInfo(flowMetaInfo);
//		result.setOutPuts(outs);
//		return result;//do it later...
	}
 
	
	private String runAnalysisProcessFile(String processFilePath,
			List<AnalyticProcessListener> listeners, String outputDir,boolean isVisual, Locale locale,String executeUser)   {
		
		AnalyticProcess process=MiningUtil.parseXMLFile(processFilePath,locale,true);
		if(process==null||process.getFlow()==null||
				process.getFlow().getAllNodes()==null||process.getFlow().getAllNodes().size()==0) {
			 for(AnalyticProcessListener listener:listeners){
				 listener.processError("Can not run an empty flow .") ;
			 }
			 return "";
			
		}
		 
		String  processID=Long.toString(System.currentTimeMillis());
		process.setProcessID(processID);
		
		AnalyticRunner thread = null;
		try {
			thread = new AnalyticRunner(process,listeners,isVisual,locale,executeUser);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				AnalyticProcessListener analyticProcessListener = (AnalyticProcessListener) iterator
						.next();
				analyticProcessListener.processError(e.getMessage()) ;
				analyticProcessListener.stopProcess(null) ;
			}
			return "";
		}
		if(thread!=null){
			if(outputDir!=null){
				thread.setOutPutPath(outputDir) ;
			}
			
			analyticThreadPool.execute(thread);
		}
		return processID;
		
	}
 
 
 
 
	 

	/* (non-Javadoc)  stepRunProcess
	 * @see com.alpine.datamining.workflow.AnalyticEngine#runToNode(com.alpine.datamining.api.AnalyticProcess, com.alpine.datamining.api.AnalyticNode, java.util.List)
	 */
	@Override
	public StepedAnalyticRunner stepRunToNode(AnalyticProcess process, AnalyticNode node,
			List<AnalyticProcessListener> listeners, boolean isVisual,Locale locale, AnalyticContext context,String executeUser) throws Exception {
 
		StepedAnalyticRunner runner=null;
		try{
			  runner = new StepedAnalyticRunner(process,
				listeners, isVisual,  locale, context,executeUser);
		
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				AnalyticProcessListener analyticProcessListener = (AnalyticProcessListener) iterator
						.next();
				analyticProcessListener.processError(e.getMessage()) ;
				analyticProcessListener.stopProcess(null) ;
			}
			return null;
		}
		if(runner!=null){
			runner.setProcessID(process.getProcessID());
	
			analyticThreadPool.execute(runner);
		}
		return  runner;

	}
	
	@Override
	public String runAnalysisProcessFile(String processFilePath,
			List<AnalyticProcessListener> list, boolean isVisual,Locale locale,String executeUser)
			throws AnalysisException {
			return runAnalysisProcessFile(processFilePath,list,null,isVisual,locale,executeUser);
 	
	}
	
	public static void main(String args[] ){
		
		if(args!=null&&args.length>0){
			//try to find the file in the current folder
	 		String processFile=args[0];
	 		String outputDir=args[1];
	 		String suffix=args[2];
	 		String visual=args[3];
	 		String runStandAlone=args[4];
	 		String modelSaveFolder=null;
	 		String hadoopLocalMode = null;
	 		
	 		int varaibleIndex = 5;
	 		if(args.length>5){
	 			modelSaveFolder = args[5];
	 			varaibleIndex=varaibleIndex+1;
	 		}
	 		if(args.length>6){
	 			hadoopLocalMode = args[6];
	 			if(hadoopLocalMode!=null){
	 				hadoopLocalMode = hadoopLocalMode.substring(ARG_HadoopLocalMode.length()+1,hadoopLocalMode.length());
		 		}
	 			varaibleIndex=varaibleIndex+1;
	 		}
	 		
	 		List<String> variableList=new ArrayList<String>();
	 		if(args.length>varaibleIndex){
	 			for(int i=varaibleIndex;i<args.length;i++){
	 				if(!args[i].startsWith(VariableModel.VARIABLE_PREFIX)){
	 					System.out.println(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Invalid_Flow_File,Locale.getDefault()));
	 					return;
	 				}
	 				variableList.add(args[i]);
	 			}
	 		}
	 		
	 		if(processFile==null||processFile.length()==0
	 				||processFile.endsWith(Operator.AFM_SUFFIX)==false
	 				||StringUtil.isEmpty(suffix)
	 				||StringUtil.isEmpty(visual)){
	 			System.out.println(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Invalid_Flow_File,Locale.getDefault())); 
	 			return;
	 		}
	 		if(!suffix.startsWith(ARG_User+"=")){
	 			System.out.println(ARG_User+" should be the third  argument!"); 
	 			return;
	 		}
	 		if(!visual.startsWith(ARG_Visual+"=")){
	 			System.out.println(ARG_Visual+" should be the fourth argument!"); 
	 			return;
	 		}
	 		if(!runStandAlone.startsWith(ARG_NewThread+"=")){
	 			System.out.println(ARG_NewThread+" should be the fifth argument!"); 
	 			return;
	 		}
	 		if(modelSaveFolder!=null&&!modelSaveFolder.startsWith(ARG_ModeSaveFolder+"=")){
	 			System.out.println(ARG_ModeSaveFolder+" should be the sixth argument!"); 
	 			return;
	 		}

	 		File f =new File(processFile);
	 		if(f.exists()==false){
	 			System.out.println(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Flow_File_NotExist,Locale.getDefault()));
	 		}
			//check license file first, in the classpath
	 		String libPath= f.getParentFile().getAbsolutePath()+File.separator+"lib";

	 		ObjectInputStream ois = null;
	 		String customId = "";
	 		try {
				ois = new ObjectInputStream(new FileInputStream(libPath + File.separator + "executable_configuration"));
				Map<String, String> configuration = (Map<String, String>) ois.readObject();
				customId = configuration.get("customId");
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(ois != null){
					try {
						ois.close();
					} catch (IOException e) {
						
					}
				}
			}
	 		LogPoster.getInstance().startup(customId, "true");
	 		//init the instance and inti the log4j
	 		//LogService.getInstance();
	 	 
	 		suffix=suffix.substring(5, suffix.length());
	 		visual=visual.substring(7,visual.length());
	 		runStandAlone=runStandAlone.substring(10,runStandAlone.length());
	 		if(modelSaveFolder!=null){
	 			modelSaveFolder = modelSaveFolder.substring(ARG_ModeSaveFolder.length()+1,modelSaveFolder.length());
	 		}
	 		boolean isVisual=true;
	 //		HTMLExporterFactory.getInstance().setLocale(Locale.getDefault());
	 		if(visual.equalsIgnoreCase(Resources.YesOpt)){
	 		 
	 			isVisual=true;
	 			//only isvisual == true can 
	 			//WARNING: Display must be created on main thread due to Cocoa restrictions.
	 				
				VisualResource.setLocale(Locale.getDefault());
				//this is for jira MINER-113, make sure init the charting static constants at the very beginning.
				ChartFactory.getChartTheme();
				
	 		}else{
	 			isVisual=false;
	 		}
	 		
	 		boolean newThread=false;
	 		if(runStandAlone.equalsIgnoreCase(Resources.YesOpt)){
	 			newThread=true;
	 		}else{
	 			newThread=false;
	 		}
	 		
	 		if(modelSaveFolder!=null&&modelSaveFolder.trim().equals("")==false&&modelSaveFolder.trim().equals("\"\"")==false){
	 			if(modelSaveFolder.startsWith("\"")){
	 				modelSaveFolder=modelSaveFolder.substring(1,modelSaveFolder.length()) ;
	 			}
	 			if(modelSaveFolder.endsWith("\"")){
	 				modelSaveFolder=modelSaveFolder.substring(0,modelSaveFolder.length()-1) ;
	 			}
	 			AlpineAnalyticEngine.modelSaveDir = modelSaveFolder;
	 		}else{
	 			AlpineAnalyticEngine.modelSaveDir = f.getParentFile().getAbsolutePath()+File.separator+MODEL_FOLDER;
	 		}
	 		File modelFolder = new File(AlpineAnalyticEngine.modelSaveDir);
	 		if(modelFolder.exists()==false){
	 			boolean success = modelFolder.mkdir();
	 			if(success==false){
	 				System.out.println("Can not create folder"+":"+modelFolder.getAbsolutePath());
	 				return;
	 			}
	 			
	 		}
			String licenseFielPath=libPath+File.separator+"Alpine_Illuminator_License";
 
 
			String errorMessage=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Invalid_License_File,Locale.getDefault()); 
			try {
				String license = FileUtility.readFiletoString(new File(licenseFielPath))
				.toString();
				ValidationResult validateResult = IlluminatorLicenseValidator.INSTANCE.validateKey(license, 0, 0);
				if(validateResult!=ValidationResult.PASS){	
					System.out.println(errorMessage);
					itsLogger.error(errorMessage);
					return;
				}
			} catch (Exception e1) {
				System.out.println(errorMessage);
				return;
			}
			
			String tempFile=null;
			try {			
				List<String> missedSubflowNames =new ArrayList<String>();
				List<String> allSubFlowPathWithParent =new ArrayList<String>();
				
				fillAllSubflowName(processFile,missedSubflowNames,allSubFlowPathWithParent);
				if(missedSubflowNames!=null&&missedSubflowNames.size()>0){
					for(String missedSubflow:missedSubflowNames){
						String fileNotFoundMessage = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.SubFlow_File_NotExist,Locale.getDefault());
						System.out.println(fileNotFoundMessage+":"+missedSubflow);
					}
					return;
				}
				int i=0;
				for(String flowPath:allSubFlowPathWithParent){
					String flowName = flowPath.substring(flowPath.lastIndexOf(File.separator)+1,flowPath.length());
					String tempFlowPath=TEMP_DIR+File.separator+flowName;
					if(i==0){
					 tempFile=tempFlowPath;
					}	
					modifyFile(flowPath,tempFlowPath,suffix,variableList);
					i++;
				}
			} catch (Exception e1) {
				itsLogger.error(e1.getMessage(),e1);
				return;
			}
			
	 		if(outputDir!=null&&outputDir.trim().length()>0){
 
	 			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
	 			outputDir=outputDir+File.separator+f.getName()+"-"+format.format( 		
	 					Calendar.getInstance().getTime());
	 		}
			CommandLineProcessListener listener= new CommandLineProcessListener();
			List<AnalyticProcessListener> listeners = new ArrayList<AnalyticProcessListener>();
			listeners.add(listener);

			try {
				if(hadoopLocalMode!=null){
					ProfileReader.getInstance().getProperties().setProperty(ProfileUtility.HD_LOCAL_MODE , hadoopLocalMode);
				}
				 
				
				
				if(newThread){
					//also need a listener...
					//will print message into console
					AlpineAnalyticEngine engine= new AlpineAnalyticEngine(2,60000);	
					engine.runAnalysisProcessFile(tempFile,listeners,outputDir,isVisual,Locale.getDefault(),System.getProperty("user.name"));		
				}else{
					AlpineMinerSDK.init();
					
					AnalyticProcess process=MiningUtil.parseXMLFile(tempFile,Locale.getDefault(),true);
					 
					String  processID=Long.toString(System.currentTimeMillis());
					process.setProcessID(processID);
					
					AnalyticRunner runner = new AnalyticRunner(process,listeners,isVisual,Locale.getDefault(),System.getProperty("user.name"));
					if(outputDir!=null){
						runner.setOutPutPath(outputDir) ;
					}
					runner.run();
				}
			} catch ( Exception e) {
				itsLogger.error(e.getMessage(),e);
			}finally{
				File temp=new File(tempFile);
				if(temp.exists()){
					temp.delete();
				}
			}
		}else{
			System.out.println(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Please_input_Flow_File,Locale.getDefault()));
		}
		System.exit( 0);
	}

 

	private static void modifyFile(String filePath,String newFilePath, String suffix, List<String> variableList) throws Exception {
		
		boolean addSuffixToOutput=false;
		if(!StringUtil.isEmpty(suffix)){
			addSuffixToOutput=true;
		}
		
		String XML_TAG_UUID="uuid";
		
		XmlDocManager opTypeXmlManager = new XmlDocManager();
		try {
			opTypeXmlManager.parseXMLFile(filePath);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		}
		
		Document xmlDoc = null;
		Element root = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			itsLogger.error(e.getMessage(),e);
		}
		
		HashMap <String,List<String>> parnetsList=new HashMap<String,List<String>>();
		ArrayList<Node> parentsLinkNodes = opTypeXmlManager.getNodeListByTag("Link");
		for (Node linkNode:parentsLinkNodes) {
			String source = linkNode.getAttributes().getNamedItem("source").getNodeValue();
			String target = linkNode.getAttributes().getNamedItem("target").getNodeValue();
			if(parnetsList.containsKey(target)){
				List<String> parents=parnetsList.get(target);
				if(!parents.contains(source)){
					parents.add(source);
				}
			}else{
				List<String> parents=new ArrayList<String>();
				parents.add(source);
				parnetsList.put(target,parents);
			}
		}
		

			
		root = xmlDoc.createElement("Process");

		
		String userName = opTypeXmlManager.getRootNode().getAttributes().getNamedItem("UserName").getNodeValue();
		String description = opTypeXmlManager.getRootNode().getAttributes().getNamedItem("Description").getNodeValue();
		String version = opTypeXmlManager.getRootNode().getAttributes().getNamedItem("Version").getNodeValue();

		root.setAttribute("UserName", userName);
		root.setAttribute("Description", description);
		root.setAttribute("Version", version);
		
		xmlDoc.appendChild(root);
		
		ArrayList<Node> opNodes = opTypeXmlManager.getNodeListByTag("Operator");
		
		List<String> dbTableNameList=new ArrayList<String>();
		HashMap<String,String> dbTableNameMap=new HashMap<String,String>();
		HashMap<String,String> dbSchemaNameMap=new HashMap<String,String>();
		for (Node opNode:opNodes){
			String operName = opNode.getAttributes().getNamedItem("name").getNodeValue();
			String operClass = opNode.getAttributes().getNamedItem("type").getNodeValue();
			if(operClass.equals("com.alpine.miner.gef.runoperator.datasource.DbTableOperator")){
				dbTableNameList.add(operName);
				ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(opNode, "Parameter");
				for (Node parameterNode: parameterNodeList) {
					String key=((Element)parameterNode).getAttribute("key");
					if(key.equals("tableName")){
						String value=((Element)parameterNode).getAttribute("value");
						dbTableNameMap.put(operName, value);
					}else if(key.equals("schemaName")){
						String value=((Element)parameterNode).getAttribute("value");
						dbSchemaNameMap.put(operName, value);
					}
				}
			}
		}
		
		HashMap<String,String> nameTypeMap=new HashMap<String,String>();
		for (Node opNode:opNodes) {
			String operName = opNode.getAttributes().getNamedItem("name").getNodeValue();
			String operClass = opNode.getAttributes().getNamedItem("type").getNodeValue();
			nameTypeMap.put(operName, operClass);
		}
		
		List<String> parentDbTableList=new ArrayList<String>();
		ArrayList<Node> linkNodesList = opTypeXmlManager.getNodeListByTag("Link");
		for (Node linkNode:linkNodesList) {
			String source = linkNode.getAttributes().getNamedItem("source").getNodeValue();
			String target = linkNode.getAttributes().getNamedItem("target").getNodeValue();
			if(dbTableNameList.contains(source)){
				parentDbTableList.add(target);
			}
		}

		for (Node opNode:opNodes) {
			String operName = opNode.getAttributes().getNamedItem("name").getNodeValue();
			String uuid = null;
			if(opNode.getAttributes().getNamedItem(XML_TAG_UUID)!=null){
				uuid = opNode.getAttributes().getNamedItem(XML_TAG_UUID).getNodeValue();
			}
			String operClass = opNode.getAttributes().getNamedItem("type").getNodeValue();
			
			if(operClass.equals("com.alpine.miner.gef.runoperator.datasource.DbTableOperator")){
				dbTableNameList.add(operName);
			}
			
			Element operator = xmlDoc.createElement("Operator");
			
			String operX = opNode.getAttributes().getNamedItem("X").getNodeValue();
			int x = Integer.parseInt(operX);
			if(x<0)x=0;
			operator.setAttribute("X", "" + x);
			String operY = opNode.getAttributes().getNamedItem("Y").getNodeValue();
			int y = Integer.parseInt(operY); 
			if(y<0)y=0;
			operator.setAttribute("Y", "" + y);
			
			
			operator.setAttribute(XML_TAG_UUID,uuid);
			operator.setAttribute("name", operName);
			operator.setAttribute("type", operClass);
			
			if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.customize.CustomizedOperator")){
				String operatorName=opNode.getAttributes().getNamedItem("operatorname").getNodeValue();
				String udfName=opNode.getAttributes().getNamedItem("udfName").getNodeValue();
				String udfschema=opNode.getAttributes().getNamedItem("udfschema").getNodeValue();
				
				operator.setAttribute("operatorname", operatorName);
				operator.setAttribute("udfName", udfName);
				operator.setAttribute("udfschema", udfschema);
				
				ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, "outputColumns");
				if(nodes!=null&&nodes.size()>0){
					for(Node node:nodes){
						Element column=(Element)node;
						Element parameter = xmlDoc.createElement("outputColumns");
						String key=column.getAttribute("column");
						String value=column.getAttribute("type");
						parameter.setAttribute("column", key);
						parameter.setAttribute("type", value);
						operator.appendChild(parameter);
					}
				}
			}
			
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.datasource.TableJoinOperator")){
				ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, TableJoinModel.TAG_NAME);
				if(nodes!=null&&nodes.size()>0){
					Element tableJoinElement=(Element)nodes.get(0) ;
					TableJoinModel tableJoinDef=TableJoinModel.fromXMLElement(tableJoinElement);
					TableJoinModel tjdNew =null;
					try {
						  tjdNew = (TableJoinModel)tableJoinDef.clone();
					} catch (CloneNotSupportedException e) {
						itsLogger.error(e.getMessage(),e);
					}
					if(tableJoinDef!=null){
						if(addSuffixToOutput&&tjdNew!=null){
							List<JoinTable> jtModelList=tjdNew.getJoinTables();
							Iterator<JoinTable> iter=jtModelList.iterator();
							while(iter.hasNext()){
								JoinTable jtModel=iter.next();
								jtModel.setTable(StringHandler.addPrefix(jtModel.getTable(), System.getProperty("user.name")));
							}
						}
						operator.appendChild(tableJoinDef.toXMLElement(xmlDoc));
					}		
				}
			}
			else  if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.field.VariableOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, QuantileFieldsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element quantileElement=(Element)nodes.get(0) ;
						QuantileFieldsModel quantileModel= QuantileFieldsModel.fromXMLElement(quantileElement);
						operator.appendChild(quantileModel.toXMLElement(xmlDoc));
					}
					
					nodes=opTypeXmlManager.getNodeList(opNode, DerivedFieldsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element quantileElement=(Element)nodes.get(0) ;
						DerivedFieldsModel derivedModel= DerivedFieldsModel.fromXMLElement(quantileElement);
						operator.appendChild(derivedModel.toXMLElement(xmlDoc));
					}
			  }
			else  if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.field.AggregateOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, AggregateFieldsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element quantileElement=(Element)nodes.get(0) ;
						AggregateFieldsModel aggModel= AggregateFieldsModel.fromXMLElement(quantileElement);
						operator.appendChild(aggModel.toXMLElement(xmlDoc));
					}
					
					nodes=opTypeXmlManager.getNodeList(opNode, WindowFieldsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element quantileElement=(Element)nodes.get(0) ;
						WindowFieldsModel winModel= WindowFieldsModel.fromXMLElement(quantileElement);
						operator.appendChild(winModel.toXMLElement(xmlDoc));
					}
			  }
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.field.HistogramOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, ColumnBinsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element interActionElement=(Element)nodes.get(0) ;
						ColumnBinsModel columnBinModel= ColumnBinsModel.fromXMLElement(interActionElement);
						operator.appendChild(columnBinModel.toXMLElement(xmlDoc));
					}	
			  }
			else if(operClass!=null&&(operClass.equals("com.alpine.miner.gef.runoperator.field.ReplaceNullOperator")
					  || operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopReplaceNullOperator"))){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, NullReplacementModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element interActionElement=(Element)nodes.get(0) ;
						NullReplacementModel nrModel= NullReplacementModel.fromXMLElement(interActionElement);
						operator.appendChild(nrModel.toXMLElement(xmlDoc));
					}	
			  } 
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.association.AssociationOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, ExpressionModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element interActionElement=(Element)nodes.get(0) ;
						ExpressionModel expModel= ExpressionModel.fromXMLElement(interActionElement);
						operator.appendChild(expModel.toXMLElement(xmlDoc));
					}	
			  }
			
			
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.linearregression.LinearRegressionOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, InterActionColumnsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element interActionElement=(Element)nodes.get(0) ;
						InterActionColumnsModel interActionModel= InterActionColumnsModel.fromXMLElement(interActionElement);
						operator.appendChild(interActionModel.toXMLElement(xmlDoc));
					}	
			  }
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.logisticregression.LogisticRegressionOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, InterActionColumnsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element interActionElement=(Element)nodes.get(0) ;
						InterActionColumnsModel interActionModel= InterActionColumnsModel.fromXMLElement(interActionElement);
						operator.appendChild(interActionModel.toXMLElement(xmlDoc));
					}	
			  }
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.adaboost.AdaboostOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, AdaboostPersistenceModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element adaboostUIModelElement=(Element)nodes.get(0) ;
						AdaboostPersistenceModel adaboostUIModel= AdaboostPersistenceModel.fromXMLElement(adaboostUIModelElement) ;
						operator.appendChild(adaboostUIModel.toXMLElement(xmlDoc));
					}
			  }
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.field.TableSetOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, TableSetModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element tableSetUIModelElement=(Element)nodes.get(0) ;
						TableSetModel tableSetUIModel = TableSetModel.fromXMLElement(tableSetUIModelElement);
						operator.appendChild(tableSetUIModel.toXMLElement(xmlDoc));
					}
			  }
 
			
			else if(operClass!=null&&(operClass.equals("com.alpine.miner.gef.runoperator.sampling.StratifiedSamplingOperator")
					  ||operClass.equals("com.alpine.miner.gef.runoperator.sampling.RandomSamplingOperator")
					  ||operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopRandomSamplingOperator"))
					  ){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, SampleSizeModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element sampleSizeUIModelElement=(Element)nodes.get(0) ;
						SampleSizeModel sampleSizeUIModel = SampleSizeModel.fromXMLElement(sampleSizeUIModelElement);
						operator.appendChild(sampleSizeUIModel.toXMLElement(xmlDoc));
					}else {//For 2.8- version flow.Migrate "String" sampleSize to "Model" sampleSize.
						ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(opNode, "Parameter");
						if(parameterNodeList!=null){
							String sampleCount=null;
							String sampleSize=null;
							for (Node parameterNode: parameterNodeList) {
								String key=((Element)parameterNode).getAttribute("key");
								String value=((Element)parameterNode).getAttribute("value");
								if(OperatorParameter.NAME_sampleCount.equals(key)){
									sampleCount=value;
								}else if (OperatorParameter.NAME_sampleSize.equals(key)){
									sampleSize=value;
								}
								if(StringUtil.isEmpty(sampleCount)==false&&
										StringUtil.isEmpty(sampleSize)==false
										&&AlpineUtil.isInteger(sampleCount)
										&&AlpineUtil.isNumber(sampleSize)){
									List<String> sampleIdList=new ArrayList<String>();
									List<String> sampleSizeList=new ArrayList<String>();
									for(int i=0;i<Integer.parseInt(sampleCount);i++){
										sampleIdList.add(String.valueOf(i+1));
										sampleSizeList.add((String.valueOf(Double.parseDouble(sampleSize)/Integer.parseInt(sampleCount))));
									}
									SampleSizeModel sampleSizeUIModel=new SampleSizeModel(sampleIdList, sampleSizeList);
									operator.appendChild(sampleSizeUIModel.toXMLElement(xmlDoc));
								}
							}
							
						}
						
					}
			  }
			  //for hadoop
			else if(operClass!=null&&
					(operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopFileOperator")
							||operClass.equals("com.alpine.miner.gef.runoperator.hadoop.CopytoHadoopOperator")
									||operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopPigExecuteOperator")
							)){
				 
				  FileStructureModel fileStructureModel=FileStructureModelFactory.
							createFileStructureModelByXML(opTypeXmlManager,opNode);
					operator.appendChild(fileStructureModel.toXMLElement(xmlDoc));
				 
					  if(  operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopPigExecuteOperator"))
						 {
						  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, PigExecutableModel.TAG_NAME);
						 		if(nodes!=null&&nodes.size()>0){
								Element tableSetUIModelElement=(Element)nodes.get(0) ;
								PigExecutableModel model = PigExecutableModel.fromXMLElement(tableSetUIModelElement);
								operator.appendChild(model.toXMLElement(xmlDoc));
							}
					  } 
			  } 
			else  if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopHistogramOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, ColumnBinsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element interActionElement=(Element)nodes.get(0) ;
						ColumnBinsModel columnBinModel= ColumnBinsModel.fromXMLElement(interActionElement);
						operator.appendChild(columnBinModel.toXMLElement(xmlDoc));
					}	
			  }
			  
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopAggregateOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, AggregateFieldsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element tableSetUIModelElement=(Element)nodes.get(0) ;
						AggregateFieldsModel model = AggregateFieldsModel.fromXMLElement(tableSetUIModelElement);
						operator.appendChild(model.toXMLElement(xmlDoc));
					}
			  }
 
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopVariableOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, DerivedFieldsModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element tableSetUIModelElement=(Element)nodes.get(0) ;
						DerivedFieldsModel model = DerivedFieldsModel.fromXMLElement(tableSetUIModelElement);
						operator.appendChild(model.toXMLElement(xmlDoc));
					}
			  }
			else  if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopJoinOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, HadoopJoinModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element hadoopJoinModelElement=(Element)nodes.get(0) ;
						HadoopJoinModel model = HadoopJoinModel.fromXMLElement(hadoopJoinModelElement);
						operator.appendChild(model.toXMLElement(xmlDoc));
					}
			  } 
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.hadoop.HadoopUnionOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, HadoopUnionModel.TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element hadoopUnionModelElement=(Element)nodes.get(0) ;
						HadoopUnionModel model = HadoopUnionModel.fromXMLElement(hadoopUnionModelElement); 
						operator.appendChild(model.toXMLElement(xmlDoc));
					}
			  } 
			  //----------------------end of hadoop --------------------------
			else if(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.structual.SubFlowOperator")){
				  ArrayList<Node> nodes = opTypeXmlManager.getNodeList(opNode, VariableModel.MODEL_TAG_NAME);
					if(nodes!=null&&nodes.size()>0){
						Element subFlowUIModelElement=(Element)nodes.get(0) ;
						VariableModel subFlowUIModel= VariableModel.fromXMLElement(subFlowUIModelElement) ;
						operator.appendChild(subFlowUIModel.toXMLElement(xmlDoc));
					}
					
					  nodes = opTypeXmlManager.getNodeList(opNode, TableMappingModel.TAG_NAME);
						if(nodes!=null&&nodes.size()>0){
							Element subFlowTableMappingModelElement=(Element)nodes.get(0) ;
							TableMappingModel subFlowUIModel= TableMappingModel.fromXMLElement(subFlowTableMappingModelElement) ;
							
							operator.appendChild(subFlowUIModel.toXMLElement(xmlDoc,addSuffixToOutput,suffix));
						}
					
			  }
			  
			
			ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(opNode, "Parameter");
			for (Node parameterNode: parameterNodeList) {
				Element parameter = xmlDoc.createElement("Parameter");
				String key=((Element)parameterNode).getAttribute("key");
				String value=((Element)parameterNode).getAttribute("value");
				if(addSuffixToOutput&&XmlDocManager.OUTPUTTABLElIST.contains(key)){
				 if((operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.svd.SVDLanczosCalculatorOperator")
							||(operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.solutions.ProductRecommendationOperator"))
							||operClass!=null&&operClass.equals("com.alpine.miner.gef.runoperator.solutions.ProductRecommendationEvaluationOperator"))
							&&!key.equals(XmlDocManager.OUTPUT_TABLE)){
						List<String> parentList=parnetsList.get(operName);
						boolean isFromDbTable=false;
						for(String parent:parentList){		
							if(nameTypeMap.get(parent).equals("com.alpine.miner.gef.runoperator.datasource.DbTableOperator")){
								String parentSchemaName=dbSchemaNameMap.get(parent);
								String parentTablename=dbTableNameMap.get(parent);
								parentTablename=StringHandler.doubleQ(parentSchemaName)+"."+StringHandler.doubleQ(parentTablename);
								if(value.equals(parentTablename)){
									isFromDbTable=true;
								}
							}
						}
						if(isFromDbTable){
							parameter.setAttribute("key", key);
							parameter.setAttribute("value", value);
						}else{
							String[] temp=value.split("\\.",2);
							String newTable=temp[0]+"."+
							StringHandler.doubleQ(StringHandler.addPrefix(StringHandler.removeDoubleQ(temp[1]), suffix));
							parameter.setAttribute("key", key);
							parameter.setAttribute("value",newTable);
						}
					}else{
						String newTable=StringHandler.addPrefix(value, suffix);
						parameter.setAttribute("key", key);
						parameter.setAttribute("value", newTable);
					}
				}else if(addSuffixToOutput&&XmlDocManager.SELECTED_OUTPUT_TABLE.equals(key)){
					String[] temp=value.split("\\.",2);
					String newTable=temp[0]+"."+StringHandler.addPrefix(temp[1], suffix);
					parameter.setAttribute("key", key);
					parameter.setAttribute("value",newTable);
				}else{
					parameter.setAttribute("key", key);
					parameter.setAttribute("value", value);
				}
				operator.appendChild(parameter);
			}
			

			
			  HashMap<String,List<HashMap<String,HashMap<String,String>>>> inputFieldMap=
				  new HashMap<String,List<HashMap<String,HashMap<String,String>>>>();
			  
			  HashMap<String,List<FileStructureModel>> inPutHadoopFileFieldsMap=
					  new HashMap<String,List<FileStructureModel>>();
			  readFields(opTypeXmlManager, opNode, operName, inputFieldMap, inPutHadoopFileFieldsMap);
			  
			  saveFields(xmlDoc, operName, operator, inputFieldMap,inPutHadoopFileFieldsMap,suffix,parentDbTableList);

				
			  
			  ArrayList<Node> modelNodeList = opTypeXmlManager.getNodeList(opNode, "Model");
			  for(Node node:modelNodeList){
				  String modelString = node.getTextContent();
				  Element modelElement = xmlDoc.createElement("Model");
				  modelElement.setTextContent(modelString);
				  operator.appendChild(modelElement);
			  }
			  root.appendChild(operator);
		}
		
		ArrayList<Node> linkNodes = opTypeXmlManager.getNodeListByTag("Link");
		for (Node linkNode:linkNodes) {
			String source = linkNode.getAttributes().getNamedItem("source").getNodeValue();
			String target = linkNode.getAttributes().getNamedItem("target").getNodeValue();
			Element line = xmlDoc.createElement("Link");
			line.setAttribute("source", source);
			line.setAttribute("target", target);
			root.appendChild(line);
		}
		
		/**
		 * save parent variable model
		 */
		ArrayList<Node> variableModelNodes = opTypeXmlManager.getNodeList(opTypeXmlManager.getRootNode(),VariableModel.MODEL_TAG_NAME); 
		for (Node variableModelNode:variableModelNodes) {
			Element variableElement=(Element)variableModelNode;
			VariableModel variableModel = VariableModel.fromXMLElement(variableElement);
			updateVariableModel(variableModel,variableList);
			root.appendChild(variableModel.toXMLElement(xmlDoc));
		}
			
		File file = new File(newFilePath);
		XmlDocManager xmlDocManager = new XmlDocManager();
		try {
			BufferedWriter writer = null;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),XmlDocManager.ENCODING_UTF8));
			writer.write(xmlDocManager.xmlToLocalString(xmlDoc));
			writer.close();
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw e;
		}
	}

	private static void updateVariableModel(VariableModel variableModel,
			List<String> variableList) {
		if(variableList!=null&&variableList.size()>0){
			for(String variable:variableList){
				String[] variableArray = variable.split("=",2);
				String variableName=variableArray[0];
				String variableValue=variableArray[1];
				variableModel.addVariable(variableName, variableValue);	
			}
		}
	}

	private static void saveFields(
			Document xmlDoc,
			String operName,
			Element operator,
			HashMap<String, List<HashMap<String, HashMap<String, String>>>> inputFieldMap,
			HashMap<String, List<FileStructureModel>> inPutHadoopFileFieldsMap, String suffix, List<String> parentDbTableList) {
		boolean addSuffixToOutput=false;
		if(!StringUtil.isEmpty(suffix)){
			addSuffixToOutput=true;
		}
		List<HashMap<String, HashMap<String, String>>> inputFields=inputFieldMap.get(operName);
		List<FileStructureModel> inPutHadoopFileFields = inPutHadoopFileFieldsMap.get(operName);
		int count=0;
		for(HashMap<String, HashMap<String, String>> hm:inputFields){
			HashMap<String, String>  paras=hm.get("Parameter");
			HashMap<String, String> fields=hm.get("Fields");
			Element ele = xmlDoc.createElement("InPutFieldList");			
			Iterator<Entry<String, String>> iter_paras=paras.entrySet().iterator();
			while(iter_paras.hasNext()){
				Entry<String, String>  entry=iter_paras.next();
				Element para = xmlDoc.createElement("Parameter");
				if(addSuffixToOutput&&
						!parentDbTableList.contains(operName)&&
						entry.getKey().equals("table")	
						){
					para.setAttribute("key", entry.getKey());
					para.setAttribute("value", StringHandler.addPrefix(entry.getValue(), suffix));
				}else{
					para.setAttribute("key", entry.getKey());
					para.setAttribute("value", entry.getValue());
				}
				ele.appendChild(para);
			}
			if(fields!=null){
				Element fieldList = xmlDoc.createElement("Fields");
				ele.appendChild(fieldList);
				Iterator<Entry<String, String>> iter_fields=fields.entrySet().iterator();
				while(iter_fields.hasNext()){
					Entry<String, String>  entry=iter_fields.next();
					Element field = xmlDoc.createElement("Field");
					field.setAttribute("name", entry.getKey());
					field.setAttribute("type", entry.getValue());
					fieldList.appendChild(field);
				}
			}
			if(inPutHadoopFileFields!=null&&inPutHadoopFileFields.size()>count&&inPutHadoopFileFields.get(count)!=null){
				ele.appendChild(inPutHadoopFileFields.get(count).toXMLElement(xmlDoc));
			}
			
			operator.appendChild(ele);	
			count++;
		}
		
	}

	private static void readFields(
			XmlDocManager opTypeXmlManager,
			Node opNode,
			String operName,
			HashMap<String, List<HashMap<String, HashMap<String, String>>>> inputFieldMap,
			HashMap<String, List<FileStructureModel>> inPutHadoopFileFieldMap){
		List<HashMap<String,HashMap<String,String>>> inPutField=new ArrayList<HashMap<String,HashMap<String,String>>>();
		List<FileStructureModel> inPutHadoopFileFields=new ArrayList<FileStructureModel>();
		ArrayList<Node> inPutFieldList=opTypeXmlManager.getNodeList(opNode, "InPutFieldList");	
		for (Node inputNode: inPutFieldList) {
			HashMap<String,HashMap<String,String>> paraAndField = new HashMap<String,HashMap<String,String>>();
			ArrayList<Node> parametersList=opTypeXmlManager.getNodeList(inputNode, "Parameter");
			HashMap<String,String> parameterFields = new HashMap<String,String>();
			for(Node paraNode: parametersList){
				parameterFields.put(((Element)paraNode).getAttribute("key"),((Element)paraNode).getAttribute("value"));	
			}	
			paraAndField.put("Parameter", parameterFields);
			inPutField.add(paraAndField);
			ArrayList<Node> fieldsList=opTypeXmlManager.getNodeList(inputNode, "Fields");
			
			if(fieldsList!=null&&fieldsList.isEmpty()==false){
				ArrayList<Node> fieldList=opTypeXmlManager.getNodeList(fieldsList.get(0), "Field");
				HashMap<String,String> fields = new HashMap<String,String>();
				for(Node paraNode: fieldList){
					fields.put(((Element)paraNode).getAttribute("name"),((Element)paraNode).getAttribute("type"));	
				}
				paraAndField.put("Fields", fields);
			}
		
			FileStructureModel fileStructureModel=FileStructureModelFactory.
					createFileStructureModelByXML(opTypeXmlManager,inputNode);
			inPutHadoopFileFields.add(fileStructureModel);
	
			
		}
		inPutHadoopFileFieldMap.put(operName, inPutHadoopFileFields);
		inputFieldMap.put(operName, inPutField);
	}
	
	private static void fillAllSubflowName(String filePath,List<String> missedSubflowNames,List<String> allSubflowNames) throws Exception {
		  XmlDocManager opTypeXmlManager = new XmlDocManager();
		  File f = new File(filePath) ;
		  if(f.exists()==false){
		   missedSubflowNames.add(filePath);
		   return;
		  }else{
		   allSubflowNames.add(filePath) ;
		  }
		  try {
		   opTypeXmlManager.parseXMLFile(filePath);
		  } catch (Exception e) {
			  itsLogger.error(e.getMessage(),e);
		   throw e;
		  }
	 
		  
		  ArrayList<Node> opNodes = opTypeXmlManager.getNodeListByTag("Operator");
		  
		  String folder = filePath.substring(0,filePath.lastIndexOf(File.separator)+1);
		  for (Node opNode:opNodes){
		   if(MiningUtil.isSubflowNode(opNode)){
		    ArrayList<Node> paramNodes = opTypeXmlManager.getNodeList(opNode, "Parameter");
		    for (Node paramNode:paramNodes){
		     if("subflowPath".equals(((Element)paramNode).getAttribute("key"))){
		      String subflowName = ((Element)paramNode).getAttribute("value");
		      fillAllSubflowName(folder+subflowName+".afm"  ,  missedSubflowNames,allSubflowNames);
		      
		     }
		    }
		    //<Parameter key="subflowPath" value="sub_exploration-a"/>
		   }
		  }
		   
		  return  ;
		 }

} 