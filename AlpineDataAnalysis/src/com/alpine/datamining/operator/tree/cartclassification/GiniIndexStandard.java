/**
 * ClassName GiniIndexStandard
 *
 * Version information: 1.00
 *
 * Data: 2010-5-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartclassification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SortedDataSet;
import com.alpine.datamining.operator.tree.cart.AbstractStandard;
import com.alpine.datamining.operator.tree.cart.Combination;
import com.alpine.datamining.utility.Tools;

/**
 * Calculates the Gini index for the given split.
 * 
 */
public class GiniIndexStandard extends AbstractStandard {
    
    private ArrayList<String> bestValues = null;
    private double bestSplit = Double.NaN;

	public double getNominalStandard(DataSet dataSet, Column column) {
		int numberOfValues = column.getMapping().size();
		double bestBenifit = Double.NaN;
		Column label = dataSet.getColumns().getLabel();
		if (label.getMapping().size() == 2)
		{
			double benefit= getNominalBenefit2Classes(dataSet, column);
			return benefit;
		}
		int numberOfLabels = label.getMapping().size();
//		Column weightColumn = dataSet.getColumns().getWeight();

		int[] bestCombin = new int[numberOfValues];
		int[] combin = new int[numberOfValues];
		for (int m = 1; m <= numberOfValues / 2; m++) {
			Combination.initCombin(combin, m, numberOfValues);

			bestBenifit = caculateBenefit(dataSet, column,
					bestBenifit, label, numberOfLabels,
					combin, bestCombin,  m);
			int position = 0;
			while (true) {
				if (Combination.firstOne(combin) == (numberOfValues - m))
					break;
				position = Combination.firstOneZero(combin);
				Combination.swap(combin, position);
				Combination.ifNeedMove(combin, position);
				bestBenifit = caculateBenefit(dataSet, column,
						bestBenifit, label, numberOfLabels,
						  combin,bestCombin, m);
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
		return bestBenifit;
	}

	private double caculateBenefit(DataSet dataSet, Column column,
			double bestBenifit, Column label, int numberOfLabels,
			 int[]combin, int[] bestCombin,int m) {
		double[][] weightCounts;
		weightCounts = new double[2][numberOfLabels];
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

		for (Data data : dataSet) {
			int labelIndex = (int) data.getValue(label);
			double value = data.getValue(column);
			if (!Double.isNaN(value)) {
				int valueIndex = (int) data.getValue(column);
				if (list.contains(new Integer(valueIndex))) {
					valueIndex = 0;
				} else {
					valueIndex = 1;

				}
				double weight = 1.0d;
				weightCounts[valueIndex][labelIndex] += weight;
			}
		}

		double benifit = getBenefit(weightCounts);
		if (Double.isNaN(bestBenifit) || benifit > bestBenifit) {
			bestBenifit = benifit;
			System.arraycopy(combin, 0, bestCombin, 0, combin.length);
		}
		return bestBenifit;
	}


  private double getNominalBenefit2Classes(DataSet dataSet,
			Column column) {
		HashMap<String,Integer> posMap=new HashMap<String,Integer>();
		HashMap<String,Integer> negMap=new HashMap<String,Integer>();
		Column label = dataSet.getColumns().getLabel();
		String labelValueP=label.getMapping().mapIndex(0);
		double sumPos=0;
		double sumNeg=0;
		double sum=0;
	  for (Data data : dataSet)
		{
			String labelValue= data.getNominalValue(label);
			String value = data.getNominalValue(column);
			if(labelValue.equals(labelValueP))
			{
				if(posMap.containsKey(value))
				{
					int temp=posMap.get(value);
					posMap.put(value, temp+1);
					sumPos++;
				}else
				{
					posMap.put(value, 1);
					sumPos++;
				}
				
			}else
			{
				if(negMap.containsKey(value))
				{
					int temp=negMap.get(value);
					negMap.put(value, temp+1);
					sumNeg++;
				}else
				{
					negMap.put(value, 1);
					sumNeg++;
				}
			}
			
		}
	  Iterator<String> i_countmap;
  
	  int  splitNumber=0;
	  if(posMap.size()<negMap.size())
	  {
		  i_countmap=negMap.keySet().iterator();
		  splitNumber=negMap.size();
	  }else
	  {
		  i_countmap=posMap.keySet().iterator();
		  splitNumber=posMap.size();
	  }
	  Object[] probability1=new Object[splitNumber];
	  int n=0;
	  while(i_countmap.hasNext())
	  {
		 String value =i_countmap.next();
		 double posValue=0;
		 double negValue=0;
		 if(posMap.get(value)==null)
			 posValue=0;
		 else
			 posValue=posMap.get(value);
		 if(negMap.get(value)==null)
			 negValue=0;
		 else
			 negValue=negMap.get(value);
		  Object[] aa={value,posValue/(posValue+negValue)};
		  probability1[n++]=aa;
	  }
	  Arrays.sort(probability1, new TComp1());
	  
	  ArrayList<String> sortedValue=new ArrayList<String>();
	  for(int j=0;j<probability1.length;j++)
	  {
		  sortedValue.add((String)((Object[])probability1[j])[0]);
	  }
	  double maxGini=Double.NEGATIVE_INFINITY;
	  for(int i=0;i<splitNumber-1;i++)
	  {
		  int count=i+1;
		  double posasc=0;
		  for(int j=0;j<count;j++)
		  {
			  if(posMap.get(sortedValue.get(j))==null)
			  {
				  posasc+=0;
			  }else
			  {
				  posasc+=posMap.get(sortedValue.get(j));
			  }
		  }
		  double posdesc=sumPos-posasc;
		  double negasc=0;
		  for(int j=0;j<count;j++)
		  {
			  if(negMap.get(sortedValue.get(j))==null)
			  {
				  negasc+=0;
			  }else
			  {
				  negasc+=negMap.get(sortedValue.get(j)); 
			  }
		  }
		  double negdesc=sumNeg-negasc;
		  sum=sumPos+sumNeg;

		  double gini=1-(((sumPos*sumPos)+(sumNeg*sumNeg))/(sum*sum))-
		  ((posasc+negasc)/sum)*(1-(posasc*posasc+negasc*negasc)/((posasc+negasc)*(posasc+negasc)))-
		  ((posdesc+negdesc)/sum)*(1-(posdesc*posdesc+negdesc*negdesc)/((posdesc+negdesc)*(posdesc+negdesc)));

		  if(gini>maxGini)
		  {
			  maxGini=gini;
			  bestValues=new ArrayList<String>();
			  bestValues.addAll(sortedValue.subList(0, i+1));
		  }
	  }
		return maxGini;
	}


	class  TComp1 implements Comparator<Object>{
		 public int compare(Object a,Object b){
			Object[] aa=(Object[])a;
			Object[] bb=(Object[])b;
			 if((Double)aa[1]<(Double)bb[1])
			 {
				 return 1;
			 }
			 else
			 {
				 return -1;
			 }
		 }

	}

/** Returns an array with  size of the number of different label
  *  values. */
 public double[] getLabelWeights(DataSet dataSet) {
     Column label = dataSet.getColumns().getLabel();
//     Column weightColumn = dataSet.getColumns().getWeight();
     double[] weights = new double[label.getMapping().size()];
     for (Data e : dataSet) {
         int labelIndex = (int)e.getValue(label);
         double weight = 1.0d;
         weights[labelIndex] += weight;
     }
     return weights;
 }

	public void startIncrementalCalculation(DataSet dataSet) {
		rightLabelWeights = getLabelWeights(dataSet);
		leftLabelWeights = new double[rightLabelWeights.length];
		totalLabelWeights = new double[rightLabelWeights.length];
		System.arraycopy(rightLabelWeights, 0, totalLabelWeights, 0, rightLabelWeights.length);
		leftWeight = 0;
		rightWeight = getTotalWeight(totalLabelWeights);
		totalWeight = rightWeight;
		
		labelColumn = dataSet.getColumns().getLabel();
//		weightColumn = dataSet.getColumns().getWeight();
	}

    public double getNumericalStandard(DataSet dataSet, Column column, double splitValue) {    	
//    	double[][] weightCounts =  frequencyCalculator.getNumericalWeightCounts(dataSet, column, splitValue);
//    	return getBenefit(weightCounts);
        SortedDataSet sortedDataSet = new SortedDataSet((DataSet)dataSet.clone(), column, SortedDataSet.INCREASING);
//        double bestSplit = Double.NaN;
        double lastValue = Double.NaN;
        double bestSplitBenefit = Double.NaN;
      
        Data lastData = null;
        if (supportsIncrementalCalculation()) {
        	startIncrementalCalculation(sortedDataSet);
        }

        int i = 0;
        for (Data e : sortedDataSet) {
    		i++;

        	double currentValue = e.getValue(column);
        	if ( i == 1)
        	{
        		lastValue = currentValue;
        		lastData = e;
        		continue;
        	}
        	// skip equal values
    		if (this.supportsIncrementalCalculation()) {
    			if (lastData != null) 
    				this.swapData(lastData);
    			lastData = e;
    		}

        	if (Tools.isEqual(currentValue, lastValue)) {
        		continue;
        	}
        	
    		if (this.supportsIncrementalCalculation()) {
	    			double benefit = this.getIncrementalStadard();

	    			if (Double.isNaN(bestSplitBenefit) || benefit > bestSplitBenefit) {
	        			bestSplitBenefit = benefit;
	        			bestSplit = (lastValue + currentValue) / 2.0d;
	    			}
    		} else 
    		{
        		double splitValue1 = (lastValue + currentValue) / 2.0d;
        		double benefit = Double.NaN;
            	double[][] weightCounts =  getNumericalWeightCounts(dataSet, column, splitValue1);
            	benefit = getBenefit(weightCounts);
	        	if (Double.isNaN(bestSplitBenefit) || benefit > bestSplitBenefit) {
        			bestSplitBenefit = benefit;
        			bestSplit = splitValue1;
        		}
        	}
            lastValue = currentValue;
        }
        return bestSplitBenefit;
    }     
    public double[][] getNumericalWeightCounts(DataSet dataSet, Column column, double splitValue) {
    	Column label = dataSet.getColumns().getLabel();
    	int numberOfLabels = label.getMapping().size();
    	
//    	Column weightColumn = dataSet.getColumns().getWeight();
    	
    	double[][] weightCounts = new double[2][numberOfLabels];
    	
    	for (Data data : dataSet) {
    		int labelIndex = (int)data.getValue(label);
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
    /** Returns the sum of the given weights. */
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
		  this.bestValues=new ArrayList<String>();
		  this.bestValues.addAll(bestValues);
	}

	public double getBestSplit() {
		return bestSplit;
	}

	public void setBestSplit(double bestSplit) {
		this.bestSplit = bestSplit;
	}

}
