package com.alpine.datamining.api.impl.visual.tree;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.RotatableDecoration;

import com.alpine.datamining.api.impl.visual.resource.VisualUtility;

public class PathFigure extends PolylineConnection {

	public PathFigure() {
		PolylineDecoration dec = new PolylineDecoration();
		dec.setAlpha(90);
	    setTargetDecoration(dec);
		setAlpha(60);
		setLineWidth(2);
		setFont(VisualUtility.getTreeFont());
		setForegroundColor(ColorConstants.darkBlue);
	}
	private int layer =0;
	public int getLayer() {
		return layer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}
	public ActivityFigure getParentNode() {
		return parentNode;
	}
	public void setParentNode(ActivityFigure parentNode) {
		this.parentNode = parentNode;
	}
	public ActivityFigure getChildNode() {
		return childNode;
	}
	public void setChildNode(ActivityFigure childNode) {
		this.childNode = childNode;
	}
	public void setDecoration(RotatableDecoration dec){
		setTargetDecoration(dec);
	}
	private ActivityFigure parentNode;
	private ActivityFigure childNode;
	private String message;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
