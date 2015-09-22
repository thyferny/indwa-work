/**
 * ClassName VisualizationModelText.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 * @author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

public class VisualizationModelNetwork extends AbstractVisualizationModelNodeGroup {
	

	public VisualizationModelNetwork(String title,List<VisualNode> allChildNodes,List<VisualNodeLink> links) {
		super(TYPE_NETWORK,title,  allChildNodes, links);
	 
	}

 
}
