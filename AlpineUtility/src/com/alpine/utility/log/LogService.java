/**
 * ClassName LogService.java
 *
 * Version information:1.00
 *
 * Data:2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.utility.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.LogManager;

import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

/**
 * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
 */
@Deprecated
public class LogService implements LoggingHandler {
 
	public static final String PROFILE = "/log4j.properties";
	private static String configFilename;
	private static String prefix="log4j.appender.";
	private static String root="log4j.rootLogger";
	private static String AppenderName="A";
	private static String Appender=prefix+AppenderName;
	private static String ApachePrefix="org.apache.log4j.";
	private static String[] AppenderType={"ConsoleAppender","FileAppender","DailyRollingFileAppender","RollingFileAppender","WriterAppender"};
	
	private Logger logger; 
	private Properties pro;
	private static LogService instance=null;
   
	/** Initial Setting:
	 * Get properties from local log4j.properties
	 * Set time zone and time format*/
	private   void init()
    {
    	pro=new Properties();

    	try {
    		InputStream in;
    	 
    			in=this.getClass().getResourceAsStream(PROFILE);
    			configFilename=PROFILE;
    	 
			pro.load(in);
			in.close();
    	}
		 catch (IOException e) 
		 	{
				BasicConfigurator.configure();
				e.printStackTrace();
			}
    }
	
	static{
		instance=new LogService();
	}
	/**
	 * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
	 */
	@Deprecated
	public static LogService getInstance(){
		if(instance==null){
			throw new IllegalStateException("Logger should have been already created..");
		}
		return instance;
		
	}
	/** Default Constructor
	 * All the setting from log4j.properties*/
    private LogService()
    {
    	this.init();
    	PropertyConfigurator.configure(pro);
    	PropertyConfigurator.configureAndWatch(configFilename);
		logger = Logger.getLogger(LogService.class.getName());

    }

    
    /** Set the output mode
     * 0:Console
     * 1:File
     * 2:DailyRollingFile
     * 3:RollingFil
     * 4:Writer
     * */
    public void setOutputMode(OutputMode outputmode)
    {
    	switch (outputmode.id)
    	{
    	case 1:
    	pro.put(Appender, ApachePrefix+AppenderType[0]);
    	break;
    	case 2:
    	pro.put(Appender, ApachePrefix+AppenderType[1]);
    	return;
    	case 3:
        pro.put(Appender, ApachePrefix+AppenderType[2]);
        break;
    	case 4:
        pro.put(Appender, ApachePrefix+AppenderType[3]);
        break;
    	case 5:
        pro.put(Appender, ApachePrefix+AppenderType[4]);
        break;
    	}
    	PropertyConfigurator.configure(pro);  
    }
    
    /** Get current output mode */
    public OutputMode getOutputMode()
    {
    	
    	String[] temp=pro.getProperty(Appender).split("\\.");
    	String appender=temp[temp.length-1].trim();
    	if(appender.equals(AppenderType[0]))
    	{
    		return OutputMode.CONSOLE;
    	}
    	else if(appender.equals(AppenderType[1]))
    	{
    		return OutputMode.FILE;
    	}
    	else if(appender.equals(AppenderType[2]))
    	{
    		return OutputMode.DailyRollingFile;
    	}
    	else if(appender.equals(AppenderType[3]))
    	{
    		return OutputMode.RollingFile;
    	}
    	else return OutputMode.Writer;
    }
	
    /**When the output mode is File
     * set the file destination 
     * append:true means that by default a FileAppender append to an existing file
     		  false means truncate it.
     */
	public void setFileAppenderDestination(String Path,boolean append)
    {
		if (this.getOutputMode().id==2)
		{
		pro.put(Appender+".File", Path);
		if(append)
    		pro.put(Appender+".Append", "true");
    	else
    		pro.put(Appender+".Append", "false");
    	PropertyConfigurator.configure(pro);   
		}
		else
			this.logError("Current Appender is not FileAppender,Please change it to FileAppender");
    }
   
	/** Overload setFileAppenderDestination()
	 * Just need the path.Append use the default value*/
	public void setFileAppenderDestination(String Path)
	{
		this.setFileAppenderDestination(Path, true);
	}

	
	/** Set log level 
	 * ALL
	 * DEBUG
	 * INFO
	 * WARN
	 * ERROR
	 * FATAL
	 * OFF
	 * */
    public void setLogLevel(Level level)
    {
        Logger.getLogger(this.getClass()).warn("SETTING LOG LEVEL TO " + level);
        int i=level.toInt()/10000;
    	if(i>10) 
    		pro.put(root, "off,"+AppenderName);
    	else if(i<0) 
    		pro.put(root, "all,"+AppenderName);
    	else
    	{
    	switch(i)
	    	{
	    	case 1:pro.put(root, "debug,"+AppenderName); break;
	    	case 2:pro.put(root, "info,"+AppenderName); break;
	    	case 3:pro.put(root, "warn,"+AppenderName);break;
	    	case 4:pro.put(root, "error,"+AppenderName); break;
	    	case 5:pro.put(root, "fatal,"+AppenderName); break;
	    	}
    	}
    	PropertyConfigurator.configure(pro); 

    }
    /**Get current log level */
    public Level getCurrentLevel()
    {
    	String[] temp=pro.getProperty(root).split(",");
    	return Level.toLevel(temp[0]);

    }
    
    
    /** If the output mode is File
     * Recommend use two methods following */
    public void setOutputToFile(String path,boolean append)
    {
    	this.setOutputMode(OutputMode.FILE);
    	this.setFileAppenderDestination(path,append);
    }
    public void setOutputToFile(String path)
    {
    	this.setOutputToFile(path, true);
    }

    
	/** Default log level */
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
    public void log(String message) {
   	 if (logger.isInfoEnabled()) {   
  	    logger.debug(message);   
  	} 
   	 }

    /** Logs a note message with the correct log service. */
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
    public void logInfo(String message) {
    	 if (logger.isInfoEnabled()) {   
     	    logger.info(message);   
     	} 
    }
    
    /** Logs a warning message with the correct log service. */
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
    public void logWarning(String message) 
    {
    	logger.warn(message);
    }
    
    /** Logs an error message with the correct log service. */
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
    public void logError(String message)  
    {
    	    logger.error(message);   
    }
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
    public void logDebug( String message){
     if (logger.isDebugEnabled()) {   
    	    logger.debug(message);   
    	    
    	} 
    }
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
    public boolean  isDebugEnabled() {
    	return logger.isDebugEnabled();
    }
    
    
    public void entry(String className, String method,String parameter){
        if (logger.isDebugEnabled()) {   
       	    logger.debug("Entering method:"+className+"'s "+method+" with parameter "+parameter);   
       	    
       	} 
    }
    public void exit( String className,String method,String returnValue){
        if (logger.isDebugEnabled()) {   
       	    logger.debug("Exiting method:"+className+"'s "+method+" with returnValue "+returnValue);   
       	    
       	} 
    }
    /**
     * @deprecated  Please use log4j instead {@link #http://logging.apache.org/ }
     */
    @Deprecated
	public void logError(Throwable e) { 
		ByteArrayOutputStream outSteam=new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(outSteam )); 
		byte[]bytes=outSteam.toByteArray();
		logError(new String (bytes));
		
	}
	
	public void setLogLevel() {
       Logger.getLogger(this.getClass()).warn("SETTING LOG LEVEL BY READING PROFILE UTILITY");
		if (ProfileReader.getInstance().getParameter(ProfileUtility.SYS_LOG).equalsIgnoreCase(ProfileUtility.D_SYS_LOG)){
			LogService.getInstance().setLogLevel(Level.INFO);
            LogManager.getRootLogger().setLevel(Level.INFO);
		} else {
			LogService.getInstance().setLogLevel(Level.DEBUG);
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        }
	}
    
}