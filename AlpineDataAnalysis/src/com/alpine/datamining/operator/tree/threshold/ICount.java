/**
 * ClassName ICount
 *
 * Version information: 1.00
 *
 * Data: 2010-5-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SplitDataSet;
import com.alpine.datamining.exception.OperatorException;

/**
 * @author Administrator
 *
 */
public interface ICount {

	    
	    public double[][] getNumericalCounts(DataSet dataSet, Column column, double splitValue) throws OperatorException ;

	    
	    public double[][] getNominalCounts(DataSet dataSet, Column column) throws OperatorException ;
	    
	    /** Returns an array of the size of the partitions. 
	     *  Each entry contains the sum of all weights of the 
	     *  corresponding partition. */
	    public double[] getPartitionCount(SplitDataSet splitted) ;
	    
	    /** Returns an array of size of the number of different label
	     *  values. Each entry corresponds to the weight sum of all
	     *  data with the current label. */
	    public double[] getLabelCounts(DataSet dataSet) ;
	    
	    /** Returns the sum of the given weights. */
	    public double getTotalCount(double[] weights) ;

}
