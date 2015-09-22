/**
 * ClassName CopyToDBAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-23
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CopyToDBConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisJSONFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisLogFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisXMLFileStructureModel;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.datamining.utility.JDBCProperties;
import com.alpine.hadoop.ext.JSONRecordParser;
import com.alpine.hadoop.ext.JSONRecordReader;
import com.alpine.hadoop.ext.LogParserFactory;
import com.alpine.hadoop.ext.XMLRecordParser;
import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ddl.ICreateParameter;
import com.alpine.importdata.ddl.TableCreator;
import com.alpine.logparser.IAlpineLogParser;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author Jeff Dong
 * 
 */
public class CopyToDBAnalyzer extends DataOperationAnalyzer {

	private static final int BATCH_NUMBER = 1000; 

	private static Logger logger = Logger.getLogger(CopyToDBAnalyzer.class);

	private Connection connection;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		try {
			HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;

			String path = hadoopSource.getFileName();
			HadoopConnection hadoopConnection = hadoopSource.getHadoopInfo();
			final AnalysisFileStructureModel fileStructureModel = hadoopSource
					.getHadoopFileStructureModel();
			final CopyToDBConfig config = (CopyToDBConfig) source
					.getAnalyticConfig();

			InputStream inputStream = HadoopHDFSFileManager.INSTANCE
					.readHadoopFileToInputStream(path, hadoopConnection);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			DBDataUtil dbDataUtil = new DBDataUtil(connection,
					config.getSystem());
			if (dbDataUtil.isTableExist(config.getSchemaName(),
					config.getCopyToTableName()) == false) {
				createTable(fileStructureModel, config);
				insertData(fileStructureModel, config, reader);
			} else {
				String ifDataExists = config.getIfDataExists();
				if (ifDataExists.equals("Drop")) {
					AlpineUtil.dropTable(connection, config.getSchemaName(),
							config.getCopyToTableName(), Resources.TableType,
							config.getSystem());
					createTable(fileStructureModel, config);
					insertData(fileStructureModel, config, reader);
				} else if (ifDataExists.equals("Append")) {
					insertData(fileStructureModel, config, reader);
				} else if (ifDataExists.equals("Skip")) {
					// Do Nothing
				} else if (ifDataExists.equals("Error")) {
					AnalysisError error = new AnalysisError(this,
							AnalysisErrorName.COPYTODB_DROPERROR,
							config.getLocale(), config.getSchemaName() + "."
									+ config.getCopyToTableName());
					logger.error(error.getMessage(),error);
					throw error;
				}
			}

			DataBaseInfo dbInfo = new DataBaseInfo(config.getSystem(),
					config.getUrl(), config.getUserName(),
					XmlDocManager.decryptedPassword(config.getPassword()),
					config.getUseSSL());

			DatabaseConnection databaseConnection = getDBConnection(config);
			AnalyzerOutPutTableObject outPut = getResultTableSampleRow(
					databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config
					.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(config.getSchemaName());
			outPut.setTableName(config.getCopyToTableName());
			return outPut;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}
	}

	private DatabaseConnection getDBConnection(final CopyToDBConfig config) {
		JDBCProperties properties = DatabaseUtil.getJDBCProperties().get(
				DatabaseUtil.getDBSystemIndex(config.getSystem()));
		DatabaseConnection databaseConnection = DatabaseConnection
				.createDatabaseConnection(connection, config.getUrl(),
						properties);
		return databaseConnection;
	}

	private void insertData(
			final AnalysisFileStructureModel fileStructureModel,
			final CopyToDBConfig config, BufferedReader reader)
			throws SQLException, IOException, Exception {

		String sqlTemplate = buildSQL(fileStructureModel, config);

		connection.setAutoCommit(false);
		PreparedStatement ps = connection.prepareStatement(sqlTemplate);

		if (fileStructureModel instanceof AnalysisCSVFileStructureModel) {
			insertDataForCSV(
					(AnalysisCSVFileStructureModel) fileStructureModel, reader,
					ps);
		} else if (fileStructureModel instanceof AnalysisXMLFileStructureModel) {
			insertDataForXML(
					(AnalysisXMLFileStructureModel) fileStructureModel, reader,
					ps);
		} else if (fileStructureModel instanceof AnalysisJSONFileStructureModel) {
			insertDataForJSON(
					(AnalysisJSONFileStructureModel) fileStructureModel, reader,
					ps);
		}  else if (fileStructureModel instanceof AnalysisLogFileStructureModel) {
			insertDataForLog(
					(AnalysisLogFileStructureModel) fileStructureModel, reader,
					ps);
		} 

		try {
			ps.executeBatch();
			connection.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

		connection.setAutoCommit(true);
	}

	private void insertDataForJSON(
			AnalysisJSONFileStructureModel fileStructureModel,
			BufferedReader reader, PreparedStatement ps) throws Exception {
		JsonRecordReader parser = new JsonRecordReader(fileStructureModel.getContainer(),
				fileStructureModel.getJsonPathList(),reader,
				fileStructureModel.getJsonDataStructureType(),
				fileStructureModel.getContainerJsonPath());
		List<String[]> rowList;
		int index =0;
		while ((rowList = parser.readNext()) != null) {
			for(String[] row:rowList){
				fillDataIntoStatement(ps, row, fileStructureModel);
				index++;
				if (index == BATCH_NUMBER / row.length) {
					try {
						ps.executeBatch();
						connection.commit();
					} catch (SQLException e) {
						logger.error(e.getMessage(), e);
					}
					index = 0;
					ps.clearParameters();
				}
			}
		}
	}
	
	private void insertDataForLog(
			AnalysisLogFileStructureModel fileStructureModel,
			BufferedReader bufferedReader, PreparedStatement ps) throws Exception {
		IAlpineLogParser parser = LogParserFactory.createALogParser(fileStructureModel.getLogFormat(),fileStructureModel.getLogType());
		List<String[]> rowList=new ArrayList<String[]>();
		int index =0;
		String line;
		while ((line = bufferedReader.readLine()) != null) {
				String[] row=parser.processTheLine(line);
				if(null==row||0==row.length)continue;
				rowList.add(row);
				fillDataIntoStatement(ps, row, fileStructureModel);
				index++;
				if (index == BATCH_NUMBER / row.length) {
					try {
						ps.executeBatch();
						connection.commit();
					} catch (SQLException e) {
						logger.error(e.getMessage(), e);
					}
					index = 0;
					ps.clearParameters();
				}
		}
	}

	/**
	 * @param fileStructureModel
	 * @param reader
	 * @param ps
	 * @throws UnsupportedEncodingException 
	 */
	private void insertDataForXML(
			AnalysisXMLFileStructureModel fileStructureModel,
			BufferedReader reader, PreparedStatement ps) throws  Exception {

		XmlRecordReader parser =  new XmlRecordReader(fileStructureModel.getContainer(),
				 fileStructureModel.getxPathList(), reader,fileStructureModel.getAttrMode(),
				 fileStructureModel.getXmlDataStructureType(),fileStructureModel.getContainerXPath()) ;
		List<String[]> rowList;
		int index =0;
		while ((rowList = parser.readNext()) != null) {
			for(String[] row:rowList){
				fillDataIntoStatement(ps, row, fileStructureModel);
				index++;
				if (index == BATCH_NUMBER / row.length) {
					try {
						ps.executeBatch();
						connection.commit();
					} catch (SQLException e) {
						logger.error(e.getMessage(), e);
					}
					index = 0;
					ps.clearParameters();
				}
			}
		}
		
	}

	private void insertDataForCSV(
			final AnalysisCSVFileStructureModel fileStructureModel,
			BufferedReader reader, PreparedStatement ps) throws IOException,
			Exception, SQLException {
		char delimiter = HadoopUtility.getDelimiterValue(fileStructureModel)
				.toCharArray()[0];
		char quote = '\"';
		char escape = '\\';
		String escapChar = (fileStructureModel).getEscapChar();
		String quoteChar = (fileStructureModel).getQuoteChar();
		if (StringUtil.isEmpty(escapChar) == false) {
			escape = escapChar.charAt(0);
		}
		if (StringUtil.isEmpty(quoteChar) == false) {
			quote = quoteChar.charAt(0);
		}
		CSVReader parser = new CSVReader(reader, delimiter, quote, escape);
		String[] row;
		int index = 0;
		if (fileStructureModel.getIsFirstLineHeader().equalsIgnoreCase(
				Resources.TrueOpt)) {
			index++;
			parser.readNext();
		}
		while ((row = parser.readNext()) != null) {
			fillDataIntoStatement(ps, row, fileStructureModel);
			index++;
			if (index == BATCH_NUMBER / row.length) {
				try {
					ps.executeBatch();
					connection.commit();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
				index = 0;
				ps.clearParameters();
			}
		}
	}

	private void createTable(
			final AnalysisFileStructureModel fileStructureModel,
			final CopyToDBConfig config) throws Exception {
		ICreateParameter parameters = new ICreateParameter() {

			@Override
			public String getTableName() {
				return config.getCopyToTableName();
			}

			@Override
			public String getSchemaName() {
				return config.getSchemaName();
			}

			@Override
			public List<columnMetaInfo> getColumnMetaList() {
				List<columnMetaInfo> columnMetaList = new ArrayList<columnMetaInfo>();
				final List<String> columnNameList = fileStructureModel
						.getColumnNameList();
				final List<String> columnTypeList = fileStructureModel
						.getColumnTypeList();
				for (int i = 0; i < columnNameList.size(); i++) {
					final String columnType = columnTypeList.get(i);
					final String columnName = columnNameList.get(i);
					columnMetaList.add(new columnMetaInfo() {

						@Override
						public DatabaseDataType getColumnType() {
							if (columnType.equals(HadoopDataType.INT)) {
								return DatabaseDataType.INTEGER;
							} else if (columnType.equals(HadoopDataType.LONG)) {
								return DatabaseDataType.BIGINT;
							} else if (columnType.equals(HadoopDataType.DOUBLE)
									|| columnType.equals(HadoopDataType.FLOAT)) {
								return DatabaseDataType.DOUBLE;
							} else if (columnType
									.equals(HadoopDataType.CHARARRAY)) {
								return DatabaseDataType.VARCHAR;
							}
							return DatabaseDataType.VARCHAR;
						}

						@Override
						public String getColumnName() {
							return columnName;
						}
					});
				}
				return columnMetaList;
			}
		};
		TableCreator.getInstance(config.getUserName(),
				XmlDocManager.decryptedPassword(config.getPassword()),
				config.getUrl(), config.getSystem(), config.getLocale(),
				config.getUseSSL()).createTable(parameters);
	}

	private String buildSQL(AnalysisFileStructureModel fileStructureModel,
			CopyToDBConfig config) {
		StringBuilder sb = new StringBuilder("insert into \"")
				.append(config.getSchemaName()).append("\".\"")
		 	.append(config.getCopyToTableName()).append("\"");

		StringBuilder headerBuilder = new StringBuilder("("), valueBuilder = new StringBuilder(
				"(");
		List<String> columnNameList = fileStructureModel.getColumnNameList();

		for (int i = 0; i < columnNameList.size(); i++) {
			headerBuilder.append("\"").append(columnNameList.get(i))
					.append("\",");
			valueBuilder.append("?,");
		}
		headerBuilder.replace(headerBuilder.length() - 1,
				headerBuilder.length(), ")");
		valueBuilder.replace(valueBuilder.length() - 1, valueBuilder.length(),
				")");
		sb.append(headerBuilder).append(" values").append(valueBuilder);
		return headerBuilder.length() < 2 ? null : sb.toString();
	}

	private void fillDataIntoStatement(PreparedStatement ps, String[] data,
			AnalysisFileStructureModel fileStructureModel) throws Exception {
		List<String> columnTypeList = fileStructureModel.getColumnTypeList();
		for (int i = 0; i < data.length; i++) {
			String columnValue = data[i];
			if (columnValue != null) {
				columnValue = columnValue.trim();
			}
			if (columnTypeList.get(i).equals(HadoopDataType.INT)) {
				try {
					if (columnValue == null || "".equals(columnValue)) {
						ps.setNull(i+1, Types.INTEGER);
					} else {
						ps.setLong(i+1, Long.parseLong(columnValue));
					}
				} catch (Exception e) {
					//throw new RuntimeException("Cannot parse " + columnValue	+ " to integer");
					ps.setNull(i+1, Types.INTEGER);

				}
			} else if (columnTypeList.get(i).equals(HadoopDataType.LONG)) {
				try {
					if (columnValue == null || "".equals(columnValue)) {
						ps.setNull(i+1, Types.BIGINT);
					} else {
						ps.setLong(i+1, Long.parseLong(columnValue));
					}
				} catch (Exception e) {
					ps.setNull(i+1, Types.BIGINT);

//					throw new RuntimeException("Cannot parse " + columnValue		+ " to integer");
				}
			} else if (columnTypeList.get(i).equals(HadoopDataType.DOUBLE)) {
				try {
					if ((columnValue == null || "".equals(columnValue))) {
						ps.setNull(i+1, Types.DOUBLE);
					} else {
						ps.setDouble(i+1, Double.parseDouble(columnValue));
					}
				} catch (Exception e) {
					ps.setNull(i+1, Types.DOUBLE);

				//	throw new RuntimeException("Cannot parse " + columnValue + " to double");
				}
			} else if (columnTypeList.get(i).equals(HadoopDataType.FLOAT)) {
				try {
					if ((columnValue == null || "".equals(columnValue))) {
						ps.setNull(i+1, Types.FLOAT);
					} else {
						ps.setFloat(i+1, Float.parseFloat(columnValue));
					}
				} catch (Exception e) {
					ps.setNull(i+1, Types.FLOAT);

//					throw new RuntimeException("Cannot parse " + columnValue + " to float");
				}
			} else if (columnTypeList.get(i).equals(HadoopDataType.CHARARRAY)) {
			 
					if ((columnValue == null) || "".equals(columnValue)) {
						ps.setNull(i+1, Types.VARCHAR);
					} else {
						ps.setString(i+1, columnValue);
					}
	 
			}
		}
		ps.addBatch();
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.AGGREGATE_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.AGGREGATE_DESCRIPTION, locale));

		return nodeMetaInfo;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public static class JsonRecordReader  implements LocalFileRecordReader{ 		
		private static final byte[] LEFT_SQUARE_BRACKET= JSONRecordReader.LEFT_SQUARE_BRACKET;
		
		private static final byte[] RIGHT_SQUARE_BRACKET= JSONRecordReader.RIGHT_SQUARE_BRACKET;
		
		private static final byte[] LEFT_BRACE= JSONRecordReader.LEFT_BRACE;
		
		private static final byte[] RIGHT_BRACE= JSONRecordReader.RIGHT_BRACE;
		
		private static final byte[] DQ= JSONRecordReader.DQ;
		
		//this is only for array and array array ( must have container, so need skip it)
		boolean isContainerSkiped =false;

		private byte[] startTag;
		private byte[] startTag1;
		private BufferedReader fsin;
		private JSONRecordParser parser;
		private DataOutputBuffer buffer = new DataOutputBuffer();
		private String jsonDataStructureType;
		private boolean isInmatch2;
		
		public JsonRecordReader(String containerTagName,
				List<String> jsonPathList,
				BufferedReader fsin,
				String jsonDataStructureType,
				String containerJsonPath)
				throws UnsupportedEncodingException {
			if(StringUtil.isEmpty(containerTagName)==false){
				startTag = (StringHandler.doubleQ(containerTagName)+":").getBytes("utf-8");
				startTag1 = (StringHandler.doubleQ(containerTagName)+" :").getBytes("utf-8");
			}
			this.fsin  = fsin;
			parser =  new JSONRecordParser(jsonPathList,jsonDataStructureType,containerJsonPath);
			this.jsonDataStructureType=jsonDataStructureType;
		}
		
		public String readARecordString() throws  Exception {
			return readStandardRecord();
		}

		private String readLineRecord() throws IOException {
			try {
				while (true) {
					int b = fsin.read();
					// end of file:
					if (b == -1)
						return null;
					if(b == 10){//"\n"
						break;
					}
					// save to buffer:
					buffer.write(b);
				}
				return new String(buffer.getData(),"utf-8").trim();
			} finally{
				buffer.reset();
			}
		}

		private String readStandardRecord() throws Exception {
			if (readUntilMatch(startTag,startTag1, false)) {
				try {
					int fisrtMatch = readUntilMatch();
					if(fisrtMatch==0){
						if(readUntilMatch(LEFT_SQUARE_BRACKET,RIGHT_SQUARE_BRACKET,1,0)){
							String newStr="["+new String(buffer.getData(),"utf-8").trim()+"]";
							return  newStr;
						}
					}else if(fisrtMatch==1){
						if(readUntilMatch(LEFT_BRACE,RIGHT_BRACE,1,0)){
							String newStr="{"+new String(buffer.getData(),"utf-8").trim()+"}";
							return  newStr;
						}
					}else if(fisrtMatch==2){
						if(readUntilMatch(DQ, true)){
							String newStr="\""+new String(buffer.getData(),"utf-8").trim();
							return  newStr;
						}
					}
				} finally {
					buffer.reset();
				}
			}
			return null;
		}	
		/**
		 * Read the next key, value pair.
		 * 
		 * @return true if a key/value pair was read
		 * @throws IOException
		 * @throws InterruptedException
		 */
		@Override
		public List<String[]> readNext() throws  Exception   {
			String record = null;
			if(jsonDataStructureType.equals(AnalysisJSONFileStructureModel.STRUCTURE_TYPE_LINE)){
				record = readLineRecord();
			}else if(jsonDataStructureType.equals(AnalysisJSONFileStructureModel.STRUCTURE_TYPE_PURE_DATA_ARRAY)){
				if(isContainerSkiped ==false){
					//skipContainerString();
					if(readUntilMatch(startTag, startTag1, false)==false){
						return null;
					}
					//now the cursor point to the first '['  of data:[[
					readUntilMatch();
					//not start for the second '[' of data:[[
					isContainerSkiped =true ;
				}
				record = readArrayArrayRecord(LEFT_SQUARE_BRACKET,RIGHT_SQUARE_BRACKET);
			}else if(jsonDataStructureType.equals(AnalysisJSONFileStructureModel.STRUCTURE_TYPE_OBJECT_ARRAY)){
				if(isContainerSkiped ==false){
					//skipContainerString();
					if(readUntilMatch(startTag, startTag1, false)==false){
						return null;
					}
					//now the cursor point to the first '['  of data:[{
					readUntilMatch();
					//not start for the '{' of data:[{
					isContainerSkiped =true ;
				}
				record = readArrayArrayRecord(LEFT_BRACE,RIGHT_BRACE);
			}else{
				record = readStandardRecord();
			}
			if(StringUtil.isEmpty(record)){
				return null;
			}
			return parser.parse(record);
		}

		//handle      "data:[["a1value","a2value"...],[]]"
		//first, cursor is from  indexof ("data
		private String readArrayArrayRecord(byte[] leftArray,
				byte[] rightArray) throws Exception {
				DataOutputBuffer buffer = new DataOutputBuffer();
				try {	
					if(readUntilMatch(leftArray,rightArray,0,0,buffer)){
						String newStr=new String(buffer.getData(),"utf-8").trim();
						return newStr;
					}
				} finally {
					buffer.close();
				}
			return null;		
		}
		
		private int readUntilMatch() throws IOException{
			while (true) {
				int b = fsin.read();
				// end of file:
				if (b == -1)
					return -1;
				if( b == 32){//space
					continue;
				}
				// check if we're matching:
				if (b == LEFT_SQUARE_BRACKET[0]) {
					return 0;	
				} else if(b == LEFT_BRACE[0]){
					return 1;	
				} else if(b == DQ[0]){
					 return 2;
				}
			}
		}

		
		private boolean readUntilMatch(byte[] leftArray,byte rightArray[],int leftCount,int rightCount) throws IOException{
			while (true) {
				int b = fsin.read();
				// end of file:
				if (b == -1)
					return false;
				// check if we're matching:
				if (b == rightArray[0]) {
					rightCount++;
					if(leftCount==rightCount){
						return true;
					}		
				} else if(b == leftArray[0]){
					leftCount++;	
				} 
				// save to buffer:
				buffer.write(b);
			}
		}

		private boolean readUntilMatch(byte[] leftArray,byte rightArray[],int leftCount,int rightCount, DataOutputBuffer buffer) throws IOException{
			boolean beginToSave = false;
			while (true) {
				int b = fsin.read();
				// end of file:
				if (b == -1)
					return false;
				// check if we're matching:
				if (b == rightArray[0]) {
					rightCount++;
					if(leftCount==rightCount){
						buffer.write(b);
						return true;
					}		
				} else if(b == leftArray[0]){
					leftCount++;	
					beginToSave =true;
				} 
				if(beginToSave){
					// save to buffer:
					buffer.write(b);
				}

			}
		}
		
		private boolean readUntilMatch(byte[] match, boolean withinBlock)
				throws IOException {
			int i = 0;
			while (true) {
				int b = fsin.read();
				// end of file:
				if (b == -1)
					return false;
				// save to buffer:
				if (withinBlock)
					buffer.write(b);

				// check if we're matching:
				if (b == match[i]) {
					i++;
					if(i>=match.length){
						return true;
					}	
				} else{
					i = 0;
				}
				// see if we've passed the stop point:
				if (!withinBlock && i==0)
					return false;
			}
	}
		//match1 = "field":   match2 = "field" :
		private boolean readUntilMatch(byte[] match1,byte[]match2, boolean withinBlock)
				throws IOException {
			int i = 0;
			while (true) {
				int b = fsin.read();
				// end of file:
				if (b == -1)
					return false;
				// save to buffer:
				if (withinBlock)
					buffer.write(b);

				// check if we're matching:
				if (b == match1[i]||b==match2[i]) {
					if(b<match1.length && b != match1[i]){
						isInmatch2=true;
					}
					i++;
					//match 2 match2
					if(i>=match2.length){
						isInmatch2=false;
						return true;
					}else{//match to match 1
						if(isInmatch2==false&&i>=match1.length){
							isInmatch2=false;
							return true;
						}					
					}	
				} else{
					i = 0;
				}
			}
		}
	}

	public static interface  LocalFileRecordReader   {
		public List<String[]> readNext() throws  Exception   ;
	}
	public static class XmlRecordReader  implements LocalFileRecordReader{
		private byte[] startTag;
//		private byte[] startTag_withBlank;
		private byte[] endTag;
		XMLRecordParser parser =  null ;
	    private   DataOutputBuffer buffer = new DataOutputBuffer();

		private BufferedReader fsin;
		private String endTagString;
		private String startTagString;
		private String containerTagName;
		private String sturctureType;
		private String containerPath;

		/**
		 * @param containerTagName
		 * @param xPathList
		 * @throws UnsupportedEncodingException
		 */
		public XmlRecordReader(String containerTagName ,List<String> xPathList,
				BufferedReader fsin ,String attributeMode,String sturctureType,String containerPath)   
				throws UnsupportedEncodingException {
			if(attributeMode.equals(AnalysisXMLFileStructureModel.NO_ATTRIBUTE)) {
				startTag = new String("<" + containerTagName + ">").getBytes("utf-8");
				endTag = new String("</" + containerTagName + ">").getBytes("utf-8");
			}
			else {
				startTag = new String("<" + containerTagName + " ").getBytes("utf-8");
				if(attributeMode.equals(AnalysisXMLFileStructureModel.HALF_ATTRIBUTE )){
					endTag = new String("</" + containerTagName + ">").getBytes("utf-8");

				}else{ // pure attribute mode
					endTag = new String("/>").getBytes("utf-8");

				}

			}
			//some  tag use attribute ... <row id =
//			startTag_withBlank =  new String("<" + containerTagName + " ").getBytes("utf-8");
			
			this.sturctureType=sturctureType;
			this.containerPath=containerPath;
			this.fsin  = fsin;
			parser =  new XMLRecordParser( xPathList,sturctureType,containerPath);
			endTagString = new String(endTag);
			startTagString = new String(startTag) ;
			this.containerTagName = containerTagName;
			
		}

 

		/**
		 * Read the next key, value pair.
		 * 
		 * @return true if a key/value pair was read
		 * @throws IOException
		 * @throws InterruptedException
		 */
		@Override
		public List<String[]> readNext() throws  Exception   {
			 
			if (readUntilMatch(startTag, false)) {
				try {
					buffer.write(startTag);
					if (readUntilMatch(endTag, true)) {
						 
				 
						return parse(new String (buffer.getData(),"UTF-8"));
					}
				} finally {
					buffer.reset();
				}
			}
		return null;
	}

 
 
		/**
		 * @param content
		 * @return
		 * @throws Exception 
		 */
		private List<String[]> parse(String content) throws Exception { 
			try {
				//this is very important!!! to avoid the parse error
				//like <album>CCP EVE Online</album>  </track>k>/track>
				if(content.indexOf(endTagString )>0){
						content=content.substring(0,content.indexOf(endTagString))+endTagString;
				} 
				return parser.parse(content);
			} catch (Exception e) {
			//	System.err.println(content);
				//System.err.println("last char ='"+content.charAt(content.length()-1)+"'	");
			if(content.lastIndexOf(startTagString)>0
					||			content.lastIndexOf(startTagString.substring(0,startTagString.length()-1)+" ")>0){

				throw new Exception("Can not have the same tag name for child element:" +containerTagName);
			}
			logger.error(e.getMessage(),e);
			}
			return new ArrayList<String[]>()  ;//an error record, need ignore
		}



		
		private boolean readUntilMatch(byte[] match, boolean withinBlock)
				throws IOException {
			int i = 0;
			while (true) {
				int b = fsin.read();
				// end of file:
				if (b == -1){
					return false;
				}
				// save to buffer:
				if (withinBlock == true){
					buffer.write(b);
				}

				// check if we're matching:
				if (b == match[i]) {
					i++;
					if (i >= match.length){
						return true;
				}
				} else{
					i = 0;
					}
			}
		}
	}
}
