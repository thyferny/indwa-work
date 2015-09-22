/**
 * ClassName:SchedulerAnalyticProcessListener
 * 
 * Author gary
 * 
 * Version Ver 1.0
 * 
 * Date 2011-3-29
 * 
 * COPYRIGHT 2010-2011 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.miner.impls.taskmanager;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.AnalyticResult;
import com.alpine.datamining.workflow.AnalyticEngine;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.controller.FlowController;
import com.alpine.miner.impls.mail.MailInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.taskmanager.executor.scheduler.quartz.JobExecuteListener;
import com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import com.alpine.miner.interfaces.MailSender;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.utility.log.LogPoster;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class SchedulerAnalyticProcessListener extends AbstractAnalyticProcessListener{
    private static Logger itsLogger = Logger.getLogger(SchedulerAnalyticProcessListener.class);

    private static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private String mailAddress;
	
	private String triggerName;
	
	private Date flowBeginningTime;
	
	private Queue<String> flowNameQ,flowFileVersionQ,flowFileNameQ,flowFullNameQ;

	 

	public SchedulerAnalyticProcessListener(String runUser, String flownName,String triggerName) {
		super(runUser, flownName, Locale.getDefault()) ;
		mailAddress = ProviderFactory.getAuthenticator(runUser).getUserInfoByName(runUser).getEmail();
		super.setUUID(String.valueOf(new Date().getTime()));
		this.triggerName = triggerName;
	}
 
	@Override
	public void processError(String errMessage) {
		if(isStop()){
			return;
		}
		showLog(null,Resources.PROCESS_ERROR,errMessage,null,VisualNLS.getMessage(VisualNLS.PROCESS_ERROR, locale));
		runNextFlow();
	}

	@Override
	public void startAnalyzerNode(String nodeName) {
		if(flowBeginningTime == null){
			flowBeginningTime = new Date();
		}
		super.startAnalyzerNode(nodeName);
	}

	@Override
	public void analyticFlowFinished(AnalyticResult result) {
		super.analyticFlowFinished(result);
		try {
			MailSender.instance.send(
					MailInfoCreator.buildMailInfo(mailAddress, getFlowName(), triggerName, flowBeginningTime, new Date(), super.resultLogList));
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
		}
		runNextFlow();
	}

	@Override
	public void stopProcess(AnalyticResult result) {
		super.stopProcess(result);
		try {
			MailSender.instance.send(
					MailInfoCreator.buildMailInfo(mailAddress, getFlowName(), triggerName, flowBeginningTime, new Date(), super.resultLogList));
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
		}
		runNextFlow();
	}
	
	private void runNextFlow(){
		if(this.flowFileNameQ == null || this.flowFileVersionQ == null || this.flowNameQ == null){
			return;
		}
		String flowFileVersion = flowFileVersionQ.poll();
		String flowName = flowNameQ.poll();
		String flowFileName = flowFileNameQ.poll();
		String flowFullName = flowFullNameQ.poll();
		if(flowFileVersion == null || flowName == null || flowFileName == null){
			JobExecuteListener.completeTask(this.getRunUser(), this.triggerName);
			return;
		}
		SchedulerAnalyticProcessListener listener = new SchedulerAnalyticProcessListener(this.getRunUser(), flowName,triggerName);
		listener.setFlowFileNameQ(flowFileNameQ);
		listener.setFlowFileVersionQ(flowFileVersionQ);
		listener.setFlowNameQ(flowNameQ);
		listener.setFlowFullNameQ(flowFullNameQ);
		
		listener.setFlowFileVersion(flowFileVersion);
		listener.setFlowFullName(flowFullName);
		AnalyticProcessListener[] listenerArray = {listener};
//		String filePath = FlowFileStore.getFlowPath(flowFileName);
		String tempDir=WebWorkFlowRunner.RUNTIME_DIR + File.separator + this.getRunUser() + File.separator + UUID.randomUUID();
		String tmpFilePath=tempDir+File.separator+UUID.randomUUID().toString()+Resources.AFM;
		
		try {
			boolean addSuffixToOutput=Boolean.parseBoolean(ProfileReader.getInstance().getParameter(ProfileUtility.UI_ADD_PREFIX));
			itsLogger.info( "Copying: " + flowFileName);
			WebWorkFlowRunner.copyFile(flowFileName, ResourceType.Personal, this.getRunUser(), tmpFilePath,Locale.getDefault(),addSuffixToOutput) ;

			   AnalyticEngine.instance.runAnalysisProcessFile( 
					 tmpFilePath, Arrays.asList(listenerArray),false,locale,this.getRunUser());
		} catch (Throwable e) {
			itsLogger.error("The " + flowName + " of " + this.getRunUser() + " has a error when startup. Detail is: " + e.getMessage());
		}
	}
	
	@Override 
	/**
	 * override to build full date for log, in order to log the full time of execute for operator.
	 */
	protected String[] createLogItem(String nodeName, String message,
			String errMessage, String output,String logmessage) {
		String[] item = new String[FlowController.RESULT_COLUMN_NUMBER];
		item[0]=nodeName;
		item[1]=message; 
		Date date = new Date();    
      
		item[2]=DATE_FORMATTER.format(date);
		item[3]=getId();
		
		item[4]=errMessage;
		item[5]=output;
		item[6]=logmessage;
		if(errMessage != null){
			itsLogger.info("errMessage:"+errMessage);
			
		}
		return item;
	}
	
	
	@Override
	public void processError(Throwable error) {
		if(isStop()){
			return;
		}
		error.printStackTrace();
		String msg=error.getMessage();
		if(error instanceof AnalysisException){
			msg=((AnalysisException)error).getFullMessage();
		}
		showLog(null,Resources.PROCESS_ERROR,msg,null,VisualNLS.getMessage(VisualNLS.PROCESS_ERROR, locale));
	}
	
	private static class MailInfoCreator{
		private static ResourceBundle RESOURCE;
		private static final MessageFormat MESSAGE_FORMATTER;
		private static String title;
		static{
			RESOURCE = ResourceBundle.getBundle("app");
			title = RESOURCE.getString("scheduler_mail_flow_title");
			String pattern = RESOURCE.getString("scheduler_mail_flow_execute_result");
			MESSAGE_FORMATTER = new MessageFormat(pattern);
		}
		
		public static MailInfo buildMailInfo(String toMail,String flowName,String triggerName,Date beginningTime,Date StopTime,List<LogInfo> logs){
			MailInfo info = new MailInfo();
			info.setContent(buildContent(MESSAGE_FORMATTER.format(
					new String[]{flowName,
							DATE_FORMATTER.format(beginningTime),
							DATE_FORMATTER.format(StopTime),
							triggerName}),logs));
			info.setSubject(title);
			info.addReceiver(toMail);
			return info;
		}

		
		private static String buildContent(String initializedStr, List<LogInfo> logs){
			StringBuilder sb = new StringBuilder(initializedStr);
			sb.append("\r\n");
			for(LogInfo log : logs){
				if(Resources.PROCESS_ERROR.equals(log.getMessage())){
					sb.append(log.getErrMessage());
				}else if(Resources.PROCESS_FINISHED.equals(log.getMessage()) || Resources.PROCESS_STOP.equals(log.getMessage())){
					sb.append(log.getLogmessage());
				}else{
					sb.append("[").append(log.getDateTime()).append("] ")
						.append(log.getNodeName());
					if(Resources.OPERATOR_IS_START.equals(log.getMessage())){
						sb.append(" ").append(log.getLogmessage());
					}else if(Resources.OPERATOR_FINISHED.equals(log.getMessage())){
						sb.append(" ").append(log.getLogmessage());
					}
				}
				sb.append("\r\n");
			}
			return sb.toString();
		}
	}

	public void setFlowNameQ(Queue<String> flowNameQ) {
		this.flowNameQ = flowNameQ;
	}

	public void setFlowFileVersionQ(Queue<String> flowFileVersionQ) {
		this.flowFileVersionQ = flowFileVersionQ;
	}

	public void setFlowFileNameQ(Queue<String> flowFileNameQ) {
		this.flowFileNameQ = flowFileNameQ;
	}

	public void setFlowFullNameQ(Queue<String> flowFullNameQ) {
		this.flowFullNameQ = flowFullNameQ;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener#saveModelToEditFlow()
	 */
	@Override
	protected void saveModelToEditFlow() throws Exception {
		//Not need to save model info to editing flow file, if Executed by schedule
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener#returnExecuteType()
	 */
	@Override
	protected String returnExecuteType() {
		return LogPoster.WORKFLOW_RUN_SCHEDULE;
	}


}
