/**
 * ClassName HadoopUnionRunner.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-4
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModelItem;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionSourceColumn;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.UnionKeySet;
import com.alpine.hadoop.union.UnionMapper;
import com.alpine.hadoop.union.UnionReducer;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

/**
 * @author Jeff Dong
 *
 */
public class HadoopUnionRunner extends AbstractHadoopRunner {

	 

	private static Logger itsLogger = Logger
			.getLogger(HadoopUnionRunner.class);
 

	protected HadoopUnionConfig config;
	
	private Configuration baseConf=new Configuration();
 
	protected String outputFileFullName;

	private ClusterStatus clusterStatus;
 

	private String unionType= null;
 

	public HadoopUnionRunner(AnalyticContext context,String operatorName) {
		super(context,operatorName);
	}

	@Override
	public int run(String[] args) throws Exception {
		
		Configuration unionConf = new Configuration(baseConf);
		initHadoopConfig(unionConf,HadoopConstants.JOB_NAME.UNION);
	 
		Class reducerClass = null;
		if(unionType.equals(AnalysisHadoopUnionModel.UNION_ALL)==false){
			reducerClass = UnionReducer.class;
		}
		Job UnionJob = createJob(HadoopConstants.JOB_NAME.UNION, unionConf, 
				UnionMapper.class,reducerClass , Text.class, Text.class, getUnionInputFiles(config.getUnionModel()) , outputFileFullName);
	 		
 		UnionJob.setMapOutputKeyClass(Text.class);
		runMapReduceJob(UnionJob,true);
		badCounter=UnionJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();
		 
		return 0 ;	 
	}
 

	@Override
	public Object runAlgorithm(AnalyticSource source) throws Exception {

		init((HadoopAnalyticSource) source);
		if(hdfsManager.exists(outputFileFullName,hadoopConnection)==true){
			if(Resources.YesOpt.equals(config.getOverride())){
				boolean success = dropIfExists(outputFileFullName);
				if(success==false){
					throw new Exception("Can not delete out put directory "+outputFileFullName);
				}
			}else{
				throw new Exception("File is existed");
			}		
		} 
	 
		 ToolRunner.run(this, null);
 

 		return null;
	}
	
	public void init(HadoopAnalyticSource hadoopSource) throws Exception {
		super.init(hadoopSource) ;

		try {
			this.clusterStatus  = HadoopConnection.getClusterInfo(hadoopConnection);
		} catch (IOException e) {
			itsLogger.error("Hadoop Cluster Status could not be obtained in  setup");
		}

		config = (HadoopUnionConfig) hadoopSource.getAnalyticConfig();

		String resultLocaltion = config.getResultsLocation();
		String resultsName = config.getResultsName();

		if (!StringUtil.isEmpty(resultLocaltion)
				&& resultLocaltion.endsWith(HadoopFile.SEPARATOR) == false) {
			resultLocaltion = resultLocaltion + HadoopFile.SEPARATOR;
		}
		outputFileFullName = resultLocaltion + resultsName;
		 
		baseConf.set(UnionKeySet.COLUMN_NAMES, getOutputColumnNames(config.getUnionModel()));
		baseConf.set(UnionKeySet.COLUMN_TYPES, getOutputColumnTypes(config.getUnionModel()));

		String firstTable = config.getUnionModel().getFirstTable();
		if(firstTable.startsWith("\"")&&firstTable.endsWith("\"")){
			firstTable=firstTable.substring(1,firstTable.length()-1);
		}
		baseConf.set(UnionKeySet.union_first_table, firstTable);
		
		unionType = config.getUnionModel().getSetType();
		baseConf.set(UnionKeySet.union_type, config.getUnionModel().getSetType());
		//file1,file2,file3
		baseConf.set(UnionKeySet.union_input_files,getUnionInputFiles(config.getUnionModel()));
		//file1.f1,file1.f2:file2.f1,file2.f2
		baseConf.set(UnionKeySet.union_input_real_files,getUnionInputRealFiles(config.getUnionModel()));
		

		//file1column1,file1column2:file2column1,file2columns2,
		baseConf.set(UnionKeySet.union_input_column_index,getUnionInputColumns(config ));
		baseConf.set(UnionKeySet.union_input_ids,getUnionInputIds(config.getUnionModel()));
		
		initHeaderLine4Union();

 
	}

	private void initHeaderLine4Union() throws AnalysisException {
		LinkedHashMap<String,  AnalysisFileStructureModel>  structModelList = config.getFileStructureModelList() ;	
		int i = 0;
		for (Iterator iterator = structModelList.keySet().iterator(); iterator.hasNext();) {
			String fileName = (String) iterator.next();
			AnalysisFileStructureModel model = structModelList.get(fileName) ;
			if("true".equalsIgnoreCase(model.getIsFirstLineHeader())){
				String firstLine = getFirstLine(model,fileName) ;
				if(firstLine!=null){
					baseConf.set(UnionKeySet.union_input_headerline+"_"+i,firstLine) ;
				 }

			}	
			i++;
		}
		 
	 
		
	}

	private String getFirstLine(
			AnalysisFileStructureModel analysisFileStructureModel,String fileName) throws AnalysisException {
 				
				try {
					String headerLine = "";
					List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(fileName,
									hadoopSource.getHadoopInfo(), 1);
					if(lineList.size()>0){
						headerLine=lineList.get(0);
					}
					return headerLine;
				 
				} catch (Exception e) {
					 
					throw new AnalysisException("Can not solve header line:"+hadoopSource.getFileName());
				}
		 
 
	}

	private String getUnionInputRealFiles(AnalysisHadoopUnionModel unionModel) throws Exception {

		StringBuffer sb = new StringBuffer();
		List<AnalysisHadoopUnionFile> files = unionModel.getUnionFiles();
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionFile analysisHadoopUnionFile = (AnalysisHadoopUnionFile) iterator
					.next();
			String filePath = analysisHadoopUnionFile.getFile();
			// could be a file, folder or wild char
			List<String> allFiles = hdfsManager.getAllRealFilePaths(filePath,
					hadoopConnection);
			if (allFiles == null ||allFiles.size()==0) {
				sb.append("null").append(",");

			} else {
				for (Iterator iterator2 = allFiles.iterator(); iterator2
						.hasNext();) {
					String fPath = (String) iterator2.next();
					sb.append(fPath).append(",");

				}
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append(":");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	private String getUnionInputIds(AnalysisHadoopUnionModel unionModel) {
		StringBuffer sb = new StringBuffer() ;
		List<AnalysisHadoopUnionFile> files = unionModel.getUnionFiles();
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionFile analysisHadoopUnionFile = (AnalysisHadoopUnionFile) iterator
					.next();
			sb.append(analysisHadoopUnionFile.getOperatorModelID()).append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length() -1) ;
		}
		return sb.toString();
 

	}

	private String getOutputColumnTypes(AnalysisHadoopUnionModel unionModel) {
		StringBuffer sb = new StringBuffer() ; 
		List<AnalysisHadoopUnionModelItem> outputColumns = unionModel.getOutputColumns() ;
		for (Iterator iterator = outputColumns.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionModelItem analysisHadoopUnionModelItem = (AnalysisHadoopUnionModelItem) iterator
					.next();
			sb.append(analysisHadoopUnionModelItem.getColumnType()).append(",") ;
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1) ;
		}
		 
		
		return sb.toString();
	}

	private String getOutputColumnNames(AnalysisHadoopUnionModel unionModel) {
		StringBuffer sb = new StringBuffer() ; 
		List<AnalysisHadoopUnionModelItem> outputColumns = unionModel.getOutputColumns() ;
		for (Iterator iterator = outputColumns.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionModelItem analysisHadoopUnionModelItem = (AnalysisHadoopUnionModelItem) iterator
					.next();
			sb.append(analysisHadoopUnionModelItem.getColumnName()).append(",") ;
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1) ;
		}
		 
		
		return sb.toString();
	}

	private String getUnionInputColumns(HadoopUnionConfig config ) {
		AnalysisHadoopUnionModel unionModel = config.getUnionModel();
		HashMap<String,List<String>>  resultMap= new HashMap<String,List<String>> ();
		List<AnalysisHadoopUnionModelItem> outputs = unionModel.getOutputColumns();
		for (Iterator iterator = outputs.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionModelItem analysisHadoopUnionModelItem = (AnalysisHadoopUnionModelItem) iterator
					.next();
			List<AnalysisHadoopUnionSourceColumn> columns = analysisHadoopUnionModelItem.getMappingColumns();
			for (Iterator iterator2 = columns.iterator(); iterator2.hasNext();) {
				AnalysisHadoopUnionSourceColumn analysisHadoopUnionSourceColumn = (AnalysisHadoopUnionSourceColumn) iterator2
						.next();
				String colName = analysisHadoopUnionSourceColumn.getColumnName();
				String modelID = analysisHadoopUnionSourceColumn.getOperatorModelID() ;
				if(resultMap.containsKey(modelID)==false){
					resultMap.put(modelID, new ArrayList<String>()) ;
				}
				resultMap.get(modelID).add(colName) ;
			}
			
		}
		 
		StringBuffer sb = new StringBuffer() ;
		HashMap<String, List<String>> inputColumnMap = config.getInputColumnMap();	
		List<AnalysisHadoopUnionFile> files = unionModel.getUnionFiles();
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionFile analysisHadoopUnionFile = (AnalysisHadoopUnionFile) iterator
					.next();
			String modelID = analysisHadoopUnionFile.getOperatorModelID();
			List<String> columnList = resultMap.get(modelID);
			 for (Iterator iterator2 = columnList.iterator(); iterator2
					.hasNext();) {
				String col = (String) iterator2.next();
				int index = inputColumnMap.get(modelID).indexOf(col) ;
				sb.append(index).append(",");
			}
 			sb.deleteCharAt(sb.length()-1) ;
			sb.append(":");
		}
 			sb.deleteCharAt(sb.length()-1) ;
		
		return sb.toString();
	}

	private String getUnionInputFiles(AnalysisHadoopUnionModel unionModel) {
		StringBuffer sb = new StringBuffer() ;
		List<AnalysisHadoopUnionFile> files = unionModel.getUnionFiles();
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			AnalysisHadoopUnionFile analysisHadoopUnionFile = (AnalysisHadoopUnionFile) iterator
					.next();
			sb.append(analysisHadoopUnionFile.getFile()).append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length() -1) ;
		}
		return sb.toString();
	}

	 
 
 
 
}
