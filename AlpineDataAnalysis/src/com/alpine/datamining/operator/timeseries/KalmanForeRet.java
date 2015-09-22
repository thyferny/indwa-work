package com.alpine.datamining.operator.timeseries;

import java.io.Serializable;

public class KalmanForeRet implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] forecasts;
	private double[] se;
	public double[] getForecasts() {
		return forecasts;
	}
	public void setForecasts(double[] forecasts) {
		this.forecasts = forecasts;
	}
	public double[] getSe() {
		return se;
	}
	public void setSe(double[] se) {
		this.se = se;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
