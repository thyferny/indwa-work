
package com.alpine.datamining.operator.evaluator;

import com.alpine.datamining.operator.OutputObject;

public class ValueGoodnessOfFit extends OutputObject {
	
	private static final long serialVersionUID = -5087073760994046876L;
	private String value = null;
	private double recall = 0;
	private double precision = 0;
	private double f1 = 0;
	private double specificity = 0;
	private double sensitivity = 0;
	public ValueGoodnessOfFit(String value, double recall,double precision,double f1,double specificity,double sensitivity)
	{
		this.value = value;
		this.recall = recall;
		this.precision = precision;
		this.f1 = f1;
		this.specificity = specificity;
		this.sensitivity = sensitivity;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getF1() {
		return f1;
	}
	public void setF1(double f1) {
		this.f1 = f1;
	}

	public double getSpecificity() {
		return specificity;
	}
	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}
	public double getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("stats(").append(value).append("): ")
		.append(" recall:").append(recall)
		.append(" precision:").append(precision)
		.append(" f1:").append(f1)
		.append(" specificity: ").append(specificity)
		.append(" sensitivity: ").append(sensitivity);
		return buffer.toString();
	}
}
