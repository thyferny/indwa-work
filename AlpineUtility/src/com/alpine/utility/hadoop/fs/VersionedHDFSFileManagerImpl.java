/**
 * ClassName  VersionedHDFSFileManagerImpl
 *
 * Version information: 1.00
 *
 * Data: 2012-6-17
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop.fs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;

import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

/**
 * @author John Zhao
 */
public class VersionedHDFSFileManagerImpl implements HadoopHDFSFileManager {
	 
	private Configuration conf = null;
	private String version = ""; 
	private HDFSFileCompressHelper compressHelper= HDFSFileCompressHelper.INSTANCE;
	@Override
	public String toString() {
		return "VersionedHDFSFileManagerImpl [  conf=" + conf + ", version=" + version
				+ "]";
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private final static Logger itsLogger = Logger
			.getLogger(VersionedHDFSFileManagerImpl.class);

	public VersionedHDFSFileManagerImpl(HadoopConnection connection)
			throws Exception {
		// connection ... use reflection
		this.version = connection.getVersion();
		
		conf = new Configuration();
		conf.setInt("ipc.client.connect.max.retries.on.timeouts", 1);
	    conf.setInt("ipc.client.connection.maxidletime", 5000); //5s
		conf.setInt("ipc.client.connect.max.retries", 1);
		connection.fillSecurityInfo2Config(conf);
		
		fillHadoopConfig(connection, conf);
	}

	private void fillHadoopConfig(HadoopConnection hadoopConnection,
			Configuration conf) {

		conf.set(
				HadoopConstants.PROPERTY_FS_NAME,
				HadoopConstants.HDFS_PREFIX
						+ hadoopConnection.getHdfsHostName() + ":"
						+ hadoopConnection.getHdfsPort());
		conf.set(
				HadoopConstants.PROPERTY_JOB_UGI,
				hadoopConnection.getUserName() + ","+ hadoopConnection.getGroupName());
	}

	private HadoopFile toHadoopFile(FileStatus fileStatus, String connName) {
		HadoopFile file = new HadoopFile();
		file.setBlockSize(fileStatus.getBlockSize());
		file.setReplication(fileStatus.getReplication());
		file.setName(fileStatus.getPath().getName());
		file.getAccessTime(fileStatus.getAccessTime());
		file.setOwner(fileStatus.getOwner());
		file.setPermission(fileStatus.getPermission().toString());
		file.setGroup(fileStatus.getGroup());
		file.setIsDir(fileStatus.isDir());
		file.setConnName(connName);
		file.setLength(fileStatus.getLen());
		file.setModificationTime(fileStatus.getModificationTime());
		String path = fileStatus.getPath().toString();
		// http://localhost/tmpxxx
		String parentDir = path.substring(
				path.indexOf(HadoopFile.SEPARATOR, 8),
				path.lastIndexOf(HadoopFile.SEPARATOR));
		if (parentDir.startsWith(HadoopFile.SEPARATOR) == false) {
			parentDir = HadoopFile.SEPARATOR + parentDir;
		}
		file.setParentDir(parentDir);
		return file;
	}

	public List<HadoopFile> getHadoopFiles(String path,
			HadoopConnection connection, boolean isRecursive) throws Exception {
		String uri = connection.getHDFSUrl();
		List<HadoopFile> hadoopFiles = new ArrayList<HadoopFile>();

		FileSystem fs = getHadoopFileSystem(uri, conf);

		FileStatus[] fileStatus = fs.listStatus(getHadoopPath (path)); 
		if (fileStatus != null) {
			for (int i = 0; i < fileStatus.length; i++) {
				HadoopFile hFile = toHadoopFile(fileStatus[i],
						connection.getConnName());
				hFile.setParentDir(path);
				hadoopFiles.add(hFile);
			}
		}
		return hadoopFiles;
	}
	
	@Override
	public HadoopFile getHadoopFile(String path, HadoopConnection connection)
			throws Exception {
		String uri = connection.getHDFSUrl();

		FileSystem fs = getHadoopFileSystem(uri, conf);

		FileStatus fileStatus = fs.getFileStatus(getHadoopPath(path));
		if (fileStatus != null) {
			HadoopFile hFile = toHadoopFile(fileStatus,
					connection.getConnName());

			return hFile;
		} else {
			return null;
		}

	}

	/**
	 * 
	 @return (Identifies given element is a file or directory)
	 */
	@Override
	public boolean isHadoopFile(String filePath, HadoopConnection connection) {
		String uri = connection.getHDFSUrl();

		try {
			FileSystem fs = getHadoopFileSystem(uri, conf);
			String hdfsPath = connection.getHDFSUrl() + filePath;
			Path path = getHadoopPath(hdfsPath);
			return fs.isFile(path);
		} catch (Exception e) {
			itsLogger.error(e);
			return false;
		}
	}

	@Override
	public boolean exists(String filePath, HadoopConnection connection) {
		String uri = connection.getHDFSUrl();

		try {
			FileSystem fs = getHadoopFileSystem(uri, conf);
			String hdfsPath = connection.getHDFSUrl() + filePath;
			Path path = getHadoopPath(hdfsPath);
			return fs.exists(path);
		} catch (Exception e) {
			itsLogger.error(e);
			return false;
		}
	}

	@Override
	public boolean deleteHadoopFile(String filePath, HadoopConnection connection) {
		String uri = connection.getHDFSUrl();

		try {
			FileSystem fs = getHadoopFileSystem(uri, conf);
			String hdfsPath = connection.getHDFSUrl() + filePath;
			Path path = getHadoopPath(hdfsPath);
			if (fs.exists(path)) {
				return fs.delete(path, true);
			}
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			return false;
		}
		return true;
	}

	/***
	 * @param limitSize , Unit is Kb.  
	 * 
	 * Can only support file,not support directory.
	 */
	public String readHadoopFileToStringBySize(String path,
			HadoopConnection connection, long limitSize) throws Exception {
		if (limitSize < 0) {
			String errMessage = "Limit Size can not be less than zero and it has the value of["
					+ limitSize + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		if (null == connection) {
			String errMessage = "Connection that is passed is null["
					+ connection + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		String uri = connection.getHDFSUrl();
		if (StringUtil.isEmpty(uri)) {
			String errMessage = "Could not get the URI from connection. URI is["
					+ uri + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		
		InputStream in = null;
		try {
			in = readFileFromFileSystem(path, connection, uri);

			limitSize=limitSize*1024; //"KB"
			long sizeSum = 0;
			ByteArrayOutputStream swapStream =new ByteArrayOutputStream();
			while(true){
				int flag = in.read();
				if(flag==-1){
					break;
				}
				sizeSum = sizeSum + 1;
				if(sizeSum>limitSize){
					break;
				}
				swapStream.write(flag);
			}
			byte[] resultByteArray = swapStream.toByteArray();
			return new String(resultByteArray,"utf-8");

		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	protected InputStream readFileFromFileSystem(String path,
			HadoopConnection connection, String uri)
			throws Exception, IOException {
		return readFileFromFileSystem(path,connection,uri,true);
	}
	protected InputStream readFileFromFileSystem(String path,
			HadoopConnection connection, String uri,boolean transform)
			throws Exception, IOException {
		FileSystem fs = getHadoopFileSystem(uri, conf);
		String hdfsPath = connection.getHDFSUrl() + path;
		
		Path filePath =getHadoopPath(hdfsPath);
		
		//if is a pure folder, will get a child file
		if(fs.exists(filePath)){
			if (true == fs.getFileStatus(filePath).isDir()) {
				if (hdfsPath.endsWith(HadoopFile.SEPARATOR) == true) {
					filePath = getHadoopPath(hdfsPath + "*");
				} else {
					filePath = getHadoopPath(hdfsPath + HadoopFile.SEPARATOR
							+ "*");
				}
			}
		}
		
		return compressHelper.generateInputStream(fs, filePath,transform);
	}

	/***
	 * @param lineNumber , 0 means read all lines  
	 */
	public String readHadoopPathToStringByLineNumber(String path,
			HadoopConnection connection, int lineNumber) throws Exception {
		List<String> lineList = readHadoopPathToLineList(path, connection, lineNumber);
		StringBuilder responseData = new StringBuilder();
		if(lineList.size()>0){
			for(String line:lineList){
				responseData.append(line).append("\n");
			}
		}
		if(responseData.length()>0){
			responseData=responseData.deleteCharAt(responseData.length()-1);
		}
		return responseData.toString();
	}

	@Override
	public boolean testConnection(HadoopConnection connection) {
	
			    
		if (null == connection) {
			itsLogger.error("Connection that is passed is null[" + connection
					+ "]");
			return false;
		}

		String uri = connection.getHDFSUrl();

		if (StringUtil.isEmpty(uri)) {
			itsLogger.error("Could not get the URI from connection. URI is["
					+ uri + "]");
			return false;
		}

		try {
			FileSystem fs = getHadoopFileSystem(uri, conf);
			fs.listStatus(getHadoopPath("/"));
			return true;
		} catch (Exception e) {
			itsLogger.error("Could not read the file", e);
		}

		return false;

	}

	@Override
	public boolean copyFromLocal(String localFile, String targetDir,
			HadoopConnection connection) throws Exception {
		try {
			if (false == verifyInputs(localFile, targetDir, connection)) {
				return false;
			}
			String uri = connection.getHDFSUrl();
			if (StringUtil.isEmpty(targetDir)) {
				itsLogger.error("Connection URL is empty name[" + uri + "] ");
				return false;
			}

			FileSystem fs = getHadoopFileSystem(uri, conf);
			InputStream in = new BufferedInputStream(new FileInputStream(
					localFile));
			
			String fileName = localFile.substring(
					localFile.lastIndexOf(File.separator) + 1,
					localFile.length());
			OutputStream out = fs.create(getHadoopPath(targetDir
					+ HadoopFile.SEPARATOR + fileName));
			IOUtils.copyBytes(in, out, 4096, true);
		} catch (Throwable e) {
			itsLogger.error("Were unable to copy over the file into Hadoop", e);
			throw new Exception(e);
		}
		return true;

	}

	private boolean verifyInputs(String localFile, String targetDir,
			HadoopConnection connection) {
		if (null == connection) {
			itsLogger.error("Connection is passed as null");
			return false;
		}
		if (StringUtil.isEmpty(targetDir) || StringUtil.isEmpty(localFile)) {
			itsLogger.error("Either or both directory name[" + targetDir
					+ "] and/or local file passed[" + localFile + "] empty");
			return false;
		}
		return true;
	}

	public static boolean verifyInputs4Empty(String parameterName,
			String parameterValue) {
		if (StringUtil.isEmpty(parameterValue) == false) {
			itsLogger.error("[" + parameterName + "] [" + parameterValue
					+ "] value is passed as null or empty");
			return false;
		}
		return true;
	}

	public static boolean isObjectNull(String parameterName, Object valueObject) {
		if (null == valueObject) {
			itsLogger.error("[" + parameterName + "] [" + valueObject
					+ "] value is passed as null");
			return false;
		}
		return true;
	}

	@Override
	public boolean createHadoopFolder(String dirPath,
			HadoopConnection connection) throws Exception {

		if (StringUtil.isEmpty(dirPath)) {
			itsLogger
					.error("Dir path is passed as null or empty with the value of["
							+ dirPath + "]");
			return false;
		}

		if (null == connection) {
			itsLogger.error("Connection that is passed is null[" + connection
					+ "]");
			return false;
		}

		String uri = connection.getHDFSUrl();

		if (StringUtil.isEmpty(uri)) {
			itsLogger.error("Could not get the URI from connection. URI is["
					+ uri + "]");
			return false;
		}

		FileSystem fs = getHadoopFileSystem(uri, conf);
		String hdfsPath = connection.getHDFSUrl() + "/" + dirPath;
		Path path = getHadoopPath(hdfsPath);

		boolean result = fs.mkdirs(path);
		if (result == false) {
			throw new Exception("Can not create folder :" + dirPath);
		}

		return result;
	}

	@Override
	public boolean readHadoopFileToOutputStream(String path,
			HadoopConnection connection, int startLineIndex, int numberOfLines,
			OutputStream outputStream) throws Exception {
		if (false == verifyInputsForReadHadoopFileToOutput(path, connection,
				startLineIndex, numberOfLines, outputStream)) {
			return false;
		}
		InputStream in = null;
		try {
			String uri = connection.getHDFSUrl();
			in = readFileFromFileSystem(path, connection, uri,false);
	
			//pivotal 42013241 When you download hadoop file, the parameter "Number of lines" doesn't work, it always download the whole file
			if(StringUtil.isEmpty(path)==true){
				throw new EmptyFileException("Input path can not be null") ; 
			}
			else if(path.endsWith(".gz")){
				IOUtils.copyBytes(in, outputStream, 4096, true);
			}
			else{
				BufferedReader bfReader = new BufferedReader(
						new InputStreamReader(in));
				String line = null;

				int currentLineIndex = 0;
				while ((line = bfReader.readLine()) != null) {

					currentLineIndex++;
 
					if (startLineIndex < currentLineIndex) {

						outputStream.write(line.getBytes());
						outputStream.write("\n".getBytes());
						// get the specified lines of data
						if (numberOfLines > 0
								&& currentLineIndex >= (numberOfLines + startLineIndex)) {
							break;
						}
					}

				}
			}
		} 
		finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					itsLogger
							.error("Were unable to close the connection due to attached error",
									e);
				}
			}
		}

		return true;
	}

	private boolean verifyInputsForReadHadoopFileToOutput(String path,
			HadoopConnection connection, int from, int numberOfLines,
			OutputStream outputStream) {
		if (StringUtil.isEmpty(path)) {
			String errString = "Number of the lines[" + from + "]or/and from["
					+ outputStream + "] parameters are negative";
			itsLogger.error(errString);
			throw new IllegalArgumentException(errString);
		}

		if (null == connection) {
			String errString = "connection[" + connection + "] is null";
			itsLogger.error(errString);
			throw new IllegalArgumentException(errString);
		}
		if (null == outputStream) {
			String errString = "outputStream[" + connection + "] is null";
			itsLogger.error(errString);
			throw new IllegalArgumentException(errString);
		}
		// -1 means all line
		if (from < 0 || (numberOfLines < 0 && numberOfLines != -1)) {
			String errString = "Number of the lines[" + from + "]or/and from["
					+ outputStream + "] parameters are negative";
			itsLogger.error(errString);
			throw new IllegalArgumentException(errString);
		}

		return true;
	}

	private FileSystem getHadoopFileSystem(String uri, Configuration conf) throws Exception {
		return FileSystem.get(URI.create(uri), conf);
	}

	private Path getHadoopPath(String pathStr) throws Exception {
		return new Path(pathStr);//
	}

	@Override
	public List<HadoopFile> getHadoopFolders(String path,
			HadoopConnection connection, boolean isRecursive) throws Exception {
		String uri = connection.getHDFSUrl();
		List<HadoopFile> hadoopFiles = new ArrayList<HadoopFile>();

		FileSystem fs = getHadoopFileSystem(uri, conf);

		FileStatus[] fileStatus = fs.listStatus(getHadoopPath (path)); 
		if (fileStatus != null) {
			for (int i = 0; i < fileStatus.length; i++) {
				HadoopFile hFile = toHadoopFile(fileStatus[i],
						connection.getConnName());
				hFile.setParentDir(path);
				if(hFile.isDir()){
					hadoopFiles.add(hFile);
				}
			}
		}

		return hadoopFiles;
	}

	@Override
	public InputStream readHadoopFileToInputStream(String path,
			HadoopConnection connection) throws Exception {
		InputStream in = null;
		
		if (null == connection) {
			String errMessage = "Connection that is passed is null["
					+ connection + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		String uri = connection.getHDFSUrl();
		if (StringUtil.isEmpty(uri)) {
			String errMessage = "Could not get the URI from connection. URI is["
					+ uri + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		
		in = readFileFromFileSystem(path, connection, uri);
		
		return in;
	}


	@Override
	public Path getHadoopPath(HadoopConnection connection, String pathStr)
			throws Exception {
		return new Path(pathStr);
	}


	@Override
	public boolean createHadoopFile(String hadoopFileName,
			HadoopConnection connection) throws Exception {
		String uri = connection.getHDFSUrl();

		FileSystem fs = getHadoopFileSystem(uri, conf);
		FSDataOutputStream file = fs.create(getHadoopPath(hadoopFileName));

		if (file != null) {
			file.close();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public FileSystem getHadoopFileSystem(HadoopConnection hadoopConenction)
			throws Exception {
		String uri = hadoopConenction.getHDFSUrl();
		return FileSystem.get(URI.create(uri), conf);
	}

	@Override
	public boolean writeStreamToFile(InputStream in, String targetFile,
			HadoopConnection connection) throws Exception {
		try {

			String uri = connection.getHDFSUrl();
			if (StringUtil.isEmpty(targetFile)) {
				itsLogger.error("Connection URL is empty name[" + uri + "] ");
				return false;
			}


			FileSystem fs = getHadoopFileSystem(uri, conf);
//			OutputStream out = compressHelper.generateOutputStream(fs, getHadoopPath(targetFile));
//			compressHelper.write(in, out, true);
			OutputStream out = fs.create(getHadoopPath(targetFile));
			IOUtils.copyBytes(in, out, 4096, true);
		} catch (Throwable e) {
			itsLogger.error("Were unable to copy over the file into Hadoop", e);
			throw new Exception(e);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.alpine.utility.hadoop.HadoopHDFSFileManager#isPathWritable(com.alpine.utility.hadoop.HadoopConnection, java.lang.String)
	 */
	@Override
	//drwxr-xr-x  or  -rw-------   or -rw-r--r--   or rwxr-xr-x
	//rwxrwxrwx
	public boolean isPathWritable(HadoopConnection hadoopConn, String path) throws Exception   {
		HadoopFile hadoopFile = this.getHadoopFile(path, hadoopConn);
		if(hadoopFile.isDir()==true){
			String tempFileName = path+HadoopFile.SEPARATOR+"tempfile"+System.currentTimeMillis();
			try{
				boolean result= createHadoopFile(tempFileName, hadoopConn) ;
				if(result==true){
					deleteHadoopFile(tempFileName, hadoopConn) ;
					return true ;
				}
			}catch (Exception e){
				return false;
			}
		}
		 return false;
		
	}

	@Override
	public long getTotalFileSize(String path, HadoopConnection connection,
			boolean isRecursive) throws Exception {
		long size = 0;
		if (null == connection) {
			String errMessage = "Connection that is passed is null["
					+ connection + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		String uri = connection.getHDFSUrl();
		if (StringUtil.isEmpty(uri)) {
			String errMessage = "Could not get the URI from connection. URI is["
					+ uri + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		FileSystem fs = getHadoopFileSystem(uri, conf);
		String hdfsPath = connection.getHDFSUrl() + path;

		Path filePath = getHadoopPath(hdfsPath);

		// if is a pure folder, will get a child file
		try {
			if (true == fs.getFileStatus(filePath).isDir()) {
				if (hdfsPath.endsWith(HadoopFile.SEPARATOR) == true) {
					filePath = getHadoopPath(hdfsPath + "*");
				} else {
					filePath = getHadoopPath(hdfsPath + HadoopFile.SEPARATOR
							+ "*");
				}

			}
		} catch (Exception e) {
			// nothing for the path like:golf*.csv
		}
		FileStatus[] files = fs.globStatus(filePath);
		if (files == null || files.length == 0) {
			return 0;
			//avoid error when islocalmode ...
//			throw new Exception("File not found for the path : " + hdfsPath);
		} else {
			for (FileStatus file : files) {
				if (file.isDir() == true) {
					if (isRecursive) {
						String childPath = file.getPath().toUri().getPath();
						size += getTotalFileSize(childPath, connection,
                                isRecursive);
					} else {
						continue;
					}
				} else {
					HadoopFile hFile = toHadoopFile(file,
							connection.getConnName());
					size += hFile.getLength();
				}
			}
		}
		return size;
	}

	@Override
	public boolean isEmptyInput(String path, HadoopConnection connection)
			throws Exception {
		if (null == connection) {
			String errMessage = "Connection that is passed is null["
					+ connection + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		String uri = connection.getHDFSUrl();
		if (StringUtil.isEmpty(uri)) {
			String errMessage = "Could not get the URI from connection. URI is["
					+ uri + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
			FileSystem fs = getHadoopFileSystem(uri, conf);
			String hdfsPath = connection.getHDFSUrl() + path;
			

			Path filePath =getHadoopPath(hdfsPath);
			
			//if is a pure folder, will get a child file
			try{
				if(true==fs.getFileStatus(filePath).isDir()){
					if(hdfsPath.endsWith(HadoopFile.SEPARATOR)==true){
						filePath = getHadoopPath(hdfsPath+"*");
					}
					else{
						filePath = getHadoopPath(hdfsPath+HadoopFile.SEPARATOR+"*");
					}
					
				}
			}catch(Exception e){
				//nothing for the path like:golf*.csv
			}
			FileStatus[] files = fs.globStatus(filePath); 
			if(files==null||files.length==0){
				throw new Exception ("File not found for the path : "+hdfsPath);		
			}else{
				int index=0;
				while(files[index].isDir()==true && index<files.length){
					index = index+1;
				}
				if(index>=files.length ||files[index].isDir()==true){
					throw new Exception ("File not found for the path : "+hdfsPath);
				}
				for(FileStatus file:files){
					if(file.isDir()==true){
						continue;
					}
					else{
					 
						if(file.getLen()>0){
							return false;
						}
					}
				}
			}
		return true;
	}
	
	@Override
	public boolean writeStringToFile(String line, String targetFilePath,
			HadoopConnection connection) throws Exception {
		OutputStream out =null;
		try {
			 
			String uri = connection.getHDFSUrl();
			if (StringUtil.isEmpty(targetFilePath)) {
				itsLogger.error("Connection URL is empty name[" + uri + "] ");
				return false;
			}
			FileSystem fs = getHadoopFileSystem(uri, conf);
//			out = fs.create(getHadoopPath(targetFilePath));
//			HadoopUtil.transformOutStream(targetFilePath, out);
//			
			out = compressHelper.generateOutputStream(fs, getHadoopPath(targetFilePath));
			out.write(line.getBytes());
		} catch (Throwable e) {
			itsLogger.error("Were unable to copy over the file into Hadoop", e);
			throw new Exception(e);
		}
		finally{
			if(out!=null){
				out.flush();
				out.close();
			}
		}
	return true;
	}
	

	@Override
	public boolean isLocalModelNeeded(String filePath,
			HadoopConnection connection) throws Exception {
		//check the gloabale setting first !!!
		String localmode = ProfileReader.getInstance().getParameter(ProfileUtility.HD_LOCAL_MODE  );
		if(StringUtil.isEmpty(localmode)==false){
			return Boolean.parseBoolean(localmode) ;
		}
		
		int threshhold = Integer.parseInt(ProfileReader.getInstance()
				.getParameter(ProfileUtility.LOCAL_HD_RUNNER_THRESHOLD));
		BigDecimal threshholdNative = new BigDecimal(threshhold);
		threshholdNative = threshholdNative
				.multiply(new BigDecimal(1024 * 1024));
		long fileSize = HadoopHDFSFileManager.INSTANCE.getTotalFileSize(
				filePath, connection,false);

		BigDecimal hFileLength = new BigDecimal(fileSize);
		if (hFileLength.compareTo(threshholdNative) > 0) {
			return false;
		}else{
			return true;
		}
	}
	
	//please be careful 
	@Override
	public List<String> readHadoopPathToLineList4All(String path,
			HadoopConnection connection) throws Exception{
		return readHadoopPathToLineList(path, connection, -1) ;
	}

	//if lineNumber==-1, means return all 
	//support .gz
	//path could be a file,a dir or a wildchar like /tmp/*.jar
	@Override
	public List<String> readHadoopPathToLineList(String path,
			HadoopConnection connection, long lineNumber)
			throws Exception {
		if (null == connection) {
			String errMessage = "Connection that is passed is null["
					+ connection + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		String uri = connection.getHDFSUrl();
		if (StringUtil.isEmpty(uri)) {
			String errMessage = "Could not get the URI from connection. URI is["
					+ uri + "]";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		
		FileSystem fs = getHadoopFileSystem(uri, conf);
		String hdfsPath = connection.getHDFSUrl() + path;
		Path filePath =getHadoopPath(hdfsPath);
		try{
			if(true==fs.getFileStatus(filePath).isDir()){
				if(hdfsPath.endsWith(HadoopFile.SEPARATOR)==true){
					filePath = getHadoopPath(hdfsPath+"*");
				}else{
					filePath = getHadoopPath(hdfsPath+HadoopFile.SEPARATOR+"*");
				}		
			}
		}catch(Exception e){
			//nothing for the path like:golf*.csv
		}
 
		FileStatus[] files = fs.globStatus(filePath);		

		List<String> resultList=new ArrayList<String>();
		for(FileStatus file:files){
			if(file.isDir()){
				//nothing to do for now!
			}
			else{//a file
				String childPath=file.getPath().toUri().getPath();
				try {
					 
					if (StringUtil.isEmpty(childPath)) {
						continue;
					}
					//pivotal 42446393, need support gz file
//					if(childPath.endsWith(".gz")){
//						continue;
//					}
					InputStream in = null;
					try {
						in = readFileFromFileSystem(childPath, connection, uri);
						BufferedReader bfReader = new BufferedReader(new InputStreamReader(
								in));
						String line = null;

					 
						while ((line = bfReader.readLine()) != null) {
							resultList.add(line);
							lineNumber = lineNumber-1;
							if (lineNumber == 0) {
								return resultList;
							}
						}
					

					} finally {
						if (in != null) {
							in.close();
						}
					}
				} catch (EmptyFileException e) {
					continue;
				}
			}
		}
		return resultList;
	}
	
	public List<String> getAllRealFilePaths(String path,
			HadoopConnection connection) throws Exception {
		String uri = connection.getHDFSUrl();
		List<String> resultList = new ArrayList<String>(); 

		FileSystem fs = getHadoopFileSystem(uri, conf);
		Path filePath = getHadoopPath(path);

		FileStatus[] files = fs.globStatus(filePath);
		if (files != null  ) {
		 
			for (FileStatus file : files) {
				if (file.isDir() == false) {
					String childPath = file.getPath().toUri().getPath();
					resultList.add(childPath) ;
				}
			}
		}
		return resultList;
	}
}