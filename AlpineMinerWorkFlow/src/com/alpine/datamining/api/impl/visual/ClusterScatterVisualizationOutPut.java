/**
 * ClassName JFreeChartImageVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import com.alpine.miner.view.ui.dataset.ClusterScatterEntity;


public class ClusterScatterVisualizationOutPut extends ImageVisualizationOutPut {
	private ClusterScatterEntity entity;

	public ClusterScatterVisualizationOutPut(ClusterScatterEntity entity) {
		this.entity = entity;
	}

	private ClusterScatterEntity getEntity() {
		return entity;
	}

	@Override
	public Object getVisualizationObject() {
		return getEntity();
	}
	
	
}
