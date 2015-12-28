
package com.alpine.datamining.operator.tree.cartclassification;

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
import com.alpine.datamining.operator.tree.threshold.DevideCond;
import com.alpine.datamining.operator.tree.threshold.GreaterDevideCond;
import com.alpine.datamining.operator.tree.threshold.IBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.LessEqualDevideCond;
import com.alpine.datamining.operator.tree.threshold.Prune;
import com.alpine.datamining.operator.tree.threshold.Stop;
import com.alpine.datamining.operator.tree.threshold.Tree;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.StatisticsChiSquareTest;
import com.alpine.utility.tools.AlpineMath;


public class ConstructTree extends AbstractConstructTree{
	
	
	private Prune pruner;	
	
	public ConstructTree(Standard criterion,
			Standard criterionMem,
			List<Stop> terminationCriteria, 
			List<Stop> terminationCriteriaMem, 
			IBuildLeaf leafCreator,
			IBuildLeaf leafCreatorMem,
			Prune pruner,
			boolean noPrePruning,
			int numberOfPrepruningAlternatives,
			int minSizeForSplit,
			int minLeafSize,
			int sizeThreshold) {
		
		super(criterion,criterionMem,terminationCriteria, terminationCriteriaMem, 
				leafCreator,leafCreatorMem,noPrePruning,
				numberOfPrepruningAlternatives,
				minSizeForSplit,
				minLeafSize,
				sizeThreshold);
		this.pruner = pruner;	
	}
	
	public ConstructTree(Standard criterion,
			Standard criterionMem,
			List<Stop> terminationCriteria, 
			List<Stop> terminationCriteriaMem, 
			IBuildLeaf leafCreator,
			IBuildLeaf leafCreatorMem,
			Prune pruner,
			boolean noPrePruning,
			int numberOfPrepruningAlternatives,
			int minSizeForSplit,
			int minLeafSize,
			int sizeThreshold,
			boolean isChiSquare) {
		
		super(criterion,criterionMem,terminationCriteria, terminationCriteriaMem, 
				leafCreator,leafCreatorMem,noPrePruning,
				numberOfPrepruningAlternatives,
				minSizeForSplit,
				minLeafSize,
				sizeThreshold,isChiSquare);
		this.pruner = pruner;	
	}

	
	public Tree trainDT(DataSet dataSet) throws OperatorException {

	
		databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
		// grow tree
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
		Vector<Score> benefits = calculateAllScores(dataSet, loadData);
		
		// sort all benefits
		Collections.sort(benefits);
		// try at most k benefits and check if prepruning is fulfilled
		boolean splitFound = false;
		for (int a = 0; a < numberOfPrepruningAlternatives + 1; a++) {
			// break if no benefits are left
			if (benefits.size() <= 0)
				break;
			
			// search current best			
			Score bestBenefit = benefits.remove(0);
			
			// check if minimum gain was reached
			if (usePrePruning && (bestBenefit.getScore() <= 0)) {
				continue;
			}
			
			if(this.isUseChiSquare)
			{
				if(StatisticsChiSquareTest.chiSquareTest(0.5,bestBenefit.getScore()/2.0) < AlpineDataAnalysisConfig.STATISTICS_CHECK_VALUE)
				{
					continue;
				}
			}
			
			
			// split by best column
			if (loadData)
			{
				// split by best column
				SplitDataSet splitted = null;
				Column bestColumn = bestBenefit.getColumn();
				double bestSplitValue = bestBenefit.getSplitValue();
				if (bestColumn.isNominal()) {
					splitted = SplitDataSet.splitByColumn(trainingSet, bestColumn, bestBenefit.getValues());
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
				for(int i = 0; i < splitted.getNumberOfSubsets(); i++){
					splitted.selectSingleSubset(i);
					if ((splitted.size()) == 0 ) {
						splitOK = false;
						break;
					}
				}
				if(splitted.getNumberOfSubsets() < 2){
					splitOK=false;
				}
				
				// if all have minimum size --> remove nominal column and recursive call for each subset
				if (splitOK) {
//					if (bestcolumn.isNominal()) {
//						splitted.getcolumns().remove(bestcolumne);
//					}
					for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
						splitted.selectSingleSubset(i);
						if (splitted.size() > 0) {
							Tree child = new Tree((DataSet)splitted.clone());
							DevideCond condition = null;
							if (bestColumn.isNominal()) {
								if (bestBenefit.getValues().contains(splitted.getRow(0).getNominalValue(bestColumn)))
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestBenefit.getValues(), false);
								}
								else
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestBenefit.getValues(), true);
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
				Column bestColumn = bestBenefit.getColumn();
				double bestSplitValue = bestBenefit.getSplitValue();
				if (bestColumn.isNominal()) {
					splittedList = SplitDBDataSet.splitByColumn(trainingSet, bestColumn,bestBenefit.getValues());
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
				for(Iterator<DataSet> it = splittedList.iterator(); it.hasNext();){
					   
					splitted = it.next();
					if ((splitted.size()) == 0 ) {
						splitOK = false;
						break;
					}
				}
				
				// if all have minimum size --> remove nominal column and recursive call for each subset
				if (splitOK) {
					if (bestColumn.isNominal()) {
//						for(Iterator<DataSet> it = splittedList.iterator(); it.hasNext();){
//							splitted = it.next();
//							splitted.getcolumns().remove(bestcolumn);
//						}
					}
					int i = 0;
					for (Iterator<DataSet> it = splittedList.iterator(); it.hasNext();) {
						splitted = it.next();
						if (splitted.size() > 0) {
							Tree child = new Tree((DataSet)splitted.clone());
							DevideCond condition = null;
							if (bestColumn.isNominal()) {
								if (i == 0)
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestBenefit.getValues(), false);
								}
								else
								{
									condition = new CartNorminalDevideCond(databaseConnection.getProperties().getName(),bestColumn, bestBenefit.getValues(), true);
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
