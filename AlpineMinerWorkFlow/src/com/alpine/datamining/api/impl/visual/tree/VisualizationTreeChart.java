/**
 * TreeChart.java
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.alpine.datamining.api.impl.visual.widgets.VisualizationChart;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.operator.tree.threshold.DevideCond;
import com.alpine.datamining.operator.tree.threshold.Side;
/**
 * @author Jimmy
 *
 */
public class VisualizationTreeChart extends VisualizationChart{

	private int defaultLayer = 3;
	private int depth = 0;
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	private static int nodeRangeHeight = 100;

	private static int TopOffset = 30;
	private static int leftOffSet =120;
	private final static int nodeOffsetX = 30;
	private ConditionFigure rootNode;
	private List<ActivityFigure> lastVisiableList = new ArrayList<ActivityFigure>();
	private int lastVisiableDepth = 0;
	private int nodeDepth =0;
	private List<ActivityFigure> lastNodeList = new ArrayList<ActivityFigure>();
	
	
	public void createChart(Tree model) {
		treeChart = new FreeformLayeredPane();
		treeChart.setLayoutManager(new FreeformLayout());
		treeChart.setBorder(new MarginBorder(5));
		treeChart.setBackgroundColor(ColorConstants.white);
		treeChart.setOpaque(true);
		Tree tree = model;
		lastVisiableList.clear();
		lastVisiableDepth =0;
		setShowHeight(0);
		setShowWidth(0);
		lastNodeList.clear();
		nodeDepth =0;
		
		walkTree(null, new ArrayList<ActivityFigure>(), tree, 0);
		calculateNodeOXYZ(null);
		calculateLinkOXYZ();
		calculateSize();
		setDepth(nodeDepth);
		treeChart.setFocusTraversable(true);
	}
	
	private void calculateSize(){
		int width =0;
		int height =0;
		if(DecisionFigure.NodeWidth>width){
			width = DecisionFigure.NodeWidth;
		}
		if(ConditionFigure.NodeWidth>width){
			width = ConditionFigure.NodeWidth;
		}
		
		if(DecisionFigure.NodeHeight>height){
			height = DecisionFigure.NodeHeight;
		}
		if(ConditionFigure.NodeHeight>height){
			height = ConditionFigure.NodeHeight;
		}
		setShowHeight(TopOffset*2+height+(height+nodeRangeHeight)*nodeDepth);
		setShowWidth(leftOffSet*2+width+(width+nodeOffsetX)*lastNodeList.size());
		
		setSaveHeight(TopOffset*2+height+(height+nodeRangeHeight)*lastVisiableDepth);
		setSaveWidth(leftOffSet*2+width+(width+nodeOffsetX)*lastVisiableList.size());
	}
	
	private void calculateNodeOXYZ(ActivityFigure node){
		if(node == null){
			if(rootNode.isVisible()){
				if(rootNode.getChildList().size()>0&&rootNode.getChildList().get(0).isVisible()){
					int maxX =0;
					int minX =0;
					for(ActivityFigure childNode:rootNode.getChildList()){
						calculateNodeOXYZ(childNode);
						if(maxX<childNode.getX()){
							maxX = childNode.getX();
						}
						if(minX ==0){
							minX = childNode.getX();
						}
					}
					rootNode.setX(((maxX-minX)/2+minX));
				}else{
					rootNode.setX(leftOffSet);
				}
				rootNode.setY(TopOffset+(rootNode.getHeight()+nodeRangeHeight)*rootNode.getLayer());
				rootNode.setBounds();
				return;
			}
		}else{
			if(node.getChildList().size()>0&&node.getChildList().get(0).isVisible()){
				int maxX =0;
				int minX =0;
				for(ActivityFigure childNode:node.getChildList()){
					calculateNodeOXYZ(childNode);
					if(maxX<childNode.getX()){
						maxX = childNode.getX();
					}
					if(minX ==0){
						minX = childNode.getX();
					}
				}
				node.setX(((maxX-minX)/2+minX));
			}else{
				node.setX(leftOffSet+(nodeOffsetX+node.width)*lastVisiableList.size());
				lastVisiableList.add(node);
				if(node.getLayer()>lastVisiableDepth){
					lastVisiableDepth = node.getLayer();
				}
			}
			node.setY(TopOffset+(node.getHeight()+nodeRangeHeight)*node.getLayer());
			node.setBounds();
		}
	}
	
	
	private void calculateLinkOXYZ(){
		Hashtable<ConnectionAnchor,ActivityFigure> htSource = new Hashtable<ConnectionAnchor, ActivityFigure>();
		Hashtable<ConnectionAnchor,ActivityFigure> htTarget = new Hashtable<ConnectionAnchor, ActivityFigure>();
		for(Object figure:treeChart.getChildren()){
			if(figure instanceof ActivityFigure){
				ActivityFigure af = ((ActivityFigure) figure);
				if(!af.isVisible())continue;
				htSource.put(af.outAnchor, af);
				htTarget.put(af.inAnchor,af);
			}
		}
		for(Object figure:treeChart.getChildren()){
			if(figure instanceof PathFigure){
				PathFigure pf = (PathFigure) figure;
				if(!pf.isVisible())continue;
				ConnectionAnchor sourceNode = pf.getSourceAnchor();
				Point start = null;
				Point end = null;
				if(htSource.containsKey(sourceNode)){
					int x = htSource.get(sourceNode).getBounds().x;
					int y = htSource.get(sourceNode).getBounds().y;
					int width = htSource.get(sourceNode).getBounds().width;
					int height = htSource.get(sourceNode).getBounds().height;
					start = new Point(x+(width/2), y+height);
				}
				
				ConnectionAnchor targetNode = pf.getTargetAnchor();
				if(htTarget.containsKey(targetNode)){
					int x = htTarget.get(targetNode).getBounds().x;
					int y = htTarget.get(targetNode).getBounds().y;
					int width = htTarget.get(targetNode).getBounds().width;
					end = new Point(x+(width/2),y);
				}
				if(start != null && end != null){
					pf.getPoints().removeAllPoints();
					pf.getPoints().addPoint(start);
					pf.getPoints().addPoint(end);
				}
				
			}
		}
	}

	
	private void walkTree(DevideCond condition, ArrayList<ActivityFigure> childrenArray, Tree tree, int depth) {
		ArrayList<ActivityFigure> mychildrenArray = new ArrayList<ActivityFigure>();
		if (!tree.isLeaf()) {
			Iterator<Side> childIterator = tree.childIterator();
			while (childIterator.hasNext()) {
				Side edge = childIterator.next();
				walkTree(edge.getCondition(), mychildrenArray, edge.getChild(), depth+1);
			}
			ConditionFigure node =  new ConditionFigure();
			
			if (condition != null) {
				if(mychildrenArray == null || mychildrenArray.size() == 0){
					return;
				}
				if(isMessageEmpty(mychildrenArray.get(0).getAttribute())){
					return;
				}
				node.setCondition(condition.getRelation()+condition.getReadableValueString());
				node.setAttribute(condition.getColumnName());
				String[] temp = omissionString(mychildrenArray.get(0).getAttribute());
				node.setMessage(mychildrenArray.get(0).getAttribute());
				node.setDisplayMessage(temp[0]);
				if(temp[1].trim().length()>0){
					ToolTip nh = createToolTip(temp[1]);
					node.setToolTip(nh);
				}
			} else {
				if (mychildrenArray.size()>0) {
					if(isMessageEmpty(mychildrenArray.get(0).getAttribute())){
						return;
					}
					String[] temp = omissionString(mychildrenArray.get(0).getAttribute());
					node.setMessage(mychildrenArray.get(0).getAttribute());
					node.setDisplayMessage(temp[0]);
					if(!temp[1].equals("")){
						ToolTip nh = createToolTip(temp[1]);
						node.setToolTip(nh);
					}
				} else {
					node.setMessage(tree.getLabel());
					node.setDisplayMessage(tree.getLabel());
				}
			}
			node.setLayer(depth);
			if(node.getLayer()>defaultLayer){
				node.setVisible(false);
			}
			childrenArray.add(node);
			calculateCoordinate(node, mychildrenArray);
			setRootNode(node,depth);
			new Dnd(node);
			treeChart.add(node);
		} else {
			if(isMessageEmpty(tree.getLabel())){
				return;
			}
			if (condition != null) {
				DecisionFigure node = new DecisionFigure();
				node.setAttribute(condition.getColumnName());
				node.setCondition(condition.getRelation()+condition.getReadableValueString());
				node.setLabel(tree.getLabel());
				String[] temp = omissionString(tree.getLabel());
				node.setDisplayMessage(temp[0]);
				
				List<String> toolList = new ArrayList<String>();
				if(!temp[1].equals("")){
					toolList.add(tree.getLabel());
				}
				for(String s:tree.getStats()){
					toolList.add(s);
				}
				node.setMessage(tree.getLabel());
				for(String s:tree.getStats()){
					node.setMessage(node.getMessage()+"\n"+s);
				}
				
				NeuralHeader nh = createToolTip(toolList);
				node.setToolTip(nh);
				node.setLayer(depth);
				if(node.getLayer()>defaultLayer){
					node.setVisible(false);
				}
				childrenArray.add(node);
				new Dnd(node);
				if(nodeDepth<node.getLayer()){
					nodeDepth = node.getLayer();
				}
				lastNodeList.add(node);
				treeChart.add(node);
			} else {
				ConditionFigure node = new ConditionFigure();
				if (mychildrenArray.size()>0) {
					node.setMessage(mychildrenArray.get(0).getAttribute());
					String[] temp = omissionString(tree.getLabel());
					node.setDisplayMessage(temp[0]);
				} else {
					node.setMessage(tree.getLabel());
					node.setDisplayMessage(tree.getLabel());
				}
				node.setLayer(depth);
				if(node.getLayer()>defaultLayer){
					node.setVisible(false);
				}
				childrenArray.add(node);
			    calculateCoordinate(node, mychildrenArray);
				setRootNode(node,depth);
				new Dnd(node);
				treeChart.add(node);
			}
		}
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
	
	private void setRootNode(ConditionFigure node, int depth) {
		if(depth ==0){
			this.rootNode = node;
		}
	}

	private boolean isMessageEmpty(String message){
		if(message == null || message.trim().length()==0){
			return true;
		}
		return false;
	}
	
	private NeuralHeader createToolTip(List<String> list){
		NeuralHeader nf = new NeuralHeader();
		nf.setX(0);
		nf.setY(0);
		nf.setHeight(18+list.size()*12);
		StringBuffer sb = new StringBuffer();
		int maxWidth =0;
		for(String ss:list){
			sb.append(ss);
			sb.append("\n");
			if(ss.length()>maxWidth){
				maxWidth =ss.length();
			}
		}
		nf.setWidth(maxWidth*20);
		nf.setMessage(sb.toString());
		nf.setDisplayMessage(sb.toString());
		nf.setBounds();
		return nf;
	}
	private ToolTip createToolTip(String s){
		ToolTip nf = new ToolTip();
		nf.setX(0);
		nf.setY(0);
		nf.setHeight(20);
		if(s == null){
			s = "";
		}
		nf.setWidth(s.length()*12);
		nf.setMessage(s);
		nf.setDisplayMessage(s);
		nf.setBounds();
		return nf;
	}
	private void calculateCoordinate(ConditionFigure node, ArrayList<ActivityFigure> mychildrenArray) {
		for (ActivityFigure childNode: mychildrenArray) {
			PathFigure path = new PathFigure();
			path.setVisible(false);
		    path.setSourceAnchor(node.outAnchor);
		    path.setTargetAnchor(childNode.inAnchor);
		    path.add(new ConditionLabel(childNode.getCondition()),new MidpointLocator(path, 0));
		    path.setMessage(childNode.getCondition());
		    node.addChild(childNode);
		    node.addPath(path);
		    childNode.setParent(node);
		    path.setLayer(childNode.getLayer());
		    path.setParent(node);
			path.setChildNode(childNode);
			treeChart.add(path);
		    if(path.getLayer()>defaultLayer){
		    	path.setVisible(false);
			}else{
				path.setVisible(true);
			}
		}
	}
	
	class Dnd extends MouseMotionListener.Stub implements MouseListener {
		  public Dnd(ActivityFigure figure) {
		    figure.addMouseMotionListener(this);
		    figure.addMouseListener(this);
		  }

		  Point start;

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
		}

		  public void mouseDoubleClicked(MouseEvent e) {
			  ActivityFigure ff = ((ActivityFigure) e.getSource());
			  FreeformLayeredPane parent = (FreeformLayeredPane) ff.getParentContain();
			  
			  if(ff.getChildList().size()>0){
				  IFigure figure = ff.getChildList().get(0);
				  if(figure.isVisible()){
					  hiddenNode(ff);
				  }else{
					  showNode(ff);
				  }
			  }
			  lastVisiableList.clear();
			  calculateNodeOXYZ(null);
			  calculateLinkOXYZ();
			  parent.repaint();
			  parent.setFocusTraversable(true);
		  }

		  public void mousePressed(MouseEvent e) {
		    start = e.getLocation();
		  }

		  public void mouseDragged(MouseEvent e) {
			  ActivityFigure ff = ((ActivityFigure) e.getSource());
			  FreeformLayeredPane parent = (FreeformLayeredPane) ff.getParentContain();	
		    Point p = e.getLocation();
		    Dimension d = p.getDifference(start);
		    start = p;
		    Figure f = ((Figure) e.getSource());
		    f.setBounds(f.getBounds().getTranslated(d.width, d.height));
		    parent.repaint();
		    parent.setFocusTraversable(true);
		  }
		}
	
	private void showNode(ActivityFigure ff){
		for(ActivityFigure child:ff.getChildList()){
			child.setVisible(true);
		}
		for(PolylineConnection pf:ff.getPathList()){
			pf.setVisible(true);
		}
	}
	
	private void hiddenNode(ActivityFigure ff){
		if(ff.getChildList().size()==0)return;
		for(ActivityFigure child:ff.getChildList()){
			hiddenNode(child);
			child.setVisible(false);
		}
		for(PolylineConnection pf:ff.getPathList()){
			pf.setVisible(false);
		}
		
	}
	
	private String[] omissionString(String s){
		if(s == null){
			s = "";
		}
		String[] temp = new String[2];
		if(s.length()*12>140){
			temp[0] = s.substring(0,11)+"...";
			temp[1] = s;
		}else{
			temp[0] = s;
			temp[1] = "";
		}
		return temp;
	}
	
	public Image getImage(){
		Image img = new Image(null, getSaveWidth(), getSaveHeight());
		GC gc = new GC(img);
		Graphics graphics = new SWTGraphics(gc);
		List<IFigure> list = treeChart.getChildren();
		for (IFigure f : list) {
			if (f.isVisible()) {
				if (f instanceof PathFigure) {
					PathFigure tcpf = (PathFigure) f;
					if (tcpf.getMessage() != null
							&& !tcpf.getMessage().trim().equals("")) {
						int x = tcpf.getChildNode().getX();
						int y = tcpf.getChildNode().getY() - 20;
						graphics.drawText(tcpf.getMessage(), new Point(x, y));
					}
				}
				f.paint(graphics);
			}
		}
		graphics.dispose();
		gc.dispose();
		return img;
	}
}
