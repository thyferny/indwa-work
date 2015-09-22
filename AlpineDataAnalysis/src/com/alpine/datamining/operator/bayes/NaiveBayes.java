/**
 * ClassName NaiveBayes.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.bayes;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;

/**
 * Naive Bayes learner.
 * 
 */
public class NaiveBayes extends Trainer {
    private static Logger itsLogger = Logger.getLogger(NaiveBayes.class);

    public NaiveBayes() {
		super();
	}
	private NaiveBayesParameter para;
	public Model train(DataSet dataSet) throws OperatorException {
		itsLogger.debug(LogUtils.entry("NaiveBayes", "learn", dataSet.toString()));
		para = (NaiveBayesParameter)getParameter();
		dataSet.computeAllColumnStatistics();
		boolean calculateDeviance = para.isCaculateDeviance();
		NBModel model = null;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType))
		{
			model = new NBModelOracle(dataSet, true, calculateDeviance);
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType))
		{
			model = new NBModelDB2(dataSet, true, calculateDeviance);
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType))
		{
			model = new NBModelNetezza(dataSet, true, calculateDeviance);
		}
		else
		{
			 model = new NBModel(dataSet, true, calculateDeviance);
		}
		model.calculateModel(dataSet);
		if (calculateDeviance)
		{
			model.caculateDeviance();
		}
		itsLogger.debug(LogUtils.exit("NaiveBayes", "learn", model.toString()));
		return model;
		
	}
}
