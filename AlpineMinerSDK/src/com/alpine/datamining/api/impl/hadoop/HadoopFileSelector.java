/**
 * ClassName  DBTableSelector.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.pig.PigServer;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.pig.AlpinePigServer;


/**
 * @author John Zhao
 *
 */
public class HadoopFileSelector extends AbstractHadoopAnalyzer {

    /* (non-Javadoc)
      * @see com.alpine.datamining.api.DataAnalyzer#doAnalysis(com.alpine.datamining.api.AnalyticSource)
      */
    private static Logger itsLogger = Logger.getLogger(HadoopFileSelector.class);
    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException  {

        HadoopAnalyticFileOutPut output=new HadoopAnalyticFileOutPut();
        HadoopFileSelectorConfig config=(HadoopFileSelectorConfig)source.getAnalyticConfig();
        String fileName = config.getHadoopFileName();
        output.setResultDir(fileName);

        loadFileToPigServer((HadoopFileSelectorConfig)source.getAnalyticConfig(), getOutputTempName());

        AnalyticNodeMetaInfo nodeMetaInfo=createNodeMetaInfo(output,config.getLocale());

        output.setAnalyticNodeMetaInfo(nodeMetaInfo);

        return output;
    }




    /**
     * @param source
     * @return
     * @throws AnalysisException
     */
    public HadoopFileSelectorConfig loadFileToPigServer(HadoopFileSelectorConfig config, String pureFileName)
            throws AnalysisException {
        String fileName = config.getHadoopFileName();
        String hostName = config.getHdfsHostname();
        AnalysisFileStructureModel fileStructureModel = (AnalysisFileStructureModel) config.getHadoopFileStructure();


        HadoopConnection hadoopConnection = new HadoopConnection(
                config.getConnName(),
                config.getUserName(),
                config.getGroupName(),
                config.getHdfsHostname(),
                Integer.parseInt(config.getHdfsPort()),
                config.getHadoopVersion(),
                config.getJobHostname(),
                Integer.parseInt(config.getJobPort()),
                config.getSecurityMode(),
                config.getHdfsPrincipal(),
                config.getHdfsKeyTab(),
                config.getMapredPrincipal(),
                config.getMapredKeyTab());
        try {
            boolean isEmptyInput=HadoopHDFSFileManager.INSTANCE.isEmptyInput(fileName, hadoopConnection);
            if (isEmptyInput==true) {
                throw new AnalysisException("The input from the preceding operator was empty. Please check the input file or the filter condition.");
            }
            AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
            fileName=fileName.replace("'", "\\'") ;
            String fileURI="hdfs://"+hostName+":"+config.getHdfsPort()+fileName;
            //   this is for the filter of the header

            String	script = pureFileName+" = load '"+fileURI+ "' USING " + super.getPigStorageScript(hadoopConnection, fileStructureModel,fileName);



            if(itsLogger.isDebugEnabled()){
                itsLogger.debug(script);
            }
            pigServer.registerQuery(script);
        } catch (Exception e) {
            itsLogger.error(e.getMessage(),e);
            throw new AnalysisException(e.getLocalizedMessage());
        }
        return config;
    }

    private AnalyticNodeMetaInfo createNodeMetaInfo(
            HadoopAnalyticFileOutPut output, Locale locale) {

        AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.HD_FILE_NAME,locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.HD_FILE_NAME_DESCRIPTION,locale));

        Properties props=new Properties();

        //filename filepath
        props.setProperty(SDKLanguagePack.getMessage(SDKLanguagePack.HD_FILE_PATH,locale), output.getResultDir());

        nodeMetaInfo.setProperties(props);

        return nodeMetaInfo;
    }

}
