
package com.alpine.datamining.operator.tree.cartregression;

import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.tree.cart.AbstractTreeTrainer;
import com.alpine.datamining.operator.tree.cart.CartParameter;
import com.alpine.datamining.operator.tree.cart.Standard;
import com.alpine.datamining.operator.tree.threshold.Stop;
import com.alpine.datamining.operator.tree.threshold.DepthStop;
import com.alpine.datamining.operator.tree.threshold.NoDataStop;
import com.alpine.datamining.operator.tree.threshold.NoColumnStop;


public class CartRegressionTrainer extends AbstractTreeTrainer {

	
	CartParameter para;
    public CartRegressionTrainer() {
        super();
    }

	public List<Stop> getTerminationCriteria(DataSet dataSet, boolean loadData) throws OperatorException {
		List<Stop> result = new LinkedList<Stop>();

		result.add(new NoColumnStop());
		result.add(new NoDataStop());
		long maxDepth = para.getMaxDepth();
		if (maxDepth <= 0) {
			maxDepth = dataSet.size();
		}
		result.add(new DepthStop(maxDepth));
		return result;
	}
    protected ConstTree getTB(DataSet dataSet) throws OperatorException {
			if( dataSet.getColumns().getLabel().isNumerical())
			{
			return new ConstTree(createStandard(false),
					createStandard(true),
			          getTerminationCriteria(dataSet, false),
			          getTerminationCriteria(dataSet, true),
			          new DBBuildLeaf(),
			          new BuildLeaf(),
			          para.isNoPrePruning(),
			          para.getPrepruningAlternativesNumber(),
			          para.getSplitMinSize(),
			          para.getMinLeafSize(),
			          para.getThresholdLoadData());
			}
		return null;
	}

	@Override
	protected Standard createStandard( boolean loadData){
        Class<?> criterionClass = null;
        if (loadData)
        {
            criterionClass = GiniIndexStandard.class;
        }
        else
        {
        	criterionClass = DBGiniIndexStandard.class;
        }
            try {
                Standard criterion = (Standard)criterionClass.newInstance();
                return criterion;
            } catch (InstantiationException e) {
                if (loadData)
                {
                	return new GiniIndexStandard();
                }
                else
                {
                	return new DBGiniIndexStandard();
                }
            } catch (IllegalAccessException e) {
                if (loadData)
                {
                	return new GiniIndexStandard();
                }
                else
                {
                	return new DBGiniIndexStandard();
                }
           }
    }

    public Model train(DataSet dataSet) throws OperatorException {
    	para = (CartParameter)getParameter();
    	ConstTree builder = getTB(dataSet); 
    	    	
    	// learn tree
    	RegressionTree root = builder.trainDT(dataSet);
        
        // create and return model
        return new RegressionTreeModel(dataSet, root);
    }
}
