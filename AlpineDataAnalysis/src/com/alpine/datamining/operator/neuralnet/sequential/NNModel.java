
package com.alpine.datamining.operator.neuralnet.sequential;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.alpine.datamining.MinerInit;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.ColumnStats;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.Data;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseRowIterator;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.utility.AlpineRandom;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public class NNModel extends Prediction {
    private static Logger logger = Logger.getLogger(NNModel.class);

    
	private static final long serialVersionUID = -3149637264324149694L;
	private static final Sigmod sigmod = new Sigmod();
	private static final Linear linear = new Linear();	
	private DataSet oldDataSet;
	private String[] column;	
//	private String specifyColumn;	
	protected double bestError = Double.MAX_VALUE;	
	private HashMap<String,HashMap<String,String>> allTransformMap_columnKey=new HashMap<String,HashMap<String,String>>();
	
	protected IDataSourceInfo dataSourceInfo = null;
	private IMultiDBUtility multiDBUtility = null;
	
	public HashMap<String, HashMap<String, String>> getAllTransformMap_columnKey() {
		return allTransformMap_columnKey;
	}

	public void setAllTransformMap_columnKey(
			HashMap<String, HashMap<String, String>> allTransformMapColumnKey) {
		allTransformMap_columnKey = allTransformMapColumnKey;
	}

	protected NodeInput[] nodeInputs = new NodeInput[0];
	
	protected NodeInner[] nodeInners = new NodeInner[0];
	
	protected NodeOutput[] nodeOutputs = new NodeOutput[0];	
	
	protected ArrayList<String> updateColumns = new ArrayList<String>();
	
	protected List<String> columnNamesList;
	
	protected double RSquare = Double.NaN;
	
	protected double deviance = Double.NaN;
	
	public double getRSquare() {
		return RSquare;
	}

	public void setRSquare(double rSquare) {
		RSquare = rSquare;
	}

	public double getDeviance() {
		return deviance;
	}

	public void setDeviance(double deviance) {
		this.deviance = deviance;
	}

	public double getNullDeviance() {
		return nullDeviance;
	}

	public void setNullDeviance(double nullDeviance) {
		this.nullDeviance = nullDeviance;
	}

	protected double nullDeviance = Double.NaN;
	private String[] hiddenLayerNames = null;
	protected int[] hiddenLayerSizes = null;
	protected boolean normalize = true;

	boolean useCFunction = false;

	
	public ArrayList<String> getUpdateColumns() {
		return updateColumns;
	}

	
	public void setUpdateColumns(ArrayList<String> updateColumns) {
		this.updateColumns = updateColumns;
	}

	public NNModel(DataSet trainingDataSet,DataSet oldDataSet,List<String> columnNamesList) {
		super(trainingDataSet);
		this.oldDataSet=oldDataSet;
		this.column = com.alpine.datamining.db.CommonUtility.getRegularColumnNames(trainingDataSet);
		this.columnNamesList = columnNamesList;
//		this.specifyColumn=specifyColumn;;
		useCFunction = MinerInit.isUseCFunction();
		dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) trainingDataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) trainingDataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
	}
	protected StringBuffer getWhere()
	{
		StringBuffer where = new StringBuffer(" where ");
		for (int i = 0; i < column.length; i++)
		{
			if (i != 0)
			{
				where.append(" and ");
			}
			where.append(StringHandler.doubleQ(column[i])).append(" is not null ");
		}
		return where;
	}

	protected StringBuffer getWherePredict()
	{
		StringBuffer where = new StringBuffer(" where ");
		boolean first = true;
		for (Column column : oldDataSet.getColumns())
		{
//			if (column.isNominal())
//			{
//				continue;
//			}
			if (!first)
			{
				where.append(" and ");
			}
			else
			{
				first = false;
			}
			where.append(StringHandler.doubleQ(column.getName())).append(" is not null ");
		}
		if (first)
		{
			return new StringBuffer("");
		}
		else
		{
			return where;
		}
	}
	
	public void train(DataSet dataSet, List<String[]> hiddenLayers, int maxCycles, double maxError, double learningRate, double momentum, boolean decay, boolean normalize, AlpineRandom randomGenerator, int fetchSize, boolean adjustPerRow) throws OperatorException {

		this.normalize = normalize;
		Column label = dataSet.getColumns().getLabel();
		int numberOfClasses = getNumberOfClasses(label);
		// SETUP NN
		inputLayerInit(dataSet, normalize);

		double labelMin;
		double labelMax;
		if (label.isNominal())
		{
			labelMin = Double.NaN;
			labelMax = Double.NaN;
		}
		else
		{
			labelMin = dataSet.getStatistics(label, ColumnStats.MINIMUM);
			labelMax = dataSet.getStatistics(label, ColumnStats.MAXIMUM);
		}
		outputLayerInit(label, numberOfClasses, labelMin, labelMax, randomGenerator);
		hiddenLayersinit(dataSet, label, hiddenLayers, randomGenerator);

		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		if (adjustPerRow)
		{
			adjustPerRow(dataSet, hiddenLayers, maxCycles, maxError,
			learningRate, momentum, decay, normalize, randomGenerator,
			fetchSize,adjustPerRow,label, numberOfClasses, databaseConnection, tableName);
		}
		else
		{
			adjustPerWholeData(dataSet, hiddenLayers, maxCycles, maxError,
				learningRate, momentum, decay, normalize, randomGenerator,
				fetchSize,adjustPerRow, label, numberOfClasses, databaseConnection, tableName);
		}
	}
	public String getArraySum()
	{
		return "sum";
	}
	protected Double[] getResult(ResultSet rs) throws SQLException
	{
		return (Double[])rs.getArray(1).getArray();
	}
	protected void adjustPerWholeData(DataSet dataSet,
			List<String[]> hiddenLayers, int maxCycles, double maxError,
			double learningRate, double momentum, boolean decay,
			boolean normalize, AlpineRandom randomGenerator, int fetchSize, boolean adjustPerRow,
			Column label, int numberOfClasses,
			DatabaseConnection databaseConnection, String tableName)
			throws OperatorException {
		double totalSize = dataSet.size();

		Statement st = null;
		ResultSet rs = null;

		try {
		    	st = databaseConnection.createStatement(true);
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
		double error = 0;
		// optimization loop
		for (int cycle = 0; cycle < maxCycles; cycle++) {
			double tempRate = learningRate;
			if (decay) {
				tempRate /= (cycle + 1);
			}
			
			StringBuffer sql = new StringBuffer();
			sql.append("select ").append(getArraySum()).append("(");
//			sql.append("select ").append("(");
			sql.append(getAllWeightChange(getNumberOfClasses(label)));
			sql.append(") from ").append(tableName);
			
			try{
				logger.debug("NNModel.adjustPerWholeData():sql="+sql);
			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				Double[] currentChanges = getResult(rs);
//				String currentChangesString= new String();
				for(int i = 0; i < currentChanges.length; i++){
					if (Double.isNaN(currentChanges[i]) || Double.isInfinite(currentChanges[i])){
						try{
							if (rs != null)
							{
								rs.close();
							}
							if (st != null)
							{
								st.close();
							}
							databaseConnection.getConnection().setAutoCommit(true);
						}catch(SQLException e)
						{
							e.printStackTrace();
//							throw new OperatorException(e.getLocalizedMessage ());
							copyBestErrorWeightsToWeights();
							return;
						}
						copyBestErrorWeightsToWeights();
						return;
					}
				}
				error = currentChanges[currentChanges.length - 1]/numberOfClasses/totalSize;
				if(error < bestError)
				{
					bestError = error;
					copyWeightsToBestErrorWeights();
				}
				updateWeight(currentChanges, tempRate, momentum);
			}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				copyBestErrorWeightsToWeights();
//				throw new OperatorException(e.getLocalizedMessage());
				return;
			}
//			error /= totalSize;
			logger.debug("cycle"+cycle+";error:"+error);
			if (error < maxError) {
				logger.debug("loop break : "+cycle+" error: "+ error);
				break;
			}

			if (Double.isInfinite(error) || Double.isNaN(error)) {
				if (Tools.isLessEqual(learningRate, 0.0d)) // should hardly happen
					throw new RuntimeException("Cannot reset network to a smaller learning rate.");
				learningRate /= 2;
				train(dataSet, hiddenLayers, maxCycles, maxError, learningRate, momentum, decay,  normalize, randomGenerator, fetchSize, adjustPerRow);
			}
		}
		copyBestErrorWeightsToWeights();

		try{
			if (rs != null)
			{
				rs.close();
			}
			if (st != null)
			{
				st.close();
			}
		}catch(SQLException e)
		{
			e.printStackTrace();
//			throw new OperatorException(e.getLocalizedMessage ());
//			copyBestErrorWeightsToWeights();
			return;
		}
	}

	protected void copyWeightsToBestErrorWeights() {
		for (NodeInner nodeInner : nodeInners) {	
			// skip outputs here and add them later
			// layer name
				nodeInner.copyWeightsToBestErrorWeights();
		}
	}
	protected void copyBestErrorWeightsToWeights() {
		for (NodeInner nodeInner : nodeInners) {	
			// skip outputs here and add them later
			// layer name
				nodeInner.copyBestErrorWeightsToWeights();
		}
	}
	private void adjustPerRow(DataSet dataSet,
			List<String[]> hiddenLayers, int maxCycles, double maxError,
			double learningRate, double momentum, boolean decay,
			boolean normalize, AlpineRandom randomGenerator, int fetchSize,boolean adjustPerRow,
			Column label, int numberOfClasses,
			DatabaseConnection databaseConnection, String tableName)
			throws OperatorException {
		double totalSize = dataSet.size();
		ArrayList<double[]> dataArray = null;
		boolean needReload = needReload(fetchSize, totalSize);

		Statement st = null;
		ResultSet rs = null;

		st = createStatement(fetchSize, needReload, databaseConnection);


		StringBuffer sql = createSql(tableName, label);

		
		if (!needReload)
		{
			dataArray = getDataArray(st, sql, dataSet);
		}
		
		// optimization loop
		for (int cycle = 0; cycle < maxCycles; cycle++) {
			DatabaseRowIterator iter = null;
			if (needReload)
			{
				try
				{
					logger.debug("NNModel.train():sql="+sql);
					rs = st.executeQuery(sql.toString());
					iter = new DatabaseRowIterator(rs);
				}
				catch(SQLException e)
				{
					throw new OperatorException(e.getLocalizedMessage());
				}
			}

			double error = 0;
			int rowNumber = 0;
		
			while (hasNext(totalSize, needReload, iter, rowNumber))
			{
				double[] row = getRow(dataSet, dataArray, needReload, label, iter,
						rowNumber);
				rowNumber++;
				if (row == null)
				continue;

				resetNetwork();
				calculateValue(row);
	
				double tempRate = learningRate;
				if (decay) {
					tempRate /= (cycle + 1);
				}

				error += (calculateError(row) / numberOfClasses);
				update(row, tempRate, momentum);
			}
			
			error /= totalSize;
			if(error < bestError)
			{
				bestError = error;
				copyWeightsToBestErrorWeights();
			}
			logger.debug("cycle"+cycle+";error:"+error);

			if (error < maxError) {
				logger.debug("loop break : "+cycle+" error: "+ error);
				break;
			}
			
			if (Double.isInfinite(error) || Double.isNaN(error)) {
				if (Tools.isLessEqual(learningRate, 0.0d)) // should hardly happen
					throw new RuntimeException("Cannot reset network to a smaller learning rate.");
				learningRate /= 2;
				train(dataSet, hiddenLayers, maxCycles, maxError, learningRate, momentum, decay,  normalize, randomGenerator, fetchSize, adjustPerRow);
			}
		}
		copyBestErrorWeightsToWeights();
		try{
			if (rs != null)
			{
				rs.close();
			}
			if (st != null)
			{
				st.close();
			}
			databaseConnection.getConnection().setAutoCommit(true);
		}catch(SQLException e)
		{
			return;
//			throw new OperatorException(e.getLocalizedMessage ());
		}
	}

	
	public void statistics() throws OperatorException
	{
		DataSet dataSet = getTrainingHeader();
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		Statement st = null;

		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		Column label = dataSet.getColumns().getLabel();
		if (label.isNumerical())
		{
			cacluateRSquare(dataSet, tableName, st, label);

		}
		else if (label.isNominal())
		{
			caculateDeviance(dataSet, tableName, st, label);
			
		}
		try{
			if (st != null)
			{
				st.close();
			}
		}catch(SQLException e)
		{
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	protected void cacluateRSquare(DataSet dataSet, String tableName,
			Statement st, Column label) throws OperatorException {
		StringBuffer RSquareSQL = new StringBuffer();
		String labelName=StringHandler.doubleQ(label.getName());
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg(").append(labelName).append(") from ").append(tableName);
		double avg = 0.0;
		try {
			logger.debug("NNModel.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				avg = rs.getDouble(1);
			}	
		} catch (SQLException e) {
			e.printStackTrace();
//			throw new OperatorException(e.getLocalizedMessage());
			return;
		}

		StringBuffer predictedValueSQL = new StringBuffer();
		if (useCFunction)
		{
			predictedValueSQL.append(getOutput(getNumberOfClasses(label),1,false));
		}
		else
		{
			predictedValueSQL.append(nodeOutputs[0].computeValue(true, dataSet));
		}
//		StringBuffer predictedValueSQL = new StringBuffer(getOutput(1));
		// Rsquare = 1-SSerror/SStotal = 1 - sum(Yi - Yihat)/sum(Yi - Ybar)
		RSquareSQL.append("select 1 - sum((").append(predictedValueSQL).append("-").append(labelName).
		append(")*(").append(predictedValueSQL).append("-").append(labelName).
		append("))*1.0/sum((").append(labelName).append("-").append(avg).append(")*(").append(labelName).append("-").append(avg).append(")) from ").append(tableName);
		RSquareSQL.append(getWhere());
		try {
			logger.debug("NNModel.cacluateRSquare():sql="+RSquareSQL);
			ResultSet rs = st.executeQuery(RSquareSQL.toString());
			if (rs.next())
			{
				RSquare = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
//			throw new OperatorException(e.getLocalizedMessage());
			return;
		}
	}

	private void caculateDeviance(DataSet dataSet, String tableName,
			Statement st, Column label)
			throws OperatorException {
		StringBuffer devianceSQL = new StringBuffer();
		int numberOfClasses = getNumberOfClasses(label);
//		String labelName=StringHandler.doubleQ(label.getName());
		String firstValue = label.getMapping().mapIndex(0);
		firstValue=StringHandler.escQ(firstValue);
		StringBuffer [] classProbabilitiesSQL = new StringBuffer[numberOfClasses];

		for (int c = 0; c < numberOfClasses; c++) {
			if (useCFunction)
			{
				classProbabilitiesSQL[c] = new StringBuffer(getOutput(numberOfClasses,c+1,false));
			}
			else
			{
				classProbabilitiesSQL[c] = new StringBuffer(nodeOutputs[c].computeValue(true, dataSet));
			}
		}

		devianceSQL.append(" select -2*sum( case ");
		for(int i = 0; i < numberOfClasses; i++)
		{
			if (i == numberOfClasses - 1)
			{
				devianceSQL.append(" else ");
			}
			else
			{
				devianceSQL.append(" when ").append(
						StringHandler.doubleQ(label.getName())).append("='").append(StringHandler.escQ(label.getMapping().mapIndex(i))).append("' then ");
			}
			devianceSQL.append(
					" ln(").append(classProbabilitiesSQL[i]).append("+0.0000001)");
		}
		devianceSQL.append(" end ) ");
		devianceSQL.append(" from ").append(tableName).append(getWhere());

		try {
			logger.debug("NNModel.caculateDeviance():sql="+devianceSQL);
			ResultSet rs = st.executeQuery(devianceSQL.toString());
			if (rs.next())
			{
				deviance = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
//			throw new OperatorException(e.getLocalizedMessage());
			return;
		}

		StringBuffer countSQL = new StringBuffer();
		countSQL.append(" select ")//.append(StringHandler.doubleQ(label.getName())).append(", ")
		.append(" count(*) from ").append(tableName).append(getWhere()).append(" group by ").append(
				StringHandler.doubleQ(label.getName()));

		double totalCount = 0.0;
		ArrayList<Double> countArray = new ArrayList<Double>();
		try {
			logger.debug("NNModel.caculateDeviance():sql="+countSQL);
			ResultSet rs = st.executeQuery(countSQL.toString());
			while (rs.next())
			{
//				String string = rs.getString(1);
				double count = rs.getDouble(1);
				countArray.add(count);
				totalCount+= count;
			}
		} catch (SQLException e) {
			e.printStackTrace();
//			throw new OperatorException(e.getLocalizedMessage());
			return;
		}
		if (totalCount != 0)
		{
			nullDeviance = 0;
			for(int i = 0; i < numberOfClasses; i++)
			{
				nullDeviance += ((Double)countArray.get(i)) * Math.log(((Double)countArray.get(i))/totalCount);
			}
			nullDeviance = -2*nullDeviance;
		}

	}
	

	
	private StringBuffer createSql(String tableName, Column label) {
		StringBuffer sql = new StringBuffer("select ");
		for (int i = 0; i < columnNamesList.size(); i++)
		{
			if (i != 0)
			{
				sql.append(",");
			}
			sql.append(StringHandler.doubleQ(columnNamesList.get(i)));
		}
		sql.append(",").append(StringHandler.doubleQ(label.getName()));

		sql.append(" from ").append(tableName);
		return sql;
	}

	
	private boolean needReload(int fetchSize, double totalSize) {
		boolean needReload;
		if (totalSize > fetchSize && fetchSize != 0)
		{
			needReload = true;
		}
		else
		{
			needReload = false;
		}
		return needReload;
	}

	
	private Statement createStatement(int fetchSize, boolean needReload,
			DatabaseConnection databaseConnection)
			throws OperatorException {
		Statement st = null;
		try {
			if (needReload)
			{
				databaseConnection.getConnection().setAutoCommit(false);
				st = databaseConnection.createStatement(false);
				st.setFetchSize(fetchSize);
			}		
			else
			{
		    	st = databaseConnection.createStatement(true);
			}
		} catch (SQLException e) {
			try {
				databaseConnection.getConnection().setAutoCommit(true);
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new OperatorException(e1.getMessage());
			}
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return st;
	}

	
	private double[] getRow(DataSet dataSet,
			ArrayList<double[]> dataArray, boolean needReload, Column label,
			DatabaseRowIterator iter, int rowNumber) {
		Data data;
		double[] row;
		if (needReload)
		{
			data = new Data(iter.next(), dataSet.getColumns());
			row = getRowFromData(dataSet, label, data);
		}
		else
		{
			row  = dataArray.get(rowNumber);
		}
		for (int i=0 ;i< row.length; i++)
		{
			if (Double.isNaN(row[i]))
			{
				return null;
			}
		}
		return row;
	}

	
	private double[] getRowFromData(DataSet dataSet, Column label,
			Data data) {
		double[] row = new double[columnNamesList.size()+1];
		for (int i = 0; i < columnNamesList.size(); i++)
		{
			row[i] = data.getValue(dataSet.getColumns().get(columnNamesList.get(i)));
		}
		row[columnNamesList.size()] = data.getValue(label);
		return row;
	}

	
	private boolean hasNext(double totalSize, boolean needReload,
			DatabaseRowIterator reader, int rowNumber) {
		boolean hasNext;
		if (needReload)
		{
			if (reader.hasNext())
			{
				hasNext = true;
			}
			else
			{
				hasNext = false;
			}
		}		
		else
		{
			if (rowNumber < totalSize)
			{
				hasNext = true;
			}
			else
			{
				hasNext = false;
			}
		}
		return hasNext;
	}
	
	private ArrayList<double[]> getDataArray(Statement st, StringBuffer sql, DataSet dataSet) throws OperatorException
	{
		ArrayList<double[]> dataArray = new ArrayList<double[]>();
		Column label = dataSet.getColumns().getLabel();
		try{
			logger.debug("NNModel.getDataArray():sql="+sql);
			ResultSet rs = st.executeQuery(sql.toString());
			DatabaseRowIterator reader = new DatabaseRowIterator(rs);
			while(reader.hasNext())
			{
				Data data = new Data(reader.next(), dataSet.getColumns());
				double[] row = getRowFromData(dataSet, label, data);
				dataArray.add(row);
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
		}catch(SQLException e)
		{
			throw new OperatorException(e.getLocalizedMessage ());
		}
		return dataArray;
	}

	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException {

		String newTableName=((DBTable) dataSet.getDBTable())
		.getTableName();
		
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		Statement st = null;
		ResultSet rs = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}
		//StringBuffer();
		resetNetwork();
		StringBuffer confidenceDrop = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		if (predictedLabel.isNominal()) {
			int numberOfClasses = getNumberOfClasses(getLabel());
			StringBuffer [] classProbabilitiesSql = new StringBuffer[numberOfClasses];

			getProbabilityiesSql(dataSet, newTableName, st, rs,
					confidenceDrop, numberOfClasses, classProbabilitiesSql);
			StringBuffer totalSQL = new StringBuffer("0.0");
			for (int c = 0; c < numberOfClasses; c++) {
				totalSQL.append( "+").append(classProbabilitiesSql[c]);
			}

			StringBuffer caseSql = new StringBuffer();
			for (int c = 0; c < numberOfClasses; c++) {
				classProbabilitiesSql[c].append("/(").append(totalSQL).append(")");
				
			}
			StringBuffer [] biggerSql = new StringBuffer[numberOfClasses - 1];
			for (int c = 0; c < numberOfClasses - 1; c++) {
				biggerSql[c] = new StringBuffer("(");
				boolean first = true;
				for (int j = c + 1; j < numberOfClasses; j++)
				{
						if (!first)
						{
							biggerSql[c].append( " and ");
						}
						else
						{
							first = false;
						}
						biggerSql[c].append(classProbabilitiesSql[c]).append(">=").append(classProbabilitiesSql[j]);
				}
				biggerSql[c].append(")");
			}
			
			sql = getNominalUpdate(dataSet, predictedLabel, newTableName,
					numberOfClasses, classProbabilitiesSql, caseSql, biggerSql);
//			sql.append(confidenceDrop);

			updateColumns.add(predictedLabel.getName());
			for ( int c = 0; c < numberOfClasses; c++)
			{
				updateColumns.add(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName());
			}

		} else {
			updateColumns.add(predictedLabel.getName());
			StringBuffer valueSQL = getValueSql();
			sql.append("update ").append(newTableName).append(" set ").append(StringHandler.doubleQ(predictedLabel.getName())+" = ").append(valueSQL);
			sql.append(getWherePredict());

		}
		try {
			logger.debug("NNModel.performPrediction():sql="+sql);
			st.execute(sql.toString());
			if(getLabel().isNominal())
			{
				dropConfidence(st, confidenceDrop);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage ());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null){
					st.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage ());
			}
		}

		return dataSet;
	}

	protected StringBuffer getNominalUpdate(DataSet dataSet,
			Column predictedLabel, String newTableName, int numberOfClasses,
			StringBuffer[] classProbabilitiesSql, StringBuffer caseSql,
			StringBuffer[] biggerSql) {
		StringBuffer sql = new StringBuffer();
		caseSql.append(" (case ");
		for (int c = 0; c < numberOfClasses - 1; c++) {
			caseSql.append(" when ").append(biggerSql[c]).append(" then '").append(StringHandler.escQ(getLabel().getMapping().mapIndex(c))).append("'");
		}
		caseSql.append(" else '").append(StringHandler.escQ(getLabel().getMapping().mapIndex(numberOfClasses - 1))).append("' end)");
		sql.append(" update "+ newTableName+" set ("+StringHandler.doubleQ(predictedLabel.getName()));
		for ( int c = 0; c < numberOfClasses; c++)
		{
			sql.append(", "+StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName()));
		}
		sql.append( ") = (").append(caseSql);
		for (int c = 0; c < numberOfClasses; c++)
		{
			sql.append( ",").append(classProbabilitiesSql[c]);
		}
		sql.append(")");
		sql.append(getWherePredict());
		return sql;
	}

	protected void dropConfidence(Statement st, StringBuffer confidenceDrop)
			throws SQLException {
		if (useCFunction)
		{
			logger.debug("NNModel.dropConfidence():sql="+confidenceDrop.toString());
			st.execute(confidenceDrop.toString());
		}
	}

	protected StringBuffer getValueSql() {
		StringBuffer valueSQL = new StringBuffer();
		if (useCFunction)
		{
			valueSQL.append(getOutput(1,1,true));
		}
		else
		{
			valueSQL.append(nodeOutputs[0].computeValuePrediction(true, oldDataSet));
		}
		return valueSQL;
	}

	protected void getProbabilityiesSql(DataSet dataSet,
			String newTableName, Statement st, ResultSet rs,
			StringBuffer confidenceDrop, int numberOfClasses,
			StringBuffer[] classProbabilitiesSQL) throws OperatorException {
		if (useCFunction)
		{
			getProbabilityiesSqlFunction(dataSet, newTableName, st, rs,
					confidenceDrop, numberOfClasses, classProbabilitiesSQL);
		}
		else
		{
			for (int c = 0; c < numberOfClasses; c++) {
				classProbabilitiesSQL[c] = new StringBuffer(nodeOutputs[c].computeValuePrediction(true, oldDataSet));
			}
		}
	}
	protected String getFloatArray()
	{
		return " float[] ";
	}
	protected void getProbabilityiesSqlFunction(DataSet dataSet,
			String newTableName, Statement st, ResultSet rs,
			StringBuffer confidenceDrop, int numberOfClasses,
			StringBuffer[] classProbabilitiesSQL) throws OperatorException {
		StringBuffer confidenceColumn = new StringBuffer();
		StringBuffer confidenceUpdate = new StringBuffer();
		StringBuffer confidenceAdd = new StringBuffer();
		Random random = new Random();
		confidenceColumn.append("predicconf").append(Math.abs(random.nextInt()));
		confidenceAdd.append("alter table ").append(newTableName).append(" add  ").append(confidenceColumn).append(getFloatArray());
		confidenceUpdate.append("update ").append(newTableName).append(" set (").append(confidenceColumn+") = (").append(getAllOutput(numberOfClasses,true)+")").append(getWherePredict());
		confidenceDrop.append("alter table ").append(newTableName).append(" drop column ").append(confidenceColumn);

		try {
			logger.debug("NNModel.getProbabilityiesSqlFunction():sql="+confidenceAdd);
			st.execute(confidenceAdd.toString());
			logger.debug("NNModel.getProbabilityiesSqlFunction():sql="+confidenceUpdate);
			st.execute(confidenceUpdate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage ());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage ());
			}
		}

		for (int c = 0; c < numberOfClasses; c++) {
			classProbabilitiesSQL[c] = new StringBuffer();
			getClassProbabilities(classProbabilitiesSQL, confidenceColumn, c);
		}
	}

	protected void getClassProbabilities(StringBuffer[] classProbabilitiesSQL,
			StringBuffer confidenceColumn, int c) {
		classProbabilitiesSQL[c].append(confidenceColumn).append("[").append(c+1).append("]");
	}

	public String[] getColumnNames() {
		return this.column;
	}
	
	public NodeInput[] getInputNodes() {
		return this.nodeInputs;
	}

	public NodeOutput[] getOutputNodes() {
		return this.nodeOutputs;
	}
	
	public NodeInner[] getInnerNodes() {
		return this.nodeInners;
	}
	
	protected int getNumberOfClasses(Column label) {
		int numberOfClasses = 1;
		if (label.isNominal()) {
			numberOfClasses = label.getMapping().size();
		}
		return numberOfClasses;
	}

	private void addNode(NodeInner node) {
		NodeInner[] newInnerNodes = new NodeInner[nodeInners.length + 1];
		System.arraycopy(nodeInners, 0, newInnerNodes, 0, nodeInners.length);
		newInnerNodes[newInnerNodes.length - 1] = node;
		nodeInners = newInnerNodes;
	}

	protected void resetNetwork() {
		for (int i = 0; i < nodeOutputs.length; i++) {
			nodeOutputs[i].reset();
		}
	}
	
	private void update(double[] row, double learningRate, double momentum) {
		for (int i = 0; i < nodeOutputs.length; i++) {
			nodeOutputs[i].update(row, learningRate, momentum);
		}
	}
	
	private void calculateValue(double[] row) {
		for (int i = 0; i < nodeOutputs.length; i++) {
			nodeOutputs[i].computeValue(true, row);
		}
	}

	private double calculateError(double[] row) {
		for (int i = 0; i < nodeInputs.length; i++) {
			nodeInputs[i].computeError(true, row);
		}
		double totalError = 0.0d;
		for (int i = 0; i < nodeOutputs.length; i++) {
			double error = nodeOutputs[i].computeError(false, row);
			totalError += error * error;
		}
		return totalError;
	}

	private int getDefaultLayerSize( Column label) {
		return (int)Math.round((columnNamesList.size() + getNumberOfClasses(label)) / 2.0d) + 1;
	}
	
	protected void inputLayerInit(DataSet dataSet, boolean normalize) {
		nodeInputs = new NodeInput[columnNamesList.size()];
		int a = 0;
		for (Column column : dataSet.getColumns()) {
			if (!columnNamesList.contains(column.getName()))
			{
				continue;
			}
			int index = columnNamesList.indexOf(column.getName());
			
			
			HashMap<String, HashMap<String, String>> AllTransformMap_columnKey=getAllTransformMap_columnKey();
			Iterator<String> i=AllTransformMap_columnKey.keySet().iterator();
			while(i.hasNext())
			{
				String columnName=i.next();

					HashMap<String,String> TransformMap_columnKey=AllTransformMap_columnKey.get(columnName);
					if(TransformMap_columnKey.containsKey((String)column.getName()))
					nodeInputs[a] = new NodeInput(column.getName(),TransformMap_columnKey);	
			}
			if(nodeInputs[a]==null)
			nodeInputs[a] = new NodeInput(column.getName());
			
			double range  = 1;
			double offset = 0;
			if (normalize) {
				double min    = dataSet.getStatistics(column, ColumnStats.MINIMUM);
				double max    = dataSet.getStatistics(column, ColumnStats.MAXIMUM);
				range  = (max - min) / 2;
				offset = (max + min) / 2;
			} 				
			nodeInputs[a].setColumn(column, getOldColumn(column), range, offset, normalize, index);
			nodeInputs[a].setdBType(dataSourceInfo.getDBType());
			a++;
		}
	}
	protected Column getOldColumn(Column column){
		for (Column oldColumn : oldDataSet.getColumns()) {
			if(allTransformMap_columnKey != null && allTransformMap_columnKey.containsKey(oldColumn.getName()) && allTransformMap_columnKey.get(oldColumn.getName()).containsKey(column.getName())){
				return oldColumn;
			}
		}
		return null;
	}

	private void outputLayerInit(Column label, int numberOfClasses, double min, double max, AlpineRandom randomGenerator) {
		double range  = (max - min) / 2;
		double offset = (max + min) / 2;
//		.println(range+" "+offset);
		nodeOutputs = new NodeOutput[numberOfClasses];
		for (int o = 0; o < numberOfClasses; o++) {
			if (!label.isNominal()) {
				nodeOutputs[o] = new NodeOutput(label.getName(), label, range, offset, columnNamesList.size());
			} else {
				nodeOutputs[o] = new NodeOutput(label.getMapping().mapIndex(o), label, range, offset,columnNamesList.size());
				nodeOutputs[o].setClassIndex(o);
			}
			NodeInner actualOutput = null;
			if (label.isNominal()) {
				String classValue = label.getMapping().mapIndex(o);
				actualOutput = new NodeInner("Class '" + classValue + "'", NNNode.OUTPUT, randomGenerator, sigmod);
			} else {
				actualOutput = new NodeInner("Regression", NNNode.OUTPUT, randomGenerator, linear);
			}
			addNode(actualOutput);
			NNNode.connect(actualOutput, nodeOutputs[o]);
		}
	}

	private void hiddenLayersinit(DataSet dataSet, Column label, List<String[]> hiddenLayerList, AlpineRandom randomGenerator) {
//		layerNames = null;
//		layerSizes = null;
		if (hiddenLayerList.size() > 0) {
			hiddenLayerNames = new String[hiddenLayerList.size()];
			hiddenLayerSizes = new int[hiddenLayerList.size()]; 

			int index = 0;
			Iterator<String[]> i = hiddenLayerList.iterator();
			while (i.hasNext()) {
				String[] nameSizePair = i.next();
				hiddenLayerNames[index] = nameSizePair[0];
				int layerSize = Integer.valueOf(nameSizePair[1]);
				if (layerSize <= 0)
					layerSize = getDefaultLayerSize(label);
				hiddenLayerSizes[index] = layerSize;
				index++;
			}
		} else {
			// create at least one hidden layer if no other layers were created
			//log("No hidden layers defined. Using default hidden layer.");
			hiddenLayerNames = new String[] { "Hidden" };
			hiddenLayerSizes = new int[] { getDefaultLayerSize(label) };
		}
		
		int lastLayerSize = 0;
		for (int layerIndex = 0; layerIndex < hiddenLayerNames.length; layerIndex++) {
			//String layerName = layerNames[layerIndex];
			int numberOfNodes = hiddenLayerSizes[layerIndex];
			for (int nodeIndex = 0; nodeIndex < numberOfNodes; nodeIndex++) {
				NodeInner nodeInner = new NodeInner("NNNode " +(layerIndex+1)+"."+ (nodeIndex + 1), layerIndex, randomGenerator, sigmod);
				addNode(nodeInner);
				if (layerIndex > 0) {
					// connect to all nodes of previous layer
					for (int i = nodeInners.length - nodeIndex - 1 - lastLayerSize; i < nodeInners.length - nodeIndex - 1; i++) {
						NNNode.connect(nodeInners[i], nodeInner);
					}
				}
			}
			lastLayerSize = numberOfNodes;
		}

		int firstLayerSize = hiddenLayerSizes[0];
		int numberOfUsedColumns = columnNamesList.size();
		int numberOfClasses = getNumberOfClasses(label);
		if (firstLayerSize == 0) { // direct connection between in- and outputs
			for (int i = 0; i < numberOfUsedColumns; i++) {
				for (int o = 0; o < numberOfClasses; o++) {
					NNNode.connect(nodeInputs[i], nodeInners[o]);
				}
			}
		} else {
			// connect input to first hidden layer
			for (int i = 0; i < numberOfUsedColumns; i++) {
				for (int o = numberOfClasses; o < numberOfClasses + firstLayerSize; o++) {
					NNNode.connect(nodeInputs[i], nodeInners[o]);
				}
			}
			// connect last hidden layer to output
			for (int i = nodeInners.length - lastLayerSize; i < nodeInners.length; i++) {
				for (int o = 0; o < numberOfClasses; o++) {
					NNNode.connect(nodeInners[i], nodeInners[o]);
				}
			}
		}
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(); 
		int lastLayerIndex = -99;
		boolean first = true;
		for (NodeInner nodeInner : nodeInners) {	
			// skip outputs here and add them later

			// layer name
			int layerIndex = nodeInner.getLayerIndex();
			if (layerIndex != NNNode.OUTPUT) {
				if ((lastLayerIndex == -99) || (lastLayerIndex != layerIndex)) {
					if (!first)
						result.append(Tools.getLineSeparators(2));
					first = false;

					String layerName = "Hidden " + (layerIndex + 1);

					result.append(layerName + Tools.getLineSeparator());
					for (int t = 0; t < layerName.length(); t++)
						result.append("=");
					lastLayerIndex = layerIndex;
					result.append(Tools.getLineSeparator());
				}

				// node name and type
				String nodeName = nodeInner.getNodeName() + " (" + nodeInner.getFunction().getTypeName() + ")";
				result.append(Tools.getLineSeparator() + nodeName + Tools.getLineSeparator());
				for (int t = 0; t < nodeName.length(); t++)
					result.append("-");
				result.append(Tools.getLineSeparator());

				// input weights
				double[] weights = nodeInner.getWeights();
				NNNode[] inputNodes = nodeInner.getInputNodes();			
				for (int i = 0; i < inputNodes.length; i++) {
					result.append(inputNodes[i].getNodeName() + ": " + (weights[i + 1]) + Tools.getLineSeparator());
				}

				// threshold weight
				result.append("Threshold: " + (weights[0]) + Tools.getLineSeparator());
			} 
		}
		
		// add output nodes
		first = true;
		for (NodeInner nodeInner : nodeInners) {				
			// layer name
			int layerIndex = nodeInner.getLayerIndex();
			if (layerIndex == NNNode.OUTPUT) {
				if (first) {
					result.append(Tools.getLineSeparators(2));
					String layerName = "Output";
					result.append(layerName + Tools.getLineSeparator());
					for (int t = 0; t < layerName.length(); t++)
						result.append("=");
					lastLayerIndex = layerIndex;
					result.append(Tools.getLineSeparator());
					first = false;
				}

				// node name and type
				String nodeName = nodeInner.getNodeName() + " (" + nodeInner.getFunction().getTypeName() + ")";
				result.append(Tools.getLineSeparator() + nodeName + Tools.getLineSeparator());
				for (int t = 0; t < nodeName.length(); t++)
					result.append("-");
				result.append(Tools.getLineSeparator());

				// input weights
				double[] weights = nodeInner.getWeights();
				NNNode[] inputNodes = nodeInner.getInputNodes();			
				for (int i = 0; i < inputNodes.length; i++) {
					result.append(inputNodes[i].getNodeName() + ": " + (weights[i + 1]) + Tools.getLineSeparator());
				}

				// threshold weight
				result.append("Threshold: " + (weights[0]) + Tools.getLineSeparator());
			}  
		}
		if (getLabel().isNumerical())
		{
			result.append(Tools.getLineSeparator() + "R2: " + (RSquare) + Tools.getLineSeparator());
		}
		else if (getLabel().isNominal())// && getLabel().getMapping().size() == 2)
		{
			result.append(Tools.getLineSeparator() + "null Deviance: " + (nullDeviance) + "; deviance: " +  deviance  + Tools.getLineSeparator());
		}
		return result.toString();
	}
	
	public StringBuffer getAllWeightChange(int outputCount)
	{
		StringBuffer output = new StringBuffer();
		output.append("alpine_miner_nn_ca_change(").append(getWeightArray()).append(",")
			.append(getColumnNames( false)).append(",").append(getInputRangeArray()).append(",")
			.append(getInputBaseArray())
			.append(",").append(getHiddenNodeNumber()).append(",")
			.append(hiddenLayerNames.length).append(",");
		if (getLabel().isNumerical())
		{
			output.append(nodeOutputs[0].getLabelRange()).append("*1.0").append(",").append(nodeOutputs[0].getLabelBase()).append("*1.0");
		}else{
			output.append("0.0").append(",").append("0.0");
		}
			
			output
			.append(",").append(outputCount).append(",");
		if (normalize)
		{
			output.append( getTrue());
		}
		else
		{
			output.append(getFalse());
		}
		output.append(",");
		if (getLabel().isNumerical())
		{
			output.append(getTrue()).append(",").append(StringHandler.doubleQ(getLabel().getName()));
		}
		else
		{
			StringBuffer labelToNum = new StringBuffer("(case ");
			for(int i = 0; i < getLabel().getMapping().size(); i++){
				String value = StringHandler.escQ(getLabel().getMapping().mapIndex(i));
				labelToNum.append(" when ").append(StringHandler.doubleQ(getLabel().getName())).append("='").append(value).append("' then ").append(i);
			}
			labelToNum.append(" end)");
			output.append(getFalse()).append(",").append(labelToNum);
		}
		output.append(",").append(getTrainingHeader().size()).append(")");
//		output.append(",").append("1").append(")");
		return output;

	}
	
	public StringBuffer getAllOutput(int outputCount,boolean prediction)
	{
		StringBuffer output = new StringBuffer();
		output.append("(alpine_miner_nn_ca_o(").append(getWeightArray()).append(",")
			.append(getColumnNames( prediction)).append(",").append(getInputRangeArray()).append(",")
			.append(getInputBaseArray())
			.append(",").append(getHiddenNodeNumber()).append(",")
			.append(hiddenLayerNames.length).append(",");
		if (getLabel().isNumerical())
		{
			output.append(nodeOutputs[0].getLabelRange()).append("*1.0").append(",").append(nodeOutputs[0].getLabelBase()).append("*1.0");
		}else{
			output.append("0.0").append(",").append("0.0");
		}
			
			output
			.append(",").append(outputCount).append(",");
		if (normalize)
		{
			output.append( getTrue());
		}
		else
		{
			output.append(getFalse());
		}
		output.append(",");
		if (getLabel().isNumerical())
		{
			output.append( getTrue());
		}
		else
		{
			output.append(getFalse());
		}
		output.append("))");
		return output;

	}
	public String getTrue()
	{
		return "true";
	}
	public String getFalse()
	{
		return "false";
	}
	public StringBuffer getOutput(int outputCount,int outputNodeNo,boolean prediction)
	{
		StringBuffer output = getAllOutput(outputCount,prediction);
		output.append("[").append(outputNodeNo).append("]");

		return output;

	}
	public String getHiddenNodeNumber(){
		StringBuffer result = new StringBuffer(multiDBUtility.intArrayHead()); 
		for ( int i = 0; i < hiddenLayerSizes.length; i++)
		{
			if ( i != 0 )
			{
				result.append(",");
			}
			result.append(hiddenLayerSizes[i]);
		}
		result.append(multiDBUtility.intArrayTail());
		return result.toString();
	}
	public String getColumnNames(boolean prediction){
		StringBuffer result = new StringBuffer(multiDBUtility.floatArrayHead());
		for (int i = 0; i < nodeInputs.length; i++)
		{
			if ( i != 0 )
			{
				result.append(",");
			}
			if(prediction)
			{
				result.append(nodeInputs[i].getTransformValue());
			}
			else
			{
				result.append(StringHandler.doubleQ(nodeInputs[i].getColumn().getName()));
			}
		}
		result.append(multiDBUtility.floatArrayTail());
//		result.append("]");
		return result.toString();

	}
	public String getInputRangeArray(){
		StringBuffer result = new StringBuffer(multiDBUtility.floatArrayHead());
		for (int i = 0; i < nodeInputs.length; i++)
		{
			if ( i != 0 )
			{
				result.append(",");
			}
			result.append(nodeInputs[i].getColumnRange());
		}
		result.append(multiDBUtility.floatArrayTail());
		return result.toString();
	}
	public String getInputBaseArray(){
		StringBuffer result = new StringBuffer(multiDBUtility.floatArrayHead());
		for (int i = 0; i < nodeInputs.length; i++)
		{
			if ( i != 0 )
			{
				result.append(",");
			}
			result.append(nodeInputs[i].getColumnBase());
		}
		result.append(multiDBUtility.floatArrayTail());
		return result.toString();
	}
	public void updateWeight(Double[] currentChanges, double learningRate, double momentum) {
		int lastLayerIndex = -99;
		int weightIndex = 0;
		for (NodeInner nodeInner : nodeInners) {	
			// skip outputs here and add them later

			// layer name
			int layerIndex = nodeInner.getLayerIndex();
			if (layerIndex != NNNode.OUTPUT) {
				if ((lastLayerIndex == -99) || (lastLayerIndex != layerIndex)) {
					lastLayerIndex = layerIndex;
				}

				// input weights
				double[] weights = nodeInner.getWeights();
				double[] weightChanges = nodeInner.getWeightChanges();
				NNNode[] inputNodes = nodeInner.getInputNodes();
				for (int i = 0; i <= inputNodes.length; i++)
				{
					double change = currentChanges[weightIndex] * learningRate + momentum * weightChanges[i];
					weights[i] += change;
					weightChanges[i] = change;
					weightIndex++;
				}
			} 
		}

		// add output nodes
		for (NodeInner nodeInner : nodeInners) {				
			// layer name
			int layerIndex = nodeInner.getLayerIndex();
			if (layerIndex == NNNode.OUTPUT) {
				// input weights
				double[] weights = nodeInner.getWeights();
				double[] weightChanges = nodeInner.getWeightChanges();
				NNNode[] inputNodes = nodeInner.getInputNodes();			
				for (int i = 0; i <= inputNodes.length; i++)
				{
					double change = currentChanges[weightIndex] * learningRate + momentum * weightChanges[i];
					weights[i] += change;
					weightChanges[i] = change;
					weightIndex++;
				}
			}  
		}
	}
	
	public String getWeightArray() {
		StringBuffer result = new StringBuffer(multiDBUtility.floatArrayHead()); 
		int lastLayerIndex = -99;
		boolean first = true;
		for (NodeInner nodeInner : nodeInners) {	
			// skip outputs here and add them later

			// layer name
			int layerIndex = nodeInner.getLayerIndex();
			if (layerIndex != NNNode.OUTPUT) {
				if ((lastLayerIndex == -99) || (lastLayerIndex != layerIndex)) {
					lastLayerIndex = layerIndex;
				}

				// input weights
				double[] weights = nodeInner.getWeights();
				NNNode[] inputNodes = nodeInner.getInputNodes();
				for (int i = 0; i <= inputNodes.length; i++)
				{
					if (!first)
					{
						result.append(",");
					}
					else
					{
						first = false;
					}
					result.append(weights[i]);
				}
			} 
		}
		
		// add output nodes
		first = true;
		for (NodeInner nodeInner : nodeInners) {				
			// layer name
			int layerIndex = nodeInner.getLayerIndex();
			if (layerIndex == NNNode.OUTPUT) {
				// input weights
				double[] weights = nodeInner.getWeights();
				NNNode[] inputNodes = nodeInner.getInputNodes();			
				for (int i = 0; i <= inputNodes.length; i++)
				{
					result.append(",");
					result.append(weights[i]);
				}
			}  
		}
		result.append(multiDBUtility.floatArrayTail());
		return result.toString();
	}
}
