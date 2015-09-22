package com.alpine.utility.tools;

import com.alpine.utility.file.StringUtil;

public class StringHandler {

	private static final Object[] EMPTY_ARGS = new Object[0];

	public static String doubleQ(String inputString) {
		if (inputString == null) {
			inputString = "";
		}
		// inputString=inputString.trim();
		if (!inputString.startsWith("\"") || !inputString.endsWith("\"")) {
			inputString = inputString.replace("\"", "\"\"");
			inputString = "\"" + inputString + "\"";
		}
		return inputString;
	}

	public static String removeMiddleDoubleQ(String inputString) {
		inputString = inputString.replace("\"", "");
		return inputString;
	}

	public static String escQ(String inputString) {
		if (inputString.contains("'")) {
			inputString = inputString.replace("'", "''");
		}
		return inputString;
	}

	public static String removeDoubleQ(String inputString) {
		if (inputString.startsWith("\"") && inputString.endsWith("\"")) {
			inputString = inputString.substring(1, inputString.length() - 1);
		}
		return inputString;

	}

	public static String bind(String message, String[] paras) {
		for (int i = 0; i < paras.length; i++) {
			message = message.replace("\"{" + i + "}\"", paras[i]);//
		}
		return message;
	}

	public static String addPrefix(String str, String prefix) {
		str = prefix + "_" + str;
		return str;
	}

	public static String combinTableName(String schema, String table) {
		if (!StringUtil.isEmpty(schema) && !StringUtil.isEmpty(table)) {
			return doubleQ(schema) + "." + doubleQ(table);
		} else {
			return null;
		}
	}

	public static String[] splitQuatedTableName(String tableName) {
		String[] tableNameArray = new String[2];
		if (tableName.startsWith("\"") && tableName.endsWith("\"")) {
			tableName=tableName.substring(1, tableName.length()-1);
			tableNameArray = tableName.split("\"\\.\"");
		}
		
		return tableNameArray;
	}
	
	public static String singleQ(String inputString) {
		if (inputString == null) {
			inputString = "";
		}
		if (!inputString.startsWith("\'") || !inputString.endsWith("\'")) {
			inputString = inputString.replace("\'", "\'\'");
			inputString = "\'" + inputString + "\'";
		}
		return inputString;
	}
}
