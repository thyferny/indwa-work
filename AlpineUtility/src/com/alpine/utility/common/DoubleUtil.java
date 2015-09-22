/**
 * ClassName AlpineUtil.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.common;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DoubleUtil {
 
	
	public static List<Double> stringToDoubleList(String source,String seperator){
		List<Double> list = new ArrayList<Double>();
		if(source!=null&&seperator!=null){
			StringTokenizer st =new StringTokenizer(source,seperator);
			while(st.hasMoreTokens()){
				list.add(Double.valueOf(st.nextToken())) ;
			}
		}
		return list;
	}
	
	public static String doubleListToString(List<Double> list,String seperator){
		
		StringBuffer sb = new StringBuffer();
		if(list!=null&&seperator!=null){
			 for (int i = 0; i < list.size(); i++) {
				if(i>0){
					sb.append(seperator) ;
				}
				sb.append(list.get(i).toString()) ;
			}
		}
		return sb.toString();
	}
}
