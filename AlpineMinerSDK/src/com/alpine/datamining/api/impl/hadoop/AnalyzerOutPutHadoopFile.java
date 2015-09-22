/**
 * ClassName AnalyzerOutPutHadoopFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.utility.hadoop.HadoopConnection;
/**
 * @author Eason
 * 
 */

public class AnalyzerOutPutHadoopFile extends AbstractAnalyzerOutPut {
	/**
	 * 
	 */
	private static final long serialVersionUID = 384104346809747259L;

	private HadoopConnection conn;
	
	private String path;
	
	private String data;
	
	public HadoopConnection getConn() {
		return conn;
	}
	public void setConn(HadoopConnection conn) {
		this.conn = conn;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
}
