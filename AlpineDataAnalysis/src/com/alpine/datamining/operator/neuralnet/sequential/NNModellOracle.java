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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.AlpineRandom;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * The model of the  neural net.
 * 
 */
public class NNModellOracle extends NNModel {
    private static Logger itsLogger= Logger.getLogger(NNModellOracle.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = 4348400661233375023L;
	public NNModellOracle(DataSet trainingDataSet,DataSet oldDataSet,List<String> columnNamesList) {
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
				if(useFloatArraySumCursor()){
					StringBuffer varcharArray = CommonUtility.splitOracleSqlToVarcharArray(sql);
					sql = new StringBuffer();
					sql.append("select floatarraysum_cursor(").append(varcharArray).append(") from dual");
				}
				itsLogger.debug("NNModel.adjustPerWholeData():sql="+sql);

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
			itsLogger.debug("cycle"+cycle+";error:"+error);
			if (error < maxError) {
				itsLogger.debug("loop break : "+cycle+" error: "+ error);
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
	public StringBuffer getOutput(int outputCount,int outputNodeNo,boolean prediction)
	{
		StringBuffer output = new StringBuffer("alpine_miner_get_fa_element(");
		StringBuffer allOutput = getAllOutput(outputCount,prediction);
		output.append(allOutput).append(",").append(outputNodeNo).append(")");

		return output;

	}
	public String getTrue()
	{
		return "1";
	}
	public String getFalse()
	{
		return "0";
	}
    private boolean useFloatArraySumCursor(){
    	int weightCount = getWeightCount();
		int resultLength = weightCount + 1;
		if(resultLength < AlpineDataAnalysisConfig.ORACLE_FLOAT_SUM_MAX_COUNT){
			return false;
		}else{
			return true;
		}
    }
	public String getArraySum()
	{
		if(useFloatArraySumCursor()){
			return "";
		}else{
			return "FloatArraySum";
		}
	}
	
	private int getWeightCount() {
		int weightCount = 0;
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
				for (int i = 0; i <= inputNodes.length; i++)
				{
					double temp = weights[i];
					if (Double.isNaN(temp)){
						temp  = 0.0;
					}
					weightCount++;
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
				for (int i = 0; i <= inputNodes.length; i++)
				{
					double temp = weights[i];
					if (Double.isNaN(temp)){
						temp  = 0.0;
					}
					weightCount++;
				}
			}  
		}
		return weightCount;
	}

	protected Double[] getResult(ResultSet rs) throws SQLException
	{
		Double[] currentChanges = null;
		ResultSet result=rs.getArray(1).getResultSet();
		if (result == null)
		{
			return null;
		}
		ArrayList<Double> listResult = new ArrayList<Double>();
		while(result.next()){
			listResult.add(result.getInt(1) - 1, result.getDouble(2));
		}
		currentChanges=new Double[listResult.size()];
		for(int i = 0; i < currentChanges.length; i++){
			if (listResult.get(i) != null && !Double.isNaN(listResult.get(i)))
			{
				currentChanges[i]=listResult.get(i);
			}
			else
			{
				currentChanges[i] = Double.NaN;
			}
		}
		return currentChanges;
	}
	protected void dropConfidence(Statement st, StringBuffer confidenceDrop)
		throws SQLException {
		if (useCFunction)
		{
			itsLogger.debug("NNModellOracle.getResult():sql="+confidenceDrop.toString());
			st.execute(confidenceDrop.toString());
		}
	}
	protected void getProbabilityiesSql(DataSet dataSet,
			String newTableName, Statement st, ResultSet rs,
			StringBuffer confidenceDrop, int numberOfClasses,
			StringBuffer[] classProbabilitiesSQL) throws OperatorException {
			getProbabilityiesSqlFunction(dataSet, newTableName, st, rs,
					confidenceDrop, numberOfClasses, classProbabilitiesSQL);
	}	
	protected String getFloatArray()
	{
		return " FloatArray ";
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
		
		sql.append(" update "+ newTableName+" set "+StringHandler.doubleQ(predictedLabel.getName())).append("=").append(caseSql);
		for ( int c = 0; c < numberOfClasses; c++)
		{
			sql.append(", "+StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(c)).getName())).append(" = ").append(classProbabilitiesSql[c]);
		}		
		sql.append(getWherePredict());
		return sql;
	}
	protected void getClassProbabilities(StringBuffer[] classProbabilitiesSQL,
			StringBuffer confidenceColumn, int c) {
		classProbabilitiesSQL[c].append("alpine_miner_get_fa_element(").append(confidenceColumn).append(",").append(c+1).append(")");
	}
	public String getWeightArray() {
		ArrayList<String> weightArray = new ArrayList<String>();
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
				for (int i = 0; i <= inputNodes.length; i++)
				{
					double temp = weights[i];
					if (Double.isNaN(temp)){
						temp  = 0.0;
					}
					weightArray.add(String.valueOf(temp));
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
				for (int i = 0; i <= inputNodes.length; i++)
				{
					double temp = weights[i];
					if (Double.isNaN(temp)){
						temp  = 0.0;
					}
					weightArray.add(String.valueOf(temp));

				}
			}  
		}
		return CommonUtility.array2OracleArray(weightArray,CommonUtility.OracleDataType.Float).toString();
	}
	public String getColumnNames(boolean prediction){
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			if(prediction)
			{
				array.add(nodeInputs[i].getTransformValue().toString());
			}
			else
			{
				array.add(StringHandler.doubleQ(nodeInputs[i].getColumn().getName()));
			}
		}
		return CommonUtility.array2OracleArray(array,CommonUtility.OracleDataType.Float).toString();
	}
	public String getInputRangeArray(){
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			double temp = nodeInputs[i].getColumnRange();
			if (Double.isNaN(temp)){
				temp  = 0.0;
			}
			array.add(String.valueOf(temp));
		}
		return CommonUtility.array2OracleArray(array,CommonUtility.OracleDataType.Float).toString();
	}
	public String getInputBaseArray(){
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < nodeInputs.length; i++)
		{
			double temp = nodeInputs[i].getColumnBase();
			if (Double.isNaN(temp)){
				temp  = 0.0;
			}
			array.add(String.valueOf(temp));
		}
		return CommonUtility.array2OracleArray(array,CommonUtility.OracleDataType.Float).toString();
	}
}
