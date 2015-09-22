/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * TempFileManagerTest.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.flow;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.utils.SysConfigManager;
import com.alpine.utility.file.FileUtility;

public class TempFileManagerTest extends AbstractFlowTest{
	String tempDir = System.getProperty( "java.io.tmpdir")+File.separator+"alpinetest"; 
	TempFileManager tfm = TempFileManager.INSTANCE;
	
	
	@Test
	public void testInit() throws Exception {
		
	 
		tfm.init(tempDir,SysConfigManager.DEFAULT_LIVE_TIME,SysConfigManager.DEFAUlT_SCAN_FREQUENCY) ;
		Assert.assertNotNull(tfm.getTempFolder4Flow()) ;
	
		
	}
	@Test
	public void testGetTempFolder4Flow ( ){
		Assert.assertEquals(tfm.getTempFolder4Flow(),tempDir+File.separator+TempFileManager.TYPE_FLOW) ;	
	}
	@Test
	public void testGetTempFolder4Model ( ){
		Assert.assertEquals(tfm.getTempFolder4Model(),tempDir+File.separator+TempFileManager.TYPE_MODEL) ;
	}
	@Test
	public void testGetTempFolder4Report ( ){
		Assert.assertEquals(tfm.getTempFolder4Report(),tempDir+File.separator+TempFileManager.TYPE_REPORT) ;	
	}
	
	//force clear all temp file
	//public void forceClearAll();
	
	//scan all the file find the exceed the live time to delete 
	@Test
	public void testScanAndClear() throws Exception{
		
		String sunFolder = tempDir+File.separator+TempFileManager.TYPE_FLOW+File.separator+"xxx";
		File f = new File(sunFolder) ;
		if(f.exists()==false){
			f.mkdir();
		}
		 
		tfm.init(tempDir,2000,1000) ;
		String fileName = tfm.getTempFolder4Model() + File.separator +"test.test" ;
		FileUtility.writeFile(fileName, "test") ;
		  f  = new File(fileName);
		Assert.assertTrue(f.exists()) ;
		Thread.currentThread().sleep(2000+2000) ;
		tfm.scanAndClear();
		
		Assert.assertFalse(f.exists()) ;
		
		
	}

}
