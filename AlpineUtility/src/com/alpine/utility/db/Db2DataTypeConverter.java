package com.alpine.utility.db;

import java.util.Arrays;
import java.util.List;

public class Db2DataTypeConverter extends DataTypeConverter {

	private static List<String> oneArgTypes = Arrays.asList(new String[] {
			DB2SqlType.CHAR, DB2SqlType.VARCHAR, DB2SqlType.LONGVARCHAR, });

	private static List<String> noArgTypes = Arrays.asList(new String[] {
			DB2SqlType.INTEGER, DB2SqlType.SMALLINT, DB2SqlType.BIGINT,
			DB2SqlType.REAL, DB2SqlType.DOUBLE,DB2SqlType.CLOB, DB2SqlType.GRAPHIC,
			DB2SqlType.VARGRAPHIC, DB2SqlType.LONGVARGRAPHIC,
			DB2SqlType.DBCLOB, DB2SqlType.BLOB, DB2SqlType.DATE,
			DB2SqlType.TIME, DB2SqlType.TIMESTAMP, });

	private static List<String> twoArgTypes = Arrays.asList(new String[] {
			DB2SqlType.DECIMAL, DB2SqlType.DEC,
			DB2SqlType.NUMERIC, DB2SqlType.NUM,
			DB2SqlType.DECFLOAT, });

	@Override
	public String getClause(String typeName, int jdbcSqlType, int colSize,
			int decimal) {

		if (noArgTypes.contains(typeName)) {
			return typeName;
		} else if (oneArgTypes.contains(typeName)) {
			return typeName + "(" + colSize + ")";
		} else if (twoArgTypes.contains(typeName)) {
			return typeName + "(" + colSize + "," + decimal + ")";
		}

		return typeName;
	}

}
