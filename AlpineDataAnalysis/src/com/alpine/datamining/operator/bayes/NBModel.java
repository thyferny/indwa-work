
package com.alpine.datamining.operator.bayes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.MinerInit;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.Mapping;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.training.Prediction;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class NBModel extends  Prediction{
    private static Logger itsLogger= Logger.getLogger(NBModel.class);

    
	private static final long serialVersionUID = 8500777461015009912L;

	private static final String UNKNOWN_VALUE_NAME = ALPINE_MINER_CATEGORY_NULL;

	protected static final int INDEX_COUNT = 0;
	
	protected static final int INDEX_VALUE_SUM = 1;
	
	protected static final int INDEX_SQUARED_VALUE_SUM = 2;
	
	protected static final int INDEX_MISSING_WEIGHTS = 3;
	
	public static final int INDEX_MEAN = 0;
	
	public static final int INDEX_STANDARD_DEVIATION = 1;
	
	protected static final int INDEX_LOG_FACTOR = 2;
	
	private boolean calculateDeviance;
	
	protected int numberOfClasses;
	
	private int numberOfColumns;
	
	private boolean[] nominal;

	protected String[] classValues;
	
	private String[] columnNames;

	private String[][] columnValues;

	protected double totalWeight;
	
	protected double[] classWeights;
	
	
	protected double[][][] weightSums;

	

	
	protected double[] priors;
	
	
	protected double[][][] distributionProperties;


	boolean laplaceCorrectionEnabled;
	
	protected double nullDeviance = Double.NaN;
	
	protected double deviance = Double.NaN;
	
	protected ArrayList<String> updateColumns = new ArrayList<String>();
	
	protected IDataSourceInfo dataSourceInfo = null;
	
	protected IMultiDBUtility multiDBUtility = null;

	
	public NBModel(DataSet dataSet, boolean laplaceCorrectionEnabled, boolean calculateDeviance){
		super(dataSet);
		dataSourceInfo = DataSourceInfoFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); 
		
		this.laplaceCorrectionEnabled = laplaceCorrectionEnabled;
		this.calculateDeviance = calculateDeviance;
	}
	
	public void calculateModel(DataSet dataSet) throws OperatorException{
		Column labelColumn = dataSet.getColumns().getLabel();
		numberOfClasses = labelColumn.getMapping().size();
		numberOfColumns = dataSet.getColumns().size();
		nominal = new boolean[numberOfColumns];
		columnNames = new String[numberOfColumns];
		columnValues = new String[numberOfColumns][];
		classValues = new String[numberOfClasses];
		for (int i = 0; i < numberOfClasses; i++) {
			classValues[i] = labelColumn.getMapping().mapIndex(i);
		}
		int columnIndex = 0;
		weightSums = new double[numberOfColumns][numberOfClasses][];
		distributionProperties = new double[numberOfColumns][numberOfClasses][];
		for (Column column : dataSet.getColumns()) {
			columnNames[columnIndex] = column.getName();
			if (column.isNominal()) {
				nominal[columnIndex] = true;
				int mappingSize = column.getMapping().size() + 1;
				columnValues[columnIndex] = new String[mappingSize];
				for (int i = 0; i < mappingSize - 1; i++) {
					columnValues[columnIndex][i] = column.getMapping().mapIndex(i);
				}
				columnValues[columnIndex][mappingSize - 1] = UNKNOWN_VALUE_NAME;
				for (int i = 0; i < numberOfClasses; i++) {
					weightSums[columnIndex][i] = new double[mappingSize];
					distributionProperties[columnIndex][i] = new double[mappingSize];
				}
			} else {
				nominal[columnIndex] = false;
				for (int i = 0; i < numberOfClasses; i++) {
					weightSums[columnIndex][i] = new double[4];
					distributionProperties[columnIndex][i] = new double[3];
				}
			}
			columnIndex++;
		}

		//  initialization of total and a priori weight counters
		totalWeight = 0.0d;
		classWeights = new double[numberOfClasses];
		priors = new double[numberOfClasses];

		// update the model
		getWeight(dataSet);
		
		// calculate the probabilites
		calculateDistribution();
	}
	
	
	public ArrayList<String> getUpdateColumns() {
		return updateColumns;
	}

	
	public void setUpdateColumns(ArrayList<String> updateColumns) {
		this.updateColumns = updateColumns;
	}
	public double[][][] getDistributionProperties() {
		return distributionProperties;
	}
	
	public double[][][] getWeightSums() {
		return weightSums;
	}
	public boolean[] getNominal() {
		return nominal;
	}
	public String[][] getColumnValues() {
		return columnValues;
	}
	public String[] getColumneNames() {
		return this.columnNames;
	}
	
	public int getNumberOfColumns() {
		return this.columnNames.length;
	}
	
	
	public void getWeight(DataSet dataSet) throws OperatorException {

		String weight = "1.0::float";
		String weightProduct = "";

		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable())
				.getTableName();

		Column label =dataSet.getColumns().getLabel();
		String labeName = StringHandler.doubleQ(label.getName());

		Statement st = null;
		ResultSet rs = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		ArrayList<String> sumString = new ArrayList<String>();
		int index = 0;
		boolean firstColumn = true;
		for (int i = 0; i < label.getMapping().size(); i++)
		{
			String labelValue = label.getMapping().mapIndex(i);
			labelValue=StringHandler.escQ(labelValue);
			labelValue = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,
					labelValue);

			for (Column column : dataSet.getColumns())
			{
				String name = StringHandler.doubleQ(column.getName());
				if (column.isNominal())
				{
					Mapping mapping = column.getMapping();
					for (int j = 0; j < column.getMapping().size(); j++)
					{
							String nominalstr = mapping.mapIndex(j);
							StringBuffer caseString = new StringBuffer();

							nominalstr=StringHandler.escQ(nominalstr);
							
							caseString.append("sum( ").append(weightProduct).append("(case when ").append(name).append("='").append(nominalstr).append("' and ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
							sumString.add(caseString.toString());
							if (firstColumn)
							{
								firstColumn = false;
							}	
					}
					StringBuffer caseString = new StringBuffer();
					caseString.append("sum( ").append(weightProduct).append("(case when ").append(name).append(" is null and ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(caseString.toString());
					if (firstColumn)
					{
						firstColumn = false;
					}	
				}
				else
				{
					String numericalstr = "sum(case when "+labeName+"="+labelValue+" and "+name+" is not null  then 1 else 0 end)";
					sumString.add(numericalstr.toString());
					if (firstColumn)
					{
						firstColumn = false;
					}	
					StringBuffer tmp = new StringBuffer();
					tmp.append("sum(").append(weightProduct).append(name).append("::float*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(tmp.toString());
					tmp = new StringBuffer();
					tmp.append("sum(").append(weightProduct).append(name).append("::float*").append(name).append("::float*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(tmp.toString());
					tmp = new StringBuffer();
					tmp.append("sum(").append(weightProduct).append("(case when ").append(name).append(" is null and ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(tmp.toString());
				}
				index++;
			}
		}
		sumString.add("sum("+weight+")");
		for (int i = 0; i < label.getMapping().size(); i++)
		{
			String labelValue = label.getMapping().mapIndex(i);
			labelValue = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,
					labelValue);
			StringBuffer tmp = new StringBuffer();
			tmp.append("sum(").append(weightProduct).append("(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
			sumString.add(tmp.toString());

		}
		ArrayList<Double> sumDouble= new ArrayList<Double>();
		try{
			int maxColumnCount = AlpineDataAnalysisConfig.NB_GP_COLUMN_COUNT;
			int cycle = (sumString.size()-1)/maxColumnCount + 1;
			for(int i = 0; i < cycle; i++)
			{
				StringBuffer sql = new StringBuffer();
				sql.append("select ");
				int count = 0;
				if (i == cycle - 1)
				{
					count = sumString.size() - maxColumnCount * ( cycle - 1);
				}
				else
				{
					count = maxColumnCount;
				}
				for (int j = 0; j < count; j++){
					if(j != 0)
					{
						sql.append(",");
					}
					sql.append(sumString.get(i*maxColumnCount+j));
				}
				sql.append(" from ").append(tableName);
				itsLogger.debug("NBModel.updateModel():sql="+sql);
				rs = st.executeQuery(sql.toString());
				while(rs.next()){
					for ( int j = 0 ; j < count; j++)
					{
						sumDouble.add(rs.getDouble(j+1));
					}
				}
				rs.close();
			}
				int columnIndex = 0;
				int valueIndex = 0;
				int classIndex = 0;
				int allColumnIndex = 0;
				for (int i = 0; i < label.getMapping().size();i++)
				{
					columnIndex = 0;
					for (Column column : dataSet.getColumns()){
						if(column.isNominal()){
							valueIndex = 0;
							for (int j =0 ; j < column.getMapping().size() + 1; j++)
							{
								weightSums[columnIndex][classIndex][valueIndex] = sumDouble.get(allColumnIndex);
								allColumnIndex++;
								valueIndex++;
							}
						}else{
							valueIndex = 0;
							weightSums[columnIndex][classIndex][INDEX_COUNT] = sumDouble.get(allColumnIndex);
							allColumnIndex ++;
							weightSums[columnIndex][classIndex][INDEX_VALUE_SUM] = sumDouble.get(allColumnIndex);
							allColumnIndex ++;
							weightSums[columnIndex][classIndex][INDEX_SQUARED_VALUE_SUM] = sumDouble.get(allColumnIndex);
							allColumnIndex ++;
							weightSums[columnIndex][classIndex][INDEX_MISSING_WEIGHTS] = sumDouble.get(allColumnIndex);
							allColumnIndex ++;
						}
						columnIndex++;
					}
					classIndex++;

				}
				totalWeight = sumDouble.get(allColumnIndex);
				allColumnIndex ++;
				for (int i = 0; i < label.getMapping().size(); i++)
				{
					classWeights[i] = sumDouble.get(allColumnIndex);
					allColumnIndex ++;
				}
			rs.close();
			st.close();
		}catch(SQLException e)
		{
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	protected void calculateDistribution() {
		double f = laplaceCorrectionEnabled ? 1 / totalWeight : Double.MIN_VALUE;
		double logFactorCoefficient = Math.sqrt(2 * Math.PI);
		for (int i = 0; i < numberOfClasses; i++) {
			priors[i] = Math.log(classWeights[i] / totalWeight);
		}
		for (int i = 0; i < numberOfColumns; i++) {
			if (nominal[i]) {
				for (int j = 0; j < numberOfClasses; j++) {
					for (int k = 0; k < weightSums[i][j].length; k++) {
						distributionProperties[i][j][k] = Math.log((weightSums[i][j][k] + f) / (classWeights[j] + f * weightSums[i][j].length)); 
					}					
				}
			} else {
				for (int j = 0; j < numberOfClasses; j++) {
					double classWeight = classWeights[j] - weightSums[i][j][INDEX_MISSING_WEIGHTS];
					distributionProperties[i][j][INDEX_MEAN] = weightSums[i][j][INDEX_VALUE_SUM] / classWeight;
					if (Double.isNaN(distributionProperties[i][j][INDEX_MEAN])){
						distributionProperties[i][j][INDEX_MEAN] = 0.0;
					}
					double standardDeviation = Math.sqrt((weightSums[i][j][INDEX_SQUARED_VALUE_SUM] - weightSums[i][j][INDEX_VALUE_SUM] * weightSums[i][j][INDEX_VALUE_SUM] / classWeight) / (((weightSums[i][j][INDEX_COUNT] - 1) / weightSums[i][j][INDEX_COUNT]) * classWeight));
					if (Double.isNaN(standardDeviation) || standardDeviation <= 1e-3) {
						standardDeviation = 1e-3;
					}
					distributionProperties[i][j][INDEX_STANDARD_DEVIATION] = standardDeviation;
					distributionProperties[i][j][INDEX_LOG_FACTOR] = Math.log(distributionProperties[i][j][INDEX_STANDARD_DEVIATION] * logFactorCoefficient);
				}
			}
		}
	}

	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException{
		Statement st = null;
    	DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		Column label = getLabel();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		if (MinerInit.isUseCFunction())
		{
			predictionCFunction(dataSet, predictedLabel, st, tableName);
		}
		else
		{
			preditionSQL(dataSet, predictedLabel, st, tableName, label);
		}

		return dataSet;
	}
private void predictionCFunction(DataSet dataSet, Column predictedLabel,
		Statement st, String tableName)
		throws OperatorException {
	StringBuffer nominalColumnNamesArray = getNominalColumnNamesArray();
	StringBuffer nominalColumnsMappingCount = getNominalColumnsMappingCount();
	StringBuffer nominalColumnsMapping = getNominalColumnsMapping();
	StringBuffer nominalColumnsProbability = getNominalColumnsProbability();
	StringBuffer dependentColumnMapping = getDependentColumnMapping();
	StringBuffer dependentColumnProbability = getDependentColumnProbability();
	StringBuffer numericalColumns = getNumericalColumns();
	StringBuffer numericalColumnsProbability = getNumericalColumnsProbability();
	StringBuffer where = getWhere(false);
	StringBuffer updateSQL = new StringBuffer();

	StringBuffer confidence_array = new StringBuffer("temp_prediction_confidence_array");
	StringBuffer addColumn = new StringBuffer();
	addColumn.append("alter table ").append(tableName).append(" add column ").append(confidence_array).append(" float[] ");
	try {
		itsLogger.debug("NBModel.predictionCFunction():sql="+addColumn);
		st.execute(addColumn.toString());
	} catch (SQLException e) {
		e.printStackTrace();
		throw new OperatorException(e.getLocalizedMessage());
	}


	updateSQL.append("update ").append(tableName).append(" set (").append(confidence_array).append(")").append(" = (alpine_miner_nb_ca_confidence(")
	.append(nominalColumnNamesArray).append(",").append(nominalColumnsMappingCount).append(",")
	.append(nominalColumnsMapping).append(",").append(nominalColumnsProbability).append(",").append(dependentColumnMapping).append(",")
	.append(dependentColumnProbability).append(",").append(numericalColumns).append(",")
	.append(numericalColumnsProbability).append("))").append(where);

	try {
		itsLogger.debug("NBModel.predictionCFunction():sql="+updateSQL);
		st.execute(updateSQL.toString());
	} catch (SQLException e) {
		e.printStackTrace();
		throw new OperatorException(e.getLocalizedMessage());
	}
	updateColumns.add(predictedLabel.getName());

	StringBuffer confidenceSet = new StringBuffer();
	StringBuffer confidenceValue = new StringBuffer();
	for(int i = 0; i < numberOfClasses; i++)
	{
		if (i != 0)
		{
			confidenceSet.append(",");
			confidenceValue.append(",");
		}
		confidenceSet.append("\"").append(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName()).append("\"");
		confidenceValue.append(confidence_array).append("[").append(i+1).append("]");
		updateColumns.add(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName());
	}
	updateSQL = new StringBuffer();
	updateSQL.append("update ").append(tableName).append(" set(").append(StringHandler.doubleQ(predictedLabel.getName())).append(",").append(confidenceSet).append(") = (");
	updateSQL.append("alpine_miner_nb_ca_prediction(").append(confidence_array).append(",").append(getDependentColumnMapping()).append(")");
	if (getLabel().isNumerical() && getLabel().isCategory())
	{
		updateSQL.append("::float");
	}
	updateSQL.append(",").append(confidenceValue).append(")");

	try {
		itsLogger.debug("NBModel.predictionCFunction():sql="+updateSQL);
		st.execute(updateSQL.toString());
	} catch (SQLException e) {
		e.printStackTrace();
		throw new OperatorException(e.getLocalizedMessage());
	}

	StringBuffer dropColumn = new StringBuffer();
	dropColumn.append("alter table ").append(tableName).append(" drop column ").append(confidence_array);
	try {
		itsLogger.debug("NBModel.predictionCFunction():sql="+dropColumn);
		st.execute(dropColumn.toString());
	} catch (SQLException e) {
		e.printStackTrace();
		throw new OperatorException(e.getLocalizedMessage());
	}
}

private void preditionSQL(DataSet dataSet, Column predictedLabel,
		Statement st, String tableName, Column label)
		throws OperatorException {
	StringBuffer[] probability = new StringBuffer[numberOfClasses];
	for (int i = 0; i < label.getMapping().size();i++)
	{
		probability[i] = new StringBuffer();
		probability[i].append("("+ priors[i]);
		int j = 0;
		for (Column column : getTrainingHeader().getColumns())
		{

			String name = StringHandler.doubleQ(column.getName());
			if (column.isNominal())
			{
				
				Mapping mapping = column.getMapping();
				StringBuffer caseString = new StringBuffer("(case ");
				for (int intValue = 0; intValue < mapping.size(); intValue++)
				{
						String nominalstr = mapping.mapIndex(intValue);
						nominalstr=StringHandler.escQ(nominalstr);
						caseString.append(" when ").append(name).append(" = '").append(nominalstr).append("' then ").append(distributionProperties[j][i][intValue]);
				}
				caseString.append(" when ").append(name).append(" is null then ").append(distributionProperties[j][i][mapping.size()]);
				caseString.append(" else 0 end)");
				probability[i].append("+").append(caseString);
			}
			else
			{
				StringBuffer base = new StringBuffer();
				base.append("(").append(name).append( "-(").append(distributionProperties[j][i][INDEX_MEAN]).append(")) /").append( distributionProperties[j][i][INDEX_STANDARD_DEVIATION]);
				probability[i].append("-(").append(distributionProperties[j][i][INDEX_LOG_FACTOR]).append(" + 0.5 * ").append(base).append("*").append(base).append(")");
			}
			j++;
		}
		probability[i].append(")");
	}
	
	StringBuffer prediction = new StringBuffer("(case ");
	for (int i = 0; i < numberOfClasses; i++)
	{
		String value = StringHandler.escQ(label.getMapping().mapIndex(i));
		value = CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,
				value);
		prediction.append(" when  ");
		for ( int j = 0; j < numberOfClasses; j++)
		{
			if (i != j)
			{
				if (j == 0 || (j == 1 && i ==0))
				{
				}
				else
				{
					prediction.append(" and ");
				}
				prediction.append(probability[i]).append(">").append(probability[j]);
			}
		}
		prediction.append(" then ").append(value);
	}
	prediction.append(" else null end)");
	updateColumns.add(predictedLabel.getName());
	String predictedLabelName = predictedLabel.getName();

	StringBuffer maxLogProbability = new StringBuffer("greatest(");
	
	for (int i = 0; i < numberOfClasses; i++){
		if (i != 0)
		{
			maxLogProbability.append(",");
		}
		maxLogProbability.append(probability[i]);
	}
	maxLogProbability.append(")");
	StringBuffer[] probability_exp = new StringBuffer[numberOfClasses];
	for (int i = 0; i < numberOfClasses; i++){
		probability_exp[i] = new StringBuffer();
		probability_exp[i].append("(case when (").append(probability[i]).append("- ").append(maxLogProbability).append(") < -45 then 0.0000001 else exp((").append(probability[i]).append("- ").append(maxLogProbability).append(")) end)");
	}
	StringBuffer probabilitySum = new StringBuffer("(");
	for (int i = 0; i < numberOfClasses; i++){
		if (i != 0)
		{
			probabilitySum.append("+");
		}
		probabilitySum.append(probability_exp[i]);
	}
	probabilitySum.append(")");
	
	StringBuffer[] probability_devide = new StringBuffer[numberOfClasses];

	for (int i = 0; i < numberOfClasses; i++){
		probability_devide[i] = new StringBuffer();
		probability_devide[i].append("(case when ").append(probability_exp[i]).append("/").append(probabilitySum).append("< 0.0000001 then 0.0 else ").append(probability_exp[i]).append("/").append(probabilitySum).append(" end)");
	}
	StringBuffer set = new StringBuffer();
	set.append("\"").append(predictedLabelName).append("\"");
	StringBuffer value = new StringBuffer(prediction);
	for (int i = 0; i < numberOfClasses; i++)
	{
		set.append(",\"").append(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName()).append("\"");
		updateColumns.add(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName());
		value.append(",").append(probability_devide[i]);
	}
	StringBuffer sql = new StringBuffer();
	sql.append("update ").append(tableName).append(" set(").append(set).append(") = (").append(value).append(")");

	try{
		itsLogger.debug("NBModel.updateModel():sql="+sql);
		st.execute(sql.toString());
	}catch(SQLException e){
		e.printStackTrace();
		throw new OperatorException(e.getLocalizedMessage());
	}
}
	public void caculateDeviance() throws OperatorException
	{
		Column label = getLabel();
		DataSet dataSet = getTrainingHeader();
		Statement st = null;
    	DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		StringBuffer devianceSQL = null;
		if(MinerInit.isUseCFunction())
		{
			devianceSQL = getDevianceFunction(label, tableName);

		}
		else
		{
			devianceSQL = getDevianceSQL(label, tableName);
		}

		try {
			itsLogger.debug("NBModel.caculateDeviance():sql="+devianceSQL);
			ResultSet rs = st.executeQuery(devianceSQL.toString());
			if (rs.next())
			{
				deviance = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer countSQL = new StringBuffer();
		countSQL.append(" select ")
		.append(" count(*) from ").append(tableName).append(" where ").append(
				StringHandler.doubleQ(label.getName())).append(" is not null group by ").append(
				StringHandler.doubleQ(label.getName()));
		
		double totalCount = 0.0;
		ArrayList<Double> countArray = new ArrayList<Double>();
		try {
			itsLogger.debug("NBModel.caculateDeviance():sql="+countSQL);
			ResultSet rs = st.executeQuery(countSQL.toString());
			while (rs.next())
			{
				double count = rs.getDouble(1);
				countArray.add(count);
				totalCount+= count;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
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
	protected StringBuffer getDevianceFunction(Column label, String tableName) {
		StringBuffer nominalColumnNamesArray = getNominalColumnNamesArray();
		StringBuffer nominalColumnsMappingCount = getNominalColumnsMappingCount();
		StringBuffer nominalColumnsMapping = getNominalColumnsMapping();
		StringBuffer nominalColumnsProbability = getNominalColumnsProbability();
		StringBuffer dependentColumnMapping = getDependentColumnMapping();
		StringBuffer dependentColumnProbability = getDependentColumnProbability();
		StringBuffer numericalColumns = getNumericalColumns();
		StringBuffer numericalColumnsProbability = getNumericalColumnsProbability();
		StringBuffer where = getWhere(true);
		StringBuffer devianceSQL = new StringBuffer();
		
		devianceSQL.append("select sum(alpine_miner_nb_ca_deviance(")
		.append(nominalColumnNamesArray).append(",").append(nominalColumnsMappingCount).append(",")
		.append(nominalColumnsMapping).append(",").append(nominalColumnsProbability).append(",");
		devianceSQL.append(getNominalColumnName(label)).append(",");
		devianceSQL.append(dependentColumnMapping).append(",")
		.append(dependentColumnProbability).append(",").append(numericalColumns).append(",")
		.append(numericalColumnsProbability).append(")) from ").append(tableName).append(where);
		return devianceSQL;
	}
	private StringBuffer getDevianceSQL(Column label, String tableName) {
		StringBuffer[] probability = new StringBuffer[numberOfClasses];
		for (int i = 0; i < label.getMapping().size();i++)
		{
			probability[i] = new StringBuffer();
			probability[i].append("(").append(priors[i]);
			int j = 0;
			for (Column column : getTrainingHeader().getColumns())
			{
				String name = StringHandler.doubleQ(column.getName());
				if (column.isNominal())
				{
					
					Mapping mapping = column.getMapping();
					StringBuffer caseString = new StringBuffer("(case ");
					for (int intValue = 0; intValue < mapping.size(); intValue++)
					{
							String nominalstr = mapping.mapIndex(intValue);
							nominalstr=StringHandler.escQ(nominalstr);
							caseString.append(" when ").append(name).append(" = '").append(nominalstr).append("' then ").append(distributionProperties[j][i][intValue]);
					}
					caseString.append(" else 0 end)" );
					probability[i].append("+").append(caseString);
				}
				else
				{
					String base = "("+name + "-("+distributionProperties[j][i][INDEX_MEAN]+")) /"+ distributionProperties[j][i][INDEX_STANDARD_DEVIATION];
					probability[i].append("-(").append(distributionProperties[j][i][INDEX_LOG_FACTOR]).append(" + 0.5 * ").append(base).append("*").append(base).append(")");
				}
				j++;
			}
			probability[i].append(")");
		}
		StringBuffer maxLogProbability = new StringBuffer("greatest(");
		
		for (int i = 0; i < numberOfClasses; i++){
			if (i != 0)
			{
				maxLogProbability.append(",");
			}
			maxLogProbability.append(probability[i]);
		}
		maxLogProbability.append(")");
		StringBuffer[] probability_exp = new StringBuffer[numberOfClasses];
		for (int i = 0; i < numberOfClasses; i++){
			probability_exp[i] = new StringBuffer();
			probability_exp[i].append("(case when (").append(probability[i] ).append("- ").append(maxLogProbability).append(") < -45 then 0.0000001 else exp(").append(probability[i]).append("- ").append(maxLogProbability).append(") end)");
		}
		StringBuffer probabilitySum = new StringBuffer("(");
		for (int i = 0; i < numberOfClasses; i++){
			if (i != 0)
			{
				probabilitySum.append("+");
			}
			probabilitySum.append(probability_exp[i]);
		}
		probabilitySum.append(")");
		
		for (int i = 0; i < numberOfClasses; i++){
			probability_exp[i].append("/").append(probabilitySum);
		}


		StringBuffer devianceSQL = new StringBuffer();

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
						StringHandler.doubleQ(label.getName())).append("=").append(CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,label.getMapping().mapIndex(i))).append(" then ");
			}
			devianceSQL.append(
					" ln(").append(probability_exp[i]).append(")");
		}
		devianceSQL.append(" end ) ");
		devianceSQL.append(" from ").append(tableName);
		return devianceSQL;
	}

	protected StringBuffer getWhere(boolean needDependentColumn)
	{
		StringBuffer sb = new StringBuffer(" where ");
		DataSet dataSet = getTrainingHeader();
		boolean first = true;
		for (Column column : dataSet.getColumns()) {
			if(column.isNominal())
			{
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(" and ");
			}
			sb.append(StringHandler.doubleQ(column.getName())).append(" is not null ");
		}
		if (needDependentColumn)
		{
			if (first) {
				first = false;
			} else {
				sb.append(" and ");
			}
			sb.append(StringHandler.doubleQ(getLabel().getName())).append(" is not null ");
		}
		if (first)
		{
			return new StringBuffer("");
		}
		return sb;
	}

	protected StringBuffer getNumericalColumnsProbability() {
		StringBuffer sb = new StringBuffer(multiDBUtility.floatArrayHead());
		boolean first = true;

		int j = 0;
		for (Column column : getTrainingHeader().getColumns()) {
			if (column.isNumerical()) {
				for (int i = 0; i < numberOfClasses; i++) {
					if (first) {
						first = false;
					} else {
						sb.append(",");
					}
					sb.append(distributionProperties[j][i][INDEX_MEAN]).append(
							",");
					sb.append(distributionProperties[j][i][INDEX_STANDARD_DEVIATION])
							.append(",");
					sb.append(distributionProperties[j][i][INDEX_LOG_FACTOR]);
				}
			}
			j++;
		}
		sb.append(multiDBUtility.floatArrayTail());
		if (first) {
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNumericalColumns() {
		StringBuffer sb = new StringBuffer(multiDBUtility.floatArrayHead());
		DataSet dataSet = getTrainingHeader();
		boolean first = true;
		for (Column column: dataSet.getColumns())
		{

			if (column.isNumerical())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(StringHandler.doubleQ(column.getName()));
			}
		}
		sb.append(multiDBUtility.floatArrayTail());
		if(first)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getDependentColumnProbability() {
		StringBuffer sb = new StringBuffer(multiDBUtility.floatArrayHead());
		boolean first = true;
		for (int i = 0; i < numberOfClasses; i++) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(priors[i]);
		}
		sb.append(multiDBUtility.floatArrayTail());
		if (first) {
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getDependentColumnMapping() {
		StringBuffer sb = new StringBuffer(multiDBUtility.stringArrayHead());
		Column label = getLabel();
		boolean first = true;
		for (int i = 0; i < label.getMapping().size(); i++) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(CommonUtility.quoteValue(dataSourceInfo.getDBType(), label,label.getMapping().mapIndex(i)));
		}
		sb.append(multiDBUtility.stringArrayTail());
		if (first) {
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnsProbability() {
		StringBuffer sb = new StringBuffer(multiDBUtility.floatArrayHead());
		boolean first = true;

		int j = 0;
		for (Column column : getTrainingHeader().getColumns()) {
			if (column.isNominal()) {
				for (int i = 0; i < numberOfClasses; i++) {
					for (int intValue = 0; intValue < column.getMapping()
							.size(); intValue++) {
						if (first) {
							first = false;
						} else {
							sb.append(",");
						}
						sb.append(distributionProperties[j][i][intValue]);
					}
					if (first) {
						first = false;
					} else {
						sb.append(",");
					}
					sb.append(distributionProperties[j][i][column.getMapping().size()]);
				}
			}
			j++;
		}

		sb.append(multiDBUtility.floatArrayTail());
		if (first) {
			return new StringBuffer("null");
		} else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnsMapping() {
		StringBuffer sb = new StringBuffer(multiDBUtility.stringArrayHead());
		DataSet dataSet = getTrainingHeader();
		boolean first = true;
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				for(int i = 0; i < column.getMapping().size(); i++)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append("'"+StringHandler.escQ(column.getMapping().mapIndex(i))+"'");
				}
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append("'"+StringHandler.escQ(ALPINE_MINER_CATEGORY_NULL)+"'");
			}
		}
		sb.append(multiDBUtility.stringArrayTail());
		if(first)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnsMappingCount() {
		StringBuffer sb = new StringBuffer(multiDBUtility.intArrayHead());
		DataSet dataSet = getTrainingHeader();
		boolean first = true;
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(column.getMapping().size()+1);
			}
		}
		sb.append(multiDBUtility.intArrayTail());
		if(first)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnNamesArray() {
		StringBuffer sb = new StringBuffer(multiDBUtility.stringArrayHead());
		DataSet dataSet = getTrainingHeader();
		boolean first = true;
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(getNominalColumnName(column));
			}
		}
		sb.append(multiDBUtility.stringArrayTail());
		if(first)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnName(Column column) {
		StringBuffer sb = new StringBuffer();
		sb.append(" (case when ").append(StringHandler.doubleQ(column.getName())).append(" is null then '").append(StringHandler.escQ(ALPINE_MINER_CATEGORY_NULL)).append("' else ");
		if (column.getValueType() == DataType.BOOLEAN)
		{
			sb.append(" (case when ").append(StringHandler.doubleQ(column.getName())).append(" is true then 't' else 'f' end)");
		}
		else if (column.getValueType() == DataType.DATE ||column.getValueType() == DataType.DATE_TIME||column.getValueType() == DataType.TIME )
		{
			sb.append(StringHandler.doubleQ(column.getName())).append("::text");
		}
		else
		{
			sb.append(StringHandler.doubleQ(column.getName())).append("::text");
		}
		sb.append(" end )");
		return sb;
	}

	
	public void setLaplaceCorrectionEnabled(boolean laplaceCorrectionEnabled) {
		this.laplaceCorrectionEnabled = laplaceCorrectionEnabled;
	}
	
	public boolean getLaplaceCorrectionEnabled() {
		return laplaceCorrectionEnabled;
	}

	public int getNumberOfClasses() {
		return numberOfClasses;
	}
	
	public String getClassName(int index) {
		return classValues[index];
	}
	
	public String[] getClassValues() {
		return classValues;
	}

	public double[] getPriors() {
		return priors;
	}

	public double getNullDeviance() {
		return nullDeviance;
	}

	public double getDeviance() {
		return deviance;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("class priors").append(Tools.getLineSeparator());
		for (int i = 0; i < numberOfClasses; i++) {
			buffer.append("\tpriors("+classValues[i]+"):").append((Math.exp(priors[i]))).append("\t");
		}
		buffer.append(Tools.getLineSeparators(2));
		for (int i = 0; i < numberOfColumns; i++) {
			buffer.append("column ").append(columnNames[i]).append(" distributionProperty:").append(Tools.getLineSeparator());
			if (nominal[i]) {
				for (int j = 0; j < numberOfClasses; j++) {
					buffer.append("\tclass: "+classValues[j]).append(Tools.getLineSeparator());//.append("\t");
					for (int k = 0; k < weightSums[i][j].length - 1; k++) {
						buffer.append("\tdistributionProperty(").append(columnValues[i][k]).append("): ").append((Math.exp(distributionProperties[i][j][k])));
					}
					if (weightSums[i][j][weightSums[i][j].length - 1] != 0)
					{
						buffer.append("\tdistributionProperty(").append(columnValues[i][weightSums[i][j].length - 1]).append("): ").append((Math.exp(distributionProperties[i][j][weightSums[i][j].length - 1])));
					}
					buffer.append(Tools.getLineSeparator());
				}
			} else {
				for (int j = 0; j < numberOfClasses; j++) {
					buffer.append("\tclass: "+classValues[j]).append(Tools.getLineSeparator()).append("\t");
					buffer.append("\tdistributionProperty(mean):").append((distributionProperties[i][j][INDEX_MEAN]));
					buffer.append("\tdistributionProperty(standard_deviation):").append((distributionProperties[i][j][INDEX_STANDARD_DEVIATION]));
					buffer.append(Tools.getLineSeparator());
				}
			}
			buffer.append(Tools.getLineSeparator());
		}
		if (isCalculateDeviance())
		{
			buffer.append("null Deviance:").append((nullDeviance)).append("\tdeviance:").append((deviance)).append(Tools.getLineSeparator());
		}
		return buffer.toString();
	}
	public boolean isCalculateDeviance() {
		return calculateDeviance;
	}

	public void setCalculateDeviance(boolean calculateDeviance) {
		this.calculateDeviance = calculateDeviance;
	}
}
