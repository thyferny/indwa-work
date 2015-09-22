/**
 * ClassName NewtonMethodNetezza.java
 *
 * Version information: 1.00
 *
 * Data: 2011-12-20
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.regressions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
/**
 * This Netezza algorithm to determine a logistic regression model.
 * @author Eason Yu
 */
public class NewtonMethodNetezza extends NewtonMethod {
    private static final Logger itsLogger = Logger.getLogger(NewtonMethodNetezza.class);
    private int aliasCount;
    public NewtonMethodNetezza() {
    	super();
    }
	protected StringBuffer getSqlFunction(boolean first) {
		return null;
	}

    protected double[] estimateVarianceFunction() throws OperatorException {

    	itsLogger.info("Enter estimateVarianceFunction");
    	double[] beta = getStatsBeta();
    	String[] columns = getColumnNamesStringArray();
    	StringBuffer where = getWhere();

    	StringBuffer aliasSql = new StringBuffer();
		aliasSql.append(getProbabilitySqlAlias(dataSet,beta));
		StringBuffer gx = new StringBuffer();
		gx.append(" (alpine_e0");
		for(int i = 0; i < aliasCount - 1; i++){
			gx.append("+alpine_e").append(i+1);
		}
		gx.append(") as alpine_gx ");
		StringBuffer pi = new StringBuffer();
		pi.append((" case when alpine_gx > 30 then  1.0::double/2.2204460492503131e-16/(1.0 + 1.0/2.2204460492503131e-16::double) when alpine_gx < -30 then  2.2204460492503131e-16/(1+ 2.2204460492503131e-16::double) else exp(alpine_gx::double)/(1+ exp(alpine_gx::double)) end as alpine_pi "));
		Matrix hessian = calculateHessian(beta, columns,where, aliasSql, gx, pi,false);
    	return getVariance(beta.length, hessian);
    }
    
	protected double[] getVariance() throws OperatorException {
		double variance[] = null;

   		variance = estimateVarianceFunction();
		return variance;
	}
	protected double getFitness(double [] beta) throws OperatorException {
		return 0;
	}

	protected void addIntercept(StringBuffer sql) {
		if (addIntercept)
        {
        	sql.append("1");
        }
        else
        {
        	sql.append("0");
        }
	}
	protected Double[] getHeDev() throws SQLException
	{
		return null;
	}

    protected void iterate(boolean first) throws OperatorException {
    	double[] beta = getCurrentBeta();
    	String[] columns = getColumnNamesStringArray();
    	if(first){
    		try {
				databaseConnection.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
    	}
//    	StringBuffer where = getWhere();
		String labelName = StringHandler.doubleQ(label.getName());
		StringBuffer where = getWhere();

    	double fitness = 0;
    	StringBuffer aliasSql = new StringBuffer();
		aliasSql.append(getProbabilitySqlAlias(dataSet,beta));
		StringBuffer gx = new StringBuffer();
		gx.append(" (alpine_e0");
		for(int i = 0; i < aliasCount - 1; i++){
			gx.append("+alpine_e").append(i+1);
		}
		gx.append(") as alpine_gx ");
		StringBuffer pi = new StringBuffer();
		if (first) {
			pi.append(" (").append(getLabelValue()).append(
					"+0.5)/2.0 as alpine_pi ");
		} else {
			pi.append((" case when alpine_gx > 30 then  1.0::double/2.2204460492503131e-16/(1.0 + 1.0/2.2204460492503131e-16::double) when alpine_gx < -30 then  2.2204460492503131e-16/(1+ 2.2204460492503131e-16::double) else exp(alpine_gx::double)/(1+ exp(alpine_gx::double)) end as alpine_pi "));
		}

		Matrix hessian = calculateHessian(beta, columns, where,aliasSql, gx, pi,first);
		aliasSql.append(",").append(labelName);
		gx.append(",").append(labelName);
		pi.append(", ").append(labelName);
		calculateDerivative(beta, columns, where, aliasSql, gx,  pi, labelName, first);
    	fitness = calculateFitness(beta, columns, where,aliasSql, gx, pi, labelName, first);
    	Matrix varianceCovarianceMatrix = null;
    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    	} catch (Exception e) {
    		matrixInverseException = true;
    		return;
    	}
    	
    	diff = 0;
    	double[] delta = new double[beta.length];
		for (int i = 0; i < beta.length; i++)
		{
			oldOldBeta[i] = oldBeta[i];
			oldBeta[i] = beta[i];
			delta[i] = 0;
			for(int j = 0; j < beta.length; j++)
			{
				delta[i] += varianceCovarianceMatrix.get(i, j) * derivative.get(j);
			}
			if (first)
			{
				beta[i] = -delta[i];
			}
			else
			{
				beta[i] = beta[i] - delta[i];
			}
		}
		oldOldFitness = oldFitness;
		oldFitness = fitness;
		
    	aliasSql.setLength(0);
		aliasSql.append(getProbabilitySqlAlias(dataSet,beta)).append(",").append(labelName);
		pi.setLength(0);
		pi.append((" case when alpine_gx > 30 then  1.0::double/2.2204460492503131e-16/(1.0 + 1.0/2.2204460492503131e-16::double) when alpine_gx < -30 then  2.2204460492503131e-16/(1+ 2.2204460492503131e-16::double) else exp(alpine_gx::double)/(1+ exp(alpine_gx::double)) end as alpine_pi ")).append(",").append(labelName);
		currentFitness = calculateFitness(beta, columns, where,aliasSql, gx, pi, labelName, false);
		diff = Math.abs(2*currentFitness - 2*oldFitness)/(0.1 + Math.abs(2*currentFitness));
    }
    private Matrix calculateHessian(double[] beta, String[] columns, StringBuffer where, StringBuffer aliasSql, StringBuffer gx, StringBuffer pi, boolean first) throws OperatorException{
		Matrix hessian = new Matrix(beta.length, beta.length);    	

		StringBuffer subQuerySplit = new StringBuffer();
		StringBuffer subQueryGx = new StringBuffer();
		StringBuffer subQueryPi = new StringBuffer();
		StringBuffer subQueryHessian = new StringBuffer();

		ArrayList<String> columnAliaArray = new ArrayList<String>();

		if(!first){
		subQuerySplit.append(" select ").append(aliasSql);
		subQueryGx.append(" select ").append(gx);
		}
		subQueryHessian.append(" select ");
		int eachCount = AlpineDataAnalysisConfig.LR_NETEZZA_EACH_COUNT;
		int countInEachCycle = 0;

		subQueryPi.append(" select ").append(pi);
		int cycle = beta.length * (beta.length + 1) / 2 / eachCount;
		int remain = beta.length * (beta.length + 1) / 2 - cycle * eachCount;
		int count = 0;
		int cycleCount = 0;
		int cycleStartI = 0;
		int cycleStartJ = 0;
		for (int i = 0; i < beta.length; i++) {
			String x = "";
			if (i == beta.length - 1 && addIntercept) {
				x = "1.0";
			} else {
				x = columns[i];
				if(!columnAliaArray.contains(x)){
					columnAliaArray.add(x);
				}
			}
			for (int j = i; j < beta.length; j++) {
				String y = "";
				if (j == beta.length - 1 && addIntercept) {
					y = "1.0";
				} else {
					y = columns[j];
					if(!columnAliaArray.contains(y)){
						columnAliaArray.add(y);
					}
				}
				if((i != beta.length - 1 || !addIntercept) && !columnAliaArray.contains(x)){
					columnAliaArray.add(x);
				}
				if (countInEachCycle != 0) {
					subQueryHessian.append(",");
				}
				subQueryHessian.append("sum(-1.0::double*").append(x).append("*")
						.append(y).append("* alpine_pi * (1.0 - alpine_pi))");
				if ((count + 1) % eachCount == 0
						|| (i == beta.length - 1 && j == beta.length - 1)) {
//					subQueryPi.append(",").append(x).append(",").append(y);
					for(String columnAlis: columnAliaArray){
						subQueryPi.append(",").append(columnAlis);
					}
					if (first) {
						subQueryPi.append(" from ").append(tableName).append(" ").append(where);
					} else {
						for(String columnAlis: columnAliaArray){
							subQuerySplit.append(",").append(columnAlis);
							subQueryGx.append(",").append(columnAlis);
						}
						
						subQuerySplit.append(" from ").append(tableName).append(" ").append(where)
								.append(" limit all ");
						subQueryGx.append(" from (").append(subQuerySplit)
								.append(") foo ");
						subQueryPi.append(" from (").append(subQueryGx).append(
								") foo ");
					}
					subQueryHessian.append(" from (").append(subQueryPi).append(
							") foo ");
					// executeSql;
					try {
						itsLogger.debug("NewtonMethodNetezza.iterate():sql="+subQueryHessian);
						ResultSet rs = st.executeQuery(subQueryHessian
								.toString());
						if (rs.next()) {
							int resultCount = 0;
							if (i == beta.length - 1 && j == beta.length - 1 &&  remain != 0) {
								resultCount = remain;
							} else {
								resultCount = eachCount;
							}
							for (int k = 0; k < resultCount; k++) {
								double result = rs.getDouble(k + 1);
								if (Double.isNaN(result)) {
									returnNan = true;
									itsLogger.warn(
											"result[" + i + "][" + j
													+ "] isNaN");
									return hessian;
								}
								hessian.set(cycleStartI, cycleStartJ, result);
								hessian.set(cycleStartJ, cycleStartI, result);
								if(cycleStartJ == beta.length - 1){
									cycleStartJ = cycleStartI + 1;
									cycleStartI = cycleStartI + 1;
								}else{
									cycleStartJ = cycleStartJ + 1;
								}
							}
						}
					} catch (SQLException e) {
						itsLogger.error(
								e.getLocalizedMessage());
						throw new OperatorException(e.getLocalizedMessage());
						// e.printStackTrace();
					}

					cycleCount++;
					if (!first) {
						subQuerySplit.setLength(0);
						subQuerySplit.append(" select ").append(aliasSql);
						subQueryGx.setLength(0);
						subQueryGx.append(" select ").append(gx);
					}
					subQueryPi.setLength(0);
					subQueryPi.append(" select ").append(pi);
					subQueryHessian.setLength(0);
					subQueryHessian.append(" select ");
					countInEachCycle = 0;
					if(j == beta.length - 1){
						cycleStartJ = i + 1;
						cycleStartI = i + 1;
					}else{
						cycleStartJ = j + 1;
						cycleStartI = i;
					}
					columnAliaArray.clear();
				}else{
					countInEachCycle++;
				}
				count++;
			}
		}
		return hessian;

    }
    private void calculateDerivative(double[] beta, String[] columns, StringBuffer where, StringBuffer aliasSql, StringBuffer gx, StringBuffer pi, String labelName, boolean first) throws OperatorException{

		derivative.clear();

		int eachCount = AlpineDataAnalysisConfig.LR_NETEZZA_EACH_COUNT;
		int countInEachCycle = 0;

		int cycle = 0;
		int remain = 0;
		int count = 0;
		int cycleCount = 0;

		StringBuffer subQuerySplit = new StringBuffer();
		StringBuffer subQueryGx = new StringBuffer();
		StringBuffer subQueryPi = new StringBuffer();
		derivative.clear();
		StringBuffer subQueryXwz = new StringBuffer();
		StringBuffer subQueryEta = new StringBuffer();
		StringBuffer subQueryMuEtaDev = new StringBuffer();
		StringBuffer xwz = new StringBuffer();
		StringBuffer eta = new StringBuffer(
				" ln(alpine_pi::double/(1.0::double - alpine_pi)) as alpine_eta,alpine_pi::double/(1::double-alpine_pi) as alpine_exp_eta , alpine_pi, ").append(labelName);
		StringBuffer muEtaDev = new StringBuffer(
				" case when  alpine_eta > 30 or alpine_eta < -30 then 2.2204460492503131e-16::double else alpine_exp_eta::double/((1+alpine_exp_eta::double)*(1+alpine_exp_eta::double)) end as alpine_mu_eta_dev, alpine_pi, alpine_eta , ").append(labelName);
		xwz.append("  alpine_pi::double*(1-alpine_pi::double)*(alpine_eta::double+(").append(
				getLabelValue()).append(
				" - alpine_pi::double)/alpine_mu_eta_dev::double) ");
		subQueryEta.append(" select ").append(eta);
		subQueryMuEtaDev.append(" select ").append(muEtaDev);
		subQueryXwz.append(" select ");
		
		StringBuffer der = new StringBuffer();
		der.append("(").append(getLabelValue()).append(" - alpine_pi)");
		StringBuffer subQueryDer = new StringBuffer();
		subQueryDer.append(" select ");

		if(!first){
		subQuerySplit.setLength(0);
		subQuerySplit.append(" select ").append(aliasSql);
		subQueryGx.setLength(0);
		subQueryGx.append(" select ").append(gx);
		}
		// if(first){
		subQueryPi.setLength(0);
		subQueryPi.append(" select ").append(pi);
		// pi := (weight * y + 0.5)/(weight + 1);
		cycle = beta.length / eachCount;
		remain = beta.length - cycle * eachCount;
		// -x*y*weight_arg*pi*(1.0 - pi)
		count = 0;
		cycleCount = 0;
		countInEachCycle = 0;

		for (int i = 0; i < beta.length; i++) {
			String x = "";
			if (i == beta.length - 1&& addIntercept) {
				x = "1.0";
			} else {
				x = columns[i];
				if (!first) {
					subQuerySplit.append(",").append(x);
					subQueryGx.append(",").append(x);
				}else{
				subQueryEta.append(",").append(x);
				subQueryMuEtaDev.append(",").append(x);
				}
				// subQueryXwz.append(",").append(x);
				subQueryPi.append(",").append(x);
			}
			if (countInEachCycle != 0) {
				if(first){
				subQueryXwz.append(",");
				}else{
					subQueryDer.append(",");
				}
			}
			if(first){
			subQueryXwz.append("sum(").append(x).append("::double*").append(xwz).append(")");
			}else{
				subQueryDer.append("sum(").append(x).append("::double*").append(der).append(")");
			}
			if ((count + 1) % eachCount == 0 || (i == beta.length - 1)) {
				if (first) {
					subQueryPi.append(" from ").append(tableName).append(" ").append(where);
					subQueryEta.append(" from (").append(subQueryPi)
					.append(") foo ");
			subQueryMuEtaDev.append(" from (").append(subQueryEta).append(
					") foo ");
			subQueryXwz.append(" from (").append(subQueryMuEtaDev).append(
					") foo ");

				} else {

					subQuerySplit.append(" from ").append(tableName).append(" ").append(where)
					.append(" limit all ");
					subQueryGx.append(" from (").append(subQuerySplit).append(") foo");
					subQueryPi.append(" from (").append(subQueryGx).append(
							") foo ");
					subQueryDer.append(" from (").append(subQueryPi).append(
					") foo ");

				}

				// executeSql;
				try {
					ResultSet rs = null;
					if(first){
					itsLogger.debug("NewtonMethodNetezza.iterate():sql="+subQueryXwz);
					rs = st.executeQuery(subQueryXwz.toString());
					}else{
						itsLogger.debug("NewtonMethodNetezza.iterate():sql="+subQueryDer);
						rs = st.executeQuery(subQueryDer.toString());

					}
					if (rs.next()) {
						int resultCount = 0;
						if (i == beta.length - 1 && remain != 0) {
							resultCount = remain;
						} else {
							resultCount = eachCount;
						}
						for (int k = 0; k < resultCount; k++) {
							double result = rs.getDouble(k + 1);
							if (Double.isNaN(result)) {
								returnNan = true;
								itsLogger.warn(
										"result[" + i + "] isNaN");
								return;
							}
							derivative.add(result);
						}
					}
				} catch (SQLException e) {
					itsLogger.error(e.getMessage(),e);
					throw new OperatorException(e.getLocalizedMessage());
					// e.printStackTrace();
				}

				cycleCount++;
				if (!first) {
					subQuerySplit.setLength(0);
					subQuerySplit.append(" select ").append(aliasSql);
					subQueryGx.setLength(0);
					subQueryGx.append(" select ").append(gx);
					subQueryDer.setLength(0);
					subQueryDer.append(" select ");
				}else{
				subQueryEta.setLength(0);
				subQueryMuEtaDev.setLength(0);
				subQueryXwz.setLength(0);
				subQueryEta.append(" select ").append(eta);
				subQueryMuEtaDev.append(" select ").append(muEtaDev);
				subQueryXwz.append(" select ");
				}
				subQueryPi.setLength(0);
				subQueryPi.append(" select ").append(pi);

				countInEachCycle = 0;
			}else{
				countInEachCycle++;
			}
			count++;
		}

    }
    private double calculateFitness(double[] beta, String[] columns, StringBuffer where, StringBuffer aliasSql, StringBuffer gx, StringBuffer pi, String labelName, boolean first) throws OperatorException{
    	double fitness = 0;
		StringBuffer sql = new StringBuffer();
		StringBuffer subQuerySplit = new StringBuffer();
		StringBuffer subQueryGx = new StringBuffer();
		StringBuffer subQueryPi = new StringBuffer();
		subQuerySplit.append(" select ").append(aliasSql);
		subQueryGx.append(" select ").append(gx);
		StringBuffer subQueryFitness = new StringBuffer();
		subQueryFitness
				.append(" select sum(case when  ")
				.append(labelName)
				.append(" = '")
				.append(goodValue)
				.append(
						"' then ln(alpine_pi::double) else ln((1.0 - alpine_pi)::double) end )");
		sql.setLength(0);
		subQueryPi.append(" select ").append(pi);
		if (first) {
			subQueryPi.append(" from ").append(tableName).append(" ").append(where);
		} else {
			subQuerySplit.append(" from ").append(tableName).append(" ").append(where)
			.append(" limit all ");
			subQueryGx.append(" from (" ).append(subQuerySplit).append(") foo ");
			subQueryPi.append(" from (").append(subQueryGx).append(") foo ");
		}
		subQueryFitness.append(" from ( ").append(subQueryPi).append(") foo");
		try {
			itsLogger.debug("NewtonMethodNetezza.iterate():sql="+subQueryFitness);
			ResultSet rs = st.executeQuery(subQueryFitness.toString());
			if (rs.next()) {
				double result = rs.getDouble(1);
				if (Double.isNaN(result)) {
					returnNan = true;
					itsLogger.warn("fitness isNaN");
				}
				fitness = result;
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
			// e.printStackTrace();
		}
		return fitness;

    }

	
	protected StringBuilder generateFunctionFitnessSql(double [] beta) {
		String labelName = StringHandler.doubleQ(label.getName());
    	StringBuffer aliasSql = new StringBuffer();
		aliasSql.append(getProbabilitySqlAlias(dataSet, getCurrentBeta()));
		aliasSql.append(",").append(labelName);

		StringBuffer subQuerySplit = new StringBuffer();
		StringBuffer subQueryGx = new StringBuffer();
		StringBuffer subQueryPi = new StringBuffer();

		StringBuffer gx = new StringBuffer();
		gx.append(" (alpine_e0");
		for(int i = 0; i < aliasCount - 1; i++){
			gx.append("+alpine_e").append(i+1);
		}
		gx.append(") as alpine_gx ");

		subQuerySplit.setLength(0);

		subQuerySplit.append(" select ").append(aliasSql);
		subQuerySplit.append(" from ").append(tableName)
		.append(" limit all ");

		// if(first){
		subQueryPi.setLength(0);
//		subQueryPi.append(" select ").append(pi);
		StringBuilder sql = new StringBuilder();

		sql
				.append(" select sum(case when  ")
				.append(labelName)
				.append(" = '")
				.append(goodValue)
				.append(
						"' then ln(alpine_pi::double) else ln((1.0 - alpine_pi)::double) end )");


		StringBuffer pi = new StringBuffer();
		pi.append((" case when alpine_gx > 30 then  1.0::double/2.2204460492503131e-16/(1.0 + 1.0/2.2204460492503131e-16::double) when alpine_gx < -30 then  2.2204460492503131e-16/(1+ 2.2204460492503131e-16::double) else exp(alpine_gx::double)/(1+ exp(alpine_gx::double)) end as alpine_pi "));

		gx.append(",").append(labelName);
		pi.append(", ").append(labelName);

		subQueryGx.setLength(0);
		subQueryGx.append(" select ").append(gx).append(" from (").append(subQuerySplit).append(") foo ");

		subQueryPi.append(" select ").append(pi);
		subQueryPi.append(" from (").append(subQueryGx).append(") foo ");
		sql.append(" from ( ").append(subQueryPi).append(") foo");
		return sql;
	}
	private String[] getColumnNamesStringArray() {
		String[] columnArray = new String[dataSet.getColumns().size()];
		Iterator<Column> ii = dataSet.getColumns().iterator();
		int i = 0;
		while (ii.hasNext()) {
			columnArray[i] = StringHandler.escQ(StringHandler.doubleQ(ii.next().getName()));
			i++;
		}
		return columnArray;
	}
	protected void dropTable() throws OperatorException {
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		try {
			itsLogger.debug("NewtonMethod.dropTable():sql="
							+ truncate.toString());
			st.execute(truncate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
			itsLogger.debug("NewtonMethod.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	protected StringBuilder getProbabilitySqlAlias(DataSet dataSet, double[] beta) {
		ISqlGeneratorMultiDB sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(DataSourceInfoNZ.dBType);

		ArrayList<String> coefficients = new ArrayList<String>();
		ArrayList<String> columns = new ArrayList<String>();
		int i = 0; 
		for(Column column : dataSet.getColumns())
		{
			 String columnName = StringHandler.doubleQ(column.getName());
				double betaValue=beta[i];
				coefficients.add("("+sqlGeneratorMultiDB.castToDouble(String.valueOf(betaValue))+")");
				columns.add(sqlGeneratorMultiDB.castToDouble(columnName));
				i++;
		}

		int aliasCount = 0;
		StringBuilder subSql = new StringBuilder(sqlGeneratorMultiDB.castToDouble(String.valueOf(beta[beta.length - 1])));
		boolean first = false;
		for (i = 0; i < coefficients.size(); i++){
			if((i+1) % AlpineDataAnalysisConfig.NZ_ALIAS_NUM == 0 || i == coefficients.size() - 1){
				if(first){
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients.get(i)+"*"+columns.get(i)).append(" alpine_e"+(aliasCount));
				aliasCount++;
				first = true;
			}else{
				if(first){
					first = false;
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients.get(i)+"*"+columns.get(i));
			}
		}
		this.aliasCount = aliasCount;
		return subSql;
	}
}
