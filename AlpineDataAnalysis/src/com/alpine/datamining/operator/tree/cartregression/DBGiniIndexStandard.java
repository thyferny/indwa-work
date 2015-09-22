/**
 * ClassName DBGiniIndexStandard
 *
 * Version information: 1.00
 *
 * Data: 2010-5-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartregression;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.CommonUtility;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.tree.cart.AbstractStandard;
import com.alpine.datamining.operator.tree.cart.Combination;
import com.alpine.datamining.utility.DBPortingFactory;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * Calculates the variance for the given split.
 * 
 */
public class DBGiniIndexStandard extends AbstractStandard {
    private static Logger itsLogger= Logger.getLogger(DBGiniIndexStandard.class);
    private List<String> bestValues = null;
	private double bestSplit = Double.NaN;
	private long countAll=0;
	private INumericalSql cartdb;
	private ISqlGeneratorMultiDB sqlGeneratorMultiDB;
	public double getNominalStandard(DataSet dataSet,
			Column column) throws OperatorException {
		sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());

		int numberOfValues = column.getMapping().size();

		double bestImpurityReduction = Double.NaN;

		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String whereCondition = ((DBTable) dataSet
				.getDBTable()).getWhereCondition();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
    	if (whereCondition == null || whereCondition.length() == 0)
    	{
    		whereCondition = "";
    	}
    	else
    	{
    		whereCondition = " where "+whereCondition;
    	}
		String labelColumnName = StringHandler.doubleQ(dataSet.getColumns().getLabel().getName())+"*1.0";

		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		int[] bestCombin = new int[numberOfValues];
		int[] combin = new int[numberOfValues];
		for (int m = 1; m <= numberOfValues / 2; m++) {
			Combination.initCombin(combin, m, numberOfValues);
			bestImpurityReduction = calculateImpurity(databaseConnection,column,
					bestImpurityReduction, whereCondition, tableName,
					labelColumnName,  st, combin, bestCombin, m);
			if (countAll == 0)
			{
				return bestImpurityReduction;
			}

			int position = 0;
			while (true) {
				if (Combination.firstOne(combin) == (numberOfValues - m))
					break;
				position = Combination.firstOneZero(combin);
				Combination.swap(combin, position);
				Combination.ifNeedMove(combin, position);
				bestImpurityReduction = calculateImpurity(databaseConnection,column,
						bestImpurityReduction, whereCondition, tableName,
						labelColumnName,  st, combin, bestCombin, m);			
				if (countAll == 0)
				{
					return bestImpurityReduction;
				}
			}
		}
		bestValues = new ArrayList<String>();
		for (int i = 0; i < bestCombin.length; i++) {
			if (bestCombin[i] != 1)
				continue;
			{
				bestValues.add(column.getMapping().mapIndex(i));
			}
		}
		try {
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		return bestImpurityReduction;

	}

	private double calculateImpurity(DatabaseConnection databaseConnection,Column column,
			double bestImpurityReduction, String whereCondition,
			String tableName, String labelColumnName, 
			Statement st, int[] combin, int[] bestCombin, int m) throws OperatorException {
		cartdb=DBPortingFactory.createCartRegressionDB(databaseConnection.getProperties().getName());

		sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());

		StringBuffer whereEqual = null;
		StringBuffer whereNon = null;
		whereEqual = new StringBuffer();
		whereNon = new StringBuffer();
		String columnName=StringHandler.doubleQ(column.getName());

		for (int i = 0, addflag = 0, quitflag = 0; i < combin.length; i++) {
			if (quitflag == m)
				break;
			if (combin[i] != 1)
				continue;
			whereEqual.append(columnName).append("=")
			.append(CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, StringHandler.escQ(column.getMapping().mapIndex(i))));
			whereNon.append(columnName).append("!=")
			.append(CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, StringHandler.escQ(column.getMapping().mapIndex(i))));

			if (addflag < m - 1) {
				whereEqual.append(" or ");
				whereNon.append(" and ");
				addflag++;
			}
			quitflag++;
		}
		StringBuilder sql = cartdb.getVarCountSql(whereCondition, tableName,
				labelColumnName, whereEqual, whereNon, columnName);

		countAll=0;
		double varianceAll=0;
		long countEqual=0;
		double varianceEqual=0;
		long countNon=0;
		double varianceNon=0;
		
		try {
			itsLogger.debug("GiniIndexCriterionDB.getNominalBenefit():sql="+sql);
			ResultSet rs = st.executeQuery(sql.toString());
			while(rs.next())
			{
				countAll = rs.getLong(1);
				varianceAll = rs.getDouble(2);
				countEqual = rs.getLong(3);
				varianceEqual = rs.getDouble(4);
				countNon = rs.getLong(5);
				varianceNon = rs.getDouble(6);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}

		double impurityReduction = varianceAll - varianceEqual * countEqual
				/ countAll - varianceNon * countNon / countAll;
		itsLogger.debug("impurityReduction:"+impurityReduction+" "+whereEqual);

		if (Double.isNaN(bestImpurityReduction)
				|| impurityReduction > bestImpurityReduction) {
			bestImpurityReduction = impurityReduction;
			System.arraycopy(combin, 0, bestCombin, 0, combin.length);
		}
		return bestImpurityReduction;
	}

	public double getNumericalStandard(DataSet dataSet,
			Column column, double splitValue) throws OperatorException {
		sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable) dataSet.
				getDBTable()).getDatabaseConnection().getProperties().getName());
		Column labelColumn = dataSet.getColumns().getLabel();
		String labelColumnNameWithCast = sqlGeneratorMultiDB.castToDouble(StringHandler.doubleQ(labelColumn.getName()));
		String labelColumnName=StringHandler.doubleQ(labelColumn.getName());
		String columnName = StringHandler.doubleQ(column.getName());
		// double bestSplit = Double.NaN;
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		String whereCondition = ((DBTable) dataSet
				.getDBTable()).getWhereCondition();
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		cartdb=DBPortingFactory.createCartRegressionDB(databaseConnection.getProperties().getName());
//		String varianceString=cartdb.getVarianceString();
    	if (whereCondition == null || whereCondition.length() == 0)
    	{
    		whereCondition = "";
    	}
    	else
    	{
    		whereCondition = " where "+whereCondition;
    	}
		
		Statement st = null;
		ResultSet rs = null;
		double impurityReductionValue = Double.NaN;
		double variance = 0.0;
		StringBuffer sql = cartdb.getVarianceSql(labelColumnNameWithCast, whereCondition,
				tableName);

		try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("GiniIndexCriterionDB.getNumericalBenefit():sql="+sql);
			rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				variance = rs.getDouble(1);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}

		sql = cartdb.getNumericSplitSql(labelColumnNameWithCast, labelColumnName, columnName,
				whereCondition, tableName,  variance);

		try {
			itsLogger.debug("GiniIndexCriterionDB.getNumericalBenefit():sql="+sql);
			rs = st.executeQuery(sql.toString());
			boolean limitFlag=true;
			if (rs.next()&limitFlag) {
				bestSplit = rs.getDouble(1);
				impurityReductionValue = rs.getDouble(2);
				limitFlag=false;
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return impurityReductionValue;

	}

	public double getBenefit(double[][] weightCounts) {
		// calculate information amount WITHOUT this column
		double[] classWeights = new double[weightCounts[0].length];
		for (int l = 0; l < classWeights.length; l++) {
			for (int v = 0; v < weightCounts.length; v++) {
				classWeights[l] += weightCounts[v][l];
			}
		}

		double totalClassWeight = getTotalWeight(classWeights);
		totalWeight = totalClassWeight;

		double totalEntropy = getGiniIndex(classWeights, totalClassWeight);

		double gain = 0;
		for (int v = 0; v < weightCounts.length; v++) {
			double[] partitionWeights = weightCounts[v];
			double partitionWeight = getTotalWeight(partitionWeights);
			gain += getGiniIndex(partitionWeights, partitionWeight)
					* partitionWeight / totalWeight;
		}
		return totalEntropy - gain;
	}
    /** Returns the sum of the given weights. */
    public double getTotalWeight(double[] weights) {
        double sum = 0.0d;
        for (double w : weights)
            sum += w;
        return sum;
    }
	private double getGiniIndex(double[] labelWeights, double totalWeight) {
		double sum = 0.0d;
		for (int i = 0; i < labelWeights.length; i++) {
			double frequency = labelWeights[i] / totalWeight;
			sum += frequency * frequency;
		}
		return 1.0d - sum;
	}

	public boolean supportsIncrementalCalculation() {
		return true;
	}

	public double getIncrementalStadard() {
		double totalGiniEntropy = getGiniIndex(totalLabelWeights, totalWeight);
		double gain = getGiniIndex(leftLabelWeights, leftWeight) * leftWeight
				/ totalWeight;
		gain += getGiniIndex(rightLabelWeights, rightWeight) * rightWeight
				/ totalWeight;
		return totalGiniEntropy - gain;
	}

	public List<String> getBestValues() {
		return bestValues;
	}

	public void setBestValues(List<String> bestValues) {
		this.bestValues = bestValues;
	}

	public double getBestSplit() {
		return bestSplit;
	}

	public void setBestSplit(double bestSplit) {
		this.bestSplit = bestSplit;
	}
}
