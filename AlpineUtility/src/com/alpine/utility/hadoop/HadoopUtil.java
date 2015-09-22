/**
 * ClassName HadoopUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-21
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.utility.hadoop;

import java.util.List;

/**
 * @author Jeff Dong
 *
 */
public class HadoopUtil {

	public static String[] transferColumnIndexToColumnName(String[] originColumnNames,List<String> realColumnNames){
		String[] realAttributeNames = new String[originColumnNames.length];
		
		for(int i=0;i<originColumnNames.length;i++){
			realAttributeNames[i]=realColumnNames.get(Integer.parseInt(originColumnNames[i]));
		}
		
		return realAttributeNames;
	}
}
