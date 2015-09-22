/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ResourceManagerTest.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.web.resource;

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;

import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.utility.db.DbConnection;

/**
 * @author sam_zang
 * 
 */
public class ResourceManagerTest {

	static private Persistence persistence;
	static private ResourceManager mgr;
	static List<FlowInfo> keptFlows = new LinkedList<FlowInfo>();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		persistence = new FilePersistence();
		mgr = ResourceManager.getInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		ResourceManager tmgr = ResourceManager.getInstance();
		Assert.assertNotNull(tmgr);
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getUserInfo(java.lang.String)}
	 * .
	 */	@Test

	public void testGetUserInfo() {
		UserInfo u = new UserInfo();
		u.setLogin("guest");
		u.setPassword("no password");
		String[] groups = { "G1", "G2", "G3", "New" };
		u.setGroups(groups);

		//persistence.storeUserInfo(u);

//		UserInfo x = mgr.getUserInfo("guest");
//		Assert.assertNotNull(x);
//		String[] xg = mgr.getUserGroups("guest");
//		Assert.assertEquals(4, xg.length);
//
//		xg = mgr.getUserGroups("Unknown");
//		Assert.assertNull(xg);
	}

 

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getFlowList(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetFlowList() {
		List<FlowInfo> list = mgr.getFlowList("Public");
		Assert.assertTrue(list.size() > 0);
		list = mgr.getFlowList("Unknown");
		Assert.assertTrue(list.size() == 0);
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getFlowData(com.alpine.miner.impls.web.resource.FlowInfo)}
	 * .
	 * @throws OperationFailedException 
	 */
	@Test
	public void testGetFlowData() throws OperationFailedException {
		List<FlowInfo> list = mgr.getFlowList("Public");
		Assert.assertTrue(list.size() > 0);
		
		OperatorWorkFlow flow = mgr.getFlowData(list.get(0),Locale.getDefault());
		Assert.assertNotNull(flow);
	}


 

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#updateFlow()}.
	 * @throws OperationFailedException 
	 */
	@Test
	public void testUpdateFlow() throws  Exception {
		List<FlowInfo> list = mgr.getFlowList("Public");
		Assert.assertTrue(list.size() > 0);
		
		OperatorWorkFlow flow = mgr.getFlowData(list.get(0),Locale.getDefault());
		FlowInfo info = keptFlows.get(0);
		try {
			mgr.updateFlow(info, flow);
		} catch (DataOutOfSyncException e) {
			e.printStackTrace();
		}
	}

 

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#deleteFlow()}.
	 */
	// @Test
	public void testDeleteFlow() {
		FlowInfo info = keptFlows.get(0);
		try {
			mgr.deleteFlow(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#createDBConnection()}
	 * .
	 */
	@Test
	public void testCreateDBConnection() {
		DbConnectionInfo info = new DbConnectionInfo("guest", "MyConn",
				ResourceType.Personal);
		DbConnection dbc = new DbConnection("oracle", "localhost", 5432,
				"demo", "guest", "testPassword", "JDBC drive name");
		dbc.setConnName("MyConn");
		info.setConnection(dbc);
		info.setCreateUser("guest");
		info.setModifiedUser("guest");
		info.setResourceType(ResourceType.Personal);
		info.setId("MyConn");

		try {
			mgr.createDBConnection(info);
		} catch (OperationFailedException e) {
			e.printStackTrace();
			fail("Create DB connection failed.");
		}

		try {
			mgr.createDBConnection(info);
			fail("Did not see connection already exist exception.");
		} catch (OperationFailedException e) {
			// Good.
		}
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getDBConnectionList(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetDBConnectionList() {
		List<DbConnectionInfo> list = mgr.getDBConnectionList("guest");
		// Assert.assertEquals(1, list.size());
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#updateDBConnection()}
	 * .
	 */
	@Test
	public void testUpdateDBConnection() {
		List<DbConnectionInfo> list = mgr.getDBConnectionList("guest");
		Assert.assertTrue(list.size() > 0);
		DbConnectionInfo local = new DbConnectionInfo("guest", "MyConn",
				ResourceType.Personal);
		DbConnection dbc = new DbConnection("oracle", "localhost", 5432,
				"demo", "guest", "testPassword", "jdbc driver name");
		dbc.setConnName("MyConn");
		local.setConnection(dbc);

		try {
			nap();
			mgr.updateDBConnection(list.get(0));
		} catch (DataOutOfSyncException ex) {
			fail("First update should work.");
		}

		try {
			nap();
			mgr.updateDBConnection(local);
			// fail("Exception DataOutOfSync did not happen.");
		} catch (DataOutOfSyncException ex) {
			// Good.
		}
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#deleteDBConnection()}
	 * .
	 * @throws Exception 
	 */
	// @Test
	public void testDeleteDBConnection() throws Exception {
		List<DbConnectionInfo> list = mgr.getDBConnectionList("guest");
		Assert.assertTrue(list.size() > 0);
		for (DbConnectionInfo info : list) {
			mgr.deleteDBConnection(info);
		}
		
		list = mgr.getDBConnectionList("guest");
		Assert.assertEquals(0, list.size());
	}

 
  

 
	private void nap() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore this.
		}
	}
}
