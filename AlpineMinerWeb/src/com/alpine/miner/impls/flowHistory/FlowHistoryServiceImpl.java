package com.alpine.miner.impls.flowHistory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.FlowHistoryService;
import org.apache.log4j.Logger;

public class FlowHistoryServiceImpl implements FlowHistoryService {
    private static Logger itsLogger = Logger.getLogger(FlowHistoryServiceImpl.class);

    private static final String FILE_NAME = "recentlyHistory.hold";
	private static final String PERSISTENCE_ROOT = FilePersistence.initRoot() + "recently_opend_flows" + File.separator;
	private static final LimitCache<String,LimitCache<String,FlowHistoryInfo>> CATEGORY_CACHE = new LimitCache<String,LimitCache<String,FlowHistoryInfo>>(50,true);
	
	private String category;
	
	public FlowHistoryServiceImpl(String category) {
		this.category = category;
	}
	
	@Override
	public Collection<FlowHistoryInfo> getFlowHistory() {
		LimitCache<String,FlowHistoryInfo> history = getHistoryByCategory(category);
		return history.values();
	}

	@Override
	public void pushNewFlowHistory(FlowHistoryInfo flowInfo) {
		LimitCache<String,FlowHistoryInfo> history = getHistoryByCategory(category);
		history.put(flowInfo.getDisplayText(), flowInfo);
		storeHistory(category,history);
	}

	@Override
	public void removeFlowHistory(FlowHistoryInfo flowInfo) {
		LimitCache<String,FlowHistoryInfo> history = getHistoryByCategory(category);
		history.remove(flowInfo.getDisplayText());
		storeHistory(category,history);
	}
	
	@Override
	public void clearFlowHistory() {
		LimitCache<String,FlowHistoryInfo> history = getHistoryByCategory(category);
		history.clear();
		storeHistory(category,history);
	}
	
	private LimitCache<String,FlowHistoryInfo> getHistoryByCategory(String category){
		String categoryHistoryFilePath = PERSISTENCE_ROOT + category + File.separator + FILE_NAME;
		LimitCache<String,FlowHistoryInfo> categoryHistory = CATEGORY_CACHE.get(categoryHistoryFilePath);
		if(categoryHistory == null){
			synchronized(CATEGORY_CACHE){
				categoryHistory = CATEGORY_CACHE.get(categoryHistoryFilePath);
				if(categoryHistory == null){
					categoryHistory = loadHistoryByPath(categoryHistoryFilePath);
					CATEGORY_CACHE.put(categoryHistoryFilePath, categoryHistory);
				}
			}
		}
		return categoryHistory;
	}
	
	private LimitCache<String,FlowHistoryInfo> loadHistoryByPath(String categoryPath){
		File categoryFile = new File(categoryPath);
		LimitCache<String,FlowHistoryInfo> history;
		if(categoryFile.exists()){
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(categoryFile)));
				history = (LimitCache<String, FlowHistoryInfo>) ois.readObject();
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				history = new LimitCache<String, FlowHistoryInfo>(10);
			}finally{
				if(ois != null){
					try {
						ois.close();
					} catch (IOException e) {
						itsLogger.error(e.getMessage(),e);
					}
				}
			}
		}else{
			history = new LimitCache<String, FlowHistoryInfo>(10);
		}
		return history;
	}
	
	private void storeHistory(String category, LimitCache<String,FlowHistoryInfo> history){
		String categoryHistoryFilePath = PERSISTENCE_ROOT + category;
		File categoryFolder = new File(categoryHistoryFilePath);
		if(!categoryFolder.exists()){
			categoryFolder.mkdirs();
		}
		categoryHistoryFilePath += File.separator + FILE_NAME;
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(categoryHistoryFilePath));
			oos.writeObject(history);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		} finally{
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
		}
		
	}
	
	private static class LimitCache<K,V> extends LinkedHashMap<K,V>{
		private static final long serialVersionUID = -5354832724035646523L;
		private int size;
		
		
		
		public LimitCache(int initialCapacity, boolean accessOrder) {
			super(initialCapacity, 1F, accessOrder);
			this.size = initialCapacity;
		}

		@Override
		protected boolean removeEldestEntry(Entry<K,V> eldest) {
			return size < this.size();
		}

		public LimitCache(int size){
			this(size,true);
		}
	}
	
}
