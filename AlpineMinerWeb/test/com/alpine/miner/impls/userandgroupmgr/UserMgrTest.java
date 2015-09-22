/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * UserTest.java
 */
package com.alpine.miner.impls.userandgroupmgr;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Test;

import com.alpine.miner.impls.web.resource.LoginManager;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.OperationNotAllowedException;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;

/**
 * @author Gary
 * Jan 22, 2013
 */
public class UserMgrTest {

	@Test
	public void testCreateUserWithChorusKey(){
		AuthenticationProvider auth = ProviderFactory.getAuthenticator(LoginManager.ADMIN_USER);
		UserInfo userInfo = new UserInfo();
		userInfo.setLogin("unitTestUser");
		userInfo.setChorusKey("a_b_c");
		try {
			auth.createUser(userInfo);
		} catch (OperationNotAllowedException e) {
			e.printStackTrace();
		}
		UserInfo checkUser = auth.getUserInfoByName("unitTestUser");
		Assert.assertEquals("a_b_c", checkUser.getChorusKey());
	}

    @Test
	public void testUpdateUserWithChorusKey(){
		AuthenticationProvider auth = ProviderFactory.getAuthenticator(LoginManager.ADMIN_USER);
		UserInfo userInfo = new UserInfo();
		userInfo.setLogin("unitTestUser");
		userInfo.setChorusKey("a_b_c_updated");
		try {
			auth.updateUser(userInfo);
		} catch (OperationNotAllowedException e) {
			e.printStackTrace();
		}
		UserInfo checkUser = auth.getUserInfoByName("unitTestUser");
		Assert.assertEquals("a_b_c_updated", checkUser.getChorusKey());
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
    @AfterClass
	public static void tearDown() throws Exception {
		AuthenticationProvider auth = ProviderFactory.getAuthenticator(LoginManager.ADMIN_USER);
		UserInfo userInfo = new UserInfo();
		userInfo.setLogin("unitTestUser");
		auth.deleteUser(userInfo);
	}
}
