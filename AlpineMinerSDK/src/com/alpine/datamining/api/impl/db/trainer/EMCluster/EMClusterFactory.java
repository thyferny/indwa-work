/**
 * 

* ClassName EMClusterFactory.java
*
* Version information: 1.00
*
* Data: Apr 26, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.EMCluster;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.operator.EMCluster.EMModelDB2;
import com.alpine.datamining.operator.EMCluster.EMModelGreenplum;
import com.alpine.datamining.operator.EMCluster.EMModelNetezza;
import com.alpine.datamining.operator.EMCluster.EMModelOracle;
import com.alpine.datamining.operator.EMCluster.EMModelPostgres;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

/**
 * @author Shawn
 *
 */
public class EMClusterFactory {

	
	public static EMClusterImpl createEMAnalyzer(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new EMClusterPostgres();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new EMClusterOracle();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new EMClusterGreenplum();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new EMClusterDB2();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return new EMClusterNetezza();
		}
		else {
			return new EMClusterGreenplum();
		}
	}
	
	
	
	public static EMModel  createEMModel(String dBType,DataSet dataSet)
	{
		if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			return new EMModelOracle(dataSet);
		} else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)) {
			return new EMModelGreenplum(dataSet);
		} else if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
			return new EMModelPostgres(dataSet);
		} else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
			return new EMModelDB2(dataSet);
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return new EMModelNetezza(dataSet);
		}
		else return new EMModelGreenplum(dataSet);
	}
}
