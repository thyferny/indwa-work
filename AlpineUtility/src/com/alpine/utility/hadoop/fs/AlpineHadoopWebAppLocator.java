package com.alpine.utility.hadoop.fs;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopConstants;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AlpineHadoopWebAppLocator {

	private static final Map<String,String> hadoopClientServers;
	static{
		hadoopClientServers=new HashMap<String,String>(); 
		//TODO bind in build time later...
		hadoopClientServers.put(HadoopConstants.VERSION_APACHE_HADOOP_0_20_2, "http://localhost:8080/AlpineHadoopAgentApache0202/");
//		 hadoopClientServers.put(HadoopConstants.VERSION_0_20_2_CDH3_U4 ,"http://localhost:8080/AlpineHadoopClientCloudera0.20.2-cdh3u4-2.8.1/");
//		hadoopClientServers.put(HadoopConstants.VERSION_APACHE_0_20_203		, "http://localhost:7777/AlpineHadoopClientApache0.20.2-2.8.1/");
//		hadoopClientServers.put(HadoopConstants.VERSION_APACHE_1_0_2		, "http://localhost:7777/AlpineHadoopClientApache0.20.2-2.8.1/");
//		hadoopClientServers.put(HadoopConstants.VERSION_CDH_4_1_1			, "http://localhost:7777/AlpineHadoopClientApache0.20.2-2.8.1/");
//		hadoopClientServers.put(HadoopConstants.VERSION_GREENPLUM_1_1		, "http://localhost:7777/AlpineHadoopClientApache0.20.2-2.8.1/");
		
	}
 	
	
	public static String getHDFSHadoopClient(HadoopConnection connection){
	 
		return hadoopClientServers.get(connection.getVersion())+"rest/pig/hdfsManager";
	}
	public static String getPigHadoopClient(HadoopConnection connection){
	 
		return hadoopClientServers.get(connection.getVersion())+"rest/pig/execPigJob";
	}
	public static String getMapReduceHadoopClient(HadoopConnection connection){
	 
		return hadoopClientServers.get(connection.getVersion())+"mapred/hadoopJob/post";
	}
	
	
	public static String makeARESTCallTo(HadoopConnection connection,String jsonObject){
		try {
			String endpoint=AlpineHadoopWebAppLocator.getHDFSHadoopClient(connection);
			
			Client client = Client.create();
			WebResource webResource = client.resource(endpoint);
			
			ClientResponse response = webResource.post(ClientResponse.class, jsonObject);

			if (response.getStatus() != 200) {
				String output = response.getEntity(String.class);
				System.out.println("Failed due to the exception of:"+output);
				throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
			}
			
			System.out.println("Output from Server .... \n");
			
			Type listType = new TypeToken<List<String>>(){}.getType();
			String output = response.getEntity(String.class);
			return output;
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		throw new RuntimeException("Rest call failed");
		
	}
	
	

}
