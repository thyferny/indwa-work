
package com.alpine.datamining.operator.attributeanalysisresult;

import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;


public class CorrelationAnalysisResult extends OutputObject {

	private static final long serialVersionUID = 1L;

	
	private String x;
	
	private String y;
	
	private double r;

	
	public CorrelationAnalysisResult(String x, String y, double r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	
	public String getX() {
		return x;
	}

	
	public void setX(String x) {
		this.x = x;
	}

	
	public String getY() {
		return y;
	}

	
	public void setY(String y) {
		this.y = y;
	}

	
	public double getR() {
		return r;
	}

	
	public void setR(double r) {
		this.r = r;
	}

	public String toString() {
		String str = x + ", " + y + " "+AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.CORRELATION_COEFFICIENT,AlpineThreadLocal.getLocale())
				+ " : " + r + Tools.getLineSeparator();
		return str;
	}
}
