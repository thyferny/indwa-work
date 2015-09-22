/**
* ClassName AdaboostNZ.java
*
* Version information: 1.00
*
* Data: 26 Dec 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.db.trainer.adaboost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DataType;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 *
 */
public class AdaboostNZ extends AdaboostIMP{
    private static Logger itsLogger = Logger.getLogger(AdaboostNZ.class);

    String samplevalueTable =null;
	@Override
	public void adaboostTrainInit(String inputSchema, String tableName,
			long timeStamp, String dependentColumn, Statement st,
			Iterator<String> dependvalueIterator, DataSet dataSet)
			throws SQLException {

		this.dataSet = dataSet;
		pnewTable = "pnew" + timeStamp;
		deleteNullTable = "dn" + timeStamp;
		outputTable = "tp" + timeStamp;
		sampleTable = "s" + timeStamp;
		predictTable = "p" + timeStamp;
		randomTable = "r" + timeStamp;
		sumPeosoTable="sp"+ timeStamp;
		samplevalueTable="sv"+timeStamp;
		String infortable="dependinfo"+timeStamp;
//		StringBuffer inforArray = new StringBuffer();
		StringBuffer sql = new StringBuffer();
	
		String createSQL="create temp table "+StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(infortable)
		+ "	(  valueinfo varchar(128) )";
		itsLogger.debug("AdaboostNZ.adaboostTrainInit():sql=" +createSQL);
		 st.execute(createSQL);
//		inforArray.append("array[");
		while (dependvalueIterator.hasNext()) {
			
			String str = (String) dependvalueIterator.next();
			
			String insertSQL="insert into "+StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(infortable)
			+ " values ('" + str + "')";
			itsLogger.debug("AdaboostNZ.adaboostTrainInit():sql=" +insertSQL);
			 st.execute(insertSQL);
			
		}
			
		sql.append("select alpine_miner_adaboost_inittra('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(tableName));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("','");
		sql.append(StringHandler.doubleQ(dependentColumn));
		sql.append("','");
		sql.append(StringHandler.doubleQ(infortable));
		sql.append("')");
		itsLogger.debug("AdaboostNZ.adaboostTrainInit():sql="+sql.toString());

		st.executeQuery(sql.toString());
	}

	@Override
	public void adaboostTrainSampleOnce(String inputSchema, long timeStamp,
			Statement st, String pnewTable) throws SQLException {

		StringBuffer sql = new StringBuffer();
		sql.append("select alpine_miner_adaboost_sample('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(pnewTable));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("',");
		sql.append(AlpineMinerConfig.ADABOOST_SAMPLE_NUMBER);
		sql.append(")");
		itsLogger.debug("AdaboostNZ.adaboostTrainSampleOnce():sql="+sql.toString());
		st.executeQuery(sql.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#
	 * adaboostChangePeoso(java.lang.String, java.lang.String, long,
	 * java.lang.String, java.lang.String, java.sql.Statement,
	 * java.sql.ResultSet, java.util.Iterator)
	 */
	@Override
	public double adaboostChangePeoso(String inputSchema, String tableName,
			long timeStamp, String dependentColumn,
			String dependentColumnReplaceQ, Statement st, ResultSet rs,
			Iterator<String> sampleDvalueIterator) throws AnalysisException  {
		 
		try {
			String dropIfExists=" select droptable_if_exists('"+samplevalueTable+"')";
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql=" +dropIfExists);
			st.execute(dropIfExists);
		String createSQL="create   table "+StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(samplevalueTable)
		+ "	(  samplecolumn varchar(128) )";//NZDatasource
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql=" +createSQL);
		st.execute(createSQL);
		while (sampleDvalueIterator.hasNext()) {
			String str = (String) sampleDvalueIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			String insertSQL="insert into "+StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(samplevalueTable)
			+ " values ('" + str + "')";
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql=" +insertSQL);
			 st.execute(insertSQL);
			
		}
		
		double result=0;
		String tempTable = "T"+System.currentTimeMillis();
		StringBuffer sql = new StringBuffer();
		sql.append("create table ").append(StringHandler.doubleQ(inputSchema)+"."+tempTable).append(" as select *, null::integer \"notsame\" from ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(outputTable));
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
		st.execute(sql.toString());
		sql.setLength(0);
		sql.append("drop table ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(outputTable));
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
		st.execute(sql.toString());
		sql.setLength(0);
		sql.append("alter table ").append(StringHandler.doubleQ(inputSchema)+"."+tempTable).append(" rename to ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(outputTable));
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
		st.execute(sql.toString());

		sql.setLength(0);
		sql.append("create table ").append(StringHandler.doubleQ(inputSchema)+"."+tempTable).append(" as select *, null::integer \"notsame\" from ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable));
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
		st.execute(sql.toString());
		sql.setLength(0);
		sql.append("drop table ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable));
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
		st.execute(sql.toString());
		sql.setLength(0);
		sql.append("alter table ").append(StringHandler.doubleQ(inputSchema)+"."+tempTable).append(" rename to ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable));
		itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
		st.execute(sql.toString());

			sql.setLength(0);
			sql.append("select alpine_miner_adaboost_changep('");
			sql.append(StringHandler.doubleQ(inputSchema));
			sql.append("','");
			sql.append(StringHandler.doubleQ(tableName));
			sql.append("','");
			sql.append(timeStamp);
			sql.append("','");
			sql.append(StringHandler.doubleQ(dependentColumn));
			sql.append("','");
			sql.append(dependentColumnReplaceQ);
			sql.append("','");
			sql.append(getDependentColumnStringGPPG(dataSet.getColumns()
					.getLabel()));
			sql.append("','");
			sql.append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(samplevalueTable));
			sql.append("')");
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql.toString());

			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				result=rs.getDouble(1);
			}
			sql.setLength(0);
			sql.append("create table ").append(StringHandler.doubleQ(inputSchema)+"."+tempTable).append(" as select ");
			Iterator<Column> columnIterator = dataSet.getColumns().allColumns();
			int count = 0;
			while(columnIterator.hasNext()){
				Column column = columnIterator.next();
				sql.append(StringHandler.doubleQ(column.getName()));
				sql.append(",");
				count++;
			}
			sql.append("alpine_adaboost_id, ").append("alpine_adaboost_peoso, ").append("alpine_adaboost_totalpeoso ").append(" from ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable));
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
			st.execute(sql.toString());
			sql.setLength(0);
			sql.append("drop table ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable));
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
			st.execute(sql.toString());
			sql.setLength(0);
			sql.append("alter table ").append(StringHandler.doubleQ(inputSchema)+"."+tempTable).append(" rename to ").append(StringHandler.doubleQ(inputSchema)+"."+StringHandler.doubleQ(pnewTable));
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql="+sql);
			st.execute(sql.toString());

			return result;
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}

		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#
	 * clearTrainResult(java.lang.String, long, java.sql.Statement,
	 * java.lang.String)
	 */
	@Override
	public void clearTrainResult(String inputSchema, long timeStamp,
			Statement st, String pnewTable) throws AnalysisException {
		try {
			String sql = new String();
			sql=" select droptable_if_exists('"+pnewTable+"')";
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);
			sql=" select droptable_if_exists('"+sumPeosoTable+"')";				
		
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);

			sql=" select droptable_if_exists('"+deleteNullTable+"')";				
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);
			sql=" select droptable_if_exists('"+outputTable+"')";				
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);
			sql=" select droptable_if_exists('"+predictTable+"')";				
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);
			
			
			sql=" select droptable_if_exists('"+samplevalueTable+"')";				
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);
			
			sql=" select droptable_if_exists('"+randomTable+"')";				
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);

			
			sql=" select droptable_if_exists('"+sampleTable+"')";				
			
			itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+sql);
			st.execute(sql);

		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#setOutputTable
	 * (com.alpine.datamining.api.impl.algoconf.PredictorConfig)
	 */
	@Override
	public void setOutputTable(PredictorConfig tempconfig) {
		tempconfig.setOutputTable(outputTable);

	}

	protected StringBuffer getDependentColumnStringGPPG(Column column) {
		StringBuffer sb = new StringBuffer();
		if (column.getValueType() == DataType.BOOLEAN) {
			sb.append(" (case when ").append(
					StringHandler.doubleQ(column.getName())).append(
					" is true then ''t'' else ''f'' end)");
		} else if (column.getValueType() == DataType.DATE
				|| column.getValueType() == DataType.DATE_TIME
				|| column.getValueType() == DataType.TIME) {
			sb.append(StringHandler.doubleQ(column.getName())).append(
					"::varchar(128)");
		} else {
			sb.append(StringHandler.doubleQ(column.getName())).append(
					"::varchar(128)");
		}
		return sb;
	}


}
