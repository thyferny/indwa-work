/**
 * ClassName FPGrowthDBAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.association;

import java.sql.CallableStatement;
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
import com.alpine.datamining.api.impl.algoconf.FPGrowthConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAssociationAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutAssociationRule;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Container;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.operator.fpgrowth.AssociationRule;
import com.alpine.datamining.operator.fpgrowth.AssociationRules;
import com.alpine.datamining.operator.fpgrowth.FPGrowthDB;
import com.alpine.datamining.operator.fpgrowth.FPGrowthParameter;
import com.alpine.datamining.operator.fpgrowth.Item;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 *
 */
public class FPGrowthAnalyzer extends AbstractDBAssociationAnalyzer{
	private static Logger logger= Logger.getLogger(FPGrowthAnalyzer.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source) throws AnalysisException {
		
		try {
 
			DataSet dataSet = getDataSet((DataBaseAnalyticSource)source, source.getAnalyticConfig());
			
			Operator fPGrowth = OperatorUtil.createOperator(FPGrowthDB.class);
			FPGrowthParameter parameter = new FPGrowthParameter();
			FPGrowthConfig config=(FPGrowthConfig)source.getAnalyticConfig();
			setSpecifyColumn(dataSet, config);
			parameter.setSupport(Double.parseDouble(config.getMinSupport()));
			parameter.setPositiveValue(config.getExpressionModel().getPositiveValue());
			parameter.setExpression(config.getExpressionModel().getExpression());
			parameter.setTableSizeThreshold(Integer.parseInt(config.getTableSizeThreshold()));
			parameter.setColumnName(config.getColumnNames());
			if(!StringUtil.isEmpty(config.getUseArray())){
				parameter.setUseArray(Boolean.parseBoolean(config.getUseArray()));
			}
//			fPGrowth.setParameter(FPGrowthDB.PARAMETER_MIN_SUPPORT,config.getMinSupport());
//			fPGrowth.setParameter(FPGrowthDB.PARAMETER_POSITIVE_VALUE, config.getPositiveValue());
//			fPGrowth.setParameter(FPGrowthDB.PARAMETER_TABLE_SIZE_THRESHOLD, config.getTableSizeThreshold());
//			fPGrowth.setParameter(FPGrowthDB.PARAMETER_COLUMN_NAMES, config.getColumnNames());
			fPGrowth.setParameter(parameter);
			Container fPGrowthResult = fPGrowth.apply(new Container(dataSet));
			AnalyticOutPut outPut = generateRules(config, fPGrowthResult);
		
			
			AnalyticNodeMetaInfo nodeMetaInfo=createNodeMetaInfo(config.getLocale());
			 
			outPut.setAnalyticNodeMetaInfo(nodeMetaInfo);
			
			//AnalyticEngine.instance.unRegistryResourceConnection(String.valueOf(this.hashCode()) );
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();

			DBSource resultDataSource = getResultDataSource((DataBaseAnalyticSource)source, 
					config,
					((AnalyzerOutPutAssociationRule)outPut).getRules(),
					databaseConnection);
			
			AnalyzerOutPutDataBaseUpdate result=new AnalyzerOutPutDataBaseUpdate();
//			 set url user pwd ,schema, table
			fillDBInfo(result, (DataBaseAnalyticSource)source);
			DataSet resultDataSet = resultDataSource.createDataSetUsingExitingDBConnection(
					((DBTable) dataSet.getDBTable()).getDatabaseConnection()
					, ((DatabaseSourceParameter)resultDataSource.getParameter()).getTableName(), 
					false);
			result.setDataset(resultDataSet);
			result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			return result;
		} 
		catch (Exception e) {
			logger.error(e) ;
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} 
			else if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		} 
		
	}
	protected DBSource getResultDataSource(
			DataBaseAnalyticSource para, AnalyticConfiguration config,
			AssociationRules rules, DatabaseConnection databaseConnection)
	throws OperatorException
	{
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(para.getDataBaseInfo().getSystem());
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(para.getDataBaseInfo().getSystem());
		String OutputSchema= ((FPGrowthConfig)config).getOutputSchema();
		String OutputTable=((FPGrowthConfig)config).getOutputTable();
		String DropIfExist=((FPGrowthConfig)config).getDropIfExist();
		String newTableName = getQuotaedTableName(OutputSchema, OutputTable);
		AnalysisStorageParameterModel analysisStorageParameterModel = ((FPGrowthConfig)config).getStorageParameters();
		boolean appendOnly = false;
		if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
			appendOnly = false;
		}else{
			appendOnly = true;
		}

		Connection conn =null;
		Statement st = null;
		try {
			conn =para.getConnection();
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e);
		} 

		StringBuilder dropSql = new StringBuilder();
		if (DropIfExist.equalsIgnoreCase("yes")) {
			if (para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoOracle.dBType)) {
				int tableExistInt = 0;
				StringBuffer tableExist = new StringBuffer();
				tableExist.append("select count(*) from dba_tables where owner = '").append(OutputSchema).append("' and table_name = '").append(OutputTable).append("'");
				ResultSet rs;
				try {
					logger
					.debug("FPGrowthAnalyzer.getResultDataSource():sql="
							+ tableExist.toString());
					rs = st.executeQuery(tableExist.toString());
					while(rs.next())
					{
						tableExistInt = rs.getInt(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new OperatorException(e1.getLocalizedMessage());
				}
				if (tableExistInt > 0)
				{
					dropSql.append("drop table ");
					dropSql.append(newTableName);
					try {
						logger
						.debug("FPGrowthAnalyzer.getResultDataSource():sql="
								+ dropSql.toString());
						st.execute(dropSql.toString());
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error(e);
						throw new OperatorException(e);
					}
				}
			}else if(para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoDB2.dBType)){
				dropSql.append("call PROC_DROPSCHTABLEIFEXISTS( '").append(StringHandler.doubleQ(OutputSchema)).append("','").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					logger
					.debug("FPGrowthAnalyzer.getResultDataSource():sql="
							+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e);
				}
			}else if(para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoNZ.dBType)){
				dropSql.append("call droptable_if_existsdoubleq('").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					logger
					.debug("FPGrowthAnalyzer.getResultDataSource():sql="
							+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
//					e.printStackTrace();
					logger.error(e);
					throw new OperatorException(e);
				}
			}else{
				dropSql.append("drop table if exists ");
				dropSql.append(newTableName);
				try {
					logger
					.debug("FPGrowthAnalyzer.getResultDataSource():sql="
							+ dropSql.toString());
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e);
					throw new OperatorException(e);
				}
			}
		}
		try {
			DatabaseUtil.alterParallel(databaseConnection, "TABLE");
			CallableStatement stpCall = null;
			StringBuilder createSql = new StringBuilder();
			if(para.getDataBaseInfo().getSystem().equals(DataSourceInfoDB2.dBType)){
				createSql.append("create table ").append(newTableName).append("(premise clob");
				createSql.append(",conclusion  clob");
				createSql.append(",confidence float, support float) ");
				StringBuffer insertSql = new StringBuffer();
				insertSql.append("insert into ").append(newTableName).append(" values(?,?,?,?)");
				stpCall = databaseConnection.getConnection().prepareCall(insertSql.toString()); /* con is the connection */
			}else if(para.getDataBaseInfo().getSystem().equals(DataSourceInfoNZ.dBType)){
					createSql.append("create table ").append(newTableName).append("(premise varchar(32000)");
					createSql.append(",conclusion  varchar(32000)");
					createSql.append(",confidence double, support double) ");
			}else{
				createSql.append("create table ").append(newTableName).append("(premise ");
				createSql.append(sqlGenerator.textArray()).append(",conclusion ").append(sqlGenerator.textArray());
				createSql.append(",confidence float, support float) ");
				createSql.append(appendOnly ? sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel()) : " ");
				createSql.append(DatabaseUtil.addParallel(databaseConnection.getProperties().getName()));
				createSql.append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()));
			}
			logger.debug("FPGrowthAnalyzer.getResultDataSource():sql="+createSql.toString());
			st.execute(createSql.toString());
			StringBuffer insertSql = new StringBuffer();
			Statement stBatch = null;
			if(!para.getDataBaseInfo().getSystem().equals(DataSourceInfoDB2.dBType)){
				conn.setAutoCommit(false);
				stBatch = conn.createStatement();
			}
			int count = 0;

				for (AssociationRule rule : rules) {
					count++;
					insertSql.setLength(0);
					StringBuffer premise = new StringBuffer();
					StringBuffer conclusion = new StringBuffer();

					if(para.getDataBaseInfo().getSystem().equals(DataSourceInfoDB2.dBType)){
						premise = new StringBuffer("'");
						boolean first = true;
						for(Item item: rule.getPremise()){
							if (first){
								first = false;
							}
							else{
								premise.append(",");
							}
							premise.append(StringHandler.escQ(item.toString()));
						}
						premise.append("'");
						conclusion = new StringBuffer("'");
						first = true;
						for(Item item:rule.getConclusion()){
							if (first){
								first = false;
							}
							else{
								conclusion.append(",");
							}
							conclusion.append(StringHandler.escQ(item.toString()));
						}
						conclusion.append("'");
						stpCall.setString(1, premise.toString());
						stpCall.setString(2, conclusion.toString());
						stpCall.setDouble(3, rule.getConfidence());
						stpCall.setDouble(4, rule.getTotalSupport());
						stpCall.executeUpdate();
					}else if(para.getDataBaseInfo().getSystem().equals(DataSourceInfoNZ.dBType)){
						premise = new StringBuffer("'");
						boolean first = true;
						for(Item item: rule.getPremise()){
							if (first){
								first = false;
							}
							else{
								premise.append(",");
							}
							premise.append(StringHandler.escQ(item.toString()));
						}
						premise.append("'");
						conclusion = new StringBuffer("'");
						first = true;
						for(Item item:rule.getConclusion()){
							if (first){
								first = false;
							}
							else{
								conclusion.append(",");
							}
							conclusion.append(StringHandler.escQ(item.toString()));
						}
						conclusion.append("'");
						insertSql.append("insert into ").append(newTableName).append(" values(").append(premise).append(",")
						.append(conclusion).append(",").append(rule.getConfidence()).append(",").append(rule.getTotalSupport()).append(")");
//						logger
//							.debug("FPGrowthAnalyzer.getResultDataSource():sql="
//									+ insertSql.toString());
//						st.executeUpdate(insertSql.toString());
					}else{
						premise = new StringBuffer(multiDBUtility.stringArrayHead());
						boolean first = true;
						for(Item item: rule.getPremise()){
							if (first){
								first = false;
							}
							else{
								premise.append(",");
							}
							premise.append("'").append(item.toString()).append("'");
						}
						premise.append(multiDBUtility.stringArrayTail());
						conclusion = new StringBuffer(multiDBUtility.stringArrayHead());
						first = true;
						for(Item item:rule.getConclusion()){
							if (first){
								first = false;
							}
							else{
								conclusion.append(",");
							}
							conclusion.append("'").append(item.toString()).append("'");
						}
						conclusion.append(multiDBUtility.stringArrayTail());
						insertSql.append("insert into ").append(newTableName).append(" values(").append(premise).append(",")
						.append(conclusion).append(",").append(rule.getConfidence()).append(",").append(rule.getTotalSupport()).append(")");
//						logger
//							.debug("FPGrowthAnalyzer.getResultDataSource():sql="
//									+ insertSql.toString());
//						st.executeUpdate(insertSql.toString());
					}
					if(!para.getDataBaseInfo().getSystem().equals(DataSourceInfoDB2.dBType)){
						try {
							logger
							.debug("FPGrowthAnalyzer.getResultDataSource():sql="
									+ insertSql.toString());
							stBatch.addBatch(insertSql.toString());
							if(count % AlpineMinerConfig.FP_INSERT_COUNT == 0 || rules.getNumberOfRules() == count){
								stBatch.executeBatch();	
								conn.commit();
							}
						} catch (SQLException e) {
							throw new OperatorException(e);
						}
						
					}
//					count++;
				}
				if(para.getDataBaseInfo().getSystem().equals(DataSourceInfoDB2.dBType)){
					try {
						String sql="CALL SYSPROC.ADMIN_CMD(' REORG TABLE " + newTableName+" ')";
						logger.debug(sql);
						st.execute(sql);
					} catch (SQLException e) {
						logger.info(e);
						throw new OperatorException(e);
					}
				}
			para.getTableInfo().setTableName(StringHandler.doubleQ(OutputTable));
			para.getTableInfo().setSchema(StringHandler.doubleQ(OutputSchema));
			if(stpCall != null){
				stpCall.close();
			}
			conn.setAutoCommit(true);

			if(st != null){
				st.close();
			}
			if(stBatch != null){
				stBatch.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e);
		} 

		DBSource dataSource = OperatorUtil
		.createOperator(DBSource.class);
		
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();

		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para
				.getDataBaseInfo().getSystem());
		parameter.setTableName(newTableName);
		parameter.setUrl(para
				.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		dataSource.setParameter(parameter);
		return  dataSource;
	}

	/**
	 * @param locale 
	 * @return
	 */
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ASSOCIATION_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ASSOCIATION_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

	
}
