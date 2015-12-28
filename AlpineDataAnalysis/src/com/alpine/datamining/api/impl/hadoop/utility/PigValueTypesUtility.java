package com.alpine.datamining.api.impl.hadoop.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PigValueTypesUtility{
	
	
	public static Map<Integer,List<String>> sortFrequencyMapByColumnTypeString(Map<Integer, Map<String, Integer>> fm,String[] columnTypes){
		if(null==fm||fm.isEmpty()||null==columnTypes||0==columnTypes.length){
			throw new IllegalArgumentException("Neither Column types nor map can be neither null nor empty");
		}
		PigColumnType[] columnTypesInArray=getPigColumnTypes(columnTypes);
		return sortFrequencyMap(fm,columnTypesInArray);
		
	}
	
	
	public static Map<Integer,List<String>> sortFrequencyMap(Map<Integer, Map<String, Integer>> fm,List<PigColumnType> columnTypes){
		if(null==fm||fm.isEmpty()||null==columnTypes||0==columnTypes.size()){
			throw new IllegalArgumentException("Neither Column types nor map can be neither null nor empty");
		}
		PigColumnType[] columnTypesInArray=columnTypes.toArray(new PigColumnType[]{});
		return sortFrequencyMap(fm,columnTypesInArray);
		
	}
	//We might be having less columns than none filtered columns, so find the columns that we are interested and process them only
	public static Map<Integer,List<String>> sortFrequencyMap(Map<Integer, Map<String, Integer>> fm,PigColumnType[] columnTypes){
		if(null==fm||fm.isEmpty()||null==columnTypes||0==columnTypes.length){
			throw new IllegalArgumentException("Neither Column types nor map can be neither null nor empty");
		}
		Map<Integer,List<String>> map=new HashMap<Integer,List<String>>();
		Comparator[] comprators = getComparatorsForTheColumns(columnTypes);
		for(int i=0;i<columnTypes.length;i++){
			Map<String,Integer> values=fm.get(i);
			List<String> list=new ArrayList<String>();
			if(null==values){
				map.put(i, new ArrayList<String>());
				continue;
			}
			
			for(String kv:values.keySet()){
				list.add(kv);
			}
			Collections.sort(list,comprators[i]);
			map.put(i, list);
			
			
			
		}
		
		return map;
	}
	
	public static Map<Integer,List<String>> sortFrequencyMap(Map<Integer, Map<String, Integer>> fm,String[] columnTypes){
		if(null==columnTypes||0==columnTypes.length){
			throw new IllegalArgumentException("Column types neither can be null nor empty");
		}
		return sortFrequencyMap(fm,getPigColumnTypes(columnTypes));

	}
	public static Comparator[] getComparatorsForTheColumns(String[] columnTypes){
		if(null==columnTypes||0==columnTypes.length){
			throw new IllegalArgumentException("Column types neither can be null nor empty");
		}
		return getComparatorsForTheColumns(getPigColumnTypes(columnTypes));
	}
	
	public static Comparator[] getComparatorsForTheColumns(PigColumnType[] columnTypes){
		if(null==columnTypes||0==columnTypes.length){
			throw new IllegalArgumentException("Column types neither can be null nor empty");
		}
		Comparator[] comps=new Comparator[columnTypes.length];
		
		for(int i=0;i<columnTypes.length;i++){
			
			PigColumnType ct=columnTypes[i];
			
			switch (ct) {
	        case Long:   
	        case Float:    
	        case Double:
	        case Integer:
	        	 
	        	comps[i]=new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						try{//string may not be a double !!! will cause error in flow
							Double l1= 0d;
							try{
								  l1=Double.valueOf(o1);
							}catch(Exception e){
								return 1;  //o1 is a real string 
							}
							Double l2=null==o2?null:Double.valueOf(o2);
							return l1.compareTo(l2);
						}catch(Exception e){ 
							return -1;   //o2 is a real string 
						}
					}
				};
	            break;              
 	        case Chararray: 
	        	comps[i]=new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o1.compareTo(o2);
					}
				};
	            break;    
	            
	        default:
	        	comps[i]=new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o1.compareTo(o2);
					}
				};
	            break;    

		}
		
	}
	return comps;
	}
	
	public static PigColumnType[] getPigColumnTypes(String[] strTypes){
		if(null==strTypes||0==strTypes.length){
			throw new IllegalArgumentException("Column types neither can be null nor empty");
		}
		PigColumnType[] ct=new PigColumnType[strTypes.length];
		for(int i=0;i<strTypes.length;i++){
			if(null==strTypes[i]||0==strTypes[i].trim().length()){
				throw new IllegalArgumentException("Column type neither can be null nor empty");
			}
			
			String sct=strTypes[i].trim();
			
			if(sct.equalsIgnoreCase(PigColumnType.Integer.toString())||
					sct.equalsIgnoreCase("int")){
				ct[i]=PigColumnType.Integer;
				continue;
			}
			
			if(sct.equalsIgnoreCase(PigColumnType.Long.toString())||
					sct.equalsIgnoreCase("long")){
				ct[i]=PigColumnType.Long;
				continue;
			}
			
			if(sct.equalsIgnoreCase(PigColumnType.Double.toString())||
					sct.equalsIgnoreCase("double")){
				ct[i]=PigColumnType.Double;
				continue;
			}
			
			if(sct.equalsIgnoreCase(PigColumnType.Chararray.toString())||
					sct.equalsIgnoreCase("chararray")||
					sct.equalsIgnoreCase("String")||
					sct.equalsIgnoreCase("str")){
				ct[i]=PigColumnType.Chararray;
				continue;
			}
			
			if(sct.equalsIgnoreCase(PigColumnType.Float.toString())||
					sct.equalsIgnoreCase("float")){
				ct[i]=PigColumnType.Float;
				continue;
			}
			
			throw new IllegalArgumentException("Type of ["+sct+"] is not recognized as a pig variable");
		}
		
		
		return ct;
	}
	
	public static String[] acquireColumnTypes(List<String> fullColumnNameList,List<String> fullColumnTypes, String[] names) {
		if(null==fullColumnNameList||0==fullColumnNameList.size()||
		   null==fullColumnTypes||0==fullColumnTypes.size()||
		   fullColumnTypes.size()!=fullColumnNameList.size()||
		   null==names||0==names.length||names.length>fullColumnNameList.size()){
			
			throw new IllegalArgumentException("Make sure to have fullColumnTypes,fullColumnNameList,names neither null nor empty. " +
					"Make sure fullColumnNameList and fullColumnTypes are having same size and names size is less or equal to fullColumnTypes size. " +
					"We got fullColumnNameList :" + (null==fullColumnNameList?null:fullColumnNameList.toString())+
					"We got fullColumnTypes:" + (null==fullColumnTypes?null:fullColumnTypes.toString())+
					"Names:"+(null==names?null:Arrays.toString(names)));
		}
		
		String[] types=new String[names.length];
		for(int i=0;i<names.length;i++){
			int index=-1;
			for(int j=0;j<fullColumnNameList.size();j++){
				if(names[i].equals(fullColumnNameList.get(j))){
					index=j;
					break;
				}
			}
			if(-1==index){
				throw new IllegalStateException("Column name ["+names[i]+"]does not exist in original column names within["+fullColumnNameList.toString()+"]");
			}
			types[i]=fullColumnTypes.get(i);
		}
		return types;
	}
	
	
}