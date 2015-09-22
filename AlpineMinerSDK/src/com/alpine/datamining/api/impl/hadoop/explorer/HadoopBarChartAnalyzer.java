/**
 * ClassName HadoopBarChartnalyzer.java
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.BarChartAnalysisConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.pigserver.AlpinePigServerUtilitiy;
import com.alpine.utility.tools.StringHandler;

public class HadoopBarChartAnalyzer extends AbstractHadoopExplorerAnalyzer {
    private static Logger itsLogger = Logger
            .getLogger(HadoopBarChartAnalyzer.class);

    private String[] fileOutputNames;

    private String valueDomain;
    private String series;
    private String categoryType;
    private List<DataRow> dataRows;

    private boolean containScopeAndCategory;

    public HadoopBarChartAnalyzer() {
    }

    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException {
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Started running");
        }
        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
        if(getContext().isEmptyPigVariable(hadoopSource.getInputTempName())){
            throw new AnalysisException(EMPTY_INPUT_MSG);
        }
        AlpinePigServer pigServer =  null;
        String[] pigScript = null;
        try {
            pigServer =  getContext().getPigServer(hadoopSource.getHadoopInfo());

            if (null == pigServer) {
                throw new IllegalStateException(
                        "PigServer is not available for the hadoop connection of["
                                + hadoopSource.getHadoopInfo() + "]");
            }

            String hdfsFileTableName = hadoopSource.getInputTempName(); //AlpineUtil.getPureHadoopFileName(fileName);

            pigScript = generatePigScript(source, hdfsFileTableName);
            AlpinePigServerUtilitiy.registerPigQueries(pigServer, pigScript);
            fetchBarChartData(pigServer, hadoopSource.getHadoopInfo());

            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("Got the result of[outputFileSampleContents.toString()] for the pigScript of:"+ pigScript);
            }

        } catch (Exception e) {
            String errString = "Could not execute the pig script of[" + Arrays.toString(pigScript)
                    + "due to the exception of"+e.getMessage();
            itsLogger.error(errString, e);
            throw new AnalysisException(errString, e);
        }

        // 2 read the file into a AnalyzerOutPutTableObject and return in ,
        // please refere to TableAnalysisAnalyzer
        return buildPresentationData(source);
    }



    private AnalyticOutPut buildPresentationData(AnalyticSource source) {
        DataTable dataTable = new DataTable();
        fillDataTableMetaInfo(source, dataTable);

        List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
        TableColumnMetaInfo dc1 = new TableColumnMetaInfo(
                StringHandler.doubleQ(valueDomain), "");
        TableColumnMetaInfo dc2 = new TableColumnMetaInfo(
                StringHandler.doubleQ(series), "");

        columns.add(dc1);
        columns.add(dc2);
        if(containScopeAndCategory){
            TableColumnMetaInfo dc3 = new TableColumnMetaInfo(
                    StringHandler.doubleQ(categoryType), "");
            columns.add(dc3);
        }

        if (itsLogger.isDebugEnabled()) {
            itsLogger.debug("BarChartIS:" + dataRows.toString());
        }
        dataTable.setColumns(columns);
        dataTable.setRows(dataRows);
        AnalyzerOutPutTableObject tableOutput = new AnalyzerOutPutTableObject();
        tableOutput.setDataTable(dataTable);
        tableOutput.setAnalyticNodeMetaInfo(createNodeMetaInfo(source));
        if(getContext().isLocalModelPig() ==true){
            tableOutput.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,source.getAnalyticConfig().getLocale()));
        }
        return tableOutput;

    }

    private AnalyticNodeMetaInfo createNodeMetaInfo(AnalyticSource source) {
        Locale locale= source.getAnalyticConfig().getLocale();
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.BAR_CHART_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.BAR_CHART_DESCRIPTION, locale));

        return nodeMetaInfo;
    }

    protected void fillDataTableMetaInfo(AnalyticSource source,
                                         DataTable dataTable) {
        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
        dataTable.setSchemaName(hadoopSource.getHadoopInfo().getConnName());
        dataTable.setTableName(hadoopSource.getFileName());
    }

    protected String[] fetchBarChartData(AlpinePigServer pigServer,
                                         HadoopConnection hadoopConnection) throws IOException, Exception {
        fileOutputNames = null;
        dataRows = new ArrayList<DataRow>();
		//this openIterator is ok since it will get all the data
        Iterator<Tuple> it = pigServer.openIterator("barGraphData");

        int i = 0;
        while (it.hasNext()) {
            Tuple next = it.next();

            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("Received Tuple is:"
                        + extractTupleColumns(next).toString());
            }

            int numberOfColumnsInTuple = 0;
            int expectSize = 3;

            if(containScopeAndCategory==false){
                expectSize = 2;
            }

            if (next.size() != expectSize) {
                numberOfColumnsInTuple = next.size();
                String stringErrorMsg = "Received the tuple with some missing or extra columns.. Tuple has["
                        + numberOfColumnsInTuple + "]columns";
                itsLogger.error(stringErrorMsg);
                if(itsLogger.isDebugEnabled()){
                    itsLogger.debug("There is the case that one or more column(s) is missing");
                }
                continue;
            }
            String[] data = null;
            if(containScopeAndCategory){
                if(null==DataType.toString(next.get(0))||
                        null==DataType.toString(next.get(1))||
                        null==DataType.toString(next.get(2))){
                    if(itsLogger.isDebugEnabled()){
                        itsLogger.debug("There are some columns that are null so we will ignore the row");
                    }
                    continue;
                }
                data = new String[] { DataType.toString(next.get(2)),
                        DataType.toString(next.get(1)),
                        DataType.toString(next.get(0))};
            }else{
                if(null==DataType.toString(next.get(0))||
                        null==DataType.toString(next.get(1))){
                    if(itsLogger.isDebugEnabled()){
                        itsLogger.debug("There are some columns that are null so we will ignore the row");
                    }
                    continue;
                }
                data = new String[] {DataType.toString(next.get(1)),
                        DataType.toString(next.get(0))};
            }

            DataRow dr = new DataRow();
            dr.setData(data);
            dr.setSequence(i++);

            dataRows.add(dr);

        }
        return fileOutputNames;
    }

    public static StringBuilder extractTupleColumns(Tuple next) throws ExecException {
        if(null==next){
            return new StringBuilder();
        }

        int numberOfColumnsInTuple = next.size();
        StringBuilder sb=new StringBuilder("{");
        for(int j=0;j<numberOfColumnsInTuple;j++){
            sb.append("[").append(next.get(j)).append("]");
        }
        sb.append("}");

        return sb;
    }

    public String[] generatePigScript(AnalyticSource source, String pigReferenceToTheDataSource) {
        if(null==source||null==pigReferenceToTheDataSource||"".equals(pigReferenceToTheDataSource.trim())){
            String errString="Neither source["+source+"]nor barData["+pigReferenceToTheDataSource+"]can be null or empty";
            itsLogger.error(errString);
            throw new IllegalArgumentException(errString);
        }
        BarChartAnalysisConfig config = (BarChartAnalysisConfig) source
                .getAnalyticConfig();

        valueDomain = config.getValueDomain();
        series = config.getScopeDomain();
        categoryType = config.getCategoryType();

        containScopeAndCategory = false;

        if(StringUtil.isEmpty(config.getCategoryType())==false
                &&StringUtil.isEmpty(config.getScopeDomain())==false){
            containScopeAndCategory = true;
        }else{
            if(StringUtil.isEmpty(config.getCategoryType())==false){
                series = categoryType;
            }
        }

        String[] scripts=new String[2];
        StringBuilder sb = new StringBuilder();
        if(containScopeAndCategory){
            sb.append("barGroup = GROUP ").append(pigReferenceToTheDataSource)
                    .append(" BY(").append(categoryType).append(",")
                    .append(series).append(");");
            scripts[0] = sb.toString();
            sb = new StringBuilder();
            sb.append("barGraphData = FOREACH barGroup GENERATE group.")
                    .append(categoryType).append(",group.").append(series)
                    .append(", SUM(").append(pigReferenceToTheDataSource)
                    .append(".").append(valueDomain).append(");");
        }else{
            sb.append("barGroup = GROUP ").append(pigReferenceToTheDataSource)
                    .append(" BY(").append(series).append(");");
            scripts[0] = sb.toString();
            sb = new StringBuilder();
            sb.append("barGraphData = FOREACH barGroup GENERATE group");
            sb.append(", SUM(").append(pigReferenceToTheDataSource)
                    .append(".").append(valueDomain).append(");");
        }

        if (itsLogger.isDebugEnabled()) {
            itsLogger.debug("valueDomain[" + valueDomain + "]series[" + series
                    + "]categoryType[" + categoryType + "]");
            itsLogger.debug("Registered the bart chart script of["
                    + sb.toString() + "] to pig server");
        }
        scripts[1]=sb.toString();
        return scripts;
    }

}
