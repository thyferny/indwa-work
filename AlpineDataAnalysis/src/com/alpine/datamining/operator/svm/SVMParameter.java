
package com.alpine.datamining.operator.svm;

import com.alpine.datamining.operator.Parameter;

public class SVMParameter implements Parameter {
	private int kernelType = 1;
	private int degree = 2;
	private double gamma = 0.1;
	private double eta = 0.05;
	private double nu = 0.001;
	public int getKernelType() {
		return kernelType;
	}
	public void setKernelType(int kernelType) {
		this.kernelType = kernelType;
	}
	public int getDegree() {
		return degree;
	}
	public void setDegree(int degree) {
		this.degree = degree;
	}
	public double getGamma() {
		return gamma;
	}
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
	public double getEta() {
		return eta;
	}
	public void setEta(double eta) {
		this.eta = eta;
	}
	public double getNu() {
		return nu;
	}
	public void setNu(double nu) {
		this.nu = nu;
	}
}
