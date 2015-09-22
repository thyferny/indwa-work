/**
 * ClassName NNModellOracle
 *
 * Version information: 1.00
 *
 * Data: 2010-4-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.neuralnet.sequential;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.AlpineRandom;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * The model of the  neural net of DB2.
 * @author Eason
 * 
 */
public class NNModellDB2 extends NNModel {
    private static Logger itsLogger= Logger.getLogger(NNModellDB2.class);

    private static final long serialVersionUID = 824176868378720876L;
	public NNModellDB2(DataSet trainingDataSet,DataSet oldDataSet,List<String> columnNamesList) {
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
		// optimization loop
		for (int cycle = 0; cycle < maxCycles; cycle++) {
			double tempRate = learningRate;
			if (decay) {
				tempRate /= (cycle + 1);
			}

			try{
				StringBuffer sql = new StringBuffer("call alpine_miner_nn_ca_change_proc(");
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
				sql.append("?").append(",") 
				.append("?").append(",") 
				.append("?").append(",") 
				.append("?").append(",") 
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(",")
				.append("?").append(")");
				CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); /* con is the connection */
				Array weightArg = getWeightSqlArray(databaseConnection);
				Array columnsArg = getColumnNamesSqlArray(databaseConnection,false);
				Array inputRangeArg = getInputRangeSqlArray(databaseConnection);
				Array inputBaseArg = getInputBaseSqlArray(databaseConnection);
				Array hiddenNodeNumberArg = getHiddenNodeNumberSqlArray(databaseConnection);
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
				stpCall.setString(1, tableName);
				stpCall.setString(2, labelToNum.toString());
				stpCall.setString(3, where.toString());
			    stpCall.setArray(4, weightArg);
			    stpCall.setArray(5, columnsArg);
			    stpCall.setArray(6, inputRangeArg);
			    stpCall.setArray(7, inputBaseArg);
			    stpCall.setArray(8, hiddenNodeNumberArg);
			    stpCall.setInt(9, hiddenLayerNumberArg);
			    stpCall.setDouble(10, outputRangeArg);
			    stpCall.setDouble(11, outputBaseArg);
			    stpCall.setInt(12, outputNodeNoArg);
			    stpCall.setInt(13, normalizeArg);
			    stpCall.setInt(14, numericalLabelArg);
			    stpCall.setDouble(15, totalSize);
				stpCall.registerOutParameter(16, java.sql.Types.ARRAY);
				itsLogger.debug("NNModelDB2.adjustPerWholeData():sql="+sql);
				stpCall.execute();
				Double []  currentChanges = (Double [] )stpCall.getArray(16).getArray();
				stpCall.close();
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
			itsLogger.debug("cycle"+cycle+";error:"+error);
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
		try {
			databaseConnection.getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
		}
	}

	private Array getWeightSqlArray(DatabaseConnection databaseConnection) throws SQLException {
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
	    return databaseConnection.getConnection().createArrayOf("DOUBLE", weightArray.toArray(new Double[0]));
	}
	private Array getColumnNamesSqlArray(DatabaseConnection databaseConnection, boolean prediction) throws SQLException{
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			if(prediction)
			{
				array.add(nodeInputs[i].getTransformValue().toString());
			}
			else
			{
				array.add(StringHandler.doubleQ((nodeInputs[i].getColumn().getName())));
			}
		}
	    return databaseConnection.getConnection().createArrayOf("VARCHAR", array.toArray(new String[0]));
	}
	
	private Array getInputRangeSqlArray(DatabaseConnection databaseConnection) throws SQLException{
		ArrayList<Double> array = new ArrayList<Double>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			double temp = nodeInputs[i].getColumnRange();
			if (Double.isNaN(temp)){
				temp  = 0.0;
			}
			array.add((temp));
		}

	    return databaseConnection.getConnection().createArrayOf("DOUBLE", array.toArray(new Double[0]));

	}
	private Array getInputBaseSqlArray(DatabaseConnection databaseConnection) throws SQLException{
		ArrayList<Double> array = new ArrayList<Double>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			double temp = nodeInputs[i].getColumnBase();
			if (Double.isNaN(temp)){
				temp  = 0.0;
			}
			array.add((temp));
		}
		return databaseConnection.getConnection().createArrayOf("DOUBLE", array.toArray(new Double[0]));
		
	}

	private Array getHiddenNodeNumberSqlArray(DatabaseConnection databaseConnection) throws SQLException {
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int i = 0; i < hiddenLayerSizes.length; i++) {
			array.add(hiddenLayerSizes[i]);
		}
		return databaseConnection.getConnection().createArrayOf("INTEGER", array.toArray(new Integer[0]));
	}
	private Array  getdependentColumnMappingArgSqlArray(DatabaseConnection databaseConnection) throws SQLException {
		ArrayList<String> array = new ArrayList<String>();
		for(int i = 0; i < getLabel().getMapping().size(); i++){
			String value = (getLabel().getMapping().mapIndex(i));
			array.add(value);
		}
		return databaseConnection.getConnection().createArrayOf("VARCHAR", array.toArray(new String[0]));
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
			cacluateRSquare(dataSet, tableName,st, label);

		}
		else if (label.isNominal())
		{
			caculateDeviance(dataSet, tableName,st ,label);
			
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

	protected void cacluateRSquare(DataSet dataSet,  String tableName, Statement st, Column label) throws OperatorException {
		String labelName=StringHandler.doubleQ(label.getName());
		StringBuffer avgSQL = new StringBuffer();
		avgSQL.append(" select avg(double(").append(labelName).append(")) from ").append(tableName);
		double avg = 0.0;
		try {
			itsLogger.debug("NNModel.cacluateRSquare():sql="+avgSQL);
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
			StringBuffer sql = new StringBuffer("call alpine_miner_nn_ca_r_square_proc(");
			
			StringBuffer where = getWhere().append(" and ").append(labelName).append(" is not null ");
			sql.append("?").append(",") 
			.append("?").append(",") 
			.append("?").append(",") 
			.append("?").append(",") 
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(",")
			.append("?").append(")");
			itsLogger.debug("NNModellDB2.cacluateRSquare():sql="+sql);
			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();

			CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); /* con is the connection */
			Array weightArg = getWeightSqlArray(databaseConnection);
			Array columnsArg = getColumnNamesSqlArray(databaseConnection,false);
			Array inputRangeArg = getInputRangeSqlArray(databaseConnection);
			Array inputBaseArg = getInputBaseSqlArray(databaseConnection);
			Array hiddenNodeNumberArg = getHiddenNodeNumberSqlArray(databaseConnection);
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
			stpCall.setString(1, tableName);
			stpCall.setString(2, labelName);
			stpCall.setString(3, where.toString());
		    stpCall.setArray(4, weightArg);
		    stpCall.setArray(5, columnsArg);
		    stpCall.setArray(6, inputRangeArg);
		    stpCall.setArray(7, inputBaseArg);
		    stpCall.setArray(8, hiddenNodeNumberArg);
		    stpCall.setInt(9, hiddenLayerNumberArg);
		    stpCall.setDouble(10, outputRangeArg);
		    stpCall.setDouble(11, outputBaseArg);
		    stpCall.setInt(12, outputNodeNoArg);
		    stpCall.setInt(13, normalizeArg);
		    stpCall.setInt(14, numericalLabelArg);
		    stpCall.setDouble(15, avg);
			stpCall.registerOutParameter(16, java.sql.Types.DOUBLE);
			stpCall.execute();
			RSquare = stpCall.getDouble(16);
			stpCall.close();

		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	private StringBuffer toChar(Column column){
		StringBuffer stringBuffer = new StringBuffer();
		if(column.getValueType() == DataType.DATE ||column.getValueType() == DataType.TIME){
			stringBuffer.append("char");
		}
		if(column.getValueType() == DataType.DATE_TIME){
			stringBuffer.append("alpine_miner_trim_timestamp_string");
		}
		stringBuffer.append("(").append(StringHandler.doubleQ(column.getName())).append(")");
		return stringBuffer;
	}
	private void caculateDeviance(DataSet dataSet, String tableName,
			Statement st, Column label)
			throws OperatorException {
		int numberOfClasses = getNumberOfClasses(label);

		try {
			
			StringBuffer sql = new StringBuffer(
				"call alpine_miner_nn_ca_deviance_proc(");
			String labelName = StringHandler.escQ(toChar(getLabel()).toString());
		
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
			sql.append("?").append(",").append("?").append(",").append("?").append(
					",").append("?").append(",").append("?").append(",")
					.append("?").append(",").append("?").append(",").append("?")
					.append(",").append("?").append(",").append("?").append(",")
					.append("?").append(",").append("?").append(",").append("?")
					.append(",").append("?").append(",").append("?").append(",")
					.append("?").append(")");
			itsLogger.debug(
					"NNModellDB2.caculateDeviance():sql=" + sql);
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			CallableStatement stpCall = databaseConnection.getConnection()
						.prepareCall(sql.toString());
			Array weightArg = getWeightSqlArray(databaseConnection);
			Array columnsArg = getColumnNamesSqlArray(databaseConnection,false);
			Array inputRangeArg = getInputRangeSqlArray(databaseConnection);
			Array inputBaseArg = getInputBaseSqlArray(databaseConnection);
			Array hiddenNodeNumberArg = getHiddenNodeNumberSqlArray(databaseConnection);
			
			Array dependentColumnMappingArg = getdependentColumnMappingArgSqlArray(databaseConnection);
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
			stpCall.setString(1, tableName);
			stpCall.setString(2, labelName);
			stpCall.setString(3, where.toString());
			stpCall.setArray(4, weightArg);
			stpCall.setArray(5, columnsArg);
			stpCall.setArray(6, inputRangeArg);
			stpCall.setArray(7, inputBaseArg);
			stpCall.setArray(8, hiddenNodeNumberArg);
			stpCall.setInt(9, hiddenLayerNumberArg);
			stpCall.setDouble(10, outputRangeArg);
			stpCall.setDouble(11, outputBaseArg);
			stpCall.setInt(12, outputNodeNoArg);
			stpCall.setInt(13, normalizeArg);
			stpCall.setInt(14, numericalLabelArg);
			stpCall.setArray(15, dependentColumnMappingArg);
			stpCall.registerOutParameter(16, java.sql.Types.DOUBLE);
			itsLogger.debug(
					"NNModelDB2.NNModel.caculateDeviance():sql=" + sql);		

			stpCall.execute();
			 deviance =  stpCall.getDouble(16);
			stpCall.close();
			
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
			

			itsLogger.debug("NNModel.caculateDeviance():sql="+countSQL);
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
				nullDeviance += ((Double)countArray.get(i)) * Math.log(((Double)countArray.get(i))/totalCount);
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
		String tempTableName = "T" + System.currentTimeMillis();

		try {
			st = databaseConnection.createStatement(false);
			sql.append("alter table ").append(newTableName).append(
					" add column "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint");
			itsLogger.debug(
					"NNModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			sql.append("update ").append(newTableName).append(
					"  set "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = row_number() over()");
			itsLogger.debug(
					"NNModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			if(getLabel().isNumerical()){
				sql.append("create  table ").append(tempTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint, prediction0 double)");
			}else{
				int n = getLabel().getMapping().size();
				sql.append("create  table ").append(tempTableName).append(
				" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint");
				for(int i = 0; i < n; i++){
					sql.append(", prediction").append(i).append(" double");
				}
				sql.append(")");
			}
			itsLogger.debug(
					"NNModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer where = getWherePredict();

		sql = new StringBuffer();

		sql.append("call  alpine_miner_nn_ca_predict_proc(");
		
		
		sql.append("?").append(",") 
		.append("?").append(",") 
		.append("?").append(",") 
		.append("?").append(",") 
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(",")
		.append("?").append(")");
		CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); /* con is the connection */
		Array weightArg = getWeightSqlArray(databaseConnection);
		Array columnsArg = getColumnNamesSqlArray(databaseConnection, true);
		Array inputRangeArg = getInputRangeSqlArray(databaseConnection);
		Array inputBaseArg = getInputBaseSqlArray(databaseConnection);
		Array hiddenNodeNumberArg = getHiddenNodeNumberSqlArray(databaseConnection);
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
		stpCall.setString(1, newTableName);
		stpCall.setString(2, tempTableName);
		stpCall.setString(3, where.toString());
	    stpCall.setArray(4, weightArg);
	    stpCall.setArray(5, columnsArg);
	    stpCall.setArray(6, inputRangeArg);
	    stpCall.setArray(7, inputBaseArg);
	    stpCall.setArray(8, hiddenNodeNumberArg);
	    stpCall.setInt(9, hiddenLayerNumberArg);
	    stpCall.setDouble(10, outputRangeArg);
	    stpCall.setDouble(11, outputBaseArg);
	    stpCall.setInt(12, outputNodeNoArg);
	    stpCall.setInt(13, normalizeArg);
	    stpCall.setInt(14, numericalLabelArg);

			itsLogger.debug(
					"NNModellDB2.performPrediction():sql=" + sql);

			stpCall.execute();
			stpCall.close();
			StringBuffer sqltemp = new StringBuffer();
			StringBuffer set = new StringBuffer();
			StringBuffer selectSet = new StringBuffer();

			if(getLabel().isNumerical()){
				updateColumns.add(predictedLabel.getName());
				set.append(StringHandler.doubleQ(predictedLabel.getName()));
				selectSet.append(tempTableName).append(".prediction0");
			}else{
				int numberOfClasses = getLabel().getMapping().size();
				updateColumns.add(predictedLabel.getName());
				for ( int c = 0; c < numberOfClasses; c++)
				{
					if(c != 0){
						set.append(",");
						selectSet.append(",");
					}
					set.append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName()));
					selectSet.append(tempTableName).append(".prediction").append(c);
					updateColumns.add(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName());
				}
				set.append(",").append(StringHandler.doubleQ(predictedLabel.getName()));
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
							biggerSql[c].append(tempTableName).append(".prediction").append(c).append(">=").append(tempTableName).append(".prediction").append(j);
					}
					biggerSql[c].append(")");
				}
				StringBuffer caseSql = new StringBuffer();
				caseSql.append(" (case ");
				for (int c = 0; c < numberOfClasses - 1; c++) {
					caseSql.append(" when ").append(biggerSql[c]).append(" then '").append(StringHandler.escQ(getLabel().getMapping().mapIndex(c))).append("'");
				}
				caseSql.append(" else '").append(StringHandler.escQ(getLabel().getMapping().mapIndex(numberOfClasses - 1))).append("' end)");

				selectSet.append(",").append(caseSql);
			}
			sqltemp.append("update ").append(newTableName).append(" set ( ")
			.append(set)
			.append(" ) = (select ").append(selectSet).append(
					" from ").append(tempTableName).append(
					" where  ").append(tempTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+")");

			itsLogger.debug(
							"NNModellDB2.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(tempTableName);
			itsLogger.debug(
					"NNModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("alter table ").append(newTableName).append(
					" drop column "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");
			itsLogger.debug(
					"NNModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}

}
