/**
 * 

* ClassName EMTrainer.java
*
* Version information: 1.00
*
* Data: Apr 26, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.EMConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.trainer.EMCluster.EMClusterFactory;
import com.alpine.datamining.api.impl.db.trainer.EMCluster.EMClusterImpl;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.EMCluster.EMClusterNode;
import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.utility.ColumnTypeTransformer;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.IDataSourceInfo;

/**
 * @author Shawn
 *
 */
public class EMTrainer extends AbstractDBModelTrainer{
	private static Logger logger= Logger.getLogger(EMTrainer.class);
	
	
	private String dbtype = null;
	
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.AbstractDBModelTrainer#createNodeMetaInfo(java.util.Locale)
	 */
	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		//TODO
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.EM_TRAIN_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.EM_TRAIN_DESCRIPTION,locale));
		return nodeMetaInfo;

	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.AbstractDBModelTrainer#train(com.alpine.datamining.api.AnalyticSource)
	 */
	@Override
	protected Model train(AnalyticSource source) throws AnalysisException {
		ResultSet rs = null;
		Statement st = null;
		EMModel trainModel = null;
		try {
			
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory
			.createConnectionInfo(source.getDataSourceType());
			dbtype = dataSourceInfo.getDBType();
			
			EMConfig config=(EMConfig)source.getAnalyticConfig();
			
			String anaColumns = config.getColumnNames();
			String[] columnsArray = anaColumns.split(",");
			List<String> transformColumns = new ArrayList<String>();
			for(int i = 0; i < columnsArray.length; i++){
				
				transformColumns.add(columnsArray[i]);
			}
			DataSet dataSet = getDataSet((DataBaseAnalyticSource)source,config);
			filerColumens(dataSet, transformColumns);
			dataSet.computeAllColumnStatistics();
			ColumnTypeTransformer transformer = new ColumnTypeTransformer();
			DataSet newDataSet = transformer.TransformCategoryToNumeric_new(dataSet);
			String tableName = ((DBTable) newDataSet.getDBTable())
			.getTableName();
			Columns columns = newDataSet.getColumns();
			List<String> newTransformColumns = new ArrayList<String>();
			HashMap<String,String> transformMap=new HashMap<String,String>();
			for(String key:transformer.getAllTransformMap_valueKey().keySet()){
				HashMap<String,String> values=(transformer.getAllTransformMap_valueKey()).get(key);
				for(String lowKey:values.keySet()){
					transformMap.put(values.get(lowKey), lowKey);
				}
			}
			
			Iterator<Column> attributeIter = columns.iterator();
			while(attributeIter.hasNext()){
				Column column = attributeIter.next();
				newTransformColumns.add(column.getName());
			}
			
			int maxIterationNumber=Integer.parseInt(config.getMaxIterationNumber());
			int clusterNumber = Integer.parseInt(config.getClusterNumber());
			double epsilon=Double.parseDouble(config.getEpsilon());
			int initClusterSize=10;
				if(config.getInitClusterSize()!=null)
				{
					initClusterSize=Integer.parseInt(config.getInitClusterSize());
				}
				if(newDataSet.size()<initClusterSize*clusterNumber)
				{
					initClusterSize=(int) (newDataSet.size()/clusterNumber+1);
				}//TODO  get it from config and make sure it will not be too large
			EMClusterImpl emImpl = EMClusterFactory.createEMAnalyzer(dbtype); 
			trainModel = EMClusterFactory.createEMModel(dbtype, newDataSet);
			Connection connection = null;
			connection = ((DataBaseAnalyticSource) source).getConnection();

			st = connection.createStatement();
					
			ArrayList<Double> tempResult=emImpl.emTrain(connection,st,tableName,maxIterationNumber,epsilon, clusterNumber, newTransformColumns,initClusterSize,trainModel);
			trainModel=generateEMModel(trainModel, newTransformColumns, clusterNumber,
					tempResult);
			if(!newDataSet.equals(this.dataSet))
			{
				trainModel.setAllTransformMap_valueKey(transformMap);
			}
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof WrongUsedException) {
				throw new AnalysisError(this, (WrongUsedException) e);
			} else if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}finally{
			try {
				if(st != null)
				{
					st.close();
				}
				if(rs!=null)
				{
					rs.close();
				}
			} catch (SQLException e) {
				logger.debug(e.toString());
			throw new AnalysisException(e.getLocalizedMessage());
			}
		}
			return trainModel;
	}

	private EMModel generateEMModel(EMModel trainModel,
			List<String> newTransformColumns, int clusterNumber,
			ArrayList<Double> tempResult) {
		trainModel.setClusterNumber(clusterNumber);
		for(int i=0;i<clusterNumber;i++)
		{
			EMClusterNode tempNode=new EMClusterNode();
			tempNode.setAlpha(tempResult.get(i));
			Map <String,Double> muValue =new LinkedHashMap<String,Double>();
			Map <String,Double> sigmaValue =new LinkedHashMap<String,Double>();
			for(int j =0;j<newTransformColumns.size();j++)
			{
				muValue.put(newTransformColumns.get(j), tempResult.get(clusterNumber+i*newTransformColumns.size()+j));
				sigmaValue.put(newTransformColumns.get(j), tempResult.get(clusterNumber+clusterNumber*newTransformColumns.size()+i*newTransformColumns.size()+j));
			}
			tempNode.setMuValue(muValue);
			tempNode.setSigmaValue(sigmaValue);
			trainModel.addClusteSingleValue(i+1,tempNode);
		}
		return trainModel;
	}
	
 
}
