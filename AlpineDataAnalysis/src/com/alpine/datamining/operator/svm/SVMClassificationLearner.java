
package com.alpine.datamining.operator.svm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class SVMClassificationLearner extends AbstractSVMLearner {
//	SVMParameter para;
private static Logger itsLogger = Logger.getLogger(SVMClassificationLearner.class);
    public Model train(DataSet dataSet,SVMParameter parameter) throws OperatorException {
		para = parameter;
		setDataSourceInfo(DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName()));
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		Column label =dataSet.getColumns().getLabel();
		if (label.getMapping().size() != 2) {
			itsLogger.error(
					AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.LR_DEPENDENT_2_VALUE, AlpineThreadLocal.getLocale()));
			throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.LR_DEPENDENT_2_VALUE, AlpineThreadLocal.getLocale()));
		}

		String labelName = StringHandler.doubleQ(label.getName());
		String lableString = "(case when "+labelName+"=''"+label.getMapping().mapIndex(0)+"'' then 1 else -1 end)";

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
		where.append(" and ").append(labelName).append(" is not null ");
		SVMClassificationModel model = new SVMClassificationModel(dataSet, newDataSet);
		if(!newDataSet.equals(dataSet))
		{
			model.setAllTransformMap_valueKey(getTransformer().getAllTransformMap_valueKey());
		}
		model.setKernelType(para.getKernelType());
		model.setDegree(para.getDegree());
		model.setGamma(para.getGamma());
		String sql = "select (model).inds, (model).cum_err, (model).epsilon, (model).rho, (model).b, (model).nsvs, (model).ind_dim, (model).weights, (model).individuals from (select alpine_miner_online_sv_cl('"+newTableName+"','"+ind+"','"+lableString+"','"+where+"',"+para.getKernelType()+","+para.getDegree()+","+para.getGamma()+","+para.getEta()+","+para.getNu()+") as model";
		if (getDataSourceInfo().getDBType().equals(DataSourceInfoOracle.dBType)){
			sql += " from dual ";
		}
		sql += ") a";
		try{
			itsLogger.debug("SVMClassification.train():sql="+sql);
			rs = st.executeQuery(sql.toString());
			setModel(rs, model);
			//delete confidence columns;
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
