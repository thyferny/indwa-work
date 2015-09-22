/**
 * ClassName VisualizationModelText.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * @author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;



public class VisualizationModelTree extends AbstractVisualizationModelNodeGroup {
 
	VisualNode treeRoot;// tree root
	 
	
	public VisualizationModelTree(String title, VisualNode treeRoot,List<VisualNode> allChildNodes, List<VisualNodeLink> links) {
		super(TYPE_TREE,title,  allChildNodes,  links);
		this.treeRoot=treeRoot;
	}

 
}
