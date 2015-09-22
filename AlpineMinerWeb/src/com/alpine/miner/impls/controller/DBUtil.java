/**
 * 
 */
package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.db.DBMetaDataUtil;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author sam_zang
 *
 */
public class DBUtil {

    private static Logger itsLogger = Logger.getLogger(DBUtil.class);
    public static final String TYPE_DATE = "date";
	public static final String TYPE_NUMBER = "number";
	public static final String TYPE_CATE = "cate"; 
	// Add by Will for PLDA
	public static final String TYPE_INT = "int";
	public static final String TYPE_ARRAY = "array";
	
	static ResourceManager rmgr = ResourceManager.getInstance();
	
	 //MINER_WEB735  always get the latest column
	//type only is category and number and date 
	public static String[][] getColumnListWithType (String user, String conn, String schema,
			String table,String columnType, ResourceType dbType) throws Exception {
		//could be null if not the type...
		List<String[]>  result = new ArrayList<String[]>();
		DbConnectionInfo info = rmgr.getDBConnection(user, conn,dbType);
		if (info == null) {
			return null;
		}
		
		DBMetaDataUtil dmd = new DBMetaDataUtil(info.getConnection());
		dmd.setJudgeConnection(false);
		dmd.setLocale(Locale.getDefault());
 		try {
			List<TableColumnMetaInfo> columnInfoList = createColumnList(schema, table, dmd);

			String dbSystem=info.getConnection().getDbType();
 
		 
			String type="";
			if(columnInfoList!=null){
				for(TableColumnMetaInfo column : columnInfoList){
			
				if(ParameterUtility.isArrayArrayType(column.getColumnsType(), dbSystem) || ParameterUtility.isArrayType(column.getColumnsType(), dbSystem)){
					type=TYPE_ARRAY;
				}
				else if(ParameterUtility.isNumberColumnType(column.getColumnsType(), dbSystem)){
					type=TYPE_NUMBER;
				}else if(ParameterUtility.isCategoryColumnType(column.getColumnsType(), dbSystem)){
					type=TYPE_CATE;					
				}else if(ParameterUtility.isIntegerColumnType(column.getColumnsType(), dbSystem)){
					type=TYPE_INT;
				}else{
					type=TYPE_DATE;
				}
				//will add date later
				if(columnType==null||columnType.equals("all")||containType(columnType,type)){
					result.add(new String[]{ column.getColumnName(), type});
				 
				}
			  }
			}

		} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				throw e;
		} finally {
			dmd.disconnect();
		}
		return result.toArray(new String[result.size()][2]);
	}
	
	
	private static List<TableColumnMetaInfo> createColumnList(String schemaName,
			String tableName, DBMetaDataUtil dmd) throws Exception {
		List<TableColumnMetaInfo> colInfos = new ArrayList<TableColumnMetaInfo>();
		ArrayList<String[]> columns = dmd.getAllColumnList(schemaName,
				tableName);
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			String[] cols = (String[]) iterator.next();
			TableColumnMetaInfo colInfo = new TableColumnMetaInfo(cols[0],
					cols[1]);
			colInfos.add(colInfo);

		}

		return colInfos;
	}
	
	/**
	 * @param columnType
	 * @param type
	 * @return
	 */
	//cate_date
	private static boolean containType(String columnType, String type) {
		if(columnType.indexOf("_") >0){
			StringTokenizer st = new StringTokenizer(columnType,"_") ; 
			 while(st.hasMoreTokens()){
				 if(st.nextToken().equals(type) ==true){
					 return true;
				 }
			 }
			
		}else {
			return columnType.trim().equals(type);			
		}
		return false;

	}
 
	//use tsimple type for web client : cate, number ,date
	public static void reSetColumnType(String dbType, DataTable datatable) {
		List<TableColumnMetaInfo> columns = datatable.getColumns();
		if(columns!=null&&columns.size()!=0){
			for(int i=0;i<columns.size();i++){
				TableColumnMetaInfo column = columns.get(i);
				if(column!=null ){
					if(StringUtil.isEmpty(column.getColumnsType()) ==false){ 
						String type = column.getColumnsType();
						if(ParameterUtility.isNumberColumnType(type, dbType)
								||type.equals(DBUtil.TYPE_NUMBER)) { 
							column.setColumnsType(DBUtil.TYPE_NUMBER) ;
						}else if(ParameterUtility.isCategoryColumnType(type, dbType)) {
							column.setColumnsType(DBUtil.TYPE_CATE) ;
						}else{
							column.setColumnsType(DBUtil.TYPE_DATE) ;
						}
					}else{
						column.setColumnsType(DBUtil.TYPE_CATE) ;
					}
					
				}
			}
		}
	 		 
		
	}
}
