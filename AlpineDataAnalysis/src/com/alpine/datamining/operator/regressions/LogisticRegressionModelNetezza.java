
package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class LogisticRegressionModelNetezza extends LogisticRegressionModelDB {
    private static final Logger itsLogger = Logger.getLogger(LogisticRegressionModelNetezza.class);

    
	private static final long serialVersionUID = -3965373796731290645L;
	private Double[] betas;
	private String[] columns;
	private int aliasCount;

	public LogisticRegressionModelNetezza(DataSet dataSet,DataSet oldDataSet, double[] beta, double[] variance, boolean interceptAdded, String goodValue) {
        super( dataSet, oldDataSet,  beta,  variance, interceptAdded, goodValue);
    }

	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException{
		if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1 || (AlpineDataAnalysisConfig.NZ_PROCEDURE_SWITCH == 0 && dataSet.getColumns().size() < AlpineDataAnalysisConfig.NZ_PROCEDURE_COLUMN_LIMIT)){
			if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1){
				return performPredictionSqlAlias(dataSet,predictedLabel);
			}
				
			return super.performPrediction(dataSet,predictedLabel);
		}else{
			return performPredictionProcedure(dataSet,predictedLabel);
		}
	}
	public DataSet performPredictionProcedure(DataSet dataSet, Column predictedLabel)
	throws OperatorException {	

		String newTableName = ((DBTable) dataSet.getDBTable()).getTableName();
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		long currentTime = System.currentTimeMillis();
		String newTempTableName = "NT" + currentTime;
		String resultTableName = "R" + currentTime;
		String betaTableName = "B"+currentTime;
		String columnTableName = "C"+currentTime;

		Statement st = null;
		StringBuilder sql = new StringBuilder();

		try {
			st = databaseConnection.createStatement(false);
			TableTransferParameter.createDoubleTable(betaTableName, st);
			TableTransferParameter.createStringTable(columnTableName, st);
			generateBetaAndColumns(oldDataSet);
			TableTransferParameter.insertTable(betaTableName, st, betas);
			TableTransferParameter.insertTable(columnTableName, st, columns);
			sql.append("create table ").append(newTempTableName).append(
					" as select *, row_number() over(order by 1) as  "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" from ").append(newTableName);
			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuilder();
			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuilder();
			sql.append("create  table ").append(resultTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint, prediction double)");
			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer where = getWhere(oldDataSet);

		sql = new StringBuilder();

		sql.append("call  alpine_miner_lr_ca_predict_proc('");
		sql.append(newTempTableName).append("','") 
		.append(where).append("','") 
		.append(betaTableName).append("','") 
		.append(columnTableName).append("',");
		addIntercept(sql);
		sql.append(",'")
		.append(resultTableName).append("')");

			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			String goodColumn = good;
			String badColumn;
			if (getLabel().getMapping().mapIndex(0).equals(good))
			{
				badColumn = getLabel().getMapping().mapIndex(1);
			}
			else if(getLabel().getMapping().mapIndex(1).equals(good))
			{
				badColumn = getLabel().getMapping().mapIndex(0);
			}
			else
			{
				itsLogger.error(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.GOOD_VALUE_NOT_EXIST, AlpineThreadLocal.getLocale()));
				throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.GOOD_VALUE_NOT_EXIST, AlpineThreadLocal.getLocale()));
			}
			goodColumn=StringHandler.escQ(goodColumn);
			badColumn=StringHandler.escQ(badColumn);
			StringBuilder predictionStringsb = new StringBuilder("(case when ");
			predictionStringsb.append(resultTableName).append(".prediction").append(" > 0.5 then ");
			appendValue(goodColumn, predictionStringsb);
			predictionStringsb.append(" else ");
			appendValue(badColumn, predictionStringsb);
			predictionStringsb.append(" end)");
			String predictedLabelName=StringHandler.doubleQ(predictedLabel.getName());

			StringBuffer sqltemp = new StringBuffer();
			sqltemp.append("update ").append(newTempTableName).append(" set  ");

			sqltemp.append(predictedLabelName).append(" = ").append(predictionStringsb).append(",");
			sqltemp.append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + goodColumn).getName())).append("=").append(resultTableName).append(".prediction")
			.append(",").append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + badColumn).getName())).append(" = 1 - ").append(resultTableName).append(".prediction");
			sqltemp.append(
			" from ").append(resultTableName).append(
			" where  ").append(resultTableName).append(
			"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTempTableName).append(
			"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");

            itsLogger.debug(
							"LogisticRegressionModelNetezza.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuilder();
			sql.append("drop table ").append(newTableName);
			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuilder();
			StringBuffer columnSql = new StringBuffer();
			Iterator<Column> allColumns = dataSet.getColumns().allColumns();
			boolean first = true;
			while(allColumns.hasNext()){
				Column column = allColumns.next();
				if(!column.getName().equals(""+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"")){
					if(first){
						first = false;
					}else{
						columnSql.append(",");
					}
					columnSql.append(StringHandler.doubleQ(column.getName()));
				}
			}
			sql.append("create table ").append(newTableName).append(
					" as select ").append(columnSql).append(" from ").append(newTempTableName);
			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuilder();
			sql.append("drop table ").append(newTempTableName);
			itsLogger.debug(
					"LogisticRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			TableTransferParameter.dropResultTable(betaTableName, st);
			TableTransferParameter.dropResultTable(columnTableName, st);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}
	private void generateBetaAndColumns(DataSet dataSet) {
		HashMap<String, Double> betaMap = getBetaMap();
		betas = new Double[betaMap.size()];
		columns = new String[betaMap.size() - 1];
        if (interceptAdded) {
        	betas[betas.length - 1] = beta[beta.length - 1];
        }
		int i = 0; 
		for(Column column : dataSet.getColumns())
		{
			 String columnName = StringHandler.doubleQ(column.getName());
			 if(column.isNumerical())
			 {
			 		if(betaMap.get(column.getName())==null)continue;
					double beta=betaMap.get(column.getName());
					betas[i] = beta;
					columns[i] = StringHandler.escQ(columnName);
					i++;
			 }else
			 {
				 List<String> mapList=column.getMapping().getValues();
	    		HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    		TransformMap_valueKey= getAllTransformMap_valueKey().get(column.getName());
	    		if(TransformMap_valueKey==null)continue;
	    		Iterator<String> mapList_i=mapList.iterator();
	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(betaMap.get(columnname)==null)continue;
 	 					double beta=betaMap.get(columnname);
 	 					value=StringHandler.escQ(value);
 						betas[i] = beta;
 						columns[i] = StringHandler.escQ(" (case"+" when "+columnName+"="+"'"+value+"' then 1.0 else 0.0 end)");
 						i++;
 	 				}
			 }
		}
		
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(betaMap.get(key)!= null){
				betas[i] = betaMap.get(key);
				columns[i] = StringHandler.escQ(value);
				i++;
			}
		}
	}
	protected StringBuilder getProbability() {
		StringBuilder probability = null;
		probability = getProbabilitySql(oldDataSet);
		return probability;
	}
	
	protected void appendUpdateSet(DataSet dataSet, StringBuilder sql,
			StringBuilder functionValuesb, String goodColumn, String badColumn,
			StringBuilder predictionStringsb, String predictedLabelName) {
		
		sql.append(" set ").append(predictedLabelName).append(" = ").append(predictionStringsb).append(",")
		.append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + goodColumn).getName())).append(" = ").append(functionValuesb)
		.append(",").append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + badColumn).getName())).append(" = 1.0 - ").append(functionValuesb);
	}

	protected void addIntercept(StringBuilder probability) {
		if (interceptAdded)
		{
			probability.append("1");
		}
		else
		{
			probability.append("0");
		}
	}
	public DataSet performPredictionSqlAlias(DataSet dataSet, Column predictedLabel) throws OperatorException
	{

		Statement st = null;
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		String tableName=((DBTable) dataSet.getDBTable())
		.getTableName();
		String predictedLabelName = StringHandler.doubleQ(predictedLabel.getName());
		long currentTime = System.currentTimeMillis();
		String newTempTableName = "NT" + currentTime;
		String resultTableName = "R" + currentTime;

		String goodColumn = good;
		String badColumn;
		if (getLabel().getMapping().mapIndex(0).equals(good))
		{
			badColumn = getLabel().getMapping().mapIndex(1);
		}
		else if(getLabel().getMapping().mapIndex(1).equals(good))
		{
			badColumn = getLabel().getMapping().mapIndex(0);
		}
		else
		{
            itsLogger.error(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.GOOD_VALUE_NOT_EXIST, AlpineThreadLocal.getLocale()));
			throw new OperatorException(AlpineDataAnalysisLanguagePack.getMessage(AlpineDataAnalysisLanguagePack.GOOD_VALUE_NOT_EXIST, AlpineThreadLocal.getLocale()));
		}
		goodColumn=StringHandler.escQ(goodColumn);
		badColumn=StringHandler.escQ(badColumn);
		String goodConfidence = StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + goodColumn).getName());
		String badConfidence = StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + badColumn).getName());

		StringBuffer predict = new StringBuffer(" select "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+", ");
		predict.append(getProbabilitySqlAlias(oldDataSet));
		predict.append(" from ").append(newTempTableName);
		StringBuffer predictedY = new StringBuffer("(e0");
		for(int i = 0; i < aliasCount - 1; i++){
			predictedY.append("+e").append(i+1);
		}
		predictedY.append(")");

		StringBuffer sql = new StringBuffer();
		try {
		sql.append("create table ").append(newTempTableName).append(
		" as select *, row_number() over(order by 1) as  "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" from ").append(tableName);
		itsLogger.debug(
		"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());


		sql = new StringBuffer();
		sql.append("drop table ").append(tableName);
		itsLogger.debug(
				"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());


		sql = new StringBuffer();
		sql.append("create table ").append(resultTableName).append(" as select "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+", ").append(predictedY).append(" as ").append(goodConfidence).append(" from (").append(predict).append("  limit all )foo");
		itsLogger.debug(
				"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());

		sql = new StringBuffer();
		StringBuffer columnSql = new StringBuffer();
		Iterator<Column> allColumns = dataSet.getColumns().allColumns();
		boolean first = true;
		while(allColumns.hasNext()){
			Column column = allColumns.next();
			if(!column.getName().equals(""+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"")){
				if(first){
					first = false;
				}else{
					columnSql.append(",");
				}
				if(column.getName().equals(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + good).getName())){
					StringBuffer gx = new StringBuffer();
					gx.append(resultTableName).append(".");
					gx.append(StringHandler.doubleQ(StringHandler.escQ((column.getName())))).append("::double");
					StringBuffer gxSim = new StringBuffer();
					gxSim.append("(case when ").append(gx).append(" > 30 then 30 when  ").append(gx).append(" < -30 then -30 else ").append(gx).append(" end)");
					StringBuilder probability=new StringBuilder("");

					probability.append("( ").append(" 1.0::double /(").append((" 1.0::double ")).append(" +exp(-(").append(gxSim).append(")::double)::double))");
					columnSql.append(probability).append(" as ");
					columnSql.append(StringHandler.doubleQ(StringHandler.escQ((column.getName()))));
				}else{
					columnSql.append(StringHandler.doubleQ(StringHandler.escQ((column.getName()))));
				}
			}
		}

		sql.append("create table ").append(tableName).append(
				" as select ").append(columnSql).append(" from ").append(newTempTableName).append(" join ").append(resultTableName).append(" on ")
				.append(newTempTableName).append("."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(resultTableName).append("."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");
		itsLogger.debug(
				"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());

		StringBuilder predictionStringsb = new StringBuilder("(case when ");
		predictionStringsb.append(goodConfidence).append(" > 0.5 then ");
		appendValue(goodColumn, predictionStringsb);
		predictionStringsb.append(" else ");
		appendValue(badColumn, predictionStringsb);
		predictionStringsb.append(" end)");

		sql = new StringBuffer();
		sql.append("update ").append(tableName).append(" set ").append(badConfidence).append(" = 1 - ").append(goodConfidence).append(",")
		.append(predictedLabelName).append(" = ").append(predictionStringsb).append(" where ").append(goodConfidence).append(" is not null");
		itsLogger.debug(
				"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());
		
		sql = new StringBuffer();
		sql.append("drop table ").append(newTempTableName);
		itsLogger.debug(
				"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());
		
		sql = new StringBuffer();
		sql.append("drop table ").append(resultTableName);
		itsLogger.debug(
				"LogisticRegressionModelNetezza.performPredictionSqlAlias():sql=" + sql);
		st.execute(sql.toString());
		}catch(SQLException e){
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}
	protected StringBuilder getProbabilitySqlAlias(DataSet dataSet) {
		ISqlGeneratorMultiDB sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(dataSourceInfo.getDBType());

		HashMap<String, Double> betaMap = getBetaMap();
		ArrayList<String> coefficients = new ArrayList<String>();
		ArrayList<String> columns = new ArrayList<String>();
		int i = 0; 
		for(Column column : dataSet.getColumns())
		{
			 String columnName = StringHandler.doubleQ(column.getName());
			 if(column.isNumerical())
			 {
			 		if(betaMap.get(column.getName())==null)continue;
					double beta=betaMap.get(column.getName());
					coefficients.add("("+sqlGeneratorMultiDB.castToDouble(String.valueOf(beta))+")");
					columns.add(sqlGeneratorMultiDB.castToDouble(columnName));
					i++;
			 }else
			 {
				 List<String> mapList=column.getMapping().getValues();
	    		HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    		TransformMap_valueKey= getAllTransformMap_valueKey().get(column.getName());
	    		if(TransformMap_valueKey==null)continue;
	    		Iterator<String> mapList_i=mapList.iterator();
	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(betaMap.get(columnname)==null)continue;
 	 					double beta=betaMap.get(columnname);
 	 					value=StringHandler.escQ(value);
 						coefficients.add("("+sqlGeneratorMultiDB.castToDouble(String.valueOf(beta))+")");
 						columns.add(sqlGeneratorMultiDB.castToDouble(" (case"+" when "+columnName+"="+"'"+value+"' then 1.0 else 0.0 end)"));
 	 					i++;
 	 				}
			 }
		}
		
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(betaMap.get(key)!= null){
				coefficients.add("("+sqlGeneratorMultiDB.castToDouble(value)+")");
				columns.add(sqlGeneratorMultiDB.castToDouble(String.valueOf(betaMap.get(key))));
			}

		}
		int aliasCount = 0;
		StringBuilder subSql = new StringBuilder(sqlGeneratorMultiDB.castToDouble(String.valueOf(betaMap.get(interceptString))));
		boolean first = false;
		for (i = 0; i < coefficients.size(); i++){
			if((i+1) % AlpineDataAnalysisConfig.NZ_ALIAS_NUM == 0 || i == coefficients.size() - 1){
				if(first){
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients.get(i)+"*"+columns.get(i)).append(" e"+(aliasCount));
				aliasCount++;
				first = true;
			}else{
				if(first){
					first = false;
					subSql.append(",");
				}else{
					subSql.append("+");
				}
				subSql.append(coefficients.get(i)+"*"+columns.get(i));
			}
		}
		this.aliasCount = aliasCount;
		return subSql;
	}
}
	
