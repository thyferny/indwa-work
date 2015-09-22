/**
 * ClassName HDFSFileCompressHelperImpl.java
 *
 * Version information: 1.00
 *
 * Data: 2012-12-14
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.fs;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public class HDFSFileCompressHelperImpl implements HDFSFileCompressHelper {

	@Override
	public OutputStream generateOutputStream(FileSystem fs, Path path) throws Exception{
		OutputStream out = fs.create(path);
		
		String targetPath=path.getName();
		if(StringUtil.isEmpty(targetPath)==false&&targetPath.endsWith(".gz")){
			out=new GZIPOutputStream(out);
		}
		return out;
	}
	
	@Override
	public OutputStream appendOutputStream(FileSystem fs, Path path)
			throws Exception {
		OutputStream out = fs.append(path);
		
		String targetPath=path.getName();
		if(StringUtil.isEmpty(targetPath)==false&&targetPath.endsWith(".gz")){
			out=new GZIPOutputStream(out);
		}
		return out;
	}

	@Override
	public InputStream generateInputStream(FileSystem fs, Path path,boolean deCompress) throws Exception{
		InputStream in=null;
		// support file lik *.log...
		FileStatus[] files = fs.globStatus(path);
		if (files == null || files.length == 0) {
			throw new Exception("File not found for the path : " + path.getName());
		} else{//files.length>0
			int index=0;
			if(files.length==1&&files[0].isDir()==false&&files[0].getLen()==0){
				throw new EmptyFileException("File length equals zero: "+path.getName());
			}
			while(index<files.length&&(files[index].isDir()==true||files[index].getLen()==0)){
				index = index+1;
			}
			if(index>=files.length ||files[index].isDir()==true){
				throw new FileNotFoundException("File not found for the path : "+path.getName());
			}
			//find the first not empty file in the path (if the path is a folder or a wildcar)
			//if the filepath is the real file, then it is the one opended
			in = fs.open( files[index].getPath());
		}
		String inputPath=path.getName();
		if(deCompress==true&&StringUtil.isEmpty(inputPath)==false&&inputPath.endsWith(".gz")){
			in = new GZIPInputStream(in);
		}
		return in;
	}

	@Override
	public void write(InputStream in, OutputStream out, boolean close) throws Exception{
		IOUtils.copyBytes(in, out, 4096, close);
	}
}
