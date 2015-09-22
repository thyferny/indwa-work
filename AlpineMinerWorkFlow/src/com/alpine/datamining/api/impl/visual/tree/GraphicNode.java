package com.alpine.datamining.api.impl.visual.tree;


public class GraphicNode {

	private int x; 			// the x coordinate of the icon on the Process Flow Panel.
	private int y; 			// the y coordinate of the icon on the Process Flow Panel.
	private int w;			// width of the icon.
	private int h;			// height of the icon.

	/**
	 * set the top-left corner x-position of the Operator node.
	 * @param x
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * 
	 * @return the top-left corner x-position of the Operator node.
	 */
	public int getX() {
		return x;
	}

	/**
	 * set the top-left corner y-position of the Operator node.
	 * @param y
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * 
	 * @return the top-left corner y-position of the Operator node.
	 */
	public int getY() {
		return y;
	}

	/**
	 * set the center x-position of the Operator NNNode
	 * it is transformed into top-left x-position
	 * @param x
	 */
	public void setCenterX(int x) {
		// if x is smaller than the half-width, x = 0, so that it won't be un-reachable.
		if (x<w/2) {
			setX(0);
		} else {
			setX(x-(w/2));
		}
	}

	/**
	 * 
	 * @return the center x-position.
	 */
	public int getCenterX() {
		return x+w/2;
	}

	/**
	 * set the center y-position of the Operator NNNode
	 * it is transformed into top-left y-position
	 * @param y
	 */
	public void setCenterY(int y) {
		// if y is smaller than the half-height, y = 0, so that it won't be un-reachable.
		if (y<h/2) {
			setY(0);
		} else {
			setY(y-(h/2));
		}
	}

	/**
	 * 
	 * @return the center y-position.
	 */
	public int getCenterY() {
		return y+h/2;
	}

	/**
	 * 
	 * @return the width of the Operation NNNode icon
	 */
	public int getWidth() {
		return w;
	}

	/**
	 * 
	 * @return the height of the Operator NNNode icon
	 */
	public int getHeight() {
		return h;
	}

	public void setHeight(int h) {
		this.h = h;
	}
	
	public void setWidth(int w) {
		this.w = w;
	}
}
