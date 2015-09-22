/**
 * NeuralToolTip.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual.tree;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Jimmy
 *
 */
public class ToolTip extends ActivityFigure {

	public ToolTip() {
	    inAnchor = new FixedAnchor(this);
	    inAnchor.place = new Point(1, 0);
	    targetAnchors.put("in_proc", inAnchor);
	    outAnchor = new FixedAnchor(this);
	    outAnchor.place = new Point(1, 2);
	    sourceAnchors.put("out_proc", outAnchor);
	}
	
	public void paintFigure(Graphics g) {
	    Rectangle r = bounds;
	    g.setBackgroundColor(ColorConstants.white);
//	    g.fillRectangle(r.x, r.y, r.width - 1, r.height - 1);
	    g.drawText(getDisplayMessage(), r.x +5, r.y +5);
	}
}
