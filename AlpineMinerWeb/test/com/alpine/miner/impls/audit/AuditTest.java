/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * AuditTest
 * May 15, 2012
 */
package com.alpine.miner.impls.audit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.alpine.miner.impls.flow.AbstractFlowTest;

/**
 * @author Gary
 *
 */
public class AuditTest extends AbstractFlowTest  {

	private AuditManager target = AuditManager.INSTANCE;

	@Test
	public void testQueryAudit(){
		List<AuditItem> audits = target.getUserAudits("guest");
		Assert.assertTrue(audits.size() > 0);
	}

	@Test
	public void testAppendAudit(){
		int originalSize = target.getUserAudits("guest").size();
		target.appendUserAuditItem("guest", new AuditItem("guest", ActionType.LOGIN));
		int latestSize = target.getUserAudits("guest").size();
		Assert.assertTrue(latestSize > originalSize);
	}
}
