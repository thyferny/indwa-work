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

public class VisualizationModelClusterProfile extends VisualizationModelTableGrouped {

	public VisualizationModelClusterProfile(String title,
			List<String> tableHeader, List<List<VisualizationModel>> models) {
		super(title, tableHeader, models);
		//super.setVisualizationType(TYPE_CLUSTERPROFILE_CHART);
	}
 

 
 
}
