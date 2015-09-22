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
import com.alpine.datamining.api.impl.algoconf.SVDCalculatorConfig;
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
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 * 
 */
public class SVDCalculator extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(SVDCalculator.class);
	
	private String UdependentColumn;
	private String VdependentColumn;
	private String UfeatureColumn;
	private String VfeatureColumn;
	private String colName;
	private String rowName;
	private boolean crossProduct;
	private String keyColumn;
	private String keyValue;
	private String UmatrixTable;
	private String VmatrixTable;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		SVDCalculatorConfig SVDconfig = (SVDCalculatorConfig) source
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

	private void setConfig(SVDCalculatorConfig SVDconfig) {
		if (!StringUtil.isEmpty(SVDconfig.getCrossProduct())) {
			crossProduct = Boolean.parseBoolean(SVDconfig.getCrossProduct());
		}
		if (!StringUtil.isEmpty(SVDconfig.getUdependentColumn())) {
			UdependentColumn = StringHandler.doubleQ(SVDconfig
					.getUdependentColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getVdependentColumn())) {
			VdependentColumn = StringHandler.doubleQ(SVDconfig
					.getVdependentColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getUfeatureColumn())) {
			UfeatureColumn = StringHandler.doubleQ(SVDconfig
					.getUfeatureColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getVfeatureColumn())) {
			VfeatureColumn = StringHandler.doubleQ(SVDconfig
					.getVfeatureColumn());
		}
		if (!StringUtil.isEmpty(SVDconfig.getColName())) {
			colName = StringHandler.doubleQ(SVDconfig.getColName());
		}
		if (!StringUtil.isEmpty(SVDconfig.getRowName())) {
			rowName = StringHandler.doubleQ(SVDconfig.getRowName());
		}
		if (!StringUtil.isEmpty(SVDconfig.getUmatrixTable())) {
			UmatrixTable = SVDconfig.getUmatrixTable();
		}
		if (!StringUtil.isEmpty(SVDconfig.getVmatrixTable())) {
			VmatrixTable = SVDconfig.getVmatrixTable();
		}
		if (!crossProduct) {
			if (!StringUtil.isEmpty(SVDconfig.getKeyColumn())) {
				keyColumn = StringHandler.doubleQ(SVDconfig.getKeyColumn());
			}
			if (!StringUtil.isEmpty(SVDconfig.getKeyValue())) {
				keyValue = SVDconfig.getKeyValue();
			}
		}
	}

	protected DBSource getDataSource(DataBaseAnalyticSource para,
			AnalyticConfiguration config) throws OperatorException{
		String OutputSchema = ((SVDCalculatorConfig) config).getOutputSchema();
		String OutputTable = ((SVDCalculatorConfig) config).getOutputTable();
		String DropIfExist = ((SVDCalculatorConfig) config).getDropIfExist();
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
			} else {
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
		StringBuffer sql = new StringBuffer();
		if (crossProduct) {
			sql.append("create table ").append(newTableName).append(
					" as select ").append(UmatrixTable).append(".").append(
					colName).append(", ").append(VmatrixTable).append(".")
					.append(rowName).append(", sum(").append(UmatrixTable)
					.append(".").append(UdependentColumn).append("*").append(
							VmatrixTable).append(".").append(VdependentColumn)
					.append(") as " + StringHandler.doubleQ(UdependentColumn)+ " from ")
					.append(UmatrixTable)
					.append(",").append(VmatrixTable).append(" where ").append(
							UmatrixTable).append(".").append(UfeatureColumn)
					.append(" = ").append(VmatrixTable).append(".").append(
							VfeatureColumn).append(" group by ").append(
							UmatrixTable).append(".").append(colName).append(
							",").append(VmatrixTable).append(".").append(
							rowName);
		} else {
			sql.append("create table ").append(newTableName).append(
					" as select ").append(UmatrixTable).append(".").append(
					colName).append(", ").append(VmatrixTable).append(".")
					.append(rowName).append(", sum(").append(UmatrixTable)
					.append(".").append(UdependentColumn).append("*").append(
							VmatrixTable).append(".").append(VdependentColumn)
					.append(") as " + StringHandler.doubleQ(UdependentColumn)+ " from ")
					.append(UmatrixTable)
					.append(",").append(VmatrixTable).append(" where ").append(
							UmatrixTable).append(".").append(UfeatureColumn)
					.append(" = ").append(VmatrixTable).append(".").append(
							VfeatureColumn).append(" and ").append(keyColumn)
					.append(" = '").append(keyValue).append("' group by ")
					.append(UmatrixTable).append(".").append(colName).append(
							",").append(VmatrixTable).append(".").append(
							rowName);
		}
	    ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(para.getDataSourceType());
	    sql.append(sqlGenerator.setCreateTableEndingSql(null));

		try {
			logger.debug(
					"SVDCalculator.getDataSource():sql=" + sql);
			st.execute(sql.toString());
		} catch (SQLException e) {
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
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
