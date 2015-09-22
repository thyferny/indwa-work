/**
 * ClassName HadoopKmeansAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.kmeans;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopKMeansConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopMRJobAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopKmeansRunner;
import com.alpine.datamining.api.impl.output.hadoop.HadoopKmeansOutput;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.hadoop.output.ClusterOutputModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpinePigServer;


/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansAnalyzer extends AbstractHadoopMRJobAnalyzer {

	protected HadoopConnection hadoopConnection;
	protected String resultsName;
	protected String resultLocaltion;
	protected HadoopAnalyticSource hadoopSource;
	protected HadoopKMeansConfig config;
	protected String outputFileFullName;
	private HadoopHDFSFileManager hdfsManager=HadoopHDFSFileManager.INSTANCE;
	
	private static Logger itsLogger = Logger
			.getLogger(HadoopKmeansAnalyzer.class);
	
	public HadoopKmeansAnalyzer(){
		super();
	}
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		init((HadoopAnalyticSource)source);
		
		if(Resources.TrueOpt.equals(config.getOverride())){
			boolean success = hdfsManager.deleteHadoopFile(outputFileFullName, hadoopConnection);
			if(success==false){
				throw new AnalysisException("Can not delete out put directory "+outputFileFullName);
			}
		}
		String outputTempName=outputFileFullName+System.currentTimeMillis();
		
		try {
			
			hadoopRunner=new HadoopKmeansRunner(getContext(),getName());
			((HadoopKmeansRunner)hadoopRunner).setOutputTempName(outputTempName);
			ClusterOutputModel model = (ClusterOutputModel) hadoopRunner.runAlgorithm(source);
			
			HadoopKmeansOutput outPut= new HadoopKmeansOutput(model);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
  			
			if(hadoopRunner.isLocalMode()==true){
				outPut.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
			}
			super.reportBadDataCount(hadoopRunner.getBadCounter(), HadoopConstants.Flow_Call_Back_URL, getName(), getFlowRunUUID());
			//load to pig ...
			loadFileIntoPig(hadoopConnection,config);
			return outPut;
		} catch (Exception e) {
			itsLogger.error("Could not complete analysis on Kmeans due to the exception of",e);
			throw new AnalysisException(e);
		}
		finally{
			if(hdfsManager.exists(outputFileFullName, hadoopConnection)){
				hdfsManager.deleteHadoopFile(outputTempName+"_k", hadoopConnection);
			}
		}
	
	}

	private void loadFileIntoPig(HadoopConnection hadoopConnection,
			HadoopKMeansConfig config) throws Exception {
		String fileName = outputFileFullName;
		 
		String hostName = hadoopConnection.getHdfsHostName();
		AnalysisFileStructureModel fileStructureModel =  hadoopSource.getHadoopFileStructureModel();
		
		List<String> columnNameList = fileStructureModel.getColumnNameList();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
		
		String pureFileName=  getOutputTempName();
			AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
			StringBuffer header=new StringBuffer();
			StringBuffer pigTypeString=new StringBuffer();
			String selectedColumnNames =config.getColumnNames();
			if(!StringUtil.isEmpty(selectedColumnNames)){
				String[] columns =selectedColumnNames.split(",");
				for(String s:columns){
					header.append(s).append(":");
					for(int i=0;i<columnNameList.size();i++){
						if(s.equals(columnNameList.get(i))){
							header.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));
							pigTypeString.append(HadoopDataType.getTransferDataType(columnTypeList.get(i))) ;
							pigTypeString.append("_") ;
							break;
						}
					}
					header.append(COLUMN_SEPRATOR);
				}
			}

	 
			String idColumn =config.getIdColumn();
            if(!StringUtil.isEmpty(idColumn)){
            	header.append(idColumn).append(":");
                for(int i=0;i<columnNameList.size();i++){
                    if(idColumn.equals(columnNameList.get(i))){
                    	header.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));     
                    	pigTypeString.append(HadoopDataType.getTransferDataType(columnTypeList.get(i))) ;
						pigTypeString.append("_") ;

                    	break;
					}
				}
				header.append(COLUMN_SEPRATOR);
            }

			String clusterNo = AlpineUtil.generateClusterName(columnNameList);
			header.append(clusterNo).append(":");
			header.append(HadoopDataType.INT);
			pigTypeString.append(HadoopDataType.INT) ;
		 
			String fileURI="hdfs://"+hostName+":"+hadoopConnection.getHdfsPort()+fileName;
			
			String storageFunction  = getCSVPigStorageByVersion(hadoopConnection) ; 
			String script = pureFileName+" = load '"+ fileURI+ "' USING " + storageFunction+
					
					"('"+		AnalysisCSVFileStructureModel.DEFAULT_DELIMITER_VALUE+"','','"
					+(byte)AnalysisCSVFileStructureModel.ESCAP_VALUE.charAt(0)+"','"
					+(byte)AnalysisCSVFileStructureModel.QUOTE_VALUE.charAt(0)+"','"
					+pigTypeString
					+"','"+HadoopConstants.Flow_Call_Back_URL+"','"+getFlowRunUUID()+"','"+getName()
					+"') " + " as ("+header+");";
			
			 
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug(script);
			}
			
		    pigServer.registerQuery(script);	
	 
		
	}
	
	protected void init(HadoopAnalyticSource hadoopSource){
		this.hadoopSource=hadoopSource;
		hadoopConnection = hadoopSource.getHadoopInfo();
		config = (HadoopKMeansConfig)hadoopSource.getAnalyticConfig();
		resultsName = config.getResultsName();
		resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}
		outputFileFullName = resultLocaltion+resultsName;
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.KMEANS_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.KMEANS_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
