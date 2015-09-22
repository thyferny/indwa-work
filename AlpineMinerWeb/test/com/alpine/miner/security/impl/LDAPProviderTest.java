/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * LDAPProviderTest.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security.impl;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.OperationNotAllowedException;
import com.alpine.miner.security.UserInfo;

/**
 * @author sam_zang
 *
 */
public class LDAPProviderTest{

	private static final String DEFAULT_USER = "Person";
	private static final String DEFAULT_PASS = "1";
	private static final String GID = "grOup_for_juNIT";
	private static AuthenticationProvider auth = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		auth = LDAPProvider.getInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		AuthenticationProvider ins = LocalProvider.getInstance();
		Assert.assertNotNull(ins);
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#LocalProvider()}.
	 */
	@Test
	public void testLocalProvider() {
		LocalProvider lp = new LocalProvider();
		Assert.assertNotNull(lp);
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAuthenticate() {
		Assert.assertTrue(auth.authenticate(DEFAULT_USER, DEFAULT_PASS));
		//Assert.assertFalse(auth.authenticate(DEFAULT_USER, ""));
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#getUserInfoList()}.
	 */
	@Test
	public void testGetUserInfoList() {
		List<UserInfo> list = auth.getUserInfoList();
		Assert.assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#getGroupInfoList()}.
	 */
	@Test
	public void testGetGroupInfoList() {
		List<GroupInfo> list = auth.getGroupInfoList();
		Assert.assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#getUserInfoByGroup(java.lang.String)}.
	 */
	@Test
	public void testGetUserInfoByGroup() {
		List<UserInfo> list = auth.getUserInfoByGroup(null);
		Assert.assertTrue(list.size() == 0);
		list = auth.getUserInfoByGroup("Group1");
		Assert.assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#createGroup(com.alpine.miner.security.GroupInfo)}.
	 */
	@Test
	public void testCreateGroup() {
		GroupInfo g = new GroupInfo();
		g.setId(GID);
		try {
			auth.createGroup(g);
		} catch (OperationNotAllowedException e) {
			//OK, operation not supported by LADP
			
		}
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#updateGroup(com.alpine.miner.security.GroupInfo)}.
	 */
	@Test
	public void testUpdateGroup() {
		GroupInfo g = new GroupInfo();
		g.setId(GID);
		try {
			auth.updateGroup(g);
		} catch (OperationNotAllowedException e) {
			//OK, operation not supported by LADP
			
		}
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#deleteGroup(com.alpine.miner.security.GroupInfo)}.
	 */
	@Test
	public void testDeleteGroup() {
		GroupInfo g = new GroupInfo();
		g.setId(GID);
		try {
			auth.deleteGroup(g);
		} catch (OperationNotAllowedException e) {
			//OK, operation not supported by LADP
			
		}
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#createUser(com.alpine.miner.security.UserInfo)}.
	 */
	@Test
	public void testCreateUser() {
		UserInfo u = new UserInfo();
		u.setLogin(GID);
		try {
			auth.createUser(u);
		} catch (OperationNotAllowedException e) {
			//OK, operation not supported by LADP
			
		}
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#updateUser(com.alpine.miner.security.UserInfo)}.
	 */
	@Test
	public void testUpdateUser() {
		UserInfo u = new UserInfo();
		u.setLogin(GID);
		try {
			auth.updateUser(u);
		} catch (OperationNotAllowedException e) {
			//OK, operation not supported by LADP
			
		}
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#deleteUser(com.alpine.miner.security.UserInfo)}.
	 */
	@Test
	public void testDeleteUser() {
		UserInfo u = new UserInfo();
		u.setLogin(GID);
		try {
			auth.deleteUser(u);
		} catch (OperationNotAllowedException e) {
			//OK, operation not supported by LADP
			
		}
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#getUserInfoByName(java.lang.String)}.
	 */
	@Test
	public void testGetUserInfoByName() {
		UserInfo u = auth.getUserInfoByName(DEFAULT_USER);
		Assert.assertNotNull(u);
		u = auth.getUserInfoByName(GID);
		Assert.assertNull(u);
	}

	/**
	 * Test method for {@link com.alpine.miner.security.impl.LocalProvider#getUserGroups(java.lang.String)}.
	 */
	@Test
	public void testGetUserGroups() {
		String[] glist = auth.getUserGroups(DEFAULT_USER);
		Assert.assertTrue(glist.length > 0);
		glist = auth.getUserGroups(GID);
		Assert.assertTrue(glist.length == 0);
	}


}
