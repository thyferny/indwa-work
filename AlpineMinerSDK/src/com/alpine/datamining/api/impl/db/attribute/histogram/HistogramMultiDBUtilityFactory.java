package com.alpine.datamining.api.impl.db.attribute.histogram;

import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

public class HistogramMultiDBUtilityFactory {
	
	private static HistogramMultiDBUtility gpDBUtility=new HistogramMultiDBUtilityGPPG();
	private static HistogramMultiDBUtility oracleDBUtility=new HistogramMultiDBUtilityOracle();
	private static HistogramMultiDBUtility db2DBUtility=new HistogramMultiDBUtilityDB2();
	

	public static HistogramMultiDBUtility createHistogramMultiDBUtility(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return gpDBUtility;
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return oracleDBUtility;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return gpDBUtility;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return db2DBUtility;
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return db2DBUtility;
		}
		else {
			return gpDBUtility;
		}
	}
	
}
