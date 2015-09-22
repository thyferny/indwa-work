package com.alpine.datamining.api.impl.db.attribute.sampling;

import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

public class SamplingMultiDBFactory {
	
	public static ISamplingMultiDB createSamplingMultiDB(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new SamplingMultiDBPostgres();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new SamplingMultiDBOracle();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new SamplingMultiDBGreenplum();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new SamplingMultiDBDB2();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
			return new SamplingMultiDBNZ();
		}else
		{
			return new SamplingMultiDBGreenplum();
		}
	}
}
