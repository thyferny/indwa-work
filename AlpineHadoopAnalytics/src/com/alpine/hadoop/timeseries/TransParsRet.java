/**
 * 
 * ClassName TransParsRet.java
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
public class TransParsRet  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] sPhi;
	private double[] sTheta;
	public double[] getsPhi() {
		return sPhi;
	}
	public void setsPhi(double[] sPhi) {
		this.sPhi = sPhi;
	}
	public double[] getsTheta() {
		return sTheta;
	}
	public void setsTheta(double[] sTheta) {
		this.sTheta = sTheta;
	}
}
