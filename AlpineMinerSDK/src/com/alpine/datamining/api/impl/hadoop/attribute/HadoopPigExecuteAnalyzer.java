/**
 * ClassName HadoopPigExecuteAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-9
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPigExecuteConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.PigInputMapItem;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.hadoop.pig.AlpinePigServer;

/**
 * @author Jeff Dong
 *
 */
public class HadoopPigExecuteAnalyzer extends AbstractHadoopAttributeAnalyzer {

    private static final Logger itsLogger = Logger.getLogger(HadoopPigExecuteAnalyzer.class);


    public static final String SPECIAL_STR = "alpine"+System.currentTimeMillis();

    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException {
        init((HadoopAnalyticSource)source);

        try {
            if(config.getOverride().equals(Resources.YesOpt)){
                boolean success = HadoopHDFSFileManager.INSTANCE.deleteHadoopFile(resultLocaltion+resultsName, hadoopConnection);
                if(success==false){
                    throw new Exception("Can not delete out put directory "+resultLocaltion+resultsName);
                }
            };

            AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
            String pigScript = generateScript4Pig(config  );

            itsLogger.error(pigScript);

            runPigScript(pigServer, pigScript);

            loadFileIntoPig(hadoopConnection,config);

            readIterator(pigServer);

        } catch (Exception e) {
            itsLogger.error(e.getMessage(),e);
            throw new AnalysisException(e.getLocalizedMessage());
        }

        HadoopMultiAnalyticFileOutPut output = generateHadoopOutput();

        output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));

        return output;
    }

    protected void readIterator(AlpinePigServer pigServer) throws IOException,
            ExecException {
		//never call the open iterator itself since it will cause performance issue
		String tempFileName= "tempStoreName"+System.currentTimeMillis();

		String pigScript = tempFileName +" = LIMIT " +getOutputTempName()+" "+ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT) +" ;";
		pigServer.registerQuery(pigScript) ;
		Iterator<Tuple> iter = pigServer.openIterator(tempFileName);

        List<String> lineList = readDataFromIterator(iter);

        addContentForTheFileOf(resultLocaltion+resultsName, lineList.toArray(new String[lineList.size()]));

        fileOutputNames.put(resultLocaltion+resultsName, new String[]{""});
    }

    private void loadFileIntoPig(HadoopConnection hadoopConnection,
                                 HadoopDataOperationConfig config) throws Exception{
        String fileName = resultLocaltion+resultsName;

        boolean fileExists = HadoopHDFSFileManager.INSTANCE.exists(
                fileName, hadoopConnection);

        if( fileExists == false){
            throw new AnalysisException("Result File:"+fileName+" does not existed!");
        }

        String hostName = hadoopConnection.getHdfsHostName();
        AnalysisFileStructureModel fileStructureModel =  (AnalysisFileStructureModel)((HadoopPigExecuteConfig)config).getHadoopFileStructure();


        String pureFileName=  getOutputTempName();
        AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);


        String fileURI = "hdfs://" + hostName + ":"
                + hadoopConnection.getHdfsPort() + fileName;

        String script = pureFileName + " = load '" + fileURI + "' USING "
                + getPigStorageScript(hadoopConnection, fileStructureModel, fileName) ;


        if (itsLogger.isDebugEnabled()) {
            itsLogger.debug(script);
        }

        pigServer.registerQuery(script);

    }

    public String generateScript4Pig(HadoopDataOperationConfig config) {
        String script = ((HadoopPigExecuteConfig)config).getPigScriptModel().getPigScript();
        script=script.replaceAll("';'", SPECIAL_STR);
        String[] scriptsArray = script.split(";");
        StringBuffer newScriptSb=new StringBuffer();
        for(String s:scriptsArray){
            if(StringUtil.isEmpty(s)==false){
                s=s.replaceAll(SPECIAL_STR,"';'");
                if(s.startsWith("\n")){
                    s=s.substring(1, s.length());
                }
                newScriptSb.append(s).append(";\n");
            }
        }
        String newScript = newScriptSb.toString();
        AnalysisPigExecutableModel scriptModel = ((HadoopPigExecuteConfig)config).getPigScriptModel();
        if(scriptModel!=null){
            List<PigInputMapItem> items = scriptModel.getPigInputMapItems();
            for(PigInputMapItem item :items){
                if(item.getPigAliasName()!=null){
                    newScript=newScript.replaceAll(item.getPigAliasName(),AbstractHadoopAnalyzer.OUT_PREFIX + item.getInputUUID());
                }

            }
        }

        return newScript;
    }

    @Override
    protected AnalysisFileStructureModel getOutPutStructure() {
        AnalysisFileStructureModel newModel = ((HadoopPigExecuteConfig)config).getHadoopFileStructure();
        return newModel;
    }

    @Override
    protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_NAME,locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.HD_ROWFILTER_DESCRIPTION,locale));
        return nodeMetaInfo;
    }

}
