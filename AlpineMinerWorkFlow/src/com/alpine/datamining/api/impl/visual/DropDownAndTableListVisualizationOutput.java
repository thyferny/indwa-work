package com.alpine.datamining.api.impl.visual;

import com.alpine.miner.view.ui.dataset.DropDownTableEntity;

public class DropDownAndTableListVisualizationOutput extends
	DataTableVisualizationOutPut {
	
	private Object obj;
	private DropDownTableEntity entity;
	public DropDownTableEntity getEntity() {
		return entity;
	}
	public void setEntity(DropDownTableEntity entity) {
		this.entity = entity;
	}
	@Override
	public Object getVisualizationObject() {
		return getEntity();
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
}
