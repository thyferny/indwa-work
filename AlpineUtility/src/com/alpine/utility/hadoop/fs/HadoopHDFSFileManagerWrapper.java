/**
 * ClassName  HadoopHDFSFileManagerImpl
 *
 * Version information: 1.00
 *
 * Data: 2012-6-17
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;

/**
 * @author John Zhao
 */
public class  HadoopHDFSFileManagerWrapper implements HadoopHDFSFileManager{
	public static final HadoopHDFSFileManager INSTANCE = new HadoopHDFSFileManagerWrapper();
	private final static  Logger itsLogger=Logger.getLogger(HadoopHDFSFileManagerWrapper.class);
	
	private HadoopHDFSFileManagerWrapper(){
		  
	}
 
	public List<HadoopFile> getHadoopFiles(String path,
			HadoopConnection connection, boolean isRecursive) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getHadoopFiles(path, connection, isRecursive); 
	}

	@Override
	public  HadoopFile  getHadoopFile(String path, HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getHadoopFile(path, connection) ;
	
	}
 
 
	/**
	 *
	@return (Identifies given element is a file or directory)
	*/
	@Override
	public boolean isHadoopFile(String filePath,HadoopConnection connection){
		try {
			HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
			return fsManager.isHadoopFile(filePath, connection);
		} catch (Exception e) {
			itsLogger.equals(e) ;
			return false;
		}
		
	}
	@Override
	public boolean exists(String filePath,HadoopConnection connection){
		try {
			HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
			return fsManager.exists(filePath, connection);
		} catch (Exception e) {
			itsLogger.equals(e) ;
			return false;
		}
	}
	
	@Override
	public boolean deleteHadoopFile(String filePath,HadoopConnection connection){
		try {
			HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
			return fsManager.deleteHadoopFile(filePath, connection);
		} catch (Exception e) {
			itsLogger.equals(e) ;
			return false;
		}
	}
	
	public String readHadoopPathToStringByLineNumber(String path,
			HadoopConnection connection, int lineNumber) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.readHadoopPathToStringByLineNumber(path, connection, lineNumber);
	}
	@Override
	public boolean testConnection(HadoopConnection connection) throws Exception {
		
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.testConnection(connection);
	}
	
	@Override
	public boolean copyFromLocal(String localFile, String targetDir,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.copyFromLocal(localFile, targetDir, connection);
	}

	@Override
	public boolean createHadoopFolder(String dirPath,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.createHadoopFolder(dirPath,  connection);
	}
 
	@Override
	public boolean readHadoopFileToOutputStream(String path,
			HadoopConnection connection, int startLineIndex, int numberOfLines,
			OutputStream outputStream) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.readHadoopFileToOutputStream(path, connection, startLineIndex, numberOfLines, outputStream);
	}

	@Override
	public List<HadoopFile> getHadoopFolders(String path,
			HadoopConnection connection, boolean isRecursive) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getHadoopFolders(path, connection, isRecursive);
	}

	@Override
	public InputStream readHadoopFileToInputStream(String path,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.readHadoopFileToInputStream(path, connection);
	}




	@Override
	public boolean createHadoopFile(String hadoopFileName,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.createHadoopFile(hadoopFileName, connection) ;
		
	}

	@Override
	public FileSystem getHadoopFileSystem(HadoopConnection connection)
			throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getHadoopFileSystem(connection) ;
	}

	@Override
	public Path getHadoopPath(HadoopConnection connection,String hadoopFileName) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getHadoopPath( connection,hadoopFileName) ;
	}

//	@Override
//	public boolean mergeHadoopFiles(List<String> sourcePaths,
//			String targetPath, HadoopConnection connection) throws Exception {
//		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
//		return fsManager.mergeHadoopFiles(sourcePaths, targetPath,connection) ;
//	}

	@Override
	public boolean writeStreamToFile(InputStream inStream, String targetFilePath,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.writeStreamToFile(inStream, targetFilePath,connection) ;
	}

	/* (non-Javadoc)
	 * @see com.alpine.utility.hadoop.HadoopHDFSFileManager#isPathWritable(com.alpine.utility.hadoop.HadoopConnection, java.lang.String)
	 */
	@Override
	public boolean isPathWritable(HadoopConnection hadoopConn, String path)
			throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hadoopConn);
		return fsManager.isPathWritable(hadoopConn, path);
	}

	@Override
	public long getTotalFileSize(String path, HadoopConnection connection,boolean isRecrusive)
			throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getTotalFileSize(path, connection,isRecrusive);
	}

	@Override
	public String readHadoopFileToStringBySize(String path,
			HadoopConnection connection, long limitSize) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.readHadoopFileToStringBySize(path, connection, limitSize);
	}

	@Override
	public boolean writeStringToFile(String line, String targetFilePath,
			HadoopConnection connection) throws Exception  {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.writeStringToFile(line, targetFilePath, connection) ;
	}

	@Override
	public boolean isLocalModelNeeded(String filePath,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.isLocalModelNeeded(filePath, connection) ;
	}

	@Override
	public boolean isEmptyInput(String path, HadoopConnection connection)
			throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.isEmptyInput(path, connection);
	}

	@Override
	public List<String> readHadoopPathToLineList(String fullPathFileName,
			HadoopConnection hadoopConnection, long lineNumber)
			throws IOException, Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(hadoopConnection);
		return fsManager.readHadoopPathToLineList(fullPathFileName, hadoopConnection, lineNumber);
	}

	@Override
	public List<String> readHadoopPathToLineList4All(String path,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.readHadoopPathToLineList4All(path, connection);
	}

	@Override
	public List<String> getAllRealFilePaths(String filePath,
			HadoopConnection connection) throws Exception {
		HadoopHDFSFileManager fsManager = HadoopHDFSFileManagerFactory.INSTANCE.getHadoopHDFSFileManager(connection);
		return fsManager.getAllRealFilePaths(filePath, connection);
	}


}