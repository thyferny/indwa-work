
package com.alpine.datamining.operator.svd;

import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class SVDModel extends Prediction {
    private static final Logger itsLogger = Logger.getLogger(SVDModel.class);

    private static final long serialVersionUID = -9077215958415330973L;
	private String Umatrix;
	private String Vmatrix;
	private String colName;
	private String rowName;

	public SVDModel(DataSet dataSet, String Umatrix, String Vmatrix, String colName, String rowName) {
		super(dataSet);
		this.Umatrix = Umatrix;
		this.Vmatrix = Vmatrix;
		this.colName = colName;
		this.rowName = rowName;
	}
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException
	{
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName=((DBTable) dataSet.getDBTable())
		.getTableName();
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo
		(databaseConnection.getProperties().getName());

		String depName = StringHandler.doubleQ(getTrainingHeader().getColumns().getLabel().getName());
		String predictedLabelName = StringHandler.doubleQ(predictedLabel.getName());
		String alpineFeature = StringHandler.doubleQ(AlpineDataAnalysisConfig.SVD_FEATURE);
		StringBuilder sbCreate=new StringBuilder();
		String tempTable = "alpinesvd"+System.currentTimeMillis();
		sbCreate.append("create table "+tempTable+" as select ").append(Umatrix).append(".").append(colName).append(", ").append(Vmatrix).append(".").append(rowName).append(", sum(").append(Umatrix).append(".").append(depName).append("*").append(Vmatrix).append(".").append(depName).append(") as alpinepredictvalue from ")
		.append(tableName).append(",").append(Umatrix).append(",").append(Vmatrix)
		.append(" where ").append(tableName).append(".").append(colName).append("=").append(Umatrix).append(".").append(colName)
		.append(" and ").append(tableName).append(".").append(rowName).append("=").append(Vmatrix).append(".").append(rowName)
		.append(" and ").append(Umatrix).append(".").append(alpineFeature).append(" = ").append(Vmatrix).append(".").append(alpineFeature)
		.append(" group by ").append(Umatrix).append(".").append(colName).append(",").append(Vmatrix).append(".").append(rowName).append(" ").append(sqlGenerator.setCreateTableEndingSql(null));

		StringBuilder sbUpdate=new StringBuilder("update ");
		StringBuilder sbDrop = new StringBuilder();
		if (databaseConnection.getProperties().getName().equals(DataSourceInfoOracle.dBType)){
			sbUpdate.append(tableName).append(" set (").append(predictedLabelName).append(")=( select alpinepredictvalue from "+tempTable+" where "+tempTable+".").append(colName).append("=").append(tableName).append(".").append(colName)
			.append(" and "+tempTable+".").append(rowName).append("=").append(tableName).append(".").append(rowName).append(")");
			sbDrop.append("call proc_droptableifexists('"+tempTable+"')");
		}else{
			sbUpdate.append(tableName).append(" set (").append(predictedLabelName).append(")=(alpinepredictvalue) from "+tempTable+" where "+tempTable+".").append(colName).append("=").append(tableName).append(".").append(colName)
			.append(" and "+tempTable+".").append(rowName).append("=").append(tableName).append(".").append(rowName);
			sbDrop.append("drop table if exists ").append(tempTable);
		}
		
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("SVDModel.performPrediction():sql="+sbCreate);
			st.execute(sbCreate.toString());
			itsLogger.debug("SVDModel.performPrediction():sql="+sbUpdate);
			st.execute(sbUpdate.toString());
			itsLogger.debug("SVDModel.performPrediction():sql="+sbDrop);
			st.execute(sbDrop.toString());
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		return result.toString();
	}
	
}
