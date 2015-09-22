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
import com.alpine.datamining.api.impl.algoconf.RandomSamplingConfig;
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
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Richie Lo
 * 
 */
public class RandomSamplingAnalyzer extends DataOperationAnalyzer {
	private static final String PERFORM_RANDOM_SAMPLING = "performRandomSampling";
	private static Logger logger= Logger.getLogger(RandomSamplingAnalyzer.class);
	
	private static final String randomTablePrefix = "sRand_";

	private static final String EXEC_UN_CONSISTENT_METHOD = "execUnConsistentMethod()";

	private static final String EXEC_CONSISTENT_METHOD = "execConsistentMethod()";

	private static final String tempTablePrefix = "s0_";

	private RandomSamplingConfig config = null;

	private int sampleCount;

	private String inputTableName;

	private String tempTableName;

	private IDataSourceInfo dataSourceInfo;

	private ISamplingMultiDB samplingMultiDB;

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

			config = (RandomSamplingConfig) source.getAnalyticConfig();
			sampleCount = Integer.parseInt(config.getSampleCount());
			analysisStorageParameterModel=config.getStorageParameters();
			if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
				appendOnlyString = " ";
			}else{
				appendOnlyString = sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel());
			}

			endingString = sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()); 

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
			tempTableName = getQuotaedTableName(getOutputSchema(),
					tempTablePrefix + getInputTable());
			dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(source
					.getDataSourceType());

			samplingMultiDB = SamplingMultiDBFactory
					.createSamplingMultiDB(source.getDataSourceType());
			samplingMultiDB.setSqlGenerator(sqlGenerator);

			performRandomSampling(databaseConnection, dataSet, sampleCount,
					getInputSchema(), getInputTable());
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo();
			AnalyzerOutPutSampling output = new AnalyzerOutPutSampling();
			List<AnalyzerOutPutTableObject> outTableList = new ArrayList<AnalyzerOutPutTableObject>();
			for (int i = 0; i < sampleCount; i++) {
				outTableList.add(getResultTableSampleRow(databaseConnection,
						dbInfo, config.getOutputSchema(), config
								.getOutputTable()
								+ "_" + i));
			}
			output.setSampleTables(outTableList);
			output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			return output;
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}
	}

	/**
	 * @param databaseConnection
	 * @throws OperatorException
	 */
	private void performRandomSampling(DatabaseConnection databaseConnection,
			DataSet dataSet, int numOfSamples, String inputSchema,
			String inputTable) throws AnalysisException, OperatorException {
		String methodName = PERFORM_RANDOM_SAMPLING;

		String outputType = getOutputType();
		String outputSchema = getOutputSchema();
		String outputTable = getOutputTable();
		String sampleSizeType = config.getSampleSizeType();
		AnalysisSampleSizeModel sampleSize = config.getSampleSize();
		String consistent = config.getConsistent();
		String replacement = config.getReplacement();
		String disjoint = config.getDisjoint();
		if (replacement == null)
			replacement = Resources.FalseOpt;
		List<Long> sampleRowSize=new ArrayList<Long>() ;
		long rowCount = 0;

		Statement st = null;

		try {
			databaseConnection.getConnection().setAutoCommit(false);
			st = databaseConnection.createStatement(false);
			// drop the database objects before creating the samples
			dropIfExist(dataSet);

			rowCount = dataSet.size();
			long sumSampleSize=0;
			if (sampleSizeType.equals(Resources.PercentageType)) {
				List<Double> percent = sampleSize.getSampleSizeList();
				for(double tempPercent : percent)
				{
					sampleRowSize.add(Math.round(rowCount *  tempPercent*0.01));
					sumSampleSize=sumSampleSize+Math.round(rowCount *  tempPercent*0.01);
				}
				
			} else {
				for(double tempSize : sampleSize.getSampleSizeList())
				{
					sampleRowSize.add(Math.round(tempSize*1.0));
					sumSampleSize=sumSampleSize+Math.round(tempSize*1.0);
				}
				 
				if ((replacement == null || replacement.equals(Resources.FalseOpt))) 
				{
					for(double tempSize : sampleSize.getSampleSizeList())
					{
						if (tempSize> rowCount)
						{
							logger.error(
									"SampleRowSize exceeds  rowCount");
							throw new AnalysisError(this,
									AnalysisErrorName.Too_Many_SampleRowSize,config.getLocale());
						}
					}
					if(disjoint != null&& disjoint.endsWith(Resources.TrueOpt)&& sumSampleSize> rowCount)
					{
						logger.error(
						"SampleRowSize exceeds  rowCount");
						throw new AnalysisError(this,
						AnalysisErrorName.Too_Many_SampleRowSize,config.getLocale());
					}
					
					
//					if (((disjoint == null || disjoint
//							.equals(Resources.FalseOpt)) && sampleRowSize > rowCount)
//							|| (disjoint != null
//									&& disjoint.endsWith(Resources.TrueOpt) && sampleRowSize
//									* sampleCount > rowCount)) {
//						logger.error(
//								"SampleRowSize exceeds  rowCount");
//						throw new AnalysisError(this,
//								AnalysisErrorName.Too_Many_SampleRowSize,config.getLocale());
					}
				}
		 
			StringBuffer columnNames = getColumnNames(databaseConnection
					.getConnection(), inputSchema, inputTable);

			if (replacement != null && replacement.equals(Resources.TrueOpt)) {
				execReplacementSampling(numOfSamples, methodName, outputType,
						sampleRowSize, st, columnNames, rowCount,
						databaseConnection);
			} else {

				String orderByClause = generateOrderByClause();
				if (consistent != null
						&& consistent.trim()
								.equalsIgnoreCase(Resources.TrueOpt)) {
					execConsistentMethod(dataSet, numOfSamples, sampleRowSize,
							rowCount, st, orderByClause, inputTable,
							outputType, outputSchema, outputTable, config
									.getRandomSeed(), columnNames, disjoint,
							databaseConnection);
				} else {
					execUnConsistentMethod(dataSet, numOfSamples, inputTable,
							outputType, outputSchema, outputTable,
							sampleRowSize, rowCount, st, columnNames, disjoint,
							databaseConnection);
				}
			}
			databaseConnection.commit();
			databaseConnection.getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			logger.error(e);
			throw new AnalysisException(e);
		} finally {
			try {
				if(st != null){
					st.close();
				}
				databaseConnection.getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}
		}
	}

	private void execReplacementSampling(int numOfSamples, String methodName,
			String outputType, List<Long> sampleRowSize, Statement st,
			StringBuffer columnNames, long rowCount,
			DatabaseConnection databaseConnection) throws SQLException,
			OperatorException {
		String tempTableName = "";
		if (dataSourceInfo.getDBType().endsWith(DataSourceInfoOracle.dBType)) {
			tempTableName = "r" + System.currentTimeMillis();
			StringBuilder sb_create = samplingMultiDB
					.generateReplacementTempTable(tempTableName, inputTableName);
			DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
			// oracle
			debugInfo(methodName, sb_create.toString());
			st.execute(sb_create.toString());
		} else if (dataSourceInfo.getDBType()
				.endsWith(DataSourceInfoDB2.dBType)) {
			tempTableName = "r" + System.currentTimeMillis();
			StringBuilder selectSql = samplingMultiDB
					.generateReplacementTempTable(tempTableName, inputTableName);
			StringBuilder createSql = new StringBuilder();
			createSql.append("create table ").append(tempTableName).append(
					" as (");
			createSql.append(selectSql).append(") ").append(
					sqlGenerator.setCreateTableEndingSql(null));
			debugInfo(methodName, createSql.toString());
			st.execute(createSql.toString());

			StringBuilder insertSql = new StringBuilder();
			insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
					tempTableName));

			if (insertSql.length() > 0) {
				st.execute(insertSql.toString());
				logger.info(
						"RandomSamplingAnalyzer.execReplacementSampling():insertTableSql="
								+ insertSql);
			}
		} else {
			tempTableName = inputTableName;
		}

		for (int i = 0; i < numOfSamples; i++) {
			if (dataSourceInfo.getDBType().endsWith(DataSourceInfoDB2.dBType)) {
				String resultTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);
				//?????????????????????????????
				String selectSql = samplingMultiDB
						.generateReplacementResultTable(
								tempTableName,
								outputType,
								resultTableName,
								columnNames.toString(),
								sampleRowSize.get(i),
								rowCount,
								AlpineMinerConfig.RANDOM_SAMPLING_COUNT_THRESHOLD,
								AlpineMinerConfig.RANDOM_SAMPLING_LIMIT_RATIO,
								AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_THRESHOLD,
								AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_LIMIT_RATIO,
								appendOnlyString,
								endingString);

				StringBuilder createSql = new StringBuilder();
				createSql.append("create ").append(outputType).append(" ")
						.append(resultTableName).append(appendOnlyString).append(" as (");
				createSql.append(selectSql).append(") ").append(
						endingString);
				debugInfo(methodName, createSql.toString());

				st.execute(createSql.toString());

				StringBuilder insertSql = new StringBuilder();
				insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
						resultTableName));

				if (insertSql.length() > 0) {
					st.execute(insertSql.toString());
					logger.info(
							"RandomSamplingAnalyzer.execReplacementSampling():insertTableSql="
									+ insertSql);
				}

			} else {
				String resultTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);
				String sqlCreateTable = samplingMultiDB
						.generateReplacementResultTable(
								tempTableName,
								outputType,
								resultTableName,
								columnNames.toString(),
								sampleRowSize.get(i),
								rowCount,
								AlpineMinerConfig.RANDOM_SAMPLING_COUNT_THRESHOLD,
								AlpineMinerConfig.RANDOM_SAMPLING_LIMIT_RATIO,
								AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_THRESHOLD,
								AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_LIMIT_RATIO,
								appendOnlyString,
								endingString);
				DatabaseUtil.alterParallel(databaseConnection, outputType);// for
				// oracle

				debugInfo(methodName, sqlCreateTable.toString());

				st.execute(sqlCreateTable.toString());

			}
		}
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

	private void execUnConsistentMethod(DataSet dataSet, int numOfSamples,
			String inputTable, String outputType, String outputSchema,
			String outputTablePrefix, List<Long> sampleRowSize, long totalCount,
			Statement st, StringBuffer columnNames, String disjoint,
			DatabaseConnection databaseConnection) throws SQLException,
			AnalysisError, OperatorException {
		String methodName = EXEC_UN_CONSISTENT_METHOD;
		String sampleTable;
		
		if (dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)) {
			if (disjoint != null && disjoint.equals(Resources.TrueOpt)) {
				dropIfExist(dataSet, outputSchema, tempTablePrefix + inputTable);
				String selectSql = samplingMultiDB.createTempTable(
						tempTableName, columnNames.toString(), inputTableName);
				StringBuilder createSql=new StringBuilder();
				createSql.append("create TABLE ").append(tempTableName).append(" ");
				createSql.append(" as (").append(selectSql).append(")").append(sqlGenerator.setCreateTableEndingSql(null));
				debugInfo(methodName, createSql.toString());
				st.executeUpdate(createSql.toString());
				
				StringBuilder insertSql = new StringBuilder();
				insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
						tempTableName));
				
				if (insertSql.length() > 0) {
					st.execute(insertSql.toString());
					logger.info(
							"RandomSamplingAnalyzer.execUnConsistentMethod():insertTableSql="
									+ insertSql);
				}
				
				sampleTable = tempTableName;
			} else {
				sampleTable = inputTableName;
			}
			
			long offset=0; 
			for (int i = 0; i < numOfSamples; i++) {
				String resultTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);
				//?????????????????????????
				if(i>0) offset+=sampleRowSize.get(i-1);
				String selectSql = samplingMultiDB.generateResultTable(
						resultTableName, outputType, sampleTable, columnNames
								.toString(), i, sampleRowSize.get(i), totalCount,
						AlpineMinerConfig.RANDOM_SAMPLING_COUNT_THRESHOLD,
						AlpineMinerConfig.RANDOM_SAMPLING_LIMIT_RATIO,
						AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_THRESHOLD,
						AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_LIMIT_RATIO,
						disjoint,
						appendOnlyString,
						endingString,offset);
				

				StringBuilder createSql=new StringBuilder();
				StringBuilder insertSql = new StringBuilder();
				createSql.append("create ").append(outputType).append(" ").append(resultTableName);
				if(getOutputType().equalsIgnoreCase(Resources.TableType)){
					createSql.append(appendOnlyString);
				}
				createSql.append(" as (").append(selectSql).append(")");
				if(getOutputType().equalsIgnoreCase(Resources.TableType)){
					createSql.append(endingString);
					insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
							resultTableName));
				}
				debugInfo(methodName, createSql.toString());
				st.executeUpdate(createSql.toString());
				
				if (insertSql.length() > 0) {
						st.execute(insertSql.toString());
						logger.info(
								"RandomSamplingAnalyzer.execReplacementSampling():insertTableSql="
										+ insertSql);
				}
						
			}
		}else{
			if (disjoint != null && disjoint.equals(Resources.TrueOpt)) {
				dropIfExist(dataSet, outputSchema, tempTablePrefix + inputTable);
				String sqlCreateTempTable = samplingMultiDB.createTempTable(
						tempTableName, columnNames.toString(), inputTableName);

				DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
				// oracle

				debugInfo(methodName, sqlCreateTempTable);
				st.executeUpdate(sqlCreateTempTable);
				sampleTable = tempTableName;
			} else {
				sampleTable = inputTableName;
			}
			long offset=0;
			for (int i = 0; i < numOfSamples; i++) {
				
				if(i>0) offset+=sampleRowSize.get(i-1);
				String resultTableName = getQuotaedTableName(getOutputSchema(),
						getOutputTable() + "_" + i);
				String sqlCreateResultTable = samplingMultiDB.generateResultTable(
						resultTableName, outputType, sampleTable, columnNames
								.toString(), i, sampleRowSize.get(i), totalCount,
						AlpineMinerConfig.RANDOM_SAMPLING_COUNT_THRESHOLD,
						AlpineMinerConfig.RANDOM_SAMPLING_LIMIT_RATIO,
						AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_THRESHOLD,
						AlpineMinerConfig.RANDOM_SAMPLING_SAMPLESIZE_LIMIT_RATIO,
						disjoint,
						appendOnlyString, 
						endingString,offset);
				debugInfo(methodName, sqlCreateResultTable);
				DatabaseUtil.alterParallel(databaseConnection, outputType);// for
				// oracle
				st.executeUpdate(sqlCreateResultTable);
				
			}
		}

	}

	private void execConsistentMethod(DataSet dataSet, int numOfSamples,
			List<Long> sampleRowSize, long rowCount, Statement st,
			String orderByClause, String inputTable, String outputType,
			String outputSchema, String outputTablePrefix, String randomSeed,
			StringBuffer columnNames, String disjoint,
			DatabaseConnection databaseConnection) throws SQLException,
			AnalysisError, OperatorException {
		String methodName = EXEC_CONSISTENT_METHOD;
		dropIfExist(dataSet, outputSchema, tempTablePrefix + inputTable);

		if (dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)) {
			StringBuilder createSql=new StringBuilder();
			StringBuilder selectSql=new StringBuilder();
			createSql.append("create table ").append(tempTableName).append(" as (");
			selectSql.append(" select ").append(columnNames).append(", row_number() over (");
			selectSql.append(orderByClause).append(" ) as alpine_sample_id from ").append(inputTableName);
			createSql.append(selectSql).append(")").append(sqlGenerator.setCreateTableEndingSql(null));
		
			debugInfo(methodName, createSql.toString());
			st.executeUpdate(createSql.toString());
			
			StringBuilder insertSql = new StringBuilder();
			insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
					tempTableName));
			
			if (insertSql.length() > 0) {
				st.execute(insertSql.toString());
				logger.info(
						"RandomSamplingAnalyzer.execConsistentMethod():insertTableSql="
								+ insertSql);
			}
			
		} else {
			String sqlCreateTempTable = "CREATE TABLE " + tempTableName
					+ DatabaseUtil.addParallel(databaseConnection, "TABLE")
					+ " AS " + "SELECT " + columnNames.toString()
					+ ",row_number() over(" + orderByClause
					+ ") AS alpine_sample_id from " + inputTableName;
			sqlCreateTempTable += sqlGenerator.setCreateTableEndingSql(null);
			DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
																	// oracle

			debugInfo(methodName, sqlCreateTempTable);
			st.executeUpdate(sqlCreateTempTable);
		}

		if (disjoint != null && disjoint.equals(Resources.TrueOpt)) {
			String randomTableName = getQuotaedTableName(outputSchema,
					randomTablePrefix + inputTable);
			dropIfExist(dataSet, outputSchema, randomTablePrefix + inputTable);
			
			if (dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)) {
				StringBuilder createSql=new StringBuilder();
				createSql.append("create table ").append(randomTableName).append(" (alpine_sample_id BIGINT, rand_order float) ");
				
				debugInfo(methodName, createSql.toString());
				st.executeUpdate(createSql.toString());
				
				StringBuilder insertSql=new StringBuilder();
				insertSql.append("call alpine_miner_generate_random_table('").append(randomTableName).append("',").append(rowCount).append(",");
				insertSql.append(randomSeed).append(")");
				
				debugInfo(methodName, insertSql.toString());
				st.executeUpdate(insertSql.toString());
				
				for (int i = 0; i < numOfSamples; i++) {
					String sampleTableName = getQuotaedTableName(getOutputSchema(),
							getOutputTable() + "_" + i);
					createSql.setLength(0);
					String selectSql = samplingMultiDB.generateResultTable(
							sampleTableName, outputType, tempTableName,
							randomTableName, columnNames.toString(), i,
							sampleRowSize.get(i), disjoint, appendOnlyString, endingString);
					createSql.append("create ").append(outputType).append(" ").append(sampleTableName);
					if(outputType.equals(Resources.TableType)){
						createSql.append(appendOnlyString);
					}
					createSql.append(" as (").append(selectSql).append(")");

										
					if(outputType.equals(Resources.TableType)){
						createSql.append(endingString);
						insertSql.setLength(0);
						insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
								sampleTableName));
					}		
					debugInfo(methodName, createSql.toString());
					st.executeUpdate(createSql.toString());
					
					if (insertSql.length() > 0) {
						st.execute(insertSql.toString());
						logger.info(
								"RandomSamplingAnalyzer.execConsistentMethod():insertTableSql="
										+ insertSql);
					}
				}
				
			}else{
				if (randomSeed != null && !randomSeed.trim().isEmpty()) {
					String sql = sqlGenerator.setSeed(randomSeed, null, null);
					debugInfo(methodName, sql);
					st.executeQuery(sql);
				}

				String sqlCreateRandomTable = samplingMultiDB.createRandomTable(
						randomTableName, rowCount);
				DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
				// oracle
				debugInfo(methodName, sqlCreateRandomTable);
				st.executeUpdate(sqlCreateRandomTable);

				for (int i = 0; i < numOfSamples; i++) {
					String sampleTableName = getQuotaedTableName(getOutputSchema(),
							getOutputTable() + "_" + i);
					String sqlCreateTable = samplingMultiDB.generateResultTable(
							sampleTableName, outputType, tempTableName,
							randomTableName, columnNames.toString(), i,
							sampleRowSize.get(i), disjoint,appendOnlyString, endingString);
					DatabaseUtil.alterParallel(databaseConnection, outputType);// for
					// oracle
					debugInfo(methodName, sqlCreateTable);
					st.executeUpdate(sqlCreateTable);
				}
			}
		} else {
			if (randomSeed == null || randomSeed.trim().isEmpty())
				return;
			
			if (dataSourceInfo.getDBType().equals(DataSourceInfoDB2.dBType)) {			
				for (int i = 0; i < numOfSamples; i++) {
					String randomTableName = getQuotaedTableName(outputSchema,
							randomTablePrefix + inputTable);
					dropIfExist(dataSet, outputSchema, randomTablePrefix + inputTable);
					StringBuilder createSql=new StringBuilder();
					createSql.append("create table ").append(randomTableName).append(" (alpine_sample_id BIGINT, rand_order float) ");
					
					debugInfo(methodName, createSql.toString());
					st.executeUpdate(createSql.toString());
					
					StringBuilder insertSql=new StringBuilder();
					insertSql.append("call alpine_miner_generate_random_table('").append(randomTableName).append("',").append(rowCount).append(",");
					double newSeed=Double.parseDouble(randomSeed)*10/numOfSamples+i;
					insertSql.append(newSeed).append(")");
					
					debugInfo(methodName, insertSql.toString());
					st.executeUpdate(insertSql.toString());
					
					String sampleTableName = getQuotaedTableName(getOutputSchema(),
							getOutputTable() + "_" + i);
					createSql.setLength(0);
					//////////?????????????????
					String selectSql = samplingMultiDB.generateResultTable(
							sampleTableName, outputType, tempTableName,
							randomTableName, columnNames.toString(), i,
							sampleRowSize.get(i), disjoint, appendOnlyString, endingString);
					createSql.append("create ").append(outputType).append(" ").append(sampleTableName);
					createSql.append(" as (").append(selectSql).append(")");
				
					if(outputType.equals(Resources.TableType)){
						createSql.append(sqlGenerator.setCreateTableEndingSql(null));
						insertSql.setLength(0);
						insertSql.append(sqlGenerator.insertTable(selectSql.toString(),
								sampleTableName));
					}
					
					debugInfo(methodName, createSql.toString());
					st.executeUpdate(createSql.toString());
								
					if (insertSql.length() > 0) {
						st.execute(insertSql.toString());
						logger.info(
								"RandomSamplingAnalyzer.execConsistentMethod():insertTableSql="
										+ insertSql);
					}
				}
			}else{
				for (int i = 0; i < numOfSamples; i++) {

					String sql = sqlGenerator.setSeed(randomSeed, numOfSamples, i);
					debugInfo(methodName, sql);
					st.execute(sql);

					String randomTableName = getQuotaedTableName(outputSchema,
							randomTablePrefix + inputTable + "_" + i);
					dropIfExist(dataSet, outputSchema, randomTablePrefix
							+ inputTable + "_" + i);
					String sqlCreateRandomTable = samplingMultiDB
							.createRandomTable(randomTableName, rowCount);
					DatabaseUtil.alterParallel(databaseConnection, "TABLE");// for
					// oracle

					debugInfo(methodName, sqlCreateRandomTable);
					st.executeUpdate(sqlCreateRandomTable);

					String sampleTableName = getQuotaedTableName(getOutputSchema(),
							getOutputTable() + "_" + i);

					String sqlCreateTable = samplingMultiDB.generateResultTable(
							sampleTableName, outputType, tempTableName,
							randomTableName, columnNames.toString(), i,
							sampleRowSize.get(i), disjoint, appendOnlyString, endingString);
					DatabaseUtil.alterParallel(databaseConnection, outputType);// for
					// oracle
					debugInfo(methodName, sqlCreateTable);
					st.executeUpdate(sqlCreateTable);
				}
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
			logger.error(e);
			throw e;
		} finally {
			rsCol.close();
		}
		return columnNames;
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.RANDOM_SAMPLEING_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.RANDOM_SAMPLEING_DESCRIPTION,locale));
		return nodeMetaInfo;
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
								AnalysisErrorName.Drop_if_Exist,config.getLocale(), tableType,
								outputSchema + "." + outputTable);
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
						logger.info(
								"RandomSamplingAnalyzer.dropIfExist():sql="
										+ sql);
						st.execute(sql.toString());
					}

				}
			}
			rsTable.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
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
									AnalysisErrorName.Drop_if_Exist, config.getLocale(),tableType,
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
							logger.info(
									"RandomSamplingAnalyzer.dropIfExist():sql="
											+ sql);
							st.execute(sql.toString());
						}

					}
				}
				rsTable.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	private void debugInfo(String methodName, String message) {
		logger.info(
				getClass().getName() + "." + methodName + ": sql=" + message);
	}
}
