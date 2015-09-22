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

import java.util.ArrayList;
import java.util.List;

public class VisualizationModelScatter extends AbstractVisualizationModelChart {

	private List<VisualPointGroup> pointGroups = null;
	private ArrayList<VisualLine> visualLines = null;
	private String sourceOperatorClass = "";

	public static final String Source_Operator_KMeans = "KMeansOperator";

	public String getSourceOperatorClass() {
		return sourceOperatorClass;
	}

	public void setSourceOperatorClass(String sourceOperatorClass) {
		this.sourceOperatorClass = sourceOperatorClass;
	}

	public List<VisualPointGroup> getPointGroups() {
		return pointGroups;
	}

	public void setPointGroups(List<VisualPointGroup> pointGroups) {
		this.pointGroups = pointGroups;
	}

	public void addVisualPointGroup(VisualPointGroup group) {
		if (pointGroups == null) {
			pointGroups = new ArrayList<VisualPointGroup>();
		}
		if (pointGroups.contains(group) == false) {
			pointGroups.add(group);
		}
	}

	public VisualizationModelScatter(String title, List<VisualPointGroup> pointGroups) {
		super(TYPE_SCATTER_CHART, title);

		this.pointGroups = pointGroups;
		super.sethGrid(true);
		super.setvGrid(true);

	}

	public VisualizationModelScatter() {
		super(TYPE_SCATTER_CHART, "");

	}

	public void addVisualLine(VisualLine visualLine) {
		if (this.visualLines == null) {
			visualLines = new ArrayList<VisualLine>();
		}
		visualLines.add(visualLine);

	}

	public ArrayList<VisualLine> getVisualLines() {
		return visualLines;
	}

	public void setVisualLines(ArrayList<VisualLine> visualLines) {
		this.visualLines = visualLines;
	}

}