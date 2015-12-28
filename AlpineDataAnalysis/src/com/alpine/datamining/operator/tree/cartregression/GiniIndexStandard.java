
package com.alpine.datamining.operator.tree.cartregression;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SortedDataSet;
import com.alpine.datamining.operator.tree.cart.AbstractStandard;
import com.alpine.datamining.operator.tree.cart.Combination;
import com.alpine.datamining.utility.Tools;


public class GiniIndexStandard extends AbstractStandard {
    
//    private FrequencyCalculator frequencyCalculator = new FrequencyCalculator();
    private List<String> bestValues = null;
    private double bestSplit = Double.NaN;    
    
	public double getNominalStandard(DataSet dataSet, Column column) {
		int numberOfValues = column.getMapping().size();
		double bestImpurityReduction = Double.NaN;
		Column label = dataSet.getColumns().getLabel();
	
		int[] bestCombin = new int[numberOfValues];
		int[] combin = new int[numberOfValues];
		for (int m = 1; m <= numberOfValues / 2; m++) {
			Combination.initCombin(combin, m, numberOfValues);
			bestImpurityReduction = caculateImpurity(dataSet,
					column, bestImpurityReduction, label, combin, bestCombin, m);

			int position = 0;
			while (true) {
				if (Combination.firstOne(combin) == (numberOfValues - m))
					break;
				position = Combination.firstOneZero(combin);
				Combination.swap(combin, position);
				Combination.ifNeedMove(combin, position);
				bestImpurityReduction = caculateImpurity(dataSet,
						column, bestImpurityReduction, label, combin, bestCombin, m);

			}
		}
		bestValues = new ArrayList<String>();
		for (int i = 0; i < bestCombin.length; i++) {
			if (bestCombin[i] != 1)
				continue;
			{
				bestValues.add(column.getMapping().mapIndex(i));
			}
		}
		return bestImpurityReduction;
	}

	private double caculateImpurity(DataSet dataSet, Column column,
			double bestImpurityReduction, Column label,int [] combin, int[]bestCombin, int m) {
		double value = 0;
		double labelValue = 0;
		double Squared = 0;
		double varianceAll = 0;
		double countAll = 0;
		double sumSquaredAll = 0;
		double sumAll = 0;
		double varianceEqual = 0;
		double countEqual = 0;
		double sumSquaredEqual = 0;
		double sumEqual = 0;
		double varianceNon = 0;
		double countNon = 0;
		double sumSquaredNon = 0;
		double sumNon = 0;

		List<Integer> list = new ArrayList<Integer>();

		
		for (int i = 0, addflag = 0, quitflag = 0; i < combin.length; i++) {
			if (quitflag == m)
				break;
			if (combin[i] != 1)
				continue;
			list.add(i);
			if (addflag < m - 1) {
				addflag++;
			}
			quitflag++;

		}

		//(sum(a1*a1) - sum(a1)*sum(a1)/count(*))/(count(*) - 1)
		for (Data data : dataSet) {
//						int labelIndex = (int) data.getValue(label);
			value = data.getValue(column);
			labelValue = data.getValue(label);
			if (Double.isNaN(value) || Double.isNaN(labelValue))
			{
				continue;
			}
			Squared = labelValue * labelValue;
			if (!Double.isNaN(value)) {
				int valueIndex = (int) data.getValue(column);
				countAll++;
				sumSquaredAll += Squared;
				sumAll += labelValue;
				if (list.contains(new Integer(valueIndex))) {
					countEqual++;
					sumSquaredEqual += Squared;
					sumEqual += labelValue;
				} else {
					countNon++;
					sumSquaredNon += Squared;
					sumNon += labelValue;
				}

				
			}
		}
		double impurityReduction = Double.NaN;
		if (countEqual - 1 > 0 && countNon - 1 > 0)
		{
			varianceAll = (sumSquaredAll - sumAll * sumAll / countAll) / (countAll - 1);
			varianceEqual = (sumSquaredEqual - sumEqual * sumEqual / countEqual) / (countEqual - 1);
			varianceNon = (sumSquaredNon - sumNon * sumNon / countNon) / (countNon - 1);
			impurityReduction = varianceAll - varianceEqual*countEqual/countAll - varianceNon*countNon/countAll;
		}
		else
		{
			impurityReduction = Double.NaN;
		}
		if (Double.isNaN(bestImpurityReduction) || impurityReduction > bestImpurityReduction) {
			bestImpurityReduction = impurityReduction;
//						bestcolumns = list;
//						list = new ArrayList<Integer>();
			System.arraycopy(combin, 0, bestCombin, 0, combin.length);
		}
		return bestImpurityReduction;
	}

    
    public double getNumericalStandard(DataSet dataSet, Column column, double splitValue) {    	
//    	double[][] weightCounts =  frequencyCalculator.getNumericalWeightCounts(dataSet, column, splitValue);
//    	return getBenefit(weightCounts);
    	Column label = dataSet.getColumns().getLabel();
        SortedDataSet sortedDataSet = new SortedDataSet((DataSet)dataSet.clone(), column, SortedDataSet.INCREASING);
//        double bestSplit = Double.NaN;
        double lastValue = Double.NaN;
        double bestImpurityReduction = Double.NaN;
      
        int i = 0;
        for (Data e : sortedDataSet) {
    		i++;

        	double currentValue = e.getValue(column);
        	if (Double.isNaN(currentValue))
        	{
        		continue;
        	}
        	if ( i == 1)
        	{
        		lastValue = currentValue;
        		continue;
        	}
        	// skip equal values
//    		if (this.supportsIncrementalCalculation()) {
//    			if (lastdata != null) 
//    				this.swapdata(lastdata);
//    			lastdata = e;
//    		}

        	if (Tools.isEqual(currentValue, lastValue)) {
        		continue;
        	}
        	
        		double splitValue1 = (lastValue + currentValue) / 2.0d;

				double value = 0;
				double labelValue = 0;
				double Squared = 0;
				double varianceAll = 0;
				double countAll = 0;
				double sumSquaredAll = 0;
				double sumAll = 0;
				double varianceEqual = 0;
				double countEqual = 0;
				double sumSquaredEqual = 0;
				double sumEqual = 0;
				double varianceNon = 0;
				double countNon = 0;
				double sumSquaredNon = 0;
				double sumNon = 0;
				for (Data data : dataSet) {
//					int labelIndex = (int) data.getValue(label);
					value = data.getValue(column);
					labelValue = data.getValue(label);
					if (Double.isNaN(value) || Double.isNaN(labelValue))
					{
						continue;
					}

					Squared = labelValue * labelValue;
					if (!Double.isNaN(value)) {
//						int valueIndex = (int) data.getValue(column);
						countAll++;
						sumSquaredAll += Squared;
						sumAll += labelValue;
						//if (list.contains(valueIndex)) {
						if (value <= splitValue1) {
//							valueIndex = 0;
							countEqual++;
							sumSquaredEqual += Squared;
							sumEqual += labelValue;
						} else {
//							valueIndex = 1;
							countNon++;
							sumSquaredNon += Squared;
							sumNon += labelValue;
						}
					}
				}
				double impurityReduction = Double.NaN;
				if (countEqual - 1 > 0 && countNon - 1 > 0)
				{
					varianceAll = (sumSquaredAll - sumAll * sumAll / countAll) / (countAll - 1);
					varianceEqual = (sumSquaredEqual - sumEqual * sumEqual / countEqual) / (countEqual - 1);
					varianceNon = (sumSquaredNon - sumNon * sumNon / countNon) / (countNon - 1);

					impurityReduction = varianceAll - varianceEqual*countEqual/countAll - varianceNon*countNon/countAll;
				}
				else
				{
					impurityReduction = Double.NaN;
				}
	        	if (Double.isNaN(bestImpurityReduction) || impurityReduction > bestImpurityReduction) {
        			bestImpurityReduction = impurityReduction;
        			bestSplit = splitValue1;
        		}
//        	}
            lastValue = currentValue;
        }
        return bestImpurityReduction;
    }
    
  
    public double getBenefit(double[][] weightCounts) {
    	// calculate information amount WITHOUT this column
    	double[] classWeights = new double[weightCounts[0].length];
    	for (int l = 0; l < classWeights.length; l++) {
        	for (int v = 0; v < weightCounts.length; v++) {
        		classWeights[l] += weightCounts[v][l];
    		}
    	}
    	
        double totalClassWeight = getTotalWeight(classWeights);
        totalWeight = totalClassWeight;
        
        double totalEntropy = getGiniIndex(classWeights, totalClassWeight);
        
        double gain = 0;
        for (int v = 0; v < weightCounts.length; v++) {
            double[] partitionWeights = weightCounts[v];
            double partitionWeight = getTotalWeight(partitionWeights);
            gain += getGiniIndex(partitionWeights, partitionWeight) * partitionWeight / totalWeight;
        }
        return totalEntropy - gain;
    }
//    
//    public double[] getPartitionWeights(SplitDataSet splitted) {
//        Column weightColumn = splitted.getcolumns().getWeight();
//        double[] weights = new double[splitted.getNumberOfSubsets()];
//        for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
//            splitted.selectSingleSubset(i);
//            for (Data e : splitted) {
//                double weight = 1.0d;
//                if (weightColumn != null) {
//                    weight = e.getValue(weightColumn);
//                }
//                weights[i] += weight;
//            }
//        }
//        return weights;
//    }
//    
//    
//    public double[] getLabelWeights(DataSet dataSet) {
//        Column label = dataSet.getcolumns().getLabel();
//        Column weightColumn = dataSet.getcolumns().getWeight();
//        double[] weights = new double[label.getMapping().size()];
//        for (Data e : dataSet) {
//            int labelIndex = (int)e.getValue(label);
//            double weight = 1.0d;
//            if (weightColumn != null) {
//                weight = e.getValue(weightColumn);
//            }
//            weights[labelIndex] += weight;
//        }
//        return weights;
//    }
    
    
    public double getTotalWeight(double[] weights) {
        double sum = 0.0d;
        for (double w : weights)
            sum += w;
        return sum;
    }
    private double getGiniIndex(double[] labelWeights, double totalWeight) {
    	double sum = 0.0d;
    	for (int i = 0; i < labelWeights.length; i++) {
    		double frequency = labelWeights[i] / totalWeight;
    		sum += frequency * frequency;
    	}
    	return 1.0d - sum;
    }
    
    public boolean supportsIncrementalCalculation() {
    	return true;
    }
    
	public double getIncrementalStadard() {
		double totalGiniEntropy = getGiniIndex(totalLabelWeights, totalWeight);
		double gain = getGiniIndex(leftLabelWeights, leftWeight) * leftWeight / totalWeight;
		gain += getGiniIndex(rightLabelWeights, rightWeight) * rightWeight / totalWeight;
		return totalGiniEntropy - gain;
	}


	public List<String> getBestValues() {
		return bestValues;
	}

	public void setBestValues(List<String> bestValues) {
		this.bestValues = bestValues;
	}

	public double getBestSplit() {
		return bestSplit;
	}

	public void setBestSplit(double bestSplit) {
		this.bestSplit = bestSplit;
	}
}
