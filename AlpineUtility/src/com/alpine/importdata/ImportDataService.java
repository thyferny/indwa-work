/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ImportDataService.java
 */
package com.alpine.importdata;

import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.importdata.ImportDataConfiguration.ColumnStructure;
import com.alpine.importdata.csvparser.ICSVParser;
import com.alpine.importdata.csvparser.ICSVParser.ParseHandler;
import com.alpine.utility.db.AlpineUtil;

/**
 * @author Gary
 * Aug 15, 2012
 */
public class ImportDataService {

	private static Logger log = Logger.getLogger(ImportDataService.class);

    private final static String[] trueValues = {"TRUE","t","yes","y","1","on"};

	public static void importData(final ImportDataConfiguration config, InputStream dataContent, final ImportHandler callback) throws Exception{
		final Map<Integer, ColumnStructure> columnInfoSet = new HashMap<Integer, ColumnStructure>();// to check which column able to import.
		String sqlTemplate = buildSQL(config, columnInfoSet);
		if(sqlTemplate == null){
			return;
		}

		Connection conn = null;
		try{
			conn = AlpineUtil.createConnection(config.getConnectionInfo());
			conn.setAutoCommit(false);
			final PreparedStatement ps = conn.prepareStatement(sqlTemplate);

			ICSVParser.ParseHandler handler = new ParseHandler(){
				@Override
				public void parseRow(String[] columns, int rowIdx) throws SQLException {
					if(!callback.isContinue()){
						throw new AbortException();
					}
					try {
						fillDataIntoStatement(ps, columns, columnInfoSet);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						callback.onFailRow(rowIdx, columns, e);
					}
					if(++rowIdx % 1000 == 0 ){
						ps.executeBatch();
						callback.onCompleteRow(rowIdx, columns);
					}
				}
			};
			
			if(config.getLimitNum() > 0){
				ICSVParser.INSTANCE.parseCSV(dataContent, 
						config.isIncludeHeader(),
						config.getDelimiter(), 
						config.getQuote(), 
						config.getEscape(), 
						config.getLimitNum(), 
						handler);
			}else{
				ICSVParser.INSTANCE.parseCSV(dataContent, 
						config.isIncludeHeader(),
						config.getDelimiter(), 
						config.getQuote(), 
						config.getEscape(), 
						handler);
			}
			ps.executeBatch();
			conn.commit();
		}catch(Exception e){
            if(e instanceof AbortException) {
                callback.onAbort(e);
            }else if(e instanceof SQLException) {
            	SQLException currentException = (SQLException) e;
                while(currentException != null){
                	currentException.printStackTrace();
                    log.error(currentException.getMessage(), currentException);
                    currentException = currentException.getNextException();
                }
                callback.onFailed(e);
            }else{
            	e.printStackTrace();
                log.error(e.getMessage(), e);
                callback.onFailed(e);
            }
		}finally{
			conn.close();
		}
	}
	
	private static String buildSQL(ImportDataConfiguration config, Map<Integer, ColumnStructure> columnInfoMap){
		StringBuilder sb = new StringBuilder("insert into \"")
								.append(config.getSchemaName())
								.append("\".\"")
								.append(config.getTableName())
								.append("\"");
		
		StringBuilder 	headerBuilder = new StringBuilder("("),
						valueBuilder = new StringBuilder("(");
		List<ColumnStructure> columnInfoList = config.getColumnInfo();
		
		for(int i = 0;i < columnInfoList.size();i++){
			ColumnStructure column = columnInfoList.get(i);
			if(!column.isInclude()){
				continue;
			}
			columnInfoMap.put(i, column);
			headerBuilder.append("\"")
						 .append(column.getColumnName())
						 .append("\",");
			valueBuilder.append("?,");
		}
		headerBuilder.replace(headerBuilder.length() - 1, headerBuilder.length(), ")");
		valueBuilder.replace(valueBuilder.length() - 1, valueBuilder.length(), ")");
		sb.append(headerBuilder)
			.append(" values")
			.append(valueBuilder);
		return headerBuilder.length() < 2 ? null : sb.toString();
	}
	
	private static void fillDataIntoStatement(PreparedStatement ps, String[] data, Map<Integer, ColumnStructure> columnInfoSet) throws Exception{
		int stIdx = 1;
		for(int i = 0;i < data.length;i++){
			String dataItem = data[i];
			ColumnStructure columnInfo = columnInfoSet.get(i);
			if(columnInfo == null){
				continue;
			}
			String columnValue = dataItem;
            if (columnValue != null) {columnValue = columnValue.trim();}
			if((columnValue == null || "".equals(columnValue)) && !columnInfo.isAllowEmpty()){
				ps.clearParameters();
				return;
			}
			switch(columnInfo.getColumnType()){
			case INTEGER:
            case BIGINT:
				try{
                    if(columnValue == null || "".equals(columnValue)) {
                        ps.setNull(stIdx++, Types.INTEGER);
                    } else {
                        ps.setLong(stIdx++, Long.parseLong(columnValue));
                    }
				}catch(Exception e){
                    throw new RuntimeException("Cannot parse " + columnValue + " in column: " + columnInfo.getColumnName() + " to integer");
				}
				break;
			case BOOLEAN:
				try{
                    if(columnValue == null || "".equals(columnValue)) {
                        ps.setNull(stIdx++, Types.BOOLEAN);
                    } else {
                        Boolean javaValue = convertFieldToBoolean(columnValue);
                        ps.setBoolean(stIdx++, javaValue);
                    }
				}catch(Exception e){
                    throw new RuntimeException("Cannot parse " + columnValue + " in column: " + columnInfo.getColumnName() + " to boolean");
				}
				break;
			case NUMERIC:
			case DOUBLE:
				try{
                    if((columnValue == null || "".equals(columnValue))) {
                        ps.setNull(stIdx++, Types.DOUBLE);
                    } else {
                        ps.setDouble(stIdx++, Double.parseDouble(columnValue));
                    }
				}catch(Exception e){
                    throw new RuntimeException("Cannot parse " + columnValue + " in column: " + columnInfo.getColumnName() + " to double");
				}
				break;
			case DATE:
				Date date;
                if(columnValue == null || "".equals(columnValue)) {
                    ps.setNull(stIdx++, Types.DATE);
                } else {
                    try{
                        date = Date.valueOf(columnValue);
                    }catch(Exception e){
                        throw new InvalidTimeException(columnValue);
                    }
                    ps.setDate(stIdx++, date);
                }
				break;
			case DATETIME:
				Timestamp ts;
                if(columnValue == null || "".equals(columnValue)) {
                    ps.setNull(stIdx++, Types.TIMESTAMP);
                } else {
                    try{
                        ts = Timestamp.valueOf(columnValue);
                    }catch(Exception e){
                        throw new InvalidTimeException(columnValue);
                    }
                    ps.setTimestamp(stIdx++, ts);
                }
				break;
			case CHAR:
			case VARCHAR:
                try{
                    if ((columnValue==null) || "".equals(columnValue)) {
                        ps.setNull(stIdx++,Types.VARCHAR);
                    } else {
                        ps.setString(stIdx++, columnValue);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Cannot parse " + columnValue + " in column: " + columnInfo.getColumnName() + " to varchar");
                }
			}
		}
		ps.addBatch();
	}

    private static Boolean convertFieldToBoolean(String stringBool) {
        for (String trueValue : trueValues) {
            if (stringBool.equalsIgnoreCase(trueValue)) {
                return true;
            }
        }
        return false;
    }
	
	public interface ImportHandler{
		
		void onCompleteRow(int rowIdx, String[] row);
		
		void onFailRow(int rowIdx, String[] row, Exception e);
		
		void onFailed(Exception e);

        void onAbort(Exception e);
		
		boolean isContinue();
	}
}
