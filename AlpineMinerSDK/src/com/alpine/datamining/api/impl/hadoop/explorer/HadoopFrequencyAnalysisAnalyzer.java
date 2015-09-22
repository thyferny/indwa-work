/**
 * ClassName HadoopFrequencyAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.FrequencyAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.api.impl.hadoop.utility.PigValueTypesUtility;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.attributeanalysisresult.FrequencyAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueFrequencyAnalysisResult;
import com.alpine.utility.file.AlpineMapUtility;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.pigserver.AlpinePigServerUtilitiy;

public class HadoopFrequencyAnalysisAnalyzer extends
        AbstractHadoopExplorerAnalyzer {
    private static Logger itsLogger = Logger
            .getLogger(HadoopFrequencyAnalysisAnalyzer.class);

    public HadoopFrequencyAnalysisAnalyzer() {
    }

    /***
     * data = load 'hdfs://192.168.1.235/home/account_nohead.csv' using
     * PigStorage (',') as (c1:int,c2:int,c3:int); c1group = group data by (c3);
     * b = FOREACH c1group generate group,COUNT(data.c1);
     */

    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException {

        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
	 	if(getContext().isEmptyPigVariable(hadoopSource.getInputTempName())){
			throw new AnalysisException(EMPTY_INPUT_MSG);
		}
        String monitorinString="";
        try {
            AlpinePigServer pigServer = getContext().getPigServer(
                    hadoopSource.getHadoopInfo());
            if (null == pigServer) {
                throw new IllegalStateException(
                        "PigServer is not available for the hadoop connection of["
                                + hadoopSource.getHadoopInfo() + "]");
            }

            String hdfsFileTableName = hadoopSource.getInputTempName();
            // columnaNameGroup=group hdfsFileTableName by columnaName;\n
            // freqresult = FOREACH columnaNameGroup generate
            // group,SIZE(hdfsFileTableName);

            AnalysisFileStructureModel hdfsm = hadoopSource
                    .getHadoopFileStructureModel();
            FrequencyAnalysisResult frequencyAnalysisResult = new FrequencyAnalysisResult("");

            FrequencyAnalysisConfig config = (FrequencyAnalysisConfig) source
                    .getAnalyticConfig();

            String columnNames = config.getColumnNames();
            String[] names = columnNames.split(",");



            String frequencyMapVarName = registerAlpineFreqAndGetFreqVariableName(hdfsFileTableName,names,pigServer);
            monitorinString="Opening["+frequencyMapVarName+"]";
			//this openIterator is ok since it will get all the data
            Iterator<Tuple> iter = pigServer.openIterator(frequencyMapVarName);
            Tuple tuple = iter.next();
            tuple =(Tuple)tuple.get(0);

            String mapString = DataType.toString(tuple.get(0));
            Map<Integer, Map<String, Integer>> initMap = AlpineMapUtility.desiralizeFrequencyMap(mapString);

            List<String> fullColumnTypes = hadoopSource.getHadoopFileStructureModel().getColumnTypeList();
            List<String> fullColumnNameList=hadoopSource.getHadoopFileStructureModel().getColumnNameList();
            String[] columnTypes=PigValueTypesUtility.acquireColumnTypes(fullColumnNameList,fullColumnTypes,names);

            Map<Integer, List<String>> sortedFreq = PigValueTypesUtility.sortFrequencyMapByColumnTypeString(initMap, columnTypes);


            int i=0;
            for (String columnaName : names) {
                List<String> keys = sortedFreq.get(i);
                Map<String,Integer> colCount=initMap.get(i++);

                String delimiter = HadoopUtility.getDelimiterValue(hdfsm);

                if (delimiter == null) {
                    delimiter = ",";
                }
                int index = 0;
                List<ValueFrequencyAnalysisResult> nameResult = new ArrayList<ValueFrequencyAnalysisResult>();

                for (String value : keys) {
                    ValueFrequencyAnalysisResult valueFrequencyAnalysisResult = new ValueFrequencyAnalysisResult();
                    valueFrequencyAnalysisResult.setColumnName(columnaName);

                    frequencyAnalysisResult
							.addValueFrequencyAnalysisResult(valueFrequencyAnalysisResult);

                    nameResult.add(valueFrequencyAnalysisResult);

                    if(value==null){
                        valueFrequencyAnalysisResult.setColumnValue("null value");

                    }

                    else if ( value.length()==0 ) {
                        if(HadoopDataType.isNumberType( columnTypes[index])){
                            valueFrequencyAnalysisResult.setColumnValue("null value");
                        }else{
                            valueFrequencyAnalysisResult.setColumnValue("\"" + value + "\"");

                        }
                    } else {
                        valueFrequencyAnalysisResult.setColumnValue("\"" + value + "\"");

                    }

                    valueFrequencyAnalysisResult.setCount(colCount.get(value));



                }

//				if(nameResult.size()==10&&
//				   nameResult.get(0).getColumnValue().equals(AlpineMapUtility.MAXED_OUT_MARKER)){
//					nameResult.get(0).setColumnValueNA(true);
//					nameResult.get(0).setAllNA(true);
//					nameResult.get(0).setColumnValueNA(true);
//					nameResult.get(0).setCountNA(true);
//					nameResult.get(0).setColumnValue("N/A");
//				}else{
                index = index + 1;

                countPercentage(nameResult);

//				}

            }



            AnalyzerOutPutObject outPut = new AnalyzerOutPutObject(
                    frequencyAnalysisResult);
            outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(source
                    .getAnalyticConfig().getLocale()));
            if(getContext().isLocalModelPig() ==true){
                outPut.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
            }
            return outPut;

        } catch (Exception e) {

            String errString = "Could not execute the pig script of[" + monitorinString
                    + "due to the exception of";
            itsLogger.error(errString, e);
            throw new AnalysisException(errString+ e.getMessage()
                    + "]", e);
        }

        // 2 read the file into a AnalyzerOutPutTableObject and return in ,
        // please refere to TableAnalysisAnalyzer

    }

    private String registerAlpineFreqAndGetFreqVariableName(String hdfsFileTableName, String[] sortedColumnNames,AlpinePigServer pigServer) throws IOException {

        String pr="pr"+hdfsFileTableName;
        String gr= "gr"+hdfsFileTableName;
        String freVarName="fr"+hdfsFileTableName;

        StringBuilder sb= new StringBuilder(pr);


        sb.append(" = FOREACH "+hdfsFileTableName+" GENERATE ");

        for(String cn:sortedColumnNames){
            sb.append(cn).append(",");
        }
        String projected=sb.toString();

        projected=sb.substring(0,projected.length()-1);
        projected+=";";

        String grS= gr+" = GROUP "+pr+"  ALL;";
        String alFre=freVarName+" = FOREACH "+gr+" GENERATE AlpinePiggyFrequency("+pr+");";

        AlpinePigServerUtilitiy.registerPigQueries(pigServer, new String[]{projected,grS,alFre});

        return freVarName;
    }

    private void countPercentage(List<ValueFrequencyAnalysisResult> nameResult) {
        float totalCount = 0f;
        for (ValueFrequencyAnalysisResult res : nameResult) {
            totalCount = totalCount + res.getCount();
        }
        if(totalCount!=0f){
            for (ValueFrequencyAnalysisResult res : nameResult) {
                res.setPercentage(res.getCount() / totalCount);
            }
        }else{
            for (ValueFrequencyAnalysisResult res : nameResult) {
                res.setPercentage(0);
            }
        }
    }

    private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.FREQUENCY_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.FREQUENCY_DESCRIPTION, locale));

        return nodeMetaInfo;
    }

}