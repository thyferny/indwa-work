/**
 * ClassName AlpineUtil.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPasswordField;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.log4j.Logger;

public class StringUtil {
    private static final Logger itsLogger = Logger.getLogger(StringUtil.class);
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

	/**
	 * assemble the password from the JPasswordField
	 * @param p: password in JPasswordField
	 * @return password in String
	 */
	static public String getPasswordField(JPasswordField p) {
		String password="";
		for (char charItem : p.getPassword()) {
			password += charItem;
		}
		return password;
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
                itsLogger.error(e);
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
                itsLogger.error(e);
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

	public static String filterEmptyString(String value) {
		if(value==null){
			return "";
			}
		else{
			return value;
		}
	}

	/**
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		if (value==null || value.trim().length()==0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static List<String> stringToList(String source,String seperator){
		List<String> list = new ArrayList<String>();
		if(source!=null&&seperator!=null){
			StringTokenizer st =new StringTokenizer(source,seperator);
			while(st.hasMoreTokens()){
				list.add(st.nextToken()) ;
			}
		}
		return list;
	}
	
	public static String listToString(List<String> list,String seperator){
		
		StringBuffer sb = new StringBuffer();
		if(list!=null&&seperator!=null){
			 for (int i = 0; i < list.size(); i++) {
				if(i>0){
					sb.append(seperator) ;
				}
				sb.append(list.get(i)) ;
			}
		}
		return sb.toString();
	}

	public static boolean safeEquals(String str1, String str2) {
		if(str1==str2){//include 2 null and itself
			return true;
		}else if(str1!=null){
			return str1.equals(str2) ;
		}else{
			return false;
		}
	}
	
	public static String filterInvalidChar4PigName(String str) {
		byte[] bytes = str.getBytes();
		if(bytes.length>0){
			if(true==isNumber(bytes[0])){
				bytes[0] = '_';
			}
			for (int j = 1; j < bytes.length; j++) {
				
				if(isValidNameChar(bytes[j])==false){
					bytes[j]='_' ;
				}
			}
		}
		return new String (bytes);
	}

	private static boolean isNumber(byte byteValue) {
		int intValue = (int)(byteValue);
		return  intValue>=48&&intValue<=57;
	}

	private static boolean isValidNameChar(byte byteValue) {
		return isNumber(byteValue)||isLetter(byteValue);
	}

	private static boolean isLetter(byte byteValue) {
		int intValue = (int)(byteValue);
		return  (intValue>=65 && intValue<= 90)||(intValue>=97 && intValue<= 122);
 
	}
}
