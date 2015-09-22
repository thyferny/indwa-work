package com.alpine.datamining.api.impl.visual.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public abstract class ActivityFigure extends Figure {
	FixedAnchor inAnchor, outAnchor;
	//Rectangle r = new Rectangle();
	int x;
	int y;
	int width;
	int height;
	
	private String attribute = "";
	private String condition = "";
	private String label = "";
	
	
	Hashtable targetAnchors = new Hashtable();
	Hashtable sourceAnchors = new Hashtable();

	String displayMessage = new String();

	public void setDisplayMessage(String msg) {
		displayMessage = msg;
	    repaint();
	}
	
	public String getDisplayMessage(){
		return displayMessage;
	}
	
	String message = "";
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ConnectionAnchor ConnectionAnchorAt(Point p) {
	    ConnectionAnchor closest = null;
	    long min = Long.MAX_VALUE;
	    Hashtable conn = getSourceConnectionAnchors();
	    conn.putAll(getTargetConnectionAnchors());
	    Enumeration e = conn.elements();
	    while (e.hasMoreElements()) {
	    	ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
	    	Point p2 = c.getLocation(null);
	    	long d = p.getDistance2(p2);
	    	if (d < min) {
	    		min = d;
	    		closest = c;
	    	}
	    }
	    return closest;
	}

	public ConnectionAnchor getSourceConnectionAnchor(String name) {
	    return (ConnectionAnchor) sourceAnchors.get(name);
	}

	public ConnectionAnchor getTargetConnectionAnchor(String name) {
	    return (ConnectionAnchor) targetAnchors.get(name);
	}

	public String getSourceAnchorName(ConnectionAnchor c) {
	    Enumeration e = sourceAnchors.keys();
	    String name;
	    while (e.hasMoreElements()) {
	    	name = (String) e.nextElement();
	    	if (sourceAnchors.get(name).equals(c))
	    		return name;
	    }
	    return null;
	}

	public String getTargetAnchorName(ConnectionAnchor c) {
	    Enumeration e = targetAnchors.keys();
	    String name;
	    while (e.hasMoreElements()) {
	    	name = (String) e.nextElement();
	    	if (targetAnchors.get(name).equals(c))
	    		return name;
	    }
	    return null;
	}

	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
	    long min = Long.MAX_VALUE;
	    Enumeration e = getSourceConnectionAnchors().elements();
	    while (e.hasMoreElements()) {
	    	ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
	    	Point p2 = c.getLocation(null);
	    	long d = p.getDistance2(p2);
	    	if (d < min) {
	    		min = d;
	    		closest = c;
	    	}
	    }
	    return closest;
	}

	public Hashtable getSourceConnectionAnchors() {
	    return sourceAnchors;
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
	    ConnectionAnchor closest = null;
	    long min = Long.MAX_VALUE;
	    Enumeration e = getTargetConnectionAnchors().elements();
	    while (e.hasMoreElements()) {
	    	ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
	    	Point p2 = c.getLocation(null);
	    	long d = p.getDistance2(p2);
	    	if (d < min) {
	    		min = d;
	    		closest = c;
	    	}
	    }
	    return closest;
	}

	public Hashtable getTargetConnectionAnchors() {
	    return targetAnchors;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getCondition() {
		return condition;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setCenterX(int x) {
		if (x<width/2) {
			setX(0);
		} else {
			setX(x-(width/2));
		}
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setCenterY(int y) {
		if (y<height/2) {
			setY(0);
		} else {
			setY(y-(height/2));
		}
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCenterX() {
		return x+width/2;
	}

	public int getCenterY() {
		return y+height/2;
	}

	public void setBounds() {
		setBounds(new Rectangle(getX(),getY(),getWidth(),getHeight()));
	}
	

	private int layer =0;

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}
	
	private ActivityFigure parent;
//	public ActivityFigure getParent() {
//		return parent;
//	}

	public void setParent(ActivityFigure parent) {
		this.parent = parent;
	}

	private List<ActivityFigure> childList = new ArrayList<ActivityFigure>();
	
	public void addChild(ActivityFigure df){
		childList.add(df);
	}
	public List<ActivityFigure> getChildList(){
		return childList;
	}
	
	private List<PolylineConnection> pathList = new ArrayList<PolylineConnection>();

	public List<PolylineConnection> getPathList() {
		return pathList;
	}

	public void addPath(PolylineConnection path) {
		pathList.add(path);
	}
	
	private FreeformFigure parentContain;

	public FreeformFigure getParentContain() {
		return parentContain;
	}

	public void setParentContain(FreeformFigure parentContain) {
		this.parentContain = parentContain;
	}
	
	private boolean drawFigure = false;

	public boolean isDrawFigure() {
		return drawFigure;
	}

	public void setDrawFigure(boolean drawFigure) {
		this.drawFigure = drawFigure;
	}

	private boolean savePdf = true;

	public boolean isSavePdf() {
		return savePdf;
	}

	public void setSavePdf(boolean savePdf) {
		this.savePdf = savePdf;
	}
	
}
