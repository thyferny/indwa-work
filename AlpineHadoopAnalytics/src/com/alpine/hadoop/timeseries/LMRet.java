/**
 * 
 * ClassName LMRet.java
 *
 * Version information: 1.00
 *
 * Date: Nov 5, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.timeseries;

import java.io.Serializable;
/**
* @author Shawn 
* 
*/
public class LMRet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] coefficient;
	private double[] ses;
	public double[] getCoefficient() {
		return coefficient;
	}
	public void setCoefficient(double[] coefficient) {
		this.coefficient = coefficient;
	}
	public double[] getSes() {
		return ses;
	}
	public void setSes(double[] ses) {
		this.ses = ses;
	}
}
