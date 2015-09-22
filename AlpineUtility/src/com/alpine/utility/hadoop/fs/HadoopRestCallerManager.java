package com.alpine.utility.hadoop.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpineRestPigJsonObject;
import com.alpine.utility.hadoop.pig.AlpineRestPigJsonObjectSerializerDeserializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class HadoopRestCallerManager implements HadoopHDFSFileManager {
	private static final String REST_ENABLED = "REST_ENABLED";
	private HadoopConnection connection;
	public static enum RESTCallType{
		pig,hdfs,mapReduce
	}
	
	private static boolean isRest;
	static{
		String isRestEnabled=System.getProperty(REST_ENABLED);
		if(null!=isRestEnabled){
			isRest=Boolean.parseBoolean(isRestEnabled);
		}else{
			isRest=false;
		}
	}
	
	public static boolean isRestEnabled(){
		return isRest;
	}
	public static void setRestFlag(boolean override,boolean isRestEnabled){
		if(override)
			isRest=isRestEnabled;
	}
	public static ClientResponse makeARESTCallTo(HadoopConnection coneection,String jsonObject,RESTCallType restCallType) {
		try {
			String endpoint;
			switch(restCallType){
			case pig:{
				endpoint= AlpineHadoopWebAppLocator.getPigHadoopClient(coneection);
				break;
			}
			case hdfs:{
				endpoint= AlpineHadoopWebAppLocator.getHDFSHadoopClient(coneection);
				break;
			}
			case mapReduce:{
				endpoint= AlpineHadoopWebAppLocator.getMapReduceHadoopClient(coneection);
				break;
			}
			default:throw new IllegalStateException("Type doesn not match");
			
				
			
			}
			Client client = Client.create();
			WebResource webResource = client.resource(endpoint);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, jsonObject);

			if (response.getStatus() != 200) {
				String output = response.getEntity(String.class);
				System.out.println("Failed due to the exception of:" + output);
				throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
			}

			return response;

		} catch (Exception e) {

			e.printStackTrace();

		}
		return null;

	}
	
	public HadoopRestCallerManager(HadoopConnection connection) {
		this.connection = connection;

	}

	public HadoopRestCallerManager() {

	}

	@Override
	public boolean testConnection(HadoopConnection connection) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"testConnection", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		return response.getEntity(Boolean.class);
	}

	@Override
	public boolean isHadoopFile(String filePath, HadoopConnection connection) {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("filePath", gson.toJson(filePath));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"isHadoopFile", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);

		return gson.fromJson(resp, Boolean.class);
	}

	@Override
	public String readHadoopFileToStringBySize(String path,
			HadoopConnection connection, long limitSize) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("limitSize", gson.toJson(limitSize));
		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"readHadoopFileToStringBySize", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);

		return response.getEntity(String.class);
	}

	@Override
	public boolean copyFromLocal(String localFile, String targetDir,
			HadoopConnection connection) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("localFile", gson.toJson(localFile));
		params.put("targetDir", gson.toJson(targetDir));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"copyFromLocal", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		return response.getEntity(Boolean.class);
	}
	
	@Override
	public boolean readHadoopFileToOutputStream(String path,
			HadoopConnection connection, int from, int numberOfLines,
			OutputStream outputStream) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("from", gson.toJson(from));
		params.put("numberOfLines", gson.toJson(numberOfLines));
	

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"readHadoopFileToOutputStream", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);
		
		MultiPart multiPart = new MultiPart();  
	
        multiPart.bodyPart( new BodyPart(outputStream,MediaType.APPLICATION_OCTET_STREAM_TYPE));  
		multiPart.bodyPart(new BodyPart(jsonString,MediaType.TEXT_PLAIN_TYPE));
        Client c = Client.create();  
        WebResource r = c. resource(AlpineHadoopWebAppLocator.getHDFSHadoopClient(connection)+"/downloadFromHDFS");  
         
        String response = r.type(MediaType.MULTIPART_FORM_DATA).
        					accept(MediaType.TEXT_PLAIN).
        					post(String.class, multiPart);   
        
		if(null!=response&&response.equalsIgnoreCase("OK")){
			return true;
		}
	
		return false;

	}
	
	
	@Override
	public boolean writeStreamToFile(InputStream inputStream,String targetFilePath, HadoopConnection connection)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("targetFilePath", gson.toJson(targetFilePath));
		
		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"writeStreamToFile", params);
		
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);
		
		MultiPart multiPart = new MultiPart();  
        multiPart.bodyPart( new BodyPart(inputStream,MediaType.APPLICATION_OCTET_STREAM_TYPE));  
		multiPart.bodyPart(new BodyPart(jsonString,MediaType.TEXT_PLAIN_TYPE));
        Client c = Client.create();  
        WebResource r = c.resource(AlpineHadoopWebAppLocator.getHDFSHadoopClient(connection)+"/uploadToHDFS");  
         
        String response = r.type(MediaType.MULTIPART_FORM_DATA).
        					accept(MediaType.TEXT_PLAIN).
        					post(String.class, multiPart);   
        
		if(null!=response&&response.equalsIgnoreCase("OK")){
			return true;
		}
	
		return false;
	}

	@Override
	public boolean writeStringToFile(String line, String targetFilePath,
			HadoopConnection connection) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("line", gson.toJson(line));
		params.put("targetFilePath", gson.toJson(targetFilePath));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"writeStringToFile", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		return response.getEntity(Boolean.class);
	}

	@Override
	public String readHadoopPathToStringByLineNumber(String path,
			HadoopConnection connection, int lineNumber) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("path", gson.toJson(path));
		params.put("lineNumber", gson.toJson(lineNumber));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"readHadoopPathToStringByLineNumber", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		return response.getEntity(String.class);

	}

	@Override
	public InputStream readHadoopFileToInputStream(String path,
			HadoopConnection connection) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("path", gson.toJson(path));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"readHadoopFileToInputStream", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);

		return response.getEntity(InputStream.class);
	}

	@Override
	public List<HadoopFile> getHadoopFiles(String path,
			HadoopConnection connection, boolean isRecursive) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("path", gson.toJson(path));
		params.put("isRecursive", gson.toJson(isRecursive));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"getHadoopFiles", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);

		return gson.fromJson(resp, new TypeToken<List<HadoopFile>>() {}.getType());

	}

	@Override
	public List<HadoopFile> getHadoopFolders(String path,
			HadoopConnection connection, boolean isRecursive) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("path", path);
		Properties properties = new Properties();
		
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"getHadoopFolders", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, new TypeToken<List<HadoopFile>>() {}.getType());
	}

//	@Override
//	public String[] getHadoopFileOutput(String path, HadoopConnection connection)
//			throws Exception {
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("path",path);
//		Properties properties = new Properties();
//
//		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"getHadoopFileOutput", params);
//
//		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
//
//		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
//		String resp = response.getEntity(String.class);
//
//		return new Gson().fromJson(resp, String[].class);
//	}

	@Override
	public boolean deleteHadoopFile(String filePath, HadoopConnection connection) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("filePath",filePath);
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"deleteHadoopFile", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, Boolean.class);
	}

	@Override
	public boolean createHadoopFolder(String dirPath,
			HadoopConnection connection) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("dirPath",dirPath);
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"createHadoopFolder", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, Boolean.class);
	}

	@Override
	public HadoopFile getHadoopFile(String path, HadoopConnection connection)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("path",path);
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"getHadoopFile", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, HadoopFile.class);
	}

	@Override
	public boolean exists(String filePath, HadoopConnection connection) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("filePath",filePath);
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"exists", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, Boolean.class);
	}

	@Override
	public boolean createHadoopFile(String hadoopFileName,
			HadoopConnection hadoopConenction) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("hadoopFileName",hadoopFileName);
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"createHadoopFile", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, Boolean.class);
	}

	@Override
	public FileSystem getHadoopFileSystem(HadoopConnection hadoopConenction)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"getHadoopFileSystem", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		
		return new Gson().fromJson(resp, FileSystem.class);
	}

	@Override
	public Path getHadoopPath(HadoopConnection connection, String hadoopFileName)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("hadoopFileName", hadoopFileName);
		Properties properties = new Properties();
		
		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(connection, properties, new ArrayList<String>(), false,"getHadoopPath", params);
		
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer.toJson(jsonObj);
		
		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);
		return new Path(resp);
	}

	@Override
	public boolean isPathWritable(HadoopConnection hadoopConn, String path)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("path", gson.toJson(path));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"isPathWritable", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);

		return gson.fromJson(resp, Boolean.class);
	}

	@Override
	public long getTotalFileSize(String path, HadoopConnection connection, boolean isRecursive)
            //TODO DEAL WITH RECURSIVE
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("path", gson.toJson(path));
        params.put("isRecursive", gson.toJson(isRecursive));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"getTotalFileSize", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);

		return gson.fromJson(resp, Long.class);
	}

	@Override
	public boolean isLocalModelNeeded(String filePath,
			HadoopConnection connection) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		params.put("filePath", gson.toJson(filePath));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"isLocalModelNeeded", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
		String resp = response.getEntity(String.class);

		return gson.fromJson(resp, Boolean.class);
	}

//	@Override
//	public List<String> readHadoopLineFileToLineList(String fullPathFileName,
//			HadoopConnection hadoopConnection, int lineNumber)
//			throws IOException, Exception {
//		Map<String, String> params = new HashMap<String, String>();
//		Gson gson = new Gson();
//		params.put("fullPathFileName", gson.toJson(fullPathFileName));
//
//		Properties properties = new Properties();
//
//		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
//				connection, properties, new ArrayList<String>(), false,
//				"readHadoopLineFileToLineList", params);
//		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
//				.toJson(jsonObj);
//
//		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
//		String resp = response.getEntity(String.class);
//
//		return gson.fromJson(resp, new TypeToken<List<String>>() {}.getType());
//	}

//	@Override
//	public String readHadoopDirToString(String path,
//			HadoopConnection connection, boolean isRecursive) throws Exception {
//		Map<String, String> params = new HashMap<String, String>();
//		Gson gson = new Gson();
//		params.put("path", gson.toJson(path));
//
//		Properties properties = new Properties();
//
//		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
//				connection, properties, new ArrayList<String>(), false,
//				"readHadoopDirToString", params);
//		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
//				.toJson(jsonObj);
//
//		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
//		String resp = response.getEntity(String.class);
//
//		return gson.fromJson(resp, String.class);
//	}

	public HadoopConnection getConnection() {
		return connection;
	}

	public void setConnection(HadoopConnection connection) {
		this.connection = connection;
	}



    @Override
    public List<String> readHadoopPathToLineList(String fullPathFileName,
                                                HadoopConnection hadoopConnection, long lineNumber) throws IOException, Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        Gson gson = new Gson();
        params.put("fullPathFileName", gson.toJson(fullPathFileName));
        params.put("lineNumber", gson.toJson(lineNumber));

        Properties properties = new Properties();

        AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
                connection, properties, new ArrayList<String>(), false,
                "readHadoopPathToLineList", params);
        String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
                .toJson(jsonObj);

        ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
        String resp = response.getEntity(String.class);

        return gson.fromJson(resp, new TypeToken<List<String>>() {}.getType());


    }

    @Override
    public List<String> readHadoopPathToLineList4All(String path,
                                                HadoopConnection connection) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        Gson gson = new Gson();
        params.put("path", gson.toJson(path));

        Properties properties = new Properties();

        AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
                connection, properties, new ArrayList<String>(), false,
                "readHadoopPathToLineList4All", params);
        String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
                .toJson(jsonObj);

        ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
        String resp = response.getEntity(String.class);

        return gson.fromJson(resp, new TypeToken<List<String>>() {}.getType());


    }

    @Override
    public boolean isEmptyInput(String path, HadoopConnection connection)
            throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        Gson gson = new Gson();
        params.put("path", gson.toJson(path));

        Properties properties = new Properties();

        AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
                connection, properties, new ArrayList<String>(), false,
                "isEmptyInput", params);
        String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
                .toJson(jsonObj);

        ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
        String resp = response.getEntity(String.class);

        return gson.fromJson(resp, Boolean.class);
    }

	public List<String> getAllRealFilePaths(String path,
			HadoopConnection connection) {
		Map<String, String> params = new HashMap<String, String>();
		Gson gson = new Gson();
		 params.put("path", gson.toJson(path));

		Properties properties = new Properties();

		AlpineRestPigJsonObject jsonObj = new AlpineRestPigJsonObject(
				connection, properties, new ArrayList<String>(), false,
				"getAllRealFilePaths", params);
		String jsonString = AlpineRestPigJsonObjectSerializerDeserializer
				.toJson(jsonObj);

		ClientResponse response = makeARESTCallTo(connection, jsonString,RESTCallType.hdfs);
        String resp = response.getEntity(String.class);

        return gson.fromJson(resp, new TypeToken<List<String>>() {}.getType());
	}


}
