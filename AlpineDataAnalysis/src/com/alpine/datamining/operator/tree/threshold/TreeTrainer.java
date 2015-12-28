
package com.alpine.datamining.operator.tree.threshold;

import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;


public class TreeTrainer extends AbstractTreeTrainer {

	DecisionTreeParameter para ;
    public TreeTrainer() {
        super();
    }

	public Prune getPruner(boolean loadData) throws OperatorException {
		para = (DecisionTreeParameter)getParameter();
        if (!para.isNoPruning()) {
        	return new DBPrunePessimistic(para.getConfidence(), new DBBuildLeaf(), new BuildLeaf());
        } else {
            return null;
        }
	}

	public List<Stop> getTerminationCriteria(DataSet dataSet, boolean loadData) throws OperatorException {
		para = (DecisionTreeParameter)getParameter();
		List<Stop> result = new LinkedList<Stop>();
		if (loadData)
		{
			result.add(new ClassPureStop());
		}
		else
		{
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

    protected ConstructTree getTreeBuilder(DataSet dataSet) throws OperatorException {
		para = (DecisionTreeParameter)getParameter();
			
			dataSet.computeAllColumnStatistics();
			return new ConstructTree(createStandard(para.getMinGain(),false),
					createStandard(para.getMinGain(),true),
//					  columnname,
			          getTerminationCriteria(dataSet, false),
			          getTerminationCriteria(dataSet, true),
			          getPruner(false),
			          new DBBuildLeaf(),
			          new BuildLeaf(),
			          para.isNoPrePruning(),
			          para.getPrepruningAlternativesNumber(),
			          para.getSplitMinSize(),
			          para.getMinLeafSize(),
			          para.getThresholdLoadData());
	}

}
