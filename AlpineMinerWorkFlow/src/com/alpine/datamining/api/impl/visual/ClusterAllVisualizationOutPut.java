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
import com.alpine.miner.view.ui.dataset.ClusterAllEntity;


public class ClusterAllVisualizationOutPut extends ImageVisualizationOutPut {
	private ClusterAllEntity entity;

	public ClusterAllVisualizationOutPut(ClusterAllEntity entity) {
		this.entity = entity;
	}

	private ClusterAllEntity getEntity() {
		return entity;
	}

	@Override
	public Object getVisualizationObject() {
		return getEntity();
	}
	
	
}
