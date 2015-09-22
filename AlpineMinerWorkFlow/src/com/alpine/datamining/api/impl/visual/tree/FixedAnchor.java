package com.alpine.datamining.api.impl.visual.tree;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

public class FixedAnchor extends AbstractConnectionAnchor {
	Point place;

	public FixedAnchor(IFigure owner) {
	    super(owner);
	}

	public Point getLocation(Point loc) {
	    Rectangle r = getOwner().getBounds();
	    int x = r.x + place.x * r.width / 2;
	    int y = r.y + place.y * r.height / 2;
	    Point p = new PrecisionPoint(x, y);
	    getOwner().translateToAbsolute(p);
	    return p;
	}
}
