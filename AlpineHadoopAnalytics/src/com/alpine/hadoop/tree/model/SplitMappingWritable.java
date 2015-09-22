/**
*
* ClassName SplitMapping.java
*
* Version information: 1.00
*
* Sep 4, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.tree.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Jonathan
 *  
 */

public class SplitMappingWritable extends ArrayWritable {
	public SplitMappingWritable() {
		super(MapWritable.class);
	}
	
	public static void populateHash(HashMap<String, int[]>[] hash, List<String> vec, HashMap<String, Double>[] splits) {
		// category for classification always in 0th index
		String category = vec.remove(0);
		
		// first hash in splits is a mapping of classification variable
		// to index in counts array
		if(splits.length==0||splits[0]==null||splits[0].get(category)==null){
			return;
		}
		int index = splits[0].get(category).intValue();
		int[] totVals = hash[0].get("total");//new int[splits[0].size()];
		
		if(totVals != null) {
			totVals[index] += 1;
		} else {
			totVals = new int[splits[0].size()];
			totVals[index] += 1;
		}
		
		
		int i = 1;
		for(String splitKey : vec) {
			if(splits[i]==null)
			{
				i++;
				continue;
			}
			boolean categorical = null == splits[i].values().iterator().next();
			 
			if(categorical) {
//				if(!hash[i * 2].containsKey(splitKey)){
//					int[] vals = new int[splits[0].size()];
//					vals[index] = 1;
//					hash[i * 2].put(splitKey, vals);
//				} else {
					hash[i * 2].get(splitKey)[index]++;//TODO
// 				}
			} else {
				for(Entry<String, Double> compare : splits[i].entrySet()) {
					if(Double.parseDouble(splitKey) <= compare.getValue()) {
						if(!hash[i *2].containsKey(compare.getKey())){
							int[] vals = new int[splits[0].size()];
							vals[index] = 1;
							hash[i * 2].put(compare.getKey(), vals);
						} else {
							hash[i * 2].get(compare.getKey())[index]++;
						}
						// counts hash is indexed as 2 * splits index.  
						// even i is less than, odd i is greater than counts
					} else {
						if(!hash[i *2 + 1].containsKey(compare.getKey())){
							int[] vals = new int[splits[0].size()];
							vals[index] = 1;
							hash[i * 2 + 1].put(compare.getKey(), vals);
						} else {
							hash[i * 2 + 1].get(compare.getKey())[index]++;
						}
					}
				}
			}
			i++;
		}
		
		// first item of counts hash is a HashMap with the total unsplit counts
		hash[0].put("total", totVals);
	}
	
//	public static SplitMappingWritable wrapHash(HashMap<String, int[]>[] hash) {
//		SplitMappingWritable writable = new SplitMappingWritable();
//		MapWritable[] newMap = new MapWritable[hash.length];
//		 
//		int i = 0;
//		for(HashMap<String, int[]> el : hash) {
//			MapWritable temp = new MapWritable();
//			
//			for(Entry<String, int[]> mapEl : el.entrySet()) {
//				String key = mapEl.getKey();
//				int[] val = mapEl.getValue();
//								
//				temp.put(new Text(key), IntegerArrayWritable.wrapArray(val));
//			}
//			
//			newMap[i] = temp;
//			i++;
//		}
//		
//		writable.set(newMap);
//		
//		return writable;
//	}
	
//	public static HashMap<String, int[]>[] aggregate(HashMap<String, int[]>[] first, HashMap<String, int[]>[] second) {
//		HashMap<String, int[]>[] agg = null;
//		
//		return agg;
//	}
	
//	public void aggregate(SplitMappingWritable second) {
//		MapWritable[] curr = (MapWritable[]) this.get();
//		Writable[] agg =  second.get();
//		
//		int i = 0;
//		for(Writable el : agg) {
//			
//			for(Entry<Writable, Writable> mapEl : ((MapWritable)el).entrySet()) {
//				Text key = (Text) mapEl.getKey();
//				IntegerArrayWritable kv = (IntegerArrayWritable) curr[i].get(key);
//				IntegerArrayWritable val = (IntegerArrayWritable) mapEl.getValue();
//				
//				if(kv == null){ 
//					int[] intArr = new int[val.size()]; 
//					kv = IntegerArrayWritable.wrapArray(intArr);
//				}
//				
//				if(val == null){ 
//					int[] intArr = new int[kv.size()];
//					val = IntegerArrayWritable.wrapArray(intArr);
//				}
//				
//				curr[i].put(new Text(key), kv.sum(val));
//			}
//
//			i++;
//		}
//	}
//	
	public SplitMappingWritable clone() {
		SplitMappingWritable newArr = new SplitMappingWritable();
		Writable[] curr = this.get();
		MapWritable[] newMap = new MapWritable[curr.length];
		
		int i = 0;
		for(Writable el : curr) {
			MapWritable temp = new MapWritable();
			
			for(Entry<Writable, Writable> mapEl : ((MapWritable) el).entrySet()) {
				temp.put(new Text((Text) mapEl.getKey()), ((IntegerArrayWritable) mapEl.getValue()).clone());
			}
			
			newMap[i] = temp;
			i++;
		}
		
		newArr.set(newMap);
		
		return newArr;
	}
	
	public boolean equals(Object o) {
		if(o instanceof SplitMappingWritable) {
			try{ 
			MapWritable[] dd = (MapWritable[]) ((SplitMappingWritable) o).toArray();
			MapWritable[] me = (MapWritable[]) this.toArray();
			
			int i = 0;
			if(dd.length != me.length) {
				return false;
			}
			
			for(MapWritable it : me) {
				for(Entry<Writable, Writable> es : it.entrySet()){
					if(!dd[i].get(es.getKey()).equals(es.getValue())) {
						return false;
					}
				}
				i++;
			}
			return true;
			} catch (NullPointerException e) {
				return false;
			}
		}
		return false;
	}
	
	public String prettyPrint() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}
}

