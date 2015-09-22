package com.alpine.datamining.api.impl.visual.widgets;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import com.alpine.datamining.api.impl.visual.tree.ActivityFigure;

public class ScatterMatrixToolTip extends ActivityFigure {

	public ScatterMatrixToolTip() {

	}

	public void paintFigure(Graphics g) {
	    Rectangle r = bounds;
	    g.setBackgroundColor(ColorConstants.white);
	    g.drawText(getDisplayMessage(), r.x +5, r.y +5);
	}
	
}
