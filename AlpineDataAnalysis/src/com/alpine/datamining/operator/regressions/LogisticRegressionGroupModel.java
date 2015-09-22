/**
 * 

* ClassName test.java
*
* Version information: 1.00
*
* Data: May 31, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.operator.regressions;

/**
 * @author Shawn
 *
 *  
 */
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.resources.AlpineThreadLocal;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

public class LogisticRegressionGroupModel extends LogisticRegressionModelDB {
    private static final Logger itsLogger = Logger.getLogger(LogisticRegressionGroupModel.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = 5509473799240707998L;

	private String groupByColumn;
	
	
	
	private Map<String,LogisticRegressionModelDB> modelList = new LinkedHashMap<String,LogisticRegressionModelDB>();
	/**
	 * 
	 */
	 
	public LogisticRegressionGroupModel(DataSet dataSet, DataSet oldDataSet,
			double[] beta, double[] variance, boolean interceptAdded,
			String goodValue) {
		super(dataSet, oldDataSet, beta, variance, interceptAdded, goodValue);
	}
	public Map<String,LogisticRegressionModelDB> getModelList() {
		return modelList;
	}
	public void setModelList(Map<String,LogisticRegressionModelDB> modelList) {
		this.modelList = modelList;
	}

	public  LogisticRegressionModelDB getOneModel(String groupString) {
		return modelList.get(groupString);
	}
	public void addOneModel( LogisticRegressionModelDB model,String groupString) {
		modelList.put(groupString, model);
	}
	
	
	
	
	
	public String getGroupByColumn() {
		return groupByColumn;
	}
	public void setGroupByColumn(String groupByColumn) {
		this.groupByColumn = groupByColumn;
	}
	
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
	throws OperatorException {	

		dataSet.getColumns().setLabel(getLabel());
		
    	Statement st = null;
    	DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
    	
    	String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}
			
		
		
		StringBuilder probability = getProbability();

		StringBuilder sql =new StringBuilder("");
		sql.setLength(0);
		StringBuilder functionValuesb=new StringBuilder("(");
		functionValuesb.append(probability).append(")");
		
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
		predictionStringsb.append(functionValuesb).append(" > 0.5 then ");
		appendValue(goodColumn, predictionStringsb);
		predictionStringsb.append(" else ");
		appendValue(badColumn, predictionStringsb);
		predictionStringsb.append(" end)");
		String predictedLabelName=StringHandler.doubleQ(predictedLabel.getName());
		sql.append("update ").append(tableName);
		appendUpdateSet(dataSet, sql, functionValuesb, goodColumn, badColumn,
				predictionStringsb, predictedLabelName);
		sql.append(getWhere(oldDataSet));
		try {
			itsLogger.debug("LogisticRegressionModelDB.performPrediction():sql="+sql);
			st.executeUpdate(sql.toString());		
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error("Sql Error!");
			throw new OperatorException(e.getLocalizedMessage());
		}

		return dataSet;
	}
	
	
	
	protected StringBuilder getProbabilityFunction(DataSet dataSet) {
		StringBuilder probability = new StringBuilder("alpine_miner_lr_ca_pi( case ");
		StringBuffer columnNamesArray = new StringBuffer(multiDBUtility.floatArrayHead());
		Iterator<Entry<String, LogisticRegressionModelDB>> modelIterator=modelList.entrySet().iterator();
		while(modelIterator.hasNext())
		{
			Entry<String, LogisticRegressionModelDB> tempEntry=modelIterator.next();
			String tempGroupValue=tempEntry.getKey();
//			Column[] tableColumns=((DBTable) dataSet.getDBTable()).getColumns();
//	    	for(Column tempColumn : tableColumns)
//	    	{
//	    		if(tempColumn.getName().equals(groupByColumn))
//	    		{
//	    			if(tempColumn.isNominal())
//	    			{
//						tempGroupValue=StringHandler.singleQ(tempGroupValue);
//					};
//	    		}
//	    	}
			
			LogisticRegressionModelDB tempModel=tempEntry.getValue();
			probability.append(" when ").append(StringHandler.doubleQ(groupByColumn)).append("=").append(StringHandler.singleQ(tempGroupValue)).append(" then ");
	
		StringBuffer betaArray = new StringBuffer(multiDBUtility.floatArrayHead());
		HashMap<String, Double> betaMap = tempModel.getBetaMap();
		boolean first = true;
		for (Column column : dataSet.getColumns()) {
		 
			if (column.isNumerical()) {
				if(betaMap.get(column.getName())==null)continue;
				double beta = betaMap.get(column.getName());
				if (!first) {
				 
					betaArray.append(",");
				}
				else
				{
					first = false;
				}
			 
				betaArray.append(beta);
			} else {
				HashMap<String, String> TransformMap_valueKey = new HashMap<String, String>();
				TransformMap_valueKey = getAllTransformMap_valueKey().get(
						column.getName());
				if(TransformMap_valueKey==null)continue;
				Iterator<String> valueIterator = TransformMap_valueKey.keySet().iterator();
				String value = null;
				while (valueIterator.hasNext())
				{
					value = valueIterator.next();
					String columnname = TransformMap_valueKey.get(value);
					if (betaMap.get(columnname) == null)
						continue;
					double beta = betaMap.get(columnname);
					value=StringHandler.escQ(value);
					if(!first)
					{
					 
						betaArray.append(",");
					}
					else
					{
						first = false;
					}
					 
					betaArray.append(beta);
				}
			}
		}
		
		Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
		 
			if(betaMap.get(key)!= null){
				if(!first)
				{
					 
					betaArray.append(",");
				}
				else
				{
					first = false;
				}
				betaArray.append(betaMap.get(key));
			 
			}
		}

		if (interceptAdded) {
			if(first)
			{
				betaArray.append(betaMap.get(interceptString));
			}else
			{
				first = false;
				betaArray.append(",").append(betaMap.get(interceptString));
			}
			
		}

	 
		betaArray.append(multiDBUtility.floatArrayTail());
		
		probability.append(betaArray);
		}
		
		 
	 	HashMap<String, Double> betaMap = getBetaMap();
	boolean first = true;
	for (Column column : dataSet.getColumns()) {
		String ColumnName = StringHandler.doubleQ(column.getName());
		if (column.isNumerical()) {
			if(betaMap.get(column.getName())==null)continue;
		 
			if (!first) {
				columnNamesArray.append(",");
				 
			}
			else
			{
				first = false;
			}
			columnNamesArray.append(ColumnName);
			 
		} else {
			HashMap<String, String> TransformMap_valueKey = new HashMap<String, String>();
			TransformMap_valueKey = getAllTransformMap_valueKey().get(
					column.getName());
			if(TransformMap_valueKey==null)continue;
			Iterator<String> valueIterator = TransformMap_valueKey.keySet().iterator();
			String value = null;
			while (valueIterator.hasNext())
			{
				value = valueIterator.next();
				String columnname = TransformMap_valueKey.get(value);
				if (betaMap.get(columnname) == null)
					continue;
			 
				value=StringHandler.escQ(value);
				if(!first)
				{
					columnNamesArray.append(",");
				 
				}
				else
				{
					first = false;
				}
				columnNamesArray.append("(case").append(
				" when ").append(ColumnName)
				.append("=")
				.append(CommonUtility.quoteValue(dataSourceInfo.getDBType(),column, value))
				.append(" then 1 else 0 end)");
		 
			}
		}
	}
	
	Iterator<Entry<String, String>>  iter = interactionColumnExpMap.entrySet().iterator();
	while (iter.hasNext()) {
		Entry<String, String> entry = iter.next();
		String key = entry.getKey();
		String value = entry.getValue();
		if(betaMap.get(key)!= null){
			if(!first)
			{
				columnNamesArray.append(",");
			 
			}
			else
			{
				first = false;
			}
			 
			columnNamesArray.append(value);
		}
	}

 

		columnNamesArray.append(multiDBUtility.floatArrayTail());
  		probability.append(" end ");
		probability.append(",").append(columnNamesArray).append(",");
		addIntercept(probability);
		probability.append(")");
		return probability;
	}
}
