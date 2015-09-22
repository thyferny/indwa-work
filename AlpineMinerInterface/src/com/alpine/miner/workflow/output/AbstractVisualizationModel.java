/**
 * ClassName AbstractVisualizationModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.miner.workflow.output.visual.VisualizationModel;

public abstract class AbstractVisualizationModel implements VisualizationModel {

	private int type = TYPE_UNKNOW;
	private String title = null;
	private boolean error = false;
	private List<String> errorMessages = null; 
	private AnalyticNodeMetaInfo analyticNodeMetaInfo = null;
	private String[][] operatorInputs = null;
	private boolean needGenerateReport = true;  
	
	
	public void setOpeatorInputs(String[][] operatorInputs){
		this.operatorInputs=operatorInputs;
	}
	public String[][] getOpeatorInputs(  ){
		return operatorInputs;
	} 
	
	public AnalyticNodeMetaInfo getAnalyticNodeMetaInfo() {
		return analyticNodeMetaInfo;
	}

	public void setAnalyticNodeMetaInfo(AnalyticNodeMetaInfo analyticNodeMetaInfo) {
		this.analyticNodeMetaInfo = analyticNodeMetaInfo;
	}

	public AbstractVisualizationModel(int typeText, String title) {
		setVisualizationType(typeText);
		setTitle(title);
	}

	@Override
	public int getVisualizationType() {
		return type;
	}

	@Override
	public void setVisualizationType(int type) {
		this.type = type;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public List<String> getErrorMessage(){
		return errorMessages;
	}

	public void setErrorMessage(List<String> errorMessage){
		this.errorMessages = errorMessage; 
	}

	public void addErrorMessage(String errorMessage){
		if(this.errorMessages ==null){
			this.errorMessages = new ArrayList<String> ();
		}
		this.errorMessages.add(errorMessage);
	}
	
	public void setNeedGenerateReport(boolean need) {
		this.needGenerateReport = need ;
	}
	public boolean isNeedGenerateReport() {
		return needGenerateReport;
	}
}
