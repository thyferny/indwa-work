/**
 * ClassName AbstractHadoopAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-25
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import org.apache.log4j.Logger;
import org.apache.pig.PigServer;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.utility.hadoop.pig.AlpinePigServer;

public abstract class AbstractHadoopExplorerAnalyzer extends AbstractHadoopAnalyzer{

    protected static Logger logger= Logger.getLogger(AbstractHadoopExplorerAnalyzer.class);


//	private void init(AnalyticSource source) throws AnalysisException {
//		
//		HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
//		
//		String errString;
//		//Setting purefilename
//		String pureFileName =hadoopSource.getFileName();
//		if(null==pureFileName||"".equals(pureFileName.trim())){
//			errString="File name["+pureFileName+"] is either empty or null";
//			throw new IllegalArgumentException(errString);
//		}
//		setPureFileName(pureFileName);
//		//Setting pig server
//		setPigServer(acquirePigServerInfo(hadoopSource));
//		//Setting locale
//		Locale loc=hadoopSource.getAnalyticConfig().getLocale();
//		if(null==loc){
//			logger.logError("Could not acquire Locale yet will try to continue");
//		}
//		setLocale(hadoopSource.getAnalyticConfig().getLocale());
//		//Setting pigData source variable name
//		String pigDataSourceName = AlpineUtil.getPureHadoopFileName(getPureFileName());
//		if(null==pigDataSourceName||"".equals(pigDataSourceName.trim())){
//			errString="PigDataSource name["+pigDataSourceName+"] is either empty or null";
//			throw new IllegalArgumentException(errString);
//		}
//		setLoadedVariableName(pigDataSourceName);
//	}

    protected AlpinePigServer acquirePigServerInfo(HadoopAnalyticSource hadoopSource)
            throws AnalysisException {
        String errString;
        try {
            AlpinePigServer pServer = getContext().getPigServer(
                    hadoopSource.getHadoopInfo());
            if (null == pServer) {
                errString = "Could not acquire Pig Server ";
                throw new AnalysisException(errString);
            }
            return pServer;
        } catch (Exception e) {
            errString = "Could not acquire Pig Server ";
            throw new AnalysisException(errString, e);
        }
    }

}
