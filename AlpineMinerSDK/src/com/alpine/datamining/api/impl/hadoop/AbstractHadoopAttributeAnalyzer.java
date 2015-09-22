/**
 * ClassName AbstractHadoopAttributeAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-25
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.executionengine.ExecJob;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
 
import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.db.Resources;
import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

/***
 * 
 * @author Eason
 * 
 */

public abstract class AbstractHadoopAttributeAnalyzer extends AbstractHadoopAnalyzer {
	protected HadoopConnection hadoopConnection;
	protected String resultsName;
	private static final Logger logger = Logger.getLogger(AbstractHadoopAttributeAnalyzer.class);
	protected String resultLocaltion;
	protected HadoopAnalyticSource hadoopSource;
	protected HadoopDataOperationConfig config;
	protected String outputTempName; 
	public static List<String> Ignored_Result_Files = Arrays.asList(new String[]{"_SUCCESS"}) ;
	public String getOutputTempName() {
		return outputTempName;
	}
 
	public void setOutputTempName(String outputTempName) {
		this.outputTempName = outputTempName;
	}

	private Map<String, List<String[]>> outputFileSampleContents=new HashMap<String,List<String[]>>();
	protected Map<String,String[]> fileOutputNames;
	private boolean saveResult = true;
	private String inputTempName; 
	
	protected void init(HadoopAnalyticSource hadoopSource){
		fileOutputNames=new HashMap<String,String[]>();
		this.hadoopSource=hadoopSource;
		hadoopConnection = hadoopSource.getHadoopInfo();
		outputTempName = super.getOutputTempName();
		inputTempName = hadoopSource.getInputTempName();
		config = (HadoopDataOperationConfig)hadoopSource.getAnalyticConfig();
		//		miningutil add if not set
		resultsName = config.getResultsName();
		resultLocaltion = config.getResultsLocation();
		if(!StringUtil.isEmpty(resultLocaltion)&&resultLocaltion.endsWith(HadoopFile.SEPARATOR)==false){
			resultLocaltion=resultLocaltion+ HadoopFile.SEPARATOR;
		}		
	}
	
	@Override
	
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {	
		//join and set are different!
		if(source.getAnalyticConfig() instanceof HadoopJoinConfig ==false 
				&&source.getAnalyticConfig() instanceof HadoopUnionConfig==false){
			
	       HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
	    	if(getContext().isEmptyPigVariable(hadoopSource.getInputTempName())){
				throw new AnalysisException(EMPTY_INPUT_MSG);
			}
	    }
		init((HadoopAnalyticSource)source);		
		try {
            AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
			String pigScript = generateScript(config, inputTempName);
			
			logger.warn(pigScript);

			runPigScript(pigServer, pigScript) ;
			
			storeOutput(pigServer);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (e.getCause() != null) {// avoid the pig can not open iterator
										// exception
				if (e.getCause().getMessage()
						.startsWith("job terminated with anomalous status FAILED")) {
					throw new AnalysisException(
							"An error occurred when running the operator. Please see Alpine.log for more detail.",
							e);
				} else {// pig root error--at most 4 layer

					int maxDepth = 4; 
					Throwable rootCause = getRootCuase(e,maxDepth);
					throw new AnalysisException(rootCause.getMessage());

				}
			} else {
				throw new AnalysisException(e);
			}
		}
	 	
		HadoopMultiAnalyticFileOutPut output = generateHadoopOutput();
	
		output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		
		return output;
	}
	
	private Throwable getRootCuase(Throwable e, int maxDepth) {
		int currentDepth = 0;
		while(e.getCause()!=null&&currentDepth<maxDepth){
			e = e.getCause();
			currentDepth = currentDepth + 1;
		}
		 
		return e;
	}

	protected String generateScript(HadoopDataOperationConfig config,
			String inputTempName) {
		return null;
	}
	protected abstract AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale);
	
	
    protected  void storeOutputForTheGivenPigVaribleIntoGivenFile(AlpinePigServer pigServer,String outputTempFileName,String fullPathFileName) //--- with quote and escpa char ...
			throws IOException, Exception {
		AnalysisCSVFileStructureModel hdfsm = (AnalysisCSVFileStructureModel)getOutPutStructure();

		byte escapeChar = '\\';	 
		if(StringUtil.isEmpty(hdfsm.getEscapChar() )==false){
			escapeChar = (byte) hdfsm.getEscapChar().charAt(0) ;
		}
		
		byte quoteChar = '\"';	
		if(StringUtil.isEmpty(hdfsm.getQuoteChar() )==false){
			quoteChar = (byte)hdfsm.getQuoteChar().charAt(0) ;
		}
		 
		String pigStorageFunction =getCSVPigStorageByVersion(hadoopConnection) ;
		String delimiter = HadoopUtility.getDelimiterValue(hdfsm);

		if(config.getStoreResults().equals(Resources.TrueOpt)){
			if(saveResult==false){
				return ;
			}
			
			if(config.getOverride().equals(Resources.YesOpt)){
				  
				boolean success = HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(fullPathFileName, hadoopConnection);
				if(success==false){
					throw new Exception("Can not delete out put directory "+fullPathFileName);
				}
			}else{
				boolean fileExist = HadoopHDFSFileManager.INSTANCE.exists(fullPathFileName, hadoopConnection);
				if(fileExist){
					String errMessage = "File "+fullPathFileName+" already exists!";
					logger.error(errMessage);
					throw new Exception(errMessage);
				}
			}
		
			ExecJob exeJob   = pigServer.store(outputTempFileName, fullPathFileName,"" +pigStorageFunction+
						"('" +delimiter+"')"); 
			
		 
			if(exeJob!=null&&exeJob.getException()!=null){
				throw new Exception("Can not store result to "+outputTempFileName,exeJob.getException()) ;
			}else if(exeJob!=null&&exeJob.getStatus().equals(ExecJob.JOB_STATUS.FAILED)){
 				throw new Exception("Can not store result to "+outputTempFileName +". Please see Alpine.log for more detail.") ;
			}
			
 			fileOutputNames.put(fullPathFileName, new String[]{""});
 			
 			  HadoopFile fullPathOutputFile = HadoopHDFSFileManager.INSTANCE.getHadoopFile( fullPathFileName, hadoopConnection);
			if(fullPathOutputFile!=null ){
			
	 			
				//empty out put file will generate a big error ,so we need delete them
				deleteEmptyOutputFile(fullPathFileName);
 
//  need remove the _log.  file 
  
				StringBuffer visualizationTypeClass=new StringBuffer();
				visualizationTypeClass.append(HadoopDataOperationConfig.HD_MULTIOUTPUT_VISUALIZATIONCLASS);
				List<String> lineList = null;
				try {
					lineList  = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(fullPathFileName, hadoopConnection,
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
					getContext().addEmptyPigVariabel(outputTempFileName);
				}else{
					getContext().removeEmptyPigVariabel(outputTempFileName);

				}
				addContentForTheFileOf(fullPathFileName, lineList.toArray(new String[lineList.size()]) );
	
				config.setVisualizationTypeClass(visualizationTypeClass.toString());
			}else{
				config.setVisualizationTypeClass("");
			}		
			
 
		}else{
			
			String tempFileName= "tempStoreName"+System.currentTimeMillis();
//			String tempOutPutFileName = "/tmp/"+tempFileName;
			String pigScript = tempFileName +" = LIMIT " +outputTempFileName+" "+ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT) +" ;"; 
			pigServer.registerQuery(pigScript) ;
 
			Iterator<Tuple> iter = pigServer.openIterator(tempFileName);
			if(iter==null||iter.hasNext()==false){
				getContext().addEmptyPigVariabel(outputTempFileName);
			}else{
				getContext().removeEmptyPigVariabel(outputTempFileName);

			}
		
		List<String> lineList = readDataFromIterator(iter);
		addContentForTheFileOf(fullPathFileName, lineList.toArray(new String[lineList.size()]));
		config.setVisualizationTypeClass(HadoopDataOperationConfig.HD_MULTIOUTPUT_VISUALIZATIONCLASS);
		
		fileOutputNames.put(fullPathFileName, new String[]{""});

 

		}
		
		 
	}

	private void deleteEmptyOutputFile(String fullPathFileName) {
		if(false == fullPathFileName.endsWith(HadoopFile.SEPARATOR)){
			fullPathFileName = fullPathFileName + HadoopFile.SEPARATOR; 
		}
		String successFile = fullPathFileName+"_SUCCESS";
		HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(successFile, hadoopConnection);
		 successFile = fullPathFileName+"_logs";
		HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(successFile, hadoopConnection);
		successFile = fullPathFileName+"_temporary";
		HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(successFile, hadoopConnection);
	}

	protected List<String> readDataFromIterator(Iterator<Tuple> iter)
			throws ExecException {
		List<String> lineList=new ArrayList<String>();
		AnalysisFileStructureModel hdfsm = getOutPutStructure();
		String quoteChar = "\"";
		String escChar = "\\";
		if(hdfsm instanceof AnalysisCSVFileStructureModel){
			  quoteChar = ((AnalysisCSVFileStructureModel)hdfsm).getQuoteChar();
			if(StringUtil.isEmpty(quoteChar)) {
				quoteChar = "\"";
			}
			  escChar =((AnalysisCSVFileStructureModel) hdfsm).getEscapChar(); 
			if(StringUtil.isEmpty(escChar)) {
				  escChar = "\\";
			}
		}
		String delimiter = HadoopUtility.getDelimiterValue(hdfsm);
		if(delimiter==null||delimiter.equals("")){
			delimiter=",";
		}
		
		int count=0;
		while(iter.hasNext()){
			Tuple tuple = iter.next();
			StringBuffer line=new StringBuffer();
			for(int i=0;i<tuple.size();i++){
				if(tuple.get(i)!=null){
					String value = DataType.toString(tuple.get(i));
					//double quote
					if(StringUtil.isEmpty(quoteChar)==false&&value.indexOf(delimiter)>-1){
						if(value.indexOf(quoteChar)>-1){
							value = value.replace(quoteChar, escChar+quoteChar); 
						}
						value = quoteChar+value+quoteChar;
					}
					line.append(value);
				}else{
					line.append("");
				}
				if(i!=tuple.size()-1){
					line.append(delimiter);
				}
			}
			
			if(count>=AlpineMinerConfig.HADOOP_LINE_THRESHOLD){
				break;
			}else{
				lineList.add(line.toString());
				count++;
			}
		}
		return lineList;
	}

	public static String[] filterIgnoredFile(String[] partialNames) {
		List<String> resultList = new ArrayList<String> ();
		for (int i = 0; i < partialNames.length; i++) {
			if(Ignored_Result_Files.contains(partialNames[i])==false){
				resultList.add(partialNames[i]) ;
			}
		}
		return resultList.toArray(new String[resultList.size()]);
	}

	protected List<String[]> addContentForTheFileOf(String fullPathFileName,
			String[] fileContent) {
		List<String[]> outputContent = outputFileSampleContents.get(fullPathFileName);
		if(null==outputContent){
			outputContent=new ArrayList<String[]>();
			outputFileSampleContents.put(fullPathFileName, outputContent);
		}
		outputContent.add(fileContent);
		return outputContent;
	}
	
	
	
    protected  void storeOutput(AlpinePigServer pigServer) throws IOException, Exception {
		storeOutputForTheGivenPigVaribleIntoGivenFile(pigServer,outputTempName,(null==resultLocaltion?"/":resultLocaltion)+resultsName); 
	}
	
	
	protected HadoopMultiAnalyticFileOutPut generateHadoopOutputForTheGivenFile(String fullPathFileName) {
		AnalysisFileStructureModel fileStructureModel=getOutPutStructure();
		
		HadoopMultiAnalyticFileOutPut output = new HadoopMultiAnalyticFileOutPut();
		output.setHadoopConnection(hadoopConnection);
		output.setHadoopFileStructureModel(fileStructureModel);
		if(!fileOutputNames.containsKey(fullPathFileName)){
			if(logger.isDebugEnabled()){
				logger.debug("There seems there is no data stored for the file of["+fullPathFileName+"]");
			}
		}
		output.setOutputFileNames(fileOutputNames.get(fullPathFileName));
		
		output.setOutputFileSampleContents(outputFileSampleContents.get(fullPathFileName));
		output.setOutputFolder(fullPathFileName);
//		output.setQuoteChar(fileStructureModel.getQuoteChar())  ;
//		output.setESCChar(fileStructureModel.getEscapChar()) ;
//		output.setDelimiter( HadoopUtility.getDelimiterValue(fileStructureModel));
		
		if(getContext().isLocalModelPig() ==true){
			output.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
		}
		return output;
	}
	
	protected HadoopMultiAnalyticFileOutPut generateHadoopOutput() {
		return generateHadoopOutputForTheGivenFile((null==resultLocaltion?"/":resultLocaltion)+resultsName);
	}

	
 	protected AnalysisFileStructureModel getOutPutStructure() {
		AnalysisFileStructureModel newModel = generateNewFileStructureModel(hadoopSource.getHadoopFileStructureModel());
 
		newModel.setColumnNameList(hadoopSource.getHadoopFileStructureModel().getColumnNameList());
		newModel.setColumnTypeList(hadoopSource.getHadoopFileStructureModel().getColumnTypeList());
		return newModel; 
	}
	

	
	protected String getOldColumnType(AnalysisFileStructureModel oldModel,String columnName){
		List<String> columnNameList = oldModel.getColumnNameList();
		List<String> columnTypeList = oldModel.getColumnTypeList();
		if(columnNameList!=null){
			for(int i=0;i<columnNameList.size();i++){
				if(columnNameList.get(i).equals(columnName)){
					return columnTypeList.get(i);
				}
			}
		}
		return null;
	}
 

	//for step run
	public void setSaveResult(boolean saveResult) { 
		this.saveResult = saveResult;
		
	}
}
