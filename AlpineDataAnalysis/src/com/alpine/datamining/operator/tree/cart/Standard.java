
package com.alpine.datamining.operator.tree.cart;

import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;



public interface Standard {

    public double getNominalStandard(DataSet dataSet, Column column) throws OperatorException;
    
    public double getNumericalStandard(DataSet dataSet, Column column, double splitValue) throws OperatorException;
    
    
    public boolean supportsIncrementalCalculation();   
    
    public void swapData(Data data);
    
	public double getIncrementalStadard();
	
	public List<String> getBestValues();
	public void setBestValues(List<String> bestValues);
	public double getBestSplit();
	public void setBestSplit(double bestSplit);

}
