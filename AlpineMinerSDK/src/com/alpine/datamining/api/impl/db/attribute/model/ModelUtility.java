/**
 * ClassName :ParameterUtility.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model;

import java.util.List;


/**
 * @author zhaoyong
 * 
 */
public class ModelUtility {

	public static boolean nullableEquales(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		} else if (obj1 != null) {
			return obj1.equals(obj2);
		} else {
			return false;
		}
	}

	public static boolean equalsWithOrder(List<?> source,
			List<?> target) {
			if(source==target){
				return true;
			}	 
			else if (source==null&&target!=null){
				return false;
			}
			else if (source!=null&&target==null){
				return false;
			}
			else if (source.size()!=target.size()){
				return false;
			}
			else{
				for(int i=0;i<source.size();i++){
					if(source.get(i)!=null&&source.get(i).equals(target.get(i))==false){
						return false;
					}
				}
				 
				return true;
			} 
 
		
	}

}
