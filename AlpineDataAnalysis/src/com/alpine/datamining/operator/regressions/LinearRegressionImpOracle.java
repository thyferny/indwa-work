
package com.alpine.datamining.operator.regressions;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;


public class LinearRegressionImpOracle extends LinearRegressionImp {
    private static Logger itsLogger= Logger.getLogger(LinearRegressionImpOracle.class);
    public LinearRegressionImpOracle() {
		super();
	}

	protected void getCoefficientAndR2(String columNames,
			DataSet dataSet, String labelName,String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder orignotnull,StringBuilder sbNotNull) throws OperatorException {
		double r2 = 0;
		coefficients = getCoefficient(newTableName,
				columnNames, labelName, st, sbNotNull);
		getCoefficientMap(columnNames);
		model =  new LinearRegressionModelDB(dataSet,columnNames, columNames, coefficients,coefficientmap);
		if(!this.newDataSet.equals(this.dataSet))
		{
			model.setAllTransformMap_valueKey(transformer.getAllTransformMap_valueKey());
		}
		model.setInteractionColumnExpMap(transformer.getInteractionColumnExpMap());
		model.setInteractionColumnColumnMap(transformer.getInteractionColumnColumnMap());
		r2 = cacluateRSquare(dataSet, tableName,
				labelName, model,orignotnull);			
		model.setR2(r2);
	}

	protected Double[] getCoefficient(String tableName,
			String[] columnNames, String label, Statement st, StringBuilder orignotnull)
			throws OperatorException {
		hessian = new Matrix(columnNames.length + 1, columnNames.length + 1);
		Matrix XY = new Matrix(columnNames.length + 1,1);
		StringBuilder sbAll=new StringBuilder("select ");
		ArrayList<String> sbAllArray = new ArrayList<String>();
		StringBuilder XYSql = new StringBuilder("select FloatArray( ");//FloatArray( 
      	for (int x = 0; x < columnNames.length + 1; x++) {
			StringBuilder X=new StringBuilder();
			if ( x == 0)
			{
		        	X.append("1.0");
			}
			else
			{
				X.append(StringHandler.doubleQ(columnNames[x-1]));
				XYSql.append(",");
			}
			XYSql.append("sum(").append(X).append("*").append(label).append(")");
    		for (int y = x; y < columnNames.length + 1; y++) {
    			StringBuilder Y=new StringBuilder();

    			if (y == 0)
    			{
    		        	Y.append("1.0");
    			}
    			else
    			{
    				Y.append(StringHandler.doubleQ(columnNames[y-1]));
    			}
    			sbAllArray.add("sum("+X+"*"+Y+")");
    		}//end for(y)
    	}//end for(x)
      	XYSql.append(") from ").append(tableName).append(orignotnull);
      	sbAll.append(CommonUtility.array2OracleArray(sbAllArray,CommonUtility.OracleDataType.Float)).append(" from ").append(tableName).append(orignotnull);
      	itsLogger.info("LinearRegressionImpOracle.getCoefficient():sql="+XYSql);
      	itsLogger.info("LinearRegressionImpOracle.getCoefficient():sql="+sbAll);
      	try {
//      		Number[] result=null;
      		ArrayList<Double> result = new ArrayList<Double>();
			ResultSet rs = st.executeQuery(XYSql.toString());
			while(rs.next())
			{
				ResultSet resultSet = rs.getArray(1).getResultSet();
				while(resultSet.next()){
					result.add(resultSet.getInt(1)-1, resultSet.getDouble(2));
				}
//				 result=(Number[])rs.getArray(1).getArray();
			}
			for(int x = 0 ; x < columnNames.length + 1; x++)
			{
				double doubleValue = 0.0;
				if (result.get(x) != null)
				{
					doubleValue = result.get(x).doubleValue();
				}
				
				XY.set(x,0,doubleValue);
			}
			rs = st.executeQuery(sbAll.toString());
	      	itsLogger.info("LinearRegressionImpOracle.getCoefficient(): sball ok");
			if (rs.next()){
				double [] arrayarrayResult = getHessian(rs, sbAllArray.size());
				int i=0;
				for(int x = 0 ; x < columnNames.length + 1; x++)
				{
					for (int y = x; y < columnNames.length + 1; y++) {
						{
							double h = 0.0;
							if(!Double.isNaN(arrayarrayResult[i])){
								h=arrayarrayResult[i];
							}
							hessian.set(x, y, h);
							if(x!=y)
							{
								hessian.set(y, x, h);
							}
							i++;
						}
					}
				}
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		Matrix beta = null;
    	Matrix varianceCovarianceMatrix = null;
		coefficients = new Double[columnNames.length + 1];
		for(int i = 0; i < coefficients.length; i++){
			coefficients[i] = 0.0;
		}
   	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    		beta = varianceCovarianceMatrix.times(XY);
    		for ( int i = 0; i < beta.getRowDimension(); i++)
    		{
    			if (i == 0)
    			{
    				coefficients[beta.getRowDimension() - 1] = beta.get(i, 0);
    			}
    			else
    			{
    				coefficients[i - 1] = beta.get(i, 0);
    			}
    		}

    	} catch (Exception e) {
   			return null;
    	}
		return coefficients;
	}

	protected double cacluateRSquare(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model, StringBuilder orignotnull) throws OperatorException {
		double RSquare = 0.0;
		StringBuffer RSquareSQL = new StringBuffer();
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg(").append(labelName).append(") from ").append(tableName).append(" ").append(orignotnull);
		double avg = 0.0;
		try {
			itsLogger.debug("LinearRegressionImpOracle.cacluateRSquare():sql=" + avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				BigDecimal ret = rs.getBigDecimal(1);
				if (ret != null)
				{
					avg = ret.doubleValue();
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			return Double.NaN;
		}

		StringBuffer predictedValueSQL = new StringBuffer();
		predictedValueSQL.append(model.generatePredictedString(dataSet));
		RSquareSQL.append("select 1 - sum((").append(predictedValueSQL).append("-").append(labelName).
		append(")*(").append(predictedValueSQL).append("-").append(labelName).
		append("))*1.0/sum((").append(labelName).append("-").append(avg).append(")*(").append(labelName).append("-").append(avg).append(")) from ").append(tableName).append(" ").append(orignotnull);
		try {
			itsLogger.debug("LinearRegressionImpOracle.cacluateRSquare():sql=" + RSquareSQL);
			ResultSet rs = st.executeQuery(RSquareSQL.toString());
			if (rs.next())
			{
				BigDecimal ret = rs.getBigDecimal(1);
				if (ret != null)
				{
					RSquare = ret.doubleValue();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Double.NaN;
		}
		return RSquare;
	}
	protected double[] getHessian(ResultSet rs, int length) throws SQLException
	{
		double[] result = new double[length];
		
		Object[] arrayarray = (Object[])rs.getArray(1).getArray();
		int i = 0;
		if (arrayarray != null){
			for(int j = 0; j < arrayarray.length; j++){
//				Object array = ((Array)arrayarray[j]).getArray();
				ResultSet array = ((Array)arrayarray[j]).getResultSet();
				ArrayList<Double> arrayDouble = new ArrayList<Double>();
				while(array.next()){
					arrayDouble.add(array.getInt(1) - 1, array.getDouble(2));
				}
				for(int k = 0; k < arrayDouble.size(); k++){
					if (arrayDouble.get(k) != null && !Double.isNaN(arrayDouble.get(k).doubleValue())){
						result[i] = arrayDouble.get(k).doubleValue();
					}else{
						result[i] = 0.0;
					}
					i++;
				}
			}
		}
		return result;
	}

	@Override
	protected Matrix getVarianceCovarianceMatrix(String tableName,
			String[] columnNames, Statement st) throws OperatorException {

    	Matrix varianceCovarianceMatrix = null;
    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    	} catch (Exception e) {
   			return null;
    	}
		return varianceCovarianceMatrix;
	}
}
