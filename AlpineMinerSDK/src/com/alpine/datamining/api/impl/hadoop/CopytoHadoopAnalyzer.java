/**
 * ClassName  DBTableSelector.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.fs.HDFSFileCompressHelper;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 * 
 */
public class CopytoHadoopAnalyzer extends AbstractHadoopAnalyzer {

	public static final String SEPRATOR = ",";
	public static final String QUOTE_CHAR = "\"";
	public static final String ESC_CHAR = "\\";
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.DataAnalyzer#doAnalysis(com.alpine.datamining
	 * .api.AnalyticSource)
	 */
	private static Logger itsLogger = Logger
			.getLogger(CopytoHadoopAnalyzer.class);

	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		// 1 do the import
		CopytoHadoopConfig config = (CopytoHadoopConfig) source
				.getAnalyticConfig();
		config.setVisualizationTypeClass(HadoopDataOperationConfig.HD_MULTIOUTPUT_VISUALIZATIONCLASS);
		DataBaseAnalyticSource dbSource = (DataBaseAnalyticSource) source;

		String ifFileExists = config.getIfFileExists();
		HadoopConnection hadoopConenction = createConnection(config);
		boolean fileExists = HadoopHDFSFileManager.INSTANCE.exists(
				config.getHadoopFileName(), hadoopConenction);
		try {
			if (ifFileExists.equals(CopytoHadoopConfig.OPTION_DROP)	 ) {
				if(fileExists == true){
					
					boolean success = HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(
						config.getHadoopFileName(), hadoopConenction);
					if(success==false){
						throw new Exception("Can not delete out put directory "+config.getHadoopFileName());
					}

				}
				copyTableIntoHadoopFile(dbSource, config.getHadoopFileName(),
						hadoopConenction,false);
				
				
				
			} else if (ifFileExists.equals(CopytoHadoopConfig.OPTION_SKIP)
					  ) {
				if(fileExists == false){
					HadoopHDFSFileManager.INSTANCE.createHadoopFile(
							config.getHadoopFileName(), hadoopConenction);
					copyTableIntoHadoopFile(dbSource, config.getHadoopFileName(),
							hadoopConenction,false);
				}

			} else if (ifFileExists.equals(CopytoHadoopConfig.OPTION_ERROR)
					) {
				if( fileExists == true){
					throw new AnalysisException("File already existed !");
				}else{
					HadoopHDFSFileManager.INSTANCE.createHadoopFile(
							config.getHadoopFileName(), hadoopConenction);
					copyTableIntoHadoopFile(dbSource, config.getHadoopFileName(),
							hadoopConenction,false);
				}
			} 
			else if (ifFileExists.equals(CopytoHadoopConfig.OPTION_APPEND)) {
				// always append since we have create a new file if drop
				if (fileExists == false) {
					HadoopHDFSFileManager.INSTANCE.createHadoopFile(
							config.getHadoopFileName(), hadoopConenction);

				}
				copyTableIntoHadoopFile(dbSource, config.getHadoopFileName(),
						hadoopConenction,true);
			}

			return generateOutPut(source);
		} catch (Exception e) {
			String errorMsg= e.getMessage();
			if(errorMsg!=null&&errorMsg.indexOf("Append is not supported") > 0){
				errorMsg = SDKLanguagePack.getMessage(SDKLanguagePack.Append_Not_Supported, config.getLocale()) ;
			}
			itsLogger.error(e.getLocalizedMessage(), e) ;
			throw new AnalysisException(errorMsg);
		}

		// 2 load for next ...

	}



	private HadoopConnection createConnection(CopytoHadoopConfig config) {
		HadoopConnection hadoopConenction = new HadoopConnection(
				config.getConnName(), config.getUserName(),
				config.getGroupName(), config.getHdfsHostname(),
				Integer.parseInt(config.getHdfsPort()),
				config.getHadoopVersion(), config.getJobHostname(),
				Integer.parseInt(config.getJobPort()),
				config.getSecurityMode(),
				config.getHdfsPrincipal(),
				config.getHdfsKeyTab(),
				config.getMapredPrincipal(),
				config.getMapredKeyTab()
				);
		return hadoopConenction;
	}

 

	private void copyTableIntoHadoopFile(DataBaseAnalyticSource dbSource,
			String hadoopFileName, HadoopConnection hadoopConenction,boolean append)
			throws Exception {

		int columnNumber = dbSource.getTableInfo().getColumnNames().size();
		ResultSet rs = null;
		OutputStream out = null;
		Connection connection = dbSource.getConnection();
		try {
 
			 
			List<TableColumnMetaInfo> columnInfoList = dbSource.getTableInfo().getColumns(); 
			String dbType = dbSource.getDataBaseInfo().getSystem();
			
			FileSystem fs = HadoopHDFSFileManager.INSTANCE
					.getHadoopFileSystem(hadoopConenction);
			if(append==true){
				out = HDFSFileCompressHelper.INSTANCE.appendOutputStream(fs, HadoopHDFSFileManager.INSTANCE.getHadoopPath(
						hadoopConenction, hadoopFileName));
			}else{
				out = HDFSFileCompressHelper.INSTANCE.generateOutputStream(fs, HadoopHDFSFileManager.INSTANCE.getHadoopPath(
						hadoopConenction, hadoopFileName));
			}
			
			String inputTableName = getQuotaedTableName(dbSource.getTableInfo()
					.getSchema(), dbSource.getTableInfo().getTableName());

			String sql = generateSelectSql(inputTableName,columnInfoList);
			
			PreparedStatement ps = connection.prepareStatement(sql,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			 
			ps.setFetchSize(2000);
			rs = ps.executeQuery();
			
			StringBuilder sb = new StringBuilder();
			rs.setFetchSize(2000);
			int step = 0;

		
			String value =null;
			DataSourceType dataSourceType = DataSourceType.getDataSourceType(dbType) ; 
		 boolean nothing = true;
			 
			while (rs.next()) {
			 
				step = step + 1;
				for (int i = 0; i < columnNumber; i++) {
				 

					if(rs.getString(i + 1)==null){
						value="" ;
					}
					else 	if(dataSourceType.isLongColumnType(columnInfoList.get(i).getColumnsType())==true){
						value = String.valueOf(rs.getLong(i + 1));
					}else 	if(dataSourceType.isIntegerColumnType(columnInfoList.get(i).getColumnsType())==true){
						value = String.valueOf(rs.getInt(i + 1));
					}
					else if(dataSourceType.isFloatColumnType(columnInfoList.get(i).getColumnsType())==true){
						value = String.valueOf(rs.getFloat(i + 1));
					}
					else if(dataSourceType.isDoubleColumnType(columnInfoList.get(i).getColumnsType())==true){
						value = String.valueOf(rs.getDouble(i + 1));
					}
					else{
						value = rs.getString(i + 1);
						if(value.startsWith(QUOTE_CHAR)){
							value = ESC_CHAR+value;
						}
						if(value.endsWith(QUOTE_CHAR)){
							value=value.substring(0,value.length()-1)+ESC_CHAR+QUOTE_CHAR;
						}
			    		value = value.replaceAll("\\n", " ");
						value = QUOTE_CHAR+value+QUOTE_CHAR;
					}
					
					sb.append(value);
					if (i < columnNumber - 1) {
						sb.append(SEPRATOR);
					}
				}

				sb.append("\n");
				if (step == 20000/columnNumber) {
					out.write(sb.toString().getBytes());
					sb.setLength(0);
				 
					step = 0;
				}
				nothing=false;
			}
			if(nothing==true){
				throw new Exception(EMPTY_INPUT_MSG);

			}
			out.write(sb.toString().getBytes());
			
		} finally {
			if (rs != null) {
				rs.close();

			}
			if (out != null) {
				
				out.flush();
				out.close();

			}
		 
		}
	}
	
	private String generateSelectSql(String inputTableName,List<TableColumnMetaInfo>  columnInfoList) {
		StringBuilder columnNames = new StringBuilder();
		
		for (TableColumnMetaInfo tableColumnMetaInfo : columnInfoList) {
			columnNames.append(StringHandler.doubleQ( tableColumnMetaInfo.getColumnName() ) ).append(",") ;
		}
		columnNames.deleteCharAt(columnNames.length()-1);
		return "  SELECT " +
				 columnNames +
				" FROM  " + inputTableName;
	}

	private HadoopMultiAnalyticFileOutPut generateOutPut(AnalyticSource source)
			throws Exception {

		CopytoHadoopConfig config = (CopytoHadoopConfig) source
				.getAnalyticConfig();

		AnalysisCSVFileStructureModel fileStructureModel = (AnalysisCSVFileStructureModel) config
				.getHadoopFileStructure();

		HadoopConnection hadoopConnection = createConnection(config);
		
		loadFileIntoPig(hadoopConnection,config);

		HadoopMultiAnalyticFileOutPut output = new HadoopMultiAnalyticFileOutPut();
		output.setHadoopConnection(hadoopConnection);
		AnalysisCSVFileStructureModel newFileStructureModel = fileStructureModel.clone();
		newFileStructureModel.setDelimiter("Comma") ;
		newFileStructureModel.setQuoteChar(QUOTE_CHAR) ;
		newFileStructureModel.setEscapChar(ESC_CHAR) ;
		output.setHadoopFileStructureModel(newFileStructureModel);
		String fileName = config.getHadoopFileName();   
		String[] fileNames = new String[] { fileName.substring(fileName.lastIndexOf(HadoopFile.SEPARATOR)+1,fileName.length()) }; 
		output.setOutputFileNames(fileNames);

		List<String[]> datas = new ArrayList<String[]>();

		List<String> lineList = HadoopHDFSFileManager.INSTANCE.readHadoopPathToLineList(config.getHadoopFileName(),
				hadoopConnection, 1+Integer.parseInt(ProfileReader
						.getInstance().getParameter(
								ProfileUtility.UI_TABLE_LIMIT)));
		if (lineList.size()>0) {
			//this is very special ... 
//			output.setDelimiter(SEPRATOR);
//			output.setQuoteChar(QUOTE_CHAR);
//			output.setESCChar(ESC_CHAR);
			//output.setStartIndex (1);
			datas.add(lineList.toArray(new String[lineList.size()]));
		 
		}
		output.setOutputFileSampleContents(datas);
		output.setOutputFolder("");

		AnalyticNodeMetaInfo nodeMetaInfo = createNodeMetaInfo(output,
				config.getLocale());

		output.setAnalyticNodeMetaInfo(nodeMetaInfo);
		return output;
	}

	private void loadFileIntoPig(HadoopConnection hadoopConnection,
			CopytoHadoopConfig config) throws Exception {
		String fileName = config.getHadoopFileName();
		 
		String hostName = config.getHdfsHostname();
		AnalysisFileStructureModel fileStructureModel = (AnalysisFileStructureModel)config.getHadoopFileStructure();
		List<String> columnNameList = fileStructureModel.getColumnNameList();
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
		
//		String delimiterValue = HadoopUtility.getDelimiterValue(fileStructureModel);
		
		String pureFileName=  getOutputTempName();
		StringBuilder sbHeaderLine = new StringBuilder();	 
			AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
			StringBuffer header=new StringBuffer();
			for(int i=0;i<columnNameList.size();i++){
				sbHeaderLine.append(columnNameList.get(i)) ;
				sbHeaderLine.append(SEPRATOR);
				header.append(columnNameList.get(i)).append(":");
				header.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));
				header.append(SEPRATOR);
			}
			if(header.length()>0){
				header=header.deleteCharAt(header.length()-1);
				sbHeaderLine=sbHeaderLine.deleteCharAt(sbHeaderLine.length()-1);
			}
			String fileURI="hdfs://"+hostName+":"+config.getHdfsPort()+fileName;
		 
			String script = pureFileName+" = load '"+fileURI+ "' USING " +getPigStorageScript(hadoopConnection, fileStructureModel, fileName) ;
			
			 
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug(script);
			}
			
		    pigServer.registerQuery(script);	
	 
		
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(
			HadoopMultiAnalyticFileOutPut output, Locale locale) {

		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.HD_FILE_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.HD_FILE_NAME_DESCRIPTION, locale));

		Properties props = new Properties();

		// filename filepath
		props.setProperty(SDKLanguagePack.getMessage(
				SDKLanguagePack.HD_FILE_PATH, locale), output.getOutputFolder()
				+ output.getOutputFileNames()[0]);

		nodeMetaInfo.setProperties(props);

		return nodeMetaInfo;
	}

	public void loadFileToPig(DataBaseAnalyticSource source) throws Exception {
		CopytoHadoopConfig config = (CopytoHadoopConfig) source
				.getAnalyticConfig();
		HadoopConnection hadoopConnection = createConnection(config);

		loadFileIntoPig(  hadoopConnection,  config) ;
	}

}
