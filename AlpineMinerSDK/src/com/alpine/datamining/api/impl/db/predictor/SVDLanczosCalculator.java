/**
 * ClassName SVDCalculator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-6-23
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.predictor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.SVDLanczosCalculatorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 * 
 */
public class SVDLanczosCalculator extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(SVDLanczosCalculator.class);
	private String UdependentColumn;
	private String VdependentColumn;
	private String singularValueDependentColumn;
	private String UfeatureColumn;
	private String VfeatureColumn;
	private String singularValueFeatureColumn;
	private String colName;
	private String rowName;
	private boolean crossProduct;
	private String keyColumn;
	private String keyValue;
	private String UmatrixTable;
	private String VmatrixTable;
	private String singularValueTable;
	private AnalysisStorageParameterModel analysisStorageParameterModel;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		SVDLanczosCalculatorConfig SVDconfig = (SVDLanczosCalculatorConfig) source
				.getAnalyticConfig();
		setConfig(SVDconfig);
		DataSet dataSet = null;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source, source
					.getAnalyticConfig());

		} catch (Exception e) {
			logger.error(e);
			if (e instanceof WrongUsedException) {
				throw new AnalysisError(this, (WrongUsedException) e);
			} else if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}

		AnalyzerOutPutDataBaseUpdate result = new AnalyzerOutPutDataBaseUpdate();

		result.setDataset(dataSet);
		fillDBInfo(result, (DataBaseAnalyticSource) source);
		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(SVDconfig.getLocale()));
		return result;
	}

	private void setConfig(SVDLanczosCalculatorConfig SVDconfig) {
		if (!StringUtil.isEmpty(SVDconfig.getCrossProduct())) {
			crossProduct = Boolean.parseBoolean(SVDconfig.getCrossProduct());
		}
		if (!StringUtil.isEmpty(SVDconfig.getUdependentColumnF())) {
			UdependentColumn = StringHandler.doubleQ(SVDconfig
					.getUdependentColumnF());
		}
		if (!StringUtil.isEmpty(SVDconfig.getVdependentColumnF())) {
			VdependentColumn = StringHandler.doubleQ(SVDconfig
					.getVdependentColumnF());
		}
		if (!StringUtil.isEmpty(SVDconfig.getSingularValuedependentColumnF())) {
			singularValueDependentColumn = StringHandler.doubleQ(SVDconfig
					.getSingularValuedependentColumnF());
		}
		if (!StringUtil.isEmpty(SVDconfig.getUfeatureColumn())) {
			UfeatureColumn = StringHandler.doubleQ(SVDconfig
					.getUfeatureColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getVfeatureColumn())) {
			VfeatureColumn = StringHandler.doubleQ(SVDconfig
					.getVfeatureColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getSingularValuefeatureColumn())) {
			singularValueFeatureColumn = StringHandler.doubleQ(SVDconfig
					.getSingularValuefeatureColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getColNameF())) {
			colName = StringHandler.doubleQ(SVDconfig.getColNameF());
		}
		if (!StringUtil.isEmpty(SVDconfig.getRowNameF())) {
			rowName = StringHandler.doubleQ(SVDconfig.getRowNameF());
		}
		if (!StringUtil.isEmpty(SVDconfig.getUmatrixTableF())) {
			UmatrixTable = SVDconfig.getUmatrixTableF();
		}
		if (!StringUtil.isEmpty(SVDconfig.getVmatrixTableF())) {
			VmatrixTable = SVDconfig.getVmatrixTableF();
		}
		if (!StringUtil.isEmpty(SVDconfig.getSingularValueTableF())) {
			singularValueTable = SVDconfig.getSingularValueTableF();
		}
		if (!crossProduct) {
			if (!StringUtil.isEmpty(SVDconfig.getKeyColumn())) {
				keyColumn = StringHandler.doubleQ(SVDconfig.getKeyColumn());
			}
			if (!StringUtil.isEmpty(SVDconfig.getKeyValue())) {
				keyValue = SVDconfig.getKeyValue();
			}
		}
		analysisStorageParameterModel = SVDconfig.getStorageParameters();
	}

	protected DBSource getDataSource(DataBaseAnalyticSource para,
			AnalyticConfiguration config) throws OperatorException {
		String OutputSchema = ((SVDLanczosCalculatorConfig) config).getOutputSchema();
		String OutputTable = ((SVDLanczosCalculatorConfig) config).getOutputTable();
		String DropIfExist = ((SVDLanczosCalculatorConfig) config).getDropIfExist();
		String newTableName = getQuotaedTableName(OutputSchema, OutputTable);
		Connection conn = null;
		Statement st = null;
		try {
			conn = para.getConnection();
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}

		StringBuilder dropSql = new StringBuilder();
		if (DropIfExist.equalsIgnoreCase("yes")) {
			if (para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoOracle.dBType)) {
				int tableExistInt = 0;
				StringBuffer tableExist = new StringBuffer();
				tableExist.append(
						"select count(*) from dba_tables where owner = '")
						.append(OutputSchema).append("' and table_name = '")
						.append(OutputTable).append("'");
				ResultSet rs;
				try {
					logger
							.debug("SVDCalculator.getDataSource:sql="
									+ tableExist.toString());
					rs = st.executeQuery(tableExist.toString());
					while (rs.next()) {
						tableExistInt = rs.getInt(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new OperatorException(e1.getLocalizedMessage());
				}
				if (tableExistInt > 0) {
					dropSql.append("drop table ");
					dropSql.append(newTableName);
					try {
						logger
								.debug("SVDCalculator.getDataSource:sql="
										+ dropSql.toString());
						st.execute(dropSql.toString());
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error(e);
						throw new OperatorException(e.getLocalizedMessage());
					}
				}
			} else if(para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoDB2.dBType)) {
				dropSql.append("call PROC_DROPSCHTABLEIFEXISTS( '").append(StringHandler.doubleQ(OutputSchema)).append("','").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					logger
							.debug("SVDCalculator.getDataSource:sql="
									+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					logger.info(e.getLocalizedMessage());
//					throw new OperatorException(e.getLocalizedMessage());
				}
			}else if(para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoNZ.dBType)){
				dropSql.append("call droptable_if_existsdoubleq('").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					logger
							.debug("SVDCalculator.getDataSource:sql="
									+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}else {
				dropSql.append("drop table if exists ");
				dropSql.append(newTableName);
				try {
					logger
							.debug("SVDCalculator.getDataSource:sql="
									+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
		}
		
		boolean appendOnly = false;
		if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
			appendOnly = false;
		}else{
			appendOnly = true;
		}

		StringBuffer select = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		select.append(" select ").append(UmatrixTable).append(".").append(
				rowName).append(", ").append(VmatrixTable).append(".").append(
				colName).append(", sum(").append(UmatrixTable).append(".")
				.append(UdependentColumn).append("*").append(VmatrixTable)
				.append(".").append(VdependentColumn).append("*").append(
						singularValueTable).append(".").append(
						singularValueDependentColumn).append(
						") as " + StringHandler.doubleQ(UdependentColumn)
								+ " from ").append(UmatrixTable).append(", ")
				.append(singularValueTable).append(",").append(VmatrixTable)
				.append(" where ").append(UmatrixTable).append(".").append(
						UfeatureColumn).append(" = ").append(VmatrixTable)
				.append(".").append(VfeatureColumn).append(" and ").append(
						singularValueTable).append(".").append(
						singularValueFeatureColumn).append(" = ").append(
						VmatrixTable).append(".").append(VfeatureColumn);
		if (!crossProduct) {
			select.append(" and ").append(keyColumn).append(" = '").append(
					keyValue).append("' ");
		}
		select.append(" group by ").append(UmatrixTable).append(".").append(
				rowName).append(",").append(VmatrixTable).append(".").append(
				colName);
	    ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(para.getDataSourceType());
		sql.append("create table ").append(newTableName).append(
				appendOnly ? sqlGenerator.getStorageString(
						analysisStorageParameterModel.isAppendOnly(),
						analysisStorageParameterModel.isColumnarStorage(),
						analysisStorageParameterModel.isCompression(),
						analysisStorageParameterModel.getCompressionLevel())
						: " ").append(" as (").append(select).append(")");
	    sql.append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()));

		try {
			logger.debug(
					"SVDCalculator.getDataSource():sql=" + sql);
			st.execute(sql.toString());
		} catch (SQLException e) {
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		if(para.getDataBaseInfo().getSystem().equals(
				DataSourceInfoDB2.dBType)){
			StringBuffer insert = new StringBuffer();
			insert.append("insert into ").append(newTableName).append(select);
			logger.debug(
					"SVDCalculator.getDataSource():sql=" + insert);
			try {
				st.execute(insert.toString());
			} catch (SQLException e) {
				logger.error(e);
				throw new OperatorException(e.getLocalizedMessage());
			}

		}

		para.getTableInfo().setTableName(StringHandler.doubleQ(OutputTable));
		para.getTableInfo().setSchema(StringHandler.doubleQ(OutputSchema));

		DBSource dataSource = OperatorUtil.createOperator(DBSource.class);
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();

		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para.getDataBaseInfo().getSystem());
		parameter.setTableName(newTableName);
		parameter.setUrl(para.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		dataSource.setParameter(parameter);
		return dataSource;
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SVD_CALCULATOR_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SVD_CALCULATOR_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}
