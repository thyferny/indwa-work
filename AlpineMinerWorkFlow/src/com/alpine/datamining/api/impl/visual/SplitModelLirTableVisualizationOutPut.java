/**
 * ClassName SplitModelLirTableVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import com.alpine.miner.view.ui.dataset.SplitModelForLirEntity;

/**
 * @author Jeff Dong
 *
 */
public class SplitModelLirTableVisualizationOutPut extends
	DataTableVisualizationOutPut {

	private SplitModelForLirEntity entity;

	public SplitModelLirTableVisualizationOutPut(SplitModelForLirEntity entity) {
		super(entity.getSummaryTable());
		this.entity = entity;
	}
	@Override
	public Object getVisualizationObject() {
		return getEntity();
	}
	
	public SplitModelForLirEntity getEntity() {
		return entity;
	}

	public void setEntity(SplitModelForLirEntity entity) {
		this.entity = entity;
	}
	
	
}
