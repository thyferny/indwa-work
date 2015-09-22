package com.alpine.hadoop.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DataPretreatUtility {

    public static final String INTEGER_OLD = "Int";
    public static final String FLOAT_OLD = "Float";
    public static final String NUMERIC_OLD  = "Numeric";

    private static List<String> numericTypes = Arrays.asList(new String[]{
            "FLOAT",
            "NUMERIC",
            "INT",
            "LONG",
            "DOUBLE"
    });

    public static boolean isNumberType(String type) {
        if(type==null){
            return false;
        }
        type=type.toUpperCase(Locale.ENGLISH);

        if(numericTypes.contains(type)){
            return true;
        }

        if(type.startsWith("DECIMAL(")&&type.endsWith(")")){
            return true;
        }
        return false;
    }

	public static boolean checkType(String value, String type) {
		if ("long".equals(type)) {
			try {
				long l = Long.parseLong(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		else if("double".equals(type)){
			if(value==null||value.equals("NaN")){
				return false;
			}
			try {
				double d = Double.parseDouble(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		else if("float".equals(type)){
			if(value==null||value.equals("NaN")){
				return false;
			}
			try {
				float d = Float.parseFloat(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		else if("int".equals(type)){
			try {
				int d = Integer.parseInt(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		else if("chararray".equals(type)||"bytearray".endsWith(type)){
			return true;
		}
		else{
			return true;
		}
	}
}
