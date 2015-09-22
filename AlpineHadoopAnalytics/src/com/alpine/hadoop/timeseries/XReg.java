/**
 * 
 * ClassName XReg.java
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
public class XReg  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ncxreg;
	private int narma;
	private double[] xreg;
	public int getNcxreg() {
		return ncxreg;
	}
	public void setNcxreg(int ncxreg) {
		this.ncxreg = ncxreg;
	}
	public int getNarma() {
		return narma;
	}
	public void setNarma(int narma) {
		this.narma = narma;
	}
	public double[] getXreg() {
		return xreg;
	}
	public void setXreg(double[] xreg) {
		this.xreg = xreg;
	}
}
