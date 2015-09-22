package com.alpine.utility.pigserver;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.utility.hadoop.pig.AlpinePigServer;

public class AlpinePigServerUtilitiy {
    private static final Logger itsLogger = Logger.getLogger(AlpinePigServerUtilitiy.class);
    /** Description of public void registerPigQueries(String... pigScript) throws IOException
     * Before method is called either init must have been already called or required fields are set.
     *
     * @argument String... pigScript: Lines of the script that we will register to pigserver
     * @throws ExecException
     * @throws AnalysisException
     *
     */
    public static void registerPigQueries(AlpinePigServer pigServer,String... pigScript) throws IOException {
        if(null==pigServer||null==pigScript||0==pigScript.length){
            String err="Neither Pigserver nor Pig Script can be null is null or pigScript to be empty";
            itsLogger.debug(err);
            throw new IllegalArgumentException(err);
        }
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("The script that we will register is:"+Arrays.toString(pigScript));
        }

        for(String pigScriptLine:pigScript){
            String[] lines = pigScriptLine.split("\n");
            for(String line:lines){
                pigServer.registerQuery(line);
                if(itsLogger.isDebugEnabled()){
                    itsLogger.debug("Registering the query of:"+line);
                }
            }
        }
    }

}
