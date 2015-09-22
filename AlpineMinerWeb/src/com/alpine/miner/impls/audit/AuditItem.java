/**
 * ClassName :AuditItem.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-22
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.impls.audit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class AuditItem {

	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String SEP = ","; 
	// user,time,action,action detail
	private static final String PATTERN = ".*,.*,.*,.*,.*";
	private String sequence;
	private String user = null;
	private String dateTime = null;
	private String action = null;
	private String actionDetail = "";
	
	public AuditItem(String user,ActionType action){
		this(user,new Date(),action);
	}
	
	public AuditItem(String user,ActionType action, String actionDetail){
		this(user,new Date(),action,actionDetail);
	}

	public AuditItem(String user, Date dateTime, ActionType action,
			String actionDetail) {
		this(user,dateTime,action);
		if(actionDetail!=null){
			this.actionDetail = actionDetail;
		}
	}
	
	public AuditItem(String user, Date dateTime, ActionType action ) {
		this.sequence = UUID.randomUUID().toString();
		
		this.user = user;
		
		if(dateTime!=null){
			this.dateTime = DF.format(dateTime);
		}
		if(action!=null){
			this.action = action.name();
		}
	}
	
	public AuditItem(String fromString) {
		if(fromString!=null&&fromString.matches(PATTERN)){
			String[] fields = fromString.split(SEP);
			for(int i = 0; i < fields.length;i++){
				switch(i){
				case 0:
					this.sequence = fields[i];
					break;
				case 1:
					this.user = fields[i];
					break;
				case 2:
					this.dateTime = fields[i];
					break;
				case 3:
					this.action = fields[i];
					break;

				case 4:
					this.actionDetail = fields[i];
					break;
				}
			}
//			StringTokenizer st = new StringTokenizer(fromSrting,SEP);
//			this.user = st.nextToken();
//			this.dateTime = st.nextToken();
//			this.action = st.nextToken();
//			this.actionDetail = st.nextToken();
		}
	}

	public String getUser() {
		return user;
	}

	public String getDateTime() {
		return dateTime;
	}

	public String getAction() {
		return action;
	}

	public String getActionDetail() {
		return actionDetail;
	}
	
	//stored in a csv file
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(sequence).append(SEP).append(user).append(SEP).append(dateTime).append(SEP);
		sb.append(action).append(SEP).append(actionDetail).append(SEP);
		return sb.toString();
	}

	public String getSequence() {
		return sequence;
	}

	public void setActionDetail(String actionDetail) {
		this.actionDetail = actionDetail;
	}

}
