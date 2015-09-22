/**
 * ClassName VisualNode.java
 *
 * Version information: 3.00
 *
 * Data: 2011-7-11
 * @author zhaoyong
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.ArrayList;
import java.util.List;

public class VisualNode {
	
	public static final int NODE_TYPE_NORMAL = 0; 
	//default value...
	private int nodeType=NODE_TYPE_NORMAL;
	public int getNodeType() {
		return nodeType;
	}
	public void setNodeType(int nodetype) {
		this.nodeType = nodetype;
	}

	public static final int NODETYPE_LEAF=1;
	public static final int NODETYPE_THRESHHOLD=2;
	//the next nodes linked to 
	List<VisualNode> childNodes;
 
	private  VisualNode parentNode;

	public VisualNode getParentNode() {
		return parentNode;
	}
	public void setParentNode(VisualNode parentNode) {
		this.parentNode = parentNode;
	}

	String label;
	String toolTip;
	//the order of the grid 1,1 means the first one in the first layer... 
	Integer xGrid=  0;
	Integer yGrid=  0;
	int layer = 0;
	
	 
	public int getLayer() {
		return layer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}
	public List<VisualNode> getChildNodes() {
		return childNodes;
	}
	public void setChildNodes(List<VisualNode> childNodes) {
		this.childNodes = childNodes;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getToolTip() {
		return toolTip;
	}
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
	public Integer getxGrid() {
		return xGrid;
	}
	public void setxGrid(Integer xGrid) {
		this.xGrid = xGrid;
	}
	public Integer getyGrid() {
		return yGrid;
	}
	public void setyGrid(Integer yGrid) {
		this.yGrid = yGrid;
	}
 
	public void addChild(VisualNode vNode) {
		if(childNodes==null){
			childNodes= new ArrayList<VisualNode>();
		}
		childNodes.add(vNode) ;
		
	}

 public String toString(){
	 String result = "(x="+xGrid+",y= "+yGrid+")\n";
	 if(childNodes!=null){
		 for(int i=0;i<childNodes.size();i++){
			 VisualNode node = childNodes.get(i); 
			 if(node!=null){
				 result=result+"children: ["+i+"]" +node.toString();
			 }	 
		 }
		 
	 }
	 return result;
 }

}
