/**
 * ClassName GoodnessOfFit.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.evaluator;
/*
 * return accuracy, error, precision, recall, sensitivity, specificity f1 of fit for all values
 */
import java.util.ArrayList;

import com.alpine.datamining.utility.Tools;
/***
 * 
 * @author Eason
 *
 */
public class GoodnessOfFit extends EvaluatorResultObjectAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1094394054710383747L;
	private ArrayList<ValueGoodnessOfFit> goodness= null;
	private double accuracy = Double.NaN;
	private double error = Double.NaN;

	public GoodnessOfFit()
	{
		goodness = new ArrayList<ValueGoodnessOfFit>();
	}
	public GoodnessOfFit(ArrayList<ValueGoodnessOfFit> goodness)
	{
		this.goodness = goodness;
	}
	public ArrayList<ValueGoodnessOfFit> getGoodness() {
		return goodness;
	}
	public void setGoodness(ArrayList<ValueGoodnessOfFit> goodness) {
		this.goodness = goodness;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public double getError() {
		return error;
	}
	public void setError(double error) {
		this.error = error;
	}
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("accuracy: "+accuracy+Tools.getLineSeparator());
		buffer.append("error: "+error+Tools.getLineSeparator());
		for(ValueGoodnessOfFit valueGoodness : goodness)
		{
			buffer.append(valueGoodness.toString()).append(Tools.getLineSeparator());
		}
		return buffer.toString();
	}
}
