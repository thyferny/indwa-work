/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ResourceManagerTest.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.flow;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.utils.SysConfigManager;
import com.alpine.miner.workflow.operator.customize.UDFManager;

public abstract class AbstractFlowTest {
	private static final String TEST_DATA = "test_data";


    private static final String TEST_DIR = "alpine_test";
	public static final String test_root =  System.getProperty("java.io.tmpdir") + TEST_DIR;
    private static File A_D_R = new File("AlpineMinerWeb/test_data/ALPINE_DATA_REPOSITORY");
    public static File test_data = new File("AlpineMinerWeb/test_data");
    private static File root = new File(test_root);

	public static final String USER_GUEST = "guest";
	public static final String USER_ADMIN = "admin";
	
	private static String   original =null;
	static File f = new File("");
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

        original = System.getProperty("user.home");
        System.getProperties().put("user.home",test_root);
        FileUtils.deleteDirectory(new File(test_root));
        FileUtils.copyDirectoryToDirectory(A_D_R, root);
		
		//init the  
		String temp = test_root + File.separator + "alpine_temp"; //.getContextPath();
		TempFileManager.INSTANCE.init(temp,SysConfigManager.DEFAULT_LIVE_TIME,SysConfigManager.DEFAUlT_SCAN_FREQUENCY) ;
		
		
		//please be carefule, this is very important to init the preference stuff
		ResourceManager rmgr = ResourceManager.getInstance();
		if(rmgr.isPreferenceInited()==false){
			rmgr.SetPreferenceInited(true);
			Collection<PreferenceInfo> preferences = rmgr.getPreferences();
			for(Iterator<PreferenceInfo> it= preferences.iterator();it.hasNext();){
				PreferenceInfo pref = it.next();		 
				rmgr.updateProfileReader(pref);
			}
			
		}
		//tel the UDF manager the filePath to save
		UDFManager.INSTANCE.setRootDir(FilePersistence.UDFPRFIX);
		UDFManager.INSTANCE.setOperatorRegistryRootDir(FilePersistence.UDFPRFIX);
		//register the db manager
		DBResourceManagerFactory.INSTANCE.registerDBResourceManager(WebDBResourceManager.getInstance()) ;

	}


	protected static String getTestDataRootDir() {
		return new File("AlpineMinerWeb/test_data").getAbsolutePath();
	}


	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public  static void tearDownAfterClass() throws Exception {
        FileUtils.deleteDirectory(new File(test_root));
		System.getProperties().put("user.home",original);
	}
}
