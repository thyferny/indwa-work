/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * UpdateInfoReader.java
 */
package com.alpine.miner.impls.systemupdate;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Gary
 * Dec 12, 2012
 */
public class SystemUpdateService {

	private static final SystemUpdateService INSTANCE = new SystemUpdateService();
	
	private SystemUpdateService(){}
	
	public static SystemUpdateService getInstance(){
		return INSTANCE;
	}
	
	public BuildInformation readBuildInformation() throws Exception{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("action", "getBuildInfo");
		Socket client = null;
		try {
			client = new Socket();
			client.connect(new InetSocketAddress("localhost", getSocketPort()), 10000);
			ObjectOutputStream printer = new ObjectOutputStream(client.getOutputStream());
			printer.writeObject(parameters);
			ObjectInputStream returnStream = new ObjectInputStream(client.getInputStream());
			final Map<String, Object> returnVal = (Map<String, Object>) returnStream.readObject();
			if(returnVal instanceof Exception){
				throw (Exception)returnVal;
			}
			if(returnVal == null){
				return null;
			}
			return new BuildInformation() {
				@Override
				public String getVersion() {
					return (String) returnVal.get("version");
				}
				@Override
				public Date getReleaseDate() {
					return (Date) returnVal.get("releaseDate");
				}
				@Override
				public String getDownloadURI() {
					return null;
				}
				@Override
				public String getDescription() {
					return (String) returnVal.get("description");
				}
				@Override
				public String getApplicationName() {
					return (String) returnVal.get("applicationName");
				}
			};
		} catch (Exception e) {
			throw e;
		}finally{
			client.close();
		}
	}
	
	public void deploy(String version, String deployPath, String contextName) throws Exception{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("version", version);
		parameters.put("deployPath", deployPath);
		parameters.put("contextName", contextName);
		parameters.put("action", "install");
		Socket client = null;
		try {
			client = new Socket();
			client.connect(new InetSocketAddress("localhost", getSocketPort()), 10000);
			ObjectOutputStream printer = new ObjectOutputStream(client.getOutputStream());
			printer.writeObject(parameters);
			ObjectInputStream returnStream = new ObjectInputStream(client.getInputStream());
//			Object returnVal = returnStream.readObject();
//			if(returnVal instanceof Exception){
//				throw (Exception)returnVal;
//			}
		}catch (Exception e) {
			throw e;
		}finally{
			client.close();
		}
	}
	
	private int getSocketPort(){
		int port = 1357;
		InputStream is = SystemUpdateService.class.getResourceAsStream("systemUpdateClientConfig.properties");
		Properties props = new Properties();
		try {
			props.load(is);
			port = Integer.valueOf(props.getProperty("installSocketPort"));
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				//ignore
			}
		}
		return port;
	}
}
