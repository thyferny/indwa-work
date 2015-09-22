/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ConfigReader.java
 */
package com.alpine.miner.security.eula;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Gary
 * Dec 24, 2012
 */
public class EULAConfig {
	
	private static final  Logger LOG = Logger.getLogger(EULAConfig.class);
	
	private static final String NAME_ACCEPT_STATUS = "ACCEPT_STATUS";
	
	private static EULAConfig instance = new EULAConfig();

	private boolean acceptStatus;
	
	private EULAConfig(){
		Properties props;
		try {
			props = readProps();
			this.acceptStatus = Boolean.valueOf(props.getProperty(NAME_ACCEPT_STATUS));
		} catch (IOException e) {
			LOG.error(e);
			this.acceptStatus = false;
		}
	}
	
	private Properties readProps() throws IOException{
		Properties props = new Properties();
		InputStream is = EULAConfig.class.getResourceAsStream("config.properties");
		try{
			props.load(is);
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				//ignore
			}
		}
		return props;
	}
	
	private void storeProps(){
		;
		Properties props = new Properties();
		props.setProperty(NAME_ACCEPT_STATUS, Boolean.toString(this.acceptStatus));
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(EULAConfig.class.getResource("config.properties").toURI().getPath()), 1024);
			props.store(out, null);
		} catch (Exception e) {
			LOG.error(e);
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
	
	public boolean getAcceptStatus() {
		return acceptStatus;
	}

	public void setAcceptStatus(boolean acceptStatus) {
		this.acceptStatus = acceptStatus;
		storeProps();
	}

	public static EULAConfig getInstance(){
		return instance;
	}
}
