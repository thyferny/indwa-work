/**
 * ClassName AbstractChartVisualizationModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;


public abstract class AbstractVisualizationModelNodeGroup extends AbstractVisualizationModelChart {


	List<VisualNode> allChildNodes;//first level child
	List<VisualNodeLink> links;
  
	
	
	public AbstractVisualizationModelNodeGroup(int typeText, String title, List<VisualNode> allChildNodes, List<VisualNodeLink> links) {
		super(typeText, title);
		this.links=links;
		this.allChildNodes=allChildNodes;
	}

	
	public List<VisualNodeLink> getLinks() {
		return links;
	}
 
	public void setLinks(List<VisualNodeLink> links) {
		this.links = links;
	}
 

	public List<VisualNode> getAllChildNodes() {
		return allChildNodes;
	}



	public void setAllChildNodes(List<VisualNode> allChildNodes) {
		this.allChildNodes = allChildNodes;
	}


 

}
