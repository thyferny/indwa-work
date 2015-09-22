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

import java.util.List;

import com.alpine.miner.workflow.output.AbstractVisualizationModel;

public abstract class AbstractVisualizationModelMultiple extends AbstractVisualizationModel implements VisualizationModelMultiple {
	 
	private List<VisualizationModel> models;

	public List<VisualizationModel> getModels() {
		return models;
	}

	public void setModels(List<VisualizationModel> models) {
		this.models = models;
	}

	public AbstractVisualizationModelMultiple(String title,int type,List<VisualizationModel> models) {
		super(type, title);
		this.models=models;
	 
	}
 
}
