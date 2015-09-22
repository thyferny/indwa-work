/**
 * ClassName SVMNoveltyDetection
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
public class SVMNoveltyDetection extends AbstractSVM {
	@Override
	public Model train(DataSet dataSet) throws OperatorException {
		para = (SVMParameter)getParameter();
		para = (SVMParameter)getParameter();
		AbstractSVMLearner learner = null;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			learner = new SVMNoveltyDetectionLearnerDB2();
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			learner = new SVMNoveltyDetectionLearnerOracle();
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			learner = new SVMNoveltyDetectionLearnerNetezza();
		}else{
			learner = new SVMNoveltyDetectionLearner();
		}
		Model model = null;
		model = learner.train(dataSet, para);
		return model;
	}
}
