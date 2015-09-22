/**
 * ClassName  CommandLineProcessListener.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-31
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticResult;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.SVDLanczosAnalyzerOutPutTrainModel;
import com.alpine.datamining.operator.svd.SVDModel;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.log.LogEvent;
import com.alpine.utility.log.LogPoster;

import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author John Zhao
 *
 */
public class CommandLineProcessListener implements AnalyticProcessListener {
	 Locale locale= Locale.getDefault();
	private DateFormat dataFormat=new SimpleDateFormat("HH:mm:ss");
    private static final Logger itsLogger =Logger.getLogger(CommandLineProcessListener.class);
    private long startTime = 0;
    private long startOperatorTime = 0;

    /* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#analyticFlowFinished(com.alpine.datamining.api.AnalyticResult)
	 */
	@Override
	public void analyticFlowFinished(AnalyticResult result) {
		String processID=result.getProcessID();
		
		//zip the result in the current directory
		System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "
				+WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Analytic_Flow_Finished,locale));
//        System.exit(0) ;//For JRuby no exit

		long numSec = Math.round((System.currentTimeMillis() - this.startTime) / 1000);
   	 	LogEvent event = LogPoster.getInstance().createEvent(LogPoster.WORKFLOW_RUN_COMMAND, String.valueOf(numSec), System.getProperty("user.name"));
		LogPoster.getInstance().sendEvent(event);
		LogPoster.getInstance().close();
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#finishAnalyzerNode(java.lang.String, com.alpine.datamining.api.AnalyticOutPut)
	 */
	@Override
	public void finishAnalyzerNode(String nodeName, AnalyticOutPut outPut) {
		//save model if necessat
		if(outPut instanceof AnalyzerOutPutTrainModel){
			//avoid the duplicate
			if(outPut.getDataAnalyzer() instanceof EngineModelWrapperAnalyzer ==false){
				//wheather is forceretrain...
				 
					try {
						saveModel((AnalyzerOutPutTrainModel)outPut);
					} catch (IOException e) {
						e.printStackTrace();
					}	
			}
			
		}
		String message = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Analytic_Node_Finished,locale)+nodeName;
		if(outPut.getExtraLogMessage()!=null) {
			message = message + " (" +outPut.getExtraLogMessage()+")" ;
		}
		System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+message);
		long numSec = Math.round((System.currentTimeMillis() - this.startOperatorTime) / 1000);
		LogEvent e = LogPoster.getInstance().createEvent(LogPoster.Operator_Execute, outPut.getAnalyticNode().getAnalyzerClass(), System.getProperty("user.name"));
		e.addExtra(LogPoster.Operator_Type, outPut.getAnalyticNode().getAnalyzerClass());
		e.addExtra(LogPoster.Operator_Exec_Time, Long.toString(numSec));
		LogPoster.getInstance().sendEvent(e);
	}

	private void saveModel(AnalyzerOutPutTrainModel outPut) throws IOException {
		EngineModel engineModel = ((AnalyzerOutPutTrainModel)outPut).getEngineModel(); 
		
	 
		String modelName = outPut.getAnalyticNode().getName(); 
		//here modelName is the algorithom node's name
		 
		List<AnalyticNode> parentList = outPut.getAnalyticNode().getParentNodes();
		//won't save if model is replaced.
		if(parentList!=null&&parentList.size()>0
				&&(engineModel.getModel() instanceof SVDLanczosAnalyzerOutPutTrainModel ==false)
				&&(engineModel.getModel() instanceof SVDModel ==false)){
			File modelFile = new File(AlpineAnalyticEngine.modelSaveDir+File.separator+modelName+".am");
			// System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] create file:" +modelFile);
			if(modelFile.exists()){
				modelFile.delete();
			}
			modelFile = new File(AlpineAnalyticEngine.modelSaveDir+File.separator+modelName+".am");
			modelFile.createNewFile();
			FileOutputStream outStream = new FileOutputStream(modelFile);
//			String modelStr = AlpineUtil.objectToString(engineModel);
			XmlDocManager xmlDocManager = new XmlDocManager();
			String saveStream = xmlDocManager.xmlToLocalString(createModelDocument(modelName, engineModel,System.getenv("user.name"),"","3.0"));
			
			byte[] bytes = saveStream.getBytes();
			outStream.write(bytes);
			outStream.close();
			System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] " +
			WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Model_Saved,locale)+modelFile.getAbsolutePath());

		}
		
	}

	private Node createModelDocument(String modelName, EngineModel engineModel,
			String userName, String Description, String Version) {
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
		root = xmlDoc.createElement("Process");
		root.setAttribute("UserName",userName);
		root.setAttribute("Description",Description);
		root.setAttribute("Version",Version);
		
		xmlDoc.appendChild(root);
		
		Element operator = xmlDoc.createElement("Operator");
		String name = modelName;
		operator.setAttribute("name", name);
		operator.setAttribute("uuid", System.currentTimeMillis()+"");
		operator.setAttribute("type", ModelOperator.class.getName());
//		Rectangle rect = om.getRect();
//		int x = rect.x;
		operator.setAttribute("X", "" + 50);
//		int y = rect.y;
		operator.setAttribute("Y", "" + 50);
		
		Element modelElement = xmlDoc.createElement("Model");
		modelElement.setTextContent(AlpineUtil.objectToString(engineModel));
		operator.appendChild(modelElement);
		root.appendChild(operator);
		return xmlDoc;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#processError(java.lang.String)
	 */
	@Override
	public void processError(String errMessage) {
		 System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+
				 WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Analytic_Error_Hanppens,locale)+errMessage);

	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#putMessage(java.lang.String)
	 */
	@Override
	public void putMessage(String message,String nodeName) {
		 System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+
				 WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Analytic_Message,locale)+message);

	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#startAnalyzerNode(java.lang.String)
	 */
	@Override
	public void startAnalyzerNode(String nodeName) {
		 System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+
				 WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Analytic_Node_Started,locale)+nodeName);

		if(startTime==0){
			startTime=System.currentTimeMillis();
		}
		startOperatorTime = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#stopProcess()
	 */
	@Override
	public void stopProcess( AnalyticResult result) {
		 System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+
				 WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticFLow_Stoped,locale));

	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcessListener#processError(java.lang.Exception)
	 */
	@Override
	public void processError(Throwable error) {
		System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Analytic_Error_Hanppens,locale)+error.getMessage());
		error.printStackTrace();
		System.out.println("["+dataFormat.format(Calendar.getInstance().getTime())+"] "+WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticFLow_Stoped,locale));
		
	}

	public String getFilePath() {
		return null;
	}

	public void setFilePath(String filePath) {
	}

	@Override
	public Locale getLocale() {
		return locale;
		
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
		
	}

	@Override
	public String getFlowRunUUID() {
		//nothing to do for command line
		return null;
	}

}
