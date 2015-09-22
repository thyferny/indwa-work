/**
 * ClassName SVD
 *
 * Version Information: 1.00
 *
 * Data: 2011-6-16
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svd;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

/**
 *  <p>This operator calculates SVD . </p>
 *
 */
public class SVDLanczos extends Trainer {
	
	public SVDLanczos() {
		super();
	}
	public Model train(DataSet dataSet) throws OperatorException {
		AbstractSVDLanczos svd = null;
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		if (databaseConnection.getProperties().getName().equals(DataSourceInfoOracle.dBType)){
			svd = new SVDLanczosOracle();
		}else if (databaseConnection.getProperties().getName().equals(DataSourceInfoPostgres.dBType)
				||databaseConnection.getProperties().getName().equals(DataSourceInfoGreenplum.dBType)){
			svd = new SVDLanczosPGGP();
		}else if(databaseConnection.getProperties().getName().equals(DataSourceInfoDB2.dBType)){
			svd = new SVDLanczosDB2();
		}else if(databaseConnection.getProperties().getName().equals(DataSourceInfoNZ.dBType)){
			svd = new SVDLanczosNetezza();
		}
		return svd.train(dataSet, (SVDParameter) getParameter());

	}
}
