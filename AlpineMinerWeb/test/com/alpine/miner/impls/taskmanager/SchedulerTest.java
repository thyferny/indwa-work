/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * SchedulerTest
 * May 16, 2012
 */
package com.alpine.miner.impls.taskmanager;

import java.io.Serializable;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import com.alpine.miner.impls.taskmanager.rule.IntervalRule;
import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public class SchedulerTest extends TestCase implements Serializable {
	private static final long serialVersionUID = 9200013012857669756L;
	private transient TaskManager testInstance = TaskManagerStore.SCHEDULER.getInstance();
	/**
	 * 
	 */
	public SchedulerTest() {
		testInstance.startup();
	}
	
	@Test
	public void testAddTask() throws InterruptedException{
		testInstance.appendTrigger(new TaskTrigger(){
			private static final long serialVersionUID = 6349434733082406054L;
			private boolean valid = true;

			@Override
			public String getGroup() {
				return "Testing group";
			}

			@Override
			public String getName() {
				return "test Unit Test";
			}

			@Override
			public TriggerRule getRule() {
				return new IntervalRule(null, 2, 100);
			}

			@Override
			public Task getTask() {
				return new Task(){
					private static final long serialVersionUID = 1154785430172941814L;

					@Override
					public String getGroup() {
						return "Testing group";
					}

					@Override
					public String getName() {
						return "test task";
					}

					@Override
					public Map<String, String> getParams() {
						return null;
					}

					@Override
					public boolean run() {
						System.out.println("------");
						return true;
					}
					
				};
			}

			@Override
			public boolean isValid() {
				return valid;
			}

			@Override
			public void toInvalid() {
				valid = false;
			}
			
		});
		
		Thread.sleep(10000);
	}
}
