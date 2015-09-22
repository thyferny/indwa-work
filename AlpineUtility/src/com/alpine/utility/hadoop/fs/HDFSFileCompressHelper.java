/**
 * ClassName HDFSFileCompressHelper.java
 *
 * Version information: 1.00
 *
 * Data: 2012-12-14
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.fs;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alpine.utility.hadoop.HDFSFileHelper;

/**
 * @author Jeff Dong
 *
 */
public interface HDFSFileCompressHelper extends HDFSFileHelper {

	public static HDFSFileCompressHelper INSTANCE=  new HDFSFileCompressHelperImpl();
	
	public abstract OutputStream generateOutputStream(FileSystem fs,Path path) throws Exception;
	
	public abstract OutputStream appendOutputStream(FileSystem fs,Path path) throws Exception;
	
	public abstract InputStream generateInputStream(FileSystem fs,Path path,boolean deCompress) throws Exception;
	
	public abstract void write(InputStream in,OutputStream out,boolean close) throws Exception;
}
