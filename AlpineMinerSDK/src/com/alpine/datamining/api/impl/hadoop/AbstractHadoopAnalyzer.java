/**
 * ClassName AbstractHadoopAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-25
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.pig.PigServer;

import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisJSONFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisLogFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisXMLFileStructureModel;
import com.alpine.hadoop.ext.LogParserFactory;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
/** 
 * @author john zhao
 *
 */
public abstract class AbstractHadoopAnalyzer extends AbstractAnalyzer{

 
	
	public static final String EMPTY_INPUT_MSG= "The input from the preceding operator was empty. Please check the input file or the filter condition." ;
    private static final String PIG_STORAGE081 = "PigStorage081";
    private static final String PIG_STORAGE010 = "PigStorage010";
	private static final String LOG4J_PIG_STORAGE_010 = "LogPigStorage010";
	private static final String LOG4J_PIG_STORAGE_081 = "LogPigStorage081";

	private static Logger itsLogger= Logger.getLogger(AbstractHadoopAnalyzer.class);
	
	public static final String PREDICT_SEP_CHAR = "_";
	public static final String PREDICTION_NAME_P = "P";
	public static final String PREDICTION_NAME_C = "C";

	public static final String BAD_VALUE_DEFAULT = PREDICTION_NAME_C + PREDICT_SEP_CHAR+"Other";
	//this is used for temp file for pig...
	public static final String OUT_PREFIX="FILE_" ;

	private static final String JSON_STORAGE_081 = "JsonPigStorage081";  
	private static final String JSON_STORAGE_010 = "JsonPigStorage010";

	private static final String XML_STORAGE_081 = "XMLPigStorage081";

	private static final String XML_STORAGE_010 = "XMLPigStorage010";     

	
	public String getOutputTempName() {
		if(null==getUUID()){
			setUUID(UUID.randomUUID().toString().replace("-", ""));
		}
		return  OUT_PREFIX+getUUID().replace(".", ""); 
	}
	
    protected void runPigScript(AlpinePigServer pigServer, String pigScript)
			throws IOException {
		String[] pigLines=pigScript.split("\n");

		for(String line:pigLines){
		    pigServer.registerQuery(line);
		    if(itsLogger.isDebugEnabled()){
		        itsLogger.debug("Registered the query of["+line+"]");
		    }
		}
	}
	
	protected String getPigStorageScript (HadoopConnection hadoopConnection, AnalysisFileStructureModel fileStructureModel,String fileName) throws Exception {
		if(fileStructureModel instanceof AnalysisCSVFileStructureModel ){
			return getPigStorageScript4CSV(hadoopConnection, (AnalysisCSVFileStructureModel)fileStructureModel, fileName) ;
		}else if(fileStructureModel instanceof AnalysisXMLFileStructureModel ){
			return getPigStorageScript4XML(hadoopConnection, (AnalysisXMLFileStructureModel)fileStructureModel, fileName) ;
		}else if(fileStructureModel instanceof AnalysisLogFileStructureModel ){
			return getPigStorageScript4Log(hadoopConnection, (AnalysisLogFileStructureModel)fileStructureModel, fileName) ;
		}else if(fileStructureModel instanceof AnalysisJSONFileStructureModel ){
			return getPigStorageScript4JSON(hadoopConnection, (AnalysisJSONFileStructureModel)fileStructureModel, fileName) ;
		}else{
			throw new Exception ("File format not supported!");
		}
		
				
				
				
				
	}
	private String getPigStorageScript4JSON(HadoopConnection hadoopConnection,
			AnalysisJSONFileStructureModel fileStructureModel, String fileName) {
		StringBuffer header = createPigSchema(fileStructureModel);
		String pigStorageFunction = "" ;
		if(hadoopConnection.getVersion().equals(HadoopConstants.VERSION_0_20_2_CDH3_U4)){
			pigStorageFunction = JSON_STORAGE_081;
		}else{
			pigStorageFunction = JSON_STORAGE_010;
			 
		}
		String typeString = generatePigTypeString(fileStructureModel) ;

		StringBuilder jsonPathListString = new StringBuilder();
		List<String> jsonPathList = fileStructureModel.getJsonPathList() ;  
		if(jsonPathList!=null){
			for (Iterator<String> iterator = jsonPathList.iterator(); iterator.hasNext();) {
				String jsonPath = iterator.next();
				jsonPathListString = jsonPathListString.append(jsonPath).append(",");
			}
			if(jsonPathListString.length()>0){
				jsonPathListString =jsonPathListString.deleteCharAt(jsonPathListString.length()-1) ;
			}
		}
		String jsonDataStructureType = fileStructureModel.getJsonDataStructureType();
		String containerJsonPath = fileStructureModel.getContainerJsonPath();
		String	script =   pigStorageFunction +
				"('"+fileStructureModel.getContainer()+"','"+ jsonPathListString.toString()
				+"','"+jsonDataStructureType+"','"+typeString+"','"+containerJsonPath
				+"','"+HadoopConstants.Flow_Call_Back_URL+"','"+getFlowRunUUID()+"','"+getOperatorNameForPig() 
				+"') " + " as ("+header+");"; 
		
		return script;
	}

	protected String getPigStorageScript4Log(HadoopConnection hadoopConnection, AnalysisLogFileStructureModel fileStructureModel,String fileName) throws Exception {
		StringBuffer header = createPigSchema(fileStructureModel);
		
		String pigStorageFunction="";
		if(fileStructureModel.getLogType().equalsIgnoreCase(LogParserFactory.LOGTYPE_LOG4J)||
				(fileStructureModel.getLogType().equalsIgnoreCase(LogParserFactory.LOGTYPE_APACHE) ||
						LogParserFactory.LOG_TYPE_APACHE_WEB_SERVER.equalsIgnoreCase(fileStructureModel.getLogType()))){
		//	pigStorageFunction=LOG4J_PIG_STORAGE;
		}else{
			throw new IllegalArgumentException("Log format must be either["+LogParserFactory.LOGTYPE_APACHE+", or"+LogParserFactory.LOGTYPE_LOG4J+", or"+
					LogParserFactory.LOG_TYPE_APACHE_WEB_SERVER+"]");
		}
		
		String typeString = generatePigTypeString(fileStructureModel) ;
 
		
		if(hadoopConnection.getVersion().equals(HadoopConstants.VERSION_0_20_2_CDH3_U4)){
			pigStorageFunction  = LOG4J_PIG_STORAGE_081;
		}else{
			pigStorageFunction  = LOG4J_PIG_STORAGE_010;
			 
		}
		
		String logFormat=fileStructureModel.getLogFormat();
		String logFormatEscaped=logFormat.replace("\"", "\\\"");
		
		String	script =   pigStorageFunction +"('"+logFormatEscaped+"','"+fileStructureModel.getLogType()+"','"+ typeString 
				+"','"+HadoopConstants.Flow_Call_Back_URL+"','"+getFlowRunUUID()+"','"+getOperatorNameForPig() 
				+ "') " + " as ("+header+");"; 
		
		return script; 
		
	}
	
	protected String getPigStorageScript4XML(HadoopConnection hadoopConnection, AnalysisXMLFileStructureModel fileStructureModel,String fileName) throws Exception {
		StringBuffer header = createPigSchema(fileStructureModel);
		String pigStorageFunction = "" ;
		if(hadoopConnection.getVersion().equals(HadoopConstants.VERSION_0_20_2_CDH3_U4)){
			pigStorageFunction = XML_STORAGE_081;
		}
		else{
			pigStorageFunction = XML_STORAGE_010;
			 
		}
		String typeString = generatePigTypeString(fileStructureModel) ;
 

		StringBuilder xpathListString = new StringBuilder();
		List<String> xpathList = fileStructureModel.getxPathList() ;  
		if(xpathList!=null){
			for (Iterator<String> iterator = xpathList.iterator(); iterator.hasNext();) {
				String xpath = iterator.next();
				xpathListString = xpathListString.append( xpath).append(",");
			}
			if(xpathListString.length()>0){
				xpathListString =xpathListString.deleteCharAt(xpathListString.length()-1) ;
			}
		}
		String	script =   pigStorageFunction +
				"('"+fileStructureModel.getContainer()+"','"+ 
				xpathListString.toString()+  "','"+
				fileStructureModel.getAttrMode()+"','"+typeString+"','"
				+fileStructureModel.getXmlDataStructureType()+"','"+fileStructureModel.getContainerXPath()
								+"','"+HadoopConstants.Flow_Call_Back_URL+"','"+getFlowRunUUID()+"','"+getOperatorNameForPig()
+"') " 
				+ " as ("+header+");"; 
		 
		return script; 
	}
	
	protected String getPigStorageScript4CSV(HadoopConnection hadoopConnection, AnalysisCSVFileStructureModel fileStructureModel,String fileName) throws Exception {
			
			
		StringBuffer header = createPigSchema(fileStructureModel);
	 
			String headerLine ="";
			if(fileStructureModel.getIncludeHeader().equalsIgnoreCase("true")){
				List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(fileName, hadoopConnection,1);
				if(lineList.size()>0){
					headerLine = lineList.get(0); 
				}
			}
			if(headerLine!=null&&headerLine.endsWith("\n")){
				headerLine=headerLine.substring(0,headerLine.lastIndexOf("\n"))  ;
			}
		 
			byte escapeChar = Character.UNASSIGNED;	 
			if(StringUtil.isEmpty(fileStructureModel.getEscapChar() )==false){
				escapeChar = (byte) fileStructureModel.getEscapChar().charAt(0) ;
			}
			
			byte quoteChar = Character.UNASSIGNED;	
			if(StringUtil.isEmpty(fileStructureModel.getQuoteChar() )==false){
				quoteChar = (byte)fileStructureModel.getQuoteChar().charAt(0) ;
			}
	
			
			String pigStorageFunction = "" ;
			pigStorageFunction = getCSVPigStorageByVersion(hadoopConnection);
			String delimiterValue = HadoopUtility.getDelimiterValue(fileStructureModel);
			String typeString = generatePigTypeString(fileStructureModel) ;
			String	script =   pigStorageFunction +
					"('"+delimiterValue+"','"+headerLine  +"','"+ escapeChar+"','"+quoteChar+"','"+typeString
					+"','"+HadoopConstants.Flow_Call_Back_URL+"','"+getFlowRunUUID()+"','"+getOperatorNameForPig() 
					+"') " + " as ("+header+");"; 
			if(StringUtil.isEmpty(fileStructureModel.getEscapChar() )==true
					&&StringUtil.isEmpty(fileStructureModel.getQuoteChar() )==true){
				script = pigStorageFunction +
						"('"+delimiterValue+"','"+headerLine +"','"+typeString  
						+"','"+HadoopConstants.Flow_Call_Back_URL+"','"+getFlowRunUUID()+"','"+getOperatorNameForPig() 
						+"') " + " as ("+header+");"; 
				}
			return script; 
	}

	private String getOperatorNameForPig() {
		if(getName()!=null){
			return getName().replace("'", "\\'");
		}else{
			return null;
		}
	}

	protected String getCSVPigStorageByVersion(HadoopConnection hadoopConnection) {
		String pigStorageFunction = null;
        //TODO handle this
		if(hadoopConnection.getVersion().equals(HadoopConstants.VERSION_0_20_2_CDH3_U4)){
            pigStorageFunction = PIG_STORAGE081;
		}else{
            pigStorageFunction = PIG_STORAGE010;
			 
		}
		return pigStorageFunction;
	}

	private StringBuffer createPigSchema(
			AnalysisFileStructureModel fileStructureModel) {
		StringBuffer header=new StringBuffer();
	
		List<String> columnNameList = fileStructureModel.getColumnNameList();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
	
		for(int i=0;i<columnNameList.size();i++){
			header.append(columnNameList.get(i)).append(":");
			header.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));
			header.append(",");
		}
		if(header.length()>0){
			header=header.deleteCharAt(header.length()-1);
		}
		return header;
	}
	
	private String generatePigTypeString(AnalysisFileStructureModel fileStructureModel){
		StringBuffer sb=new StringBuffer();
		
		List<String> columnNameList = fileStructureModel.getColumnNameList();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
	
		for(int i=0;i<columnNameList.size();i++){
			sb.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));
			sb.append("_");
		}
		if(sb.length()>0){
			sb=sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString(); 
	}
	
	protected AnalysisFileStructureModel generateNewFileStructureModel(AnalysisFileStructureModel oldModel){
//		AnalysisCSVFileStructureModel newModel = new AnalysisCSVFileStructureModel();
//		if(oldModel instanceof AnalysisCSVFileStructureModel ==false){
//			newModel.setDelimiter(AnalysisCSVFileStructureModel.DELIMITER[1]);
//			newModel.setOther("");
//			newModel.setEscapChar(AnalysisCSVFileStructureModel.ESCAP_VALUE);
//			newModel.setQuoteChar(AnalysisCSVFileStructureModel.QUOTE_VALUE);
//		}else{
//			newModel.setDelimiter(((AnalysisCSVFileStructureModel)oldModel).getDelimiter());
//			newModel.setOther(((AnalysisCSVFileStructureModel)oldModel).getOther());
//			newModel.setEscapChar(((AnalysisCSVFileStructureModel)oldModel).getEscapChar());
//			newModel.setQuoteChar(((AnalysisCSVFileStructureModel)oldModel).getQuoteChar());
//		}
		return createOutFileStructureModel();
	}
	
	protected AnalysisFileStructureModel createOutFileStructureModel(){
		AnalysisCSVFileStructureModel newModel = new AnalysisCSVFileStructureModel();
		newModel.setDelimiter(AnalysisCSVFileStructureModel.DELIMITER[1]);
		newModel.setOther("");
		newModel.setEscapChar(AnalysisCSVFileStructureModel.ESCAP_VALUE);
		newModel.setQuoteChar(AnalysisCSVFileStructureModel.QUOTE_VALUE);
		return newModel;
	}
	
	public void reportBadDataCount(long badDataLineCount, String callBackURL,
			String operatorName, String uuid) {
		
		itsLogger.debug("reportBadDataCount:" + uuid +":" +operatorName+":"+operatorName+":"+badDataLineCount);
		if(badDataLineCount==0||callBackURL==null||callBackURL.trim().length()==0||callBackURL.equalsIgnoreCase( "null")){
			return;
		}
		try{
		HttpClient httpclient = new HttpClient();
		String pKey = HadoopConstants.COUNT_BADDATA_MR  + operatorName ; 
		
		//operatorName could contains blank and cause problem 
		pKey =URLEncoder.encode(pKey);
		pKey =URLEncoder.encode(pKey);

			String url = callBackURL
					+ "/main/flowRunner?method=putFlowRunningProperty&uuid="
					+ uuid + "&pKey=" + pKey + "&pValue=" + badDataLineCount ;
		//avoid the blank error in url
		GetMethod getMethod = new GetMethod(url);
		
			getMethod.addRequestHeader("Accept", "text/plain");
			getMethod.addRequestHeader("Content-Type", "text/plain");
	
			httpclient.executeMethod(getMethod);
	 
				
				
			getMethod.releaseConnection();
		}catch(Exception e){
			//nothing to do
			//e.printStackTrace();
			itsLogger.error("Can not reportBadDataCount to "+callBackURL,e) ;
		}
		
	}
}
