package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONArray;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticResult;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.SVDLanczosAnalyzerOutPutTrainModel;
import com.alpine.datamining.operator.svd.SVDModel;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.controller.FlowController;
import com.alpine.miner.impls.report.FlowResultGenerator;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.result.OutPutVisualAdapter;
import com.alpine.miner.impls.result.OutPutVisualAdapterFactory;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.taskmanager.SchedulerAnalyticProcessListener;
import com.alpine.miner.interfaces.AnalysisModelManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.utils.JSONUtility;
import com.alpine.miner.utils.ModelUtility;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelEmpty;
import com.alpine.util.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.log.LogPoster;

import org.apache.log4j.Logger;
/**
 * ClassName:WebRunAnalyticProcessListener
 * 
 * Author kemp zhang,john zhao
 * 
 * Version Ver 1.0
 * 
 * Date 2011-3-29
 * 
 * COPYRIGHT 2010-2011 Alpine Solutions. All Rights Reserved.
 */
public abstract class AbstractAnalyticProcessListener implements AnalyticProcessListener {
    private static Logger logger = Logger.getLogger(AbstractAnalyticProcessListener.class);

    private String runUser;
	//this is the flow file version like cvs
	private String flow_file_Version;
	 
	private boolean stop =false;
 
	private String flowRunUUID = null;
 
	public String getFlowRunUUID() {
		return flowRunUUID;
	}

	private String flowName;
	private String tempFileName;
	private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
 
	protected   List<LogInfo> resultLogList= new ArrayList<LogInfo>();
	protected   List<String> resultJSONList= new ArrayList<String>();
	Persistence persistence =   FilePersistence.INSTANCE;
	//all time should be time mills...
	private long startTime = 0; 

	private long endTime = 0;
	protected Locale locale = null;
	private boolean hasModel =false;  
	
	private String flowFullName;//be saved at flow result.

	public AbstractAnalyticProcessListener(String runUser,String flownName, Locale locale){
		this.runUser = runUser;
		this.flowName = flownName;
		this.locale=locale;
 
	}
  
	@Override
	public void startAnalyzerNode(String nodeName) {
		if(stop){
			return;
		}
		
		showLog(nodeName,Resources.OPERATOR_IS_START,null,null,
				VisualNLS.getMessage(VisualNLS.OPERATOR_IS_START, locale)); 
		//first node...
		if(startTime==0){
			startTime=System.currentTimeMillis();
		}
	}
  
	@Override
	public void analyticFlowFinished(AnalyticResult result) {
		if(stop){
			return;
		}
		
	 	String[][] props = FlowResultGenerator.toFlowMetaInfoProperties(result.getAnalyticMetaInfo(),this.runUser,locale);
		String flowMetaInfo = JSONArray.fromObject(props).toString(); 
		showLog(null, Resources.PROCESS_FINISHED,null,flowMetaInfo,VisualNLS.getMessage(VisualNLS.PROCESS_FINISHED, locale));
		try {
			saveRunningResult(flowMetaInfo);
			sendLogger();
		} catch (IOException e) {
			// can not save result, just in the log is OK
			e.printStackTrace();
			logger.error(e.getMessage(), e) ;
		}
	 	 
		if(hasModel==true){
			try {
				saveModelToEditFlow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		clearTempFile(getFilePath());
	}

	protected abstract void saveModelToEditFlow() throws Exception;
 

	@Override
	public void stopProcess(AnalyticResult result) {
		if(stop){
			return;
		}
		this.stop =true;
 
		String flowMetaInfo=""  ;
		if(result!=null){
		 	String[][] props = FlowResultGenerator.toFlowMetaInfoProperties(result.getAnalyticMetaInfo(),this.runUser,locale);
			  flowMetaInfo = JSONArray.fromObject(props).toString();
		} 
		showLog(null, Resources.PROCESS_STOP,null,flowMetaInfo,VisualNLS.getMessage(VisualNLS.PROCESS_STOP, locale));

		//the stopped flow result also need saved (like errro happen)
		try {
			if(StringUtil.isEmpty(flowMetaInfo)==false){
				saveRunningResult(flowMetaInfo);
			}
		} catch (IOException e) {
			// can not save result, just in the log is OK
			e.printStackTrace();
			logger.error(e.getMessage(), e) ;
		}
	}

	@Override
	public void processError(String errMessage) {
		if(stop){	
			return;
		}
		showLog(null,Resources.PROCESS_ERROR,errMessage,null,VisualNLS.getMessage(VisualNLS.PROCESS_ERROR, locale));
	}

	@Override
	public void processError(Throwable error) {
		if(stop){
			return;
		}
		error.printStackTrace();
		String msg=error.getMessage();
		if(error instanceof AnalysisException){
			msg=((AnalysisException)error).getFullMessage();
		}
		showLog(null,Resources.PROCESS_ERROR,msg,null,VisualNLS.getMessage(VisualNLS.PROCESS_ERROR, locale));
	}

	/**
	 * @param nodeName
	 * @param message
	 * @param errMessage
	 * @param output
	 * @param logMessage
	 */
	protected void showLog(String nodeName, String message,String errMessage,String output,String logMessage) {
		errMessage = formatErrorMsg(errMessage);
		String[] item = createLogItem(nodeName, message, errMessage, output,logMessage);
		if (errMessage != null) {
			logger.info("errMessage:" + errMessage);
		}
		resultLogList.add(new LogInfo(nodeName,message,item[2],getId(),errMessage,logMessage));  
	 
 	}


	@Override
	//without step run support...
	public void finishAnalyzerNode(String arg0, AnalyticOutPut outPut) {
		OutPutVisualAdapterFactory visualFactory=OutPutVisualAdapterFactory.getInstance();
		if(isStop()){
			return;
		}
		String jsonString="";
		try {
			jsonString = generateVisualString(outPut, visualFactory );
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("finishAnalyzerNode error:" + e.getMessage());
			processError(e);
			return;
		}
		 

		resultJSONList.add(jsonString);
		String message = VisualNLS.getMessage(VisualNLS.OPERATOR_FINISHED, locale);
		if(outPut.getExtraLogMessage()!=null){
			message = message+" (" + outPut.getExtraLogMessage() +")" ;
		}
		showLog(arg0,Resources.OPERATOR_FINISHED,null,jsonString,message);
	}

	@Override
	public void putMessage(String message, String nodeName) {
		if(stop){
			return;
		}
		showLog(nodeName,message,null,null,message);
	}
	
	 
	
	public void setUUID(String uuid) {
		this.flowRunUUID = uuid;
	}
	
 



	protected String[] createLogItem(String nodeName, String message,
			String errMessage, String output,String logMessage) {
		String[] item = new String[FlowController.RESULT_COLUMN_NUMBER];
		item[0]=nodeName;
		if(message!=null){
			message = message.replace("\"", "");
		}
		item[1]=message;
		Date date = new Date();    
      
		item[2]=dateFormat.format(date);
		item[3]=flowRunUUID;
		
		item[4]=errMessage;
		if(errMessage!=null){
			item[4] = errMessage.replace("\"", "");
		}
		item[5]=output;
		item[6]= logMessage;
		if(null!=logMessage){
			item[6] = logMessage.replace("\"", "");
		}
		if(errMessage != null){
			logger.info("errMessage:" + errMessage);
			
		}
		return item;
	}
  	
//	protected String toJSONLog(String nodeName, String message, String dateTime,
//			String id, String errMessage) { 
//		 //{nodeName:nodeName,message:message,dateTime:dateTime,uuid:id,errorMessage:errorMessage}
//		StringBuffer sb= new StringBuffer();
//		sb.append("{").append("nodeName:\"").append(nodeName).append("\",") ;
//		sb.append("nodeName:\"").append(nodeName).append("\",") ;
//		sb.append("message:\"").append(message).append("\",") ;
//		sb.append("dateTime:\"").append(dateTime).append("\",") ;
//		sb.append("uuid:\"").append(id).append("\",") ;
//		sb.append("errMessage:\"").append(errMessage).append("\"}") ;
//		return sb.toString();
//	}

	protected String formatErrorMsg(String errMessage) {
		if(errMessage != null)
		{
			errMessage = errMessage.trim();
			errMessage = errMessage.replace("\"", " ");
			errMessage = errMessage.replace("'", " ");
			errMessage = errMessage.replace("\n", " ");
		}
		return errMessage;
	}

	public String getFilePath() {
		return tempFileName;
	}

	public void setFilePath(String filePath) {
		this.tempFileName=filePath;
	}

	protected String generateVisualString(AnalyticOutPut outPut,
			OutPutVisualAdapterFactory visualFactory)
			throws Exception {
	 
		OutPutVisualAdapter adapter = visualFactory.getAdapter(outPut);
		VisualizationModel visualModel = null;
		if(adapter!=null){
              AlpineUtil.VALUE_PASSER.set(this.getRunUser());
			  visualModel = adapter.toVisualModel(outPut,locale);
 
		} else{
			  visualModel= new VisualizationModelEmpty(outPut.getAnalyticNode().getName());
		}	
		visualModel.setAnalyticNodeMetaInfo(outPut.getAnalyticNodeMetaInfo());
		String[][] operatorInputs = FlowResultGenerator.getOperatorInputs(outPut,locale);
		visualModel.setOpeatorInputs(operatorInputs);
		String jsonString=JSONUtility.toJSONString(visualModel,this.locale);
		
		//if is a model, ceate a model
		//only user can run personal path
		if(outPut instanceof AnalyzerOutPutTrainModel){
			//avoid the duplicate
			if(outPut.getDataAnalyzer() instanceof EngineModelWrapperAnalyzer ==false){
				//wheather is forceretrain...
				if(((AnalyzerOutPutTrainModel)outPut).isComeFromRetrain()==true){
					saveModel((AnalyzerOutPutTrainModel)outPut);	
					this.hasModel = true;
				}
			}
			
		}
		return jsonString;
	}



	
//this is for trainer 
	private void saveModel(AnalyzerOutPutTrainModel outPut) throws Exception { 
		Long createTime = System.currentTimeMillis();
		EngineModel engineModel = ((AnalyzerOutPutTrainModel)outPut).getEngineModel(); 
		
		String algorithmName = ModelUtility.getAlorithmModel(engineModel);
		String modelName = outPut.getAnalyticNode().getName(); 
		//here modelName is the algorithom node's name
		ModelInfo modelInfo = new ModelInfo(runUser,ResourceType.Personal, flowRunUUID, modelName, algorithmName, flowName);
 
		modelInfo.setVersion(flow_file_Version);
		modelInfo.setCreateTime(createTime ) ;
		List<AnalyticNode> parentList = outPut.getAnalyticNode().getParentNodes();
		//won't save if model is replaced.
		if(parentList!=null&&parentList.size()>0
				&&(engineModel.getModel() instanceof SVDLanczosAnalyzerOutPutTrainModel ==false)
				&&(engineModel.getModel() instanceof SVDModel ==false)){
			AnalysisModelManager.INSTANCE.saveEngineModel( 
					modelInfo, engineModel);
			
			com.alpine.miner.inter.resources.Resources.retrainHash.put(outPut.getAnalyticNode().getID(), engineModel) ;
			
		}
		
		//force retrain...
//		HashMap<String, String> paramManp = outPut.getDataAnalyzer().getAnalyticSource().getAnalyticConfig().getValueAsMap();
//		String forceRetraine =paramManp.get(OperatorParameter.NAME_forceRetrain);
//		if(forceRetraine!=null&&forceRetraine.equalsIgnoreCase("no")) {
//			
//		}
		
		
	}
	
	protected void saveRunningResult(String flowMetaInfo) throws IOException {
		//meta data who when run which flow and cost times ...
		//{log:[...],output:[...]}
		StringBuffer resultJSON=new StringBuffer();
		
		resultJSON.append("{").append("logs:[") ;	
		
		if(resultLogList!=null&&resultLogList.size()>0){			
			Iterator<LogInfo> it = resultLogList.iterator();
			while(it.hasNext()){
				// For json string has "
				LogInfo logInfo = it.next();
				if(null!=logInfo){
					String errorMsg = logInfo.getErrMessage();
					String logMsg = logInfo.getLogmessage();
					String msg = logInfo.getMessage();
					if(null!=errorMsg){
						logInfo.setErrMessage(errorMsg.replaceAll("\"", ""));
					}
					if(null!=logMsg){
						logInfo.setLogmessage(logMsg.replaceAll("\"", ""));
					}
					if(null!=msg){
						logInfo.setMessage(msg.replaceAll("\"",""));
					}
				}
				resultJSON.append(logInfo) ;
				resultJSON.append(",");
			}
		}		
		resultJSON.append("],outputs:[");
		
		if(resultJSONList!=null&&resultJSONList.size()>0){			
			Iterator<String> it = resultJSONList.iterator();
			while(it.hasNext()){
				resultJSON.append(it.next()) ;
				resultJSON.append(",");
			}
			resultJSONList.clear();
			resultJSONList =null;
		}	
		
		resultJSON.append("]"); 
		//For MINERWEB-825
		resultJSON.append(",flowMetaInfo:");
		resultJSON.append(flowMetaInfo);
		//end add by Will
		resultJSON.append("}");
		endTime=System.currentTimeMillis();
		String runType = FlowResultInfo.RUN_TYPE_MANUAL;
		if(this instanceof SchedulerAnalyticProcessListener){
			runType = FlowResultInfo.RUN_TYPE_SCHEDULER;
		} 
		FlowResultInfo flowResultInfo= new FlowResultInfo(  runUser,ResourceType.Personal, flowRunUUID, startTime,  endTime,  flowName,runType);
		flowResultInfo.setFlowFullName(flowFullName);
		flowResultInfo.setVersion(flow_file_Version);
		ResourceManager.getInstance().saveFlowResultInfo(flowResultInfo, resultJSON.toString()) ;

	}

	protected void clearTempFile(String filePath) {
		if(filePath == null || filePath.trim().equals("")){
			return;
		}
		File dir= new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
		if(dir.exists()==true){
			for(File file : dir.listFiles()){
				file.delete();
			}
			dir.delete();
		}
	}
	
	public String getFlowFileVersion() {
		return flow_file_Version;
	}

	public void setFlowFileVersion(String flow_file_Version) {
		this.flow_file_Version = flow_file_Version;
	}



	public String getRunUser() {
		return runUser;
	}



	protected boolean isStop() {
		return stop;
	}



	protected String getId() {
		return flowRunUUID;
	}

	
	protected static class LogInfo{
		private String nodeName,
						message, 
						dateTime,
						id, 
						errMessage;

		private String logmessage;
		
		public String getLogmessage() {
			return logmessage;
		}

		public void setLogmessage(String logmessage) {
			this.logmessage = logmessage;
		}

		public LogInfo(String nodeName, String message, String dateTime,
				String id, String errMessage,String logmessage){
			this.nodeName = nodeName;
			this.message = message;
			this.dateTime = dateTime;
			this.id = id;
			this.errMessage = errMessage;
			this.logmessage= logmessage;
		}

		public String getNodeName() {
			return nodeName;
		}

		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}

		public String getDateTime() {
			return dateTime;
		}

		public String getId() {
			return id;
		}

		public String getErrMessage() {
			return errMessage;
		}
		
		public void setErrMessage(String errMessage) {
			this.errMessage = errMessage;
		}

		@Override
		public String toString() {
			StringBuffer sb= new StringBuffer();
			sb.append("{").append("nodeName:\"").append(nodeName).append("\",") ;
			sb.append("nodeName:\"").append(nodeName).append("\",") ;
			sb.append("message:\"").append(message).append("\",") ;
			sb.append("dateTime:\"").append(dateTime).append("\",") ;
			sb.append("uuid:\"").append(id).append("\",") ;
			sb.append("errMessage:\"").append(errMessage).append("\",") ;			
			sb.append("logmessage:\"").append(logmessage).append("\"}") ;
			return sb.toString();
		}
	}

	private void sendLogger(){
		long numSec = Math.round((this.endTime - this.startTime) / 1000);
		LogPoster.getInstance().createAndAddEvent(returnExecuteType(), String.valueOf(numSec), this.runUser);
	}
	
	/**
	 * return the name of run flow method.
	 * @return
	 */
	protected abstract String returnExecuteType();

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
		
	}
	protected String getFlowName() {
		return flowName;
	}

	public void setFlowFullName(String flowFullName) {
		this.flowFullName = flowFullName;
	}

	protected String getTempFileName() {
		return tempFileName;
	}

}
