/**
 * 

* ClassName PLDAFactory.java
*
* Version information: 1.00
*
* Data: Mar 16, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer.PLDA;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.plda.PLDAModel;
import com.alpine.datamining.operator.plda.PLDAModelDB2;
import com.alpine.datamining.operator.plda.PLDAModelGreenplum;
import com.alpine.datamining.operator.plda.PLDAModelNZ;
import com.alpine.datamining.operator.plda.PLDAModelOracle;
import com.alpine.datamining.operator.plda.PLDAModelPostgres;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;


/**
 * @author Shawn
 *
 */
public class PLDAFactory {

	private static PLDAPostgres PGAnalyzer= new PLDAPostgres();
	private static PLDAGreenplum GPAnalyzer= new PLDAGreenplum();
	private static PLDAOracle OracleAnalyzer = new PLDAOracle();
	private static PLDADB2 DB2Analyzer = new PLDADB2();
	private static PLDANZ NZAnalyzer = new PLDANZ();

	
	public static PLDAImpl createPLDAAnalyzer(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return PGAnalyzer;
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return OracleAnalyzer;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return GPAnalyzer;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return DB2Analyzer;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return  NZAnalyzer;
		}
		else {
			return GPAnalyzer;
		}
	}
	
	
	public static PLDAModel  createPLDAModel(String dBType,DataSet dataSet)
	{
		if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			return new PLDAModelOracle(dataSet);
		} else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)) {
			return new PLDAModelGreenplum(dataSet);
		} else if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
			return new PLDAModelPostgres(dataSet);
		} else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
			return new PLDAModelDB2(dataSet);
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return new PLDAModelNZ(dataSet);
		}
		else return new PLDAModelGreenplum(dataSet);
	}
}
