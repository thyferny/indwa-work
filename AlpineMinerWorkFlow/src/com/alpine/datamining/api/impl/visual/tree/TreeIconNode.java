package com.alpine.datamining.api.impl.visual.tree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class TreeIconNode extends GraphicNode {

	private String attibuteName = "";
	private String condition = "";
	private String label = "";
	private final static BasicStroke Stroke = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	
	public TreeIconNode() {
		super();
		setWidth(75);
		setHeight(75);
	}
	
	public void setAttibuteName(String attibuteName) {
		this.attibuteName = attibuteName;
	}

	public String getAttibuteName() {
		return attibuteName;
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
	
	public void draw(Graphics2D g2d) {
		g2d.setStroke(Stroke);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(getX(), getY(), getWidth(), getHeight());
		g2d.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
		g2d.drawString(attibuteName, (float)(getX()+getWidth()/2-attibuteName.length()*5/2), (float)(getY()+15));
		g2d.setFont(new Font(Font.MONOSPACED,Font.PLAIN,11));
		g2d.drawString(condition, (float)(getX()+10), (float)(getY()+35));
		g2d.setColor(Color.RED);
		g2d.drawString(label, (float)(getX()+10), (float)(getY()+45));		
	}
	
}
