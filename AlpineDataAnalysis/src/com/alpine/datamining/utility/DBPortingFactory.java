package com.alpine.datamining.utility;

import com.alpine.datamining.operator.tree.cartclassification.CartClassificationDB2;
import com.alpine.datamining.operator.tree.cartclassification.CartClassificationGP;
import com.alpine.datamining.operator.tree.cartclassification.CartClassificationNZ;
import com.alpine.datamining.operator.tree.cartclassification.CartClassificationOracle;
import com.alpine.datamining.operator.tree.cartclassification.CartClassificationPostgres;
import com.alpine.datamining.operator.tree.cartclassification.ICartClassfication;
import com.alpine.datamining.operator.tree.cartregression.CartRegressionDB2;
import com.alpine.datamining.operator.tree.cartregression.CartRegressionGP;
import com.alpine.datamining.operator.tree.cartregression.CartRegressionNZ;
import com.alpine.datamining.operator.tree.cartregression.CartRegressionOracle;
import com.alpine.datamining.operator.tree.cartregression.CartRegressionPostgres;
import com.alpine.datamining.operator.tree.cartregression.INumericalSql;
import com.alpine.datamining.operator.tree.threshold.MultiDBSql;
import com.alpine.datamining.operator.tree.threshold.MultiDBSqlDB2;
import com.alpine.datamining.operator.tree.threshold.MultiDBSqlGP;
import com.alpine.datamining.operator.tree.threshold.MultiDBSqlNZ;
import com.alpine.datamining.operator.tree.threshold.MultiDBSqlOracle;
import com.alpine.datamining.operator.tree.threshold.MultiDBSqlPostgres;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;

public class DBPortingFactory {
	public static MultiDBSql createDecisionTree(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new MultiDBSqlGP();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new MultiDBSqlOracle();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new MultiDBSqlPostgres();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new MultiDBSqlDB2();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType))
		{
			return new MultiDBSqlNZ();
		}else
		{
			return new MultiDBSqlGP();
		}
	}
	
	public static INumericalSql createCartRegressionDB(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new CartRegressionGP();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new CartRegressionOracle();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new CartRegressionPostgres();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new CartRegressionDB2();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType))
		{
			return new CartRegressionNZ();
		}
		else
		{
			return new CartRegressionGP();
		}
	}
	
	public static ICartClassfication createCartClassificationDB(String dBType){
		if (dBType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			return new CartClassificationGP();
		}
		else if (dBType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			return new CartClassificationOracle();
		}else if(dBType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			return new CartClassificationPostgres();
		}else if(dBType.equalsIgnoreCase(DataSourceInfoDB2.dBType)){
			return new CartClassificationDB2();
		}else if (dBType.equalsIgnoreCase(DataSourceInfoNZ.dBType))
		{
			return new CartClassificationNZ();
		}
		else
		{
			return new CartClassificationGP();
		}
	}
}
