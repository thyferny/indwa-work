/**
 * NeuralChart.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual.tree;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.api.impl.visual.widgets.ScrollDialog;
import com.alpine.datamining.api.impl.visual.widgets.VisualizationChart;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;
import com.alpine.datamining.operator.neuralnet.sequential.NNNode;
import com.alpine.datamining.operator.neuralnet.sequential.NodeInner;
import com.alpine.datamining.operator.neuralnet.sequential.NodeInput;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.tools.AlpineMath;

/**
 * @author Jimmy
 *
 */
public class NeuralChart extends VisualizationChart{
	public void createChart(NNModel model){
		treeChart = new FreeformLayeredPane();
		treeChart.setLayoutManager(new FreeformLayout());
		treeChart.setBorder(new MarginBorder(5));
		treeChart.setBackgroundColor(ColorConstants.white);
		treeChart.setOpaque(true);
		
		List<List<NeuralFigure>> list = new ArrayList<List<NeuralFigure>>();
		int xLayer = 180;
		int yLayer = 70;		

		String ConstThreshold = "Threshold";
		// add head node in list
		NodeInput[] inputNodes = model.getInputNodes();
		List<NeuralFigure> headNodes = new ArrayList<NeuralFigure>();
		for(int i=0;i<inputNodes.length;i++){
			NeuralFigure nf = new NeuralFigure();
			nf.setNodeName(inputNodes[i].getNodeName());
			headNodes.add(nf);
		}
		NeuralFigure nf_head = new NeuralFigure();
		nf_head.setNodeName(ConstThreshold);
		nf_head.setBackGroundColorNf(ColorConstants.lightBlue);
		headNodes.add(nf_head);
		list.add(headNodes);
		
		//add center node in list
		NodeInner[] InnerNodes = model.getInnerNodes();
		int layerIndex = -1;
		List<NeuralFigure> tempList = null;
		for(int i=0;i<InnerNodes.length;i++){
			if (InnerNodes[i].getLayerIndex() != NNNode.OUTPUT) {
				if(layerIndex<InnerNodes[i].getLayerIndex()){
					if(tempList != null){
						NeuralFigure nf_cent = new NeuralFigure();
						nf_cent.setNodeName(ConstThreshold);
						nf_cent.setBackGroundColorNf(ColorConstants.lightBlue);
						tempList.add(nf_cent);
						list.add(tempList);
					}
					tempList = new ArrayList<NeuralFigure>();
					layerIndex = InnerNodes[i].getLayerIndex();
				}
				NeuralFigure nf = new NeuralFigure();
				nf.setNodeName(InnerNodes[i].getNodeName());
				NNNode[] nodes = InnerNodes[i].getInputNodes();
				for(NNNode nNNode:nodes){
					nf.addInputNodeName(nNNode.getNodeName());
				}
				nf.addInputNodeName("Threshold");
				double[] doubles = InnerNodes[i].getWeights();
				for(int j=0;j<doubles.length;j++){
					if(j==doubles.length-1){
						break;
					}
					nf.addInputNodeValue(AlpineMath.doubleExpression(doubles[j+1]));
				}
				nf.addInputNodeValue(AlpineMath.doubleExpression(doubles[0]));
				tempList.add(nf);
			}
		}
		if(tempList != null){
			NeuralFigure nf_cent = new NeuralFigure();
			nf_cent.setNodeName(ConstThreshold);
			nf_cent.setBackGroundColorNf(ColorConstants.lightBlue);
			tempList.add(nf_cent);
			list.add(tempList);
		}
		
		
		//add end node in list
		List<NeuralFigure> endNodes = new ArrayList<NeuralFigure>();
		
		for(int i=0;i<InnerNodes.length;i++){
			if (InnerNodes[i].getLayerIndex() == NNNode.OUTPUT) {
				NeuralFigure nf = new NeuralFigure();
				nf.setNodeName(InnerNodes[i].getNodeName());
				NNNode[] nodes = InnerNodes[i].getInputNodes();
				for(NNNode nNNode:nodes){
					nf.addInputNodeName(nNNode.getNodeName());
				}
				nf.addInputNodeName("Threshold");
				double[] doubles = InnerNodes[i].getWeights();
				for(int j=0;j<doubles.length;j++){
					if(j==doubles.length-1){
						break;
					}
					nf.addInputNodeValue(AlpineMath.doubleExpression(doubles[j+1]));
				}
				nf.addInputNodeValue(AlpineMath.doubleExpression(doubles[0]));
				endNodes.add(nf);
			}
		}
		list.add(endNodes);
		
		
		for(int i=0;i<list.size();i++){
			List<NeuralFigure> hiddenList = list.get(i);
			for(int j=0;j<hiddenList.size();j++){
				NeuralFigure nf = hiddenList.get(j);
				nf.setCenterX(xLayer+i*xLayer);
				nf.setCenterY(yLayer + j * yLayer);
				nf.setBounds();
				ToolTip a = new ToolTip();
				StringBuffer sb = new StringBuffer();
				if (nf.getInputNodeNames().size() > 0) {
					int maxLength = "weights:".length();
					for (int n = 0; n < nf.getInputNodeNames().size(); n++) {
						sb.append(nf.getInputNodeNames().get(n) + ":"
								+ nf.getInputNodeValues().get(n));
						sb.append("\n");
						String str = nf.getInputNodeNames().get(n)+":"+nf.getInputNodeValues().get(n);
						if(str.length()>maxLength){
							maxLength = str.length();
						}
					}
					a.setMessage("weights:\n"+sb.toString());
					a.setDisplayMessage("weights:\n"+sb.toString());
					a.setX(0);
					a.setY(0);
					a.setWidth(maxLength*8);
					a.setHeight(18+nf.getInputNodeNames().size()*20);
					a.setBounds();
				}else{
					a.setMessage(nf.getNodeName());
					a.setDisplayMessage(nf.getNodeName());
					a.setX(0);
					a.setY(0);
					a.setWidth(nf.getNodeName().length()*15);
					a.setHeight(20);
					a.setBounds();
				}
				nf.setToolTip(a);
				nf.addMouseListener(new MouseListener(){

					@Override
					public void mouseDoubleClicked(MouseEvent paramMouseEvent) {
						ScrollDialog.openInformation(new Shell(),((ToolTip)((NeuralFigure)paramMouseEvent.getSource()).getToolTip()).getMessage());
					}

					@Override
					public void mousePressed(MouseEvent paramMouseEvent) {
						
					}

					@Override
					public void mouseReleased(MouseEvent paramMouseEvent) {
						
					}
					
				});
				treeChart.add(nf);
			}
		}
		
		int hiddenSize = list.size();
		for(int i=0;i<hiddenSize;i++){
			if(i==0){
				NeuralHeader nh = new NeuralHeader();
				nh.setCenterX(xLayer+i*xLayer-17);
				nh.setCenterY(25);
				nh.setWidth(60);
				nh.setHeight(20);
				nh.setBounds();
				nh.setMessage("Input");
				nh.setDisplayMessage("Input");
				nh.setFont(VisualUtility.getTreeFont());
				treeChart.add(nh);
			}else if(i==hiddenSize-1){
				NeuralHeader nh = new NeuralHeader();
				nh.setCenterX(xLayer+i*xLayer-17);
				nh.setCenterY(25);
				nh.setWidth(60);
				nh.setHeight(20);
				nh.setBounds();
				nh.setMessage("Output");
				nh.setDisplayMessage("Output");
				nh.setFont(VisualUtility.getTreeFont());
				treeChart.add(nh);
			}else{
				NeuralHeader nh = new NeuralHeader();
				nh.setCenterX(xLayer+i*xLayer-17);
				nh.setCenterY(25);
				nh.setWidth(80);
				nh.setHeight(20);
				nh.setBounds();
				nh.setMessage("Hidden "+i);
				nh.setDisplayMessage("Hidden "+i);
				nh.setFont(VisualUtility.getTreeFont());
				treeChart.add(nh);
			}
		}
		
		Hashtable<String,NeuralFigure> ht = new Hashtable<String, NeuralFigure>();
		for(int i=0;i<list.size();i++){
			if(i==0){
				continue;
			}
			ht.clear();
			List<NeuralFigure> previousList = list.get(i-1);
			for(NeuralFigure nn:previousList){
				ht.put(nn.getNodeName(),nn);
			}
			List<NeuralFigure> hiddenList = list.get(i);
			for(int j=0;j<hiddenList.size();j++){
				NeuralFigure nf = hiddenList.get(j);
				List<String> nodeList = nf.getInputNodeNames();
				for(String name:nodeList){
					if(ht.containsKey(name)){
						PathFigure path = new PathFigure();
						path.setSourceAnchor(ht.get(name).outAnchor);
						path.setTargetAnchor(nf.inAnchor);
						path.setAlpha(40);
						
						PolylineDecoration dec = new PolylineDecoration();
						dec.setAlpha(80);
						path.setDecoration(dec);
						//add link loaction point
						path.getPoints().removeAllPoints();
						NeuralFigure sourceNode = ht.get(name);
						NeuralFigure targetNode = nf;
					    path.getPoints().addPoint(sourceNode.getX()+sourceNode.getWidth(),sourceNode.getY()+sourceNode.getHeight()/2);
					    path.getPoints().addPoint(targetNode.getX(),targetNode.getY()+targetNode.getHeight()/2);
						treeChart.add(path);
					}
				}
			}
		}
		
		calculateSize();
		
		String showMessage = getMessage(model);
		NeuralHeader nh = new NeuralHeader();
		nh.setX(10);
		nh.setY(0);
		nh.setWidth(showMessage.length()*13);
		nh.setHeight(20);
		nh.setBounds();
		nh.setMessage(showMessage);
		nh.setDisplayMessage(showMessage);
		nh.setFont(VisualUtility.getTreeFont());
		nh.setSavePdf(false);
		treeChart.add(nh);
	}
	

	public Image getImage(){
		Image img = new Image(null, getShowWidth(),getShowHeight());
		GC gc = new GC(img);
    	Graphics graphics = new SWTGraphics(gc);
    	List<IFigure> list = treeChart.getChildren();
    	for(IFigure f:list){
    		if(f instanceof NeuralFigure){
    			String name=((NeuralFigure)f).getNodeName();
        		graphics.drawText(name, ((NeuralFigure) f).getCenterX()-15,
        				18+((NeuralFigure) f).getCenterY() );
    		}
    		if(f instanceof ActivityFigure){
    			if(!((ActivityFigure)f).isSavePdf())continue;
    		}
    		f.paint(graphics);
    	}
    	graphics.dispose();
        gc.dispose();
        return img;
	}
	
	static final int addOffsetX = 80;
	private void calculateSize() {
		List list = treeChart.getChildren();
		int maxX=0;
		int maxY=0;
		for(int i =0;i<list.size();i++){
				if(list.get(i) instanceof ActivityFigure){
				ActivityFigure figure=(ActivityFigure) list.get(i);
				if(maxX<figure.getBounds().x){						
					maxX=figure.getBounds().x;
				}
				if(maxY<figure.getBounds().y){						
					maxY=figure.getBounds().y;
				}
			}
		}
		setShowHeight(maxY+addOffsetX);
		setShowWidth(maxX+addOffsetX);
	}
	

	private String getMessage(NNModel model){
		StringBuffer baseInfo=new StringBuffer();
		if (model.getLabel().isNumerical())
		{
			baseInfo.append("R2: " + (AlpineMath.doubleExpression(model.getRSquare())) + Tools.getLineSeparator());
		}
		else if (model.getLabel().isNominal())// && getLabel().getMapping().size() == 2)
		{
			baseInfo.append("null Deviance: " 
					+ (AlpineMath.doubleExpression(model.getNullDeviance())) + "; deviance: " 
					+  AlpineMath.doubleExpression(model.getDeviance()) + Tools.getLineSeparator());
		}
		
		 
		return baseInfo.toString();
		
	}
}
