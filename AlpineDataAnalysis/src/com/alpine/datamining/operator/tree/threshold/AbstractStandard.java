/**
 * ClassName DBGiniIndexStandard
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

/**
 * This DBGiniIndexStandard class can be used for the incremental calculation of benefits.
 * 
 */
public abstract class AbstractStandard implements Standard {
	
    // data for incremental calculation
    
    protected double leftWeight;
    protected double rightWeight;
    protected double totalWeight;
    protected double[] totalLabelWeights;
    protected double[] leftLabelWeights;
    protected double[] rightLabelWeights;
    protected Column labelColumn;
//    protected Column weightColumn;

	public boolean supportsIncrementalCalculation() {
    	return false;
    }

	public void startIncrementalCalculation(DataSet dataSet, ICount calculator) {
		rightLabelWeights = calculator.getLabelCounts(dataSet);
		leftLabelWeights = new double[rightLabelWeights.length];
		totalLabelWeights = new double[rightLabelWeights.length];
		System.arraycopy(rightLabelWeights, 0, totalLabelWeights, 0, rightLabelWeights.length);
		leftWeight = 0;
		rightWeight = calculator.getTotalCount(totalLabelWeights);
		totalWeight = rightWeight;
		
		labelColumn = dataSet.getColumns().getLabel();
//		weightColumn = dataSet.getColumns().getWeight();
	}

	public void swapData(Data data) {
		double weight = 1;
//		if (weightColumn != null) {
//			weight = data.getValue(weightColumn);
//		}
		int label = 0;
		if (labelColumn.isNumerical() && labelColumn.isCategory()){
    		String valueString = null;
    		double labelValue = data.getValue(labelColumn); 
        	valueString = String.valueOf((int)labelValue);
        	label = labelColumn.getMapping().mapString(valueString);
		}else{
			label = (int)data.getValue(labelColumn);
		}
		leftWeight += weight;
		rightWeight -= weight;
		leftLabelWeights[label] += weight;
		rightLabelWeights[label] -= weight;
	}
	
	public double getIncrementalScore() {
		return 0;
	}
}
