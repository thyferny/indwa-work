/**
 * ClassName LogisticRegressionModelDBOracle.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;

import com.alpine.datamining.db.DataSet;


/**
 * The model determined by the {@link LogisticRegressionDBNewton} operator.
 * @author Eason Yu,Jeff Dong

 */
public class LogisticRegressionModelDB2 extends LogisticRegressionModelDB {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -3965373796731290645L;

	public LogisticRegressionModelDB2(DataSet dataSet,DataSet oldDataSet, double[] beta, double[] variance, boolean interceptAdded, String goodValue) {
        super( dataSet, oldDataSet,  beta,  variance, interceptAdded, goodValue);
    }

	protected StringBuilder getProbability() {
		StringBuilder probability = null;
		probability = getProbabilitySql(oldDataSet);
		return probability;
	}
	

	protected void addIntercept(StringBuilder probability) {
		if (interceptAdded)
		{
			probability.append("1");
		}
		else
		{
			probability.append("0");
		}
	}
}
	
