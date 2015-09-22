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

public class VisualizationModelPieChart extends AbstractVisualizationModel {
	 
	private List<String> labels; 
	private List<String> numbers; 

	public List<String> getNumbers() {
		return numbers;
	}

	public void setNumbers(List<String> numbers) {
		this.numbers = numbers;
	} 

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public VisualizationModelPieChart(String title,List<String> labels, List<String> numbers) {
		super(TYPE_PIECHART,title);
		this.labels=labels;
		this.numbers=numbers;
	}

 
	
	
}
