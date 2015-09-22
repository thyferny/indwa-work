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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.ResourceManager;

/**
 * @author sam_zang
 * 
 */
public class PublishFlowTest{

	static final String DATA_DIR = "test_data";
	// static final String host = "192.168.31.128:8080";
	static final String host = "localhost:8000";
	
	ResourceManager rmgr = ResourceManager.getInstance();
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

//	@Test
	public void publishAll() throws Exception {
		File folder = new File(DATA_DIR);
		
		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isFile() == true && f.getName().endsWith(".afm")) {
				int endIndex = f.getName().length() - 4;
				String name = f.getName().substring(0, endIndex);
				doPublish(name);
			}			
		}
	}
	
	/**
	 * Here is an example of how to read a flow file
	 * and publish it to the server.
	 * 
	 * @throws IOException
	 */
	// @Test
	public void publishFlow() throws Exception {
			
		doPublish("sample_aggregate");		
		doPublish("sample_algorithm_comparison");
		doPublish("sample_attribute_analysis");
		doPublish("sample_bar_chart_preview");
		doPublish("sample_correlation");
		doPublish("sample_customer_abandonment");
		doPublish("sample_decision_tree_train_normalized");
	}

	private void doPublish(String flowName)
			throws IOException, HttpException, OperationFailedException {
		String fileName = DATA_DIR + File.separator + flowName + ".afm";
		String xmlData = readFile(fileName);
		
		
			rmgr.publishFlow(host, 
					ResourceType.Public,
					"admin",
					null, 
					flowName, 
					xmlData,"comments");
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
