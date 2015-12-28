
package com.alpine.datamining.operator.tree.threshold;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SplitDataSet;
import com.alpine.datamining.exception.OperatorException;


public interface ICount {

	    
	    public double[][] getNumericalCounts(DataSet dataSet, Column column, double splitValue) throws OperatorException ;

	    
	    public double[][] getNominalCounts(DataSet dataSet, Column column) throws OperatorException ;
	    
	    
	    public double[] getPartitionCount(SplitDataSet splitted) ;
	    
	    
	    public double[] getLabelCounts(DataSet dataSet) ;
	    
	    
	    public double getTotalCount(double[] weights) ;

}
