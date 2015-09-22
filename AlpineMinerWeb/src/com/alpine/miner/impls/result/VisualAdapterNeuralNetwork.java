/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterNeuralNetwork.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.operator.neuralnet.sequential.NNNode;
import com.alpine.datamining.operator.neuralnet.sequential.NodeInner;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualNode;
import com.alpine.miner.workflow.output.visual.VisualNodeLink;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelNetwork;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterNeuralNetwork extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	private static final String HIDDEN = "Hidden";

	private static final String INPUT = "Input";

	private static final String OUTPUT = "Output";

	public static final OutPutVisualAdapter INSTANCE = new VisualAdapterNeuralNetwork();
  	
	private HashMap<NNNode,VisualNode> addedNodeMap=new  HashMap<NNNode,VisualNode> (); 
	private HashMap<VisualNode,VisualNode> parentThreshHoldMap = new   HashMap<VisualNode,VisualNode> (); 
	int innerLayerNumber=0;
	List<NNNode[]> layeredInnerNodes=null;
	List<VisualNode> outputNodes = new ArrayList<VisualNode>() ;
	 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			NNModel model = (NNModel)  ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
			List<VisualNode> networkNodes= new ArrayList<VisualNode>();
			List<VisualNodeLink> nodeLinks=new ArrayList<VisualNodeLink> ();
	 
			innerLayerNumber=countInnerLayerNumber(model.getInnerNodes());
			layeredInnerNodes = buildLayerNodeList(model);
			
			fillNodeList(model,networkNodes);
	 		
			fillLinkList(model,nodeLinks,networkNodes);
			 
			
			countYGrid(model);
			 	
			
			VisualizationModelNetwork networkModel= new VisualizationModelNetwork(VisualNLS.getMessage(VisualNLS.Output_Image, locale),
					networkNodes,nodeLinks);
			List<String[]> xLabels = new ArrayList<String[]>() ; 
			xLabels.add(new String[]{"1",INPUT});
			for (int i = 0; i < innerLayerNumber; i++) {
				xLabels.add(new String[]{String.valueOf(i+2),HIDDEN + 		" "+String.valueOf(i+1)});
			}
			
			xLabels.add(new String[]{String.valueOf(innerLayerNumber+2),OUTPUT});
			//this si special used for the tilte header
			networkModel.setxLabels(xLabels);
			networkModel.setDescription(getDescription(model)) ;
			
			
			//composite:+ table (node description...)
//			Output Nodes Description:
			DataTable nodeDescTable= new DataTable();
			List<TableColumnMetaInfo> columns =Arrays.asList(new TableColumnMetaInfo[]{
					new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.Node_Name, locale),""), 
					
					new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.Node_Description, locale),"")
			});  
			nodeDescTable.setColumns(columns);
			List<DataRow> rows = new ArrayList<DataRow>(); 

		 
			addNodesDescriptiopn(rows,  networkNodes);
			 
			
			nodeDescTable.setRows(rows) ;
			VisualizationModelDataTable descModel = new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.Node_Description, locale) ,nodeDescTable) ;
			
 
			List<VisualizationModel> models =Arrays.asList(new VisualizationModel[]{
					networkModel,descModel
			}); 
			VisualizationModel vModel = new VisualizationModelComposite (analyzerOutPut.getAnalyticNode().getName()
					,models); 
			return vModel;
			
		}else{
			return null;
		}

	}
 
 

	/**
	 * @param rows
	 * @param networkNodes
	 */
	private void addNodesDescriptiopn(List<DataRow> rows, List<VisualNode> networkNodes) {
	if(networkNodes!=null){
		for (int i = 0; i < networkNodes.size(); i++) {
			   VisualNode node = networkNodes.get(i);
			 DataRow row = new DataRow();
			 row.setData(new String[]{node.getLabel(),node.getToolTip()}) ;
			 rows.add(row) ;
		}
	}
		
		
		
	}



	private String getDescription(NNModel model){
		StringBuffer baseInfo=new StringBuffer();
		if (model.getLabel().isNumerical())
		{
			baseInfo.append(R2+": " + (AlpineMath.doubleExpression(model.getRSquare())) + Tools.getLineSeparator());
		}
		else if (model.getLabel().isNominal())// && getLabel().getMapping().size() == 2)
		{
			baseInfo.append(NULL_DEVIANCE +": " 
					+ (AlpineMath.doubleExpression(model.getNullDeviance())) + "; " +DEVIANCE+": " 
					+  AlpineMath.doubleExpression(model.getDeviance()) + Tools.getLineSeparator());
		}
		
		 
		return baseInfo.toString();
		
	}

	private void fillLinkList(NNModel model, List<VisualNodeLink> nodeLinks, List<VisualNode> networkNodes) {
		
		Set<NNNode> keys = addedNodeMap.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			NNNode nnNode = (NNNode) iterator.next();
			if(nnNode.getLayerIndex()!=NNNode.INPUT){
			  fillLinkForNode(nodeLinks, networkNodes, 
						(NodeInner)nnNode);
			  }
		}
 
	}

	private void fillLinkForNode(List<VisualNodeLink> nodeLinks,
			List<VisualNode> networkNodes,   NodeInner node) {
		VisualNode 	endNode=addedNodeMap.get(node);
		int end=  networkNodes.indexOf(endNode);  
		VisualNode startNode;
		NNNode[] inputs = node.getInputNodes();
		
		for (int j = 0; j < inputs.length; j++) {
			startNode=addedNodeMap.get(inputs[j]);
		
			int start = networkNodes.indexOf(startNode);
		
			VisualNodeLink link=  new VisualNodeLink(start,end,AlpineMath.doubleExpression(node.getWeights()[j+1]));
			nodeLinks.add(link);
		}
		VisualNode threshHoldNode = parentThreshHoldMap.get(endNode);
		//this is for the inputs's threshhold
		VisualNodeLink link=  new VisualNodeLink(networkNodes.indexOf(threshHoldNode),end,AlpineMath.doubleExpression(node.getWeights()[0]));
		nodeLinks.add(link);
		
		//threa
	 
	}


	private void countYGrid(NNModel model) {
		
		NNNode[] nodes = (NNNode[])model.getInputNodes();		
		countYGrid(nodes);
		
		//get out nodes from inner nodes,this is real
		
		NNNode[] innerNodes = (NNNode[])model.getInnerNodes();
		List<NNNode> outList= new ArrayList<NNNode>();
		
		for (int i = 0; i < innerNodes.length; i++) {
			if(innerNodes[i].getLayerIndex()==NNNode.OUTPUT){
				outList.add(innerNodes[i]);
			}
		}
		
		
		nodes=outList.toArray(new NNNode[outList.size()] ) ;
		
		countYGrid(nodes); 
		for (Iterator iterator = layeredInnerNodes.iterator(); iterator.hasNext();) {
			 nodes =  (NNNode[]) iterator.next(); 
			 countYGrid(nodes);
		}
		 
	 		
	}


	private List<NNNode[] > buildLayerNodeList(NNModel model) {
		List<NNNode[]> list =new ArrayList<NNNode[]>();
		NodeInner[] innerNodes = model.getInnerNodes();
		for (int i = 0; i < innerLayerNumber; i++) {
			NNNode[] nodes=getInnerNodes(innerNodes,i);
			list.add(nodes) ;
		
		}
		 
		return list;
	}


	private void countYGrid(NNNode[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			VisualNode vNode = addedNodeMap.get(nodes[i]);
			if(vNode!=null){
				vNode.setyGrid(i+1) ;
			}
		}
		
	}


	private NNNode[] getInnerNodes(NodeInner[] innerNodes, int  layerIndex) {
		List<NNNode> list= new ArrayList<NNNode>();
		for (int i = 0; i < innerNodes.length; i++) {
			if(layerIndex==innerNodes[i].getLayerIndex()){
				list.add(innerNodes[i]) ;
			}
		}
				return list.toArray(new NNNode[list.size()]);
	}


	private int countInnerLayerNumber(NodeInner[] innerNodes) {
		int number = 0;
		for (int i = 0; i < innerNodes.length; i++) {
			if (number < innerNodes[i].getLayerIndex()) {
				number = innerNodes[i].getLayerIndex();
			}
		}

		return number + 1;
	}

	private void fillNodeList(NNModel model, List<VisualNode> networkNodes) {

		NNNode[] nodes = model.getInputNodes();
		fillNodes(networkNodes, nodes);

		// add threashold node for inuts
		VisualNode inputThreshHold = createThreshHoldNode( 1,
				nodes.length + 1);
		 
			
		networkNodes.add(inputThreshHold);
		// output can from innernodes
		nodes = model.getInnerNodes();
		fillNodes(networkNodes, nodes);

		int layer = 1;
		for (Iterator iterator = layeredInnerNodes.iterator(); iterator.hasNext();) {
			NNNode[] layerNodes = (NNNode[]) iterator.next();
			for (int i = 0; i < layerNodes.length; i++) {
				parentThreshHoldMap.put(addedNodeMap.get(layerNodes[i]), inputThreshHold) ;	
			}
			
			inputThreshHold = createThreshHoldNode(  1 + layer,
					layerNodes.length + 1);

			networkNodes.add(inputThreshHold);

			layer = layer + 1;

		}
		//the last layer
		for (int i = 0; i < outputNodes.size(); i++) {
			parentThreshHoldMap.put(outputNodes.get(i), inputThreshHold) ;	
		}
		

	}



	private VisualNode createThreshHoldNode(  Integer xGrid, Integer yGrid) {
		VisualNode  inputThreshHold=new VisualNode();
		inputThreshHold.setNodeType(VisualNode.NODETYPE_THRESHHOLD) ;
		
		inputThreshHold.setLabel("Threshhold") ;
		inputThreshHold.setToolTip( "Threshhold");
		inputThreshHold.setxGrid(xGrid);
		inputThreshHold.setyGrid(yGrid) ;
		return inputThreshHold;
	}


	private void fillNodes(List<VisualNode> networkNodes,
		  NNNode[] inputNodes) {
		for (int i = 0; i < inputNodes.length; i++) {
			NNNode nnNode=(NNNode)inputNodes[i];
			VisualNode vNode = createVisualNodeByNNNode(nnNode,networkNodes);
			
				if(addedNodeMap.keySet().contains(nnNode)==false){
					networkNodes.add(vNode) ;
					addedNodeMap.put(nnNode, vNode);
				}
			}
	}
 

	private VisualNode createVisualNodeByNNNode( NNNode nnNode,
			List<VisualNode> networkNodes ) { 
		VisualNode vNode= new VisualNode();
		
	 
		NNNode[] inputs = nnNode.getInputNodes();
		if(inputs!=null&&inputs.length>0){
			StringBuffer toolTip= new StringBuffer();
			for (int i = 0; i < inputs.length; i++) {
				NNNode nodei = inputs[i];
				toolTip.append(nodei.getNodeName()).append(":").append(nnNode.getWeight(i)).append("\n") ;
			}
			vNode.setToolTip(toolTip.toString()); 
		}else{
			if(StringUtil.isEmpty(nnNode.getNodeName())==true){
				vNode.setToolTip("");
			}else{
				vNode.setToolTip(nnNode.getNodeName());
			}
		}
		
	
	 
		vNode.setLabel(nnNode.getNodeName());
		if(nnNode.getLayerIndex()==NNNode.INPUT){
			vNode.setxGrid(1) ;
		}
		else if(nnNode.getLayerIndex()==NNNode.OUTPUT){
			outputNodes.add(vNode) ;
			vNode.setxGrid(innerLayerNumber+2) ;
		}else{
			vNode.setxGrid(nnNode.getLayerIndex()+2) ;
		}
	 	return vNode;
	 	
	}
  
}
