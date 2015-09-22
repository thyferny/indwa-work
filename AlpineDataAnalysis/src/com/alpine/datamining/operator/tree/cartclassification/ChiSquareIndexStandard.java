/**
* ClassName ChiSquareIndexStandard.java
*
* Version information: 1.00
*
* Data: 3 Nov 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
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
 * @author Shawn
 *
 */
public class ChiSquareIndexStandard extends AbstractStandard{
    private ArrayList<String> bestValues = null;
    private double bestSplit = Double.NaN;

	public double getNominalStandard(DataSet dataSet, Column column)   {
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
		  double maxChiSquare=Double.NEGATIVE_INFINITY;
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
		  
			  double ChiSquare=0;
			  if(sumPos==0||sumNeg==0||(posasc+negasc)==0||(posdesc+negdesc)==0)
			  {
				  ChiSquare=0;
			  }
			  else{
				  ChiSquare=Math.pow(posasc-(posasc+negasc)*(posasc+posdesc)/sum,2)/((posasc+negasc)*(posasc+posdesc)/sum)
				  +Math.pow(negasc-(posasc+negasc)*(negasc+negdesc)/sum,2)/((posasc+negasc)*(negasc+negdesc)/sum)
				  +Math.pow(posdesc-(posdesc+negdesc)*(posasc+posdesc)/sum,2)/((posdesc+negdesc)*(posasc+posdesc)/sum)
				  +Math.pow(negdesc-(negdesc+negasc)*(negdesc+posdesc)/sum,2)/((negdesc+negasc)*(negdesc+posdesc)/sum)
				  ;
			  }
			  
			  if(ChiSquare>maxChiSquare)
			  {
				  maxChiSquare=ChiSquare;
				  bestValues=new ArrayList<String>();
				  bestValues.addAll(sortedValue.subList(0, i+1));
			  }
		  }
			return maxChiSquare;
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
		rightWeight = getTotalNumber(totalLabelWeights);
		totalWeight = rightWeight;
		
		labelColumn = dataSet.getColumns().getLabel();
//		weightColumn = dataSet.getColumns().getWeight();
	}

    public double getNumericalStandard(DataSet dataSet, Column column, double splitValue) {    	
        SortedDataSet sortedDataSet = new SortedDataSet((DataSet)dataSet.clone(), column, SortedDataSet.INCREASING);
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
		double[] rowCWeights = new double[weightCounts[0].length];
		double[] columnRWeights = new double[weightCounts.length];
		for (int l = 0; l < rowCWeights.length; l++) {
			for (int v = 0; v < weightCounts.length; v++) {
				rowCWeights[l] += weightCounts[v][l];
				columnRWeights[v] += weightCounts[v][l];
			}
		}
		double totalClassWeight = getTotalNumber(rowCWeights);
		totalWeight = totalClassWeight;
		double chiSquareTestValue=0;
		for (int l = 0; l < rowCWeights.length; l++) {
			for (int v = 0; v < columnRWeights.length; v++) {
				double tempE=columnRWeights[v]*rowCWeights[l]/totalClassWeight;
				chiSquareTestValue+= Math.pow((weightCounts[v][l]-tempE),2)/tempE;
			}
		}
//		double pTest=StatisticsChiSquareTest.chiSquareTest(0.5, chiSquareTestValue);
		return chiSquareTestValue;
	}
    /** Returns the sum of the given weights. */
    public double getTotalNumber(double[] weights) {
        double sum = 0.0d;
        for (double w : weights)
            sum += w;
        return sum;
    }
  
    
    public boolean supportsIncrementalCalculation() {
    	return true;
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
