/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * EULAManager.java
 */
package com.alpine.miner.security.eula;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.miner.security.impl.SecurityConfiguration;
import com.alpine.miner.security.impl.SecurityConfiguration.ProviderType;

/**
 * @author Gary
 * Dec 24, 2012
 */
public class EULAManager {
	
	private static final Logger LOG = Logger.getLogger(EULAManager.class);

	private static final EULAManager INSTANCE = new EULAManager();
	
	private static final String POSTFIX = "_recorder";
	
	private Map<ProviderType, Set<String>> acceptCache = new EnumMap<ProviderType, Set<String>>(ProviderType.class);
	
	private EULAManager(){}
	
	public static EULAManager getInstance(){
		return INSTANCE;
	}
	
	// call this while tomcat startup.
	public void init(){
		if(EULAConfig.getInstance().getAcceptStatus()){
			// wee need reset all accept records and turn accept status to false.
			resetAcceptRecords();
			EULAConfig.getInstance().setAcceptStatus(false);
		}
	}
	
	public boolean isUserAccepted(String userName){
		SecurityConfiguration securityConfig = ProviderFactory.loadConfiguration();
		ProviderType securityType = securityConfig.getCurrent_choice();
		return loadRecorderByProviderType(securityType).contains(userName);
	}
	
	public void saveAcceptStatus(String userName){
		SecurityConfiguration securityConfig = ProviderFactory.loadConfiguration();
		ProviderType securityType = securityConfig.getCurrent_choice();
		Set<String> recorder = loadRecorderByProviderType(securityType);
		recorder.add(userName);
		storeRecorder(recorder, securityType);
	}
	
	private void resetAcceptRecords(){
		for(ProviderType pt : ProviderType.values()){
			resetAcceptRecordsByProviderType(pt);
		}
	}
	
	private void resetAcceptRecordsByProviderType(ProviderType pt){
		storeRecorder(new HashSet<String>(), pt);
	}
	
	private synchronized Set<String> loadRecorderByProviderType(ProviderType pt){
		Set<String> recorder = acceptCache.get(pt);
		if(recorder != null){
			return recorder;
		}
		File root = getRootFolder();
		File recorderFile = new File(root, pt.toString() + POSTFIX);
		if(!recorderFile.exists()){
			recorder = new HashSet<String>();
			acceptCache.put(pt, recorder);
			return recorder;
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(recorderFile));
			recorder = (Set<String>) ois.readObject();
		} catch (Exception e) {
			LOG.error(e);
			recorder = new HashSet<String>();
		}finally{
			try {
				ois.close();
			} catch (IOException e) {
				//ignore
			}
		}
		acceptCache.put(pt, recorder);
		return recorder;
	}
	
	private synchronized void storeRecorder(Set<String> recorder, ProviderType pt){
		ObjectOutputStream oos = null;
		File root = getRootFolder();
		File recorderFile = new File(root, pt.toString() + POSTFIX);
		try {
			oos = new ObjectOutputStream(new FileOutputStream(recorderFile));
			oos.writeObject(recorder);
		} catch (Exception e) {
			LOG.error(e);
		} finally{
			try {
				oos.close();
			} catch (IOException e) {
				//ignore
			}	
		}
		acceptCache.put(pt, recorder);
	}
	
	private File getRootFolder(){
		File rootFolder = new File(FilePersistence.ROOT + File.separator + "EULA");
		if(!rootFolder.exists()){
			rootFolder.mkdirs();
		}
		return rootFolder;
	}
}
