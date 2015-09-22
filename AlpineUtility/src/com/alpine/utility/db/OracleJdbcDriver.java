package com.alpine.utility.db;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class OracleJdbcDriver {

    private static final Logger itsLogger = Logger.getLogger(OracleJdbcDriver.class);
    private Driver driver;

	private static OracleJdbcDriver instance = null;

	private OracleJdbcDriver() throws Exception {
		this.init();

	}

	public static OracleJdbcDriver getInstance() {
		if (instance == null) {
			try {
				instance = new OracleJdbcDriver();
			} catch (Exception e) {
				instance = null;
				itsLogger.error(e.getMessage(),e);
			}
		}
		return instance;

	}

	public Driver getDriver() {
		return driver;
	}

	private void init() throws Exception {
		String dec = DataSourceInfoOracle.getJdbcFullFilePath();

		File decFile = new File(dec);

		URL jdbcDriverURL;
		jdbcDriverURL = decFile.toURL();
		itsLogger.info("jdbcDriverURL=" + jdbcDriverURL);
		URL[] urls = new URL[1];
		urls[0] = jdbcDriverURL;
		URLClassLoader urlclassLoader = new URLClassLoader(urls, ClassLoader
				.getSystemClassLoader());
		driver = (Driver) urlclassLoader.loadClass(
				DataSourceInfoOracle.dBDriver).newInstance();
		DriverManager.registerDriver(driver);

	}
}
