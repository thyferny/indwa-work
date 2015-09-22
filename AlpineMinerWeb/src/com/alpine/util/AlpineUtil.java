/**   
 * ClassName AlpineUtil.java
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 3.0
 *   
 * Date     2011-3-29    
 * 
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 */

package com.alpine.util ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.log4j.Logger;

public class AlpineUtil {
    private static Logger itsLogger = Logger.getLogger(AlpineUtil.class);

    public static final ThreadLocal VALUE_PASSER = new ThreadLocal();

	public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');
 
        if (f.isDirectory())
        	ext = null;
        else if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isPositiveInteger(String theString) {
		try {
			int num =Integer.parseInt(theString);
			return (num >=0);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isVariableName(String theString) {
		return matchRegex(theString,"[a-zA-Z_]\\w*");
	}
	
	public static boolean isUserName(String theString) {
		return matchRegex(theString,"[a-zA-Z_].*\\w*");
	}

	public static boolean matchRegex(String theString, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(theString);
		return m.matches();
	}

	public static String objectToString(Object obj) {
		BASE64Encoder encode = new BASE64Encoder();
		String out = null;
		if (obj != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				out = encode.encode(baos.toByteArray());
			} catch (IOException e) {
				itsLogger.error(e.getMessage(),e);
				itsLogger.error(AlpineUtil.class.getName()+"\n"+e.toString());
				return null;
			}
		}
		return out;
	}
	
	public static Object stringToObject(String str) {
		BASE64Decoder decode = new BASE64Decoder();

		Object out = null;
		if (str != null) {
			try {		
				ByteArrayInputStream bios = new ByteArrayInputStream(decode.decodeBuffer(str));
				ObjectInputStream ois = new ObjectInputStream(bios);
				out = ois.readObject();
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				itsLogger.error(AlpineUtil.class.getName()+"\n"+e.toString());
				return null;
			}
		}
		return out;
	}

	public static boolean isNumber(String value) {
		try {
			double val = Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public static boolean isFNumber(String value){
		try {
			double val = Double.parseDouble(value);
			if(val<0){
				return true;
			}else{
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public static boolean isFloat(String value){
		try{
			float f = Float.parseFloat(value);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	//avoid the {} and [] in json
	public static String addDoubleQuo(String message){
		if(message!=null){
			message=message.trim();
		}else{
			return "\"null\"";//client can handle this 
		}
		if((message.startsWith("{") && message.endsWith("}"))
				|| (message.startsWith("[") && message.endsWith("]"))){
			return message;
		}else{
			return "\""+message+"\"";
		}
	}

}
