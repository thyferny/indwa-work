
package com.alpine.datamining.operator.bayes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.Mapping;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;



public class NBModelNetezza extends NBModel{
    private static Logger itsLogger= Logger.getLogger(NBModelNetezza.class);

    private String nominalColumnsTableName;
	private String nominalMappingCountTableName;
	private String nominalColumnsMappingTableName;
	private String nominalColumnsProTableName;
	private String dependentColumnMappingTableName;
	private String dependentColumnProTableName;
	private String numericalColumnsTableName;
	private String numericalColumnsProTableName;
	
	private static final long serialVersionUID = 2184046417856337653L;
	
	private void initTable(Statement st, boolean predict) throws OperatorException{
		long currentTime = System.currentTimeMillis();
		nominalColumnsTableName = "N" + currentTime;
		nominalMappingCountTableName = "NMC" + currentTime;
		nominalColumnsMappingTableName = "NM" + currentTime;
		nominalColumnsProTableName = "NP" + currentTime;
		if(!predict){
			dependentColumnMappingTableName = "DM" + currentTime;
		}
		dependentColumnProTableName = "DP" + currentTime;
		numericalColumnsTableName = "NU" + currentTime;
		numericalColumnsProTableName = "NUP" + currentTime;
		TableTransferParameter.createStringTable(nominalColumnsTableName,st);
		TableTransferParameter.createDoubleTable(nominalMappingCountTableName,st);
		TableTransferParameter.createStringTable(nominalColumnsMappingTableName,st);
		TableTransferParameter.createDoubleTable(nominalColumnsProTableName,st);
		if(!predict){
			TableTransferParameter.createStringTable(dependentColumnMappingTableName,st);
		}
		TableTransferParameter.createDoubleTable(dependentColumnProTableName,st);
		TableTransferParameter.createStringTable(numericalColumnsTableName,st);
		TableTransferParameter.createDoubleTable(numericalColumnsProTableName,st);
	}
	protected void dropTable(Statement st, boolean predict) throws OperatorException {
		TableTransferParameter.dropResultTable(nominalColumnsTableName,st);
		TableTransferParameter.dropResultTable(nominalMappingCountTableName,st);
		TableTransferParameter.dropResultTable(nominalColumnsMappingTableName,st);
		TableTransferParameter.dropResultTable(nominalColumnsProTableName,st);
		if(!predict){
			TableTransferParameter.dropResultTable(dependentColumnMappingTableName,st);
		}
		TableTransferParameter.dropResultTable(dependentColumnProTableName,st);
		TableTransferParameter.dropResultTable(numericalColumnsTableName,st);
		TableTransferParameter.dropResultTable(numericalColumnsProTableName,st);
	}

	public NBModelNetezza(DataSet dataSet, boolean laplaceCorrectionEnabled, boolean calculateDeviance) throws OperatorException {
		super(dataSet,laplaceCorrectionEnabled,  calculateDeviance);
	}
	

	public void getWeight(DataSet dataSet) throws OperatorException {

		String weight = "1.0";
		String weightProduct = "1.0 * ";
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
					tmp.append("sum(").append(weightProduct).append(name).append("::double*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
					sumString.add(tmp.toString());
					tmp = new StringBuffer();
					tmp.append("sum(").append(weightProduct).append(name).append("::double*").append(name).append("::double*(case when ").append(labeName).append("=").append(labelValue).append(" then 1 else 0 end))");
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
			int maxColumnCount = AlpineDataAnalysisConfig.NZ_MAX_COLUMN_COUNT;
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
				itsLogger.debug("NBModelNetezza.updateModel():sql="+sql);
				rs = st.executeQuery(sql.toString());
				while(rs.next()){
					for ( int j = 0 ; j < count; j++)
					{
						sumDouble.add(rs.getDouble(j+1));
					}
				}
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
		}catch(SQLException e)
		{
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}finally{
			try {
				if(rs != null){
					rs.close();
				}
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
			try {
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
	}

	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		Statement st = null;
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		String newTableName = ((DBTable) dataSet.getDBTable()).getTableName();
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		StringBuffer sql = new StringBuffer();
		String newTempTableName = "NT" + System.currentTimeMillis();
		String resultTableName = "R" + System.currentTimeMillis();

		try {
			initTable(st, true);

			TableTransferParameter.insertTable(nominalColumnsTableName, st,
					getNominalColumnNamesArrayNZ());
			TableTransferParameter.insertTable(nominalMappingCountTableName,
					st, getNominalColumnsMappingCountArrayNZ());
			TableTransferParameter.insertTable(nominalColumnsMappingTableName,
					st, getNominalColumnsMappingArrayNZ());
			TableTransferParameter.insertTable(nominalColumnsProTableName, st,
					getNominalColumnsProbabilityArrayNZ());
			TableTransferParameter.insertTable(dependentColumnProTableName, st,
					getDependentColumnProbabilityArrayNZ());
			TableTransferParameter.insertTable(numericalColumnsTableName, st,
					getNumericalColumnsArrayNZ());
			TableTransferParameter.insertTable(numericalColumnsProTableName,
					st, getNumericalColumnsProbabilityArrayNZ());

			sql
					.append("create table ")
					.append(newTempTableName)
					.append(
							" as select *, row_number() over(order by 1) as  "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" from ")
					.append(newTableName);
			itsLogger.debug(
					"NBModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			int n = getLabel().getMapping().size();
			sql.append("create  table ").append(resultTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint");
			for (int i = 0; i < n; i++) {
				sql.append(", prediction").append(i).append(" double");
			}
			sql.append(")");
			itsLogger.debug(
					"NBModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			sql.append("call alpine_miner_nb_ca_predict_proc('");
			StringBuffer where = getWhere(false);
			sql.append(newTempTableName).append("',");
			sql.append(where).append(",'");
			sql.append(nominalColumnsTableName).append("','");
			sql.append(nominalMappingCountTableName).append("','");
			sql.append(nominalColumnsMappingTableName).append("','");
			sql.append(nominalColumnsProTableName).append("','");
			sql.append(dependentColumnProTableName).append("','");
			sql.append(numericalColumnsTableName).append("','");
			sql.append(numericalColumnsProTableName).append("','");
			sql.append(resultTableName).append("')");

			itsLogger.debug(
					"NBModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer sqltemp = new StringBuffer();
			sqltemp.append("update ").append(newTempTableName).append(" set  ");

			int numberOfClasses = getLabel().getMapping().size();
			updateColumns.add(predictedLabel.getName());
			for (int c = 0; c < numberOfClasses; c++) {
				if (c != 0) {
					sqltemp.append(",");
				}
				sqltemp.append(StringHandler.doubleQ(dataSet.getColumns()
						.getSpecial(
								Column.CONFIDENCE_NAME + "_"
										+ getLabel().getMapping().mapIndex(c))
						.getName()));
				sqltemp.append(" = ");
				sqltemp.append(resultTableName).append(".prediction").append(c);
				updateColumns.add(dataSet.getColumns().getSpecial(
						Column.CONFIDENCE_NAME + "_"
								+ getLabel().getMapping().mapIndex(c))
						.getName());
			}
			sqltemp.append(",").append(
					StringHandler.doubleQ(predictedLabel.getName()));
			StringBuffer[] biggerSql = new StringBuffer[numberOfClasses - 1];
			for (int c = 0; c < numberOfClasses - 1; c++) {
				biggerSql[c] = new StringBuffer("(");
				boolean first = true;
				for (int j = c + 1; j < numberOfClasses; j++) {
					if (!first) {
						biggerSql[c].append(" and ");
					} else {
						first = false;
					}
					biggerSql[c].append(resultTableName).append(".prediction")
							.append(c).append(">=").append(resultTableName)
							.append(".prediction").append(j);
				}
				biggerSql[c].append(")");
			}
			StringBuffer caseSql = new StringBuffer();
			caseSql.append(" (case ");
			for (int c = 0; c < numberOfClasses - 1; c++) {
				caseSql.append(" when ").append(biggerSql[c]).append(" then '")
						.append(
								StringHandler.escQ(getLabel().getMapping()
										.mapIndex(c))).append("'");
			}
			caseSql.append(" else '").append(
					StringHandler.escQ(getLabel().getMapping().mapIndex(
							numberOfClasses - 1))).append("' end)");

			sqltemp.append(" = ").append(caseSql);
			sqltemp.append(" from ").append(resultTableName).append(" where  ")
					.append(resultTableName).append("."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ")
					.append(newTempTableName).append("."+AlpineDataAnalysisConfig.ALPINE_MINER_ID);

			itsLogger.debug(
					"NBModelNetezza.performPrediction():sql=" + sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTableName);
			itsLogger.debug(
					"NBModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			StringBuffer columnSql = new StringBuffer();
			Iterator<Column> allColumns = dataSet.getColumns().allColumns();
			boolean first = true;
			while (allColumns.hasNext()) {
				Column column = allColumns.next();
				if (!column.getName().equals(AlpineDataAnalysisConfig.ALPINE_MINER_ID)) {
					if (first) {
						first = false;
					} else {
						columnSql.append(",");
					}
					columnSql.append(StringHandler.doubleQ(column.getName()));
				}
			}
			sql.append("create table ").append(newTableName).append(
					" as select ").append(columnSql).append(" from ").append(
					newTempTableName);
			itsLogger.debug(
					"NBModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			TableTransferParameter.dropResultTable(newTempTableName,st);
			TableTransferParameter.dropResultTable(resultTableName,st);
			dropTable(st, true);
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
		initTable(st, false);

		TableTransferParameter.insertTable(nominalColumnsTableName, st, getNominalColumnNamesArrayNZ());
		TableTransferParameter.insertTable(nominalMappingCountTableName, st, getNominalColumnsMappingCountArrayNZ());
		TableTransferParameter.insertTable(nominalColumnsMappingTableName, st, getNominalColumnsMappingArrayNZ());
		TableTransferParameter.insertTable(nominalColumnsProTableName, st, getNominalColumnsProbabilityArrayNZ());
		TableTransferParameter.insertTable(dependentColumnMappingTableName, st, getDependentColumnMappingArrayNZ());
		TableTransferParameter.insertTable(dependentColumnProTableName, st, getDependentColumnProbabilityArrayNZ());
		TableTransferParameter.insertTable(numericalColumnsTableName, st, getNumericalColumnsArrayNZ());
		TableTransferParameter.insertTable(numericalColumnsProTableName, st, getNumericalColumnsProbabilityArrayNZ());

		StringBuffer devianceSQL = null;
		devianceSQL = getDevianceFunction(label, tableName);
		try {
			itsLogger.debug("NBModelNetezza.caculateDeviance():sql="+devianceSQL);
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
			itsLogger.debug("NBModelNetezza.caculateDeviance():sql="+countSQL);
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
		dropTable(st, false);
		try {
			st.close();
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());

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
			sb.append("(").append(StringHandler.doubleQ(column.getName())).append("::varchar(2000))");
		}
		else if(column.isNumerical() && column.isCategory())
		{
			sb.append("(").append(StringHandler.doubleQ(column.getName())).append(")");
		}
		else
		{
			sb.append(StringHandler.doubleQ(column.getName()));
		}
		sb.append(" end )");
		return sb;
	}

	protected Double[] getNumericalColumnsProbabilityArrayNZ() {
		Double[] ret = new Double[0];
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
		return array.toArray(ret);
	}

	protected String[] getNumericalColumnsArrayNZ() {
		String[] ret = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNumerical())
			{
				array.add(StringHandler.escQ(StringHandler.doubleQ(column.getName())));
			}
		}
		return array.toArray(ret);
	}

	protected Double[] getDependentColumnProbabilityArrayNZ() {
		Double[] ret = new Double[0];
		ArrayList<Double> array = new ArrayList<Double>();
		for (int i = 0; i < numberOfClasses; i++) {
			array.add((priors[i]));
		}
		return array.toArray(ret);
	}

	protected String[] getDependentColumnMappingArrayNZ() {
		String[] ret = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		Column label = getLabel();
		for (int i = 0; i < label.getMapping().size(); i++) {
			array.add(label.getMapping().mapIndex(i));
		}
		return array.toArray(ret);
	}

	protected Double[] getNominalColumnsProbabilityArrayNZ() {
		Double[] ret = new Double[0];
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
		return array.toArray(ret);
	}

	protected String[] getNominalColumnsMappingArrayNZ() {
		String[] ret = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				for(int i = 0; i < column.getMapping().size(); i++)
				{
					array.add((column.getMapping().mapIndex(i)));
				}
				array.add((ALPINE_MINER_CATEGORY_NULL));
			}
		}
		return array.toArray(ret);
	}

	protected Integer[] getNominalColumnsMappingCountArrayNZ() {
		Integer[] ret = new Integer[0];
		ArrayList<Integer> array = new ArrayList<Integer>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				array.add((column.getMapping().size()+1));
			}
		}
		return array.toArray(ret);
		}

	private String[] getNominalColumnNamesArrayNZ() {
		String[] ret = new String[0];
		ArrayList<String> array = new ArrayList<String>();
		DataSet dataSet = getTrainingHeader();
		for (Column column: dataSet.getColumns())
		{
			if (column.isNominal())
			{
				array.add(StringHandler.escQ(getNominalColumnName(column).toString()));
				
			}
		}
		return array.toArray(ret);
	}
	protected StringBuffer getDevianceFunction(Column label, String tableName) {
		StringBuffer where = getWhere(true);
		StringBuffer devianceSQL = new StringBuffer();
		devianceSQL.append("call alpine_miner_nb_ca_deviance_proc('");
		devianceSQL.append(StringHandler.escQ(tableName)).append("',");
		devianceSQL.append(where).append(",'");
		devianceSQL.append(nominalColumnsTableName).append("','");
		devianceSQL.append(nominalMappingCountTableName).append("','");
		devianceSQL.append(nominalColumnsMappingTableName).append("','");
		devianceSQL.append(nominalColumnsProTableName).append("','");
		devianceSQL.append(StringHandler.escQ(StringHandler.doubleQ(label.getName()))).append("','");
		devianceSQL.append(dependentColumnMappingTableName).append("','");
		devianceSQL.append(dependentColumnProTableName).append("','");
		devianceSQL.append(numericalColumnsTableName).append("','");
		devianceSQL.append(numericalColumnsProTableName).append("')");
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
			sb.append(StringHandler.escQ(StringHandler.doubleQ(column.getName()))).append(" is not null ");
		}
		if (needDependentColumn)
		{
			if (first) {
				first = false;
			} else {
				sb.append(" and ");
			}
			sb.append(StringHandler.escQ(StringHandler.doubleQ(getLabel().getName()))).append(" is not null ");
		}
		sb.append(" '");
		if (first)
		{
			return new StringBuffer("' '");
		}
		return sb;
}	
}
