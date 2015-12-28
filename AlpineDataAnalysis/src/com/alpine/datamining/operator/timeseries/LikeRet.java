package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

public class LikeRet  implements Serializable{
	
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
