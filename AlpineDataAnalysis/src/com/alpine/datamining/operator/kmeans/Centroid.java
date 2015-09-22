/**
 * ClassName Centroid.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.kmeans;

import java.util.Collection;

import com.alpine.datamining.utility.Tools;


public class Centroid {
	
	private double[] centroid;

	private double[] centroidSum;
	private int numberOfAssigned = 0;
	
	public Centroid(int numberOfDimensions) {
		centroid = new double[numberOfDimensions];
		centroidSum = new double[numberOfDimensions];
	}

	public double[] getCentroid() {
		return centroid;
	}


	public void setCentroid(double[] coordinates) {
		this.centroid = coordinates;
	}
	public void assignData(double[] dataValues) {
		numberOfAssigned++;
		for (int i = 0; i < dataValues.length; i++) {
			centroidSum[i] += dataValues[i];
		}
	}
	

	public boolean finishAssign() {
		double[] newCentroid = new double[centroid.length];
		boolean stable = true;
		for (int i = 0; i < centroid.length; i++) {
			newCentroid[i] = centroidSum[i] / numberOfAssigned;
			stable &= Double.compare(newCentroid[i], centroid[i]) == 0;
		}
		centroid = newCentroid;
		centroidSum = new double[centroidSum.length];
		numberOfAssigned = 0;
		return stable;
	}

	/** 
	 * This method only returns the first 100 columns
	 */
	public String toString(Collection<String> dimensionNames) {
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (String dimName: dimensionNames) {
			buffer.append(dimName + ":\t");
			buffer.append((centroid[i]) + Tools.getLineSeparator());
			i++;
			if (i > 100)
				break;
		}
		return buffer.toString();
	}
	

}
