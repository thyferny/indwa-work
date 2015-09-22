/**
 * ClassName VisualizationModelMultiple.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

public interface VisualizationModelMultiple extends VisualizationModel {

	public List<VisualizationModel> getModels() ;

	public void setModels(List<VisualizationModel> models);
}
