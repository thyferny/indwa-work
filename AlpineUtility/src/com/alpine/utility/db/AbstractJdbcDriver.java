package com.alpine.utility.db;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public abstract class AbstractJdbcDriver {
    private static final Logger itsLogger = Logger.getLogger(AbstractJdbcDriver.class);
    protected Driver driver;

	protected AbstractJdbcDriver(String dec,String driverName) throws Exception {
		this.init(dec,driverName);
	}

	public Driver getDriver() {
		return driver;
	}

	protected void init(String dec,String driverName) throws Exception {
		File decFile = new File(dec);

		URL jdbcDriverURL;

		jdbcDriverURL = decFile.toURL();
		itsLogger.info("jdbcDriverURL=" + jdbcDriverURL);
		URL[] urls = new URL[1];
		urls[0] = jdbcDriverURL;
		URLClassLoader urlclassLoader = new URLClassLoader(urls, ClassLoader
				.getSystemClassLoader());
		driver = (Driver) urlclassLoader.loadClass(driverName)
				.newInstance();
		DriverManager.registerDriver(driver);

	}
}
