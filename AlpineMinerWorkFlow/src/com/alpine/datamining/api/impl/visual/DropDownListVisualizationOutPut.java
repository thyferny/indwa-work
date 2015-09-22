package com.alpine.datamining.api.impl.visual;

import java.util.List;

import org.jfree.chart.JFreeChart;

import com.alpine.miner.view.ui.dataset.DropDownListEntity;

public class DropDownListVisualizationOutPut extends ImageVisualizationOutPut {
	private DropDownListEntity entity;
	private JFreeChart chart;
	private List list;

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	public DropDownListVisualizationOutPut(DropDownListEntity entity) {
		this.entity = entity;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public DropDownListEntity getEntity() {
		return entity;
	}

	public void setEntity(DropDownListEntity entity) {
		this.entity = entity;
	}

	@Override
	public Object getVisualizationObject() {
		return getEntity();
	}
}
