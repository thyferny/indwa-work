
package com.alpine.datamining.operator.training;

import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;


public abstract class Trainer extends Operator implements Training {




	public Trainer() {
		super();
	}

	public ConsumerProducer[] apply() throws OperatorException {
		DataSet dataSet = null;
		dataSet = getInput(DataSet.class);

		// some checks
		if (dataSet.getColumns().getLabel() == null) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.MISS_DEP);
		}
		if (dataSet.getColumns().size() == 0) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.MISS_COL);
		}
        if (dataSet.size() == 0) {
            throw new WrongUsedException(this, AlpineAnalysisErrorName.DATA_EMPTY);
        }
        

		List<ConsumerProducer> results = new LinkedList<ConsumerProducer>();
		Model model = train(dataSet);
		results.add(model);

		ConsumerProducer[] resultArray = new ConsumerProducer[results.size()];
		results.toArray(resultArray);
		return resultArray;
	}


	public boolean shouldEstimatePerformance() {
		return false;
	}


	public boolean shouldCalculateWeights() {
		return false;
	}

    public boolean shouldDeliverOptimizationPerformance() {
        return false;
    }
    
	public boolean onlyWarnForNonSufficientCapabilities() {
		return false;
	}
	
	public Class<?>[] getInputClasses() {
		return new Class[] { DataSet.class };
	}

	public Class<?>[] getOutputClasses() {
		List<Class<?>> classList = new LinkedList<Class<?>>();
		classList.add(Model.class);
		Class<?>[] result = new Class[classList.size()];
		classList.toArray(result);
		return result;
	}
}
