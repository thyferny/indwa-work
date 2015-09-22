/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ImportDataTest.java
 */
package com.alpine.miner.impls.importdata;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ImportDataConfiguration;
import com.alpine.importdata.ImportDataConfiguration.ColumnStructure;
import com.alpine.importdata.ImportDataService;
import com.alpine.importdata.ImportDataService.ImportHandler;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;

/**
 * @author Gary
 * Aug 15, 2012
 */
public class ImportDataTest extends TestCase{

	@Test
	public void testImportDataToDB() throws Exception{
		FileInputStream fis = new FileInputStream("E:\\importTest.csv");
		final List<String> failMsgSet = new ArrayList<String>();
		String identifier = UploadDataService.INSTANCE.saveUploadData(fis);
		InputStream dataContent = UploadDataService.INSTANCE.getDataContent(identifier);
		ImportDataConfiguration config = buildTestConf();
		long startTime = new Date().getTime();
		ImportDataService.importData(config, dataContent, new ImportHandler() {
			private boolean ableToContinue = true;
			@Override
			public void onFailed(Exception e) {
            	SQLException currentException = (SQLException) e;
                while((currentException = currentException.getNextException()) != null){
                	currentException.printStackTrace();
    				failMsgSet.add(e.getMessage());
                }
                ableToContinue = false;
			}

			@Override
			public void onAbort(Exception e) {
				System.out.println("import has been abort.");
                ableToContinue = false;
			}

			@Override
			public void onFailRow(int rowIdx, String[] row, Exception e) {
				failMsgSet.add(e.getMessage());
			}
			
			@Override
			public void onCompleteRow(int rowIdx, String[] row) {
				System.out.println(rowIdx + " is complete!");
			}

			@Override
			public boolean isContinue() {
				return ableToContinue;
			}
		});
		System.out.println(new Date().getTime() - startTime + "ms");
		Assert.assertTrue(failMsgSet.size() == 0);
	}
	
	private ImportDataConfiguration buildTestConf() throws Exception{
		ImportDataConfiguration config = new ImportDataConfiguration();
		config.setDelimiter('|');
		config.setEscape('\\');
		config.setIncludeHeader(true);
		config.setLimitNum(-1);
		config.setQuote('"');
		config.setSchemaName("public");
		config.setTableName("IMPORT_UNIT_TEST");
		config.setConnectionInfo(WebDBResourceManager.getInstance().getDBConnection("admin", "Connection Demo", ResourceType.Personal).getConnection());

		config.addColumnStructure(new ColumnStructure("ID", DatabaseDataType.INTEGER, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_STRING", DatabaseDataType.VARCHAR, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_CHAR", DatabaseDataType.CHAR, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_NUMERIC", DatabaseDataType.NUMERIC, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_INTEGER", DatabaseDataType.INTEGER, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_BOOLEAN", DatabaseDataType.BOOLEAN, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_DATE", DatabaseDataType.DATE, true, true));
		config.addColumnStructure(new ColumnStructure("COLUMN_TIMESTAMP", DatabaseDataType.DATETIME, true, true));
		return config;
	}
}
