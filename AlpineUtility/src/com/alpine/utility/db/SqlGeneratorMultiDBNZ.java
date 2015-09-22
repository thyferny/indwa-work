package com.alpine.utility.db;

public class SqlGeneratorMultiDBNZ extends AbstractSqlGeneratorMultiDBGPPG {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1905089157675338231L;

	public String castToDouble(String columnName){
		return "(("+columnName+")::double)";
	}

	@Override
	public String insertTable(String sql, String tableName) {
		return "";
	}

	@Override
	public String setCreateTableEndingSql(String distributeColumns) {
		return " ";
	}

	@Override
	public String cascade() {
		return " ";
	}

	@Override
	public String castToText(String columnName) {
		return " cast("+columnName+" as varchar(100)) ";
	}

	@Override
	public String setSeed(String seed, Integer para, Integer index) {
		String sql;
		if(para==null||index==null){
			sql = "call setseed("+seed+");";
		}else{
			sql = "call setseed("+(Double.parseDouble(seed)/(para+index))+");";
		}	
		return sql;
	}
	
	@Override
	public String dropTableIfExists(String tableName) {
		String[] temp=tableName.split("\\.",2);
		StringBuilder sb=new StringBuilder();
		sb.append("call droptable_if_existsdoubleq('").append(temp[1]).append("')");
		return sb.toString();
	}
	@Override
	public String dropViewIfExists(String tableName) {
		String[] temp=tableName.split("\\.",2);
		StringBuilder sb=new StringBuilder();
		sb.append("call dropview_if_existsdoubleq('").append(temp[1]).append("')");
		return sb.toString();
	}
	
	@Override
	public String rownumberOverByNull() {
		return " row_number() over (order by random()) ";
	}
	@Override
	public String getStorageString(boolean isAppendOnly, boolean isColumnarStorage, boolean isCompression, int compressionLevel){
		return "";
	}
	
	@Override
	public String getCastDataType(String sql,String dataType) {
		if(dataType.equals(GPSqlType.VARCHAR)){
			return castToText(sql);
		}else {
			return sql;
		}
	}
}
