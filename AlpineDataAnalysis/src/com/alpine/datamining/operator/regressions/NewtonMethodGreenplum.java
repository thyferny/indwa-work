

package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.tools.matrix.Matrix;
import org.apache.log4j.Logger;

public class NewtonMethodGreenplum extends NewtonMethod {
    private static final Logger itsLogger = Logger.getLogger(NewtonMethodGreenplum.class);

    public NewtonMethodGreenplum() {
    	super();
    }
	protected StringBuffer getSqlFunction(boolean first) {
		StringBuffer sql;
		if (first)
        {
        	sql = new StringBuffer("select (alpine_miner_lr_ca_beta(");
        }
        else
        {
        	sql = new StringBuffer("select (alpine_miner_lr_ca_he_de(");
        }
		return sql;
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
        StringBuffer sql = new StringBuffer("select (alpine_miner_lr_ca_he(");
        sql.append(betaArray).append(",").append(columnArray).append(",");
        addIntercept(sql);
        sql.append(",").append(weightString).append("))");
        sql.append(" from ").append(tableName).append(where);
		try {
			itsLogger.debug("NewtonMethodGreenplum.estimateVarianceFunction():sql="+sql);
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				Number[] b=(Number[])rs.getArray(1).getArray();
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
			}
			rs.close();
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
		if (useCFunction)
		{
			sql = generateFunctionFitnessSql(beta);
		}
		else
		{
			sql = GenerateFitnessSql(beta);
		}
		double fitness = 0;
		try {
			itsLogger.debug("NewtonMethodGreenplum.getFitness():sql="+sql);
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
        	sql.append("true");
        }
        else
        {
        	sql.append("false");
        }
	}
	protected Double[] getHeDev() throws SQLException
	{
		Number[] b1=(Number[])rs.getArray(1).getArray();
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
}
