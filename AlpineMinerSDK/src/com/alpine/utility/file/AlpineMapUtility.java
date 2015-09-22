package com.alpine.utility.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.pig.backend.executionengine.ExecException;

import static com.alpine.hadoop.pig.AlpinePigConstants.*;

public class AlpineMapUtility {
 	public static Map<Integer, Map<Integer, Integer>> desiralizeTheMap(
			String strMap)  {
		
		Map<Integer, Map<Integer, Integer>> map = new TreeMap<Integer, Map<Integer, Integer>>();
		if(null==strMap){
			return map;
		}
		
		String[] maps = strMap.split(MAP_SPLITTER);
		if (null == maps) {
			return map;
		}

		for (String m : maps) {
			String[] cn = m.split(COLUMN_SPLITTER);
			if (2 != cn.length) {
				throw new RuntimeException("Got Wrong number of columns for [" + m
						+ "]");
			}
			String[] binCounts = cn[1].split(BIN_SPLITTER);
			Integer columnNumber = Integer.parseInt(cn[0]);
			if (null == binCounts || 0 != binCounts.length % 2) {
				throw new RuntimeException("Got Wrong number of bin counts for ["
						+ cn[1] + "]");
			}
			Map<Integer, Integer> binMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < binCounts.length / 2; i++) {
				binMap.put(Integer.parseInt(binCounts[2 * i]),
						Integer.parseInt(binCounts[2 * i + 1]));
			}
			mergeTheMaps(columnNumber, map, binMap);
		}

		return map;
	}

	private static void mergeTheMaps(Integer columnNumber,
			Map<Integer, Map<Integer, Integer>> map,
			Map<Integer, Integer> binMap) {
		Map<Integer, Integer> cm = map.get(columnNumber);
		if (null == cm) {
			map.put(columnNumber, binMap);
			return;
		}
		Set<Integer> binIDs = binMap.keySet();
		for (Integer bID : binIDs) {
			Integer count = binMap.get(bID);
			Integer pc = cm.get(bID);
			count = (null == pc ? count : pc + count);
			cm.put(bID, count);
		}

	}
	
	
	
	public static Map<Integer,Map<String,Integer>> desiralizeFrequencyMap(String strMap) throws ExecException{
		strMap=strMap.substring(ALPINE_PIGGY_FREQUENCY_MAP_PREFIX.length(),strMap.length());
		Map<Integer,Map<String,Integer>> map =new TreeMap<Integer, Map<String,Integer>>();
		String[] maps=strMap.split(MAP_SPLITTER);
		if(null==maps){
			return map;
		}
		
		
		for(String m:maps){
			String[] cn=m.split(COLUMN_SPLITTER);
			if(2!=cn.length){
				throw new ExecException("Got Wrong number of columns for ["+m+"]");
			}
			String[] binCounts = cn[1].split(BIN_SPLITTER);
			Integer columnNumber =Integer.parseInt(cn[0]);
			if(null==binCounts||0!=binCounts.length%2){
				throw new ExecException("Got Wrong number of bin counts for ["+cn[1]+"]");
			}
			Map<String,Integer> binMap=new HashMap<String,Integer>();
			for(int i=0;i<binCounts.length/2;i++){
				binMap.put(binCounts[2*i]+"", Integer.parseInt(binCounts[2*i+1]));
			}
			mergeFrequencyMaps(columnNumber,map,binMap);
		}
		
		
		
		return map;
	}
	
	
	private static void mergeFrequencyMaps(Integer columnNumber,
			Map<Integer, Map<String, Integer>> map,
			Map<String, Integer> binMap) {
		Map<String, Integer> cm = map.get(columnNumber);
		if(null==cm){
			map.put(columnNumber, binMap);
			return;
		}
		Set<String> binIDs = binMap.keySet();
		for(String bID:binIDs){
			Integer count = binMap.get(bID);
			Integer pc=cm.get(bID);
			count=(null==pc?count:pc+count);
			cm.put(bID, count);
		}
		
	}

	private static String serilazeFrequencyMap(Map<Integer,Map<String,Integer>> map) {
		StringBuilder sb = new StringBuilder();
		Set<Integer> ck=map.keySet();
		for(Integer co:ck){
			sb.append(co).append(COLUMN_SPLITTER);
			Map<String, Integer> binMap = map.get(co);
			Set<String> binKeys = binMap.keySet();
			boolean addCommaFront=false;
			for(String bk:binKeys){
				Integer binContentCount=binMap.get(bk);
				if(!addCommaFront){
					addCommaFront=true;
				}else{
					sb.append(BIN_SPLITTER);
				}
				sb.append(bk).append(BIN_SPLITTER).append(binContentCount);
			}
			sb.append(MAP_SPLITTER);
		}
		String ser=ALPINE_PIGGY_FREQUENCY_MAP_PREFIX+sb.toString();
		
		return ser.substring(0,ser.length()-1);
	}
}
