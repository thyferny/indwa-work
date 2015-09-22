/**
* ClassName AdaboostModelNZ.java
*
* Version information: 1.00
*
* Data: 26 Dec 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.operator.adboost;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.db.DataSourceInfoNZ;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 */
public class AdaboostModelNZ extends AdaboostModel{
	/**
	 * 
	 */
	private static final Logger itsLogger = Logger.getLogger(AdaboostModelNZ.class);
	private static final long serialVersionUID = 8307317892655383410L;
	private String infoTable=new String();
	private String idTable=new String();
	/**
	 * @param paramDataSet
	 */
	public AdaboostModelNZ(DataSet paramDataSet) {
		super(paramDataSet);	
		}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.operator.adboost.AdaboostModel#adaboostPredictResult(java.lang.String, long, java.lang.String, java.lang.String, java.sql.Statement, java.lang.StringBuffer, double)
	 */
	@Override
	protected void adaboostPredictResult(String outTable, long timeStamp,
			String tempOutTable, String dependentColumn, Statement st,
			StringBuffer inforArray, double sumc,DataSet dataSet) throws OperatorException {
		try {
			String[] infors=inforArray.toString().split(",");
			String deleteSQL="delete from   "+StringHandler.doubleQ(infoTable);
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictResult():sql=" +deleteSQL);
			}
			st.execute(deleteSQL);
			for (int i=0;i<infors.length;i++)
			{
				String columnValue = infors[i];
				columnValue = StringHandler.doubleQ(columnValue);
				columnValue = columnValue.substring(1, columnValue.length() - 1);
				String insertSQL="insert into "+StringHandler.doubleQ(infoTable)+ " values ('" + columnValue + "')";
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AdaboostModelNZ.adaboostPredictResult():sql=" +insertSQL);
				}
				 st.execute(insertSQL);
			}
			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_prere('");
			sql.append(outTable);
			sql.append("','");
			sql.append(dependentColumn.replace("\"", "\"\""));
			sql.append("','");
			sql.append(StringHandler.doubleQ(infoTable));
			sql.append("',");
			sql.append((getLabel().isNumerical() ? 1 : 0));
			sql.append(") ");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionResult():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());
			String sql1 = "drop table " + tempOutTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionResult():sql="+sql1);
			}
			st.execute(sql1);


			StringBuffer normaString = new StringBuffer();
			normaString.append("update " + outTable + " set ");
			Iterator<String> localIterator = getTrainingHeader().getColumns()
					.getLabel().getMapping().getValues().iterator();
			while (localIterator.hasNext()) {
				String str = (String) localIterator.next();
				str = StringHandler.doubleQ(str);
				str = str.substring(1, str.length() - 1);
				normaString.append("\"").append(Column.CONFIDENCE_NAME).append(
						"(").append(str).append(")\"=").append("\"").append(
						Column.CONFIDENCE_NAME).append("(").append(str).append(
						")\"/").append(sumc).append(" ,");
			}
			normaString.deleteCharAt(normaString.length() - 1);
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionResult():sql="+normaString.toString());
			}
			st.execute(normaString.toString());
			
			StringBuffer columnSql = new StringBuffer();
			Iterator<Column> allColumns = dataSet.getColumns().allColumns();
			boolean first = true;
			while(allColumns.hasNext()){
				Column column = allColumns.next();
				if(!column.getName().equalsIgnoreCase("alpine_adaboost_id")){
					if(first){
						first = false;
					}else{
						columnSql.append(",");
					}
					columnSql.append(StringHandler.doubleQ(column.getName()));
				}
			}
			String newTableName="nt"+timeStamp;
			StringBuffer CreateTableSQL=new StringBuffer();
			CreateTableSQL.append("create table ").append(newTableName).append(
					" as select ").append(columnSql).append(" from ").append(outTable);
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.performPrediction():sql=" + CreateTableSQL);
			}
			st.execute(CreateTableSQL.toString());
			String dropSQL="drop table "+outTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.performPrediction():sql=" + dropSQL);
			}
			st.execute(dropSQL);
			String createSQL="create table "+outTable+
			" as select *  from "+newTableName;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.performPrediction():sql=" + createSQL);
			}
			st.execute(createSQL);
			dropSQL="drop table "+newTableName;
			st.execute(dropSQL);
			
			
			
			String tempSql=new String();
			tempSql=" select droptable_if_exists('"+idTable+"')";
			
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+tempSql);
			}
			st.executeQuery(tempSql);
			tempSql=" select droptable_if_exists('"+infoTable+"')";				
		
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostNZ.clearTrainResult():sql="+tempSql);
			}
			st.executeQuery(tempSql);
			
			
			
			
			
		} catch (SQLException e) {
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.operator.adboost.AdaboostModel#adaboostPredictStep(java.lang.String, java.lang.String, java.lang.String, java.sql.Statement, double, java.util.Iterator, java.lang.StringBuffer)
	 */
	@Override
	protected void adaboostPredictStep(String outTable, String tempOutTable,
			String dependentColumn, Statement st, double algWeight,
			Iterator<String> sampleDvalueIterator, StringBuffer sampleArray)
			throws SQLException {	

		String deleteSQL="delete from   "+StringHandler.doubleQ(infoTable);
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql=" +deleteSQL);
		}
		st.execute(deleteSQL);
		while (sampleDvalueIterator.hasNext()) {
			String str = (String) sampleDvalueIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			String insertSQL="insert into "+StringHandler.doubleQ(infoTable)+ " values ('" + str + "')";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionInits():sql=" +insertSQL);
			}
			 st.execute(insertSQL);
		}
			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_prestep('");
			sql.append(outTable);
			sql.append("','");
			sql.append(StringHandler.doubleQ(tempOutTable));
			sql.append("','");
			sql.append(dependentColumn);
			sql.append("',");
			sql.append(algWeight);
			sql.append(",'");
			sql.append(infoTable);
			sql.append("') ");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionPredictStep():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.operator.adboost.AdaboostModel#adaboostPredictionInit(java.lang.String, java.lang.String, long, java.lang.String, java.lang.String, java.sql.Statement, java.lang.StringBuffer)
	 */
	@Override
	protected String adaboostPredictionInit(String outTable,
			String tempOutTable, long timeStamp, String schemaName,
			String dependentColumn, Statement st, StringBuffer inforArray)
			throws OperatorException {		
		try {
			String[] infors=inforArray.toString().split(",");
			infoTable="info"+timeStamp;
			idTable="id"+timeStamp;
			String createSQL="create    table  "+StringHandler.doubleQ(infoTable)
			+ "	(  info varchar("+DataSourceInfoNZ.maxColumnLength+") )";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostNZ.adaboostChangePeoso():sql=" +createSQL);
			}
			st.execute(createSQL);
			for (int i=0;i<infors.length;i++)
			{
				String columnValues = infors[i];
				columnValues = StringHandler.doubleQ(columnValues);
				columnValues = columnValues.substring(1, columnValues.length() - 1);
				String insertSQL="insert into "+StringHandler.doubleQ(infoTable)+ " values ('" + columnValues + "')";
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AdaboostModelNZ.adaboostPredictionInits():sql=" +insertSQL);
				}
				 st.execute(insertSQL);
			}
				boolean isTemp=false;
			if(!outTable.contains("."))
			{
				isTemp=true;
			};
			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_initpre('");
			sql.append(outTable);
			sql.append("','");
			sql.append(timeStamp);
			sql.append("','");
			sql.append(StringHandler.doubleQ(dependentColumn));
			sql.append("','");
			sql.append(infoTable);
			sql.append("',");
			sql.append(isTemp);
			sql.append(")");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionInit():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());

			String sql1 = "CREATE   TABLE " + StringHandler.doubleQ(tempOutTable)
					+ " as select * from  " + outTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelNZ.adaboostPredictionInit():sql="+sql1);
			}
			st.execute(sql1);
			tempOutTable=StringHandler.doubleQ(tempOutTable);
			} catch (SQLException e1) {
				throw new OperatorException(e1.getLocalizedMessage());
			}
			return tempOutTable;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.operator.adboost.AdaboostModel#spellArray(java.lang.StringBuffer, java.util.Iterator)
	 */
	@Override
	protected StringBuffer spellArray(StringBuffer inforArray,
			Iterator<String> localIterator) {
		while (localIterator.hasNext()) {
			String columnValues = (String) localIterator.next();
			columnValues = StringHandler.doubleQ(columnValues);
			columnValues = columnValues.substring(1, columnValues.length() - 1);
			inforArray.append(columnValues + ",");
		}
		inforArray=inforArray.deleteCharAt(inforArray.length()-1);
		return inforArray;		 
	}

}
