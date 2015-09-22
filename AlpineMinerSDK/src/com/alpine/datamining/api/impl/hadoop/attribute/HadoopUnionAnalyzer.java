/**
 * ClassName HadoopUnionAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModelItem;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopUnionRunner;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.db.Resources;
import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
/**
 * @author john zhao
 *
 */
public class HadoopUnionAnalyzer extends AbstractHadoopAttributeAnalyzer {
    private static final Logger itsLogger = Logger.getLogger(HadoopUnionAnalyzer.class);

	private HadoopHDFSFileManager hdfsManager=HadoopHDFSFileManager.INSTANCE;

	private HadoopUnionRunner hadoopRunner;

	private String outputFileFullName;
	private static final Logger logger = Logger.getLogger(HadoopUnionAnalyzer.class);

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		init((HadoopAnalyticSource)source);
		outputFileFullName=(null==resultLocaltion?"/":resultLocaltion)+resultsName;
			fileOutputNames.put(outputFileFullName, new String[]{""});

		//String outputTempName=outputFileFullName+System.currentTimeMillis();
		
		try {
			
			hadoopRunner=new HadoopUnionRunner(getContext(),getName());
 		     hadoopRunner.runAlgorithm(source);

				List<String> lineList = null;
				try {
					lineList  = hdfsManager.readHadoopPathToLineList(outputFileFullName, hadoopConnection,
							Integer.parseInt(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT) ));
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
					if(e instanceof EmptyFileException){
						lineList=new ArrayList<String>();
					}else{
						throw e;
					}
				} 
				if(lineList==null||lineList.size()==0){
					getContext().addEmptyPigVariabel(outputTempName);
				}else{
					getContext().removeEmptyPigVariabel(outputTempName);

				}
				addContentForTheFileOf(outputFileFullName, lineList.toArray(new String[lineList.size()]) );
	

 		     
 		     HadoopMultiAnalyticFileOutPut outPut = generateHadoopOutput();
		
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			
			
			if(hadoopRunner.isLocalMode()==true){
				outPut.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
			}
			super.reportBadDataCount(hadoopRunner.getBadCounter(), HadoopConstants.Flow_Call_Back_URL, getName(), getFlowRunUUID());
			//load to pig ...
			loadFileIntoPig(hadoopConnection );
			return outPut;
		} catch (Exception e) {
			itsLogger.error("Could not complete analysis due to the exception of",e);
			throw new AnalysisException(e);
		}
	 
	
	}

	private void loadFileIntoPig(HadoopConnection hadoopConnection) throws Exception {
		String fileName = outputFileFullName;
		 
		String hostName = hadoopConnection.getHdfsHostName();
		AnalysisFileStructureModel fileStructureModel =  getOutPutStructure();
		
		List<String> columnNameList = fileStructureModel.getColumnNameList();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
		
		String pureFileName=  getOutputTempName();
			AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
			StringBuffer header=new StringBuffer();
			StringBuffer pigTypeString=new StringBuffer();
			 
 
	 
					
					for(int i=0;i<columnNameList.size();i++){
							header.append(columnNameList.get(i)).append(":");
							header.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));
							pigTypeString.append(HadoopDataType.getTransferDataType(columnTypeList.get(i))) ;
							pigTypeString.append("_") ;
							header.append(COLUMN_SEPRATOR);
						}
				 
					
			 
		 

			if(header.length()>0){
				header.deleteCharAt(header.length()-1) ;
			}
 

	 
		 
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
 
 
 
	
	@Override
	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);

		HadoopUnionConfig newConfig = (HadoopUnionConfig)config;
		AnalysisHadoopUnionModel unionModel = newConfig.getUnionModel();
		List<String> newColumnNameList = new ArrayList<String>();
		List<String> newColumnTypeList = new ArrayList<String>();
		
		if(unionModel!=null&&unionModel.getOutputColumns()!=null){ 
		 List<AnalysisHadoopUnionModelItem> outputColumns = unionModel.getOutputColumns(); 
			for(AnalysisHadoopUnionModelItem outColumn:outputColumns){
				newColumnNameList.add(outColumn.getColumnName());
				newColumnTypeList.add(outColumn.getColumnType());
			}
		}
		newModel.setColumnNameList(newColumnNameList);
		newModel.setColumnTypeList(newColumnTypeList);

		return newModel; 
	}

	@Override
	public AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.TABLESET_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.TABLESET_DESCRIPTION,locale));
		return nodeMetaInfo;
	}
}
