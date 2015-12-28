
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;


public class InformationGainStandard extends AbstractStandard implements StandardI {
    
    private static double LOG_FACTOR = 1d / Math.log(2);
    
    private Count frequencyCalculator = new Count();
    
    private double minimalGain = 0.1;
    
    public InformationGainStandard() {}
    
    public InformationGainStandard(double minimalGain) {
    	this.minimalGain = minimalGain;
    }
    
    public void setMinimalGain(double minimalGain) {
    	this.minimalGain = minimalGain;    	
    }
    
    public double getNominalBenefit(DataSet dataSet, Column column) {
    	double[][] weightCounts = frequencyCalculator.getNominalCounts(dataSet, column);
    	return getBenefit(weightCounts);
    }
    
    public double getNumericalBenefit(DataSet dataSet, Column column, double splitValue) {
    	double[][] weightCounts = frequencyCalculator.getNumericalCounts(dataSet, column, splitValue);
    	return getBenefit(weightCounts);
    }
    
    protected double getBenefit(double[][] weightCounts) {
    	int numberOfValues = weightCounts.length;
    	int numberOfLabels = weightCounts[0].length;

    	// calculate entropies
    	double[] entropies = new double[numberOfValues];
    	double[] totalWeights = new double[numberOfValues]; 
    	for (int v = 0; v < numberOfValues; v++) {
    		for (int l = 0; l < numberOfLabels; l++) {
    			totalWeights[v] += weightCounts[v][l];
    		}
    		
    		for (int l = 0; l < numberOfLabels; l++) {
                if (weightCounts[v][l] > 0) {
                    double proportion = weightCounts[v][l] / totalWeights[v];
                    entropies[v] -= (Math.log(proportion) * LOG_FACTOR) * proportion;
                }
    		}
    	}
    	
    	// calculate information amount WITH this column
    	double totalWeight = 0.0d;
    	for (double w : totalWeights) {
    		totalWeight += w;
    	}
    	
    	double information = 0.0d;
    	for (int v = 0; v < numberOfValues; v++) {
    		information += totalWeights[v] / totalWeight * entropies[v];
    	}
    	
    	
    	// calculate information amount WITHOUT this column
    	double[] classWeights = new double[numberOfLabels];
    	for (int l = 0; l < numberOfLabels; l++) {
        	for (int v = 0; v < numberOfValues; v++) {
        		classWeights[l] += weightCounts[v][l];
    		}
    	}
    	
    	double totalClassWeight = 0.0d;
    	for (double w : classWeights) {
    		totalClassWeight += w;
    	}
    	
    	double classEntropy = 0.0d;
		for (int l = 0; l < numberOfLabels; l++) {
            if (classWeights[l] > 0) {
                double proportion = classWeights[l] / totalClassWeight;
                classEntropy -= (Math.log(proportion) * LOG_FACTOR) * proportion;
            }
		}
		
    	// calculate and return information gain
    	double informationGain = classEntropy - information;
    	if (informationGain < minimalGain * classEntropy) {
    		informationGain = 0;
    	}
    	return informationGain;
    }
    
    protected double getEntropy(double[] labelWeights, double totalWeight) {
        double entropy = 0;
        for (int i = 0; i < labelWeights.length; i++) {
            if (labelWeights[i] > 0) {
                double proportion = labelWeights[i] / totalWeight;
                entropy -= (Math.log(proportion) * LOG_FACTOR) * proportion;
            }
        }
        return entropy;
    }
    
    public boolean supportsIncrementalCalculation() {
    	return true;
    }
    
	public double getIncrementalScore() {
		 double totalEntropy = getEntropy(totalLabelWeights, totalWeight);
		 double gain = getEntropy(leftLabelWeights, leftWeight) * leftWeight / totalWeight;
		 gain += getEntropy(rightLabelWeights, rightWeight) * rightWeight / totalWeight;
		 double informationGain = totalEntropy - gain;
	    if (informationGain < minimalGain * totalEntropy) {
	    	informationGain = 0;
	    }
		 return informationGain;
	}
}
