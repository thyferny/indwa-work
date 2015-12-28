
package com.alpine.datamining.db;

import java.util.ArrayList;

import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DataType;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;


public class CommonUtility {

	
	public static enum OracleDataType {Integer, Float, FloatArrayArray, Varchar2};
	
	public static String[] OracleDataTypeArray = {"IntegerArray", "FloatArray", "Floataaa", "Varchar2Array"};
	
	public static String[] OracleDataTypeArrayArray = {"IntegerArrayArray", "FloatArrayArray", "Floataaaa", "Varchar2ArrayArray"};

	
	public static String[] getRegularColumnNames(DataSet dataSet) {
		String[] columnNames = new String[dataSet.getColumns().size()];
		int counter = 0;
		for (Column column : dataSet.getColumns())
			columnNames[counter++] = column.getName();
		return columnNames;
	}


	
	public static String quoteValue(String dbType, Column column, String value){
		String valueQ;
		value = StringHandler.escQ(value);
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType) && column != null && (column.getValueType() == DataType.DATE ||column.getValueType() == DataType.DATE_TIME||column.getValueType() == DataType.TIME)){
			valueQ = "to_date('"+value+"', 'YYYY-MM-DD HH24:MI:SS')";
		}else{
			valueQ = "'"+value+"'";
		}
		return valueQ;
	}
	
	public static String quoteValue(String dbType, Column column, String typeName, String value){
		String valueQ;
		value = StringHandler.escQ(value);
		int type = column.getValueType();
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			if (type == DataType.DATE_TIME){
				if(typeName.equalsIgnoreCase("DATE")){
					if(!StringUtil.isEmpty(value)){
						value = value.split("\\.")[0];
					}
					valueQ = "to_date('"+value+"', 'YYYY-MM-DD HH24:MI:SS')";
				}else{
					valueQ = "to_timestamp('"+value+"', 'YYYY-MM-DD HH24:MI:SS.FF')";
				}
			}else{
				valueQ = "'"+value+"'";
			}
		}else{
			valueQ = "'"+value+"'";
		}
		return valueQ;
	}
	
	public static StringBuffer array2OracleArray(ArrayList<String> array, OracleDataType oracleDataType) {
		int dataType = oracleDataType.ordinal();
		StringBuffer oracleArray = new StringBuffer(OracleDataTypeArrayArray[dataType]+"(");
		int iIndex = array.size()
				/ AlpineDataAnalysisConfig.ORACLE_ARRAY_MAX_COUNT;
		int jIndex = array.size()
				- AlpineDataAnalysisConfig.ORACLE_ARRAY_MAX_COUNT * iIndex;
		boolean first = true;
		for (int i = 0; i < iIndex; i++) {
			if (first) {
				first = false;
			} else {
				oracleArray.append(",");
			}
			oracleArray.append(OracleDataTypeArray[dataType]+"(");
			for (int j = 0; j < AlpineDataAnalysisConfig.ORACLE_ARRAY_MAX_COUNT; j++) {
				if (j != 0) {
					oracleArray.append(",");
				}
				String temp = array.get(i
						* AlpineDataAnalysisConfig.ORACLE_ARRAY_MAX_COUNT + j);
				oracleArray.append(temp);
			}
			oracleArray.append(")");
		}
		if (jIndex > 0) {
			if (first) {
				first = false;
			} else {
				oracleArray.append(",");
			}
			oracleArray.append(OracleDataTypeArray[dataType]+"(");
			for (int j = 0; j < jIndex; j++) {
				if (j != 0) {
					oracleArray.append(",");
				}
				String temp = array.get(iIndex
						* AlpineDataAnalysisConfig.ORACLE_ARRAY_MAX_COUNT + j);
				oracleArray.append(temp);
			}
			oracleArray.append(")");
		}
		oracleArray.append(")");
		return oracleArray;
	}
	
	public static StringBuffer array2OracleArray(double[] array)
	{
		ArrayList<String> arrayList = new ArrayList<String>();
		for(int i = 0; i < array.length; i++){
			double temp = array[i];
			if (Double.isNaN(temp)){
				temp = 0.0;
			}
			arrayList.add(String.valueOf(temp));
		}
		return array2OracleArray(arrayList, OracleDataType.Float);
	}
	
	public static StringBuffer splitOracleSqlToVarcharArray(StringBuffer sql){
		StringBuffer varcharArray = new StringBuffer("varchar2array(");
		int varcharLength = 3500;
		int count = sql.length()/varcharLength;
		for(int i = 0; i < count; i++){
			if(i != 0){
				varcharArray.append(",");
			}
			varcharArray.append("'").append(StringHandler.escQ(sql.substring(i*varcharLength, (i+1)*varcharLength))).append("'");
		}
		if(sql.length() > count * varcharLength){
			if(count != 0){
				varcharArray.append(",");
			}
			varcharArray.append("'").append(StringHandler.escQ(sql.substring(count*varcharLength))).append("'");
		}
		varcharArray.append(")");
		
		return varcharArray;
	}
}

