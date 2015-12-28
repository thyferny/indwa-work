
package com.alpine.datamining.operator.randomforest;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.AbstractModel;
import com.alpine.datamining.operator.adboost.AdaboostModelDB2;
import com.alpine.datamining.operator.adboost.AdaboostModelGreenplum;
import com.alpine.datamining.operator.adboost.AdaboostSingleModel;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.operator.training.SingleModel;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;
import org.apache.log4j.Logger;



public abstract class RandomForestModel extends Prediction{

	
	private static final long serialVersionUID = -4269257709330617314L;
	protected static final Logger itsLogger = Logger.getLogger(AdaboostModelGreenplum.class);

	protected RandomForestModel(DataSet trainingDataSet) {
		super(trainingDataSet);
		// TODO Auto-generated constructor stub
	}
	public String[] UPDATE;
	private List<SingleModel> modelList = new LinkedList<SingleModel>();
	private String dependColumn = null;
	private String columnNames = null;
	private String tableName = null;
	public ArrayList<Double> oobEstimateError=new ArrayList<Double>();
	public ArrayList<Double> oobLoss=new ArrayList<Double>();
	public ArrayList<Double> oobMape=new ArrayList<Double>();
	
	public ArrayList<Double> getOobEstimateError() {
		return oobEstimateError;
	}
	public void setOobEstimateError(ArrayList<Double> oobEstimateError) {
		this.oobEstimateError = oobEstimateError;
	}
	public ArrayList<Double> getOobLoss() {
		return oobLoss;
	}
	public void setOobLoss(ArrayList<Double> oobLoss) {
		this.oobLoss = oobLoss;
	}
	public ArrayList<Double> getOobMape() {
		return oobMape;
	}
	public void setOobMape(ArrayList<Double> oobMape) {
		this.oobMape = oobMape;
	}
	
	public List<SingleModel> getModelList() {
		return modelList;
	}
	public void setModelList(List<SingleModel> modelList) {
		this.modelList = modelList;
	}
	public String getDependColumn() {
		return dependColumn;
	}
	public void setDependColumn(String dependColumn) {
		this.dependColumn = dependColumn;
	}
	public String getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public void addModel(SingleModel model) {
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

 
	public void setTableNames(String tableName) {
		this.tableName = tableName;
	}

	public String getTableNames() {
		return tableName;
	}

	public int getModelNum() {
		return this.modelList.size();
	}

	public SingleModel getModel(int i) {
		return this.modelList.get(i);
	}
	
	@Override
	public DataSet performPrediction(DataSet pDataSet, Column column)
			throws OperatorException {
		DatabaseConnection conncetion = ((DBTable) pDataSet.getDBTable())
				.getDatabaseConnection();
		if( this instanceof RandomForestModelDB2)
		{
			((RandomForestModelDB2)this).DB2Connection=conncetion.getConnection();
		}
		
		if(this.modelList.get(0) instanceof DecisionTreeModel)
		{
			return DecisionPredict(pDataSet);
		}
		else if( this.modelList.get(0) instanceof RegressionTreeModel )
		{
			return RegressionPredct(pDataSet,column);
		}
		else
		{
			return null;
		}
	}
	private DataSet RegressionPredct(DataSet dataSet, Column predictedLabel) throws OperatorException {
		StringBuffer valueString = new StringBuffer("(");
        DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
        String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
        for(int i=0;i<this.modelList.size();i++)
        {
        	((RegressionTreeModel)this.modelList.get(i)).getRulesAndConfidence(null, ((RegressionTreeModel)this.modelList.get(0)).getRoot(), valueString);
        	valueString.append("  +");
        }
        	valueString=valueString.deleteCharAt(valueString.length()-1);
        	valueString.append(")/").append(this.modelList.size()*1.0);
        	StringBuffer set = new StringBuffer( "\""+predictedLabel.getName()+"\"");
//        UPDATE=new String[1];
//        UPDATE[0]=predictedLabel.getName();
        StringBuffer value = valueString;
        String sql = "update "+tableName+" set "+set+"=("+value + ")";
        Statement st = null;
        try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("TreeModelDB.performPrediction():sql="+sql);
			st.execute(sql);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		return dataSet;
	 
//		((RegressionTreeModel)this.modelList.get(0)).getRulesAndConfidence(null, ((RegressionTreeModel)this.modelList.get(0)).getRoot(), valueString);
//		return null;
	}
	private DataSet DecisionPredict(DataSet pDataSet) throws OperatorException {
		DBSource commonDataSource = null;
		DatabaseConnection conncetion = ((DBTable) pDataSet.getDBTable())
				.getDatabaseConnection();
	 
//		if( this instanceof AdaboostModelDB2)
//		{
//			((AdaboostModelDB2)this).DB2Connection=conncetion.getConnection();
//		}
		
		
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

		tempOutTable = randomForestPredictionInit(outTable, tempOutTable,
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
		 
		for (int i = 0; i < modelnum; i++) {
			SingleModel predmodel = this.getModel(i);

 			
			 
			dataSet =  predmodel.apply(dataSet);
			Iterator<String> sampleDvalueIterator = ((AbstractModel) predmodel
					).getTrainingHeader().getColumns().getLabel()
					.getMapping().getValues().iterator();
			StringBuffer sampleArray = new StringBuffer();
			
			try {
				randomForestPredictStep(outTable, tempOutTable, dependentColumn,
						st,  sampleDvalueIterator, sampleArray);
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		randomForestPredictResult(outTable, timeStamp, tempOutTable,
				dependentColumn, st, inforArray,   modelnum,dataSet);

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
	
	
	protected abstract StringBuffer spellArray(StringBuffer inforArray,
			Iterator<String> localIterator);

	
protected abstract void randomForestPredictResult(String outTable, long timeStamp,
			String tempOutTable, String dependentColumn, Statement st,
			StringBuffer inforArray,  double sumc,DataSet dataSet)
			throws OperatorException;
	
	
	protected abstract void randomForestPredictStep(String outTable, String tempOutTable,
			String dependentColumn, Statement st, Iterator<String> sampleDvalueIterator, StringBuffer sampleArray)
			throws SQLException;

	
	protected abstract String randomForestPredictionInit(String outTable, String tempOutTable,
			long timeStamp, String schemaName, String dependentColumn,
			Statement st, StringBuffer inforArray) throws OperatorException ;


}
