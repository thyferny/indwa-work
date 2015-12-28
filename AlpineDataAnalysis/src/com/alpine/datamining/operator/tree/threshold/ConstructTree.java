
package com.alpine.datamining.operator.tree.threshold;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBDataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSource;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.db.SplitDBDataSet;
import com.alpine.datamining.db.SplitDataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.db.DataSourceInfoNZ;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.StringHandler;


public class ConstructTree {
    private static Logger itsLogger= Logger.getLogger(ConstructTree.class);

    protected Stop minLeafSizeTerminator;

	private List<Stop> otherTerminators;

	private int minSizeForSplit = 2;
	
	private Standard criterion;

	private DBNumericDevide splitter;
	
	private Prune pruner;

	protected IBuildLeaf leafCreator = new DBBuildLeaf();
	
	protected int numberOfPrepruningAlternatives = 0;
	
	protected boolean usePrePruning = true;
	

	

	private List<Stop> otherTerminatorsMem;

	private Standard criterionMem;

	private NumericDevide splitterMem;

	protected IBuildLeaf leafCreatorMem = new BuildLeaf();
	
	protected int numberOfPrepruningAlternativesMem = 0;
	
	protected boolean usePrePruningMem = true;

	private int sizeThreshold = 1000;
	
	private DatabaseConnection databaseConnection;
	
	
	public ConstructTree(Standard criterion,
			Standard criterionMem,
			List<Stop> terminationCriteria, 
			List<Stop> terminationCriteriaMem, 
			Prune pruner,
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
		this.splitter = new DBNumericDevide(this.criterion);
		this.splitterMem = new NumericDevide(this.criterionMem);
		this.pruner = pruner;
		this.sizeThreshold = sizeThreshold;
		
		
	}

	
	public Tree learnTree(DataSet dataSet) throws OperatorException {
		// grow tree
		databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		Tree root = new Tree((DataSet)dataSet.clone());
		boolean loadData = needLoadData(dataSet);
		if (loadData && dataSet instanceof DBDataSet)
		{
			// create simpledataet;
			dataSet = getDataSetMem(dataSet);
			
		}
		if (shouldStop(dataSet, 0, loadData)) {
			if (loadData)
			{
				leafCreatorMem.changeToLeaf(root, dataSet);
			}
			else
			{
				leafCreator.changeToLeaf(root, dataSet);
			}
		} else {
			constTree(root, dataSet, 1);
		}

		if (pruner != null)
			pruner.prune(root);

		return root;
	}

	
	public Score calculateScore(DataSet trainingSet, Column column, boolean loadData) throws OperatorException {	
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

				return new Score(criterionLocal.getNominalBenefit(trainingSet, column), column);

		} 
		else 
		{
			double splitValue;
			double bestBenefit;
			// numerical column
			if (loadData)
			{
				splitValue = splitterMem.getBestSplit(trainingSet, column);
				bestBenefit = splitterMem.getBestSplitScore();
			}
			else
			{
				splitValue = splitter.getBestSplit(trainingSet, column);
				bestBenefit = splitter.getBestSplitScore();
			}
			if (!Double.isNaN(splitValue))
				return new Score(bestBenefit, column, splitValue);
			else
				return null;
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
			Score currentScore = calculateScore(trainingSet, column, loadData);
			if (currentScore != null) {
				benefits.add(currentScore);
			}
		}
		return benefits;
	}
	
	protected void constTree(Tree current, DataSet dataSet, int depth) throws OperatorException {
		if(dataSet.getColumns().size() == 0){
			if (dataSet instanceof DBDataSet)
			{
				leafCreator.changeToLeaf(current, dataSet);
			}
			else
			{
				leafCreatorMem.changeToLeaf(current, dataSet);
			}
			return;
		}
		boolean loadData = needLoadData(dataSet);
		if (loadData && dataSet instanceof DBDataSet)
		{
			// create simpledataet;
			dataSet = getDataSetMem(dataSet);
		}

		if (shouldStop(dataSet, depth, loadData)) {
			if (loadData)
			{
				leafCreatorMem.changeToLeaf(current, dataSet);
			}
			else
			{
				leafCreator.changeToLeaf(current, dataSet);
			}
			return;
		}

		DataSet trainingSet = (DataSet)dataSet.clone();
		
		// calculate all benefits
		Vector<Score> scores = calculateAllScores(dataSet, loadData);

		// sort all benefits
		Collections.sort(scores);
		// try at most k benefits and check if prepruning is fulfilled
		boolean splitFound = false;
		for (int a = 0; a < numberOfPrepruningAlternatives + 1; a++) {
			// break if no benefits are left
			if (scores.size() <= 0)
				break;
			
			// search current best			
			Score bestScore = scores.remove(0);
			
			// check if minimum gain was reached
			if (usePrePruning && (bestScore.getScore() <= 0)) {
				continue;
			}

			// split by best column
			if (loadData)
			{
				// split by best column
				SplitDataSet splitted = null;
				Column bestColumn = bestScore.getColumn();
				double bestSplitValue = bestScore.getSplitValue();
				if (bestColumn.isNominal()) {
					splitted = SplitDataSet.splitByColumn(trainingSet, bestColumn);
				} else {
					splitted = SplitDataSet.splitByColumn(trainingSet, bestColumn, bestSplitValue);
				}

				// check if children all have the minimum size
				boolean splitOK = true;
				if (usePrePruning) {
					for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
						splitted.selectSingleSubset(i);
						if ((splitted.size()) > 0 && (minLeafSizeTerminator.shouldStop(splitted, depth))) {
							splitOK = false;
							break;
						}
					}
				}
				
				// if all have minimum size --> remove nominal column and recursive call for each subset
				if (splitOK) {
					if (bestColumn.isNominal()) {
						splitted.getColumns().remove(bestColumn);
					}
					for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
						splitted.selectSingleSubset(i);
						if (splitted.size() > 0) {
							Tree child = new Tree((DataSet)splitted.clone());
							DevideCond condition = null;
							if (bestColumn.isNominal()) {
								condition = new NorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, splitted.getRow(0).getValueAsString(bestColumn));
							} else {
								if (i == 0) {
									condition = new LessEqualDevideCond(bestColumn, Double.parseDouble(AlpineMath.doubleExpression(bestSplitValue)));
								} else {
									condition = new GreaterDevideCond(bestColumn, Double.parseDouble(AlpineMath.doubleExpression(bestSplitValue)));
								}
							}
							current.addChild(child, condition);
							constTree(child, splitted, depth + 1);
						}
					}
					
					// end loop
					splitFound = true;
					break;
				} else {
					continue;
				}
			}
			else
			{
				List<DataSet> splittedList = new ArrayList<DataSet>();
				Column bestColumn = bestScore.getColumn();
				double bestSplitValue = bestScore.getSplitValue();
				if (bestColumn.isNominal()) {
					splittedList = SplitDBDataSet.splitByColumn(trainingSet, bestColumn);
				} else {
					splittedList = SplitDBDataSet.splitByColumn(trainingSet, bestColumn, bestSplitValue);
				}
	
				// check if children all have the minimum size
				boolean splitOK = true;
				DataSet splitted = null;
			
				if (usePrePruning) {
					for(Iterator<DataSet> it = splittedList.iterator(); it.hasNext();){
						   
						splitted = it.next();
						if ((splitted.size()) > 0 && (minLeafSizeTerminator.shouldStop(splitted, depth))) {
							splitOK = false;
							break;
						}
					}
				}
				
				// if all have minimum size --> remove nominal column and recursive call for each subset
				if (splitOK) {
					if (bestColumn.isNominal()) {
						for(Iterator<DataSet> it = splittedList.iterator(); it.hasNext();){
							splitted = it.next();
							splitted.getColumns().remove(bestColumn);
						}
					}
					int i = 0;
					for (Iterator<DataSet> it = splittedList.iterator(); it.hasNext();) {
						splitted = it.next();
						if (splitted.size() > 0) {
							Tree child = new Tree((DataSet)splitted.clone());
							DevideCond condition = null;
							 Statement st = null;
						        ResultSet rs = null;
							if (bestColumn.isNominal()) {
						    	String columnName = bestColumn.getName();
						    	String selectSQL = ((DBTable)splitted.getDBTable()).getSQL();
						       
						        String valueString = "";
						    	String sql = "select distinct "+StringHandler.doubleQ(columnName)+" from ("+selectSQL+") foo";
						        try {
//									databaseConnection.getConnection().setAutoCommit(false);
									st = databaseConnection.createStatement(false);
//									st.setFetchSize(1);
									itsLogger.debug("TreeBuilderDB.buildTree():sql="+sql);
									rs = st.executeQuery(sql);
									boolean limitFlag=true;
									while (rs.next()&&limitFlag)
									{
										valueString = rs.getString(1);
										limitFlag=false;
									}
								} catch (SQLException e) {
									e.printStackTrace();
									throw new OperatorException(e.getLocalizedMessage());
								}finally {
							    			try {
							    				if (rs != null)
							    					rs.close();
							    				if (st != null)
							    					st.close();
//							    				databaseConnection.getConnection().setAutoCommit(true);
							    			} catch (SQLException e) {
							    			 	throw new OperatorException(e.getLocalizedMessage());
							    			}	
						}
								condition = new NorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, valueString);//getValueAsString(bestcolumn));
							} else {
								if (i == 1) {
									condition = new LessEqualDevideCond(bestColumn, Double.parseDouble(AlpineMath.doubleExpression(bestSplitValue)));
								} else {
									condition = new GreaterDevideCond(bestColumn, Double.parseDouble(AlpineMath.doubleExpression(bestSplitValue)));
								}
							}
							current.addChild(child, condition);
							constTree(child, splitted, depth + 1);
						}
						i++;
					}
					
					// end loop
					splitFound = true;
					break;
				} else {
					continue;
				}

			}
		}
		
		// no split found --> change to leaf and return
		if (!splitFound) {
			if (loadData)
			{
				leafCreatorMem.changeToLeaf(current, trainingSet);
			}
			else
			{
				leafCreator.changeToLeaf(current, trainingSet);
			}
		}
	}


	
	private DataSet getDataSetMem(DataSet dataSet
			) throws OperatorException {
					DataSet newDataSet = null;
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

					boolean category = false;
					if (dataSet.getColumns().getLabel().isNumerical() && ((NumericColumn)dataSet.getColumns().getLabel()).isCategory())
					{
						category = true;
					}

					newDataSet = operator.createDataSetUsingExitingDBHandler(
							databaseConnection, category, labelColumnName);
					newDataSet.computeAllColumnStatistics();
		return newDataSet;
	}

	
	private boolean needLoadData(DataSet dataSet) {
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
