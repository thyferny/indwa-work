package com.alpine.utility.db;

import org.apache.log4j.Logger;

public class NZJdbcDriver extends AbstractJdbcDriver{

    private static final Logger itsLogger = Logger.getLogger(NZJdbcDriver.class);

    private static NZJdbcDriver instance = null;

	private NZJdbcDriver(String dec,String driverName) throws Exception {
		super(dec,driverName);
	}

	public static NZJdbcDriver getInstance() {
		if (instance == null) {
			try {
				instance = new NZJdbcDriver(DataSourceInfoNZ.getJdbcFullFilePath(),DataSourceInfoNZ.dBDriver);
			} catch (Exception e) {
				instance = null;
				itsLogger.error(e.getMessage(),e);
			}
		}
		return instance;

	}
}
