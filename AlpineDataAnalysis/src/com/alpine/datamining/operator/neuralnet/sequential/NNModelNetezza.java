/**
 * ClassName NNModelNetezza.java
 *
 * Version information: 1.00
 *
 * Data: 2012-1-4
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.neuralnet.sequential;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.ColumnStats;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.AlpineRandom;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import com.alpine.datamining.utility.Tools;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * The model of the  neural net of Netezza.
 * @author Eason
 * 
 */
public class NNModelNetezza extends NNModel {
    private static Logger itsLogger= Logger.getLogger(NNModelNetezza.class);

    private static final long serialVersionUID = 824176868378720876L;
	private String columnTableName;
	private String weightsTableName;
	private String inputRangeTableName;
	private String inputBaseTableName;
	private String hiddenNodeNumberTableName;
	private String dependentColumnMappingTableName;
	private String resultTableName;

	public NNModelNetezza(DataSet trainingDataSet,DataSet oldDataSet,List<String> columnNamesList) {
		super( trainingDataSet, oldDataSet, columnNamesList);
	}
	
	protected void adjustPerWholeData(DataSet dataSet,
			List<String[]> hiddenLayers, int maxCycles, double maxError,
			double learningRate, double momentum, boolean decay,
			boolean normalize, AlpineRandom randomGenerator, int fetchSize, boolean adjustPerRow,
			Column label, int numberOfClasses,
			DatabaseConnection databaseConnection, String tableName)
			throws OperatorException {
		double totalSize = dataSet.size();


		double error = 0;
		Statement st = null;
		try {
			st = databaseConnection.getConnection().createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}
		initTable(st);
		// optimization loop
		for (int cycle = 0; cycle < maxCycles; cycle++) {
			double tempRate = learningRate;
			if (decay) {
				tempRate /= (cycle + 1);
			}

			try{
				String labelName = StringHandler.doubleQ(getLabel().getName());
				
				StringBuffer labelToNum = new StringBuffer();
				if (getLabel().isNumerical()){
					labelToNum = new StringBuffer(labelName);
				}else{
					labelToNum.append("(case ");
					for(int i = 0; i < getLabel().getMapping().size(); i++){
						String value = StringHandler.escQ(getLabel().getMapping().mapIndex(i));
						labelToNum.append(" when ").append(StringHandler.doubleQ(getLabel().getName())).append("='").append(value).append("' then ").append(i);
					}
					labelToNum.append(" end)");
				}
				StringBuffer where = getWhere().append(" and ").append(labelName).append(" is not null ");
				TableTransferParameter.truncateTable(weightsTableName, st);
				TableTransferParameter.truncateTable(columnTableName, st);
				TableTransferParameter.truncateTable(inputRangeTableName, st);
				TableTransferParameter.truncateTable(inputBaseTableName, st);
				TableTransferParameter.truncateTable(hiddenNodeNumberTableName, st);
				TableTransferParameter.truncateTable(resultTableName, st);

				TableTransferParameter.insertTable(weightsTableName, st, getWeightSqlArrayNZ());
				TableTransferParameter.insertTable(columnTableName, st, getColumnsArrayNZ(dataSet));
				TableTransferParameter.insertTable(inputRangeTableName, st, getInputRangeArrayNZ());
				TableTransferParameter.insertTable(inputBaseTableName, st, getInputBaseArrayNZ());
				TableTransferParameter.insertTable(hiddenNodeNumberTableName, st, getHiddenNodeNumberArrayNZ());

				int hiddenLayerNumberArg = hiddenLayerSizes.length;
				
				double outputRangeArg = 0.0;
				if (getLabel().isNumerical()){
					outputRangeArg = nodeOutputs[0].getLabelRange();
				}
				double outputBaseArg = 0.0;
				if (getLabel().isNumerical()){
					outputBaseArg = nodeOutputs[0].getLabelBase();
				}
				int outputNodeNoArg = nodeOutputs.length;
				int normalizeArg = 0;
				if (normalize){
					normalizeArg = 1;
				}
				int numericalLabelArg = 0;
				if (label.isNumerical()){
					numericalLabelArg = 1;
				}

				StringBuffer sql = new StringBuffer("call alpine_miner_nn_ca_change_proc('");
				sql.append(tableName).append("','") 
				.append(where).append("','") 
				.append(StringHandler.escQ(labelToNum.toString())).append("','") 
				.append(weightsTableName).append("','") 
				.append(columnTableName).append("','")
				.append(inputRangeTableName).append("','")
				.append(inputBaseTableName).append("','")
				.append(hiddenNodeNumberTableName).append("',")
				.append(hiddenLayerNumberArg).append(",")
				.append(outputRangeArg).append(",")
				.append(outputBaseArg).append(",")
				.append(outputNodeNoArg).append(",")
				.append(normalizeArg).append(",")
				.append(numericalLabelArg).append(",")
				.append(totalSize).append(",'")
				.append(resultTableName).append("')");
				
				itsLogger.debug("NNModelNetezza.adjustPerWholeData():sql="+sql);
				st.execute(sql.toString());

				Double []  currentChanges = TableTransferParameter.getDoubleResult(resultTableName, st);

				for(int i = 0; i < currentChanges.length; i++){
					if (Double.isNaN(currentChanges[i]) || Double.isInfinite(currentChanges[i])){
						try{
							databaseConnection.getConnection().setAutoCommit(true);
						}catch(SQLException e)
						{
							e.printStackTrace();
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
			catch(SQLException e)
			{
				e.printStackTrace();
				copyBestErrorWeightsToWeights();
				return;
			}
			itsLogger.debug("cycle:"+cycle+";error:"+error);
			if (error < maxError) {
				itsLogger.debug("loop break : "+cycle+" error: "+ error);
				break;
			}

			if (Double.isInfinite(error) || Double.isNaN(error)) {
				if (Tools.isLessEqual(learningRate, 0.0d)) // should hardly happen
					throw new RuntimeException("learning rate is too small.");
				learningRate /= 2;
				train(dataSet, hiddenLayers, maxCycles, maxError, learningRate, momentum, decay,  normalize, randomGenerator, fetchSize, adjustPerRow);
			}
		}
		copyBestErrorWeightsToWeights();
		dropProcTable(st);
		try {
			databaseConnection.getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
		}
	}

	private Double[] getWeightSqlArrayNZ() throws SQLException {
		ArrayList<Double> weightArray = new ArrayList<Double>();
		int lastLayerIndex = -99;
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
				for (int i = 0; i <= inputNodes.length; i++) {
					double temp = weights[i];
					if (Double.isNaN(temp)) {
						temp = 0.0;
					}
					weightArray.add(temp);
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
				NNNode[] inputNodes = nodeInner.getInputNodes();
				for (int i = 0; i <= inputNodes.length; i++) {
					double temp = weights[i];
					if (Double.isNaN(temp)) {
						temp = 0.0;
					}
					weightArray.add(temp);

				}
			}
		}
		//(Double[])
	    return weightArray.toArray(new Double[0]);
	}
	
	private Double[] getInputRangeArrayNZ() throws SQLException{
		ArrayList<Double> array = new ArrayList<Double>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			double temp = nodeInputs[i].getColumnRange();
			if (Double.isNaN(temp)){
				temp  = 0.0;
			}
			array.add((temp));
		}

	    return array.toArray(new Double[0]);

	}
	private Double[] getInputBaseArrayNZ() throws SQLException{
		ArrayList<Double> array = new ArrayList<Double>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			double temp = nodeInputs[i].getColumnBase();
			if (Double.isNaN(temp)){
				temp  = 0.0;
			}
			array.add((temp));
		}
		return array.toArray(new Double[0]);
		
	}

	private Integer[] getHiddenNodeNumberArrayNZ() throws SQLException {
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int i = 0; i < hiddenLayerSizes.length; i++) {
			array.add(hiddenLayerSizes[i]);
		}
		return array.toArray(new Integer[0]);
	}
	private String[] getdependentColumnMappingArgArrayNZ() throws SQLException {
		ArrayList<String> array = new ArrayList<String>();
		for(int i = 0; i < getLabel().getMapping().size(); i++){
			String value = (getLabel().getMapping().mapIndex(i));
			array.add(value);
		}
		return array.toArray(new String[0]);
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
		initTable(st);
		Column label = dataSet.getColumns().getLabel();
		if (label.isNumerical())
		{
			cacluateRSquare(dataSet, tableName,st, label);
		}
		else if (label.isNominal())
		{
			caculateDeviance(dataSet, tableName,st ,label);
		}
		dropProcTable(st);

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

	protected void cacluateRSquare(DataSet dataSet,  String tableName, Statement st, Column label) throws OperatorException {
		String labelName=StringHandler.doubleQ(label.getName());
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg((").append(labelName).append("::double)) from ").append(tableName);
		double avg = 0.0;
		try {
			itsLogger.debug("NNModelNetezza.cacluateRSquare():sql="+avgSQL);
			ResultSet rs = st.executeQuery(avgSQL.toString());
			if (rs.next())
			{
				avg = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		try {
		
			StringBuffer where = getWhere().append(" and ").append(labelName).append(" is not null ");

			TableTransferParameter.truncateTable(weightsTableName, st);
			TableTransferParameter.truncateTable(columnTableName, st);
			TableTransferParameter.truncateTable(inputRangeTableName, st);
			TableTransferParameter.truncateTable(inputBaseTableName, st);
			TableTransferParameter.truncateTable(hiddenNodeNumberTableName, st);
			TableTransferParameter.truncateTable(resultTableName, st);

			TableTransferParameter.insertTable(weightsTableName, st, getWeightSqlArrayNZ());
			TableTransferParameter.insertTable(columnTableName, st, getColumnsArrayNZ(dataSet));
			TableTransferParameter.insertTable(inputRangeTableName, st, getInputRangeArrayNZ());
			TableTransferParameter.insertTable(inputBaseTableName, st, getInputBaseArrayNZ());
			TableTransferParameter.insertTable(hiddenNodeNumberTableName, st, getHiddenNodeNumberArrayNZ());

			int hiddenLayerNumberArg = hiddenLayerSizes.length;
			
			double outputRangeArg = 0.0;
			if (getLabel().isNumerical()){
				outputRangeArg = nodeOutputs[0].getLabelRange();
			}
			double outputBaseArg = 0.0;
			if (getLabel().isNumerical()){
				outputBaseArg = nodeOutputs[0].getLabelBase();
			}
			int outputNodeNoArg = nodeOutputs.length;
			int normalizeArg = 0;
			if (normalize){
				normalizeArg = 1;
			}
			int numericalLabelArg = 0;
			if (label.isNumerical()){
				numericalLabelArg = 1;
			}
			StringBuffer sql = new StringBuffer("call alpine_miner_nn_ca_r_square_proc('");
			sql.append(tableName).append("','") 
			.append(where).append("','") 
			.append(labelName).append("','") 
			.append(weightsTableName).append("','") 
			.append(columnTableName).append("','")
			.append(inputRangeTableName).append("','")
			.append(inputBaseTableName).append("','")
			.append(hiddenNodeNumberTableName).append("',")
			.append(hiddenLayerNumberArg).append(",")
			.append(outputRangeArg).append(",")
			.append(outputBaseArg).append(",")
			.append(outputNodeNoArg).append(",")
			.append(normalizeArg).append(",")
			.append(numericalLabelArg).append(",")
			.append(avg).append(",'")
			.append(resultTableName)
			.append("')");

			itsLogger.debug("NNModelNetezza.cacluateRSquare():sql="+sql);
			st.execute(sql.toString());
			double result [] = TableTransferParameter.getResult(resultTableName, st);
			if(result.length >=1){
				RSquare = result[0];
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	private void caculateDeviance(DataSet dataSet, String tableName,
			Statement st, Column label)
			throws OperatorException {
		int numberOfClasses = getNumberOfClasses(label);
		try {
			TableTransferParameter.createStringTable(dependentColumnMappingTableName,st);

			TableTransferParameter.truncateTable(weightsTableName, st);
			TableTransferParameter.truncateTable(columnTableName, st);
			TableTransferParameter.truncateTable(inputRangeTableName, st);
			TableTransferParameter.truncateTable(inputBaseTableName, st);
			TableTransferParameter.truncateTable(hiddenNodeNumberTableName, st);
			TableTransferParameter.truncateTable(resultTableName, st);

			TableTransferParameter.insertTable(weightsTableName, st, getWeightSqlArrayNZ());
			TableTransferParameter.insertTable(columnTableName, st, getColumnsArrayNZ(dataSet));
			TableTransferParameter.insertTable(inputRangeTableName, st, getInputRangeArrayNZ());
			TableTransferParameter.insertTable(inputBaseTableName, st, getInputBaseArrayNZ());
			TableTransferParameter.insertTable(hiddenNodeNumberTableName, st, getHiddenNodeNumberArrayNZ());
			TableTransferParameter.insertTable(dependentColumnMappingTableName,st,getdependentColumnMappingArgArrayNZ());
			
			String labelName = StringHandler.doubleQ(getLabel().getName());
		
			StringBuffer labelToNum = new StringBuffer("(case ");
			for (int i = 0; i < getLabel().getMapping().size(); i++) {
				String value = StringHandler.escQ(getLabel().getMapping().mapIndex(
						i));
				labelToNum.append(" when ").append(
						StringHandler.doubleQ(getLabel().getName())).append("='")
						.append(value).append("' then ").append(i);
			}
			labelToNum.append(" end)");
			StringBuffer where = getWhere().append(" and ").append(labelName)
					.append(" is not null ");
			int hiddenLayerNumberArg = hiddenLayerSizes.length;
		
			double outputRangeArg = 0.0;
			if (getLabel().isNumerical()) {
				outputRangeArg = nodeOutputs[0].getLabelRange();
			}
			double outputBaseArg = 0.0;
			if (getLabel().isNumerical()) {
				outputBaseArg = nodeOutputs[0].getLabelBase();
			}
			int outputNodeNoArg = nodeOutputs.length;
			int normalizeArg = 0;
			if (normalize) {
				normalizeArg = 1;
			}
			int numericalLabelArg = 0;
			if (label.isNumerical()) {
				numericalLabelArg = 1;
			}

			StringBuffer sql = new StringBuffer(
			"call alpine_miner_nn_ca_deviance_proc('");
			sql.append(tableName).append("','") 
			.append(where).append("','") 
			.append(labelName).append("','") 
			.append(weightsTableName).append("','") 
			.append(columnTableName).append("','")
			.append(inputRangeTableName).append("','")
			.append(inputBaseTableName).append("','")
			.append(hiddenNodeNumberTableName).append("',")
			.append(hiddenLayerNumberArg).append(",")
			.append(outputRangeArg).append(",")
			.append(outputBaseArg).append(",")
			.append(outputNodeNoArg).append(",")
			.append(normalizeArg).append(",")
			.append(numericalLabelArg).append(",'")
			.append(dependentColumnMappingTableName).append("','")
			.append(resultTableName)
			.append("')");
			itsLogger.debug("NNModelNetezza.caculateDeviance():sql="+sql);
			st.execute(sql.toString());
			double result [] = TableTransferParameter.getResult(resultTableName, st);
			if(result.length >=1){
				deviance = result[0];
			}
		
			TableTransferParameter.dropResultTable(dependentColumnMappingTableName, st);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		StringBuffer countSQL = new StringBuffer();
		countSQL.append(" select ")
		.append(" count(*) from ").append(tableName).append(getWhere()).append(" group by ").append(
				StringHandler.doubleQ(label.getName()));

		double totalCount = 0.0;
		ArrayList<Double> countArray = new ArrayList<Double>();
		try {
			

			itsLogger.debug("NNModelNetezza.caculateDeviance():sql="+countSQL);
			ResultSet rs = st.executeQuery(countSQL.toString());
			while (rs.next())
			{
				double count = rs.getDouble(1);
				countArray.add(count);
				totalCount+= count;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		if (totalCount != 0)
		{
			nullDeviance = 0;
			for(int i = 0; i < numberOfClasses; i++)
			{
				nullDeviance += countArray.get(i) * Math.log(countArray.get(i)/totalCount);
			}
			nullDeviance = -2*nullDeviance;
		}

	}

	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException {
		resetNetwork();
		String newTableName = ((DBTable) dataSet.getDBTable()).getTableName();
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();

		Statement st = null;
		StringBuffer sql = new StringBuffer();
		String newTempTableName = "NT"+System.currentTimeMillis();

		try {
			st = databaseConnection.createStatement(false);
			initPredictTable(st);

			TableTransferParameter.insertTable(weightsTableName, st, getWeightSqlArrayNZ());
			TableTransferParameter.insertTable(columnTableName, st, getColumnNamesPredictArray());
			TableTransferParameter.insertTable(inputRangeTableName, st, getInputRangeArrayNZ());
			TableTransferParameter.insertTable(inputBaseTableName, st, getInputBaseArrayNZ());
			TableTransferParameter.insertTable(hiddenNodeNumberTableName, st, getHiddenNodeNumberArrayNZ());
			sql.append("create table ").append(newTempTableName).append(
					" as select *, row_number() over(order by 1) as  "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" from ").append(newTableName);
			itsLogger.debug(
					"NNModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			itsLogger.debug(
					"NNModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			if(getLabel().isNumerical()){
				sql.append("create  table ").append(resultTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint, prediction0 double)");
			}else{
				int n = getLabel().getMapping().size();
				sql.append("create  table ").append(resultTableName).append(
				" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint");
				for(int i = 0; i < n; i++){
					sql.append(", prediction").append(i).append(" double");
				}
				sql.append(")");
			}
			itsLogger.debug(
					"NNModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer where = getWherePredict();

			int hiddenLayerNumberArg = hiddenLayerSizes.length;
			
			double outputRangeArg = 0.0;
			if (getLabel().isNumerical()){
				outputRangeArg = nodeOutputs[0].getLabelRange();
			}
			double outputBaseArg = 0.0;
			if (getLabel().isNumerical()){
				outputBaseArg = nodeOutputs[0].getLabelBase();
			}
			int outputNodeNoArg = nodeOutputs.length;
			int normalizeArg = 0;
			if (normalize){
				normalizeArg = 1;
			}
			int numericalLabelArg = 0;
			if (getLabel().isNumerical()){
				numericalLabelArg = 1;
			}

		sql = new StringBuffer();

		sql.append("call  alpine_miner_nn_ca_predict_proc('");
		
		sql.append(newTempTableName).append("','") 
		.append(where).append("','") 
		.append(weightsTableName).append("','") 
		.append(columnTableName).append("','")
		.append(inputRangeTableName).append("','")
		.append(inputBaseTableName).append("','")
		.append(hiddenNodeNumberTableName).append("',")
		.append(hiddenLayerNumberArg).append(",")
		.append(outputRangeArg).append(",")
		.append(outputBaseArg).append(",")
		.append(outputNodeNoArg).append(",")
		.append(normalizeArg).append(",")
		.append(numericalLabelArg).append(",'")
		.append(resultTableName).append("')");

			itsLogger.debug(
					"NNModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer sqltemp = new StringBuffer();
			sqltemp.append("update ").append(newTempTableName).append(" set  ");

			if(getLabel().isNumerical()){
				updateColumns.add(predictedLabel.getName());
				sqltemp.append(StringHandler.doubleQ(predictedLabel.getName())).append(" = ");
				sqltemp.append(resultTableName).append(".prediction0");
			}else{
				int numberOfClasses = getLabel().getMapping().size();
				updateColumns.add(predictedLabel.getName());
				for ( int c = 0; c < numberOfClasses; c++)
				{
					if(c != 0){
						sqltemp.append(",");
					}
					sqltemp.append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName()));
					sqltemp.append(" = ");
					sqltemp.append(resultTableName).append(".prediction").append(c);
					updateColumns.add(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName());
				}
				sqltemp.append(",").append(StringHandler.doubleQ(predictedLabel.getName()));
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
							biggerSql[c].append(resultTableName).append(".prediction").append(c).append(">=").append(resultTableName).append(".prediction").append(j);
					}
					biggerSql[c].append(")");
				}
				StringBuffer caseSql = new StringBuffer();
				caseSql.append(" (case ");
				for (int c = 0; c < numberOfClasses - 1; c++) {
					caseSql.append(" when ").append(biggerSql[c]).append(" then '").append(StringHandler.escQ(getLabel().getMapping().mapIndex(c))).append("'");
				}
				caseSql.append(" else '").append(StringHandler.escQ(getLabel().getMapping().mapIndex(numberOfClasses - 1))).append("' end)");

				sqltemp.append(" = ").append(caseSql);
			}
			sqltemp.append(
					" from ").append(resultTableName).append(
					" where  ").append(resultTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTempTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");

			itsLogger.debug(
							"NNModelNetezza.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTableName);
			itsLogger.debug(
					"NNModelNetezza.performPrediction():sql=" + sql);
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
					"NNModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTempTableName);
			itsLogger.debug(
					"NNModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			

			dropProcTable( st);
			
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}finally{
			try {
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		return dataSet;
	}

	private String[] getColumnsArrayNZ(DataSet newDataSet){
		String[] ret = new String[0];
		Columns attsNew = newDataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		ArrayList<String> columnsArray = new ArrayList<String>();
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			columnsArray.add(columnName);
		}
		return columnsArray.toArray(ret);
	}
	public void initPredictTable(Statement st) throws OperatorException{
		long currentTime = System.currentTimeMillis();
		columnTableName = "C" + currentTime;
		weightsTableName = "W" + currentTime;
		inputRangeTableName = "IR" + currentTime;
		inputBaseTableName = "IB" + currentTime;
		hiddenNodeNumberTableName = "H" + currentTime;
		resultTableName = "R" + currentTime;
		dependentColumnMappingTableName = "D" + currentTime;
		
		TableTransferParameter.createStringTable(columnTableName,st);
		TableTransferParameter.createDoubleTable(weightsTableName,st);
		TableTransferParameter.createDoubleTable(inputRangeTableName,st);
		TableTransferParameter.createDoubleTable(inputBaseTableName,st);
		TableTransferParameter.createDoubleTable(hiddenNodeNumberTableName,st);
	}

	public void initTable(Statement st) throws OperatorException{
		long currentTime = System.currentTimeMillis();
		columnTableName = "C" + currentTime;
		weightsTableName = "W" + currentTime;
		inputRangeTableName = "IR" + currentTime;
		inputBaseTableName = "IB" + currentTime;
		hiddenNodeNumberTableName = "H" + currentTime;
		resultTableName = "R" + currentTime;
		dependentColumnMappingTableName = "D" + currentTime;
		
		TableTransferParameter.createStringTable(columnTableName,st);
		TableTransferParameter.createDoubleTable(weightsTableName,st);
		TableTransferParameter.createDoubleTable(inputRangeTableName,st);
		TableTransferParameter.createDoubleTable(inputBaseTableName,st);
		TableTransferParameter.createDoubleTable(hiddenNodeNumberTableName,st);
		TableTransferParameter.createDoubleTable(resultTableName,st);

	}
	public void dropProcTable(Statement st) throws OperatorException {
		TableTransferParameter.dropResultTable(columnTableName,st);
		TableTransferParameter.dropResultTable(weightsTableName,st);
		TableTransferParameter.dropResultTable(inputRangeTableName,st);
		TableTransferParameter.dropResultTable(inputBaseTableName,st);
		TableTransferParameter.dropResultTable(hiddenNodeNumberTableName,st);
		TableTransferParameter.dropResultTable(resultTableName,st);
	}
	
	private String[] getColumnNamesPredictArray() throws SQLException{
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			array.add(StringHandler.escQ(nodeInputs[i].getTransformValue().toString()));
		}
	    return array.toArray(new String[0]);
	}	

	public String[] generatePredictColumnWhere(DataSet dataSet){
		String [] whereArray = new String[dataSet.getColumns().size()];
		Columns attsNew = dataSet.getColumns();
		Iterator<Column> attsIter=attsNew.iterator();
		int i = 0;
		while(attsIter.hasNext())
		{
			Column att=attsIter.next();
			String columnName=StringHandler.doubleQ(att.getName());
			whereArray[i] = columnName;
			i++;
		}
		return whereArray;
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
					Iterator<Entry<String, String>> iter = TransformMap_columnKey.entrySet().iterator(); 
					while (iter.hasNext()) { 
					    Map.Entry<String, String> entry = iter.next(); 
					    String key = entry.getKey(); 
					    if(key.trim().equals(column.getName())){
							nodeInputs[a] = new NodeInput(column.getName(),TransformMap_columnKey);	
					    }
					} 
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

}
