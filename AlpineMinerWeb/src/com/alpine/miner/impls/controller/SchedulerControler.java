/**
 * 
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence;
import com.alpine.miner.impls.taskmanager.TaskManager;
import com.alpine.miner.impls.taskmanager.TaskManagerStore;
import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.taskmanager.impl.FlowTaskRunerImpl;
import com.alpine.miner.impls.taskmanager.impl.scheduler.SchedulerTask;
import com.alpine.miner.impls.taskmanager.impl.scheduler.SchedulerTrigger;
import com.alpine.miner.impls.taskmanager.rule.*;
import com.alpine.utility.file.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Gary
 *
 */
@Controller
@RequestMapping("/main/schedulerManage.do")
public class SchedulerControler extends AbstractControler {

	private TaskManager service = TaskManagerStore.SCHEDULER.getInstance();
	/**
	 * @throws Exception
	 */
	public SchedulerControler() throws Exception {
		super();
	}

	@RequestMapping(params = "method=loadTaskList", method = RequestMethod.GET)
	public void loadTaskList(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String userName = getUserName(request);
		
		List<TaskInfo> taskList = new ArrayList<TaskInfo>();
		List<TaskTrigger> triggerList = service.loadTriggerByGroup(userName);
		for(TaskTrigger trigger : triggerList){
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.taskName = trigger.getTask().getName();
			TriggerItem triggerItem = new TriggerItem(trigger);
			taskInfo.trigger = triggerItem;
			taskList.add(taskInfo);
		}
		ProtocolUtil.sendResponse(response, taskList);
	}

	@RequestMapping(params = "method=saveTasks", method = RequestMethod.POST)
	public void saveTrigger(HttpServletRequest request,
			HttpServletResponse response,
			ModelMap model) throws IOException{
		try{
			String userName = getUserName(request);
			TaskInfo[] taskInfoArray = ProtocolUtil.getRequest(request, TaskInfo[].class);
			List<TaskTrigger> allTriggers = null;
			Set<String> triggerNames = new HashSet<String>();
			//add or update tasks.
			for(TaskInfo task : taskInfoArray){
				TaskTrigger trigger = adaptTrigger(task.getTrigger(), userName);
				triggerNames.add(trigger.getName());
				service.appendTrigger(trigger);
			}
			allTriggers = service.loadTriggerByGroup(userName);
			//delete tasks
			for(TaskTrigger trigger: allTriggers){
				if(!triggerNames.contains(trigger.getName())){
					service.removeTrigger(trigger);
				}
			}
			returnSuccess(response) ;
		}catch(Exception e){
			generateErrorDTO(response, e, request.getLocale());	
		}
	}
	
	private TaskTrigger adaptTrigger(TriggerItem arg,String group){
		Map<String,String> params = new HashMap<String,String>();
		params.put(FlowTaskRunerImpl.FLOW_NAME, arg.getFlowPurelyName());
		params.put(FlowTaskRunerImpl.TRIGGER_NAME, arg.getName());
		params.put(FlowTaskRunerImpl.USER_NAME, group);
		params.put(FlowTaskRunerImpl.FLOW_VERSION, arg.getFlowVersion());
		StringBuilder flowFileNames = new StringBuilder();
		for(String flowFileName : arg.getFlowName().split(FlowTaskRunerImpl.SEPARTOR)){
			try {
				flowFileName = IFlowCategoryPersistence.INSTANCE.getFlowAbsolutelyPath(flowFileName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			flowFileNames.append(flowFileName).append(FlowTaskRunerImpl.SEPARTOR);
		}
		params.put(FlowTaskRunerImpl.FLOW_FILE_NAME, flowFileNames.toString());
		params.put(FlowTaskRunerImpl.FLOW_FULL_NAME, arg.getFlowName());
		SchedulerTask task = new SchedulerTask(group,arg.getTaskName(),new FlowTaskRunerImpl(),params);
		SchedulerTrigger t = new SchedulerTrigger(group,arg.getName(),task,buildRule(arg), arg.isValid());
		return t;
	}
	
	private TriggerRule buildRule(TriggerItem arg){
		TriggerRule rule;
		String type = arg.getType();
		Date startDate = arg.getStartDate();
		
		if(TriggerItem.INTERVAL_TYPE.equals(type)){
			int repeatTimes;
			long interval;
			repeatTimes = StringUtil.isEmpty(arg.getRepeatTimes()) ? 0 : Integer.valueOf(arg.getRepeatTimes());
			interval = StringUtil.isEmpty(arg.getInterval()) ? 0 : Long.valueOf(arg.getInterval());
			rule = new IntervalRule(startDate, repeatTimes, interval, RepeatType.valueOf(arg.getRepeatType()));
		}else{
			TimeExpression express = new TimeExpression();
			express.setFrequence(arg.getFrequence());
			express.setHour(convertTimeExpression(arg.getHour()));
			if(TriggerItem.SCHEDULE_FREQUENCE_MONTH.equals(arg.getFrequence())){
				express.setDayOfMonth(convertTimeExpression(arg.getDayOfMonth()));
			}else if(TriggerItem.SCHEDULE_FREQUENCE_WEEK.equals(arg.getFrequence())){
				express.setDayOfWeek(convertTimeExpression(arg.getDayOfWeek()));
			}else if(TriggerItem.SCHEDULE_FREQUENCE_DATE.equals(arg.getFrequence())){
				express.setMonth(convertTimeExpression(arg.getMonth()));
				express.setDayOfMonth(convertTimeExpression(arg.getDateOfMonth()));
			}
			
			rule = new ScheduleRule(startDate, express);
		}
		if(TriggerItem.TERMINAL_TYPE_DATE.equals(arg.terminalType) && arg.getEndDate() != null){
			Date endingDate = arg.getEndDate();
			Calendar endTime = Calendar.getInstance();
			endTime.clear();
			endTime.setTime(endingDate);
			endTime.set(Calendar.HOUR_OF_DAY, 23);
			endTime.set(Calendar.MINUTE, 59);
			endTime.set(Calendar.SECOND, 59);
			rule.setEndingTime(endTime.getTime());
		}
		return rule;
	}
	
	private String convertTimeExpression(String arg){
		if(arg == null || "".equals(arg)){
			return TimeExpression.NONE;
		}else{
			return arg;
		}
	}
	
	public static class TaskInfo{
		private String taskName;
		private TriggerItem trigger;
		public TriggerItem getTrigger() {
			return trigger;
		}
		public String getTaskName() {
			return taskName;
		}
	}
	
	public static class TriggerItem{
		
		public static final String 	INTERVAL_TYPE = "interval",
									SCHEDULE_TYPE = "schedule";
		
		public static final String 	SCHEDULE_FREQUENCE_DAILY = "cron-day",
									SCHEDULE_FREQUENCE_WEEK = "cron-week",
									SCHEDULE_FREQUENCE_MONTH = "cron-month",
									SCHEDULE_FREQUENCE_DATE = "cron-date";
		
		public static final String 	TERMINAL_TYPE_NEVER = "never",
									TERMINAL_TYPE_TIMES = "times",
									TERMINAL_TYPE_DATE = "date";
		
		private String 	taskName,
						flowName,//include category and flow name. used to generate scheduler run flow file.
						flowPurelyName,//just include flow name. passed to flow runner engine to generate Model, flow run result.
						flowVersion;
		private boolean isValid;
		
		private String 	name;
		private String 	type,//interval or schedule
					   	terminalType,//never, times, date
		
						
		//for interval
						repeatTimes,
						interval,
						repeatType,
		
		//for schedule
						frequence,//day,week month,year...
						
						hour,
						dayOfMonth,
						dayOfWeek,
						month,//Deprecated
						dateOfMonth;
		
		private Date 	startDate,
						endDate;
		
		public TriggerItem(){}
		
		public TriggerItem(TaskTrigger proxy){
			this.taskName = proxy.getTask().getName();
			this.flowName = (String) proxy.getTask().getParams().get(FlowTaskRunerImpl.FLOW_FULL_NAME);
			this.flowPurelyName = (String) proxy.getTask().getParams().get(FlowTaskRunerImpl.FLOW_NAME);
			this.flowVersion = proxy.getTask().getParams().get(FlowTaskRunerImpl.FLOW_VERSION);
			this.name = proxy.getName();
			this.startDate = proxy.getRule().getBeginningTime();
			this.endDate = proxy.getRule().getEndingTime();
			this.isValid = proxy.isValid();
			if(proxy.getRule().getEndingTime() != null){
				this.terminalType = TriggerItem.TERMINAL_TYPE_DATE;
			}else{
				this.terminalType = TriggerItem.TERMINAL_TYPE_NEVER;
			}
			if(proxy.getRule() instanceof IntervalRule){
				this.type = TriggerItem.INTERVAL_TYPE;
				IntervalRule rule = (IntervalRule) proxy.getRule();
				this.repeatTimes = rule.getRepeatCount() == 0 ? "" : String.valueOf(rule.getRepeatCount());
				this.interval = rule.getInterval() == -1 ? "" : String.valueOf(rule.getInterval());
				this.repeatType = rule.getRepeatType().name();
				if(rule.getRepeatCount() != 0 && proxy.getRule().getEndingTime() == null){
					this.terminalType = TriggerItem.TERMINAL_TYPE_TIMES;
				}
			}else{
				this.type = TriggerItem.SCHEDULE_TYPE;
				ScheduleRule rule = (ScheduleRule) proxy.getRule();
				this.hour = rule.getExecuteTime().getHour();
				this.frequence = rule.getExecuteTime().getFrequence();

				if(TriggerItem.SCHEDULE_FREQUENCE_MONTH.equals(rule.getExecuteTime().getFrequence())){
					this.dayOfMonth = rule.getExecuteTime().getDayOfMonth();
				}else if(TriggerItem.SCHEDULE_FREQUENCE_WEEK.equals(rule.getExecuteTime().getFrequence())){
					this.dayOfWeek = rule.getExecuteTime().getDayOfWeek();
				}else if(TriggerItem.SCHEDULE_FREQUENCE_DATE.equals(rule.getExecuteTime().getFrequence())){
					this.month = rule.getExecuteTime().getMonth();
					this.dateOfMonth = rule.getExecuteTime().getDayOfMonth();
				}
			}
		}

		public String getTaskName() {
			return taskName;
		}

		public String getFlowName() {
			return flowName;
		}

		public String getFlowPurelyName() {
			return flowPurelyName;
		}

		public String getFlowVersion() {
			return flowVersion;
		}

		public boolean isValid() {
			return isValid;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public String getRepeatTimes() {
			return repeatTimes;
		}

		public String getInterval() {
			return interval;
		}

		public String getRepeatType() {
			return repeatType;
		}

		public String getFrequence() {
			return frequence;
		}

		public String getHour() {
			return hour;
		}

		public String getDayOfMonth() {
			return dayOfMonth;
		}

		public String getDayOfWeek() {
			return dayOfWeek;
		}

		public String getMonth() {
			return month;
		}

		public String getDateOfMonth() {
			return dateOfMonth;
		}

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public String getTerminalType() {
			return terminalType;
		}
						
	}
}
