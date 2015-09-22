/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OperatorFrequentManagementImpl.java
 */
package com.alpine.miner.impls.editworkflow.operator.frequentmgr.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.miner.impls.editworkflow.operator.frequentmgr.IOperatorFrequentManagement;
import com.alpine.miner.impls.web.resource.FilePersistence;

/**
 * @author Gary
 * Aug 22, 2012
 */
public class OperatorFrequentManagementFileImpl implements IOperatorFrequentManagement {
	
	private static final File STORAGE_ROOT = new File(FilePersistence.ROOT, "operator_frequent");
	
	private static final String FILE_NAME = "FREQUENCE_OPERATOR";
	
	private static final Map<String, HashMap<String, Integer>> OPERATOR_FREQUENCE_CACHE = new HashMap<String, HashMap<String, Integer>>(); 
	
	private static final Logger LOGGER = Logger.getLogger(OperatorFrequentManagementFileImpl.class);
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.operator.frequentmgr.IOperatorFrequentManagement#increaseFrequent(java.lang.String)
	 */
	@Override
	public void increaseFrequent(String operatorClassName, String userName) {
		//ignore Dataset and Hadoop File Operator.
		if("DbTableOperator".equals(operatorClassName) || "HadoopFileOperator".equals(operatorClassName)){
			return;
		}
		HashMap<String, Integer> cache = getCache(userName);
		Integer usedCount = cache.get(operatorClassName);
		cache.put(operatorClassName, (usedCount == null ? 0 : usedCount) + 1);
		storeCache(cache, userName);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.operator.frequentmgr.IOperatorFrequentManagement#getFrequentOperatorNameList(int)
	 */
	@Override
	public List<String> getFrequentOperatorNameList(int topSize, String userName) {
		final Map<String, Integer> cache = getCache(userName);
		List<String> frequenceList = new ArrayList<String>(cache.keySet());
		Collections.sort(frequenceList, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				Integer o1Frequent = cache.get(o1),
					o2Frequent = cache.get(o2);
				return o2Frequent.compareTo(o1Frequent);
			}
		});
		topSize = frequenceList.size() >= topSize ? topSize : frequenceList.size();
		return frequenceList.subList(0, topSize);
	}
	
	private HashMap<String, Integer> getCache(String userName){
		HashMap<String, Integer> cache = OPERATOR_FREQUENCE_CACHE.get(userName);
		if(cache == null){
			synchronized(OPERATOR_FREQUENCE_CACHE){
				cache = OPERATOR_FREQUENCE_CACHE.get(userName);
				if(cache == null){
					cache = loadCacheFromFile(userName);
				}
			}
		}
		return cache;
	}
	
	private HashMap<String, Integer> loadCacheFromFile(String userName){
		File cacheFile = new File(STORAGE_ROOT, userName + File.separator + FILE_NAME);
		HashMap<String, Integer> cache;
		if(cacheFile.exists()){
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
				cache = (HashMap<String, Integer>) ois.readObject();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				cache = new HashMap<String, Integer>();
			}finally{
				if(ois != null){
					try {
						ois.close();
					} catch (IOException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}else{
			cache = new HashMap<String, Integer>();
		}
		return cache;
	}
	
	private void storeCache(HashMap<String, Integer> cache, String userName){
		File userFolder = new File(STORAGE_ROOT, userName);
		if(!userFolder.exists()){
			userFolder.mkdirs();
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(userFolder, FILE_NAME)));
			oos.writeObject(cache);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally{
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
}
