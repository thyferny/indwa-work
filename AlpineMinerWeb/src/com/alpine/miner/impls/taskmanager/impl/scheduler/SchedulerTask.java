/**
 * 
 */
package com.alpine.miner.impls.taskmanager.impl.scheduler;

import java.util.Map;

import com.alpine.miner.impls.taskmanager.Task;
import com.alpine.miner.impls.taskmanager.TaskRuner;

/**
 * @author Gary
 *
 */
public class SchedulerTask implements Task {
	private static final long serialVersionUID = 1621701433871364853L;
	
	private String group,
					name;
	private TaskRuner		runner;
	private Map<String,String> params;
	
	public SchedulerTask(String group,
							String name,
							TaskRuner runner,
							Map<String,String> params){
		this.group = group;
		this.name = name;
		this.runner = runner;
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.Task#getGroup()
	 */
	@Override
	public String getGroup() {
		return group;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.Task#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

 

	 

	public void setTaskRunner(TaskRuner taskJob) {
		this.runner = taskJob;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.Task#run()
	 */
	@Override
	public boolean run() {
		return runner.execute(params); 
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.Task#getParams()
	 */
	@Override
	public Map<String, String> getParams() {
		return params;
	}

}
