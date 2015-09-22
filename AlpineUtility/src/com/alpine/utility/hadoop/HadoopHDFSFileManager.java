/**
 * ClassName  HadoopHDFSFileManagerImpl
 *
 * Version information: 1.00
 *
 * Data: 2012-6-17
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alpine.utility.hadoop.fs.HadoopHDFSFileManagerWrapper;

/**
 * @author John Zhao
 */
public interface  HadoopHDFSFileManager { 
	public static final String OP_GET_FILE_STATUS = "GETFILESTATUS"; 
	public static final String OP_LIST_STATUS = "LISTSTATUS";
	public static final String ROOT_PATH = "/";
	
	
	public boolean writeStreamToFile(InputStream inStream,String targetFilePath,HadoopConnection connection) throws Exception ;
	
	public boolean writeStringToFile(String line,String targetFilePath,HadoopConnection connection) throws Exception  ;
	
	public static   HadoopHDFSFileManager INSTANCE=  HadoopHDFSFileManagerWrapper.INSTANCE ;
	public   boolean testConnection(HadoopConnection connection) throws Exception  ;
	
	boolean copyFromLocal(String localFile, String targetDir, HadoopConnection connection)
			throws Exception ;

	/***
	 * @param limitSize , Unit is Kb.  
	 */
	public   String readHadoopFileToStringBySize(String path,
			HadoopConnection connection,long limitSize) throws Exception ;
	/***
	 * @param lineNumber , 0 means read all lines  
	 */
	public   String readHadoopPathToStringByLineNumber(String path,
			HadoopConnection connection,int lineNumber) throws Exception ;
	
	public boolean readHadoopFileToOutputStream(String path,HadoopConnection connection, int from, int numberOfLines,OutputStream outputStream) throws Exception;
	public InputStream readHadoopFileToInputStream(String path,HadoopConnection connection) throws Exception;
	
//path= /user/home... 	// "/" mean root
	public   List<HadoopFile> getHadoopFiles(String path,HadoopConnection connection, boolean isRecursive) throws Exception ;
	public   List<HadoopFile> getHadoopFolders(String path,HadoopConnection connection, boolean isRecursive) throws Exception ;
//	public abstract String[] getHadoopFileOutput(String path, HadoopConnection connection) throws Exception;



	public abstract boolean deleteHadoopFile(String filePath, HadoopConnection connection);
	/**
	@return (Identifies given element is a file or directory)
	*/
	public abstract boolean isHadoopFile(String filePath, HadoopConnection connection);
	
	public boolean createHadoopFolder(String dirPath, HadoopConnection connection) throws Exception;

	/**
	 * @param path
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	public HadoopFile getHadoopFile(String path, HadoopConnection connection)
			throws Exception;

	public boolean exists(String filePath, HadoopConnection connection);
 
	public boolean createHadoopFile(String hadoopFileName,
			HadoopConnection hadoopConenction) throws Exception;

	public FileSystem getHadoopFileSystem(HadoopConnection hadoopConenction) throws Exception;

	public Path getHadoopPath(HadoopConnection connection,String hadoopFileName) throws Exception;

//	boolean mergeHadoopFiles(List<String> sourcePaths, String targetPath,
//			HadoopConnection connection) throws Exception; 

	boolean isPathWritable(HadoopConnection hadoopConn, String path) throws Exception;

	//path could  be a file, a dir, or a wildchar    /home/log/*123.log
	public long getTotalFileSize(String path, HadoopConnection connection, boolean isRecursive)
			throws Exception;

	boolean isLocalModelNeeded(String filePath, HadoopConnection connection)
			throws Exception;

	public List<String> readHadoopPathToLineList(String fullPathFileName,
			HadoopConnection hadoopConnection, long lineNumber) throws IOException, Exception;
	public List<String> readHadoopPathToLineList4All(String path,
			HadoopConnection connection) throws Exception;
//	public String readHadoopDirToString(String path,
//			HadoopConnection connection ,boolean isRecursive) throws Exception;
	
//	public String readHadoopDirToStringLimitLine(String path,
//			HadoopConnection connection ,boolean isRecursive,long lineNumber) throws Exception;

	public boolean isEmptyInput(String path, HadoopConnection connection)
			throws Exception;

	//input  could be a file, folder or wild char
	public List<String> getAllRealFilePaths(String path,
			HadoopConnection hadoopConnection) throws Exception;
}