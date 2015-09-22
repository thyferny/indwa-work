/**
 * Classname ListUtility.java
 *
 * Version information:1.00
 *
 * Data:2010-8-11
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Administrator
 *
 */
public class ListUtility {
	public static boolean equalsFocusOrder(List source,List target){
		if(source==target){
			return true;
		}
		else if(source==null&&target==null){
			return true;
		}
		else if (source==null&&target!=null){
			return false;
		}
		else if (source!=null&&target==null){
			return false;
		}else if(source.size()!=target.size()){
			return false;
		}else{
			for(int i=0;i<source.size();i++){
				if(source.get(i)==null
						||target.get(i)==null
						||source.get(i).equals(target.get(i))==false){
					return false;
				}
			}
			return true;
		}
	}
	public static boolean equalsIgnoreOrder(List source,List target){
		if(source==target){
			return true;
		}
		else if(source==null&&target==null){
			return true;
		}
		else if (source==null&&target!=null){
			return false;
		}
		else if (source!=null&&target==null){
			return false;
		}else{

			for (Iterator iterator = source.iterator(); iterator.hasNext();) {
				Object obj =   iterator.next();
				if(hasSameObject(target ,obj)==false){
					return false;
				}
				
			}
			
			for (Iterator iterator = target.iterator(); iterator.hasNext();) {
				Object obj =   iterator.next();
				if(hasSameObject(source ,obj)==false){
					return false;
				}
				
			}
			return true;
		} 
		
		
		}
	
	public static boolean hasSameObject(	List  list,	Object target) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object =   iterator	.next();
			if(object!=null&&object.equals(target)==true){
				return true;
			}
		}
		return false;
	}

 
	/**
	 * @param values
	 * @return
	 */
	public static List<String> cloneStringList(List<String> values) { 
		if(values==null){
			return null;
		}else {
			ArrayList<String> clone = new ArrayList<String>();
			for (Iterator iterator = values.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				clone.add(string);
			}
			return clone;
		}
	
	}

	/**
	 * @param values
	 * @return
	 */
	public static List<Double> cloneDoubleList(List<Double> values) {
		if(values==null){
			return null;
		}else {
			ArrayList<Double> clone = new ArrayList<Double>();
			for (Iterator iterator = values.iterator(); iterator.hasNext();) {
				Double value = (Double) iterator.next();
				clone.add(value);
			}
			return clone;
		}
	}

	/**
	 * @param valueList
	 * @param value
	 * @return
	 */
	public static boolean containsDuplicate(List<String> valueList, String value) {
		if(valueList!=null&&value!=null){		
			int n=0;
			for (Iterator iterator = valueList.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				if(value.equals(string)){
					n=n+1;
				}
				if(n>=2){
					return true;
				}
			}
		}

		return false;
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
	
	public static List<String> stringToDoubleList(String source,String seperator){
		List<String> list = new ArrayList<String>();
		if(source!=null&&seperator!=null){
			StringTokenizer st =new StringTokenizer(source,seperator);
			while(st.hasMoreTokens()){
				list.add(st.nextToken()) ;
			}
		}
		return list;
	}
	
	public static int getRealIndex( List list,Object object){
		if(list!=null){
			for(int i=0;i<list.size();i++){
				if(list.get(i) == object){
					return i;
					
				}
			}
		}
		return -1;
		
	}
	public static boolean isEmpty(List<?> list) {
		if(list==null||list.size()==0){		
			return true;
		}else{
			return false;
		}
	}
}
