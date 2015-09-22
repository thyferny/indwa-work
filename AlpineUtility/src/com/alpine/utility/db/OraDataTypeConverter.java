package com.alpine.utility.db;

import java.util.ArrayList;


public class OraDataTypeConverter extends DataTypeConverter {
	
	// there are data type need to use COL_SIZE
	private static final String[] OneArgTypes = {
			"CHAR",
			"VARCHAR2",
			"NCHAR",
			"NVARCHAR2",
			"RAW",
			"UROWID"
	};

	private static final String NumberType = "NUMBER";
	private static final String FloatType = "FLOAT";
	private static final String RealType = "REAL";
	@Override
	public String getClause(String typeName, int jdbcSqlType, int colSize,
			int decimal) {
		
		ArrayList<String> oneArgTypeList = new ArrayList<String>();
		for (String dataType: OneArgTypes) {
			oneArgTypeList.add(dataType);
		}
		if (oneArgTypeList.contains(typeName)) {
//			return String.format("%s(%d)",typeName,colSize);
			return typeName+"("+colSize+")";
		}
		// number types need to use COL_SIZE and DECIMAL fields
		if (typeName.equalsIgnoreCase(NumberType)) {
//			return String.format("%s(%d,%d)", typeName,colSize,decimal);
			return typeName+"("+colSize+","+decimal+")";
		}
		// distinguish between float and real type by the COL_SIZE
		if (typeName.equalsIgnoreCase(FloatType)) {
			if (colSize==126) {
				return FloatType;
			} else {
				return RealType;
			}
		}
		// just return the typeName for all the others
		return typeName;
	}

}
