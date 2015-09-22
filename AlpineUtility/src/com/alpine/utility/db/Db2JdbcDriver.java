package com.alpine.utility.db;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class Db2JdbcDriver {

    private static final Logger itsLogger = Logger.getLogger(Db2JdbcDriver.class);
    private Driver driver;

	private static Db2JdbcDriver instance = null;

	private Db2JdbcDriver() throws Exception {
		this.init();

	}

	public static Db2JdbcDriver getInstance() {
		if (instance == null) {
			try {
				instance = new Db2JdbcDriver();
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
		String dec = DataSourceInfoDB2.getJdbcFullFilePath();

		File decFile = new File(dec);

		URL jdbcDriverURL;

		jdbcDriverURL = decFile.toURL();
		itsLogger.info("jdbcDriverURL=" + jdbcDriverURL);
		URL[] urls = new URL[1];
		urls[0] = jdbcDriverURL;
		URLClassLoader urlclassLoader = new URLClassLoader(urls, ClassLoader
				.getSystemClassLoader());
		driver = (Driver) urlclassLoader.loadClass(DataSourceInfoDB2.dBDriver)
				.newInstance();
		DriverManager.registerDriver(driver);

	}
}
