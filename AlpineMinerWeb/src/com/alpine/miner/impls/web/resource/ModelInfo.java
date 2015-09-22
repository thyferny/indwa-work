/**
 * ClassName :ModelInfo.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.web.resource;

import com.alpine.miner.impls.resource.ResourceInfo;

/**
 *  ModelInfo is a pure property file
 * @author zhaoyong
 *
 */
public class ModelInfo extends ResourceInfo{
	public static final String ALGORITHM_NAME = "algorithmName";

	public static final String MODEL_NAME = "modelName";

	public static final String FLOW_NAME = "flowName"; 

	//same as the UI's node name
	String modelName;	
	String algorithmName;
	String flowName;
	
	public ModelInfo(String userName,ResourceType type,String id,String modelName,String algorithmName,String flowname){
		super(userName, id, type) ;
		this.modelName=modelName;
		this.algorithmName=algorithmName;
		this.flowName=flowname;
	}
	
	public ModelInfo(){
		
	}
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getAlgorithmName() {
		return algorithmName;
	}
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}
	public String getFlowName() {
		return flowName;
	}
	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}
 

}
