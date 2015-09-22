/**
 * ClassName RandomSamplingAnalyzer.java
 *
 * Version information:1.00
 *
 * Date:Jun 8, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.StratifiedSamplingConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.model.sampling.AnalysisSampleSizeModel;
import com.alpine.datamining.api.impl.db.attribute.sampling.ISamplingMultiDB;
import com.alpine.datamining.api.impl.db.attribute.sampling.SamplingMultiDBFactory;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutSampling;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Richie Lo
 * 
 */
public class StratifiedSamplingAnalyzer extends DataOperationAnalyzer {
	private static Logger itsLogger= Logger.getLogger(StratifiedSamplingAnalyzer.class);
	
	private static final String randomTablePrefix = "sRand1_";

	private static final String tempTablePrefix = "s1_";

	private static final String typeSize = AlpineMinerConfig.STRATIFIED_SAMPLING_THRESHOLD;

	private int sampleCount;

	private String inputTableName;

	private ISamplingMultiDB samplingMultiDB;

	private String tempTableSample;

	StratifiedSamplingConfig config;
	private String sampleTypeName;
	private Column sampleColumn;

	private String dbType;

	private ISqlGeneratorMultiDB sqlGenerator;
	private AnalysisStorageParameterModel analysisStorageParameterModel = null;
	private String appendOnlyString = "";
	private String endingString = "";

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		DatabaseConnection databaseConnection = null;
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,
					source.getAnalyticConfig());
			databaseConnection = ((DBTable) dataSet.getDBTable())
					.getDatabaseConnection();
			sqlGenerator = SqlGeneratorMultiDBFactory
					.createConnectionInfo(source.getDataSourceType());
			config = (StratifiedSamplingConfig) source.getAnalyticConfig();
			sampleCount = Integer.parseInt(config.getSampleCount());
			analysisStorageParameterModel = config.getStorageParameters();
			if (analysisStorageParameterModel == null
					|| !analysisStorageParameterModel.isAppendOnly()) {
				appendOnlyString = " ";
			} else {
				appendOnlyString = sqlGenerator.getStorageString(
						analysisStorageParameterModel.isAppendOnly(),
						analysisStorageParameterModel.isColumnarStorage(),
						analysisStorageParameterModel.isCompression(),
						analysisStorageParameterModel.getCompressionLevel());
			}

			endingString = sqlGenerator
					.setCreateTableEndingSql(analysisStorageParameterModel == null ? null
							: analysisStorageParameterModel
									.getSqlDistributeString());

			setDropIfExist(config.getDropIfExist());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setOutputType(Resources.TableType);
			setInputSchema(((DataBaseAnalyticSource) source).getTableInfo()
					.getSchema());
			setInputTable(((DataBaseAnalyticSource) source).getTableInfo()
					.getTableName());
			inputTableName = getQuotaedTableName(getInputSchema(),
					getInputTable());
			tempTableSample = getQuotaedTableName(getOutputSchema(),
					tempTablePrefix + getInputTable());

			samplingMultiDB = SamplingMultiDBFactory
					.createSamplingMultiDB(source.getDataSourceType());
			samplingMultiDB.setSqlGenerator(sqlGenerator);

			dbType = source.getDataSourceType();

			performStratifiedSampling(databaseConnection, dataSet, sampleCount,
					getInputSchema(), getInputTable(), config.getLocale());

			DataBaseInfo dbInfo = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo();
			AnalyzerOutPutSampling output = new AnalyzerOutPutSampling();
			List<AnalyzerOutPutTableObject> outTableList = new ArrayList<AnalyzerOutPutTableObject>();
			for (int i = 0; i < sampleCount; i++) {
				outTableList.add(getResultTableSampleRow(databaseConnection,
				// dbInfo, config.getOutputSchema(), String.format(
						// "%s_%d", config.getOutputTable(), i)));
						dbInfo, config.getOutputSchema(), config
								.getOutputTable()
								+ "_" + i));
			}
			output.setSampleTables(outTableList);
			output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config
					.getLocale()));
			return output;
		} catch (Exception e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		}

	}

	/**
	 * @param databaseConnection
	 * @param locale
	 * @throws OperatorException
	 */
	private void performStratifiedSampling(
			DatabaseConnection databaseConnection, DataSet dataSet,
			int sampleCount, String inputSchema, String inputTable,
			Locale locale) throws AnalysisException, OperatorException {

		String outputType = getOutputType();
		String outputSchema = getOutputSchema();
		String outputTablePrefix = getOutputTable();
		String sampleSizeType = config.getSampleSizeType();
		AnalysisSampleSizeModel sampleSize = config.getSampleSize();
		String consistent = config.getConsistent();
		String disjoint = config.getDisjoint();
		// String methodName = "performStratifiedSampling()";
		String sampleColumnName = config.getSamplingColumn();
		sampleColumn = dataSet.getColumns().get(sampleColumnName);

		int numOfSamples = sampleCount;
		List<Long> sampleRowSize = new ArrayList<Long>();
		long rowCount = 0;
		Statement st = null;
		// ResultSet rs = null;

		try {
			st = databaseConnection.createStatement(false);

			// drop the database objects before creating the samples
			dropIfExist(dataSet);

			String dataSourceType = databaseConnection.getProperties()
					.getName();
			IMultiDBUtility multiDBUtility = MultiDBUtilityFactory
					.createConnectionInfo(dataSourceType);
			long count = multiDBUtility.getSampleDistinctCount(st,
					inputTableName, sampleColumnName, null);

			if (count > Integer.parseInt(typeSize)) {
				itsLogger.info(SDKLanguagePack.getMessage(
						SDKLanguagePack.STRATIFIED_OUT_BOUNDS, locale)
						+ "  " + typeSize);
			}
			sampleTypeName = getSampleTypeName(dataSourceType, st,
					sampleColumnName, inputTableName);

			rowCount = dataSet.size();
			long sumSampleSize = 0;
			if (sampleSizeType.equals(Resources.PercentageType)) {
				List<Double> percent = sampleSize.getSampleSizeList();
				for (double tempPercent : percent) {
					sampleRowSize
							.add(Math.round(rowCount * tempPercent * 0.01));
					sumSampleSize = sumSampleSize
							+ Math.round(rowCount * tempPercent * 0.01);
				}
			} else {
				for (double tempSize : sampleSize.getSampleSizeList()) {
					sampleRowSize.add(Math.round(tempSize * 1.0));
					sumSampleSize = sumSampleSize + Math.round(tempSize * 1.0);
				}
				for (double tempSize : sampleSize.getSampleSizeList()) {
					if (tempSize > rowCount) {
						itsLogger.error(
								"SampleRowSize exceeds  rowCount");
						throw new AnalysisError(this,
								AnalysisErrorName.Too_Many_SampleRowSize,
								config.getLocale());
					}
				}
				if (disjoint != null && disjoint.endsWith(Resources.TrueOpt)
						&& sumSampleSize > rowCount) {
					itsLogger.error(
							"SampleRowSize exceeds  rowCount");
					throw new AnalysisError(this,
							AnalysisErrorName.Too_Many_SampleRowSize, config
									.getLocale());
				}
			}

			String orderByClause = generateOrderByClause();

			StringBuffer columnNames = getColumnNames(databaseConnection
					.getConnection(), inputSchema, inputTable);

			if (consistent != null
					&& consistent.trim().equalsIgnoreCase(Resources.TrueOpt)) {
				execConsistentMethod(dataSet, st, numOfSamples, sampleRowSize,
						rowCount, orderByClause, inputSchema, inputTable,
						outputType, outputSchema, outputTablePrefix, config
								.getRandomSeed(), sampleColumnName,
						columnNames, databaseConnection, disjoint);
			} else {
				execUnConsistentMethod(dataSet, inputSchema, inputTable,
						outputType, outputSchema, outputTablePrefix,
						sampleColumnName, numOfSamples, sampleRowSize,
						rowCount, st, columnNames, databaseConnection, disjoint);
			}
		} catch (SQLException e) {
			itsLogger.error(e);
			throw new AnalysisException(e);
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException e) {
				itsLogger.error(e);
			}
		}

	}

	private String getSampleTypeName(String dbType, Statement st,
			String sampleName, String tableName) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(StringHandler.doubleQ(sampleName));
		sb.append(" from ").append(tableName).append(" where 1=0 ");
		ResultSet rs = st.executeQuery(sb.toString());
		ResultSetMetaData metaData = rs.getMetaData();
		return metaData.getColumnTypeName(1);
	}

	private String generateOrderByClause() {
		String orderColumnList = config.getKeyColumnList();
		String orderByClause = "";
		if (orderColumnList != null && !orderColumnList.trim().isEmpty()) {
			String[] columns = orderColumnList.split(",");
			StringBuffer sb = new StringBuffer();
			sb.append("ORDER BY ");
			for (int n = 0; n < columns.length; n++) {
				String tempColumn = columns[n].replaceAll("\"", "");
				sb.append(StringHandler.doubleQ(tempColumn));
				if (n < columns.length - 1) {
					sb.append(",");
				}
			}

			orderByClause = sb.toString();
		}
		return orderByClause;
	}

	private void execUnConsistentMethod(DataSet dataSet, String inputSchema,
			String inputTable, String outputType, String outputSchema,
			String outputTablePrefix, String sampleColumnName,
			int numOfSamples, List<Long> sampleRowSize, long rowCount,
			Statement st, StringBuffer columnNames,
			DatabaseConnection databaseConnection, String disjoint)
			throws SQLException, AnalysisError, OperatorException {
		String methodName = "execUnConsistentMethod()";
		dropIfExist(dataSet, outputSchema, tempTablePrefix + inputTable);
		if (dbType.equals(DataSourceInfoDB2.dBType)) {
			String selectSql = samplingMultiDB.createTempTable(tempTableSample,
					columnNames.toString(), inputTableName, sampleColumnName);

			StringBuilder createSql = new StringBuilder();
			createSql.append("create table ").append(tempTableSample).append(
					" as(");
			createSql.append(selectSql).append(")").append(
					sqlGenerator.setCreateTableEndingSql(null));
			itsLogger.info( createSql.toString());

			st.execute(createSql.toString());

			StringBuilder insertSql = new StringBuilder();
			insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
					tempTableSample));

			if (insertSql.length() > 0) {
				itsLogger.info(
						"StratifiedSamplingAnalyzer.execUnConsistentMethod():insertTableSql="
								+ insertSql);
				st.execute(insertSql.toString());
			}

		} else {
			String sqlCreateSample0 = samplingMultiDB.createTempTable(
					tempTableSample, columnNames.toString(), inputTableName,
					sampleColumnName);
			DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
			// oracle
			itsLogger.info( sqlCreateSample0);
			st.executeUpdate(sqlCreateSample0);
		}

		List<SamplngColumnCount> list = calculateColumnNameCount(st,
				inputSchema, inputTable, sampleColumnName, methodName);

		if (dbType.equals(DataSourceInfoDB2.dBType)) {
			long[] offset = new long[list.size()];
			if (list.size() > 0) {
				for (int n = 0; n < list.size(); n++) {
					offset[n] = 0;
				}
			}
			for (int i = 0; i < numOfSamples; i++) {
				if (!(disjoint != null && disjoint.trim().equalsIgnoreCase(
						Resources.TrueOpt))) {
					if (list.size() > 0) {
						for (int n = 0; n < list.size(); n++) {
							offset[n] = 0;
						}
					}
					String truncateSql = samplingMultiDB
							.truncate(tempTableSample);
					itsLogger.info( truncateSql);
					st.execute(truncateSql);
					StringBuilder selectSql = new StringBuilder(samplingMultiDB
							.createTempTable(tempTableSample, columnNames
									.toString(), inputTableName,
									sampleColumnName)
							+ " order by rand_order");// UnConsistent

					StringBuilder insertSql = new StringBuilder();
					insertSql.append(sqlGenerator.insertTable(selectSql
							.toString(), tempTableSample));

					if (insertSql.length() > 0) {
						itsLogger.info(
								"StratifiedSamplingAnalyzer.execUnConsistentMethod():insertTableSql="
										+ insertSql);
						st.execute(insertSql.toString());
					}
				}
				String sampleTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);

				String createTable = samplingMultiDB.generateResultTable(
						sampleTableName, outputType);

				StringBuilder createSql = new StringBuilder();
				createSql.append(createTable).append(" AS (");
				StringBuilder selectSql = new StringBuilder();

				if (list.size() > 0) {
					for (int n = 0; n < list.size(); n++) {
						long limit = sampleRowSize.get(i)
								* list.get(n).getColumnCount() / rowCount;
						if (i > 0) {
							offset[n] +=(sampleRowSize.get(i-1)
											* list.get(n).getColumnCount() / rowCount);
						}
						selectSql
								.append(samplingMultiDB
										.stratifiedSamplingAppendStringUnConsistent(
												columnNames.toString(),
												tempTableSample,
												StringHandler
														.doubleQ(sampleColumnName),
												(sampleColumn.isNumerical() ? list
														.get(n).getColumnName()
														: CommonUtility
																.quoteValue(
																		databaseConnection
																				.getProperties()
																				.getName(),
																		sampleColumn,
																		sampleTypeName,
																		list
																				.get(
																						n)
																				.getColumnName())),
												limit, offset[n]));
						if (n < list.size() - 1) {
							selectSql.append(" UNION ALL ");
						}
					}
				} else {
					selectSql.append(samplingMultiDB
							.stratifiedSamplingAppendStringUnConsistent(
									columnNames.toString(), tempTableSample,
									sampleRowSize.get(i), i
											* sampleRowSize.get(i)));
				}
				StringBuilder insertSql = new StringBuilder();
				createSql.append(selectSql).append(") ");
				if (outputType.equals(Resources.TableType)) {
					createSql
							.append(sqlGenerator.setCreateTableEndingSql(null));
					insertSql.append(sqlGenerator.insertTable(selectSql
							.toString(), sampleTableName));
				}
				itsLogger.info( createSql.toString());
				st.execute(createSql.toString());

				if (insertSql.length() > 0) {
					st.execute(insertSql.toString());
					itsLogger.info(
							"StratifiedSamplingAnalyzer.execConsistentMethod():insertTableSql="
									+ insertSql);
				}
			}
		} else {
			long[] offset = new long[list.size()];
			if (list.size() > 0) {
				for (int n = 0; n < list.size(); n++) {
					offset[n] = 0;
				}
			}
			for (int i = 0; i < numOfSamples; i++) {
				if (!(disjoint != null && disjoint.trim().equalsIgnoreCase(
						Resources.TrueOpt))) {
					if (list.size() > 0) {
						for (int n = 0; n < list.size(); n++) {
							offset[n] = 0;
						}
					}
					String truncateSql = samplingMultiDB
							.truncate(tempTableSample);
					DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
					// oracle
					itsLogger.info( truncateSql);
					st.executeUpdate(truncateSql);

					String sqlInsertSample0 = samplingMultiDB.insertTempTable(
							tempTableSample, columnNames.toString(),
							inputTableName, sampleColumnName);
					DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
					// oracle
					itsLogger.info( sqlInsertSample0);
					st.executeUpdate(sqlInsertSample0);
				}
				String sampleTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);

				String createTable = samplingMultiDB.generateResultTable(
						sampleTableName, outputType);

				StringBuffer sb = new StringBuffer();
				sb.append(createTable);
				if (outputType.equalsIgnoreCase(Resources.TableType)) {
					sb.append(appendOnlyString);
				}
				sb.append(" AS ");
				if (list.size() > 0) {
					sb.append("(");
					for (int n = 0; n < list.size(); n++) {
						long limit = sampleRowSize.get(i)
								* list.get(n).getColumnCount() / rowCount;
						if (i > 0) {
							offset[n] += (sampleRowSize.get(i-1)
											* list.get(n).getColumnCount() / rowCount);
						}
						sb
								.append(samplingMultiDB
										.stratifiedSamplingAppendStringUnConsistent(
												columnNames.toString(),
												tempTableSample,
												StringHandler
														.doubleQ(sampleColumnName),
												(sampleColumn.isNumerical() ? list
														.get(n).getColumnName()
														: CommonUtility
																.quoteValue(
																		databaseConnection
																				.getProperties()
																				.getName(),
																		sampleColumn,
																		sampleTypeName,
																		list
																				.get(
																						n)
																				.getColumnName())),
												limit, offset[n]));
						if (!(disjoint != null && disjoint.trim()
								.equalsIgnoreCase(Resources.TrueOpt))) {
							String sbString;
							if (sb.indexOf("OFFSET") != -1) {
								sbString = sb
										.substring(0, sb.indexOf("OFFSET"));
								sb = new StringBuffer(sbString + ")");
							}
						}
						if (n < list.size() - 1) {
							sb.append(" UNION ALL ");
						}
					}
					sb.append(")");
				} else {
					sb.append(samplingMultiDB
							.stratifiedSamplingAppendStringUnConsistent(
									columnNames.toString(), tempTableSample,
									sampleRowSize.get(i), i
											* sampleRowSize.get(i)));
				}
				if (outputType.equalsIgnoreCase(Resources.TableType)) {
					sb.append(endingString);
				}
				DatabaseUtil.alterParallel(databaseConnection, outputType);// for
				// oracle

				itsLogger.info( sb.toString());
				st.execute(sb.toString());
			}
		}
	}

	private StringBuffer getColumnNames(Connection dbCon, String schemaName,
			String tableName) throws SQLException {
		StringBuffer columnNames = new StringBuffer();
		ResultSet rsCol = null;
		try {
			DatabaseMetaData md = dbCon.getMetaData();
			rsCol = md.getColumns(null, schemaName, tableName, "%");
			boolean first = true;
			while (rsCol.next()) {
				if (first) {
					first = false;
				} else {
					columnNames.append(",");
				}
				columnNames.append(StringHandler.doubleQ(rsCol
						.getString("COLUMN_NAME")));
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			rsCol.close();
		}
		return columnNames;
	}

	private void execConsistentMethod(DataSet dataSet, Statement st,
			int numOfSamples, List<Long> sampleRowSize, long rowCount,
			String orderByClause, String inputSchema, String inputTable,
			String outputType, String outputSchema, String outputTablePrefix,
			String randomSeed, String sampleColumnName,
			StringBuffer columnNames, DatabaseConnection databaseConnection,
			String disjoint)// String disjoint
			throws SQLException, AnalysisError, OperatorException {
		String methodName = "execConsistentMethod()";

		dropIfExist(dataSet, outputSchema, tempTablePrefix + inputTable);

		String randomTableName = getQuotaedTableName(outputSchema,
				randomTablePrefix + inputTable);
		dropIfExist(dataSet, outputSchema, randomTablePrefix + inputTable);

		if (dbType.equals(DataSourceInfoDB2.dBType)) {
			StringBuilder createSql = new StringBuilder();
			StringBuilder selectSql = new StringBuilder();
			createSql.append("create table ").append(tempTableSample).append(
					" as (");
			selectSql.append(" select ").append(columnNames).append(
					", row_number() over (");
			selectSql.append(orderByClause).append(
					" ) as alpine_sample_id from ").append(inputTableName);
			createSql.append(selectSql).append(")").append(
					sqlGenerator.setCreateTableEndingSql(null));

			itsLogger.info( createSql.toString());
			st.executeUpdate(createSql.toString());

			StringBuilder insertSql = new StringBuilder();
			insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
					tempTableSample));

			if (insertSql.length() > 0) {
				st.execute(insertSql.toString());
				itsLogger.info(
						"RandomSamplingAnalyzer.execConsistentMethod():insertTableSql="
								+ insertSql);
			}

			createSql.setLength(0);
			createSql.append("create table ").append(randomTableName).append(
					" (alpine_sample_id BIGINT, rand_order float) ");

			itsLogger.info( createSql.toString());
			st.executeUpdate(createSql.toString());

			insertSql.setLength(0);
			insertSql.append("call alpine_miner_generate_random_table('")
					.append(randomTableName).append("',").append(rowCount)
					.append(",");
			insertSql.append(randomSeed).append(")");

			itsLogger.info( insertSql.toString());
			st.executeUpdate(insertSql.toString());

		} else {
			String sqlCreateTempTable = "CREATE TABLE " + tempTableSample
					+ DatabaseUtil.addParallel(databaseConnection, "TABLE")
					+ " AS " + "SELECT " + columnNames.toString()
					+ ",row_number() over(" + orderByClause
					+ ") AS alpine_sample_id from " + inputTableName;
			sqlCreateTempTable += sqlGenerator.setCreateTableEndingSql(null);

			DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
			// oracle
			itsLogger.info( sqlCreateTempTable);
			st.executeUpdate(sqlCreateTempTable);

			if (randomSeed != null && !randomSeed.trim().isEmpty()) {
				String sql = sqlGenerator.setSeed(randomSeed, null, null);
				itsLogger.info( sql);
				st.executeQuery(sql);
			}
			String sqlCreateRand = samplingMultiDB.createRandomTable(
					randomTableName, rowCount);

			DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
			// oracle
			itsLogger.info( sqlCreateRand);
			st.executeUpdate(sqlCreateRand);
		}

		List<SamplngColumnCount> list = calculateColumnNameCount(st,
				inputSchema, inputTable, sampleColumnName, methodName);

		if (dbType.equals(DataSourceInfoDB2.dBType)) {
			long[] offset = new long[list.size()];
			if (list.size() > 0) {
				for (int n = 0; n < list.size(); n++) {
					offset[n] = 0;
				}
			}
			for (int i = 0; i < numOfSamples; i++) {
				if (!(disjoint != null && disjoint.trim().equalsIgnoreCase(
						Resources.TrueOpt))) {
					// Consistent,,must disjoint
					
				}
				String sampleTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);
				StringBuilder createSql = new StringBuilder();
				createSql.append("create ").append(outputType).append(" ")
						.append(sampleTableName);
				createSql.append(" as (");
				StringBuilder selectSql = new StringBuilder();
				if (list.size() > 0) {
					// selectSql.append("(");

					for (int n = 0; n < list.size(); n++) {
						long limit = sampleRowSize.get(i)
								* list.get(n).getColumnCount() / rowCount;
						if (i > 0) {
							offset[n] += (sampleRowSize.get(i-1)
											* list.get(n).getColumnCount() / rowCount);
						}

						selectSql
								.append(samplingMultiDB
										.stratifiedSamplingAppendStringConsistent(
												// fromTableWhere,
												columnNames.toString(),
												tempTableSample,
												randomTableName,
												sampleColumnName,
												(sampleColumn.isNumerical() ? list
														.get(n).getColumnName()
														: CommonUtility
																.quoteValue(
																		databaseConnection
																				.getProperties()
																				.getName(),
																		sampleColumn,
																		sampleTypeName,
																		list
																				.get(
																						n)
																				.getColumnName())),
												limit, offset[n]));
						if (n < list.size() - 1) {
							selectSql.append(" UNION ALL ");
						}
					}
					// selectSql.append(")");
				} else {
					selectSql.append(samplingMultiDB
							.stratifiedSamplingAppendStringConsistent(
									tempTableSample, randomTableName,
									sampleRowSize.get(i), i
											* sampleRowSize.get(i), columnNames
											.toString()));
				}
				StringBuilder insertSql = new StringBuilder();
				createSql.append(selectSql).append(") ");
				if (outputType.equals(Resources.TableType)) {
					createSql
							.append(sqlGenerator.setCreateTableEndingSql(null));
					insertSql.append(sqlGenerator.insertTable(selectSql
							.toString(), sampleTableName));
				}
				itsLogger.info( createSql.toString());
				st.execute(createSql.toString());

				if (insertSql.length() > 0) {
					st.execute(insertSql.toString());
					itsLogger.info(
							"StratifiedSamplingAnalyzer.execConsistentMethod():insertTableSql="
									+ insertSql);
				}
			}

		} else {
			long[] offset = new long[list.size()];
			if (list.size() > 0) {
				for (int n = 0; n < list.size(); n++) {
					offset[n] = 0;
				}
			}
			for (int i = 0; i < numOfSamples; i++) {
				if (!(disjoint != null && disjoint.trim().equalsIgnoreCase(
						Resources.TrueOpt))) {

					String truncateSql = samplingMultiDB
							.truncate(tempTableSample);
					DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
					// oracle
					itsLogger.info( truncateSql);
					st.executeUpdate(truncateSql);

					String sqlInsertTempTable = "INSERT INTO "
							+ tempTableSample
							+ DatabaseUtil.addParallel(databaseConnection,
									"TABLE") + " SELECT "
							+ columnNames.toString() + ",row_number() over("
							+ orderByClause + ") AS alpine_sample_id from "
							+ inputTableName;

					DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
					// oracle
					itsLogger.info( sqlInsertTempTable);
					st.executeUpdate(sqlInsertTempTable);

				}
				String sampleTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);
				String createTableAs = "CREATE "
						+ outputType
						+ " "
						+ sampleTableName
						+ (outputType.equalsIgnoreCase(Resources.TableType) ? appendOnlyString
								: "")
						+ DatabaseUtil.addParallel(databaseConnection,
								outputType) + " AS";

				StringBuffer sb = new StringBuffer();
				sb.append(createTableAs);
				if (list.size() > 0) {
					sb.append("(");
					for (int n = 0; n < list.size(); n++) {
						long limit = sampleRowSize.get(i)
								* list.get(n).getColumnCount() / rowCount;
						if (i > 0) {
							offset[n] += (sampleRowSize.get(i-1)
											* list.get(n).getColumnCount() / rowCount);
						}
						sb
								.append(samplingMultiDB
										.stratifiedSamplingAppendStringConsistent(
												// fromTableWhere,
												columnNames.toString(),
												tempTableSample,
												randomTableName,
												sampleColumnName,
												(sampleColumn.isNumerical() ? list
														.get(n).getColumnName()
														: CommonUtility
																.quoteValue(
																		databaseConnection
																				.getProperties()
																				.getName(),
																		sampleColumn,
																		sampleTypeName,
																		list
																				.get(
																						n)
																				.getColumnName())),
												limit, offset[n]));
						if (!(disjoint != null && disjoint.trim()
								.equalsIgnoreCase(Resources.TrueOpt))) {
							String sbString;
							if (sb.indexOf("OFFSET") != -1) {
								sbString = sb
										.substring(0, sb.indexOf("OFFSET"));
								sb = new StringBuffer(sbString + ")");
							}
						}
						if (n < list.size() - 1) {
							sb.append(" UNION ALL ");
						}
					}
					sb.append(")");
				} else {
					sb.append(samplingMultiDB
							.stratifiedSamplingAppendStringConsistent(
									tempTableSample, randomTableName,
									sampleRowSize.get(i), i
											* sampleRowSize.get(i), columnNames
											.toString()));
				}
				sb.append(endingString);
				DatabaseUtil.alterParallel(databaseConnection, outputType);// for
				// oracle

				itsLogger.info( sb.toString());
				st.execute(sb.toString());
			}
		}
	}

	private List<SamplngColumnCount> calculateColumnNameCount(Statement st,
			String inputSchema, String inputTable, String sampleColumnName,
			String methodName) throws SQLException {
		ResultSet rs;
		String sqlGroupCount = "SELECT "
				+ StringHandler.doubleQ(sampleColumnName)
				+ " ,COUNT(*) AS CNT FROM "
				+ StringHandler.doubleQ(inputSchema) + "."
				+ StringHandler.doubleQ(inputTable) + " WHERE "
				+ StringHandler.doubleQ(sampleColumnName)
				+ " IS NOT NULL GROUP BY "
				+ StringHandler.doubleQ(sampleColumnName)
				+ " ORDER BY CNT DESC";
		itsLogger.info( sqlGroupCount);
		rs = st.executeQuery(sqlGroupCount);
		List<SamplngColumnCount> list = new ArrayList<SamplngColumnCount>();
		while (rs.next()) {
			SamplngColumnCount scc = new SamplngColumnCount();
			scc.setColumnName(rs.getString(1));
			scc.setColumnCount(rs.getLong(2));
			list.add(scc);
			if (list.size() > Integer.parseInt(typeSize))
				break;
		}
		rs.close();
		return list;
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.STRATIFIED_SAMPLEING_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.STRATIFIED_SAMPLEING_DESCRIPTION, locale));

		return nodeMetaInfo;
	}

	class SamplngColumnCount {
		private String columnName;
		private long columnCount;

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public long getColumnCount() {
			return columnCount;
		}

		public void setColumnCount(long l) {
			this.columnCount = l;
		}
	}

	protected void dropIfExist(DataSet dataSet, String outputSchema,
			String outputTable) throws OperatorException, AnalysisError {
		String tableType;
		String dropIfExist = getDropIfExist();
		StringBuilder sql = new StringBuilder();
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		try {
			String[] tableTypes = { "TABLE", "VIEW" };
			DatabaseMetaData md = databaseConnection.getConnection()
					.getMetaData();
			ResultSet rsTable = md.getTables(null, outputSchema, "%",
					tableTypes);
			while (rsTable.next()) {
				if (outputTable.equals(rsTable.getString("TABLE_NAME"))) {
					tableType = rsTable.getString("TABLE_TYPE");
					if (!dropIfExist.equalsIgnoreCase("yes")) {
						rsTable.close();
						throw new AnalysisError(this,
								AnalysisErrorName.Drop_if_Exist, config
										.getLocale(), tableType, outputSchema
										+ "." + outputTable);
					} else {
						if (tableType.equalsIgnoreCase("table")) {
							sql.append("drop table ");
						} else {
							sql.append("drop view ");
						}
						sql.append(
								getQuotaedTableName(outputSchema, outputTable))
								.append(" ").append(sqlGenerator.cascade());
						Statement st = databaseConnection
								.createStatement(false);
						itsLogger.info(
								"StratifiedSamplingAnalyzer.dropIfExist():sql="
										+ sql);
						st.execute(sql.toString());
					}

				}
			}
			rsTable.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	protected void dropIfExist(DataSet dataSet) throws OperatorException,
			AnalysisError {
		String tableType;
		String outputSchema = getOutputSchema();
		String outputTable = getOutputTable();
		String dropIfExist = getDropIfExist();

		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		try {
			String[] tableTypes = { "TABLE", "VIEW" };
			DatabaseMetaData md = databaseConnection.getConnection()
					.getMetaData();
			for (int i = 0; i < sampleCount; i++) {
				String tempOutputTable = outputTable + "_" + i;
				ResultSet rsTable = md.getTables(null, outputSchema, "%",
						tableTypes);
				StringBuilder sql = new StringBuilder();
				while (rsTable.next()) {
					if (tempOutputTable.equals(rsTable.getString("TABLE_NAME"))) {
						tableType = rsTable.getString("TABLE_TYPE");
						if (!dropIfExist.equalsIgnoreCase("yes")) {
							rsTable.close();
							throw new AnalysisError(this,
									AnalysisErrorName.Drop_if_Exist, config
											.getLocale(), tableType,
									outputSchema + "." + tempOutputTable);
						} else {
							if (tableType.equalsIgnoreCase("table")) {
								sql.append("drop table ");
							} else {
								sql.append("drop view ");
							}
							sql.append(
									getQuotaedTableName(outputSchema,
											tempOutputTable)).append(" ")
									.append(sqlGenerator.cascade());
							Statement st = databaseConnection
									.createStatement(false);
							itsLogger.info(
									"StratifiedSamplingAnalyzer.dropIfExist():sql="
											+ sql);
							st.execute(sql.toString());
						}

					}
				}
				rsTable.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
}
