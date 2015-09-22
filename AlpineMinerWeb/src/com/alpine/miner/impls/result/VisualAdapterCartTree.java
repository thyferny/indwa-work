/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterCartTree.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.hadoop.models.DecisionTreeHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.operator.tree.cartregression.RegressionTree;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.operator.tree.threshold.Side;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.miner.workflow.output.visual.VisualNode;
import com.alpine.miner.workflow.output.visual.VisualNodeLink;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelTree;
import com.alpine.utility.file.StringUtil;

public class VisualAdapterCartTree extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterCartTree INSTANCE = new VisualAdapterCartTree();
 	
	private HashMap<Tree,VisualNode> addedNodeMap=new  HashMap<Tree,VisualNode> (); 
	 //   layer    x used...           
	 	
	private List<List<VisualNode>> layeredNodeList= null;


	private List<VisualNode> lastVisiableList = new ArrayList<VisualNode>();

	private int nodetHeight = 30;
	private int lastVisiableDepth = 0;
	private int nodewidth = 90;  
	private static int nodeRangeHeight = 80;

	private static int TopOffset = 30;
	private static int leftOffSet =80;
	private final static int nodeOffsetX = 30;	 
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		 
		 
		Object model = null;
		Tree tree=null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			model = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
			if(model instanceof RegressionTreeModel){
				tree=(((RegressionTreeModel) model).getRoot());
			}else if(model instanceof DecisionTreeModel){
				tree=(((DecisionTreeModel) model).getRoot());
			}else if(model instanceof DecisionTreeHadoopModel){
				tree=(((DecisionTreeHadoopModel) model).toVisualTree());
			}
			String treeName = analyzerOutPut.getAnalyticNode().getName();
			return createTreeModel(tree, treeName); 
			
		}else{
			return null;
		}

	}

	public VisualizationModelTree createTreeModel(Tree tree, String treeName) {
		List<VisualNode> visualNodes= new ArrayList<VisualNode>();
		List<VisualNodeLink> nodeLinks=new ArrayList<VisualNodeLink> ();
		 fillNodeAndLinkListByTree(null,tree,visualNodes,nodeLinks,0);
		  VisualNode rootNode = visualNodes.get(0);
		 calculateNodeOXYZ(null,rootNode);
 
		//makesure the node mode is pure node with out child
		clearChildNodes(visualNodes);
		 
		VisualizationModelTree visualModel= new VisualizationModelTree(treeName,
				rootNode,visualNodes,nodeLinks);
		
		visualModel.setMaxY(String.valueOf(TopOffset*2+nodetHeight+(nodetHeight+nodeRangeHeight)*lastVisiableDepth));
		visualModel.setMaxX(String.valueOf(leftOffSet*2+nodewidth+(nodewidth+nodeOffsetX)*lastVisiableList.size()));
		return visualModel;
	}
 
	protected void calculateNodeOXYZ(VisualNode node,VisualNode myRootNode){
		if(node == null){
			 
				if(myRootNode.getChildNodes()!=null&&myRootNode.getChildNodes().size()>0 ){
					int maxX =0;
					int minX =0;
					for(VisualNode childNode:myRootNode.getChildNodes()){
						calculateNodeOXYZ(childNode,myRootNode);
						if(maxX<childNode.getxGrid()){
							maxX = childNode.getxGrid();
						}
						if(minX ==0){
							minX = childNode.getxGrid();
						}
					}
					myRootNode.setxGrid(((maxX-minX)/2+minX));
				}else{//only one root, no more children
					myRootNode.setxGrid(leftOffSet);
				} 
				myRootNode.setyGrid(TopOffset+(nodetHeight+nodeRangeHeight)*myRootNode.getyGrid());
				
				return;
			 
		}else{
			if(node.getChildNodes()!=null&&node.getChildNodes().size()>0){
				int maxX =0;
				int minX =0;
				int size = node.getChildNodes().size();
				int index= 0;
				for(VisualNode childNode:node.getChildNodes()){
					calculateNodeOXYZ(childNode,myRootNode);
	 					if(maxX<childNode.getxGrid()){
						maxX = childNode.getxGrid();
					}
					if(minX ==0){
						minX = childNode.getxGrid();
					}
					index=index+1;
				}
				node.setxGrid(((maxX-minX)/2+minX));
			}else{
				node.setxGrid((leftOffSet+(nodeOffsetX+nodewidth)*lastVisiableList.size()));
				lastVisiableList.add(node);
				if(node.getLayer()>lastVisiableDepth){
					lastVisiableDepth = node.getLayer();
				}
			}
			node.setyGrid(TopOffset+(nodetHeight+nodeRangeHeight)*node.getLayer());
			
		 
		}
	}

	private void balanceAllNodes( int allDpeth) {
		//the 0 layer need not do this so >0 not >=0
		for (int i = allDpeth-1; i >=0; i--) {
			List<VisualNode> layerList = layeredNodeList.get(i);
			balanceAllNodes(layerList,i);
		}
	 }
	
	private void balanceAllNodes(List<VisualNode> layerList, int depth) {
		if(layerList!=null&&layerList.size()>0){
			for (Iterator iterator = layerList.iterator(); iterator.hasNext();) {
				VisualNode visualNode = (VisualNode) iterator.next();
			if(visualNode.getChildNodes()!=null&&visualNode.getChildNodes().size()>0){
				balanceNode(visualNode,depth);
			}
			}
		}
		
	}


	private void balanceNode(VisualNode visualNode, int depth) {
		int size = visualNode.getChildNodes().size();
		List<VisualNode> childs = visualNode.getChildNodes();
		for (int i = 0;i<childs.size();i++) {
			VisualNode child = (VisualNode) childs.get(i);
			int shouldX =visualNode.getxGrid()-(size/2)+i;
			if(child.getxGrid()<shouldX){
				int delta = shouldX-child.getxGrid();
				addDeltaRecusively(depth+1,layeredNodeList.get(depth+1).indexOf(child),delta);
			}
			
			}
		
		if(size%2==0){
			
		}else{
			
		}
		
	}


	private void addDeltaRecusively(int depth, int index, int delta) {
		List<VisualNode> list = layeredNodeList.get(depth);
		if(list!=null&&list.size()>0&&list.size()>index){
			for (int i = index; i < list.size(); i++) {
				VisualNode node = list.get(i);
				if(node!=null){
					node.setxGrid(delta+node.getxGrid()) ;
					if(node.getChildNodes()!=null&&node.getChildNodes().size()>0){
						int childIndex = layeredNodeList.get(depth+1).indexOf(node.getChildNodes().get(0));
						 addDeltaRecusively(  depth+1, childIndex,   delta); 
							
					}
				}
				
			}
		}
		
	}


//form the last layer...
	private void rearrangeAllXGrids( int depth) {
		//the 0 layer need not do this so >0 not >=0
		for (int i = depth; i >0; i--) {
			List<VisualNode> layerList = layeredNodeList.get(i);
			rearrangeLayeredXGrids(layerList,i);
		}
	 }


	private void makrAllBlankNodes(int depth) {
		//now keep the middel blakk for oushu
		for (int i = depth; i >0; i--) {
			List<VisualNode> layerList = layeredNodeList.get(i);
			makeBlanXGrids(layerList,i);
			
		}
	}

 

	private void makeBlanXGrids(List<VisualNode> layerList, int layerDepth) {
		List<VisualNode> handledParents= new ArrayList<VisualNode>();
		for (Iterator iterator = layerList.iterator(); iterator.hasNext();) {
			VisualNode visualNode = (VisualNode) iterator.next();
			VisualNode parent = visualNode.getParentNode();
			if(handledParents.contains(parent)==false){
				int size = parent.getChildNodes().size();
				//finde the half one
				 VisualNode middle = parent.getChildNodes().get(size/2);
				if(size%2==0){
						 //add a blank
					if(middle.getxGrid()-1==parent.getChildNodes().get((size/2)-1).getxGrid()){
						 addDelta(layerList,layerList.indexOf(middle),1);
						 }
				 
					 
				} 
				handledParents.add(parent) ;
			}
		
		}
	
}


	private void rearrangeLayeredXGrids(List<VisualNode> layerList, int layerDepth) {
		List<VisualNode> handledParents= new ArrayList<VisualNode>();
		for (Iterator iterator = layerList.iterator(); iterator.hasNext();) {
			VisualNode visualNode = (VisualNode) iterator.next();
			VisualNode parent = visualNode.getParentNode();
			if(handledParents.contains(parent)==false){
				//make sure the first child...
				if(visualNode.getxGrid()<parent.getxGrid()){
					int delta=parent.getxGrid()-visualNode.getxGrid();
					
					addDelta(layerList,layerList.indexOf(visualNode),delta);
					
				}else if(visualNode.getxGrid()>parent.getxGrid()){
					int delta=visualNode.getxGrid()-parent.getxGrid();
					List<VisualNode> parentList = layeredNodeList.get(layerDepth-1);
					addDelta(parentList,parentList.indexOf(parent),delta);
					
				}
				List<VisualNode> children = parent.getChildNodes();
				int xGrid=(children.get(0).getxGrid()+children.get(children.size()-1).getxGrid())/2;
				int delta = xGrid-parent.getxGrid();
				List<VisualNode> parentList = layeredNodeList.get(layerDepth-1);
				addDelta(parentList,parentList.indexOf(parent),delta);
			
				
				handledParents.add(parent) ;
				
			}
		}
	
}



	private void addDelta(List<VisualNode> layerList, int startIndex, int delta) {
		if(layerList!=null){
			for (int i = startIndex; i < layerList.size(); i++) {
				VisualNode node = layerList.get(i);
				if(node!=null){
					node.setxGrid(delta+node.getxGrid()) ;
				}
				
			}
		}
	}



	private void initLayeredNodeList(List<VisualNode> visualNodes,int depth) {
		layeredNodeList= new  ArrayList<List<VisualNode>>();
		for (int i = 0; i <=depth; i++) {
			List<VisualNode> layer=new ArrayList<VisualNode>();
			layeredNodeList.add(layer) ;
			for (Iterator iterator = visualNodes.iterator(); iterator.hasNext();) {
				VisualNode visualNode = (VisualNode) iterator.next();
				if(visualNode.getyGrid()==i){
					layer.add(visualNode);
				}
				
			}
		}
		
	}




	private void countNativeXGrid(List<VisualNode> visualNodes, int depth,int parentX) {
 
		for (int i = 0; i <=depth; i++) {
			int xGrid=0;
			for (Iterator iterator = visualNodes.iterator(); iterator.hasNext();) {
				VisualNode visualNode = (VisualNode) iterator.next();
				if(visualNode.getyGrid()==i){
					visualNode.setxGrid(xGrid);
				 
					
					xGrid++;
				}
				
			}
		}
	}

	private void clearChildNodes(List<VisualNode> visualNodes) {
		for (Iterator iterator = visualNodes.iterator(); iterator.hasNext();) {
			VisualNode visualNode = (VisualNode) iterator.next();
			visualNode.setChildNodes(null) ;
			visualNode.setParentNode(null) ;
		}
		
	}
  

	private int  fillNodeAndLinkListByTree(VisualNode parent,Tree tree, 
			List<VisualNode> vNodes, List<VisualNodeLink> nodeLinks, int depth) {
		int maxDepth=depth;
		VisualNode vNode= new VisualNode();
		vNode.setParentNode(parent) ;
		//regeressiontree's leaf is special
		setLabelAndTooltip(tree.getLabel(), vNode);
		 
		vNode.setLayer(depth);
		if(tree.childIterator()!=null&&tree.childIterator().hasNext()){
			vNode.setNodeType(VisualNode.NODE_TYPE_NORMAL);
		}else{//leaf
			vNode.setNodeType(VisualNode.NODETYPE_LEAF);
			 String label = tree.getLabel();
			 if(tree instanceof RegressionTree){
				
				 
				 String dev=String.valueOf(((RegressionTree)tree).getDeviance());
				 String count = String.valueOf(((RegressionTree)tree).getCount());
				 StringBuffer tooltip = new StringBuffer();
					label = resizeLabel(label);
					if(label.equals(tree.getLabel())==false){
						tooltip.append(tree.getLabel()).append("\n");
					} 
					//regeressiontree leaf have special label 
					vNode.setLabel(label);
					
					tooltip.append(DEVIANCE).append(" : ").append(dev).append("\n");
					tooltip.append(COUNT).append(" : ").append(count).append("\n");
					vNode.setToolTip(tooltip.toString()) ;
				 
			 }else{
				Map<String, Integer> countMap = tree.getCounterMap();
				 StringBuffer tooltip = new StringBuffer();
					label = resizeLabel(label);
					if(label.equals(tree.getLabel())==false){
						tooltip.append(tree.getLabel()).append("\n");
					} 
					//regeressiontree leaf have special label 
					vNode.setLabel(label);
				for (Iterator iterator = countMap.keySet().iterator(); iterator
						.hasNext();) {
					String key = (String) iterator.next();
					Integer value = countMap.get(key);
					tooltip.append(COUNT+"(").append(key).append("): ").append(value).append("\n");
				}
				vNode.setToolTip(tooltip.toString()) ;
				
			 }
		}
 
		if(addedNodeMap.keySet().contains(tree)==false){
			vNodes.add(vNode) ;
			addedNodeMap.put(tree, vNode);
		} 
		
		if(parent!=null){
			parent.addChild(vNode);
		}
		Iterator<Side> childIterator = tree.childIterator();
		
		if(childIterator!=null){
			depth=depth+1;
		
			while (childIterator.hasNext()) {
				Side edge = childIterator.next();
				//handle the child first
				int tempDepth=fillNodeAndLinkListByTree(vNode,edge.getChild(), vNodes,nodeLinks,depth);
				if(maxDepth<tempDepth){
					maxDepth=tempDepth;
				}
				 
			}
		
			 childIterator = tree.childIterator();
			//then hand the link ...
			 while (childIterator.hasNext()) {
				 Side edge = childIterator.next();
				VisualNode childNode=addedNodeMap.get(edge.getChild());
 
				//? r1 < 7  =>  <7   (r)
				String linkLabel = edge.getCondition().toString();
				if(vNode.getToolTip()!=null){
					linkLabel =linkLabel.replace(vNode.getToolTip(), "");
				}
				VisualNodeLink link = new VisualNodeLink(vNodes.indexOf(vNode),
						vNodes.indexOf(childNode),linkLabel); 
				nodeLinks.add(link);
			}
			
		 
		}
	  return maxDepth; 
		
	}



	private void setLabelAndTooltip(String label, VisualNode vNode) {
		vNode.setToolTip(label);
		label = resizeLabel(label);
 
		vNode.setLabel(label);
	 
	}



	private String resizeLabel(String label) {
		if(StringUtil.isEmpty(label)){
			label= "";
		}
		if(label.length()>14){
			label=label.substring(0,10)+"...";
		}
		return label;
	}
 	 
}
