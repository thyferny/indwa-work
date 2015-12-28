
package com.alpine.datamining.operator.tree.cart;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;


public abstract class AbstractStandard implements Standard {
	
    // data for incremental calculation
    
    protected double leftWeight;
    protected double rightWeight;
    protected double totalWeight;
    protected double[] totalLabelWeights;
    protected double[] leftLabelWeights;
    protected double[] rightLabelWeights;
    protected Column labelColumn;

	public boolean supportsIncrementalCalculation() {
    	return false;
    }

	public void swapData(Data data) {
		double weight = 1;
		int label = (int)data.getValue(labelColumn);
		leftWeight += weight;
		rightWeight -= weight;
		leftLabelWeights[label] += weight;
		rightLabelWeights[label] -= weight;
	}
	
	public double getIncrementalStadard() {
		return 0;
	}

}
