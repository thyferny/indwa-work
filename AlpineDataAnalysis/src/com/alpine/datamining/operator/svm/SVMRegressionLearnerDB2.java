
package com.alpine.datamining.operator.svm;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class SVMRegressionLearnerDB2 extends AbstractSVMLearner {
    private static final Logger itsLogger = Logger.getLogger(SVMRegressionLearnerDB2.class);
    public Model train(DataSet dataSet, SVMParameter parameter) throws OperatorException {
		para = parameter;
		setDataSourceInfo(DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName()));
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		Column label =dataSet.getColumns().getLabel();
		String labelString = StringHandler.doubleQ(label.getName());

		DataSet newDataSet = getTransformer().TransformCategoryToNumeric_new(dataSet);
		String newTableName = ((DBTable) newDataSet
				.getDBTable()).getTableName();

		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer where = getColumnWhere(newDataSet);
		where.append(" and ").append(labelString).append(" is not null ");
		SVMRegressionModel model = new SVMRegressionModelDB2(dataSet, newDataSet);
		if(!newDataSet.equals(dataSet))
		{
			model.setAllTransformMap_valueKey(getTransformer().getAllTransformMap_valueKey());
		}
		model.setKernelType(para.getKernelType());
		model.setDegree(para.getDegree());
		model.setGamma(para.getGamma());
		StringBuffer sql = new StringBuffer();
		sql.append("call alpine_miner_online_sv_reg( '").append(newTableName).append("',?,?,?,")
		.append("?,")
		.append("?,")
		.append("?,")
		.append("?,")
		.append("?,")
		.append("?,")
		.append("?,?,?,?,?,?,?,?,?)");
		
		try{
			itsLogger.debug("SVMRegressionLearnerDB2.train():sql="+sql);
			CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); 
			Array ind = getColumnSqlArray(newDataSet);
		    stpCall.setArray(1, ind);
		    stpCall.setString(2,labelString);
		    stpCall.setString(3, " where "+(where.toString()));
		    stpCall.setInt(4, para.getKernelType());
		    stpCall.setInt(5, para.getDegree());
		    stpCall.setDouble(6, para.getGamma());
		    stpCall.setDouble(7, para.getEta());
		    stpCall.setDouble(8, ((SVMRegressionParameter)para).getSlambda());
		    stpCall.setDouble(9, para.getNu());
		    
			stpCall.registerOutParameter(10, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(11, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(12, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(13, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(14, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(15, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(16, java.sql.Types.DOUBLE);
			stpCall.registerOutParameter(17, java.sql.Types.ARRAY);
			stpCall.registerOutParameter(18, java.sql.Types.ARRAY);
			stpCall.execute();
			setModel(stpCall, model, 1);

			stpCall.close();
			if(getTransformer().isTransform())
			{
				dropTable(st, newTableName);
			}

		}catch(SQLException e)
		{
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return model;
	}
	protected void dropTable(Statement st,String tableName) throws OperatorException {
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
            itsLogger.debug("SVMRegressionLearnerDB2.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}


}
