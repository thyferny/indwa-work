/**
 * 
 * ClassName LikeRet.java
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
public class LikeRet  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] nres;
	private double[] sResid;
	public double[] getNres() {
		return nres;
	}
	public void setNres(double[] nres) {
		this.nres = nres;
	}
	public double[] getsResid() {
		return sResid;
	}
	public void setsResid(double[] sResid) {
		this.sResid = sResid;
	}

}
