/**
 * ClassName LogisticRegressionDBNewton.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.regressions;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
/**
 * This operator determines a logistic regression model.
 * @author Eason Yu,Jeff Dong
 */
public class LogisticRegressionDBNewton extends Trainer {


	private LogisticRegressionParameter para;
    public LogisticRegressionDBNewton() {
        super();
    }

    public Model train(DataSet dataSet) throws OperatorException {
    	para = (LogisticRegressionParameter)getParameter();
    	NewtonMethod method = null;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType))
    	{
    		method = new NewtonMethodOracle();
    	}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType))
    	{
    		method = new NewtonMethodDB2();
    	}
		else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType))
    	{
    		method = new NewtonMethodNetezza();
    	}
    	else
    	{
    		if(para.isGroupBy())
    		{
    			method = new NewtonMethodGreenplumGroup();
    		}
    		else
    		{
    			method = new NewtonMethodGreenplum();
    		}
    	}
    	return method.learn(dataSet,
    			para);
    }
}
