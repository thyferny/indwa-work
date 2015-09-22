/**
 * ClassName KMeansAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-18
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.ClusterKMeansConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.kmeans.ClusterModel;
import com.alpine.datamining.operator.kmeans.KMeansDBFunctionPara;
import com.alpine.datamining.operator.kmeans.KmeansParameter;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
/**
 * @author Jeff Dong
 * 
 */
public class KMeansAnalyzer extends AbstractDBAttributeAnalyzer {
    private static Logger itsLogger = Logger.getLogger(KMeansAnalyzer.class);

    private static final int MIN_K = 2;
	
	public static final String alpine_cluster = "alpine_cluster";

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
 
		ClusterKMeansConfig config = (ClusterKMeansConfig)source.getAnalyticConfig();
		
		ClusterModel model=train(source);
		AnalyzerOutPutObject outPut= new AnalyzerOutPutObject(model);
		 outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		 
		 outPut.setDbInfo(((DataBaseAnalyticSource)source).getDataBaseInfo());
		 outPut.setSchemaName(config.getOutputSchema());
		 outPut.setTableName(config.getOutputTable()) ;
		 
		 
		 return outPut;
 
	}
	private  ClusterModel train( AnalyticSource source) throws AnalysisException {
		try {
			ClusterKMeansConfig config =(ClusterKMeansConfig)source.getAnalyticConfig();
			DataSet dataSet = getDataSet((DataBaseAnalyticSource)source,config);
			KmeansParameter parameter = new KmeansParameter();
			
			setClusterName(config, dataSet, parameter);
			
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			Operator cluster = OperatorUtil.createOperator(KMeansDBFunctionPara.class);
			

			itsLogger.info("KMeansAnalyzer  addAnalyzerID: ="+String.valueOf(this.hashCode()));
			if(!StringUtil.isEmpty(config.getK()))
			{
				itsLogger.debug("set \"K\" to "+config.getK());
				parameter.setK(Integer.parseInt(config.getK()));
			}
			if(!StringUtil.isEmpty(config.getMax_optimization_steps()))
			{
				itsLogger.debug("set \"max_optimization_steps\" to "+config.getMax_optimization_steps());
				parameter.setMaxOptimizationSteps(Integer.parseInt(config.getMax_optimization_steps()));
			}
			if(!StringUtil.isEmpty(config.getMax_runs()))
			{
				itsLogger.debug("set \"max_runs\" to "+config.getMax_runs());
				parameter.setMaxRuns(Integer.parseInt(config.getMax_runs()));
			}
			if(!StringUtil.isEmpty(config.getSplit_Number()))
			{
				itsLogger.debug("set \"split_number\" to "+config.getSplit_Number());
				parameter.setSplitNumber(Integer.parseInt(config.getSplit_Number()));
			}
			if(Integer.parseInt(config.getK())<2)
			{
				AnalysisError error = new AnalysisError(this,AnalysisErrorName.K_LESS_THEN_2,config.getLocale(),new Integer(MIN_K));
				itsLogger.error(error.getMessage(),error);
				throw error;//"K cannot be less than 2"
			}
			if(!StringUtil.isEmpty(config.getDistance()))
			{
				itsLogger.debug("set \"Distance\"");
				parameter.setDistance(config.getDistance());
			}
			if(!StringUtil.isEmpty(config.getOutputSchema()))
			{
				itsLogger.debug("set \"ResultShemaName\"");
				parameter.setResultSchema(config.getOutputSchema());
			}
			if(!StringUtil.isEmpty(config.getOutputTable()))
			{
				itsLogger.debug("set \"ResultTableName\"");
				parameter.setResultTableName(config.getOutputTable());
			}
			if(!StringUtil.isEmpty(config.getDropIfExist()))
			{
				itsLogger.debug("set \"DropIfExist\"");
				parameter.setDropIfExist(config.getDropIfExist());
			}
			if(!StringUtil.isEmpty(config.getUseArray()))
			{
				itsLogger.debug("set \"UseArray\"");
				parameter.setUseArray(Boolean.parseBoolean(config.getUseArray()));
			}
			parameter.setAnalysisStorageParameterModel(config.getStorageParameters());
			cluster.setParameter(parameter);
			warnTooManyValue(dataSet,Integer.parseInt(AlpineMinerConfig.C2N_WARNING),config.getLocale());
			ClusterModel Model =((KMeansDBFunctionPara)cluster).generateClusterModel(dataSet);
			return Model;
	
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} 
			else if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}	
		} //should not close conenction ,will used for visualization
		//workflow will close it
	}
	private void setClusterName(ClusterKMeansConfig config, DataSet dataSet,
			KmeansParameter parameter) {
		if(!StringUtil.isEmpty(config.getClusterColumnName()))
		{
			itsLogger.debug("set \"clusterColumnName\"");
			parameter.setClusterColumnName(config.getClusterColumnName());
		}else{
			Columns columns = dataSet.getColumns();
			if(columns!=null){
				List<String> columnNameList=new ArrayList<String>();
				Iterator<Column> iter = columns.allColumns();
				while(iter.hasNext()){
					columnNameList.add(iter.next().getName());
				}
				String clusterName=getClusterNo(columnNameList);
				parameter.setClusterColumnName(clusterName);
			}
		}
	}
	

	@Override
	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setId(((ClusterKMeansConfig)config).getIdColumn());
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.KMEANS_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.KMEANS_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

	protected String getClusterNo(List<String> list){
		String cluster = alpine_cluster;
		if(list == null){
			return cluster;
		}
		int id = 1;
		if(!list.contains(cluster)){
			return cluster;
		}
		while(true){
			if(list.contains(cluster+"_"+id)){
				id++;
			}else{
				break;
			}
		}
		return cluster+"_"+id;
	}
}
