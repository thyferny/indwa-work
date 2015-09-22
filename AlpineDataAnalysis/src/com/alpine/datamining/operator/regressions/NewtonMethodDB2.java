/**
 * ClassName NewtonMethodGreenplum.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.regressions;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Mapping;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.tools.matrix.Matrix;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
/**
 * This DB2 algorithm to determine a logistic regression model.
 * @author Eason Yu,Jeff Dong
 */
public class NewtonMethodDB2 extends NewtonMethod {
    private static final Logger itsLogger = Logger.getLogger(NewtonMethodDB2.class);
    CallableStatement stpCall;
    public NewtonMethodDB2() {
    	super();
    }
	protected StringBuffer getSqlFunction(boolean first) {
		StringBuffer sql;
		if (first)
        {
        	sql = new StringBuffer("call alpine_miner_lr_ca_beta_proc(");
        }
        else
        {
        	sql = new StringBuffer("call alpine_miner_lr_ca_he_de_proc(");
        }
		return sql;
	}

    protected double[] estimateVarianceFunction() throws OperatorException {
    	itsLogger.info("Enter estimateVarianceFunction");
    	double[] beta = getStatsBeta();
    	Matrix hessian = new Matrix(beta.length, beta.length);
    	StringBuffer where = getWhere();
        StringBuffer sql = new StringBuffer("call alpine_miner_lr_ca_he_proc(");
        sql.append("'").append(tableName).append("',");
        sql.append("'").append(where).append("',");
        sql.append("?, ?,");
        addIntercept(sql);
        sql.append(",?").append(")");
		try {
			itsLogger.debug("NewtonMethodGreenplum.estimateVarianceFunction():sql="+sql);
			CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); /* con is the connection */
	    	Double[] betas = getBetaDoubleArray();
	    	String[] columns = getColumnNamesStringArray();
		    java.sql.Array columnArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		    java.sql.Array betaArray =databaseConnection.getConnection().createArrayOf("DOUBLE", betas);
		      // Set IN parameters
		    stpCall.setArray(1, betaArray);
		    stpCall.setArray(2, columnArray);
			stpCall.registerOutParameter(3, java.sql.Types.ARRAY);

			stpCall.execute();
			Array arrayOut = stpCall.getArray(3);
			Number[] b = (Number[]) arrayOut.getArray();
				int index = 0;
				for (int x = 0; x < beta.length; x++)
				{
					for (int y = x; y < beta.length; y++)
					{
						double h = 0.0;
						if(!Double.isNaN(b[index].doubleValue()))
						{
							h = b[index].doubleValue();
						}
				    	hessian.set(x, y, h);
				    	hessian.set(y, x, h);
				    	index++;
					}
				}
			stpCall.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

    	return getVariance(beta.length, hessian);
    }
    
	protected double[] getVariance() throws OperatorException {
		double variance[] = null;

   		variance = estimateVarianceFunction();
		return variance;
	}
	protected double getFitness(double [] beta) throws OperatorException {
		StringBuilder sql = null;
		sql = generateFunctionFitnessSql(beta);
		double fitness = 0;
		try {
			itsLogger.debug("NewtonMethodGreenplum.getFitness():sql="+sql);
			CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); 
	    	Double[] betas = getBetaDoubleArray();
	    	String[] columns = getColumnNamesStringArray();
		    java.sql.Array columnArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		    java.sql.Array betaArray =databaseConnection.getConnection().createArrayOf("DOUBLE", betas);
		      // Set IN parameters
		    stpCall.setArray(1, betaArray);
		    stpCall.setArray(2, columnArray);
			stpCall.registerOutParameter(3, java.sql.Types.DOUBLE);
			stpCall.execute();
			fitness = stpCall.getDouble(3);
			stpCall.close();
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
		Number[] b1=(Number[])stpCall.getArray(3).getArray();
		Double [] b = new Double[b1.length];
		
		for ( int i = 0; i < b1.length; i++)
		{
			if (b1[i] == null || Double.isNaN(b1[i].doubleValue()))
			{
				returnNan = true;
				return null;
			}
			else
			{
				b[i] = b1[i].doubleValue();
			}
		}
		return b;
	}
	protected void dropTable() throws OperatorException {
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


	private Double[] getBetaDoubleArray(){
    	double[] beta = getCurrentBeta();
    	Double[] betas = new Double[beta.length];
    	for(int i = 0; i < beta.length; i++){
    		betas[i] = beta[i];
    	}
    	return betas;
	}
    protected void iterate(boolean first) throws OperatorException {
    	double[] beta = getCurrentBeta();
    	Double[] betas = getBetaDoubleArray();
    	String[] columns = getColumnNamesStringArray();
    	StringBuffer where = getWhere();
    	double fitness = 0;
    	Matrix hessian = new Matrix(beta.length, beta.length);

        StringBuffer sql = null;
        sql = getSqlFunction(first);
        sql.append("'").append(tableName).append("'").append(",");
        sql.append("'").append(where).append("',");
        sql.append("?").append(",").append("?").append(",");
        addIntercept(sql);
        sql.append(",").append(getLabelValueString());
        if (first)
        {
        	sql.append(",").append("0");
        }
        sql.append(",?)");

		try {
			itsLogger.debug("NewtonMethodDB2.iterate():sql="+sql);
			
			
			
			
			stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); 
		      // Create an SQL ARRAY value of type "ARRAY of VARCHAR"
		    java.sql.Array columnArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		    java.sql.Array betaArray =databaseConnection.getConnection().createArrayOf("DOUBLE", betas);
		      // Set IN parameters
		    stpCall.setArray(1, betaArray);
		    stpCall.setArray(2, columnArray);
			stpCall.registerOutParameter(3, java.sql.Types.ARRAY);
			stpCall.execute();
			
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
				stpCall.close();
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
    protected String getLabelValueString()
    {
        Mapping mapping = label.getMapping();
        String labelName = StringHandler.doubleQ(label.getName());
        StringBuilder sb= new StringBuilder("(case ");
        for (int i = 0; i < mapping.size(); i++)
        {
        	String value = mapping.mapIndex(i);
        	int valueNumber = 0;
        	if (value.equals(goodValue))
        	{
        		valueNumber = 1;
        	}
        	value=StringHandler.escQ(value);
        	sb.append("when ").append(labelName).append("='").append(value).append("' then ").append(valueNumber).append(" ");
        }
        sb.append(" else 0 end)");
        return "'"+StringHandler.escQ(sb.toString())+"'";
    }


	protected StringBuffer getWhere() {
		StringBuffer where = new StringBuffer(" where ");
		Iterator<Column> ii = dataSet.getColumns().iterator();
		boolean first = true;
		while (ii.hasNext()) {
			if (first)
			{
				first = false;
			}
			else
			{
				where.append(" and ");
			}
			where.append(StringHandler.doubleQ(ii.next().getName())).append(" is not null ");
		}
		where.append(" and ").append(StringHandler.doubleQ(label.getName())).append(" is not null ");
		return where;
	}
	
	protected StringBuilder generateFunctionFitnessSql(double [] beta) {

		StringBuilder sql;
//		sql = new StringBuilder("select ");
		sql = new StringBuilder("");
		
		StringBuffer addInterceptString = new StringBuffer();
        addIntercept(addInterceptString);

        sql.append("call alpine_miner_lr_ca_fitness_proc('")
				.append(tableName).append("','")
				.append(getWhere()).append("',")
				.append("?").append(",")
				.append("?").append(",")
				.append(addInterceptString).append(",")
				.append(getLabelValueString())
				.append(",?")
				.append(")");
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
}
