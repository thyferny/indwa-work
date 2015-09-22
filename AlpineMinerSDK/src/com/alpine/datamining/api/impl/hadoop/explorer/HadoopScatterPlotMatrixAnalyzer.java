/**
 * ClassName HadoopScatterPlotMatrixAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-7-24
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 *
 *
 * @author Jeff Dong
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
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableScatterMatrixConfig;
import com.alpine.datamining.api.impl.db.table.ScatterMatrixColumnPairs;
import com.alpine.datamining.api.impl.db.table.ScatterMatrixInstanceCorrelation;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatterMatrix;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class HadoopScatterPlotMatrixAnalyzer extends
        AbstractHadoopExplorerAnalyzer {

    private static final String COMMA = ",";
    private static Logger itsLogger = Logger
            .getLogger(HadoopScatterPlotMatrixAnalyzer.class);
    private static final String SUM_OF = "sumOf";
    private static final String SUM = "sum";

    private static final String XXX = "XXX";
    private AlpinePigServer pigServer;
    private String loadedVariableName;
    private Locale locale;
    private String[] columnNames;
    private Map<String, Integer> scatterLocation;
    private Map<String, Integer> countLocation;

    private List<ScatterMatrixInstanceCorrelation> scatterColumnsList;
    private Map<ScatterMatrixColumnPairs, DataTable> dataTableMap;

    private static final String[] RAW_PIG_SCRIPT;
    private static final String PIG_SCATTER_PLOT_MATRIX = "PIG_SCATTER_PLOT_MATRIX";

    private static final String PIG_VARIABLE = "pigVariable";

    private Double max_rows;

    private Tuple correlationTuple;
    private Iterator<Tuple> tupleIteratorForSampling;

    private SortedMap<String, String> alias;

    private Map<String, Map<String, String>> mapOfCalcSigles;
    private Map<String, Integer> aliasIndexNumMap;
    private HashMap<String, Integer> locationsInTheReturnTuple;
    private List<String> pigScript;
    private String samplingScript;

    static {
        RAW_PIG_SCRIPT = HadoopExplorerPigTemplateReader.fetchAlpinePigTemplate(PIG_SCATTER_PLOT_MATRIX);
    }

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
            calculateCorrelationAndScatterPlotInfo();
            return buildResultFromPigResult(source.getAnalyticConfig());

        } catch (IOException e) {
            itsLogger.error("Could not complete analysis on scatter plot matrix due to the exception of",e);
            throw new AnalysisException(e);
        }
    }


    /** Description of  init(AnalyticSource source) throws AnalysisException
     *
     * @param source: Source of required parameters
     * @throws AnalysisException in case there are some unexpected or wrong input values
     *
     */
    private void init(AnalyticSource source) throws AnalysisException {
        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
        // hadoopSource.getHadoopFileStructureModel().
        String errString;
        initTheListAndTheMap();

        // Setting pig server
        setPigServer(acquirePigServerInfo(hadoopSource));
        // Setting the column names
        TableScatterMatrixConfig config = (TableScatterMatrixConfig) source
                .getAnalyticConfig();
        String cNames = config.getColumnNames();
        if (null == cNames || "".equals(cNames.trim())) {
            errString = "Column names[" + cNames + "] are either empty or null";
            throw new IllegalArgumentException(errString);
        }
        setColumnNames(cNames.split(COMMA));

        // Setting locale
        Locale loc = hadoopSource.getAnalyticConfig().getLocale();
        if (null == loc) {
            itsLogger.error("Could not acquire Locale yet will try to continue");
        }
        setLocale(hadoopSource.getAnalyticConfig().getLocale());
        // Setting pigData source variable name
        String pigDataSourceName = hadoopSource.getInputTempName();
        if (null == pigDataSourceName || "".equals(pigDataSourceName.trim())) {
            errString = "PigDataSource name[" + pigDataSourceName
                    + "] is either empty or null";
            throw new IllegalArgumentException(errString);
        }
        setLoadedVariableName(pigDataSourceName);
    }

    /** Description of  public AnalyzerOutPutScatterMatrix buildResultFromPigResult()
     *
     * @return AnalyzerOutPutScatterMatrix : We build the output with the assumption that pig script is
     * already executed. We will transform the result into the way UI is expecting
     *
     */
    public AnalyzerOutPutScatterMatrix buildResultFromPigResult(AnalyticConfiguration config
    ) {

        ArrayList<Double> corrList = new ArrayList<Double>();

        for (ScatterMatrixInstanceCorrelation corrPair : scatterColumnsList) {
            corrList.add((null==corrPair?
                    null:
                    corrPair.getCorrelationValue()));
        }

        AnalyzerOutPutScatterMatrix output = new AnalyzerOutPutScatterMatrix();
        output.setAnalyticNodeMetaInfo(createNodeMetaInfo(locale));
        output.setDataTableMap(dataTableMap);
        output.setCorrList(corrList);
        output.setColumnNames(columnNames);
        if(getContext().isLocalModelPig() ==true){
            output.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
        }
        return output;
    }

    private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.SCATTERMATRIX_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.SCATTERMATRIX_DESCRIPTION, locale));
        return nodeMetaInfo;
    }
    /** Description of  public void initTheListAndTheMap()
     *
     *  This method is provided so we can clean up the maps and create them as neccessary.
     *
     */
    public void initTheListAndTheMap() {
        if (null == scatterColumnsList) {
            scatterColumnsList = new ArrayList<ScatterMatrixInstanceCorrelation>();
        }
        if (null == dataTableMap) {
            dataTableMap = new HashMap<ScatterMatrixColumnPairs, DataTable>();
        }
        try{
            max_rows = Double.parseDouble(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT));
        }catch(Throwable e){
            itsLogger.error("Got an exception while trying to fetch the max_rows from user profile on ProfileReader");
        }
        if(null==max_rows){
            max_rows=200D;
        }

    }

    private DataTable acquireScatterPlotForTheColumns(String column1,
                                                      String column2, Map<String, List<Double>> sampleDataMap)
            throws AnalysisException {
        DataTable dataTable = new DataTable();

        List<Double> column1Samples = sampleDataMap.get(column1);

        List<Double> column2Samples = sampleDataMap.get(column2);
        List<DataRow> rowList = new ArrayList<DataRow>();

        for (int i = 0; i < column1Samples.size(); i++) {
            String[] items = new String[2];
            DataRow dr = new DataRow();
            String columnX = (null==column1Samples.get(i))
                    ?null
                    :column1Samples.get(i).toString();
            String columnY = (null==column2Samples.get(i))
                    ?null
                    :column2Samples.get(i).toString();
            items[0] = columnX;
            items[1] = columnY;
            if(null==columnX||null==columnY){
                if(itsLogger.isDebugEnabled()){
                    itsLogger.debug("Either columnX["+columnX+"]or columnY["+columnY+"] is null will ignore this to calculate the scatter plot matrix");
                }
                continue;
            }
            dr.setData(items);
            rowList.add(dr);
        }

        dataTable.setRows(rowList);
        return dataTable;
    }
    /** Description of  public List<ScatterMatrixInstanceCorrelation> calculateCorrelationAndScatterPlotInfo()
     *
     * @return AnalyzerOutPutScatterMatrix : We build the output with the assumption that pig script is
     * already executed. We will transform the result into the way UI is expecting
     *
     * @throws AnalysisException,IOException
     * This method must be called after required artifacts are set such as pigserver, column names, etc
     *
     *
     */
    public List<ScatterMatrixInstanceCorrelation> calculateCorrelationAndScatterPlotInfo()
            throws AnalysisException, IOException {
        if (null == columnNames || columnNames.length < 2) {
            String errString = "Column names["
                    + (null == columnNames ? null : Arrays
                    .toString(columnNames))
                    + "] must have at least two columns to be defined";
            throw new AnalysisException(errString);
        }
        initTheListAndTheMap();
        buildTheMaps();

        String generatedAlias = generateAlias();

        pigScript = buildPigScriptFromTheTemplate(generatedAlias);

        runThePigScriptAndOpenTheTuples(pigScript);

        fetchAndBuildTheResult();

        return scatterColumnsList;

    }

    private void fetchAndBuildTheResult()
            throws ExecException, AnalysisException {
        Map<String, List<Double>> sampleDataMap = extractSamplingData();
        buildScatterMatrixInstances(correlationTuple, sampleDataMap);

    }

    private void runThePigScriptAndOpenTheTuples(List<String> pigScript)
            throws IOException, AnalysisException {

        acquireCorrelationTuple(pigScript);
        acquireSamplingTuple();
    }


    private void acquireSamplingTuple() throws ExecException, IOException {
        int cs=correlationTuple.size();
        Double totalNumber=0D;
        for(int i=0;i<cs/6;i++){
            Object xcount=correlationTuple.get(i*6);
            if(null==xcount)continue;

            totalNumber=DataType.toDouble(xcount)>totalNumber?DataType.toDouble(xcount):totalNumber;
        }

        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Total number of the rows is["+totalNumber+"]");
            itsLogger.debug(totalNumber);
        }

        samplingScript="samplePlotColumnspigVariable = SAMPLE aScatterPlotColumnspigVariable ";
        Double samplingFactor=max_rows/totalNumber;
        samplingScript+=(samplingFactor>=1?1:samplingFactor)+";";
        samplingScript=samplingScript.replace("cVal", totalNumber+"");

        String pigReady = samplingScript.replace(PIG_VARIABLE,getLoadedVariableName());


        registerPigQueries(getPigServer(), pigReady);

        tupleIteratorForSampling = pigServer.openIterator("samplePlotColumns"+getLoadedVariableName());
    }

    private String filterOutSamplingCodeDueToPig81Issue(List<String> script) {

        for(int i=0;i<script.size();i++){
            if(script.get(i).contains("samplePlotColumns")){
                samplingScript  = script.get(i);
                script.remove(i);

                return samplingScript;
            }
        }
        itsLogger.error("Could not find the sampling code that suppose to have["+"samplePlotColumns");
        return "";
    }


    private void acquireCorrelationTuple(List<String> pigScript) throws AnalysisException {
        Iterator<Tuple> it = null;
        try {

            filterOutSamplingCodeDueToPig81Issue(pigScript);
            registerPigQueries(getPigServer(), pigScript.toArray(new String[] {}));

            it = pigServer.openIterator("agPrNumber"+getLoadedVariableName());
            if (null == it || !it.hasNext()) {
                String stringErrorMsg = "Could not fetch the Number of availabeRows for the file of[hadoopSource.getFileName()]";
                itsLogger.error(stringErrorMsg);
                throw new AnalysisException(stringErrorMsg);
            }

            if(!it.hasNext()){
                throw new AnalysisException("Could not fetch the values from ScatterPlotterMatrix");
            }


            Tuple next = it.next();
            correlationTuple = (Tuple)next.get(0);



        } catch (Exception e) {
            String err = "Could not fetch the Correlation tuple due to: ";
            itsLogger.error(err, e);
            throw new AnalysisException(err, e);

        }

    }

    public void buildTheMaps() {
        alias = generateColumnAliasMap();
        mapOfCalcSigles = generateCalcFirstColumnAliasMap();
        locationsInTheReturnTuple = new HashMap<String, Integer>();
        buildScatterColumnCorrLocations();
    }

    /** Description of  List<String> buildPigScriptFromTheTemplate(String generatedAlias,String generatedFirstCalcs, String finalCalcQuery)
     *
     * This methods generates pig script from the template yet it must be called after the paramaters already had been calculated
     *
     * @param generatedAlias
     * @return AnalyzerOutPutScatterMatrix : We build the output with the assumption that pig script is
     * already executed. We will transform the result into the way UI is expecting
     *
     * @throws AnalysisException,IOException
     * This method must be called after required artifacts are set such as pigserver, column names, etc
     *
     *
     */

    public List<String> buildPigScriptFromTheTemplate(String generatedAlias) {
        List<String> pigScript = new ArrayList<String>();
        for (String line : RAW_PIG_SCRIPT) {
            String pigReady = line.replace(PIG_VARIABLE,getLoadedVariableName());
            pigReady = pigReady.replace(XXX, " " + generatedAlias);
            pigScript.add(pigReady);
        }
        return pigScript;
    }

    public void buildScatterMatrixInstances(Tuple correlationTuple,
                                            Map<String, List<Double>> sampleDataMap) throws AnalysisException {
        int i6=-6;
        for (int i = 0; i < columnNames.length; i++) {
            for (int j = i + 1; j < columnNames.length; j++) {
                i6+=6;
                // Step 1:Calculate Scatter plot for each pairs and generate
                // output
                // structure "DataTable".
                if(itsLogger.isDebugEnabled()){
                    String columns ="Will calculate the values for the colmns of["+columnNames[i]+"], ["+columnNames[j]+"]";
                    itsLogger.debug(columns);
                }
                DataTable dt = acquireScatterPlotForTheColumns(columnNames[i],
                        columnNames[j], sampleDataMap);
                dataTableMap.put(new ScatterMatrixColumnPairs(columnNames[i],
                        columnNames[j]), dt);

                // Step 2:Calculate correlation for each pairs and put result
                // into
                // "DataTable".
                ScatterMatrixInstanceCorrelation scatterCorrelation = acquireCorrelationForTheColumns(
                        correlationTuple, columnNames[i], columnNames[j],i6);
                if(null==scatterCorrelation){
                    String errString="For the columns of["+columnNames[i]+"], ["+columnNames[j]+"] it seems there is no correlation could get built.";
                    itsLogger.error(errString);
                    throw new AnalysisException(errString);
                }
                scatterColumnsList.add(scatterCorrelation);

                if (itsLogger.isDebugEnabled()) {
                    itsLogger.debug("Got ScatterMatrixInstanceCorrelation value ["+ scatterCorrelation + "]");
                }

            }
        }
    }

    private Map<String, List<Double>> extractSamplingData() throws ExecException {
        Map<String, List<Double>> sampleDataMap = new HashMap<String, List<Double>>();
        for (int i = 0; i < columnNames.length; i++) {
            List<Double> dataList = new ArrayList<Double>();
            sampleDataMap.put(columnNames[i], dataList);
        }
        int counter=0;
        while (tupleIteratorForSampling.hasNext()) {
            Tuple sampleT = tupleIteratorForSampling.next();
            for (int i = 0; i < columnNames.length; i++) {
                Double data = DataType.toDouble(sampleT.get(i));
                sampleDataMap.get(columnNames[i]).add(data);
                counter++;
            }

        }
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("We got total number of["+counter+"]sampling");
        }
        return sampleDataMap;
    }


    private Map<String, Map<String, String>> generateCalcFirstColumnAliasMap() {
        Map<String, Map<String, String>> theMap = new HashMap<String, Map<String, String>>();

        for (int i = 0; i < columnNames.length; i++) {
            Map<String, String> uglyMap = new HashMap<String, String>();
            for (int j = i; j < columnNames.length; j++) {
                String lv = alias.get(columnNames[i]) + "*"
                        + alias.get(columnNames[j]);
                String nk = alias.get(columnNames[i])
                        + alias.get(columnNames[j]);
                uglyMap.put(lv, nk);
            }
            theMap.put(alias.get(columnNames[i]), uglyMap);
        }

        return theMap;
    }

    public String generateAlias() {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        aliasIndexNumMap = new HashMap<String, Integer>();
        for (int i = 0; i < columnNames.length; i++) {
            String al = alias.get(columnNames[i]);
            aliasIndexNumMap.put(al, index++);
            sb.append(columnNames[i]).append(" as ").append(al).append(COMMA);
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1) + ";";
        return str;
    }


    private SortedMap<String, String> generateColumnAliasMap() {
        SortedMap<String, String> aliasMap = new TreeMap<String, String>();
        for (int i = 0; i < columnNames.length; i++) {
            aliasMap.put(columnNames[i], columnNames[i] + "X" + i);
        }
        return aliasMap;
    }

    private void buildScatterColumnCorrLocations() {
        int numberOfTheColumns = columnNames.length;
        int sumStart = numberOfTheColumns;
        scatterLocation = new HashMap<String, Integer>();
        countLocation = new HashMap<String, Integer>();

        int comboCountStart = numberOfTheColumns * numberOfTheColumns / 2
                + numberOfTheColumns;
        for (int i = 0; i < numberOfTheColumns; i++) {
            for (int j = i; j < numberOfTheColumns; j++) {
                String alias1 = alias.get(columnNames[i]);
                String alias2 = alias.get(columnNames[j]);
                scatterLocation.put(SUM + alias1 + alias2, sumStart++);
                countLocation.put(SUM + alias1 + alias2, ++comboCountStart);
            }
        }

        for (int i = 0; i < numberOfTheColumns; i++) {
            scatterLocation.put(SUM_OF + alias.get(columnNames[i]),i);
            countLocation.put(SUM_OF + alias.get(columnNames[i]),++comboCountStart);
        }

    }

    // We need to make sure that pig script is already registered
    private ScatterMatrixInstanceCorrelation acquireCorrelationForTheColumns(
            Tuple scatterTuple, String column1, String column2,int i6)
            throws AnalysisException {

        try {
            String x = alias.get(column1);
            String y = alias.get(column2);
            if(itsLogger.isDebugEnabled()){
                itsLogger.debug("Will calculate the values for the alias of["+x+"]ColumnX and ["+y+"]ColumnY");
            }
            /*
                // count
               increaseTheValueOfElInTheTupleBy(vaTuple, i6, 1);
               // value x
               increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 1, x);
               // value y
               increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 2, y);
               // value xx
               increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 3, x * x);
               // value yy
               increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 4, y * y);
               // value xy
               increaseTheValueOfElInTheTupleBy(vaTuple, i6 + 5, x * y);
                */

            Double COUNT 	= DataType.toDouble(scatterTuple.get(i6));
            Double SUMX 	= DataType.toDouble(scatterTuple.get(i6+1));
            Double SUMY 	= DataType.toDouble(scatterTuple.get(i6+2));
            Double SUMXX 	= DataType.toDouble(scatterTuple.get(i6+3));
            Double SUMYY 	= DataType.toDouble(scatterTuple.get(i6+4));
            Double SUMXY 	= DataType.toDouble(scatterTuple.get(i6+5));

            if(null==COUNT||null==SUMXX||null==SUMYY||null==SUMXY||null==SUMX||null==SUMY){
                itsLogger.error("Got a null value among one of the values ofCOUNT["+COUNT+"],SUMXX["+SUMXX+"], SUMYY["+SUMYY+"],SUMXY["+SUMXY+"], SUMX["+SUMX+"], SUMY["+SUMY+"] Total number of index size of the " +
                        "iterator is["+scatterTuple.size()+"] where we observed total of["+locationsInTheReturnTuple.size()+"]");
                return null;

            }
            Double correlationValue = calculateCorrelation(COUNT, SUMXX, SUMYY,
                    SUMXY, SUMX, SUMY);
            if(correlationValue>1.001||correlationValue<-1.001){
                throw new AnalysisException("Correlation values between[+"+column1+"] and["+column2+"] is outside of the limit of [-1,1]. Please make sure to choose the values to be same data type such as Double Double");
            }

            ScatterMatrixColumnPairs pair = new ScatterMatrixColumnPairs(
                    column1, column2);
            ScatterMatrixInstanceCorrelation sctatterMatrixValue = new ScatterMatrixInstanceCorrelation(
                    pair);
            sctatterMatrixValue.setCorrelationValue(correlationValue);
            sctatterMatrixValue.setCount(COUNT);
            sctatterMatrixValue.setSumSquareX(SUMXX);
            sctatterMatrixValue.setSumSquareY(SUMYY);
            sctatterMatrixValue.setSumx(SUMX);
            sctatterMatrixValue.setSumy(SUMY);
            sctatterMatrixValue.setSumxy(SUMXY);
            return sctatterMatrixValue;

        } catch (ExecException e) {
            throw new AnalysisException(
                    "Failed to fetch any aggregated result from pig server"
                            + " for the columns of[" + column1 + COMMA + column2
                            + "]", e);
        }

    }
    public static double calculateCorrelation(double COUNT, double SUMXX,
                                              double SUMYY, double SUMXY, double SUMX, double SUMY) {
        return ((COUNT * SUMXY) - (SUMX * SUMY))
                / (Math.sqrt(Math.abs((COUNT * SUMXX) - (SUMX * SUMX))) * Math.sqrt(Math.abs((COUNT * SUMYY) - (SUMY * SUMY))));
    }

    public AlpinePigServer getPigServer() {
        return pigServer;
    }

    public void setPigServer(AlpinePigServer pigServer) {
        this.pigServer = pigServer;
    }

    public String getLoadedVariableName() {
        return loadedVariableName;
    }

    public void setLoadedVariableName(String loadedVariableName) {
        this.loadedVariableName = loadedVariableName;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String... columnNames) {
        this.columnNames = columnNames;
    }

}
