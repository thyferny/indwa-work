/**
 * ClassName NNTrain.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.neuralnet.sequential;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.datamining.utility.AlpineRandom;
import com.alpine.datamining.utility.ColumnTypeTransformer;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;

/**
 */
public class NNTrain extends Trainer {

    private static final Logger itsLogger = Logger.getLogger(NNTrain.class);

    private NNParameter para;
	
	public NNTrain() {
		super();
	}
	
	
	public Model train(DataSet dataSet) throws OperatorException {
		itsLogger.debug(LogUtils.entry("NNTraining", "train", dataSet.toString()));
		para = (NNParameter)getParameter();
		ColumnTypeTransformer transformer =new ColumnTypeTransformer();
		DataSet newDataSet=transformer.TransformCategoryToNumeric_new(dataSet);
		try {
			newDataSet.calculateAllNumericStatistics();	
			Iterator<Column> atts_i=newDataSet.getColumns().iterator();
			List<String> columnNamesList =new ArrayList<String>();
			while(atts_i.hasNext())
			{
				Column att=atts_i.next();
				columnNamesList.add(att.getName());
			}	
			NNModel model = null;
			if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType))
	    	{
				model = new NNModellOracle(newDataSet,dataSet,columnNamesList);
	    	}
	    	else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoDB2.dBType))
	    	{
				model = new NNModellDB2(newDataSet,dataSet,columnNamesList);
	    	}
	    	else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoNZ.dBType))
	    	{
				model = new NNModelNetezza(newDataSet,dataSet,columnNamesList);
	    	}

	    	else
	    	{
				model = new NNModel(newDataSet,dataSet,columnNamesList);
	    	}

			model.setAllTransformMap_columnKey(transformer.getAllTransformMap_columnKey());
			List<String[]> hiddenLayers = para.getHiddenLayers();
			int maxCycles = para.getTrainingIteration();
			double maxError = para.getErrorEpsilon();
			double learningRate = para.getLearningRate();
			double momentum = para.getMomentum();
			boolean decay = para.isDecay();
			boolean adjustPerRow = para.isAdjustPerRow();
			boolean normalize = para.isNormalize();
			AlpineRandom randomGenerator = AlpineRandom.getRandomGenerator(para.getRandomSeed());
			
			model.train(newDataSet, hiddenLayers, maxCycles, maxError, learningRate, momentum, decay, normalize, randomGenerator,para.getFetchSize(),adjustPerRow);
			model.statistics();
			//transformer.dropTemptable(dataSet);
			if(transformer.isTransform())
			{
				IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(((DBTable) newDataSet.getDBTable()).getDatabaseConnection().getProperties().getName()); ; 
				Statement st = ((DBTable) newDataSet.getDBTable()).getDatabaseConnection().createStatement(false);
				String tableName = ((DBTable) newDataSet.getDBTable()).getTableName();
				multiDBUtility.dropTraingTempTable(st, tableName);
				st.close();
			}
			itsLogger.debug(LogUtils.exit("NNTrain", "learn", model.toString()));
			return model;
		} catch (Exception e) {
			//transformer.dropTemptable(dataSet);
			e.printStackTrace();
			itsLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
}
