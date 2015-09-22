/**
 * ClassName TreeVisualizationOutPut.java
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
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.swt.graphics.Image;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AbstractVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.widgets.VisualizationChart;

public   class TreeVisualizationOutPut extends
		AbstractVisualizationOutPut implements VisualizationOutPut {
	private FreeformLayeredPane freeLayer;
	
	public static final String TYPE_DECISION_TREE="Tree";
	public static final String TYPE_CART_TREE="Tree";//cart is same as Tree now
	public static final String TYPE_NEAURAL_NETWORK="NN";
	
	String type;
	int depth=0;
	private VisualizationChart visualizationChart;
	
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public TreeVisualizationOutPut(FreeformLayeredPane layer) {
		freeLayer = layer;
	}
	@Override
	public Object getVisualizationObject() {
		return visualizationChart;
	}
	public FreeformLayeredPane getTreeModel() {
		return freeLayer;
	}
	
	private int  saveWidth =0;
	public int getSaveWidth() {
		return saveWidth;
	}

	public void setSaveWidth(int saveWidth) {
		this.saveWidth = saveWidth;
	}

	public int getSaveHeight() {
		return saveHeight;
	}

	public void setSaveHeight(int saveHeight) {
		this.saveHeight = saveHeight;
	}

	public int getShowWidth() {
		return showWidth;
	}

	public void setShowWidth(int showWidth) {
		this.showWidth = showWidth;
	}

	public int getShowHeight() {
		return showHeight;
	}

	public void setShowHeight(int showHeight) {
		this.showHeight = showHeight;
	}
	private int  saveHeight =0;
	private int showWidth =0;
	private int showHeight =0;
	
	private Image img;

	public Image getImg() {
		return img;
	}
	public void setImg(Image img) {
		this.img = img;
	}
	public VisualizationChart getVisualizationChart() {
		return visualizationChart;
	}
	public void setVisualizationChart(VisualizationChart visualizationChart) {
		this.visualizationChart = visualizationChart;
	}
	
}
