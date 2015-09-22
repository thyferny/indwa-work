/**
 * ClassName HadoopFowFilterAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 *
 *
 * @author Nihat Hosgur
 *
 **/
package com.alpine.datamining.api.impl.hadoop.explorer;

import static com.alpine.utility.pigserver.AlpinePigServerUtilitiy.registerPigQueries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.builtin.AlpinePigSummaryStatistics;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.ValueAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueAnalysisResult;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
/*
 * @author Nihat Hosgur
 */
public class HadoopValueAnalysisAnalyzer extends AbstractHadoopExplorerAnalyzer {

    private static Logger itsLogger = Logger
            .getLogger(HadoopValueAnalysisAnalyzer.class);

    private AlpinePigServer pigServer;
    private String 	  pureFileName;//pureFileName
    private String    loadedVariableName;
    private SortedSet<String>  columnNames;
    private Map<String,String> columnNameColumnTypeMap;
    private Locale    locale;
    private Map<String,ColumnValueAnalysisResult> columnValuesMap;

    private List<String> textColumns;
    private List<String> numberColumns;

    private HadoopValueAnalysisScriptGenerator generator;

    public HadoopValueAnalysisScriptGenerator getGenerator() {
        return generator;
    }
    public void setGenerator(HadoopValueAnalysisScriptGenerator generator) {
        this.generator = generator;
    }


    private Map<String, Integer> columOrdersForNumbersAlone;


    public HadoopValueAnalysisAnalyzer() {
    }
    /*
      *
      * (non-Javadoc)
      * @see com.alpine.datamining.api.DataAnalyzer#doAnalysis(com.alpine.datamining.api.AnalyticSource)
      */
    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException {
		if(getContext().isEmptyPigVariable(((HadoopAnalyticSource)source).getInputTempName())){
			throw new AnalysisException(EMPTY_INPUT_MSG);
		}
        try {
            if(itsLogger.isDebugEnabled()){
                itsLogger.debug("Started running");
            }
            init(source);

            ValueAnalysisResult valueAnalysisResult = fetchValueAnalysisResult();
            AnalyzerOutPutObject outPut = new AnalyzerOutPutObject(
                    valueAnalysisResult);

            outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo());
            if(getContext().isLocalModelPig() ==true){
                outPut.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,source.getAnalyticConfig().getLocale()));
            }
            return outPut;
        } catch (Exception e) {
            throw new AnalysisException(e);
        }
    }

    private ValueAnalysisResult fetchValueAnalysisResult() throws IOException,
            AnalysisException {
        ValueAnalysisResult valueAnalysisResult = new ValueAnalysisResult(getPureFileName());
        List<String> fullSecondRoundScript=new ArrayList<String>();
        List<ColumnValueAnalysisResult> result = fetchValueAnalysisData();
        if(null!=numberColumns&&0!=numberColumns.size()){
            List<ColumnValueAnalysisResult> numbrRows = filterOutTextColumns(result);
            if(numbrRows!=null&&numbrRows.size()!=0){
                List<String> deviationScript = generator.generateDeviationScript(numbrRows);
                fullSecondRoundScript.addAll(deviationScript);
                registerPigQueries(getPigServer(), deviationScript.toArray(new String[]{}));
                Tuple deviationTuple = acquireDeviationTuple();
                fetchDeviationValues(numbrRows,deviationTuple);
            }
        }

        itsLogger.info("ValueAnalysis script for SECOND round is:\n"+fullSecondRoundScript.toString());


        valueAnalysisResult.setValueAnalysisResult(result);
        return valueAnalysisResult;
    }

    private List<ColumnValueAnalysisResult> filterOutTextColumns(
            List<ColumnValueAnalysisResult> allColumns) {
        List<ColumnValueAnalysisResult>  nColumns = new ArrayList<ColumnValueAnalysisResult>();
        columOrdersForNumbersAlone= new HashMap<String,Integer>();
        int i=0;
        for(ColumnValueAnalysisResult row:allColumns){
			if(numberColumns.contains(row.getColumnName())&&row.getCount()>0){
                nColumns.add(row);
                columOrdersForNumbersAlone.put(row.getColumnName(), i++);
            }

        }
        return nColumns;
    }

    private void fetchDeviationValues(List<ColumnValueAnalysisResult> result, Tuple deviationTuple) throws ExecException {

        for(ColumnValueAnalysisResult row:result){
            Integer columnOrder = columOrdersForNumbersAlone.get(row.getColumnName());
            row.setDeviation(Math.sqrt(DataType.toDouble(deviationTuple.get(columnOrder))));
        }



    }
    /** Description of private void init(AnalyticSource source) throws AnalysisException
     *
     * @param source: Source of required parameters
     * @throws AnalysisException in case there are some unexpected or wrong input values
     *
     */
    private void init(AnalyticSource source) throws AnalysisException {
        columnValuesMap = new HashMap<String,ColumnValueAnalysisResult>();

        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
        //hadoopSource.getHadoopFileStructureModel().
        String errString;
        //Setting purefilename
        String pureFileName =hadoopSource.getFileName();
        if(null==pureFileName||"".equals(pureFileName.trim())){
            errString="File name["+pureFileName+"] is either empty or null";
            throw new IllegalArgumentException(errString);
        }
        setPureFileName(pureFileName);
        //Setting pig server
        setPigServer(acquirePigServerInfo(hadoopSource));
        //Setting the column names
        ValueAnalysisConfig config = (ValueAnalysisConfig) source
                .getAnalyticConfig();
        String cNames = config.getColumnNames();
        if(null==cNames||"".equals(cNames.trim())){
            errString="Column names["+cNames+"] are either empty or null";
            throw new IllegalArgumentException(errString);
        }
        setColumnNames(new TreeSet<String>(Arrays.asList(cNames.split(","))));
        fetchColumnValuePairs(hadoopSource.getHadoopFileStructureModel());
        //Setting locale
        Locale loc=hadoopSource.getAnalyticConfig().getLocale();
        if(null==loc){
            itsLogger.error("Could not acquire Locale yet will try to continue");
        }
        setLocale(hadoopSource.getAnalyticConfig().getLocale());
        //Setting pigData source variable name
        String pigDataSourceName = hadoopSource.getInputTempName();
        if(null==pigDataSourceName||"".equals(pigDataSourceName.trim())){
            errString="PigDataSource name["+pigDataSourceName+"] is either empty or null";
            throw new IllegalArgumentException(errString);
        }
        setLoadedVariableName(pigDataSourceName);
        initNumberAndTextColumns();
    }

    private void initNumberAndTextColumns() {

        textColumns = new ArrayList<String>();
        numberColumns = new ArrayList<String>();

        for(String columnName :columnNames){
            if (isText(columnNameColumnTypeMap.get(columnName))) {
                textColumns.add(columnName);
            } else {
                numberColumns.add(columnName);
            }
        }

        generator = new HadoopValueAnalysisScriptGenerator(getLoadedVariableName(),columnNames);
    }
    /** Description of private void fetchColumnValuePairs(AnalysisHadoopFileStructureModel hfsm)
     *
     * @param  hfsm: Sourece where we get the column types and names
     *
     */

    private void fetchColumnValuePairs(AnalysisFileStructureModel hfsm) {
        if(null==hfsm||null==hfsm.getColumnNameList()||hfsm.getColumnNameList().isEmpty()||
                null==hfsm.getColumnTypeList()||hfsm.getColumnTypeList().isEmpty()||hfsm.getColumnTypeList().size()!=hfsm.getColumnNameList().size()){
            String errString="AnalysisHadoopFileStructureModel nor column names nor getColumnTypeList can not be null nor have diffrent sizes";
            throw new IllegalArgumentException(errString);
        }

        List<String> hColumnNames = hfsm.getColumnNameList();
        List<String> hColumnValues = hfsm.getColumnTypeList();

        generateTheColumnNameColumnTypeMap(hColumnNames, hColumnValues);

    }

    public void generateTheColumnNameColumnTypeMap(List<String> hColumnNames,List<String> hColumnValues) {
        if(null==columnNameColumnTypeMap){
            columnNameColumnTypeMap = new HashMap<String,String>();
        }
        for(int i=0;i<hColumnNames.size();i++){
            if(columnNameColumnTypeMap.containsKey(hColumnNames.get(i))){
                itsLogger.error("Have duplicate values for the column of["+hColumnValues.get(i)+"]however will ignore this and continue as it is");
                continue;
            }
            columnNameColumnTypeMap.put(hColumnNames.get(i),hColumnValues.get(i));
        }
    }

    /** Description of public String[] generatePigScript()
     * Generates the pig script . Make sure to have init is called or required fields are already set
     *
     * @return String[]: Pig script that is generated base on column and table name etc to fetch the valueanalysis values
     *
     */
    public String[] generatePigScript() {
        SortedSet<String> columnNames = getColumnNames();
        if(null==columnNames||columnNames.isEmpty()){
            throw new IllegalStateException("Got empty column names can not generate the script");
        }



        List<String> pigScript = new ArrayList<String>();

        pigScript.addAll(generator.genPigScriptForNumbers());
        //pigScript.addAll(generator.genPigScriptForText());

        return pigScript.toArray(new String[]{});
    }
    private boolean isText(String type) {
        if(type.toLowerCase().trim().equals("chararray")){
            return true;
        }
        return false;
    }
    /** Description of public List<ColumnValueAnalysisResult> fetchValueAnalysisData() throws IOException, AnalysisException
     * This method runs the pig script on designated hadoop instead
     * Before method is caleld either init must have been already called or requried fileds are set.
     *
     * @return List<ColumnValueAnalysisResult>: Values of the selected columns...
     *
     */
    public List<ColumnValueAnalysisResult> fetchValueAnalysisData() throws IOException, AnalysisException {
        String[] pigScript = generatePigScript();
        if(itsLogger.isDebugEnabled()){itsLogger.debug("ValueAnalysis script for first round is:\n"+Arrays.toString(pigScript));}

        registerPigQueries(getPigServer(),pigScript);
        List<ColumnValueAnalysisResult> results = new ArrayList<ColumnValueAnalysisResult>();
        if(null==columnValuesMap){
            columnValuesMap= new HashMap<String,ColumnValueAnalysisResult>();
        }


        Tuple itForNumber = acquireIterator(generator.getAggregatorVariableNameForNumber());

        for(String column:columnNames){
            ColumnValueAnalysisResult analy = fetcTheAggreagedValuesForTheColumnOf(itForNumber,column);
            columnValuesMap.put(column, analy);
            results.add(analy);

        }

        return results;
    }

    private Tuple acquireIterator(String iteratorName) throws AnalysisException, IOException {
        try{
            Iterator<Tuple> it = pigServer.openIterator(iteratorName);

            if (!it.hasNext()) {
                throw new AnalysisException(
                        "Failed to fetch any aggregated iterator");
            }

            Tuple next = it.next();
            next = (Tuple) next.get(0);
            return next;
        }catch(Exception e){
            throw new AnalysisException("Got the exception of["+e.getMessage()+"]",e);
        }
    }

    private Tuple acquireDeviationTuple() throws AnalysisException, IOException {

        Iterator<Tuple> it = pigServer.openIterator(generator.getDeviationVariableName());

        if (!it.hasNext()) {
            throw new AnalysisException(
                    "Failed to fetch any aggregated iterator");
        }

        Tuple next = (Tuple)it.next().get(0);

        if (next.size() != numberColumns.size()) {
            String stringErrorMsg = "Received the tuple with some missing or extra columns.. Tuple has["
					+ next.size() + "]columns where we were expecting  " +numberColumns.size() +
					". This is probably due to there are some columns only contains null value.";
            itsLogger.error(stringErrorMsg);
            throw new AnalysisException(stringErrorMsg);
        }

        return next;
    }

    private ColumnValueAnalysisResult fetcTheAggreagedValuesForTheColumnOf(Tuple itForNumber, String columnName) throws AnalysisException {
        ColumnValueAnalysisResult result = new ColumnValueAnalysisResult();

        result.setColumnName(columnName);
        try {

            result.setColumnName(columnName);
            result.setColumnType(columnNameColumnTypeMap.get(columnName));
            if(isText(result.getColumnType())){
                fetchStringValue(itForNumber,columnName, result);
            }else{
                fetchNumericalValue(itForNumber,columnName, result);
            }

        }catch(Exception e){
            throw new AnalysisException("Failed to fetch the aggregated result from pig server" +
                    " for the column of["+columnName+"]",e);
        }
        return result;
    }
    private void fetchNumericalValue(Tuple next,String columnName,
                                     ColumnValueAnalysisResult result) throws IOException,
            AnalysisException, ExecException {
        Integer columnOrder = AlpinePigSummaryStatistics.analyzingColumns.length*generator.getColumnOrder(columnName);
		//columnOrder = 0;

        setValuesForTheColumn(result, next, columnOrder);

    }
    //	 "StarCount", 0
//	 "Count", 1
//	 "Null", 2
//	 "Zero", 3
//	 "Positive", 4
//	 "Negative", 5
//	 "Sum", 6
//	 "Distinct", 7
//	 "Max", 8
//	 "Min" 9
    private void fetchStringValue(Tuple next, String columnName,
                                  ColumnValueAnalysisResult result) throws IOException,
            AnalysisException, ExecException {
		Integer order = AlpinePigSummaryStatistics.analyzingColumns.length*generator.getColumnOrder(columnName);
		//order = 0;

		result.setMaxNA(true);
		result.setQ1NA(true);
		result.setMedianNA(true);
		result.setQ3NA(true);
		result.setMinNA(true);
		result.setAvgNA(true);
		result.setDeviationNA(true);
		result.setZeroCountNA(true);
		result.setPositiveValueCountNA(true);
		result.setNegativeValueCountNA(true);

		result.setMinNA(true);

		result.setCount(null==next.get(order+1)?0:DataType.toLong(next.get(order+1)));
		result.setUniqueValueCount(null==next.get(order+7)?0:DataType.toLong(next.get(order+7)));
		result.setNullCount(null==next.get(order+2)?0:DataType.toLong(next.get(order+2)));
		result.setEmptyCount(null==next.get(order+2)?0:DataType.toLong(next.get(order+2)));
		if(-1==result.getUniqueValueCount()){
			result.setUniqueValueCountNA(true);
		}

		result.setTop01Value(null == next.get(order + 14) ? 0 : (next.get(order + 14)));
		result.setTop01Count(null == next.get(order + 15) ? 0 : DataType.toLong(next.get(order + 15)));
		result.setTop02Value(null == next.get(order + 16) ? 0 : (next.get(order + 16)));
		result.setTop02Count(null == next.get(order + 17) ? 0 : DataType.toLong(next.get(order + 17)));
		result.setTop03Value(null == next.get(order + 18) ? 0 : (next.get(order + 18)));
		result.setTop03Count(null == next.get(order + 19) ? 0 : DataType.toLong(next.get(order + 19)));
		result.setTop04Value(null == next.get(order + 20) ? 0 : (next.get(order + 20)));
		result.setTop04Count(null == next.get(order + 21) ? 0 : DataType.toLong(next.get(order + 21)));
		result.setTop05Value(null == next.get(order + 22) ? 0 : (next.get(order + 22)));
		result.setTop05Count(null == next.get(order + 23) ? 0 : DataType.toLong(next.get(order + 23)));
		result.setTop06Value(null == next.get(order + 24) ? 0 : (next.get(order + 24)));
		result.setTop06Count(null == next.get(order + 25) ? 0 : DataType.toLong(next.get(order + 25)));
		result.setTop07Value(null == next.get(order + 26) ? 0 : (next.get(order + 26)));
		result.setTop07Count(null == next.get(order + 27) ? 0 : DataType.toLong(next.get(order + 27)));
		result.setTop08Value(null == next.get(order + 28) ? 0 : (next.get(order + 28)));
		result.setTop08Count(null == next.get(order + 29) ? 0 : DataType.toLong(next.get(order + 29)));
		result.setTop09Value(null == next.get(order + 30) ? 0 : (next.get(order + 30)));
		result.setTop09Count(null == next.get(order + 31) ? 0 : DataType.toLong(next.get(order + 31)));
		result.setTop10Value(null == next.get(order + 32) ? 0 : (next.get(order + 32)));
		result.setTop10Count(null == next.get(order + 33) ? 0 : DataType.toLong(next.get(order + 33)));


	}



    /*
 //			 "StarCount", 0
 //			 "Count", 1
 //			 "Null", 2
 //			 "Zero", 3
 //			 "Positive", 4
 //			 "Negative", 5
 //			 "Sum", 6
 //			 "Distinct", 7
 //			 "Max", 8
 //			 "Min" 9
      *
     }

      *
      */

    private void setValuesForTheColumn(ColumnValueAnalysisResult result,
                                       Tuple next, Integer columnOrder) throws ExecException {
/*
		"StarCount",	//0
				"Count",		//1
				"Null",			//2
				"Zero",			//3
				"Positive",		//4
				"Negative",		//5
				"Sum",			//6
				"Distinct",		//7
				"Min",			//8
				"25%",			//9
				"50%",			//10
				"75%",			//11
				"Max",			//12
				"Mean"			// 13
		*/
        result.setNullCount(null==next.get(columnOrder+2)?0:DataType.toLong(next.get(columnOrder+2)));
        result.setCount(null==next.get(columnOrder+1)?0:DataType.toLong(next.get(columnOrder+1)));
        result.setMax(null==next.get(columnOrder+12)?0:DataType.toDouble(next.get(columnOrder+12)));
        result.setMin(null==next.get(columnOrder+8)?0:DataType.toDouble(next.get(columnOrder+8)));
        result.setPositiveValueCount(null==next.get(columnOrder+4)?0:DataType.toLong(next.get(columnOrder+4)));
        result.setNegativeValueCount(null==next.get(columnOrder+5)?0:DataType.toLong(next.get(columnOrder+5)));
        result.setZeroCount(null==next.get(columnOrder+3)?0:DataType.toLong(next.get(columnOrder+3)));
        result.setUniqueValueCount(null==next.get(columnOrder+7)?0:DataType.toLong(next.get(columnOrder+7)));
		result.setQ1(null == next.get(columnOrder + 9) ? 0 : DataType.toDouble(next.get(columnOrder + 9)));
		result.setMedian(null == next.get(columnOrder + 10) ? 0 : DataType.toDouble(next.get(columnOrder + 10)));
		result.setQ3(null == next.get(columnOrder + 11) ? 0 : DataType.toDouble(next.get(columnOrder + 11)));
		result.setAvg((null==next.get(columnOrder+6)?0:DataType.toDouble(next.get(columnOrder+6)))/
                (null==next.get(columnOrder+1)?0:DataType.toDouble(next.get(columnOrder+1))));

		result.setTop01Value(null == next.get(columnOrder + 14) ? 0 : (next.get(columnOrder + 14)));
		result.setTop01Count(null == next.get(columnOrder + 15) ? 0 : DataType.toLong(next.get(columnOrder + 15)));
		result.setTop02Value(null == next.get(columnOrder + 16) ? 0 : (next.get(columnOrder + 16)));
		result.setTop02Count(null == next.get(columnOrder + 17) ? 0 : DataType.toLong(next.get(columnOrder + 17)));
		result.setTop03Value(null == next.get(columnOrder + 18) ? 0 : (next.get(columnOrder + 18)));
		result.setTop03Count(null == next.get(columnOrder + 19) ? 0 : DataType.toLong(next.get(columnOrder + 19)));
		result.setTop04Value(null == next.get(columnOrder + 20) ? 0 : (next.get(columnOrder + 20)));
		result.setTop04Count(null == next.get(columnOrder + 21) ? 0 : DataType.toLong(next.get(columnOrder + 21)));
		result.setTop05Value(null == next.get(columnOrder + 22) ? 0 : (next.get(columnOrder + 22)));
		result.setTop05Count(null == next.get(columnOrder + 23) ? 0 : DataType.toLong(next.get(columnOrder + 23)));
		result.setTop06Value(null == next.get(columnOrder + 24) ? 0 : (next.get(columnOrder + 24)));
		result.setTop06Count(null == next.get(columnOrder + 25) ? 0 : DataType.toLong(next.get(columnOrder + 25)));
		result.setTop07Value(null == next.get(columnOrder + 26) ? 0 : (next.get(columnOrder + 26)));
		result.setTop07Count(null == next.get(columnOrder + 27) ? 0 : DataType.toLong(next.get(columnOrder + 27)));
		result.setTop08Value(null == next.get(columnOrder + 28) ? 0 : (next.get(columnOrder + 28)));
		result.setTop08Count(null == next.get(columnOrder + 29) ? 0 : DataType.toLong(next.get(columnOrder + 29)));
		result.setTop09Value(null == next.get(columnOrder + 30) ? 0 : (next.get(columnOrder + 30)));
		result.setTop09Count(null == next.get(columnOrder + 31) ? 0 : DataType.toLong(next.get(columnOrder + 31)));
		result.setTop10Value(null == next.get(columnOrder + 32) ? 0 : (next.get(columnOrder + 32)));
		result.setTop10Count(null == next.get(columnOrder + 33) ? 0 : DataType.toLong(next.get(columnOrder + 33)));

		if(-1==result.getUniqueValueCount()){
            result.setUniqueValueCountNA(true);
        }

    }

    private AnalyticNodeMetaInfo createNodeMetaInfo() {
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.VALUE_ANALYSIS_NAME, getLocale()));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.VALUE_ANALYSIS_DESCRIPTION, getLocale()));

        return nodeMetaInfo;
    }




    public AlpinePigServer getPigServer() {
        return pigServer;
    }

    public void setPigServer(AlpinePigServer pServer) {
        this.pigServer = pServer;
    }

    public String getPureFileName() {
        return pureFileName;
    }

    public void setPureFileName(String pureFileName) {
        this.pureFileName = pureFileName;
    }

    public String getLoadedVariableName() {
        return loadedVariableName;
    }

    public void setLoadedVariableName(String loadedVariableName) {
        this.loadedVariableName = loadedVariableName;
    }

    public SortedSet<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(SortedSet<String> columnNames) {
        this.columnNames = columnNames;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }


    public Map<String, String> getColumnNameColumnTypeMap() {
        return columnNameColumnTypeMap;
    }


    public void setColumnNameColumnTypeMap(
            Map<String, String> columnNameColumnTypeMap) {
        this.columnNameColumnTypeMap = columnNameColumnTypeMap;
    }



}