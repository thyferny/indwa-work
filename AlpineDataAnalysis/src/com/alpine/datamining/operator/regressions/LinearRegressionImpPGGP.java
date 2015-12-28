
package com.alpine.datamining.operator.regressions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.tools.matrix.Matrix;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class LinearRegressionImpPGGP extends LinearRegressionImp {
    private static Logger itsLogger = Logger.getLogger(LinearRegressionImpPGGP.class);
    public LinearRegressionImpPGGP() {
		super();
	}

	protected void getCoefficientAndR2(String columNames,
			DataSet dataSet, String labelName,String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder orignotnull,StringBuilder sb_notNull) throws OperatorException {
		Iterator<Column> atts_i;
		StringBuffer columnNamesArray = new StringBuffer();
		columnNamesArray.append("array[1.0,");
					
		atts_i=atts.iterator();
		int i = 0;
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
			if(i != 0){
				columnNamesArray.append(",");
			}
			columnNamesArray.append(StringHandler.doubleQ(att.getName())).append("::float");
			i++;
		}
		columnNamesArray.append("]");
		String sql = null;
		sql = "select alpine_miner_mregr_coef("+labelName+"::float,"+columnNamesArray+") from "+newTableName+sb_notNull;
		itsLogger.debug("LinearRegressionImpPGGP.getCoefficientAndR2():sql="+sql);
		hessian = new Matrix(columnNames.length + 1, columnNames.length + 1);
		Matrix XY = new Matrix(columnNames.length + 1,1);
      	try {
      		Object[] object = null;
			ResultSet rs = st.executeQuery(sql.toString());
			if(rs.next())
			{
				object=(Object[])rs.getArray(1).getArray();
				for(int x = 0 ; x < columnNames.length + 1; x++)
				{
					int y = x + 1;
					double doubleValue = 0.0;
					if (object[y] != null)
					{
						if (object[y] instanceof BigDecimal){
							doubleValue = ((BigDecimal)object[y]).doubleValue();
						}else if (object[y] instanceof Double){
							doubleValue = ((Double)object[y]).doubleValue();
						}else if (object[y] instanceof Integer){
							doubleValue = ((Integer)object[y]).doubleValue();
						}else{
							doubleValue = ((Number)object[y]).doubleValue();
						}
					}
					XY.set(x, 0, doubleValue);
				}
				double [] arrayarrayResult = getHessian(object, columnNames.length + 2, (columnNames.length + 1)*(columnNames.length + 2)/2);//new double[sbAllArray.size()];
				i=0;

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
    	for(i = 0; i < coefficients.length; i++){
    		coefficients[i] = 0.0;
    	}

    	try {
    		varianceCovarianceMatrix = hessian.SVDInverse();
    		beta = varianceCovarianceMatrix.times(XY);

    		for (i = 0; i < beta.getRowDimension(); i++)
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
    		itsLogger.error(e.getMessage(), e);
    	}
		double r2 = 0;
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

	protected double cacluateRSquare(DataSet dataSet, String tableName,
			 String  labelName, LinearRegressionModelDB model,StringBuilder orignotnull) throws OperatorException {
		double RSquare = 0.0;
		StringBuffer RSquareSQL = new StringBuffer();
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg(").append(labelName).append(") from ").append(tableName).append(" ").append(orignotnull.toString());
		double avg = 0.0;
		try {
			itsLogger.debug("LinearRegressionImpPGGP.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				avg = rs.getDouble(1);
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			return Double.NaN;
		}

		StringBuffer predictedValueSQL = new StringBuffer();
		predictedValueSQL.append(model.generatePredictedString(dataSet));
		RSquareSQL.append("select 1 - sum((").append(predictedValueSQL).append("-").append(labelName).
		append(")*(").append(predictedValueSQL).append("-").append(labelName).
		append("))*1.0/sum((").append(labelName).append("-(").append(avg).append("))*(").append(labelName).append("-(").append(avg).append("))) from ").append(tableName).append(" ").append(orignotnull.toString());
		try {
			itsLogger.debug("LinearRegressionImpPGGP.cacluateRSquare():sql="+RSquareSQL);
			ResultSet rs = st.executeQuery(RSquareSQL.toString());
			if (rs.next())
			{
				RSquare = rs.getDouble(1);
			}
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			return Double.NaN;
		}
		return RSquare;
	}
	protected double[] getHessian(Object[] object, int start, int length) throws SQLException
	{
		double[] result = new double[length];
		if (object != null){
			for(int i = 0; i < length ; i++){
				int j = i + start;
				if (object[j] != null){
					if (object[j] instanceof BigDecimal){
						result[i] = ((BigDecimal)object[j]).doubleValue();
					}else if (object[i] instanceof Double){
						result[i] = ((Double)object[j]).doubleValue();
					}else if (object[i] instanceof Integer){
						result[i] = ((Integer)object[j]).doubleValue();
					}else{
						result[i] = ((Number)object[j]).doubleValue();
					}
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
