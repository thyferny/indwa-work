
package com.alpine.datamining.operator.svm;

import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import com.alpine.utility.db.DataSourceInfoFactory;
import org.apache.log4j.Logger;

public class SVMNoveltyDetectionLearnerNetezza extends AbstractSVMLearner {
    private static final Logger itsLogger = Logger.getLogger(SVMNoveltyDetectionLearnerNetezza.class);
    private NetezzaProcUtil netezzaProcUtil = new NetezzaProcUtil();
	public Model train(DataSet dataSet, SVMParameter parameter) throws OperatorException {
		para = parameter;
		setDataSourceInfo(DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName()));
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
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
		SVMNoveltyDetectionModel model = new SVMNoveltyDetectionModelNetezza(dataSet, newDataSet);
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

		sql.append("call alpine_miner_online_sv_nd( '")
		.append(newTableName).append("','")
		.append(netezzaProcUtil.getColumnTableName()).append("','")
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
			itsLogger.debug("SVMNoveltyDetectionLearnerNetezza.train():sql="+sql);
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
			itsLogger.debug("SVMNoveltyDetectionLearnerNetezza.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
}
