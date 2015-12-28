
package com.alpine.datamining.operator.bayes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.Mapping;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class NBModelOracle extends NBModel{


	
	private static final long serialVersionUID = 2184046417856337653L;
    private static Logger itsLogger= Logger.getLogger(NBModelOracle.class);


    public NBModelOracle(DataSet dataSet, boolean laplaceCorrectionEnabled, boolean calculateDeviance) throws OperatorException {
		super(dataSet,laplaceCorrectionEnabled,  calculateDeviance);
	}
	

	public void getWeight(DataSet dataSet) throws OperatorException {

		String weight = "1.0";
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
							nominalstr = CommonUtility.quoteValue(dataSourceInfo.getDBType(),column,nominalstr);
							caseString.append("sum( ").append(weightProduct).append("(case when ").append(name).append("=").append(nominalstr).append(" and ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
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
					tmp.append("sum(").append(weightProduct).append(name).append("*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(tmp.toString());
					tmp = new StringBuffer();
					tmp.append("sum(").append(weightProduct).append(name).append("*").append(name).append("*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
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
			int maxColumnCount = 1000;
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
				itsLogger.debug("NBModelOracle.updateModel():sql="+sql);
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


	public DataSet performPrediction(DataSet dataSet, Column predictedLabel) throws OperatorException{
		Statement st = null;
    	DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		predictionOracle(dataSet, predictedLabel, st, tableName);
		return dataSet;
	}
	

private void predictionOracle(DataSet dataSet, Column predictedLabel,
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

	StringBuffer confidenceArray = new StringBuffer();
	StringBuffer confidenceColumnArray = new StringBuffer("floatarray(");
	confidenceArray.append(" alpine_miner_nb_ca_confidence(")
	.append(nominalColumnNamesArray).append(",").append(nominalColumnsMappingCount).append(",")
	.append(nominalColumnsMapping).append(",").append(nominalColumnsProbability).append(",").append(dependentColumnMapping).append(",")
	.append(dependentColumnProbability).append(",").append(numericalColumns).append(",")
	.append(numericalColumnsProbability).append(") ");

	updateColumns.add(predictedLabel.getName());
	updateSQL = new StringBuffer();

	for(int i = 0; i < numberOfClasses; i++)
	{
		if (i != 0)
		{
			confidenceColumnArray.append(",");
		}
		updateColumns.add(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName());
		confidenceColumnArray.append(StringHandler.doubleQ(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName()));
		updateSQL.setLength(0);
		updateSQL.append("update ").append(tableName).append(" set ").append("\"").append(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + classValues[i]).getName()).append("\"").append(" = ").append("alpine_miner_get_fa_element(").append(confidenceArray).append(",").append(i+1).append(")").append(where);
		itsLogger.debug("NBModelOracle.predictionOracleFunction():sql="+updateSQL);
		try {
			st.execute(updateSQL.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}

	}
	confidenceColumnArray.append(")");
	updateSQL.setLength(0);
	updateSQL.append("update ").append(tableName).append(" set ").append(StringHandler.doubleQ(predictedLabel.getName())).append(" = alpine_miner_nb_ca_prediction(").append(confidenceColumnArray).append(",").append(getDependentColumnMapping()).append(")").append(where);
	try {
		itsLogger.debug("NBModelOracle.predictionOracleFunction():sql="+updateSQL);
		st.execute(updateSQL.toString());
	} catch (SQLException e) {
		e.printStackTrace();
		itsLogger.error(e.getMessage(),e);
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
		devianceSQL = getDevianceFunction(label, tableName);
		try {
			itsLogger.debug("NBModelOracle.caculateDeviance():sql="+devianceSQL);
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
			itsLogger.debug("NBModelOracle.caculateDeviance():sql="+countSQL);
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


	protected StringBuffer getNominalColumnName(Column column) {
		StringBuffer sb = new StringBuffer();
		sb.append(" (case when ").append(StringHandler.doubleQ(column.getName())).append(" is null then '").append(StringHandler.escQ(ALPINE_MINER_CATEGORY_NULL)).append("' else ");
		if (column.getValueType() == DataType.BOOLEAN)
		{
			sb.append(" (case when ").append(StringHandler.doubleQ(column.getName())).append(" is true then 't' else 'f' end)");
		}
		else if (column.getValueType() == DataType.DATE ||column.getValueType() == DataType.DATE_TIME||column.getValueType() == DataType.TIME )
		{
			sb.append("to_char(").append(StringHandler.doubleQ(column.getName())).append(")");
		}
		else if(column.isNumerical() && column.isCategory())
		{
			sb.append("to_char(").append(StringHandler.doubleQ(column.getName())).append(")");
		}
		else
		{
			sb.append(StringHandler.doubleQ(column.getName()));
		}
		sb.append(" end )");
		return sb;
	}

	protected StringBuffer getNumericalColumnsProbability() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		int j = 0;
		for (Column column : getTrainingHeader().getColumns()) {
			if (column.isNumerical()) {
				for (int i = 0; i < numberOfClasses; i++) {
					array.add(String.valueOf(distributionProperties[j][i][INDEX_MEAN]));
					array.add(String.valueOf(distributionProperties[j][i][INDEX_STANDARD_DEVIATION]));
					array.add(String.valueOf(distributionProperties[j][i][INDEX_LOG_FACTOR]));
				}
			}
			j++;
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Float));
		if (array.size() == 0) {
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNumericalColumns() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNumerical())
			{
				array.add(StringHandler.doubleQ(column.getName()));
			}
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Float));
		if (array.size() == 0) {
			return new StringBuffer("null");
		}else{
			return sb;
		}
	}

	protected StringBuffer getDependentColumnProbability() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < numberOfClasses; i++) {
			array.add(String.valueOf(priors[i]));
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Float));
		if (array.size() == 0) {
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getDependentColumnMapping() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		Column label = getLabel();
		for (int i = 0; i < label.getMapping().size(); i++) {
			array.add(CommonUtility.quoteValue(dataSourceInfo.getDBType(), label, label.getMapping().mapIndex(i)));
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Varchar2));
		if (array.size() == 0) {
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnsProbability() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		int j = 0;
		for (Column column : getTrainingHeader().getColumns()) {
			if (column.isNominal()) {
				for (int i = 0; i < numberOfClasses; i++) {
					for (int intValue = 0; intValue < column.getMapping()
							.size(); intValue++) {
						array.add(String.valueOf(distributionProperties[j][i][intValue]));
					}
					array.add(String.valueOf(distributionProperties[j][i][column.getMapping().size()]));
				}
			}
			j++;
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Float));
		if (array.size() == 0) {
			return new StringBuffer("null");
		} else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnsMapping() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				for(int i = 0; i < column.getMapping().size(); i++)
				{
					array.add("'"+StringHandler.escQ(column.getMapping().mapIndex(i))+"'");
				}
				array.add("'"+StringHandler.escQ(ALPINE_MINER_CATEGORY_NULL)+"'");
			}
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Varchar2));
		if(array.size() == 0)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnsMappingCount() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				array.add(String.valueOf(column.getMapping().size()+1));
			}
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Integer));
		if(array.size() == 0)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}

	protected StringBuffer getNominalColumnNamesArray() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				array.add(getNominalColumnName(column).toString());
				
			}
		}
		sb.append(CommonUtility.array2OracleArray(array, CommonUtility.OracleDataType.Varchar2));
		if(array.size() == 0)
		{
			return new StringBuffer("null");
		}
		else
		{
			return sb;
		}
	}
}
