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

import com.alpine.miner.workflow.output.AbstractVisualizationModel;

public class VisualizationModelText extends AbstractVisualizationModel {
	private String text;

	public VisualizationModelText(String title,String text) {
		super(TYPE_TEXT,title);
		setText(text);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
