/**
 * ClassName CartClassificationTrainer
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.cartclassification;

import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.tree.cart.AbstractTreeTrainer;
import com.alpine.datamining.operator.tree.cart.CartParameter;
import com.alpine.datamining.operator.tree.cart.Standard;
import com.alpine.datamining.operator.tree.threshold.BuildLeaf;
import com.alpine.datamining.operator.tree.threshold.ClassPureStop;
import com.alpine.datamining.operator.tree.threshold.DBBuildLeaf;
import com.alpine.datamining.operator.tree.threshold.DBPrunePessimistic;
import com.alpine.datamining.operator.tree.threshold.DBPureStop;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DepthStop;
import com.alpine.datamining.operator.tree.threshold.NoColumnStop;
import com.alpine.datamining.operator.tree.threshold.NoDataStop;
import com.alpine.datamining.operator.tree.threshold.Prune;
import com.alpine.datamining.operator.tree.threshold.Stop;
import com.alpine.datamining.operator.tree.threshold.Tree;

/**
 * <p>
 * This operator learns cart classification trees. This class is for data in
 * database.
 */
public class CartClassificationTrainer extends AbstractTreeTrainer {

	CartParameter para;

	public CartClassificationTrainer() {
		super();
	}

	public Prune getPrune(boolean loadData) throws OperatorException {
		if (!para.isNoPruning()) {
			return new DBPrunePessimistic(para.getConfidence(),
					new DBBuildLeaf(), new BuildLeaf());
		} else {
			return null;
		}
	}

	public List<Stop> getStop(DataSet dataSet, boolean loadData)
			throws OperatorException {
		List<Stop> result = new LinkedList<Stop>();
		if (loadData) {
			result.add(new ClassPureStop());
		} else {
			result.add(new DBPureStop());
		}
		result.add(new NoColumnStop());
		result.add(new NoDataStop());
		long maxDepth = para.getMaxDepth();
		if (maxDepth <= 0) {
			maxDepth = dataSet.size();
		}
		result.add(new DepthStop(maxDepth));
		return result;
	}

	protected ConstructTree getTB(DataSet dataSet) throws OperatorException {
		if (dataSet.getColumns().getLabel().isNominal()||(para.isForWoe()==true)) {
			
			if(para.isUseChiSquare()==true||para.isForWoe()==true)
			{
				return new ConstructTree(createStandard(false),
						createStandard(true), getStop(dataSet, false), getStop(
								dataSet, true), new DBBuildLeaf(), new BuildLeaf(),
						getPrune(false), para.isNoPrePruning(), para
								.getPrepruningAlternativesNumber(), para
								.getSplitMinSize(), para.getMinLeafSize(), para
								.getThresholdLoadData(),para.isUseChiSquare());
			}
			else{
			return new ConstructTree(createStandard(false),
					createStandard(true), getStop(dataSet, false), getStop(
							dataSet, true), new DBBuildLeaf(), new BuildLeaf(),
					getPrune(false), para.isNoPrePruning(), para
							.getPrepruningAlternativesNumber(), para
							.getSplitMinSize(), para.getMinLeafSize(), para
							.getThresholdLoadData());
			}
		}
		return null;
	}

	@Override
	protected Standard createStandard(boolean loadData) {
		Class<?> criterionClass = null;
		if (para.isUseChiSquare() == false) {
			if (loadData) {
				criterionClass = GiniIndexStandard.class;
			} else {
				criterionClass = DBGiniIndexStandard.class;
			}
		} else {
			if (loadData) {
				criterionClass = ChiSquareIndexStandard.class;
			} else {
				criterionClass = DBChiSquareIndexStandard.class;
			}
		}
		try {
			Standard criterion = (Standard) criterionClass.newInstance();
			return criterion;
		} catch (InstantiationException e) {
			if (para.isUseChiSquare() == false) {
				if (loadData) {
					return new GiniIndexStandard();
				} else {
					return new DBGiniIndexStandard();
				}
			} else {
				if (loadData) {
					return new ChiSquareIndexStandard();
				} else {
					return new DBChiSquareIndexStandard();
				}
			}
		} catch (IllegalAccessException e) {
			if (para.isUseChiSquare() == false) {
				if (loadData) {
					return new GiniIndexStandard();
				} else {
					return new DBGiniIndexStandard();
				}
			} else {
				if (loadData) {
					return new ChiSquareIndexStandard();
				} else {
					return new DBChiSquareIndexStandard();
				}
			}
		}
	}

	@Override
	public Model train(DataSet dataSet) throws OperatorException {
		para = (CartParameter) getParameter();

		// create tree builder
		ConstructTree builder = getTB(dataSet);

		// learn tree
		Tree root = builder.trainDT(dataSet);

		// create and return model
		return new DecisionTreeModel(dataSet, root);
	}
}
