/**
 * 
 * ClassName CSSRet.java
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
public class CSSRet implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] sResid;
	private double value;
	private boolean useResid;
	public double[] getsResid() {
		return sResid;
	}
	public void setsResid(double[] sResid) {
		this.sResid = sResid;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public boolean isUseResid() {
		return useResid;
	}
	public void setUseResid(boolean useResid) {
		this.useResid = useResid;
	}
}
