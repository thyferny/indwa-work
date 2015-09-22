/**
 * ClassName :AuditManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-22
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.audit;

import java.util.List;

public interface AuditManager {
	public static AuditManager INSTANCE = new AuditManagerImpl();
	
	public List<AuditItem> getUserAudits(String user);
	public void appendUserAudits(String user,List<AuditItem> audits);
	public void appendUserAuditItem(String user,AuditItem item);
	
	public void deleteUserAudits(String user,List<AuditItem> audits);
	public void deleteUserItem(String user, AuditItem item);
	

}
