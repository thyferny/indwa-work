/**
 * ClassName Count
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SplitDataSet;
import com.alpine.datamining.utility.Tools;

/**
 * counts frequencies.
 * 
 */
public class Count implements ICount{

    public Count() {}
    
    public double[][] getNumericalCounts(DataSet dataSet, Column column, double splitValue) {
    	Column label = dataSet.getColumns().getLabel();
    	int numberOfLabels = label.getMapping().size();
    	
    	double[][] weightCounts = new double[2][numberOfLabels];
    	
    	for (Data data : dataSet) {
    		int labelIndex = 0;
    		if (label.isNumerical() && label.isCategory()){
        		String valueString = null;
        		double labelValue = data.getValue(label); 
	        	valueString = String.valueOf((int)labelValue);
	        	labelIndex = label.getMapping().mapString(valueString);
    		}else{
    			labelIndex = (int)data.getValue(label);
    		}
    		double value = data.getValue(column);
    		
    		double weight = 1.0d;
    		
    		if (Tools.isLessEqual(value, splitValue)) {
    			weightCounts[0][labelIndex] += weight;	
    		} else {
    			weightCounts[1][labelIndex] += weight;
    		}    		
    	}
    	
    	return weightCounts;
    }
    
    public double[][] getNominalCounts(DataSet dataSet, Column column) {
    	Column label = dataSet.getColumns().getLabel();
    	int numberOfLabels = label.getMapping().size();
    	int numberOfValues = column.getMapping().size();
    	
    	double[][] weightCounts = new double[numberOfValues][numberOfLabels];
    	
    	for (Data data : dataSet) {
    		int labelIndex = 0;
    		if (label.isNumerical() && label.isCategory()){
        		String valueString = null;
        		double labelValue = data.getValue(label); 
	        	valueString = String.valueOf((int)labelValue);
	        	labelIndex = label.getMapping().mapString(valueString);
    		}else{
    			labelIndex = (int)data.getValue(label);
    		}
    		double value = data.getValue(column);
    		if (!Double.isNaN(value)) {
	    		int valueIndex = (int)data.getValue(column);
	    		double weight = 1.0d;
	    		weightCounts[valueIndex][labelIndex] += weight;
    		}
    	}
    	
    	return weightCounts;
    }
    
    /** Returns an array of the size of the partitions.  */
    public double[] getPartitionCount(SplitDataSet splitted) {
        double[] weights = new double[splitted.getNumberOfSubsets()];
        for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
            splitted.selectSingleSubset(i);
            for (Data e : splitted) {
                double weight = 1.0d;
                weights[i] += weight;
            }
        }
        return weights;
    }
    
    /** Returns an array of size of the number of different label
     *  values.*/
    public double[] getLabelCounts(DataSet dataSet) {
        Column label = dataSet.getColumns().getLabel();
        double[] weights = new double[label.getMapping().size()];
        for (Data e : dataSet) {
    		int labelIndex = 0;
    		if (label.isNumerical() && label.isCategory()){
        		String valueString = null;
        		double labelValue = e.getValue(label); 
	        	valueString = String.valueOf((int)labelValue);
	        	labelIndex = label.getMapping().mapString(valueString);
    		}else{
    			labelIndex = (int)e.getValue(label);
    		}
            double weight = 1.0d;
            weights[labelIndex] += weight;
        }
        return weights;
    }
    
    /** Returns the sum of the given weights. */
    public double getTotalCount(double[] weights) {
        double sum = 0.0d;
        for (double w : weights)
            sum += w;
        return sum;
    }
}
