/**
 * ClassName LinearRegressionDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
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
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

/**
 *  <p>This operator calculates a linear regression model. </p>
 *
 */
public class LinearRegressionDB extends Trainer {
	
	LinearRegressionParameter para;
	public LinearRegressionDB() {
		super();
	}

	public Model train(DataSet dataSet) throws OperatorException {
		para = (LinearRegressionParameter)getParameter();	
		LinearRegressionImp imp = null;
		String columNames = para.getColumnNames();
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType))
    	{
			imp = new LinearRegressionImpOracle();
    	}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoGreenplum.dBType)
    			|| ((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
    		if(para.isGroupBy()==true)
    		{
    			imp= new LinearRegressionIMPGroupbyPGGP();
    			((LinearRegressionIMPGroupbyPGGP)imp).setGroupbyColumn(para.getGroupByColumn());
    		}
    		else{
    			imp = new LinearRegressionImpPGGP();
    		}
    	}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
    		imp = new LinearRegressionImpDB2();
    	}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType)){
    		imp = new LinearRegressionImpNetezza();
    	}
    	else
    	{
    		imp = new LinearRegressionImpPGGP();
    	}
	
		return imp.learn(dataSet, para, columNames);
	}

}
