
package com.alpine.datamining.operator.svm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class SVMRegressionLearner extends AbstractSVMLearner {
    private static final Logger itsLogger = Logger.getLogger(SVMRegressionLearner.class);
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
		ResultSet rs = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer ind = getColumnArray(newDataSet);
		StringBuffer where = getColumnWhere(newDataSet);
		where.append(" and ").append(labelString).append(" is not null ");
		SVMRegressionModel model = new SVMRegressionModel(dataSet, newDataSet);
		if(!newDataSet.equals(dataSet))
		{
			model.setAllTransformMap_valueKey(getTransformer().getAllTransformMap_valueKey());
		}
		model.setKernelType(para.getKernelType());
		model.setDegree(para.getDegree());
		model.setGamma(para.getGamma());

		String sql = "select (model).inds, (model).cum_err, (model).epsilon, (model).rho, (model).b, (model).nsvs, (model).ind_dim, (model).weights, (model).individuals from (select alpine_miner_online_sv_reg('"+newTableName+"','"+ind+"','"+labelString+"','"+where+"',"+para.getKernelType()+","+para.getDegree()+","+para.getGamma()+","+para.getEta()+","+((SVMRegressionParameter)para).getSlambda()+","+para.getNu()+") as model ";
		if (getDataSourceInfo().getDBType().equals(DataSourceInfoOracle.dBType)){
			sql += " from dual ";
		}
		sql += ") a";
		try{
			itsLogger.debug("SVMRegression.train():sql="+sql);
			rs = st.executeQuery(sql.toString());
			setModel(rs, model);
			if(getTransformer().isTransform())
			{
				dropTable(st, newTableName);
			}
			rs.close();
			st.close();
		}catch(SQLException e)
		{
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return model;
	}
}
