/**
 * ClassName GpDataTypeConverer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class GpDataTypeConverter extends DataTypeConverter {

	private HashMap<String, String> dataType = new HashMap<String, String>();
	private String[] simpleType = {
			GPSqlType.SMALLINT,
			GPSqlType.INTEGER,
			GPSqlType.BIGINT,
			GPSqlType.REAL,
			GPSqlType.DOUBLE_PRECISION,			
			GPSqlType.SERIAL,
			GPSqlType.BIGSERIAL,
			GPSqlType.MONEY,
			GPSqlType.TEXT,
			GPSqlType.BYTEA,
			GPSqlType.DATE,
			GPSqlType.POINT,
			GPSqlType.LINE,
			GPSqlType.LSEG,
			GPSqlType.BOX,
			GPSqlType.PATH,
			GPSqlType.POLYGON,
			GPSqlType.CIRCLE,
			GPSqlType.CIDR,
			GPSqlType.INET,
			GPSqlType.MACADDR,
			GPSqlType.BOOLEAN};
	
	private ArrayList<String> simpleTypeList = new ArrayList<String>();

	public GpDataTypeConverter() {
		super();
		dataType.put(GPSqlType.INT2, GPSqlType.SMALLINT);
		dataType.put(GPSqlType.INT4, GPSqlType.INTEGER);
		dataType.put(GPSqlType.INT8, GPSqlType.BIGINT);
		dataType.put(GPSqlType.FLOAT4, GPSqlType.REAL);
		dataType.put(GPSqlType.FLOAT8, GPSqlType.DOUBLE_PRECISION);
		dataType.put(GPSqlType.BPCHAR, GPSqlType.CHAR);
		dataType.put(GPSqlType.BOOL, GPSqlType.BOOLEAN);
		dataType.put(GPSqlType.VARBIT, GPSqlType.BIT_VARYING);
		dataType.put(GPSqlType.VARCHAR, GPSqlType.CHARACTER_VARYING);
		
		for (String type:simpleType) {
			simpleTypeList.add(type);
		}
		
	}
	
	@Override
	public String getClause(String typeName, int jdbcSqlType, int colSize, int decimal) {
		
		String clause = typeName.toUpperCase(Locale.ENGLISH);

		if (dataType.containsKey(clause)) {
			clause = dataType.get(typeName.toUpperCase(Locale.ENGLISH));
		}

		if (simpleTypeList.contains(clause)) {
			return clause;
		}
		
		if (clause.equals(GPSqlType.NUMERIC)) {
			if (colSize==131089) {
				return clause;
			} 
			else if (decimal==0) {
				return singleParm(clause,colSize);
			} else {
				return doubleParm(clause,colSize,decimal);
			}
		}
		
		if (clause.equals(GPSqlType.CHARACTER_VARYING)) {
			if (colSize==Integer.MAX_VALUE) {
				return clause;
			}
			else {
				return singleParm(clause,colSize);
			}
		}
		
		if (clause.equals(GPSqlType.CHAR)) {
			if (colSize==1) {
				return clause;
			}
			else {
				return singleParm(clause,colSize);
			}
		}
		
		if (clause.equals(GPSqlType.TIMESTAMP)) {
			if (decimal==6) {
				return clause;
			}
			else {
				return singleParm(clause,decimal);
			}
		}
		
		if (clause.equals(GPSqlType.TIMESTAMPTZ)) {
			if (decimal==6) {
				return GPSqlType.TIMESTAMP_WITH_TIME_ZONE;
			}
			else {
//				return String.format(GPSqlType.TIMESTAMP_P_WITH_TIME_ZONE,decimal);
				return GPSqlType.getTIMESTAMP_P_WITH_TIME_ZONE(decimal);
			}
		}

		if (clause.equals(GPSqlType.INTERVAL)) {
			if (decimal==6) {
				return clause;
			}
			else {
				return singleParm(clause,decimal);
			}
		}
		
		if (clause.equals(GPSqlType.TIME)) {
			if (decimal==6) {
				return clause;
			}
			else {
				return singleParm(clause,decimal);
			}
		}
		
		if (clause.equals(GPSqlType.TIMETZ)) {
			if (decimal==6) {
				return GPSqlType.TIME_WITH_TIME_ZONE;
			}
			else {
//				return String.format(GPSqlType.TIME_P_WITH_TIME_ZONE,decimal);
				return GPSqlType.getTIME_P_WITH_TIME_ZONE(decimal);

			}
		}

		if (clause.equals(GPSqlType.BIT)) {
			if (colSize==1) {
				return clause;
			} else {
				return singleParm(clause,colSize);
			}
		}
		
		if (clause.equals(GPSqlType.BIT_VARYING)) {
			if (colSize==Integer.MAX_VALUE) {
				return clause;
			} else {
				return singleParm(clause,colSize);
			}
		}
		if(jdbcSqlType==2003){
			return GPSqlType.ARRAY;
		}
//for others... avoid null point
		return clause;
	}

	
	private String doubleParm(String clause, int parm1, int parm2) {
//		return String.format("%s(%d,%d)",clause,parm1,parm2);
		return clause+"("+parm1+","+parm2+")";
	}
	
	private String singleParm(String clause, int parm) {
//		return String.format("%s(%d)",clause,parm);
		return clause+"("+parm+")";
	}
	
	
	

}

