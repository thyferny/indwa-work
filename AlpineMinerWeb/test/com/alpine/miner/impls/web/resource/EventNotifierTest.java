/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * EventNotifierTest.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Nov 9, 2011
 */
package com.alpine.miner.impls.web.resource;

import java.util.List;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author sam_zang
 * 
 */
public class EventNotifierTest{

	static private ResourceManager rmgr;
	EventNotifier.EventType ctype = EventNotifier.EventType.FlowCreate;
	EventNotifier.EventType utype = EventNotifier.EventType.FlowUpdate;
	EventNotifier.EventType dtype = EventNotifier.EventType.FlowDelete;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rmgr = ResourceManager.getInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for
	 */
	@Test
	public void testPublic() {
		List<FlowInfo> list = rmgr.getFlowList("Public");
		testSendEvent(list);	
	}
	
	@Test
	public void testGroup() {
		List<FlowInfo> list = rmgr.getFlowList("Group/Business");
		testSendEvent(list);	
	}
	
	private void testSendEvent(List<FlowInfo> list) {
		
		try {
			Thread.sleep(3000);
			int n = 0;
			for (FlowInfo info : list) {
				switch (n % 3) {
				case 0:
					EventNotifier.sendEvent(info, utype);
					break;
				case 1:
					EventNotifier.sendEvent(info, ctype);
					break;
				default:
					EventNotifier.sendEvent(info, dtype);
					break;
				}
				
				n ++;
				if (n % 3 == 0)
					Thread.sleep(1000);
				
				if (n > 4) break;
			}

			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
