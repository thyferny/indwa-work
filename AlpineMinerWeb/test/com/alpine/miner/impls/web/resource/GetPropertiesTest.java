/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * PublishFlowTest.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jul 3, 2011
 */

package com.alpine.miner.impls.web.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.impls.controller.FlowDTO;
import com.alpine.miner.impls.controller.OperatorDTO;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.customize.UDFManager;

/**
 * @author sam_zang
 * 
 */
public class GetPropertiesTest{

	static final String DATA_DIR = "test_data";

	ResourceManager rmgr = ResourceManager.getInstance();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
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

	static StringBuffer data;

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// String path = DATA_DIR +File.separator + "prop.txt";
		// File f = new File(path);
		// if (f.exists() == false) {
		// f.createNewFile();
		// }
		// // System.out.println("Create file at: " + f.getAbsolutePath());
		//
		// Writer out = null;
		// try {
		// out = new OutputStreamWriter(new FileOutputStream(path));
		// out.write(data.toString());
		// } finally {
		// if (out != null) {
		// out.close();
		// }
		// }
	}

	@Test
	public void getAll() {
		List<FlowInfo> list = rmgr.getFlowList("Public");
		for (FlowInfo info : list) {
			try {
				OperatorWorkFlow flow = rmgr.getFlowData(info,Locale.getDefault());
				new FlowDTO(info, flow, "guest");
				for (UIOperatorModel op : flow.getChildList()) {
					System.out.println("================================");
					OperatorDTO d = new OperatorDTO(null, info, op,flow.getVariableModelList(),Locale.getDefault());
					d.updateCustomProperty(op, null, null);
					System.out.println("");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private String readFile(String fileName) throws IOException {
		StringBuilder data = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = new Scanner(new FileInputStream(fileName));
		try {
			while (scanner.hasNextLine()) {
				data.append(scanner.nextLine() + NL);
			}
		} finally {
			scanner.close();
		}

		return data.toString();
	}
}
