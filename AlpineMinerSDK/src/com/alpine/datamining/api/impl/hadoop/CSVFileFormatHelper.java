/**
 * ClassName CSVFileFormatHelper.java
 *
 * Version information: 1.00
 *
 * Date: Oct 30, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.ext.RecordParserFactory;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

/**
 * @author John Zhao
 *
 */
public class CSVFileFormatHelper implements FileFormatHelper {

	private AnalysisCSVFileStructureModel fileStructureModel;
	private HadoopAnalyticSource hadoopSource;
	private boolean isIncludeHeader =false; 

	/**
	 * @param fileStructureModel
	 * @param hadoopSource 
	 */
	public CSVFileFormatHelper(AnalysisCSVFileStructureModel fileStructureModel, HadoopAnalyticSource hadoopSource) { 
		this.fileStructureModel = fileStructureModel ;
		this.hadoopSource=hadoopSource;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.FileFormatHelper#initHadoopConfig(org.apache.hadoop.conf.Configuration)
	 */
	@Override
	public void initHadoopConfig(Configuration hadoopConf) throws AnalysisException {
		hadoopConf.set(AlpineHadoopConfKeySet.DELIMITER_CHAR,HadoopUtility.getDelimiterValue(fileStructureModel));
		
		hadoopConf.set(AlpineHadoopConfKeySet.ESC_CHAR, (fileStructureModel).getEscapChar());
		hadoopConf.set(AlpineHadoopConfKeySet.QUOTE_CHAR, (fileStructureModel).getQuoteChar());
 
		initConfig4HeaderLine(hadoopConf,AlpineHadoopConfKeySet.HEADER_LINE_VALUE);	
		
		hadoopConf.set(AlpineHadoopConfKeySet.INPUT_FORMAT_KEY, RecordParserFactory.INPUT_FORMAT_VALUE_CSV) ;
		 
		
	}

	protected void initConfig4HeaderLine(Configuration hadoopConf,String headerKey ) throws AnalysisException {
		if (fileStructureModel.getIsFirstLineHeader().equalsIgnoreCase("true")) {
			
			try {
				String headerLine = "";
				List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(hadoopSource.getFileName(),
								hadoopSource.getHadoopInfo(), 1);
				if(lineList.size()>0){
					headerLine=lineList.get(0);
				}
				if(StringUtil.isEmpty(headerLine)==false){
					isIncludeHeader = true;
				}
				hadoopConf.set(headerKey,
						headerLine);
			} catch (Exception e) {
				 
				throw new AnalysisException("can not solve header line:"+hadoopSource.getFileName());
			}
		} else {
			hadoopConf.set(headerKey, "");
		}
	}

 
	 

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.hadoop.FileFormatHelper#setInputFormatClass(org.apache.hadoop.mapreduce.Job)
	 */
	@Override
	public void setInputFormatClass(Job job) {
	 //nothing to do , will use default textinputformat
	}

	public boolean isIncludeHeader() {
		return isIncludeHeader;
	}

}
