/**
 * ClassName CorrelationAnalysisResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-16
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.attributeanalysisresult;

import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;

/**
 * @author Eason
 * 
 */
public class CorrelationAnalysisResult extends OutputObject {

	private static final long serialVersionUID = 1L;

	/**
	 * column name X
	 */
	private String x;
	/**
	 * column name Y
	 */
	private String y;
	/**
	 * correlation result r
	 */
	private double r;

	/**
	 * @param x2
	 * @param y2
	 * @param r2
	 */
	public CorrelationAnalysisResult(String x, String y, double r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	/**
	 * @return the x
	 */
	public String getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(String x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public String getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(String y) {
		this.y = y;
	}

	/**
	 * @return the r
	 */
	public double getR() {
		return r;
	}

	/**
	 * @param r
	 *            the r to set
	 */
	public void setR(double r) {
		this.r = r;
	}

	public String toString() {
		String str = x + ", " + y + " "+AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.CORRELATION_COEFFICIENT,AlpineThreadLocal.getLocale())
				+ " : " + r + Tools.getLineSeparator();
		return str;
	}
}
