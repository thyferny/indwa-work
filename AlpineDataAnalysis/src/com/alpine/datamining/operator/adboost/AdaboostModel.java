/**
 * ClassName AdaboostModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-20
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.adboost;

/**
 * @author Shawn
 *
 */
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.AbstractModel;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;

public abstract class AdaboostModel extends Prediction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6608309148147402941L;
	
	public AdaboostModel(DataSet paramDataSet) {
		super(paramDataSet);
	}

	private List<AdaboostSingleModel> modelList = new LinkedList<AdaboostSingleModel>();
	private String dependColumn = null;
	private String columnNames = null;
	private String tableName = null;
	

	public void addModel(AdaboostSingleModel model) {
		modelList.add(model);
	}

	public boolean IsListEmpty() {
		return modelList.isEmpty();
	}

	public void setDependentColumn(String dependColumn) {
		this.dependColumn = dependColumn;
	}

	public String getDependentColumn() {
		return dependColumn;
	}

	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

	public String getColumnNames() {
		return columnNames;
	}

	public void setTableNames(String tableName) {
		this.tableName = tableName;
	}

	public String getTableNames() {
		return tableName;
	}

	public int getModelNum() {
		return this.modelList.size();
	}

	public AdaboostSingleModel getModel(int i) {
		return this.modelList.get(i);
	}

	@Override
	public DataSet performPrediction(DataSet pDataSet, Column column)
			throws OperatorException {

		DBSource commonDataSource = null;
		DatabaseConnection conncetion = ((DBTable) pDataSet.getDBTable())
				.getDatabaseConnection();
		if( this instanceof AdaboostModelDB2)
		{
			((AdaboostModelDB2)this).DB2Connection=conncetion.getConnection();
		}
		
		
		try {
			commonDataSource = OperatorUtil.createOperator(DBSource.class);
		} catch (OperatorException e1) {
			throw new OperatorException(e1.getLocalizedMessage());
		}
		String outTable = ((DBTable) pDataSet.getDBTable()).getTableName();
		long timeStamp = System.currentTimeMillis();
		String tempOutTable = "to" + timeStamp;
		String schemaName = "";

		if (outTable.contains(".")){
			String[] schemaTable = outTable.split("\\.",2);
			schemaName = schemaTable[0];
		}
		String dependentColumn = getLabel().getName();
		Statement st = null;
		try {
			st = conncetion.createStatement(false);
		
		StringBuffer inforArray = new StringBuffer();

		Iterator<String> localIterator = getTrainingHeader().getColumns()
				.getLabel().getMapping().getValues().iterator();

		inforArray=spellArray(inforArray, localIterator);

		tempOutTable = adaboostPredictionInit(outTable, tempOutTable,
				timeStamp, schemaName, dependentColumn, st, inforArray);
		
		DatabaseSourceParameter databaseSourceParameter = new DatabaseSourceParameter();

		databaseSourceParameter.setWorkOnDatabase(true);

		databaseSourceParameter.setDatabaseSystem(((DBTable) pDataSet
				.getDBTable()).getDatabaseConnection().getProperties()
				.getName());

		databaseSourceParameter.setTableName(tempOutTable);

		databaseSourceParameter.setUrl(((DBTable) pDataSet.getDBTable())
				.getUrl());
		databaseSourceParameter
				.setUsername(((DBTable) pDataSet.getDBTable()).getUserName());
		databaseSourceParameter
				.setPassword(((DBTable) pDataSet.getDBTable()).getPassword());
		commonDataSource.setParameter(databaseSourceParameter);
		DataSet dataSet = null;
		dataSet = commonDataSource.createDataSetUsingExitingDBConnection(
				(DatabaseConnection) conncetion, tempOutTable, false);

		int modelnum = this.getModelNum();
		double sumWeight = 0;
		for (int i = 0; i < modelnum; i++) {
			AdaboostSingleModel predmodel = this.getModel(i);

			double algWeight = predmodel.getPeoso();
			sumWeight += algWeight;
			dataSet = ((Prediction) predmodel.getModel()).apply(dataSet);
			Iterator<String> sampleDvalueIterator = ((AbstractModel) predmodel
					.getModel()).getTrainingHeader().getColumns().getLabel()
					.getMapping().getValues().iterator();
			StringBuffer sampleArray = new StringBuffer();
			
			try {
				adaboostPredictStep(outTable, tempOutTable, dependentColumn,
						st, algWeight, sampleDvalueIterator, sampleArray);
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		adaboostPredictResult(outTable, timeStamp, tempOutTable,
				dependentColumn, st, inforArray,   sumWeight,dataSet);

		return pDataSet;
		} catch (SQLException e1) {
			throw new OperatorException(e1.getLocalizedMessage());
		}finally{
			try {
				st.close();
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * @param inforArray
	 * @param localIterator
	 */
	protected abstract StringBuffer spellArray(StringBuffer inforArray,
			Iterator<String> localIterator);

	/**
	 * @param outTable
	 * @param timeStamp
	 * @param tempOutTable
	 * @param dependentColumn
	 * @param st
	 * @param inforArray
	 * @param localIterator
	 * @param sumc
	 * @throws OperatorException
	 */
protected abstract void adaboostPredictResult(String outTable, long timeStamp,
			String tempOutTable, String dependentColumn, Statement st,
			StringBuffer inforArray,  double sumc,DataSet dataSet)
			throws OperatorException;
	
	/**
	 * @param outTable
	 * @param tempOutTable
	 * @param dependentColumn
	 * @param st
	 * @param algWeight
	 * @param sampleDvalueIterator
	 * @param sampleArray
	 * @throws SQLException
	 */
	protected abstract void adaboostPredictStep(String outTable, String tempOutTable,
			String dependentColumn, Statement st, double algWeight,
			Iterator<String> sampleDvalueIterator, StringBuffer sampleArray)
			throws SQLException;

	/**
	 * @param outTable
	 * @param timeStamp
	 * @param tempOutTable
	 * @param schemaName
	 * @param dependentColumn
	 * @param st
	 * @param inforArray
	 * @return
	 * @throws OperatorException
	 */
	protected abstract String adaboostPredictionInit(String outTable, String tempOutTable,
			long timeStamp, String schemaName, String dependentColumn,
			Statement st, StringBuffer inforArray) throws OperatorException ;

}
