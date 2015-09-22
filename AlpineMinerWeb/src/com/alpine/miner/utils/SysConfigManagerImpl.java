package com.alpine.miner.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.utility.file.FileUtility;
import com.alpine.utility.hadoop.fs.HadoopRestCallerManager;

import org.apache.log4j.Logger;

public class SysConfigManagerImpl implements SysConfigManager {
    private static final String REST_ENABLED = "REST_ENABLED";
	private static Logger itsLogger = Logger.getLogger(SysConfigManagerImpl.class);
    String serverEncoding = Default_Server_Encoding;
	// default keep 1 days

	long liveTime = DEFAULT_LIVE_TIME;
	// temp_file_scan_frequency
	long scanFrequency = DEFAUlT_SCAN_FREQUENCY;
	
	private String sampleFolder;//used to sync Sample flow to personal folder.

	public long getLiveTime() {
		return liveTime;
	}

	public void setLiveTime(long liveTime) {
		this.liveTime = liveTime;
	}

	public long getScanFrequency() {
		return scanFrequency;
	}

	public void setScanFrequency(long scanFrequency) {
		this.scanFrequency = scanFrequency;
	}


	public SysConfigManagerImpl() {

	}

	public String getServerEncoding() {
		return serverEncoding;
	}

	public void setServerEncoding(String serverEncoding) {
		this.serverEncoding = serverEncoding;
	}

	public void init() {
		String configFile = FilePersistence.Preference_PREFIX
				+ configFileName;
		FileInputStream stream = null;
		try {
			File file = new File(configFile);

			if (file.exists() == false) {
				// create a new one ...
				FileUtility.writeFile(configFile, KEY_LIVETIME
						+ " = " + liveTime + "\n"
						+ KEY_SCAN_FREQUENCY + " = "
						+ scanFrequency);

			}

			stream = new FileInputStream(file);
			Properties props = new Properties();
			props.load(stream);
			if (props.get(KEY_LIVETIME) != null) {
				liveTime = Long.valueOf((String) props
						.get(KEY_LIVETIME));
			}
			if (props.get(KEY_SCAN_FREQUENCY) != null) {
				scanFrequency = Long.valueOf((String) props
						.get(KEY_SCAN_FREQUENCY));
			}
			if (props.get(KEY_SERVER_ENCODING) != null) {
				liveTime = Long.valueOf((String) props
						.get(KEY_SERVER_ENCODING));
			}
			if(props.get(KEY_SAMPLE_FOLDER) != null){
				sampleFolder = props.getProperty(KEY_SAMPLE_FOLDER);
			}
			if(props.get(REST_ENABLED) != null){
				HadoopRestCallerManager.setRestFlag(false,Boolean.parseBoolean(props.getProperty(REST_ENABLED)));
			}
			
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					itsLogger.error(e.getMessage(),e);
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.utils.SysConfigManager#getSampleFolder()
	 */
	@Override
	public String getSampleFolder() {
		return sampleFolder;
	}
}
