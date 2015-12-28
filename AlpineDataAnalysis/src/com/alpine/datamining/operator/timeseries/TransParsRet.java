package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

public class TransParsRet  implements Serializable{
	
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
