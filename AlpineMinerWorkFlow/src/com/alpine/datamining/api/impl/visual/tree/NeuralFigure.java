/**
 * NeuralFigure.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import com.alpine.datamining.api.impl.visual.resource.VisualUtility;

/**
 * @author Jimmy
 *
 */
public class NeuralFigure extends ActivityFigure {
	
	public final static int NodeWidth = 38;
	public final static int NodeHeight = 48;
	
	
	public NeuralFigure(){
		inAnchor = new FixedAnchor(this);
	    inAnchor.place = new Point(0, 1);
	    targetAnchors.put("in_proc", inAnchor);
	    outAnchor = new FixedAnchor(this);
	    outAnchor.place = new Point(2, 1);
	    sourceAnchors.put("out_proc", outAnchor);
	    setWidth(NodeWidth);
	    setHeight(NodeHeight);
	}
	
	public void paintFigure(Graphics g) {
	    Rectangle r = bounds;
	    if(getBackGroundColorNf() == null){
	    	g.drawImage(VisualUtility.getnnNodeImage(), r.x, r.y);
	    }else{
	    	g.drawImage(VisualUtility.getnnEndNodeImage(), r.x, r.y);
	    }
	}
	
	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	private List<String> inputNodeNames = new ArrayList<String>();
	public List<String> getInputNodeNames() {
		return inputNodeNames;
	}

	public void setInputNodeNames(List<String> inputNodeNames) {
		this.inputNodeNames = inputNodeNames;
	}
	
	public void addInputNodeName(String name){
		this.inputNodeNames.add(name);
	}

	public List<String> getInputNodeValues() {
		return inputNodeValues;
	}

	public void setInputNodeValues(List<String> inputNodeValues) {
		this.inputNodeValues = inputNodeValues;
	}
	
	public void addInputNodeValue(String value){
		this.inputNodeValues.add(value);
	}

	private  List<String> inputNodeValues = new ArrayList<String>();
	
	private Color backGroundColor;

	public Color getBackGroundColorNf() {
		return backGroundColor;
	}

	public void setBackGroundColorNf(Color backGroundColor) {
		this.backGroundColor = backGroundColor;
	}
}
