/**
 * ClassName SVMClassificationLearnerNetezza.java
 *
 * Version information: 1.00
 *
 * Data: 2011-12-28
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.DataSourceInfoFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;
/**
 * @author Eason
 */
public class SVMClassificationLearnerNetezza extends AbstractSVMLearner {
    private static Logger itsLogger = Logger.getLogger(SVMClassificationLearnerNetezza.class);
    private NetezzaProcUtil netezzaProcUtil = new NetezzaProcUtil();

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
		String labelString = "(case when "+labelName+"='"+label.getMapping().mapIndex(0)+"' then 1 else -1 end)";
		labelString = StringHandler.escQ(labelString);
		DataSet newDataSet = getTransformer().TransformCategoryToNumeric_new(dataSet);
		String newTableName = ((DBTable) newDataSet
				.getDBTable()).getTableName();

		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		netezzaProcUtil.initTable(st,false);
		SVMClassificationModel model = new SVMClassificationModelNetezza(dataSet, newDataSet);
		if(!newDataSet.equals(dataSet))
		{
			model.setAllTransformMap_valueKey(getTransformer().getAllTransformMap_valueKey());
		}
		model.setKernelType(para.getKernelType());
		model.setDegree(para.getDegree());
		model.setGamma(para.getGamma());
		TableTransferParameter.insertTable(netezzaProcUtil.getWhereCondTableName(), st, netezzaProcUtil.getWhereCondArrayNZ(newDataSet));
		TableTransferParameter.insertTable(netezzaProcUtil.getColumnTableName(), st, netezzaProcUtil.getColumnsArrayNZ(newDataSet));
		StringBuffer sql = new StringBuffer();
		sql.append("call alpine_miner_online_sv_cl( '")
		.append(newTableName).append("','")
		.append(netezzaProcUtil.getColumnTableName()).append("','")
		.append(labelString).append("','")
		.append(netezzaProcUtil.getWhereCondTableName()).append("',")
		.append(para.getKernelType()).append(",")
		.append(para.getDegree()).append(",")
		.append(para.getGamma()).append(",")
		.append(para.getEta()).append(",")
		.append(para.getNu()).append(",'")
		.append(netezzaProcUtil.getIndividualsTableName()).append("','")
		.append(netezzaProcUtil.getWeightsTableName()).append("','")
		.append(netezzaProcUtil.getModelStatsTableName()).append("')");
		
		try{
			itsLogger.debug("SVMClassificationLearnerNetezza.train():sql="+sql);
			st.execute(sql.toString());
			Double[] weights = TableTransferParameter.getDoubleResult(netezzaProcUtil.getWeightsTableName(), st);
			Double[] individuals = TableTransferParameter.getDoubleResult(netezzaProcUtil.getIndividualsTableName(), st);
			double[] modelStats = TableTransferParameter.getResult(netezzaProcUtil.getModelStatsTableName(), st);

			model.setWeights(weights);
			model.setIndividuals(individuals);

			model.setInds((int)modelStats[0]);
			model.setCumErr(modelStats[1]);
			model.setEpsilon(modelStats[2]);
			model.setRho(modelStats[3]);
			model.setB(modelStats[4]);
			model.setNsvs((int)modelStats[5]);
			model.setIndDim((int)modelStats[6]);

			if(getTransformer().isTransform())
			{
				dropTable(st, newTableName);
			}
			netezzaProcUtil.dropProcTable(st,false);
			st.close();
		}catch(SQLException e)
		{
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		return model;
	}
	protected void dropTable(Statement st,String tableName) throws OperatorException {
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
			itsLogger.debug("SVMClassificationLearnerNetezza.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
}
