/**
 * ClassName LoggingHandler.java
 *
 * Version information:1.00
 *
 * Data:2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.utility.log;

public interface LoggingHandler {
    /** Logs a status message with the correct log service. */
    public void log(String message);

    /** Logs a Info message with the correct log service. */
    public void logInfo(String message);
    
    /** Logs a warning message with the correct log service. */
    public void logWarning(String message);
    
    /** Logs an error message with the correct log service. */
    public void logError(String message);
    
    /** Logs a debugging message */
    public void logDebug(String message);
}
