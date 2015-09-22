/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * IImportDataService.java
 */
package com.alpine.miner.impls.importdata;

import java.io.InputStream;
import java.util.List;

import com.alpine.miner.impls.importdata.impl.UploadDataServiceFileImpl;

/**
 * @author Gary
 * Aug 8, 2012
 */
public interface UploadDataService {
	
	UploadDataService INSTANCE = new UploadDataServiceFileImpl();

	/**
	 * store upload data. will be close the input stream in the function.
	 * @param is
	 * @return	storage identifier
	 */
	String saveUploadData(InputStream is)throws Exception;
	
	/**
	 * to load upload data by id
	 * @param identifier
	 * @param rowCount	line number to read
	 * @return
	 */
	List<String> loadData(String identifier, int rowCount) throws Exception;
	
	/**
	 * load upload data by id
	 * @param identifier
	 * @return
	 * @throws Exception
	 */
	InputStream getDataContent(String identifier) throws Exception;
	
	/**
	 * delete upload data by id
	 * @param identifier
	 */
	void deleteUploadData(String identifier);
	
	/**
	 * clear all upload data.
	 */
	void clearUploadData();
}
