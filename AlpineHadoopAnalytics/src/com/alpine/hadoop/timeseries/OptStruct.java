
/**
 * 
 * ClassName OptStruct.java
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
public class OptStruct  implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] ndeps;   /* tolerances for numerical derivatives */
	private double fnscale;  /* scaling for objective */
	private double[] parscale;/* scaling for parameters */
	private int usebounds;
	private double[] lower;
	private double[]upper;
	private int[] arma;
	private int ncond;
	public double[] getNdeps() {
		return ndeps;
	}
	public void setNdeps(double[] ndeps) {
		this.ndeps = ndeps;
	}
	public double getFnscale() {
		return fnscale;
	}
	public void setFnscale(double fnscale) {
		this.fnscale = fnscale;
	}
	public double[] getParscale() {
		return parscale;
	}
	public void setParscale(double[] parscale) {
		this.parscale = parscale;
	}
	public int getUsebounds() {
		return usebounds;
	}
	public void setUsebounds(int usebounds) {
		this.usebounds = usebounds;
	}
	public double[] getLower() {
		return lower;
	}
	public void setLower(double[] lower) {
		this.lower = lower;
	}
	public double[] getUpper() {
		return upper;
	}
	public void setUpper(double[] upper) {
		this.upper = upper;
	}
	public int[] getArma() {
		return arma;
	}
	public void setArma(int[] arma) {
		this.arma = arma;
	}
	public int getNcond() {
		return ncond;
	}
	public void setNcond(int ncond) {
		this.ncond = ncond;
	}
}
