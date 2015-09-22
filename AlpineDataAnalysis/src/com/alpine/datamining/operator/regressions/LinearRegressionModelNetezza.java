/**
 * ClassName LinearRegressionModelDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


/**
 * The model netezza for linear regression.
 *  @author Eason
 */
public class LinearRegressionModelNetezza extends LinearRegressionModelDB {

    private static final Logger itsLogger = Logger.getLogger(LinearRegressionModelNetezza.class);

    private static final long serialVersionUID = 3560702652340212186L;
	private Double[] betas;
	private String[] columns;
	private int aliasCount;
	public LinearRegressionModelNetezza(DataSet dataSet, String [] columnNames,String specifyColumn,Double[] coefficients, HashMap<String,Double> coefficientmap) {
		super(dataSet, columnNames,specifyColumn, coefficients, coefficientmap);
	}
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException{
		if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1 || (AlpineDataAnalysisConfig.NZ_PROCEDURE_SWITCH == 0 && dataSet.getColumns().size() < AlpineDataAnalysisConfig.NZ_PROCEDURE_COLUMN_LIMIT)){
			if(AlpineDataAnalysisConfig.NZ_ALIAS_SWITCH == 1){
				return performPredictionSqlAlias(dataSet,predictedLabel);
			}
				
			return performPredictionSql(dataSet,predictedLabel);
		}else{
			return performPredictionProcedure(dataSet,predictedLabel);
		}
	}

	public DataSet performPredictionProcedure(DataSet dataSet, Column predictedLabel) throws OperatorException
	{
		this.predictedLabelName = predictedLabel.getName();
		String newTableName = ((DBTable) dataSet.getDBTable()).getTableName();
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		long currentTime = System.currentTimeMillis();
		String newTempTableName = "NT" + currentTime;
		String resultTableName = "R" + currentTime;
		String betaTableName = "B"+currentTime;
		String columnTableName = "C"+currentTime;

		Statement st = null;
		StringBuffer sql = new StringBuffer();

		try {
			st = databaseConnection.createStatement(false);
			TableTransferParameter.createDoubleTable(betaTableName, st);
			TableTransferParameter.createStringTable(columnTableName, st);
			generateBetaAndColumns();
			TableTransferParameter.insertTable(betaTableName, st, betas);
			TableTransferParameter.insertTable(columnTableName, st, columns);
			sql.append("create table ").append(newTempTableName).append(
					" as select *, row_number() over(order by 1) as  "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" from ").append(newTableName);
			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			sql.append("create  table ").append(resultTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint, prediction double)");
			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer where = getWherePredict();

		sql = new StringBuffer();

		sql.append("call  alpine_miner_lir_ca_predict_proc('");
		sql.append(newTempTableName).append("','") 
		.append(where).append("','") 
		.append(betaTableName).append("','") 
		.append(columnTableName).append("','")
		.append(resultTableName).append("')");

			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer sqltemp = new StringBuffer();
			sqltemp.append("update ").append(newTempTableName).append(" set  ");

			sqltemp.append(StringHandler.doubleQ(predictedLabel.getName())).append(" = ");
			sqltemp.append(resultTableName).append(".prediction");
			sqltemp.append(
					" from ").append(resultTableName).append(
					" where  ").append(resultTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTempTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");

            itsLogger.debug(
							"LinearRegressionModelNetezza.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTableName);
			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
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
					columnSql.append(StringHandler.doubleQ(column.getName()));
				}
			}
			sql.append("create table ").append(newTableName).append(
					" as select ").append(columnSql).append(" from ").append(newTempTableName);
			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTempTableName);
			itsLogger.debug(
					"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
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
	private StringBuffer getWherePredict(){
		StringBuffer where = new StringBuffer(" ");
		Columns atts=getTrainingHeader().getColumns();
		Iterator<Column> attsIterator=atts.iterator();
		int i = 0;
		while(attsIterator.hasNext())
		{
			Column att=attsIterator.next();
			String columnName=StringHandler.escQ(StringHandler.doubleQ(att.getName()));
			if(att.isNumerical())
			{
				if(i != 0){
					where.append(" and ");
				}else{
					where.append(" where ");
				}
				where.append(columnName).append(" is not null ");
				i++;
			}
		}
		return where;
	}
	private void generateBetaAndColumns() {
		betas = new Double[coefficients.length];
		columns = new String[coefficients.length - 1];
		betas[coefficients.length - 1] = coefficients[coefficients.length - 1];
		Columns atts=getTrainingHeader().getColumns();
		Iterator<Column> attsIterator=atts.iterator();
		int i = 0;
		while(attsIterator.hasNext())
		{
			Column att=attsIterator.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{			
				if(coefficientsMap.get(att.getName())==null)continue;
				double coefficient=coefficientsMap.get(att.getName());
				betas[i] = coefficient;
				columns[i] = columnName;
				i++;
			}else
			{
				
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(coefficientsMap.get(columnname)==null)continue;
 	 					double coefficient=coefficientsMap.get(columnname);
 	 					String caseString = "(case when "+columnName+"='"+StringHandler.escQ(value)+"' then 1  else 0 end)";
 	 					betas[i] = coefficient;
 	 					columns[i] = StringHandler.escQ(caseString);
 	 					i++;
 	 				}
			}
		}
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = StringHandler.escQ(entry.getValue());
			if(coefficientsMap.get(key)!= null){
					betas[i] = coefficientsMap.get(key);
	 				columns[i] = value;
	 				i++;
			}
		} 
	}
	public DataSet performPredictionSql(DataSet dataSet, Column predictedLabel) throws OperatorException
	{
		this.predictedLabelName = predictedLabel.getName();
		
		dataSet.getColumns().setLabel(getLabel());

	
		String tableName=((DBTable) dataSet.getDBTable())
		.getTableName();
		String predictedLabelName = StringHandler.doubleQ(predictedLabel.getName());
		StringBuilder sb_update=new StringBuilder("update ");
		sb_update.append(tableName).append(" set ").append(predictedLabelName).append(" =(");
		StringBuffer predictedString = generatePredictedString(dataSet);
		sb_update.append(predictedString).append(")");
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("LinearRegressionModelNetezza.performPrediction():sql="+sb_update);
			st.execute(sb_update.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		
		
		return dataSet;
	}

	public StringBuffer generatePredictedString(DataSet dataSet) {
		String dbType = ((DBTable) dataSet.getDBTable())
		.getDatabaseConnection().getProperties().getName();
		StringBuffer predictedString = new StringBuffer();
		predictedString.append(coefficients[coefficients.length - 1]).append("::double");
		Columns atts=getTrainingHeader().getColumns();
		Iterator<Column> atts_i=atts.iterator();
		while(atts_i.hasNext())
		{
			Column att=atts_i.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{			
				if(coefficientsMap.get(att.getName())==null)continue;
				double coefficient=coefficientsMap.get(att.getName());
				predictedString.append("+(").append(coefficient).append(")::double*").append(columnName);
			}else
			{
				
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(coefficientsMap.get(columnname)==null)continue;
 	 					double coefficient=coefficientsMap.get(columnname);

 	 					predictedString.append("+(").append(coefficient).append(")::double*").append("(case ");
 	 					predictedString.append(" when ").append(columnName).append("=");
 	 					value=StringHandler.escQ(value);
						value = CommonUtility.quoteValue(
								dbType, att, value);
 	 					predictedString.append(value).append(" then 1  else 0 end)");

 	 				}
			}
		}
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(coefficientsMap.get(key)!= null){
				predictedString.append("+"+value +"*(" + coefficientsMap.get(key)+")");
			}
		} 
		return predictedString;
	}
	public DataSet performPredictionSqlAlias(DataSet dataSet, Column predictedLabel) throws OperatorException
	{
		this.predictedLabelName = predictedLabel.getName();
		
//		dataSet.getColumns().setLabel(getLabel());
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

		StringBuffer predict = new StringBuffer(" select "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+", ");
		predict.append(generatePredictedStringAlias(getTrainingHeader(), databaseConnection.getProperties().getName()));
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
		"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
		st.execute(sql.toString());


		sql = new StringBuffer();
		sql.append("drop table ").append(tableName);
		itsLogger.debug(
				"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
		st.execute(sql.toString());


		sql = new StringBuffer();
		sql.append("create table ").append(resultTableName).append(" as select "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+", ").append(predictedY).append(" as ").append(predictedLabelName).append(" from (").append(predict).append("  limit all )foo");
		itsLogger.debug(
				"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
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
				if(column.getName().equals(predictedLabel.getName())){
					columnSql.append(resultTableName).append(".");
				}
				columnSql.append(StringHandler.doubleQ(column.getName()));
			}
		}

		sql.append("create table ").append(tableName).append(
				" as select ").append(columnSql).append(" from ").append(newTempTableName).append(" join ").append(resultTableName).append(" on ")
				.append(newTempTableName).append("."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(resultTableName).append("."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");
		itsLogger.debug(
				"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
		st.execute(sql.toString());

		sql = new StringBuffer();
		sql.append("drop table ").append(newTempTableName);
		itsLogger.debug(
				"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
		st.execute(sql.toString());
		
		sql = new StringBuffer();
		sql.append("drop table ").append(resultTableName);
		itsLogger.debug(
				"LinearRegressionModelNetezza.performPrediction():sql=" + sql);
		st.execute(sql.toString());
		}catch(SQLException e){
			e.printStackTrace();
			itsLogger.warn(e.getLocalizedMessage());
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}

	public StringBuffer generatePredictedStringAlias(DataSet dataSet, String dbType) {
		StringBuffer predictedString = new StringBuffer();
		predictedString.append("(");
		
		predictedString.append(coefficients[coefficients.length - 1]).append("::double");
		Columns atts=getTrainingHeader().getColumns();
		Iterator<Column> attsIterator=atts.iterator();
		int i = 0;
		ArrayList<String> coefficients = new ArrayList<String>();
		ArrayList<String> columns = new ArrayList<String>();
		
		while(attsIterator.hasNext())
		{
			Column att=attsIterator.next();
			String columnName=StringHandler.doubleQ(att.getName());
			if(att.isNumerical())
			{			
				if(getCoefficientsMap().get(att.getName())==null)continue;
				double coefficient=getCoefficientsMap().get(att.getName());
				predictedString.append("+");
				predictedString.append("(");
				predictedString.append("((").append(coefficient).append(")::double*").append(columnName).append(")");
				coefficients.add("("+coefficient+")::double");
				columns.add(columnName);
				i++;
			}else
			{
				
	 				List<String> mapList=att.getMapping().getValues();
	    			HashMap<String,String> TransformMap_valueKey=new HashMap<String,String>();
	    			TransformMap_valueKey=getAllTransformMap_valueKey().get(att.getName());
	    			if(TransformMap_valueKey==null)continue;
 	 				Iterator<String> mapList_i=mapList.iterator();
 	 				while(mapList_i.hasNext())
 	 				{
 	 					String value=mapList_i.next();
 	 					String columnname=TransformMap_valueKey.get(value);
 	 					if(getCoefficientsMap().get(columnname)==null)continue;
 	 					double coefficient=getCoefficientsMap().get(columnname);
 	 					predictedString.append("+");
 	 					predictedString.append("(");
 	 					predictedString.append("((").append(coefficient).append(")::double*").append("(case ");
 	 					predictedString.append(" when ").append(columnName).append("=");
 	 					value=StringHandler.escQ(value);
						value = CommonUtility.quoteValue(
								dbType, att, value);
 	 					predictedString.append(value).append(" then 1  else 0 end))");
 	 					coefficients.add("("+coefficient+")::double");
 	 					columns.add("(case  when "+columnName+"="+value+" then 1 else 0 end)");
 	 					i++;

 	 				}
			}
		}
		Iterator<Entry<String, String>>  iter = getInteractionColumnExpMap().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(getCoefficientsMap().get(key)!= null){
				predictedString.append("+");
				predictedString.append("(");
				predictedString.append("("+value +"*(" + getCoefficientsMap().get(key)+"))");
				coefficients.add("("+getCoefficientsMap().get(key)+")::double");
				columns.add(value);
				i++;
			}
		}
		int aliasCount = 0;
		StringBuffer subSql = new StringBuffer(String.valueOf(this.coefficients[this.coefficients.length - 1])+"::double");
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
