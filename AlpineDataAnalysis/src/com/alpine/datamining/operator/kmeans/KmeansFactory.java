package com.alpine.datamining.operator.kmeans;

import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

public class KmeansFactory {
	public static IKmeansDB createKmeansDB(String dBType) {
		if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)) {
			return new KmeansDBGP();
		} else if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
			return new KmeansDBPostgres();
		} else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			return new KmeansDBOracle();
		} else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
			return new KmeansDBDB2();
		} else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType)) {
			return new KmeansDBNZ();
		}else {
			return new KmeansDBGP();
		}
	}
}
