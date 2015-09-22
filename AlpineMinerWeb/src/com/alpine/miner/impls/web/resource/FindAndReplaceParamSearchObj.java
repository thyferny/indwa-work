/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * FlowInfo.java
 * 
 * Author Will
 * 
 * Version 2.0
 * 
 * Date 2014/4/12
 */
 
package com.alpine.miner.impls.web.resource;

import java.util.List;
import java.util.Map;


public class FindAndReplaceParamSearchObj{
	private String paramterName;
	private String parameterValue;
	private String searchScope;



	private boolean ignoreCase;

	private FlowInfo flowInfo;
	
	private String replaceValue;
	private List<ReplaceObj> replaceList;
	
	
	public List<ReplaceObj> getReplaceList() {
		return replaceList;
	}
	public void setReplaceList(List<ReplaceObj> replaceList) {
		this.replaceList = replaceList;
	}
	public FindAndReplaceParamSearchObj(){
   	 
    }
	public String getParamterName() {
		return paramterName;
	}
	public void setParamterName(String paramterName) {
		this.paramterName = paramterName;
	}
	public String getParameterValue() {
		return parameterValue;
	}
	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public String getReplaceValue() {
		return replaceValue;
	}
	public void setReplaceValue(String replaceValue) {
		this.replaceValue = replaceValue;
	}
	

	
	public FlowInfo getFlowInfo() {
		return flowInfo;
	}
	public void setFlowInfo(FlowInfo flowInfo) {
		this.flowInfo = flowInfo;
	}
	
	public String getSearchScope() {
		return searchScope;
	}
	public void setSearchScope(String searchScope) {
		this.searchScope = searchScope;
	}
	

	@Override
	public String toString() {
		
		return this.paramterName+"\t"+this.parameterValue+"\t"+this.flowInfo+"\t"+this.searchScope+"\t"+this.ignoreCase;
	}

}
