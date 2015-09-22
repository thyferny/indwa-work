/**
 * ClassName ClusterModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.kmeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;


public class CentroidClusterModel extends ClusterModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4647820502405349141L;
	private Collection<String> dimensionNames;
	private ArrayList<Centroid> centroids;
//	private DistanceMeasure distanceMeasure;
	
	public CentroidClusterModel(int k, Collection<String> dimensionNames/*, DistanceMeasure distanceMeasure*/) {
		super(k);
//		this.distanceMeasure = distanceMeasure;
		this.dimensionNames = dimensionNames;
		centroids = new ArrayList<Centroid>(k);
		for (int i = 0; i < k; i++) {
			centroids.add(new Centroid(dimensionNames.size()));
		}
	}
	public CentroidClusterModel(int k, Columns columns) {
		super(k);
		List<String> dimensionNames = new LinkedList<String>();
		for (Column column: columns)
			dimensionNames.add(column.getName());
		this.dimensionNames = dimensionNames;
		centroids = new ArrayList<Centroid>(k);
		for (int i = 0; i < k; i++) {
			centroids.add(new Centroid(dimensionNames.size()));
		}
	}

	/* This model does not need ids*/
	public void checkCapabilities(DataSet dataSet) throws OperatorException {
		
	}
	public String[] getColumnNames() {
		return dimensionNames.toArray(new String[0]);
	}

	public double[] getCentroidCoordinates(int i) {
		return centroids.get(i).getCentroid();
	}

	public Centroid getCentroid(int i) {
		return centroids.get(i);
	}
	
	public void assignData(int i, double[] asDoubleArray) {
		centroids.get(i).assignData(asDoubleArray);
	}

	public boolean finishAssign() {
		boolean stable = true;
		for (Centroid centroid: centroids)
			stable &= centroid.finishAssign();
		return stable;
	}

	public String getExtension() {
		return "ccm";
	}

	public String getFileDescription() {
		return "Centroid based cluster model";
	}
}
