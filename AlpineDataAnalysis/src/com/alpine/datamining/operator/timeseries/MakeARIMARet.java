package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

public class MakeARIMARet implements Serializable{

	private static final long serialVersionUID = 1L;
	private double[] coefs;
	private double [] phi;
	private double[]theta;
	private double[]Delta;
	private double[]Z;
	private double[]a;
	private double [][]P; 
	private double[][] T;
	private double[][]V;
	private double h;
	private double[][]Pn;
	public double[] getCoefs() {
		return coefs;
	}
	public void setCoefs(double[] coefs) {
		this.coefs = coefs;
	}
	public double[] getPhi() {
		return phi;
	}
	public void setPhi(double[] phi) {
		this.phi = phi;
	}
	public double[] getTheta() {
		return theta;
	}
	public void setTheta(double[] theta) {
		this.theta = theta;
	}
	public double[] getDelta() {
		return Delta;
	}
	public void setDelta(double[] delta) {
		Delta = delta;
	}
	public double[] getZ() {
		return Z;
	}
	public void setZ(double[] z) {
		Z = z;
	}
	public double[] getA() {
		return a;
	}
	public void setA(double[] a) {
		this.a = a;
	}
	public double[][] getP() {
		return P;
	}
	public void setP(double[][] p) {
		P = p;
	}
	public double[][] getT() {
		return T;
	}
	public void setT(double[][] t) {
		T = t;
	}
	public double[][] getV() {
		return V;
	}
	public void setV(double[][] v) {
		V = v;
	}
	public double getH() {
		return h;
	}
	public void setH(double h) {
		this.h = h;
	}
	public double[][] getPn() {
		return Pn;
	}
	public void setPn(double[][] pn) {
		Pn = pn;
	}

}
