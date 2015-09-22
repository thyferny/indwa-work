package com.alpine.datamining.api.impl.visual.tree;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.alpine.datamining.api.impl.visual.resource.VisualUtility;



public class DecisionFigure extends ActivityFigure {
	
//	String msg_line2;
//	String msg_line3;
	public final static int NodeWidth = 100;
	public final static int NodeHeight = 43;

	public DecisionFigure() {
	    inAnchor = new FixedAnchor(this);
	    inAnchor.place = new Point(1, 0);
	    targetAnchors.put("in_proc", inAnchor);
	    outAnchor = new FixedAnchor(this);
	    outAnchor.place = new Point(1, 2);
	    sourceAnchors.put("out_proc", outAnchor);
	    setWidth(NodeWidth);
	    setHeight(NodeHeight);
	}

	public void paintFigure(Graphics g) {
	    Rectangle r = bounds;
	    
	    g.drawImage(VisualUtility.getLeafImage(), r.x, r.y);

	    if(getDisplayMessage() != null){
	    	g.setForegroundColor(ColorConstants.black);
	    	g.setFont(VisualUtility.getTreeFont());
	    	g.drawText(VisualUtility.resizeLabel(getDisplayMessage()), r.x + r.width/6, r.y + r.height /4);
	    }
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#addChild(com.alpine.datamining.api.impl.visual.tree.DecisionFigure)
	 */
	@Override
	public void addChild(ActivityFigure df) {
		super.addChild(df);
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#getChildList()
	 */
	@Override
	public List<ActivityFigure> getChildList() {
		return super.getChildList();
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#setLayer(int)
	 */
	@Override
	public void setLayer(int layer) {
		super.setLayer(layer);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#getLayer()
	 */
	@Override
	public int getLayer() {
		return super.getLayer();
	}
	

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#getPath()
	 */
	@Override
	public List<PolylineConnection> getPathList() {
		return super.getPathList();
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#setPath(org.eclipse.draw2d.PolylineConnection)
	 */
	@Override
	public void addPath(PolylineConnection path) {
		super.addPath(path);
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#isDrawFigure()
	 */
	@Override
	public boolean isDrawFigure() {
		return super.isDrawFigure();
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.tree.ActivityFigure#setDrawFigure(boolean)
	 */
	@Override
	public void setDrawFigure(boolean drawFigure) {
		super.setDrawFigure(drawFigure);
	}
	
}
