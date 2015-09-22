/**
* ClassName WOEDataSqlFactory.java
*
* Version information: 1.00
*
* Data: 6 Dec 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.attribute.woe;

import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

/**
 * @author Shawn
 *
 */
public class WOEDataSqlFactory {
	public static WOEDataSQL generalWOEDataSQL(String dbType){
		WOEDataSQL dataWOE = null;
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			dataWOE = new WOEDataSQLOracle();
			dataWOE.DBType=DataSourceInfoOracle.dBType;
		} else if (dbType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)
				|| dbType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
			dataWOE = new WOEDataSQLPGGP();
			dataWOE.DBType=DataSourceInfoGreenplum.dBType;
		} else if (dbType.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
			dataWOE = new WOEDataSQLDB2();
			dataWOE.DBType=DataSourceInfoDB2.dBType;
		}else if (dbType.equalsIgnoreCase(DataSourceInfoNZ.dBType)) {
			dataWOE = new WOEDataSQLNZ();
			dataWOE.DBType=DataSourceInfoNZ.dBType;
		}
		return dataWOE;
	};
}
