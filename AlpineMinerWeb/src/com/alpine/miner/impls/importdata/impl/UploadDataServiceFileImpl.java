/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ImportDataServiceFileImpl.java
 */
package com.alpine.miner.impls.importdata.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alpine.miner.impls.importdata.UploadDataService;
import com.alpine.miner.impls.web.resource.FilePersistence;
import org.apache.log4j.Logger;

/**
 * @author Gary
 * Aug 8, 2012
 */
public class UploadDataServiceFileImpl implements UploadDataService {
    private static final Logger itsLogger =Logger.getLogger(UploadDataServiceFileImpl.class);

    private static final File ROOT_FOLDER = new File(FilePersistence.ROOT + "import_data" + File.separator);
	private static final String extensionName = ".csv";

	static{
		if(!ROOT_FOLDER.exists()){
			ROOT_FOLDER.mkdirs();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.importdata.ImportDataService#saveUploadData(java.io.InputStream)
	 */
	@Override
	public String saveUploadData(InputStream is) throws Exception {
		String fileName = UUID.randomUUID().toString();
		File dataFile = new File(ROOT_FOLDER, fileName + extensionName);
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(new FileOutputStream(dataFile));
			byte[] buffere = new byte[1024];
			int offset;
			while((offset = bis.read(buffere)) != -1){
				bos.write(buffere, 0, offset);
			}
			bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw e;
		}finally{
			bis.close();
			bos.close();
		}
		return fileName;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.importdata.ImportDataService#loadData(java.lang.String)
	 */
	@Override
	public List<String> loadData(String identifier, int rowCount) throws Exception {
		List<String> dataList = new ArrayList<String>();
		File dataFile = new File(ROOT_FOLDER, identifier + extensionName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
			int i = 0;
			String row = null;
			while((row = reader.readLine()) != null){
				if(i++ >= rowCount){
					break;
				}
				dataList.add(row);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw e;
		}finally{
			reader.close();
		}
		return dataList;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.importdata.ImportDataService#deleteUploadData(java.lang.String)
	 */
	@Override
	public void deleteUploadData(String identifier) {
		File dataFile = new File(ROOT_FOLDER, identifier + extensionName);
		if(dataFile.exists()){
			dataFile.delete();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.importdata.ImportDataService#clearUploadData()
	 */
	@Override
	public void clearUploadData() {
		for(File dataFile : ROOT_FOLDER.listFiles()){
			try{
				dataFile.delete();
			}catch(Exception e){
				e.printStackTrace();
				itsLogger.warn(e.getMessage());
				continue;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.importdata.ImportDataService#getDataContent(java.lang.String)
	 */
	@Override
	public InputStream getDataContent(String identifier) throws Exception {
		File dataFile = new File(ROOT_FOLDER, identifier + extensionName);
		return new BufferedInputStream(new FileInputStream(dataFile));
	}

}
