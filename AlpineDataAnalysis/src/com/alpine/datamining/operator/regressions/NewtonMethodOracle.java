/**
 * ClassName NewtonMethodOracle
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.regressions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.tools.matrix.Matrix;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
/**
 * This oracle algorithm to determine a logistic regression model.
 * @author Eason Yu,Jeff Dong
 */
public class NewtonMethodOracle extends NewtonMethod {
    private static final Logger itsLogger = Logger.getLogger(NewtonMethodOracle.class);
    public NewtonMethodOracle() {
        super();
    }
    private boolean iterateUseFloatArraySumCursor(){
		int betaLength = getCurrentBeta().length;
		int resultLength = betaLength *(betaLength+1)/2 + betaLength + 1;
		if(resultLength < AlpineDataAnalysisConfig.ORACLE_FLOAT_SUM_MAX_COUNT){
			return false;
		}else{
			return true;
		}
    }
    private boolean varianceUseFloatArraySumCursor(){
		int betaLength = getCurrentBeta().length;
		int resultLength = betaLength *(betaLength+1)/2 ;
		if(resultLength < AlpineDataAnalysisConfig.ORACLE_FLOAT_SUM_MAX_COUNT){
			return false;
		}else{
			return true;
		}
    }

    protected StringBuffer getSqlFunction(boolean first) {
		StringBuffer sql;
		String sumFunction = null;
		if(iterateUseFloatArraySumCursor()){
			sumFunction = "";
		}else{
			sumFunction = "FloatArraySum";
		}
		if (first)
        {
        	sql = new StringBuffer("select "+sumFunction+"(alpine_miner_lr_ca_beta(");
        }
        else
        {
        	sql = new StringBuffer("select "+sumFunction+"(alpine_miner_lr_ca_he_de(");
        }
		return sql;
	}
	protected StringBuffer getBetaArray(double[] beta)
	{
		return CommonUtility.array2OracleArray(beta);
	}
	protected StringBuffer getColumnNamesArray() {
		ArrayList<String> columnNamesArray = new ArrayList<String>();
		Iterator<Column> ii = dataSet.getColumns().iterator();
		while (ii.hasNext()) {
			columnNamesArray.add(StringHandler.doubleQ(ii.next().getName()));
		}
		return CommonUtility.array2OracleArray(columnNamesArray,CommonUtility.OracleDataType.Float);
	}
	protected void iterate(boolean first) throws OperatorException {
    	double[] beta = getCurrentBeta();
    	StringBuffer columnArray = getColumnNamesArray();
    	StringBuffer where = getWhere();

    	//Generate parameters for SQL.
        String weightString = getWeightString();
        StringBuffer betaArray = getBetaArray(beta);
        StringBuffer sql = null;
        sql = getSqlFunction(first);
        sql.append(betaArray).append(",").append(columnArray).append(",");
        addIntercept(sql);
        sql.append(",").append(weightString).append(",").append(getLabelValue());
        if (first)
        {
        	sql.append(",").append("0");
        }
        sql.append("))");
        sql.append(" from ").append(tableName).append(where);
        
    	double fitness = 0;
    	Matrix hessian = new Matrix(beta.length, beta.length);
		try {
			if(iterateUseFloatArraySumCursor()){
				StringBuffer varcharArray = CommonUtility.splitOracleSqlToVarcharArray(sql);
				sql = new StringBuffer();
				sql.append("select floatarraysum_cursor(").append(varcharArray).append(") from dual");
			}

			itsLogger.debug("NewtonMethodOracle.iterate():sql="+sql);
			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				Double[] b = getHeDev();
				if (returnNan)
				{
					return;
				}
				int index = 0;
				for (int x = 0; x < beta.length; x++)
				{
					for (int y = x; y < beta.length; y++)
					{
						double h = b[index];
				    	hessian.set(x, y, h);
				    	hessian.set(y, x, h);
				    	index++;
					}
				}
				derivative.clear();
				for(int i = 0; i < beta.length; i++)
				{
					derivative.add(b[index]);
					index++;
				}
				fitness = b[index];
			}
			rs.close();
		} catch (Throwable e) {
			
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
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
		currentFitness = getFitness(beta);
		diff = Math.abs(2*currentFitness - 2*oldFitness)/(0.1 + Math.abs(2*currentFitness));
    }

	protected double[] getVariance() throws OperatorException {
		double variance[] = null;
   		variance = estimateVarianceFunction();
		return variance;
	}
    protected double[] estimateVarianceFunction() throws OperatorException {
    	itsLogger.info("Enter estimateVarianceFunction");
    	double[] beta = getStatsBeta();
    	Matrix hessian = new Matrix(beta.length, beta.length);
    	StringBuffer columnArray = getColumnNamesArray();
    	StringBuffer where = getWhere();
    	//Generate parameters for SQL.
        String weightString = getWeightString();
        StringBuffer betaArray  = getBetaArray(beta);
		String sumFunction = null;
		if(varianceUseFloatArraySumCursor()){
			sumFunction = "";
		}else{
			sumFunction = "FloatArraySum";
		}
        StringBuffer sql = new StringBuffer("select "+sumFunction+"(alpine_miner_lr_ca_he(");//result_count := beta_count *(beta_count+1)/2;
        sql.append(betaArray).append(",").append(columnArray).append(",");
        addIntercept(sql);
        sql.append(",").append(weightString).append("))");
        sql.append(" from ").append(tableName).append(where);
		try {
			if(varianceUseFloatArraySumCursor()){
				StringBuffer varcharArray = CommonUtility.splitOracleSqlToVarcharArray(sql);
				sql = new StringBuffer();
				sql.append("select floatarraysum_cursor(").append(varcharArray).append(") from dual");
			}
			itsLogger.debug("NewtonMethodOracle.estimateVarianceFunction():sql="+sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				ArrayList<Double> b = new ArrayList<Double>();
				ResultSet  resultSet =rs.getArray(1).getResultSet();
				while(resultSet.next()){
					b.add(resultSet.getInt(1) - 1,resultSet.getDouble(2));
				}
				int index = 0;
				for (int x = 0; x < beta.length; x++)
				{
					for (int y = x; y < beta.length; y++)
					{
						double h = 0.0;
						if (b.get(index) != null && !Double.isNaN(b.get(index)))
						{
							h = b.get(index).doubleValue();
						}
				    	hessian.set(x, y, h);
				    	hessian.set(y, x, h);
				    	index++;
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

    	return getVariance(beta.length, hessian);
    }
    

	protected double getFitness(double [] beta) throws OperatorException {
		StringBuilder sql = null;
		sql = generateFunctionFitnessSql(beta);
		double fitness = 0;
		try {
			itsLogger.debug("NewtonMethodOracle.getFitness():sql="+sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				fitness = rs.getDouble(1);
			}
			rs.close();
		} catch (SQLException e) {
			if (e.getSQLState().equals("2201E")||e.getSQLState().equals("22003")) {
				fitness = Double.NEGATIVE_INFINITY;
				return fitness;
			}
			throw new OperatorException(e.getLocalizedMessage());
		}
		return fitness;
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
		ArrayList<Double> b1 = new ArrayList<Double>();
		ResultSet resultSet = rs.getArray(1).getResultSet();

		while(resultSet.next()){
			b1.add(resultSet.getInt(1) - 1, resultSet.getDouble(2));
		}
		Double [] b = new Double[b1.size()];
		
		for ( int i = 0; i < b1.size(); i++)
		{
			if (b1.get(i) == null || Double.isNaN(b1.get(i)))
			{
				returnNan = true;
				return null;
			}
			else
			{
				b[i] = b1.get(i);
			}
		}
		return b;
	}
}
