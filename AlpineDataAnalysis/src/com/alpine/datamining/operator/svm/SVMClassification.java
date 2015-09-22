/**
 * ClassName SVMClassification.java
 *
 * Version information: 1.00
 *
 * Data: Apr 18, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
/**
 * 
 * @author Eason
 *
 */
public class SVMClassification extends AbstractSVM {
	public Model train(DataSet dataSet) throws OperatorException {
		para = (SVMParameter)getParameter();
		AbstractSVMLearner learner = null;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			learner = new SVMClassificationLearnerDB2();
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			learner = new SVMClassificationLearnerOracle();
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			learner = new SVMClassificationLearnerNetezza();
		}else{
			learner = new SVMClassificationLearner();
		}
		Model model = null;
		model = learner.train(dataSet, para);
		return model;
	}
}