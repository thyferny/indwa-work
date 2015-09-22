package com.alpine.miner.impls.flowHistory;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alpine.miner.impls.web.resource.FlowInfo;

public class FlowHistoryInfo implements Serializable {
	private static final long serialVersionUID = 6299222173403565451L;

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String 	id,
					type,
					key,
					version,
					comments,
					createUser,
					modifiedUser,
					groupName,
					opendTime,
					displayText;
					
	private String[] categories;
	private long 	createTime,
					modifiedTime;
					
	
	public FlowHistoryInfo(FlowInfo flow) {
		this.id = flow.getId();
		this.type = flow.getResourceType().name();
		this.key = flow.getKey();
		this.version = flow.getVersion();
		this.comments = flow.getComments();
		this.createUser = flow.getCreateUser();
		this.modifiedUser = flow.getModifiedUser();
		this.groupName = flow.getGroupName();
		this.createTime = flow.getCreateTime();
		this.modifiedTime = flow.getModifiedTime();
		this.categories = flow.getCategories();
		this.displayText = buildDisplayText(flow);
		
		this.opendTime = SDF.format(new Date());
	}
	
	private String buildDisplayText(FlowInfo flow){
		String text = flow.getCategories() == null || flow.getCategories().length == 0 ? flow.getModifiedUser() : flow.getCategories()[0];
		text += File.separator;
		text += flow.getId();
		return text;
	}

	public String getKey() {
		return key;
	}

	public String getOpendTime() {
		return opendTime;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}

	public String getComments() {
		return comments;
	}

	public String getCreateUser() {
		return createUser;
	}

	public String getModifiedUser() {
		return modifiedUser;
	}

	public String getGroupName() {
		return groupName;
	}

 

	public long getCreateTime() {
		return createTime;
	}

	public long getModifiedTime() {
		return modifiedTime;
	}

	/**
	 * @return the displayText
	 */
	public String getDisplayText() {
		return displayText;
	}

	/**
	 * @param displayText the displayText to set
	 */
	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	/**
	 * @return the categories
	 */
	public String[] getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(String[] categories) {
		this.categories = categories;
	}

}
