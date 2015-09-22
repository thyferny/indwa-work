/**
 * ClassName ConstructTree
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartregression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBDataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.SplitDBDataSet;
import com.alpine.datamining.db.SplitDataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.tree.cart.AbstractConstructTree;
import com.alpine.datamining.operator.tree.cart.Score;
import com.alpine.datamining.operator.tree.cart.Standard;
import com.alpine.datamining.operator.tree.cartclassification.CartNorminalDevideCond;
import com.alpine.datamining.operator.tree.threshold.DevideCond;
import com.alpine.datamining.operator.tree.threshold.GreaterDevideCond;
import com.alpine.datamining.operator.tree.threshold.IBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.LessEqualDevideCond;
import com.alpine.datamining.operator.tree.threshold.Stop;
import com.alpine.datamining.operator.tree.threshold.Tree;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.AlpineMath;

/**
 * Build a tree from an data set.
 * This class is used for data in database.
 */
public class ConstTree extends AbstractConstructTree{
    private static Logger itsLogger= Logger.getLogger(ConstTree.class);

    /* for data not loaded in memory */
		
	public ConstTree(Standard criterion,
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
		
		super(criterion,criterionMem,terminationCriteria,terminationCriteriaMem, 
				leafCreator,leafCreatorMem,noPrePruning,
				numberOfPrepruningAlternatives,
				minSizeForSplit,minLeafSize,
				sizeThreshold);	
	}

	public RegressionTree trainDT(DataSet dataSet) throws OperatorException {

		databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		
		// grow tree
		RegressionTree root = new RegressionTree((DataSet)dataSet.clone());
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

//		if (pruner != null)
//			pruner.prune(root);

		return root;
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
		if (dataSet.getColumns().getLabel().isNumerical())
		{
			numericalLabel = true;
		}
		
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
		itsLogger.debug(scores.toString());
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
					splitted = SplitDataSet.splitByColumn(trainingSet, bestColumn, bestScore.getValues());
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
//					if (bestcolumn.isNominal()) {
//						splitted.getcolumns().remove(bestcolumn);
//					}
					for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
						splitted.selectSingleSubset(i);
						if (splitted.size() > 0) {
							RegressionTree child = new RegressionTree((DataSet)splitted.clone());
							DevideCond condition = null;
							if (bestColumn.isNominal()) {
								if (bestScore.getValues().contains(splitted.getRow(0).getNominalValue(bestColumn)))
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestScore.getValues(), false);
								}
								else
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestScore.getValues(), true);
								}
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
					splittedList = SplitDBDataSet.splitByColumn(trainingSet, bestColumn,bestScore.getValues());
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
					int i = 0;
					for (Iterator<DataSet> it = splittedList.iterator(); it.hasNext();) {
						splitted = it.next();
						if (splitted.size() > 0) {
							RegressionTree child = new RegressionTree((DataSet)splitted.clone());
							DevideCond condition = null;
							if (bestColumn.isNominal()) {
								if (i == 0)
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestScore.getValues(), false);
								}
								else
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestScore.getValues(), true);
								}

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



}
