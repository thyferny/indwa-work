/**
 * ClassName :AuditManagerImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-22
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.audit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alpine.miner.impls.web.resource.FilePersistence;

public class AuditManagerImpl implements AuditManager {
	public static final String AUDIT_FILE = "audit.log";

	private static final File AUDIT_ROOT;
	
	static{
		AUDIT_ROOT = new File(FilePersistence.initRoot() + "audit_logs" + File.separator);
		if(!AUDIT_ROOT.exists()){
			AUDIT_ROOT.mkdirs();
		}
	}

	@Override
	public void appendUserAuditItem(String user, AuditItem item) {
		try {
			appendLineToAuditFile(user,item);
		} catch (Exception e) {
			throw new AuditException(e);
		}
	}

	@Override
	public void appendUserAudits(String user, List<AuditItem> audits) {
		try {
			appendLineToAuditFile(user,audits.toArray(new AuditItem[audits.size()]));
		} catch (Exception e) {
			throw new AuditException(e);
		}
	}

	@Override
	public void deleteUserAudits(String user, List<AuditItem> audits) {
		// TODO Auto-generated method stub
		// add by will begin
		File auditsFile = findCategoryFile(user);
		if(null!=auditsFile && auditsFile.exists()){
			auditsFile.delete();
		}
	}	

	@Override
	public void deleteUserItem(String user, AuditItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AuditItem> getUserAudits(String user) {
		return iterateContentOfAudit(user);
	}
	
	
	private void appendLineToAuditFile(String category, AuditItem... items) throws Exception{
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(findCategoryFile(category),true)));
			for(AuditItem item : items){
				writer.println(item.toString());
			}
		} catch (IOException e) {
			throw e;
		}finally{
			writer.close();
		}
	}
	
	private List<AuditItem> iterateContentOfAudit(String category){
		BufferedReader reader = null;
		LinkedList<AuditItem> result = new LinkedList<AuditItem>();
		try {
			reader = new BufferedReader(new FileReader(findCategoryFile(category)));
			String line;
			while((line = reader.readLine()) != null){
				result.addFirst(new AuditItem(line));
			}
		} catch (Exception e) {
			throw new AuditException(e);
		}finally{
			try {
				reader.close();
			} catch (IOException e) {
				//ignore
			}
		}
		return result;
	}
	
	private File findCategoryFile(String category){
		File auditFile = new File(AUDIT_ROOT.getPath() + File.separator + category + File.separator + AUDIT_FILE);
		if(!auditFile.exists()){
			File userFolder = auditFile.getParentFile();
			if(!userFolder.exists()){
				userFolder.mkdir();
			}
			try {
				auditFile.createNewFile();
			} catch (IOException e) {
				throw new AuditException(e);
			}
		}
		return auditFile;
	}
}
