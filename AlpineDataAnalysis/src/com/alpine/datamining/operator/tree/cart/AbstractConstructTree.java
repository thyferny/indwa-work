/**
 * ClassName AbstractConstructTree
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cart;

import java.util.List;
import java.util.Vector;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSource;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.tree.threshold.BuildLeaf;
import com.alpine.datamining.operator.tree.threshold.DBBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.IBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.SizeLimitStop;
import com.alpine.datamining.operator.tree.threshold.Stop;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.tools.StringHandler;

/**
 * Build a tree from an data set.
 * This class is used for data in database.
 */
public abstract class AbstractConstructTree {
	
	/* for data not loaded in memory */
	
	protected Stop minLeafSizeTerminator;

	protected List<Stop> otherTerminators;

	protected int minSizeForSplit = 2;
	
	protected Standard criterion;

	protected IBuildLeaf leafCreator = new DBBuildLeaf();
	
	protected int numberOfPrepruningAlternatives = 0;
	
	protected boolean usePrePruning = true;

	protected List<Stop> otherTerminatorsMem;

	protected Standard criterionMem;


	protected IBuildLeaf leafCreatorMem = new BuildLeaf();
	
	protected int numberOfPrepruningAlternativesMem = 0;
	
	protected boolean usePrePruningMem = true;

	protected int sizeThreshold = 1000;
	
	protected boolean numericalLabel = false;
	protected boolean isUseChiSquare = false;
	protected DatabaseConnection databaseConnection;

	public AbstractConstructTree(Standard criterion,
			Standard criterionMem,
			List<Stop> terminationCriteria, 
			List<Stop> terminationCriteriaMem, 
			IBuildLeaf leafCreator,
			IBuildLeaf leafCreatorMem,
			boolean noPrePruning,
			int numberOfPrepruningAlternatives,
			int minSizeForSplit,
			int minLeafSize,
			int sizeThreshold) {
		
		this.minLeafSizeTerminator = new SizeLimitStop(minLeafSize);
		this.otherTerminators = terminationCriteria;
		this.otherTerminators.add(this.minLeafSizeTerminator);

		this.otherTerminatorsMem = terminationCriteriaMem;
		this.otherTerminatorsMem.add(this.minLeafSizeTerminator);

		this.usePrePruning = !noPrePruning;
		this.numberOfPrepruningAlternatives = Math.max(0, numberOfPrepruningAlternatives);
		this.minSizeForSplit = minSizeForSplit;
		
		this.leafCreator = leafCreator;
		this.leafCreatorMem = leafCreatorMem;
		this.criterion = criterion;
		this.criterionMem = criterionMem;
		this.sizeThreshold = sizeThreshold;
		
		
	}
	
	
	public AbstractConstructTree(Standard criterion,
			Standard criterionMem,
			List<Stop> terminationCriteria, 
			List<Stop> terminationCriteriaMem, 
			IBuildLeaf leafCreator,
			IBuildLeaf leafCreatorMem,
			boolean noPrePruning,
			int numberOfPrepruningAlternatives,
			int minSizeForSplit,
			int minLeafSize,
			int sizeThreshold,
			boolean isChiSquare) {
		
		this.minLeafSizeTerminator = new SizeLimitStop(minLeafSize);
		this.otherTerminators = terminationCriteria;
		this.otherTerminators.add(this.minLeafSizeTerminator);

		this.otherTerminatorsMem = terminationCriteriaMem;
		this.otherTerminatorsMem.add(this.minLeafSizeTerminator);

		this.usePrePruning = !noPrePruning;
		this.numberOfPrepruningAlternatives = Math.max(0, numberOfPrepruningAlternatives);
		this.minSizeForSplit = minSizeForSplit;
		
		this.leafCreator = leafCreator;
		this.leafCreatorMem = leafCreatorMem;
		this.criterion = criterion;
		this.criterionMem = criterionMem;
		this.sizeThreshold = sizeThreshold;
		this.isUseChiSquare = isChiSquare;
		
	}
	
	

	
	protected abstract Tree trainDT(DataSet dataSet) throws OperatorException;

	/** This method calculates the benefit of the given column. This implementation
	 *  utilizes the defined {@link Standard}. Subclasses might want to override this
	 *  method in order to calculate the benefit in other ways. */
	public Score calculateBenefit(DataSet trainingSet, Column column, boolean loadData) throws OperatorException {	
		Standard criterionLocal = null;
		if (loadData)
		{
			criterionLocal = criterionMem;
		}
		else
		{
			criterionLocal = criterion;
		}
		if (column.isNominal()) {//InfoGainCriterionDB

				double bestBenefit = criterionLocal.getNominalStandard(trainingSet, column);
				if (!Double.isNaN(bestBenefit))
				{
					Score benefit = new Score(bestBenefit, column);
					benefit.setValues(criterionLocal.getBestValues());
					return benefit;
				}
				else
				{
					return null;
				}

		} 
		else 
		{

			double bestBenefit = criterionLocal.getNumericalStandard(trainingSet, column, 0);
			if (!Double.isNaN(bestBenefit))
			{
				Score benefit = new Score(bestBenefit, column, criterionLocal.getBestSplit());
				return benefit ;
			}
			else
			{
				return null;
			}
		}
	}
	
	protected boolean shouldStop(DataSet dataSet, int depth, boolean loadData) throws OperatorException {
		if (usePrePruning && (dataSet.size() < minSizeForSplit)) {
			return true;
		} else {
			List<Stop> terminators = null;
			if (loadData)
			{
				terminators = otherTerminatorsMem;
			}
			else
			{
				terminators = otherTerminators;
			}
			for (Stop terminator : terminators) {
				if (terminator.shouldStop(dataSet, depth))
					return true;
			}
			return false;
		}
	}

	protected Vector<Score> calculateAllScores(DataSet trainingSet, boolean loadData) throws OperatorException {
		Vector<Score> benefits = new Vector<Score>();
		for (Column column : trainingSet.getColumns()) {
//			if(!list.contains((String)column.getName()))continue;
			if (column.isNominal() && column.getMapping().size() <= 1)
			{
				continue;
			}
			Score currentBenefit = calculateBenefit(trainingSet, column, loadData);			
			if (currentBenefit != null) {
				benefits.add(currentBenefit);
			}
		}
		return benefits;
	}
	
	protected abstract void constTree(Tree current, DataSet dataSet, int depth) throws OperatorException;


	/**
	 * @param dataSet
	 * @param newdataet
	 * @return
	 * @throws OperatorException
	 */
	protected DataSet getDataSetMem(DataSet dataSet
	) throws OperatorException {
			DataSet newDataSet = null;
//	        String selectSQL = ((DBTable)dataSet.getdataTable()).getSQL();
	        String whereCondition = ((DBTable)dataSet.getDBTable()).getWhereCondition();
	        String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
	        String url = ((DBTable)dataSet.getDBTable()).getUrl();
	        String userName = ((DBTable)dataSet.getDBTable()).getUserName();
	        String password = ((DBTable)dataSet.getDBTable()).getPassword();
	        String labelColumnName  = dataSet.getColumns().getLabel().getName();
	        String columnArray = "";
	        boolean first = true;
	        for (Column column: dataSet.getColumns())
	        {
	        	if (!first)
	        	{
	        		columnArray += ",";
	        	}
	        	else
	        	{
	        		first = false;
	        	}
	        	columnArray += StringHandler.doubleQ(column.getName());
	        }
	        columnArray += ","+(StringHandler.doubleQ(labelColumnName));

	        StringBuilder selectSQL=new StringBuilder("select ");
	        selectSQL.append(columnArray).append(" from ").append(tableName);
	    	if (whereCondition != null && whereCondition.length() != 0)
	    	{
	    		selectSQL.append(" where ").append(whereCondition);
	    	}

			DatabaseSource operator = null;
			try {
				operator = OperatorUtil.createOperator(DatabaseSource.class);
				DatabaseSourceParameter parameter = new DatabaseSourceParameter();
				parameter.setWorkOnDatabase(false);
				parameter.setQuery(selectSQL.toString());
				parameter.setDatabaseSystem(((DBTable)dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());
				parameter.setUrl(url);
				parameter.setUsername(userName);
				parameter.setPassword(password);
				parameter.setLabel(labelColumnName);
				operator.setParameter(parameter);

			} catch (OperatorException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
//			Container result = operator.apply(new Container());
//			try {
//				newdataSet = result.get(DataSet.class);
//			} catch (IncorrectObjectException e) {
//				e.printStackTrace();
//				throw new OperatorException(e.getLocalizedMessage());
//			}
			newDataSet = operator.createDataSetUsingExitingDBHandler(
					databaseConnection, false, null);
			return newDataSet;
}
	/**
	 * @param dataSet
	 */
	protected boolean needLoadData(DataSet dataSet) {
		boolean loadData = false;
		if (dataSet.size() * dataSet.getColumns().size() < sizeThreshold)
		{
			loadData = true;
		}
		else
		{
			loadData = false;
		}
		return loadData;
	}


}
