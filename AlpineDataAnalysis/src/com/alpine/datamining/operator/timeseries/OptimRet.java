package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

public class OptimRet  implements Serializable{
    	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] par;
	private double value;
	private int[]counts;
	private int conv;

	public double[] getPar() {
		return par;
	}

	public void setPar(double[] par) {
		this.par = par;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int[] getCounts() {
		return counts;
	}

	public void setCounts(int[] counts) {
		this.counts = counts;
	}

	public int getConv() {
		return conv;
	}

	public void setConv(int conv) {
		this.conv = conv;
	}
}
