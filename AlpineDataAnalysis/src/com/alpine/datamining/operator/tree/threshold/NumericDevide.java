
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SortedDataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.Tools;




public class NumericDevide {
	
    private Standard standard;
    double bestSplitBenefit = Double.NEGATIVE_INFINITY;
    
    
    public NumericDevide(Standard standard) {
        this.standard = standard;
    }
    
    public double getBestSplit(DataSet inputSet, Column column) throws OperatorException {
        SortedDataSet dataSet = new SortedDataSet((DataSet)inputSet.clone(), column, SortedDataSet.INCREASING);
        double bestSplit = Double.NaN;
        double lastValue = Double.NaN;
        bestSplitBenefit = Double.NEGATIVE_INFINITY;
      
        Data lastData = null;
        if (standard.supportsIncrementalCalculation()) {
        	standard.startIncrementalCalculation(dataSet, new Count());
        }

        int i = 0;
        for (Data e : dataSet) {
    		i++;

        	double currentValue = e.getValue(column);
        	if ( i == 1)
        	{
        		lastValue = currentValue;
        		lastData = e;
        		continue;
        	}
        	// skip equal values
    		if (this.standard.supportsIncrementalCalculation()) {
    			if (lastData != null) 
    				this.standard.swapData(lastData);
    			lastData = e;
    		}

        	if (Tools.isEqual(currentValue, lastValue)) {
        		continue;
        	}
        	
    		if (this.standard.supportsIncrementalCalculation()) {
	    			double benefit = this.standard.getIncrementalScore();

	    			if (benefit > bestSplitBenefit) {
	        			bestSplitBenefit = benefit;
	        			bestSplit = (lastValue + currentValue) / 2.0d;
	    			}
    		} else 
    		{
        		double splitValue = (lastValue + currentValue) / 2.0d;
        		double benefit = standard.getNumericalBenefit(dataSet, column, splitValue);
	        	if (benefit > bestSplitBenefit) {
        			bestSplitBenefit = benefit;
        			bestSplit = splitValue;
        		}
        	}
            lastValue = currentValue;
        }
        return bestSplit;
    }

	public double getBestSplitScore() {
		return bestSplitBenefit;
	}

	public void setBestSplitScore(double bestSplitBenefit) {
		this.bestSplitBenefit = bestSplitBenefit;
	}
}
