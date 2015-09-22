/**
 * ClassName LinearRegressionImpDB2.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.tools.matrix.Matrix;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 *  <p>This DB2 algorithm to calculate a linear regression model.</p>
 * @author Eason
 */
public class LinearRegressionImpDB2 extends LinearRegressionImp {
    private static Logger itsLogger = Logger.getLogger(LinearRegressionImpDB2.class);
    private int maxColumn = 1012;
	public LinearRegressionImpDB2() {
		super();
		maxColumn = AlpineDataAnalysisConfig.DB2_MAX_COLUMN_COUNT;
	}

	protected void getCoefficientAndR2(String columNames,
			DataSet dataSet, String labelName,String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder orignotnull,StringBuilder sbNotNull) throws OperatorException {
		double r2 = 0;
		coefficients = getCoefficient(newTableName,
				columnNames, labelName, st,sbNotNull);
		getCoefficientMap(columnNames);
		model =  new LinearRegressionModelDB2(dataSet,columnNames, columNames, coefficients,coefficientmap);
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
			String[] columnNames, String label, Statement st,
			StringBuilder sbNotNull)
			throws OperatorException {
		hessian = new Matrix(columnNames.length + 1, columnNames.length + 1);
		Matrix XY = new Matrix(columnNames.length + 1,1);
		ArrayList<String> sbAllArray = new ArrayList<String>();
		StringBuilder XYSql = new StringBuilder("select  ");//FloatArray( 
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
			XYSql.append("sum(double(").append(X).append(")*").append(label).append(")");
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
    			sbAllArray.add("sum(double("+X+")*"+Y+")");
    		}//end for(y)
    	}//end for(x)
      	XYSql.append(" from ").append(tableName).append(sbNotNull);
//      	itsLogger.info("LinearRegressionImpDB2.getCoefficient():sql="+sbAll);
      	try {
          	itsLogger.info("LinearRegressionImpDB2.getCoefficient():sql="+XYSql);
			ResultSet rs = st.executeQuery(XYSql.toString());
			while(rs.next())
			{
				for(int i = 0; i < columnNames.length + 1; i++){
					double doubleValue = rs.getDouble(i + 1);;
					XY.set(i,0,doubleValue);
				}
			}
			int times = sbAllArray.size() / maxColumn;
			double[] rsResult = new double[sbAllArray.size()];
			for(int i = 0; i < times; i++){
				StringBuffer sql = new StringBuffer(" select ");
				for(int j = 0 ; j < maxColumn; j++){
					if(j != 0){
						sql.append(",");
					}
					sql.append(sbAllArray.get(i * maxColumn + j));
				}
				sql.append(" from ").append(tableName).append(sbNotNull);
		      	itsLogger.info("LinearRegressionImpDB2.getCoefficient():sql="+sql);
				rs = st.executeQuery(sql.toString());
				if (rs.next()){
					for(int j = 0; j < maxColumn; j++){
						rsResult[i * maxColumn + j] = rs.getDouble(j + 1);
					}
				}
			}
			if (sbAllArray.size() > times*maxColumn){
				StringBuffer sql = new StringBuffer(" select ");
				for(int j = 0 ; j < sbAllArray.size() - times*maxColumn; j++){
					if(j != 0){
						sql.append(",");
					}
					sql.append(sbAllArray.get(times * maxColumn + j));
				}
				sql.append(" from ").append(tableName).append(sbNotNull);
		      	itsLogger.info("LinearRegressionImpDB2.getCoefficient():sql="+sql);
				rs = st.executeQuery(sql.toString());
				if (rs.next()){
					for(int j = 0; j < sbAllArray.size() - times * maxColumn; j++){
						rsResult[times * maxColumn + j] = rs.getDouble(j + 1);
					}
				}
			}
				int i=0;
				for(int x = 0 ; x < columnNames.length + 1; x++)
				{
					for (int y = x; y < columnNames.length + 1; y++) {
						{
							double h = 0.0;
							if(!Double.isNaN(rsResult[i])){
								h=rsResult[i];
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
//			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(), e);
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
	/**
	 * @param datasize
	 * @param tableName
	 * @param label
	 * @param columnNames
	 * @param coefficients
	 * @return
	 */
	protected StringBuffer createSSQLL(int datasize, String tableName,
			Column label, String[] columnNames, Double[] coefficients) {
		StringBuffer predictedY = new StringBuffer("double("+coefficients[coefficients.length - 1]);
		for (int i = 0; i < columnNames.length; i++){
			predictedY.append("+double("+coefficients[i]+")*\""+columnNames[i]+"\"");
		}
		String labelName=StringHandler.doubleQ(label.getName());
		predictedY.append(")");
		StringBuffer sSQL = new StringBuffer("select sqrt(");
			
		sSQL.append("sum(("+labelName+" - "+predictedY+")*1.0*("+labelName+" - "+predictedY+"))/"+(datasize-columnNames.length - 1));
		sSQL.append(") from ").append(tableName);
		return sSQL;
	}
	protected double cacluateRSquare(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model,StringBuilder orignotnull) throws OperatorException {
		double RSquare = 0.0;
		StringBuffer RSquareSQL = new StringBuffer();
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg(double(").append(labelName).append(")) from ").append(tableName).append(" ").append(orignotnull);
		double avg = 0.0;
		try {
			itsLogger.debug("LinearRegressionImpDB2.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				Double ret = rs.getDouble(1);
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
		RSquareSQL.append("select 1 - sum((").append(predictedValueSQL).append("-double(").append(labelName).
		append("))*(").append(predictedValueSQL).append("-double(").append(labelName).
		append(")))*1.0/sum((double(").append(labelName).append(")-").append(avg).append(")*(double(").append(labelName).append(")-").append(avg).append(")) from ").append(tableName).append(" ").append(orignotnull);
		try {
			itsLogger.debug("LinearRegressionImpDB2.cacluateRSquare():sql="+RSquareSQL);
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
	protected void dropTable(String tableName) throws OperatorException {
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
            itsLogger.debug("LinearRegressionImpDB2.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

}
