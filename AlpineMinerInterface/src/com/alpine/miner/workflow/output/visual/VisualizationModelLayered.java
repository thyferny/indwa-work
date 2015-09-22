/**
 * ClassName VisualizationModelText.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.HashMap;
import java.util.List;

import com.alpine.miner.workflow.output.AbstractVisualizationModel;

/**
 * VisualizationModelLayered is a special model it has multiple visual modes with each key.
 * The user can switch in a Combo to see each the visual( could be a table, a chart or any thing)
 * 
 *   please be careful the VisualizationModel can also be the layerd model so you can create 2, 3 layerd model
 *   like kmeans scatter, it is 3  layered ...
 * */

public class VisualizationModelLayered extends AbstractVisualizationModel {
	
	private String keyLable=""; 
	HashMap<String, VisualizationModel> modelMap =new HashMap<String, VisualizationModel>();
	
	public HashMap<String, VisualizationModel> getModelMap() {
		return modelMap;
	}

	public void setModelMap(HashMap<String, VisualizationModel> modelMap) {
		this.modelMap = modelMap;
	}

	//keep the same order...
	private List<String> keys;
	
	
	public String getKeyLable() {
		return keyLable;
	}
 
	public void setKeyLable(String keyLable) {
		this.keyLable = keyLable;
	}

 	public List<String> getKeys() {
		return keys;
	}
 
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public VisualizationModelLayered(String title,String keyLable,List<String> keys,HashMap<String, VisualizationModel> modelMap ) {
		super(TYPE_LAYERED,title);
		this.modelMap=modelMap;
		this.keyLable=keyLable;
		this.keys=keys;
	}
 
}
