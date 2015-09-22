/**
 * ClassName  AnalyticThread.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-17
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticFlowMetaInfo;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.AbstractAnalyticSource;
import com.alpine.datamining.api.impl.AnalyticResultImpl;
import com.alpine.datamining.api.impl.AnalyzerFactory;
import com.alpine.datamining.api.impl.DBTableSelectorConfig;
import com.alpine.datamining.api.impl.ModelWrapperConfig;
import com.alpine.datamining.api.impl.algoconf.ARIMARPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.CopyToDBConfig;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopKMeansConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPigExecuteConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopRandomSamplingConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopSampleSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.ModelNeededConfig;
import com.alpine.datamining.api.impl.algoconf.SQLAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.DBTableSelector;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.CopyToDBAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModelItem;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionSourceColumn;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;
import com.alpine.datamining.api.impl.db.execute.SQLAnalyzer;
import com.alpine.datamining.api.impl.db.predictor.ARIMARPredictor;
import com.alpine.datamining.api.impl.db.trainer.AdaboostTrainer;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.CopytoHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopPigExecuteAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopSampleSelectorAnalyzer;
import com.alpine.datamining.api.impl.hadoop.kmeans.HadoopKmeansAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.datamining.workflow.exporter.html.AnalyticResultHTMLExporter;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.log.LogEvent;
import com.alpine.utility.log.LogPoster;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author John Zhao
 * 
 */
public class AnalyticRunner  {
	private static final Logger itsLogger = Logger.getLogger(AnalyticRunner.class);
	protected AnalyticProcess process;

	private List<AnalyticProcessListener> listeners;
	
	private AnalyticFlowMetaInfo metaInfo;
	
	protected HashMap<String, AnalyticOutPut> outPuts;
	
	protected List<DataAnalyzer> analyzerList;
 
	protected boolean stop=false;
	
	private boolean visual;
	
	private String outputDir;
	//"userName-url"-> connection
	private HashMap<String,Connection> connMap=new HashMap<String,Connection>();
	
	protected Locale locale = Locale.getDefault();
	private String processID;
	private AnalyticContext context;
	private String executeUser;
	private String flowRunUUID = null;  
 
 

	public AnalyticContext getContext() {
		return context;
	}

 
	public AnalyticRunner(AnalyticProcess process,
			List<AnalyticProcessListener> listeners ,boolean visual,Locale locale,String executeUser ) throws Exception {
		this(process,  listeners ,  visual,  locale , null, executeUser) ;
		 
	}
	
	 
		public AnalyticRunner(AnalyticProcess process,
				List<AnalyticProcessListener> listeners ,boolean visual,Locale locale ,  AnalyticContext context,String executeUser) throws Exception {
	 		if(itsLogger.isDebugEnabled()){
				itsLogger.debug("Create a new AnalyticRunner for :" + process.getFlowFilePath());
			}
	 		 
	 		if(listeners!=null&&listeners.size()>0){
	 			for (Iterator iterator = listeners.iterator(); iterator
						.hasNext();) {
					AnalyticProcessListener analyticProcessListener = (AnalyticProcessListener) iterator
							.next();
					flowRunUUID = analyticProcessListener.getFlowRunUUID();
					break;
				}
	 		}
	 		
			this.process = process;
			this.processID = process.getProcessID();
			this.executeUser=executeUser;
			this.listeners = listeners;
			this.analyzerList=new ArrayList<DataAnalyzer>();
			metaInfo = new AnalyticFlowMetaInfo();
			this.visual=visual;
			this.locale=locale;
			if(context==null){
				this.context = new AnalyticContext();
		 	this.context.setLocalModelPig(isLocalModelPig(process.getFlow().getAllNodes())); 
			}else{
				this.context = context ;
			}
			 
		}

	protected boolean isLocalModelPig(List<AnalyticNode> allNodes)
			throws Exception {
		int threshhold = Integer.parseInt(ProfileReader.getInstance()
				.getParameter(ProfileUtility.LOCAL_HD_RUNNER_THRESHOLD));
		BigDecimal threshholdNative = new BigDecimal(threshhold);
		threshholdNative = threshholdNative
				.multiply(new BigDecimal(1024 * 1024));
		for (AnalyticNode node : allNodes) {
			// when have copy to hadoop will always use remote mode.
			if (node.getAnalyzerClass() != null
					&& node.getAnalyzerClass().equals(
							CopytoHadoopAnalyzer.class.getName())) {
				return false;
			} else if (node.getAnalyzerClass() != null
					&& node.getAnalyzerClass().equals(
							HadoopFileSelector.class.getName())) {
				String filePath = ((HadoopAnalyticSource) node.getSource())
						.getFileName();
				HadoopConnection connection = ((HadoopAnalyticSource) node
						.getSource()).getHadoopInfo();

				boolean isLocalModel = HadoopHDFSFileManager.INSTANCE
						.isLocalModelNeeded(filePath, connection);
				if (isLocalModel == false) {
					return false;
				}

			} else if (node.getAnalyzerClass() != null
					&& node.getAnalyzerClass().equals(
							HadoopPigExecuteAnalyzer.class.getName())) {
				HadoopPigExecuteConfig pigConfig = (HadoopPigExecuteConfig) ((HadoopAnalyticSource) node
						.getSource()).getAnalyticConfig();
				HadoopConnection connection = ((HadoopAnalyticSource) node
						.getSource()).getHadoopInfo();
				AnalysisPigExecutableModel pigScriptModel = pigConfig
						.getPigScriptModel();
				String scritp = pigScriptModel.getPigScript();
				//TODO: handle pig script execute...
				// List<String>
				// try {
				// HadoopFile hFile =
				// HadoopHDFSFileManagerFactoryImpl.INSTANCE.getHadoopHDFSFileManager(connection).getHadoopFile(filePath,
				// connection);
				// if(hFile.getLength()>threshholdNative){
				// return false;
				// }
				// } catch (Exception e) {
				// e.printStackTrace();
				// }

			}
		}
		return true;
	}


	public String getProcessID() {
		return processID;
	}

 
	public void run() throws Exception {
        if(itsLogger.isDebugEnabled()){
			itsLogger.debug("Before flow running "
				+" total memory="+Runtime.getRuntime().totalMemory()
				+" free memory="+Runtime.getRuntime().freeMemory());
		}
		AlpineThreadLocal.setLocale(locale) ;
		String thredName=Thread.currentThread().getName();
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug(thredName + " Runing..."+ process.getName());
		}
		try {
			 
			runAnalysisProcess();
		// in case of out of menory
		} catch (Throwable e) {
 
			itsLogger.error(e.getMessage(),e);
			
			notifyError(e);
  
			throw new Exception(e) ;
 
		} 
		
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug(thredName+ "Finished..." );
		}
    }
	//will call this while the flow is finished or stoped

	protected void releaseContext() {
		 
		if(context!=null){
			context.dispose();
		}
		
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("After releaseContext "
				+" total memory="+Runtime.getRuntime().totalMemory()
				+" free memory="+Runtime.getRuntime().freeMemory());
		}
	}

	/**
	 * @param e
	 */
	public void notifyError(Throwable e) {
		if (listeners != null) {
			for (AnalyticProcessListener listener:listeners) {
				listener.processError(e);
			}
		}
	}

	/**
	 * @throws Exception
	 */
	private void runAnalysisProcess() throws Exception {
		

		String processID = process.getProcessID();
	
		
		initFlowMetaInfo();
		
		Properties configProps = initConfigProps();
		
	    metaInfo.setAnalyticServerConfig(configProps);
		
		outPuts = new HashMap<String, AnalyticOutPut>();
		// put into the map for the code to save the output

		List<AnalyticNode> allNodes =  process.getFlow().getAllNodes();

		// will store the out put in the outPuts map
		 
		doAnalysisForNodes(process, listeners, allNodes);

		AnalyticResultImpl result = getAnalyticResult(processID);

		process.getFlow().setFinished(true);


		if(	outputDir!=null&&outputDir.trim().length()>0){
			AnalyticResultHTMLExporter.exportToDir(result,outputDir);
		}
		
		if (listeners != null) {
			for (AnalyticProcessListener listener:listeners) {
				listener.analyticFlowFinished(result);
			}
		}

	}
 

	private AnalyticResultImpl getAnalyticResult(String processID) {
		AnalyticResultImpl result = new AnalyticResultImpl(processID);

		addOutPutIntoResult(  result);

		metaInfo.setEndTime(Calendar.getInstance().getTime());
		result.setAnalyticMetaInfo(metaInfo);
		return result;
	}

	/**
	 * @return
	 */
	private Properties initConfigProps() {
		Properties props = System.getProperties();
		Properties configProps=new Properties();
		for (Iterator iterator = props.keySet().iterator(); iterator.hasNext();) {
			String  key = (String) iterator.next();
			if(key.equals("java.vendor")){
				configProps.put("Java Vendor", props.get(key));
			}
			else if(key.equals("os.name")){
				configProps.put("Operation System", props.get(key));
			}else if( key.equals("java.version")){
				configProps.put("Java Version", props.get(key));
			}
		}
		return configProps;
	}

	/**
	 * 
	 */
	private void initFlowMetaInfo() {
		metaInfo.setFlowOwnerUser(process.getFlow().getFlowOwnerUser());
		metaInfo.setFlowDescription(process.getFlow().getFlowDescription());
		 
		metaInfo.setStartTime(Calendar.getInstance().getTime());
		metaInfo.setFlowFileName(process.getName());
		metaInfo.setExecuteUserName(executeUser);
	}

	public AnalyticFlowMetaInfo getMetaInfo() {
		return metaInfo;
	}

	private void doAnalysisForNodes(AnalyticProcess process,
			List<AnalyticProcessListener> listeners, List<AnalyticNode> nodes)
			throws Exception {
		for (AnalyticNode analyticNode : nodes) {
			doAnalysis(process, analyticNode, listeners);
		}
	}

	//should wait for all parent is OK
	//will not use the output, will only use the info from xml...
	//please see the mining util...
	private void enhanceConfig( 
			AnalyticNode analyticNode  ) {
		List<AnalyticNode> parentNodes = analyticNode.getParentNodes();
		 
		
		AbstractAnalyticSource source=(AbstractAnalyticSource)analyticNode.getSource(); 
		//training and predictor ,training is ok from mingutil
		if (source.getAnalyticConfig() instanceof ModelNeededConfig) {
			//set train model ...
			for (AnalyticNode parentNode: parentNodes) {
				AnalyticOutPut parentOutPut=outPuts.get(parentNode.getName());
				// for predict and evaluator --ModelNeededConfig
				
				ModelNeededConfig config = (ModelNeededConfig) source
							.getAnalyticConfig();
				if ( parentOutPut != null//config.getTrainedModel() == null &&
						&& parentOutPut instanceof AnalyzerOutPutTrainModel) {
	
					config
								.setTrainedModel(((AnalyzerOutPutTrainModel) parentOutPut)
										.getEngineModel());
					break;
				}
				
			}
		}//evaluator
		else if (source.getAnalyticConfig() instanceof EvaluatorConfig) {
			
			//predictor can support more than 1 models...
			//1 get all parent
			//if parent is model 
			boolean firsttime = true;
			for (AnalyticNode parentNode:parentNodes) {
				AnalyticOutPut parentOutPut=outPuts.get(parentNode.getName());
				// for predict and evaluator --ModelNeededConfig
				
				EvaluatorConfig config = (EvaluatorConfig) source
							.getAnalyticConfig();
					if (firsttime) {
						config.clearTrainedModels();
				    	firsttime = false;
					}
					if ( parentOutPut != null
							&& parentOutPut instanceof AnalyzerOutPutTrainModel) {
	
						config.addTrainedModel(((AnalyzerOutPutTrainModel) parentOutPut)
										.getEngineModel());
					 
					}
				
				}
			
		}//hadoop union
		else if (source.getAnalyticConfig() instanceof HadoopUnionConfig) {
			LinkedHashMap<String,  AnalysisFileStructureModel>  fileStructureModelList = new LinkedHashMap<String,  AnalysisFileStructureModel> ();  

 			HashMap<String, List<String>> columnMap = new HashMap<String, List<String>>();  
			for (AnalyticNode parentNode:parentNodes) {
				List<String> columnNameList = ((HadoopAnalyticSource)parentNode.getSource()).getHadoopFileStructureModel().getColumnNameList();
				fileStructureModelList.put(((HadoopAnalyticSource)parentNode.getSource()).getFileName(), ((HadoopAnalyticSource)parentNode.getSource()).getHadoopFileStructureModel()) ;
				String modelID = parentNode.getID();
				columnMap.put(modelID, ListUtility.cloneStringList(columnNameList) ) ;
 
				}
			((HadoopUnionConfig)source.getAnalyticConfig() ).setInputColumnMap(columnMap);
			((HadoopUnionConfig)source.getAnalyticConfig() ).setInputFileStructureModelList(fileStructureModelList);
		}
		
	}

 

	/**
     * @param process
     * @param analyticNode
	 * @param listeners
	 * @return
	 * @throws Exception
	 */
	protected void doAnalysis(AnalyticProcess process, AnalyticNode analyticNode,
			List<AnalyticProcessListener> listeners) throws Exception {

		if(analyticNode.isFinished()==true||stop==true)
			return;
		
		
		AnalyticSource source = analyticNode.getSource();

		List<AnalyticNode> parentNodes = analyticNode.getParentNodes();
		// make sure the parenet is OK
		for (AnalyticNode parentNode  : parentNodes) {
			if (parentNode != null) {
				if (parentNode.isFinished() == false) {
					doAnalysis(process, parentNode, listeners);
				}
			}
		}
		
		notifyNodeStart(analyticNode, listeners);


		AnalyticOutPut outPut = executeNode(analyticNode, source,true);
        long numSecs = Math.round((outPut.getAnalyticNodeMetaInfo().getEndTime().getTime() - outPut.getAnalyticNodeMetaInfo().getStartTime().getTime())/1000);
        String username = null;
        if (this.metaInfo != null)
        {
            username = this.metaInfo.getExecuteUserName();
        }
        LogEvent e = LogPoster.getInstance().createEvent(LogPoster.Operator_Execute,analyticNode.getAnalyzerClass(),username);
        e.addExtra(LogPoster.Operator_Type, analyticNode.getAnalyzerClass());
        e.addExtra(LogPoster.Operator_Exec_Time, Long.toString(numSecs));
        LogPoster.getInstance().addEvent(e);
		notifyNodeFinished(analyticNode, listeners, outPut);
		

	}

	/**
	 * @param analyticNode
	 * @param source
	 * @return
	 * @throws AnalysisException
	 * @throws Exception
	 */
	protected AnalyticOutPut executeNode(AnalyticNode analyticNode,
			AnalyticSource source,boolean saveResult) throws AnalysisException, Exception {
		enhanceConfig( analyticNode); 
		 
		String analyzerClass = analyticNode.getAnalyzerClass();
		String oldTableName=null;
		String oldSchemaName=null;
		if(source instanceof DataBaseAnalyticSource
				&&source.getAnalyticConfig() instanceof DBTableSelectorConfig==false
				&&source.getAnalyticConfig() instanceof ModelWrapperConfig==false
				&&source.getAnalyticConfig() instanceof ARIMARPredictorConfig==false
				&&source.getAnalyticConfig() instanceof SQLAnalysisConfig==false){
			  oldTableName=((DataBaseAnalyticSource)source).getTableInfo().getTableName();
			  oldSchemaName=((DataBaseAnalyticSource)source).getTableInfo().getSchema();
 		}
		
		DataAnalyzer analyzer = AnalyzerFactory.getAnalyzer(analyzerClass);
		if(analyzer instanceof AbstractHadoopAttributeAnalyzer){
			((AbstractHadoopAttributeAnalyzer)analyzer).setSaveResult( saveResult);
		}
		analyzer.setContext(getContext()) ;
		analyzer.setListeners(listeners) ;
		analyzerList.add( analyzer);
		analyzer.setName(analyticNode.getName());
		analyzer.setUUID(analyticNode.getID()) ;
		analyzer.setFlowRunUUID(flowRunUUID);
		Date startTime=Calendar.getInstance().getTime();
		//this is important, may need some pretreatment before done...
		//only these 1 need not the connection
		if(analyzer instanceof EngineModelWrapperAnalyzer==false){
			setDBConnection(source,analyzer);
		}
		setChildResultNameForHadoop(analyticNode, source, analyzer);
		analyzer.setAnalyticSource(source);
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("Before analysis:"+analyzer.getName()
				+" total memory="+Runtime.getRuntime().totalMemory()
				+" free memory="+Runtime.getRuntime().freeMemory());
		}
		AnalyticOutPut outPut = analyzer.doAnalysis(source);
		
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("After analysis:"+analyzer.getName()
					+" total memory="+Runtime.getRuntime().totalMemory()
					+" free memory="+Runtime.getRuntime().freeMemory());
		}
		
		
		//this is important
		analyzer.setOutPut(outPut);
		outPut.setDataAnalyzer(analyzer) ;
	
		VisualizationManager vm = VisualizationManager.instance;
		outPut.setAnalyticNode(analyticNode);
		outPut.setVisualizationTypeClass(source.getAnalyticConfig()
				.getVisualizationTypeClass());

		 
			vm.visual(outPut,visual);
	 
				
		//set the analyzer for the vOut...
		VisualizationOutPut vOut = outPut.getVisualizationOutPut();
		if(!(analyzer instanceof AdaboostTrainer)){
			setOutAnlyzer(analyzer, vOut);
		}			
		enhanceMetaInfo(analyticNode, startTime, outPut);
		analyticNode.setFinished(true);
		if(source instanceof DataBaseAnalyticSource
				&&source.getAnalyticConfig() instanceof DBTableSelectorConfig==false
				&&source.getAnalyticConfig() instanceof ModelWrapperConfig==false
				&&source.getAnalyticConfig() instanceof ARIMARPredictorConfig==false
				&&source.getAnalyticConfig() instanceof SQLAnalysisConfig==false){
			//avoid someone change the table...
			((DataBaseAnalyticSource)source).getTableInfo().setTableName(oldTableName);
			((DataBaseAnalyticSource)source).getTableInfo().setSchema(oldSchemaName);
		}
		outPuts.put(analyticNode.getName(), outPut);
		return outPut;
	}
	
	private void generateAndRegisterDesignTimePigVariables(
			HadoopRandomSamplingConfig randomSamplingConfig,
			String inputTempName) throws AnalysisException {
		if(null==inputTempName||null==randomSamplingConfig||null==randomSamplingConfig.getSampleSize()||null==randomSamplingConfig.getSampleSize().getSampleIdList()){
			throw new AnalysisException("Either selected sample ids,size at config["+randomSamplingConfig+"] or inputName["+inputTempName+"] is Null ");
		}
		List<Integer> ids = randomSamplingConfig.getSampleSize().getSampleIdList();
		if(null==randomSamplingConfig.getResultsName()){
			//throw new AnalysisException("Destination file name is not selected");
		}
		for (int id = 0; id < ids.size(); id++) {
			String key = (null==randomSamplingConfig.getResultsLocation())?"":randomSamplingConfig.getResultsLocation()
					+ HadoopFile.SEPARATOR
					+ ((null==randomSamplingConfig.getResultsName())?"":randomSamplingConfig.getResultsName()) + "_" + ids.get(id);
			String value = inputTempName + "_" + ids.get(id);
			getContext().addDesignTimePigVariable(key, value);
		}

	}

	protected void setChildResultNameForHadoop(AnalyticNode analyticNode,
			AnalyticSource source, DataAnalyzer analyzer) throws AnalysisException {
		if(analyzer instanceof AbstractHadoopAttributeAnalyzer){
			if(analyzer instanceof HadoopSampleSelectorAnalyzer){
				HadoopSampleSelectorConfig  sampleConfig = (HadoopSampleSelectorConfig)source.getAnalyticConfig();
				String resultName = sampleConfig.getSelectedFile();
				setChildFileNameForHadoop(analyticNode, resultName,resultName);
			}else{
				HadoopDataOperationConfig hConfig=(HadoopDataOperationConfig)(source).getAnalyticConfig();
				String resultName = hConfig.getResultsName();
				String tempName;
				if(StringUtil.isEmpty(resultName)){
					tempName=null;
					if(hConfig instanceof HadoopSampleSelectorConfig ){
						HadoopSampleSelectorConfig  sampleConfig = (HadoopSampleSelectorConfig) hConfig;
						tempName=getContext().getPigVariableNameForTheFileOf(sampleConfig.getSelectedFile());
					}
					if(null==tempName){
						tempName="A"+System.currentTimeMillis();
					}
					
					hConfig.setResultsName(tempName);
					resultName=tempName;
					
				}else{
					tempName = resultName;
				}
				String resultLocation = hConfig.getResultsLocation();
				String fileName = null==resultName?
								  "":resultLocation+HadoopFile.SEPARATOR+resultName;
				setChildFileNameForHadoop(analyticNode, fileName,tempName);
			}
		}else if(analyzer instanceof HadoopFileSelector){
			HadoopFileSelectorConfig hConfig=(HadoopFileSelectorConfig)(source).getAnalyticConfig();
			String fileName = hConfig.getHadoopFileName();
			setChildFileNameForHadoop(analyticNode, fileName);
		}else if(analyzer instanceof CopytoHadoopAnalyzer){
			CopytoHadoopConfig hConfig=(CopytoHadoopConfig)(source).getAnalyticConfig();
			String fileName = hConfig.getHadoopFileName();
			setChildFileNameForHadoop(analyticNode, fileName);
		}else if(analyzer instanceof HadoopKmeansAnalyzer){
			HadoopKMeansConfig hConfig=(HadoopKMeansConfig)(source).getAnalyticConfig();
			String fileName = hConfig.getResultsLocation()+HadoopFile.SEPARATOR+hConfig.getResultsName() ; 
			setChildFileNameForHadoop(analyticNode, fileName);
		}
	}
	private void setChildFileNameForHadoop(AnalyticNode analyticNode,
			String fileName,String tempName) throws AnalysisException {
		List<AnalyticNode> childNodes = analyticNode.getChildNodes();
		if(childNodes!=null){
			for (Iterator<AnalyticNode> iterator = childNodes.iterator(); iterator.hasNext();) {
				AnalyticNode analyticNode2 = iterator.next();
				AnalyticSource source = analyticNode2.getSource();
				if(source instanceof HadoopAnalyticSource){
					((HadoopAnalyticSource)source).setFileName(fileName);
					
					String uuid = analyticNode.getID().replace(".",""); // ". will cause pig problem" 
					String inputTempName= AbstractHadoopAnalyzer.OUT_PREFIX + uuid;
					((HadoopAnalyticSource)source).setInputTempName(AbstractHadoopAnalyzer.OUT_PREFIX + uuid);

					if(source instanceof HadoopAnalyticSource &&  ((HadoopAnalyticSource) source).getAnalyticConfig() instanceof HadoopRandomSamplingConfig){
						  HadoopRandomSamplingConfig selectorConfig = ((HadoopRandomSamplingConfig) source.getAnalyticConfig());
						  generateAndRegisterDesignTimePigVariables(selectorConfig,inputTempName);
					      ((HadoopAnalyticSource)source).setInputTempName(inputTempName);
					}
					else if(source instanceof HadoopAnalyticSource && ((HadoopAnalyticSource) source).getAnalyticConfig() instanceof HadoopSampleSelectorConfig){
						 HadoopSampleSelectorConfig selectorConfig = (HadoopSampleSelectorConfig) ((HadoopAnalyticSource) source).getAnalyticConfig();
						 String p1=getContext().getDesignTimePigVariableNameForTheFileOf(selectorConfig.getSelectedFile());
						 if (null==p1){
							 p1=getContext().getPigVariableNameForTheFileOf(selectorConfig.getSelectedFile());
						 }
						 ((HadoopAnalyticSource)source).setInputTempName(null==p1?inputTempName:p1);
					}else if(source instanceof HadoopAnalyticSource && isNodeSampleSelector(analyticNode)) {
						 updateChildNodesTempFileNames(analyticNode,analyticNode2);
					}
					
				}
				if(StringUtil.isEmpty(tempName)==false){
					AnalyticConfiguration config = source.getAnalyticConfig();
					if(config instanceof HadoopJoinConfig){
						HadoopJoinConfig hadoopJoinConfig = (HadoopJoinConfig)config;
						AnalysisHadoopJoinModel joinModel = hadoopJoinConfig.getJoinModel();
						if(joinModel!=null&&joinModel.getJoinTables()!=null){
							List<AnalysisHadoopJoinFile> joinTables = joinModel.getJoinTables();
							for(AnalysisHadoopJoinFile joinFile:joinTables){
								if(analyticNode.getID().equals(joinFile.getOperatorModelID())){
									joinFile.setFile(tempName);
									if(source instanceof HadoopAnalyticSource && isNodeSampleSelector(analyticNode)){
										AnalyticSource parentConfig=(HadoopAnalyticSource)analyticNode.getSource();
										HadoopSampleSelectorConfig ac = (HadoopSampleSelectorConfig) parentConfig.getAnalyticConfig();
										String newId = getContext().getDesignTimePigVariableNameForTheFileOf((ac.getSelectedFile()));
										joinFile.setOperatorModelID(newId);
									}
									break;
								}
							}
						}
						if(joinModel!=null&&joinModel.getJoinColumns()!=null){
							List<AnalysisHadoopJoinColumn> joinColumns = joinModel.getJoinColumns();
							for(AnalysisHadoopJoinColumn joinColumn:joinColumns){
								if(analyticNode.getID().equals(joinColumn.getFileId())){
									joinColumn.setFileName(tempName);
									if(source instanceof HadoopAnalyticSource && isNodeSampleSelector(analyticNode)){
										AnalyticSource parentConfig=(HadoopAnalyticSource)analyticNode.getSource();
										HadoopSampleSelectorConfig ac = (HadoopSampleSelectorConfig) parentConfig.getAnalyticConfig();
										String newId = getContext().getDesignTimePigVariableNameForTheFileOf((ac.getSelectedFile()));
										joinColumn.setFileId(newId);
									}
								}
							}
						}
					} else 	if(config instanceof HadoopUnionConfig){
						HadoopUnionConfig hadoopUnionConfig = (HadoopUnionConfig)config; 
						AnalysisHadoopUnionModel unionModel = hadoopUnionConfig.getUnionModel() ;
						if(unionModel!=null&&unionModel.getUnionFiles() !=null){  
							  List<AnalysisHadoopUnionFile> joinTables = unionModel.getUnionFiles();
							for(AnalysisHadoopUnionFile joinFile:joinTables){
								if(analyticNode.getID().equals(joinFile.getOperatorModelID())){
									joinFile.setFile(tempName);
									if(source instanceof HadoopAnalyticSource && isNodeSampleSelector(analyticNode)){
										AnalyticSource parentConfig=(HadoopAnalyticSource)analyticNode.getSource();
										HadoopSampleSelectorConfig ac = (HadoopSampleSelectorConfig) parentConfig.getAnalyticConfig();
										String newId = getContext().getDesignTimePigVariableNameForTheFileOf((ac.getSelectedFile()));
										joinFile.setOperatorModelID(newId);
									}
									break;
								}
							}
						}
						if(unionModel!=null&&unionModel.getOutputColumns()!=null){  
							List<AnalysisHadoopUnionModelItem> outputColumns = unionModel.getOutputColumns();
							for(AnalysisHadoopUnionModelItem item:outputColumns){
								List<AnalysisHadoopUnionSourceColumn> mappingColumns = item.getMappingColumns();
								if(mappingColumns!=null){
									for(AnalysisHadoopUnionSourceColumn column:mappingColumns){
										if(column.getOperatorModelID().equals(analyticNode.getID())){
											if(source instanceof HadoopAnalyticSource && isNodeSampleSelector(analyticNode)){
												AnalyticSource parentConfig=(HadoopAnalyticSource)analyticNode.getSource();
												HadoopSampleSelectorConfig ac = (HadoopSampleSelectorConfig) parentConfig.getAnalyticConfig();
												String newId = getContext().getDesignTimePigVariableNameForTheFileOf((ac.getSelectedFile()));
												column.setOperatorModelID(newId);
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean isNodeSampleSelector(AnalyticNode analyticNode) {
		return analyticNode.getAnalyzerClass().equals(HadoopSampleSelectorAnalyzer.class.getName());
	}

	private void updateChildNodesTempFileNames(AnalyticNode analyticNode,AnalyticNode n2) {
		List<AnalyticNode> childNodes = analyticNode.getChildNodes();
		AnalyticSource parentConfig=(HadoopAnalyticSource)analyticNode.getSource();
		HadoopSampleSelectorConfig ac = (HadoopSampleSelectorConfig) parentConfig.getAnalyticConfig();
		for(AnalyticNode c:childNodes){
			AnalyticSource source = c.getSource();
			if(source instanceof HadoopAnalyticSource){
				HadoopAnalyticSource hs = (HadoopAnalyticSource)source;
				hs.setInputTempName(getContext().getDesignTimePigVariableNameForTheFileOf((ac.getSelectedFile())));
			}
		}
		
		
	}

	private void setChildFileNameForHadoop(AnalyticNode analyticNode,
			String fileName) throws AnalysisException {
		setChildFileNameForHadoop(analyticNode,fileName,null);
	}

	/**
	 * @param analyticNode
	 * @param listeners
	 * @param outPut
	 */
	protected void notifyNodeFinished(AnalyticNode analyticNode,
			List<AnalyticProcessListener> listeners, AnalyticOutPut outPut) {
		if(listeners!=null){
			for (AnalyticProcessListener listener:listeners) {
//				if(StringUtil.isEmpty(analyticNode.getGroupID())){
					listener.finishAnalyzerNode(analyticNode.getName(), outPut);
//				}else{//for subflow
//					listener.finishAnalyzerNode(analyticNode.getGroupID()+"_"+analyticNode.getName(), outPut);
//				}
				
			}
		}
	}

	/**
	 * @param source
	 * @throws AnalysisException 
	 */
	private void setDBConnection(AnalyticSource source, DataAnalyzer analyzer)
			throws AnalysisException {
		if (source instanceof DataBaseAnalyticSource) {
			DataBaseAnalyticSource dbSource = (DataBaseAnalyticSource) source;
			String userName = null;
			String password = null;
			String url = null;
			String system = null;
			String useSSL ="false";
			if (analyzer instanceof DBTableSelector) {
				DBTableSelectorConfig conf = (DBTableSelectorConfig) dbSource
						.getAnalyticConfig();
				userName = conf.getUserName();
				password = conf.getPassword();
				url = conf.getUrl();
				system = conf.getSystem();
				useSSL = conf.getUseSSL();
			}
 
			else if(analyzer instanceof SQLAnalyzer){
				SQLAnalysisConfig conf=(SQLAnalysisConfig)dbSource
				.getAnalyticConfig();
				
				if(!StringUtil.isEmpty(conf.getDbConnectionName())){
 
					userName = conf.getUserName();
					password = conf.getPassword();
					url = conf.getUrl();
					system = conf.getSystem();
					useSSL = conf.getUseSSL();
				}else{
					userName = dbSource.getDataBaseInfo().getUserName();
					password = dbSource.getDataBaseInfo().getPassword();
					url = dbSource.getDataBaseInfo().getUrl();
					system = dbSource.getDataBaseInfo().getSystem();
					useSSL = dbSource.getDataBaseInfo().getUseSSL();
				}
			}else {
				userName = dbSource.getDataBaseInfo().getUserName();
				password = dbSource.getDataBaseInfo().getPassword();
				url = dbSource.getDataBaseInfo().getUrl();
				system = dbSource.getDataBaseInfo().getSystem();
				useSSL = dbSource.getDataBaseInfo().getUseSSL();
			}

			String key = userName + "-" + url;
			if (connMap.get(key) == null) {

				try {
					Connection connection = AlpineUtil.createConnection(
							userName, password, url, system,AlpineThreadLocal.getLocale(),useSSL);
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AnalyticRunner:  create connection:"
									+ connection.toString());
					}
					connMap.put(key, connection);

				} catch (Exception e) {
					
					itsLogger.error(e);
					throw new AnalysisError(analyzer,
							AnalysisErrorName.Database_connection_error,AlpineThreadLocal.getLocale(), url);
				}

			}
			Connection connection = connMap.get(key);
			if (analyzer instanceof DBTableSelector
					||analyzer instanceof ARIMARPredictor) {
			} else if(analyzer instanceof SQLAnalyzer){
				SQLAnalysisConfig conf=(SQLAnalysisConfig)dbSource
				.getAnalyticConfig();
				if(!StringUtil.isEmpty(conf.getDbConnectionName())){
				}else{
					if (system.equalsIgnoreCase(DataSourceInfoPostgres.dBType)|| system.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
						if(AlpineUtil.isGreenplum(connection)){
							system = DataSourceInfoGreenplum.dBType;
						}else{
							system = DataSourceInfoPostgres.dBType;
						}
					}
					dbSource.getDataBaseInfo().setSystem(system);
				}
			}else {
				if (system.equalsIgnoreCase(DataSourceInfoPostgres.dBType) || system.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
					if(AlpineUtil.isGreenplum(connection)){
						system = DataSourceInfoGreenplum.dBType;
					}else{
						system = DataSourceInfoPostgres.dBType;
					}
				}
				dbSource.getDataBaseInfo().setSystem(system);
			}

			dbSource.setConenction(connection);
		}else{
			if(analyzer instanceof CopyToDBAnalyzer)  {
				CopyToDBConfig config = (CopyToDBConfig)source.getAnalyticConfig();
				
				String userName = config.getUserName();
				String url = config.getUrl();
				
				String key = userName  + "-" + url ;
				if (connMap.get(key) == null) {	
					String password = XmlDocManager.decryptedPassword(config.getPassword());
					String system = config.getSystem();
					String useSSL = config.getUseSSL();
					Connection connection;
					try {
						connection = AlpineUtil.createConnection(
								userName, password, url, system,AlpineThreadLocal.getLocale(),useSSL);
						if(itsLogger.isDebugEnabled()){
							itsLogger.debug("AnalyticRunner:  create connection:"
										+ connection.toString());
										
						}
						connMap.put(key, connection);	
						((CopyToDBAnalyzer)analyzer).setConnection(connection);
					} catch (SQLException e) {
						itsLogger.error(e);
						throw new AnalysisError(analyzer,
								AnalysisErrorName.Database_connection_error,AlpineThreadLocal.getLocale(), url);
					}
				}else{
					((CopyToDBAnalyzer)analyzer).setConnection(connMap.get(key));
				}
			}
		}
	}

	/**
	 * @param analyticNode
	 * @param listeners
	 */
	protected void notifyNodeStart(AnalyticNode analyticNode,
			List<AnalyticProcessListener> listeners) {
		// keep the order...
		if (listeners != null) {

			for (AnalyticProcessListener listener : listeners) {
				
//				if(StringUtil.isEmpty(analyticNode.getGroupID())){
					listener.startAnalyzerNode(analyticNode.getName());
//				}else{//for subflow
//					listener.startAnalyzerNode(analyticNode.getGroupID()+"_"+analyticNode.getName());
//				}
			}
		}
	}

	/**
	 * @param analyticNode
	 * @param startTime
	 * @param outPut
	 */
	private void enhanceMetaInfo(AnalyticNode analyticNode, Date startTime,
			AnalyticOutPut outPut) {
		AnalyticNodeMetaInfo nodeMetaInfo=outPut.getAnalyticNodeMetaInfo();
		if(nodeMetaInfo==null){
			nodeMetaInfo= new AnalyticNodeMetaInfo();
		}
		if(nodeMetaInfo!=null){
			nodeMetaInfo.setName(analyticNode.getName());			
			nodeMetaInfo.setStartTime(startTime);
			nodeMetaInfo.setEndTime(Calendar.getInstance().getTime());
		}
	}

	/**
	 * @param analyzer
	 * @param vOut
	 */
	private void setOutAnlyzer(DataAnalyzer analyzer, VisualizationOutPut vOut) {
		if(vOut!=null){
			vOut.setAnalyzer(analyzer) ;
			if(vOut instanceof CompositeVisualizationOutPut){
				List<VisualizationOutPut> outs = ((CompositeVisualizationOutPut)vOut).getChildOutPuts();
				for (VisualizationOutPut visualizationOutPut :outs) {
					if(visualizationOutPut != null){
						visualizationOutPut.setAnalyzer(analyzer);
					}
				}
			} 
	
		}
	}

	private void addOutPutIntoResult( 
			AnalyticResultImpl result) {
		
		List<AnalyticOutPut> outPutList = new ArrayList<AnalyticOutPut>();

		for (DataAnalyzer analyzer: analyzerList) {
			outPutList.add(analyzer.getOutPut());
		}

		result.setOutPuts(outPutList);
	}

	/**
	 *
	 */
	public void stopProcess(   ) {
		 
		itsLogger.debug("AnalyticRunner  :stopProcess:" + getProcessID());
		if(stop==true){
			return;
		}
		else{
			stop=true;
		
			if (listeners != null) {
				for (AnalyticProcessListener listener:listeners) {
					listener.stopProcess(getAnalyticResult(getProcessID()));
				}
			}
	 
			releaseResourceConnections( );
			releaseContext();
		}
	}

	/**
	 * @param  
	 */
	protected void releaseResourceConnections( ) {
  
		if(connMap!=null){
			Collection<Connection> conns = connMap.values();
			for (Connection connection :conns) {
				try {
					if(connection!=null&&connection.isClosed()==false){
						connection.close();
						if(itsLogger.isDebugEnabled()){
							itsLogger.debug("AnalyticRunner: stop, release connection:"+ connection.toString());
						}
	 						
					}
				} catch (SQLException e) {
					itsLogger.error(e);
				}
			}
			connMap.clear();
		}
	}
	/**
		 * 
		 */
	public AnalyticProcess getProcess() {
		return process;

	}

	public List<AnalyticProcessListener> getListeners() {
		return listeners;
	}

	/**
	 * @param outputDir
	 */
	public void setOutPutPath(String outputDir) {
		itsLogger.info("outputDir="+outputDir);
		this.outputDir=outputDir;
		
	}
	
	String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return
	 */
	public List<DataAnalyzer> getAnalyzers() {
		return analyzerList;
	}


}
