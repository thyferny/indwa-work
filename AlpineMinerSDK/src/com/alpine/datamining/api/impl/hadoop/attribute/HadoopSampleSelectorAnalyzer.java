/**
 * ClassName HadoopSampleSelectorAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-10
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopSampleSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutSampling;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.hadoop.ext.CSVRecordParser;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
/**
 * @author Jeff Dong
 *
 */
public class HadoopSampleSelectorAnalyzer extends
		AbstractHadoopAttributeAnalyzer {
	private static Logger itsLogger=Logger.getLogger(HadoopSampleSelectorAnalyzer.class);
	
	private AnalysisFileStructureModel hadoopFileStructureModel;
	private HadoopSampleSelectorConfig hadoopSampleSelectorConfig;
	private HadoopAnalyticSource hadoopAnalyticSource;
    private AlpinePigServer pigServer;
	private String filePath;

	private List<String> lineList;
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException  {
		try {
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("Started running");
			}
			
			initSampleSelector(source);
			
			lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(hadoopSampleSelectorConfig.getSelectedFile(), hadoopConnection,
					Integer.parseInt(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT) ));
			
		} catch (Exception e) {
			throw new AnalysisException("Could not complete the operation due to the exception of",e);
		}
		outputTempName=getContext().getPigVariables().get(hadoopSampleSelectorConfig.getSelectedFile());
		
		AnalyzerOutPutSampling output = new AnalyzerOutPutSampling();
		output.setHadoopSampling(true);
		output.setSampleTables(convertHadoopSamplesToSampleTables());
		output.setAnalyticNodeMetaInfo(createNodeMetaInfo(hadoopSampleSelectorConfig.getLocale()));
		return output;
	}
	


	private void initSampleSelector(AnalyticSource source) throws AnalysisException {
		hadoopAnalyticSource=(HadoopAnalyticSource)source;
		
		init(hadoopAnalyticSource);
		setOutputTempName(hadoopSource.getInputTempName());
		hadoopSampleSelectorConfig = (HadoopSampleSelectorConfig)hadoopAnalyticSource.getAnalyticConfig();
		hadoopFileStructureModel = hadoopAnalyticSource.getHadoopFileStructureModel();
		
		filePath=hadoopSampleSelectorConfig.getSelectedFile();
		
		setPigServer(acquirePigServerInfo(hadoopSource));
		
		String result = getContext().getPigVariableNameForTheFileOf(filePath);
		config.setResultsLocation(filePath);
		config.setResultsName(result);
		
		
		
		setOutputTempName(result);
	
	}



    protected AlpinePigServer acquirePigServerInfo(HadoopAnalyticSource hadoopSource)
			throws AnalysisException {
		String errString;
		try {
			if((null==hadoopSource||null==hadoopSource.getHadoopInfo()||null==getContext())){
				if(null==pigServer){
					throw new AnalysisException("Can not acquire pigserver since hadopsource, hadoopinfo and or context along pigserver itself null");	
				}
				return pigServer;
			}
            AlpinePigServer pServer = getContext().getPigServer(hadoopSource.getHadoopInfo());
			if (null == pServer) {
				errString = "Could not acquire Pig Server ";
				throw new AnalysisException(errString);
			}
			return pServer;
		} catch (Exception e) {
			errString = "Could not acquire Pig Server ";
			throw new AnalysisException(errString, e);
		}
	}

	
	private List<AnalyzerOutPutTableObject> convertHadoopSamplesToSampleTables() {
		List<AnalyzerOutPutTableObject> theList=new ArrayList<AnalyzerOutPutTableObject>();
		
		List<String> columnNameList = hadoopFileStructureModel.getColumnNameList();
		List<String> columnTypeList = hadoopFileStructureModel.getColumnTypeList();
		
		if(lineList!=null){
			AnalyzerOutPutTableObject inst = new AnalyzerOutPutTableObject();
			//Adding table meta info
			List<TableColumnMetaInfo> tableMetaInfo = new ArrayList<TableColumnMetaInfo>();
			for (int i = 0; i < columnNameList.size(); i++) {
				TableColumnMetaInfo ti = new TableColumnMetaInfo(
						columnNameList.get(i), columnTypeList.get(i));
				tableMetaInfo.add(ti);
			}
			
			DataTable dataTable = new DataTable();
			

	 		CSVRecordParser csvParser = createCSVParser(hadoopFileStructureModel);
	 		String delimiter = HadoopUtility.getDelimiterValue(hadoopFileStructureModel);
			if(delimiter==null||delimiter.equals("")){
				delimiter=",";
			}
			
			List<DataRow> dr = new ArrayList<DataRow>();
			int rcount=0;
			//Reading each line and then splitting each column with the delimeter
			if (null != lineList) {
				for (int i = 0; i < lineList.size(); i++) {
						DataRow rd = new DataRow();
						try {
							rd.setData(parseTheLines(lineList.get(i),delimiter,csvParser));
							rd.setSequence(rcount++);
							dr.add(rd);
						} catch (IOException e) {
							itsLogger.error("Could not parse the line of[].Will skip this line and will work on the next line",e);
					}
				}
			}
			dataTable.setRows(dr);
			dataTable.setSchemaName(hadoopSampleSelectorConfig.getSelectedFile());
			dataTable.setColumns(tableMetaInfo);
			inst.setDataTable(dataTable);
			theList.add(inst);
		}		
		return theList;
	}
	
	private static String[] parseTheLines(String sr, String delimeter,CSVRecordParser csvParser) throws IOException {
		if(null!=csvParser)
			return csvParser.parseLine(sr);
		return sr.split(delimeter);
	}

	private static CSVRecordParser createCSVParser(AnalysisFileStructureModel fsModel){
		
		CSVRecordParser csvParser =null;
		if(null!=fsModel){
			
			String quoteChar = "\"";
			String escChar = "\\";
			if(fsModel instanceof AnalysisCSVFileStructureModel){
				  quoteChar = ((AnalysisCSVFileStructureModel)fsModel).getQuoteChar();
				if(StringUtil.isEmpty(quoteChar)) {
					quoteChar = "\"";
				}
				  escChar =((AnalysisCSVFileStructureModel) fsModel).getEscapChar(); 
				if(StringUtil.isEmpty(escChar)) {
					  escChar = "\\";
				}
			}
			String delimiter = HadoopUtility.getDelimiterValue(fsModel);
			if(delimiter==null||delimiter.equals("")){
				delimiter=",";
			}
 
			csvParser = new CSVRecordParser(delimiter.charAt(0), 
					quoteChar.charAt(0), escChar.charAt(0));
		}
		return csvParser;
		
	}
	
	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SAMPLEING_SELECTOR_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SAMPLEING_SELECTOR_DESCRIPTION,locale));
		return nodeMetaInfo;
	}

	public AnalysisFileStructureModel getHadoopFileStructureModel() {
		return hadoopFileStructureModel;
	}

	public void setHadoopFileStructureModel(
			AnalysisFileStructureModel hadoopFileStructureModel) {
		this.hadoopFileStructureModel = hadoopFileStructureModel;
	}

	public HadoopSampleSelectorConfig getHadoopSampleSelectorConfig() {
		return hadoopSampleSelectorConfig;
	}

	public void setHadoopSampleSelectorConfig(
			HadoopSampleSelectorConfig hadoopSampleSelectorConfig) {
		this.hadoopSampleSelectorConfig = hadoopSampleSelectorConfig;
	}

	public HadoopAnalyticSource getHadoopAnalyticSource() {
		return hadoopAnalyticSource;
	}

	public void setHadoopAnalyticSource(HadoopAnalyticSource hadoopAnalyticSource) {
		this.hadoopAnalyticSource = hadoopAnalyticSource;
	}

    public AlpinePigServer getPigServer() {
		return pigServer;
	}

    public void setPigServer(AlpinePigServer alpineRestPigServer) {
        this.pigServer = alpineRestPigServer;
	}
}
