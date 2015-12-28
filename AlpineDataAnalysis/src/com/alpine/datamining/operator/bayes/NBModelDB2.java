
package com.alpine.datamining.operator.bayes;

import java.sql.Array;
import java.sql.CallableStatement;
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
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class NBModelDB2 extends NBModel{
    private static Logger itsLogger= Logger.getLogger(NBModelDB2.class);

    private static final long serialVersionUID = 603035429261419192L;
	public NBModelDB2(DataSet dataSet, boolean laplaceCorrectionEnabled, boolean calculateDeviance) throws OperatorException {
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
					tmp.append("sum(").append(weightProduct).append(" double(").append(name).append(")*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(tmp.toString());
					tmp = new StringBuffer();
					tmp.append("sum(").append(weightProduct).append(" double(").append(name).append(")*double(").append(name).append(")*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
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
			int maxColumnCount = AlpineDataAnalysisConfig.DB2_MAX_COLUMN_COUNT;
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
				itsLogger.debug("NBModelDB2.updateModel():sql="+sql);
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
					"NBModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			sql.append("update ").append(newTableName).append(
					"  set "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = row_number() over()");
			itsLogger.debug(
					"NBModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
				int n = getLabel().getMapping().size();
				sql.append("create  table ").append(tempTableName).append(
				" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint");
				for(int i = 0; i < n; i++){
					sql.append(", prediction").append(i).append(" double");
				}
				sql.append(")");
			itsLogger.debug(
					"NBModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer where = getWhere(true);

		sql = new StringBuffer();

		sql.append("call alpine_miner_nb_ca_prediction_proc('")
		.append(newTableName).append("',").append(where).append(",").append("?,?,?,?,?,?,?,?,?)");
		CallableStatement stpCall = databaseConnection.getConnection().prepareCall(sql.toString()); 
		stpCall.setArray(1, getNominalColumnNamesSqlArray(databaseConnection));
		stpCall.setArray(2, getNominalColumnsMappingCountSqlArray(databaseConnection));
		stpCall.setArray(3, getNominalColumnsMappingSqlArray(databaseConnection));
		stpCall.setArray(4, getNominalColumnsProbabilitySqlArray(databaseConnection));
		stpCall.setArray(5, getDependentColumnMappingSqlArray(databaseConnection));
		stpCall.setArray(6, getDependentColumnProbabilitySqlArray(databaseConnection));
		stpCall.setArray(7, getNumericalColumnsSqlArray(databaseConnection));
		stpCall.setArray(8, getNumericalColumnsProbabilitySqlArray(databaseConnection));
		stpCall.setString(9, tempTableName);

		itsLogger.debug("NBModelDB2.performPrediction():sql="+sql);
		stpCall.execute();
		stpCall.close();
			StringBuffer sqltemp = new StringBuffer();
			StringBuffer set = new StringBuffer();
			StringBuffer selectSet = new StringBuffer();

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
			sqltemp.append("update ").append(newTableName).append(" set ( ")
			.append(set)
			.append(" ) = (select ").append(selectSet).append(
					" from ").append(tempTableName).append(
					" where  ").append(tempTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTableName).append(
					"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+")");

			itsLogger.debug(
							"NBModellDB2.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(tempTableName);
			itsLogger.debug(
					"NBModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("alter table ").append(newTableName).append(
					" drop column "+AlpineDataAnalysisConfig.ALPINE_MINER_ID);
			itsLogger.debug(
					"NBModellDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
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
			CallableStatement stpCall = databaseConnection.getConnection().prepareCall(devianceSQL.toString()); 
			stpCall.setArray(1, getNominalColumnNamesSqlArray(databaseConnection));
			stpCall.setArray(2, getNominalColumnsMappingCountSqlArray(databaseConnection));
			stpCall.setArray(3, getNominalColumnsMappingSqlArray(databaseConnection));
			stpCall.setArray(4, getNominalColumnsProbabilitySqlArray(databaseConnection));
			stpCall.setString(5, StringHandler.escQ(toChar(getLabel()).toString()));
			stpCall.setArray(6, getDependentColumnMappingSqlArray(databaseConnection));
			stpCall.setArray(7, getDependentColumnProbabilitySqlArray(databaseConnection));
			stpCall.setArray(8, getNumericalColumnsSqlArray(databaseConnection));
			stpCall.setArray(9, getNumericalColumnsProbabilitySqlArray(databaseConnection));
			stpCall.registerOutParameter(10, java.sql.Types.DOUBLE);

			itsLogger.debug("NBModelDB2.caculateDeviance():sql="+devianceSQL);
			stpCall.execute();
			deviance = stpCall.getDouble(10);
			stpCall.close();
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
			itsLogger.debug("NBModelDB2.caculateDeviance():sql="+countSQL);
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
		sb.append(" (case when ").append(StringHandler.doubleQ(column.getName())).append(" is null then '").append((ALPINE_MINER_CATEGORY_NULL)).append("' else ");
		sb.append(toChar(column)).append(" end)");
		return sb;
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
	protected Array getNumericalColumnsProbabilitySqlArray(DatabaseConnection databaseConnection) throws SQLException  {
		Double [] prob = new Double[0];
		ArrayList<Double> array = new ArrayList<Double>();
		int j = 0;
		for (Column column : getTrainingHeader().getColumns()) {
			if (column.isNumerical()) {
				for (int i = 0; i < numberOfClasses; i++) {
					array.add((distributionProperties[j][i][INDEX_MEAN]));
					array.add((distributionProperties[j][i][INDEX_STANDARD_DEVIATION]));
					array.add((distributionProperties[j][i][INDEX_LOG_FACTOR]));
				}
			}
			j++;
		}
		prob = array.toArray(prob);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("DOUBLE", prob);
		return sqlArray;
	}

	protected Array getNumericalColumnsSqlArray(DatabaseConnection databaseConnection) throws SQLException {
		String [] columns = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNumerical())
			{
				array.add(StringHandler.escQ(StringHandler.doubleQ(column.getName())));
			}
		}
		columns = array.toArray(columns);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		return sqlArray;
	}

	protected Array getDependentColumnProbabilitySqlArray(DatabaseConnection databaseConnection) throws SQLException {
		Double[] prob = new Double[0];
		ArrayList<Double> array = new ArrayList<Double>();
		for (int i = 0; i < numberOfClasses; i++) {
			array.add(priors[i]);
		}
		prob = array.toArray(prob);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("DOUBLE", prob);
		return sqlArray;
	}

	protected Array getDependentColumnMappingSqlArray(DatabaseConnection databaseConnection) throws SQLException{
		String [] mapping = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		Column label = getLabel();
		for (int i = 0; i < label.getMapping().size(); i++) {
			array.add(StringHandler.escQ(label.getMapping().mapIndex(i)));
		}
		mapping = array.toArray(mapping);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("VARCHAR", mapping);
		return sqlArray;

	}

	protected Array getNominalColumnsProbabilitySqlArray(DatabaseConnection databaseConnection) throws SQLException{
		Double[] prob = new Double[0];
		ArrayList<Double> array = new ArrayList<Double>();
		int j = 0;
		for (Column column : getTrainingHeader().getColumns()) {
			if (column.isNominal()) {
				for (int i = 0; i < numberOfClasses; i++) {
					for (int intValue = 0; intValue < column.getMapping()
							.size(); intValue++) {
						array.add((distributionProperties[j][i][intValue]));
					}
					array.add((distributionProperties[j][i][column.getMapping().size()]));
				}
			}
			j++;
		}
		prob = array.toArray(prob);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("DOUBLE", prob);
		return sqlArray;
	}

	protected Array getNominalColumnsMappingSqlArray(DatabaseConnection databaseConnection) throws SQLException  {
		String[] columns = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				for(int i = 0; i < column.getMapping().size(); i++)
				{
					array.add(StringHandler.escQ(column.getMapping().mapIndex(i)));
				}
				array.add(StringHandler.escQ(ALPINE_MINER_CATEGORY_NULL));
			}
		}
		columns = array.toArray(columns);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("VARCHAR", columns);
		return sqlArray;
	}

	protected Array getNominalColumnsMappingCountSqlArray(DatabaseConnection databaseConnection) throws SQLException  {
		Integer[] countArray = null;
		ArrayList<Integer> array = new ArrayList<Integer>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				array.add((column.getMapping().size()+1));
			}
		}
		countArray = array.toArray(new Integer[0]);
		Array sqlArray =databaseConnection.getConnection().createArrayOf("INTEGER", countArray);
		return sqlArray;
	}

	protected Array getNominalColumnNamesSqlArray(DatabaseConnection databaseConnection) throws SQLException {
		String[] nominalColumnNames = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				array.add((getNominalColumnName(column).toString()));
				
			}
		}
		nominalColumnNames = array.toArray(nominalColumnNames);
		Array columnArray =databaseConnection.getConnection().createArrayOf("VARCHAR", nominalColumnNames);
		return columnArray;
	}

	protected StringBuffer getDevianceFunction(Column label, String tableName) {
			StringBuffer where = getWhere(true);
			StringBuffer devianceSQL = new StringBuffer();
			devianceSQL.append("call alpine_miner_nb_ca_deviance_proc('")
				.append(tableName).append("',").append(where).append(",").append("?,?,?,?,?,?,?,?,?,?)");
			return devianceSQL;
		}
	protected StringBuffer getWhere(boolean needDependentColumn)
	{
			StringBuffer sb = new StringBuffer("' where ");
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
			sb.append(" '");
			if (first)
			{
				return new StringBuffer("' '");
			}
			return sb;
	}
}
