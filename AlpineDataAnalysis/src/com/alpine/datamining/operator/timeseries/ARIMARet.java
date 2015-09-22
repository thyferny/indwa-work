package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

//import com.alpine.datamining.operator.timeseries.ARIMAR.makeARIMARes;

public class ARIMARet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[]coef;
	private double sigma2 ;
	private double[] varCoef;
	private double mask ;
	private double loglik;
	private double aic;
	private int []arma;
	private double[] residuals;
	private double[] x;
	private double code;
	private int  nCond;
	private MakeARIMARet model;
	public double[] getCoef() {
		return coef;
	}
	public void setCoef(double[] coef) {
		this.coef = coef;
	}
	public double getSigma2() {
		return sigma2;
	}
	public void setSigma2(double sigma2) {
		this.sigma2 = sigma2;
	}
	public double[] getVarCoef() {
		return varCoef;
	}
	public void setVarCoef(double[] varCoef) {
		this.varCoef = varCoef;
	}
	public double getMask() {
		return mask;
	}
	public void setMask(double mask) {
		this.mask = mask;
	}
	public double getLoglik() {
		return loglik;
	}
	public void setLoglik(double loglik) {
		this.loglik = loglik;
	}
	public double getAic() {
		return aic;
	}
	public void setAic(double aic) {
		this.aic = aic;
	}
	public int[] getArma() {
		return arma;
	}
	public void setArma(int[] arma) {
		this.arma = arma;
	}
	public double[] getResiduals() {
		return residuals;
	}
	public void setResiduals(double[] residuals) {
		this.residuals = residuals;
	}
	public double[] getX() {
		return x;
	}
	public void setX(double[] x) {
		this.x = x;
	}
	public double getCode() {
		return code;
	}
	public void setCode(double code) {
		this.code = code;
	}
	public int getnCond() {
		return nCond;
	}
	public void setnCond(int nCond) {
		this.nCond = nCond;
	}
	public MakeARIMARet getModel() {
		return model;
	}
	public void setModel(MakeARIMARet model) {
		this.model = model;
	}

}
