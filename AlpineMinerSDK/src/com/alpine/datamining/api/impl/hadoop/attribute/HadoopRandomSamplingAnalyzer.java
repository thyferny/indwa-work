/**
 * ClassName HadoopRandomSamplingAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: Aug 1, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopRandomSamplingConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisCSVFileStructureModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.sampling.AnalysisSampleSizeModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopUtility;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopExplorerPigTemplateReader;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutSampling;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.hadoop.ext.CSVRecordParser;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.hadoopfile.HadoopDelimeter;
import com.alpine.utility.pigserver.AlpinePigServerUtilitiy;

public class HadoopRandomSamplingAnalyzer extends AbstractHadoopAttributeAnalyzer {

    private static final String ROWS_COUNT_PIG_VARIABLE = "rowsCountPigVariable";
    private static Logger itsLogger = Logger.getLogger(HadoopRandomSamplingAnalyzer.class);
    private static final String PERCENTAGE = "Percentage";
    private static final String PIG_VARIABLE = "PigVariable";

    private static final String END = ";";
    private HadoopRandomSamplingConfig randomSamplingConfig;
    private AlpinePigServer pigServer;

    private String loadedVariableName;
    private Locale locale;

    private Map<Integer,String> idPigVariableMap;

    private boolean isSamplingDone;
    private boolean isVerificationDone=false;

    private String[] pigScript;
    private String pigCountVariableName;
    private Double totalNumber;

    private static final String[] RAW_PIG_SCRIPT;
    private static final String PIG_RANDOM_SAMPLING = "PIG_RANDOM_SAMPLING";

    static {
        RAW_PIG_SCRIPT = HadoopExplorerPigTemplateReader.fetchAlpinePigTemplate(PIG_RANDOM_SAMPLING);
        itsLogger.debug("Pig Script template code is:\n"+RAW_PIG_SCRIPT);
    }

    @Override
    protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.RANDOM_SAMPLEING_NAME,locale));
        nodeMetaInfo
                .setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.RANDOM_SAMPLEING_DESCRIPTION,locale));
        return nodeMetaInfo;
    }


    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source) throws AnalysisException{
        List<HadoopMultiAnalyticFileOutPut> theHadoopList  = null;
		 HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
	    	if(getContext().isEmptyPigVariable(hadoopSource.getInputTempName())){
				throw new AnalysisException(EMPTY_INPUT_MSG);
			}
        try {
            if(itsLogger.isDebugEnabled()){
                itsLogger.debug("Started running");
            }
            initHadoopRandomSampling(source);
            storeRandomSamples();
            theHadoopList = buildHadoopMultiAnalyticFiles();
            if(itsLogger.isDebugEnabled()){
                itsLogger.debug(null==theHadoopList?"NULL":theHadoopList.toString());
            }

        } catch (Exception e) {
            itsLogger.error("Got the exception for the file of["+(null==hadoopSource?"":hadoopSource.getFileName())+"]"+e.getLocalizedMessage(),e);
            throw new AnalysisException(e);
        }


        AnalyzerOutPutSampling output = new AnalyzerOutPutSampling();
        output.setHadoopSampling(true);
        output.setSampleTables(convertHadoopSamplesToSampleTables(theHadoopList));
        output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		if(getContext().isLocalModelPig() ==true){
			output.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,source.getAnalyticConfig().getLocale()));
		}
        return output;

    }

    /** Description of  init(AnalyticSource source) throws AnalysisException
     *
     * @param AnalyticSource source: Source of required parameters
     * @throws AnalysisException in case there are some unexpected or wrong input values
     *
     */
    private void initHadoopRandomSampling(AnalyticSource source) throws AnalysisException {

        setRandomSamplingConfig((HadoopRandomSamplingConfig) source.getAnalyticConfig());
        assertSource();
        isSamplingDone=false;
        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
        //First we do initilization
        init(hadoopSource);
        // hadoopSource.getHadoopFileStructureModel().
        String errString;
        //init the map
        idPigVariableMap=new HashMap<Integer,String>();

        // Setting pig server
        setPigServer(acquirePigServerInfo(hadoopSource));
        // Setting locale
        Locale loc = hadoopSource.getAnalyticConfig().getLocale();
        if (null == loc) {
            itsLogger.error("Could not acquire Locale yet will try to continue");
        }
        setLocale(hadoopSource.getAnalyticConfig().getLocale());
        // Setting pigData source variable name
        String pigDataSourceName = hadoopSource.getInputTempName();
        if (null == pigDataSourceName || "".equals(pigDataSourceName.trim())) {
            errString = "PigDataSource name[" + pigDataSourceName + "] is either empty or null for the file of["+hadoopSource.getFileName()+"]";
            throw new AnalysisException(errString);
        }
        setLoadedVariableName(pigDataSourceName);

        //Setting count variable
        pigCountVariableName= ROWS_COUNT_PIG_VARIABLE.replace(PIG_VARIABLE, getLoadedVariableName());

        randomSamplingConfig = (HadoopRandomSamplingConfig) source.getAnalyticConfig();

    }


    public void assertSource() throws AnalysisException {

        if(null==randomSamplingConfig){
            throw new AnalysisException("Analytic source is passed as null");
        }

        if(null==randomSamplingConfig.getSampleCount()){
            throw new AnalysisException("There is no sampling request associated");
        }

        if(Integer.parseInt(randomSamplingConfig.getSampleCount())<=0){
            throw new AnalysisException("Sampling count should be at least one");
        }



        String sampleType=randomSamplingConfig.getSampleSizeType();

        if(null==sampleType||( !PERCENTAGE.equalsIgnoreCase(sampleType.toLowerCase().trim())&&
                !"ROW".equalsIgnoreCase(sampleType.toLowerCase().trim()))){
            throw new AnalysisException("Sampling size type must be either Percentage or Row");
        }

        AnalysisSampleSizeModel randomSamplingSizes = randomSamplingConfig.getSampleSize();
        if(null==randomSamplingSizes){
            throw new AnalysisException("RandomSamplingSizes is null");
        }
        List<Integer> IDs = randomSamplingSizes.getSampleIdList();
        List<Double> sizes = randomSamplingSizes.getSampleSizeList();
        int sampleCount=Integer.parseInt(randomSamplingConfig.getSampleCount());
        if(null==sizes||null==IDs||sizes.size()!=IDs.size()||sampleCount!=sizes.size()){
            throw new AnalysisException("Size of the sampler and id of the samplers are not equal or some of the sampler counts are not set yet");
        }

        boolean isPercentage=(null==sampleType||PERCENTAGE.equalsIgnoreCase(sampleType.toLowerCase().trim()));

        for(int i=0;i<IDs.size();i++){
            Integer ID=IDs.get(i);
            Double size=sizes.get(i);
            if(null==ID||null==size||ID<0||size<=0){
                throw new AnalysisException("Neither size nor ID of sampling can be null nor negative, size also must be a positive");
            }
            if(isPercentage&&size>100){
                throw new AnalysisException("Percentage size can not be more than 100 percents");
            }
        }

        isVerificationDone=true;

    }

    public static List<AnalyzerOutPutTableObject> convertHadoopSamplesToSampleTables(
            List<HadoopMultiAnalyticFileOutPut> theHadoopList) {
        List<AnalyzerOutPutTableObject> theList=new ArrayList<AnalyzerOutPutTableObject>();
        for(HadoopMultiAnalyticFileOutPut h:theHadoopList){

            AnalyzerOutPutTableObject inst = new AnalyzerOutPutTableObject();
            inst.setTableName(h.getOutputFolder());
            inst.setSchemaName(h.getOutputFolder());

            inst.setHadoopFileFlag(true);
            List<String> columnNameList = h.getHadoopFileStructureModel().getColumnNameList();
            List<String> columnTypeList=h.getHadoopFileStructureModel().getColumnTypeList();
            List<TableColumnMetaInfo> tableMetaInfo=new ArrayList<TableColumnMetaInfo>();
            for(int i=0;i<columnNameList.size();i++){
                TableColumnMetaInfo ti=new TableColumnMetaInfo(columnNameList.get(i), columnTypeList.get(i));
                tableMetaInfo.add(ti);
            }

            inst.setColumns(tableMetaInfo);

            DataTable dataTable = new DataTable();

            CSVRecordParser csvParser = createCSVParser(h.getHadoopFileStructureModel());
            String delimeter = ((AnalysisCSVFileStructureModel)h.getHadoopFileStructureModel()).getDelimiter();
            if("other".equalsIgnoreCase(delimeter)){
                delimeter=((AnalysisCSVFileStructureModel)h.getHadoopFileStructureModel()).getOther();
            }
            delimeter=HadoopDelimeter.getDelimeter(delimeter);

            List<String[]> data =h.getOutputFileSampleContents();
            List<DataRow> dr = new ArrayList<DataRow>();
            int rcount=0;
            //Reading each line and then splitting each column with the delimeter
            if (null != data) {
                for (int i = 0; i < data.size(); i++) {
                    String[] rows = data.get(i);
                    for (String sr : rows) {
                        DataRow rd = new DataRow();
                        String[] lines;
                        try {
                            lines = parseTheLines(sr,delimeter,csvParser);
                            rd.setData(lines);
                            rd.setSequence(rcount++);
                            dr.add(rd);
                        } catch (IOException e) {
                            itsLogger.error("Could not parse the line of[].Will skip this line and will work on the next line",e);
                        }

                    }
                }
            }

            dataTable.setRows(dr);
            dataTable.setSchemaName(h.getOutputFolder());
            dataTable.setColumns(tableMetaInfo);
            inst.setDataTable(dataTable);
            theList.add(inst);
        }

        return theList;
    }

    private static String[] parseTheLines(String sr, String delimeter,CSVRecordParser csvParser) throws IOException {
        if(null!=csvParser)
            return csvParser.parseLine(sr);
        return sr.split(delimeter);
    }


    private static CSVRecordParser createCSVParser(AnalysisFileStructureModel fsModel){

        CSVRecordParser csvParser =null;
        if(null!=fsModel&&
                null!=((AnalysisCSVFileStructureModel)fsModel).getQuoteChar()&&
                StringUtil.isEmpty(((AnalysisCSVFileStructureModel)fsModel).getDelimiter())==false&&
                null!=((AnalysisCSVFileStructureModel)fsModel).getEscapChar()){
            String quoteChar = ((AnalysisCSVFileStructureModel)fsModel).getQuoteChar();
            if(quoteChar==""){
                quoteChar="\"" ;
            }
            String escChar = ((AnalysisCSVFileStructureModel)fsModel).getEscapChar();
            if(escChar==""){
                escChar="\\" ;
            }
            csvParser = new CSVRecordParser(HadoopUtility.getDelimiterValue(fsModel).charAt(0),
                    quoteChar.charAt(0), escChar.charAt(0));
        }
        return csvParser;

    }


    public String[] generatePigScript() throws AnalysisException, IOException {
        if(!isVerificationDone||itsLogger.isDebugEnabled()){
            itsLogger.debug("Verification has not been done yet will carry on");
        }

        List<String> genPigScript= new ArrayList<String>();

        List<Integer> ids 	= randomSamplingConfig.getSampleSize().getSampleIdList();
        List<Double> counts = randomSamplingConfig.getSampleSize().getSampleSizeList();

        boolean isPercentage = isSampleTypePercentage();

        if(!isPercentage){
            String firstLine=ROWS_COUNT_PIG_VARIABLE+" = FOREACH (GROUP PigVariable ALL) GENERATE COUNT($1) as totalCount;";
            firstLine = firstLine.replace(PIG_VARIABLE, getLoadedVariableName());
            AlpinePigServerUtilitiy.registerPigQueries(pigServer,firstLine);
            assertRequestedSize();
            //We don't need to add the first line anymore into pig script registry since due to issue on pig 0.8.1 we have to pass the percentage up front
        }

        for (int id = 0; id < ids.size(); id++) {
            String pigSampleVariableName = getLoadedVariableName() + "_"
                    + ids.get(id);
            String line = pigSampleVariableName + " = SAMPLE "
                    + getLoadedVariableName() + " " + (!isPercentage?((counts.get(id)/totalNumber)) :(counts.get(id)/100))
                    + END;
            idPigVariableMap.put(ids.get(id), pigSampleVariableName);
            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("Generated sample script:\n" + line);
            }
            genPigScript.add(line);
        }

        pigScript=genPigScript.toArray(new String[]{});

        return pigScript;

    }



    public boolean isSampleTypePercentage() {
        return (null==randomSamplingConfig.getSampleSizeType()||PERCENTAGE.equalsIgnoreCase(randomSamplingConfig.getSampleSizeType().toLowerCase().trim()));
    }


    public void storeRandomSamples() throws IOException, Exception {
        if(null==randomSamplingConfig){
            throw new IllegalStateException("Sampling configuration is not set yet, it is null for the file of["+hadoopSource.getFileName()+"]");
        }
        assertSource();

        pigScript= generatePigScript();
        AlpinePigServerUtilitiy.registerPigQueries(pigServer, pigScript);
        addPigVariablesIntoContext();
        for(Integer id:randomSamplingConfig.getSampleSize().getSampleIdList()){
			storeOutputForTheGivenPigVaribleIntoGivenFile(pigServer,idPigVariableMap.get(id),(null==resultLocaltion?"/":resultLocaltion)+resultsName+"_"+id);
        }

        isSamplingDone=true;
    }


    private void assertRequestedSize() throws AnalysisException {
        if(!isSampleTypePercentage()) {
            totalNumber = acquireTotalNumberOfAvailableRows();
            Double requestedSize = getRequestedMaxSampleSize();
            if(totalNumber<requestedSize){
                throw new AnalysisException("Sampling row count["+requestedSize+"] is bigger than available count["+totalNumber+"]. Please change the sampling count accordingly for the file of["+hadoopSource.getFileName()+"]");
            }
        }
    }


    public Double getRequestedMaxSampleSize() {
        AnalysisSampleSizeModel randomSamplingSizes = randomSamplingConfig.getSampleSize();
        List<Double> sizes = randomSamplingSizes.getSampleSizeList();
        Double requestedSize = sizes.get(0);
        for(Double sz:sizes){
            if(requestedSize<sz){
                requestedSize=sz;
            }
        }
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Max request sampling size is["+requestedSize+"] for the file of["+hadoopSource.getFileName()+"]");
        }
        return requestedSize;
    }



    private Double acquireTotalNumberOfAvailableRows() throws AnalysisException {
        Iterator<Tuple> it = null;
        try {
			//this openIterator is ok since it will get all the data

            it = pigServer.openIterator(pigCountVariableName);

            if (null == it || !it.hasNext()) {
                String stringErrorMsg = "Could not fetch the Number of availabeRows for the file of["+hadoopSource.getFileName()+"]";
                itsLogger.error(stringErrorMsg);
                throw new AnalysisException(stringErrorMsg);
            }


            Tuple next = it.next();

            if (next.size() != 1) {
                String stringErrorMsg = "Received the tuple with some missing or extra columns.. Tuple has["
                        + next.size() + "]columns where it was suppose to have only one for the file of["+hadoopSource.getFileName()+"]";
                itsLogger.error(stringErrorMsg);
                throw new AnalysisException(stringErrorMsg);
            }
            Double totalNumberOfTheRows = DataType.toDouble(next.get(0));
            if(itsLogger.isDebugEnabled()){
                itsLogger.debug("Total number of the rows is["+totalNumberOfTheRows+"]");
            }
            return totalNumberOfTheRows;


        } catch (Exception e) {
            String err = "Could not fetch the Number of availabeRows due to the exception of: ";
            itsLogger.error(err, e);
            throw new AnalysisException(err, e);

        }

    }


    private void addPigVariablesIntoContext() {
        Set<Integer> keys=idPigVariableMap.keySet();
        for(Integer k:keys){
			getContext().addPigVariable(resultLocaltion+resultsName+"_"+k, idPigVariableMap.get(k));
        }
    }


    public List<HadoopMultiAnalyticFileOutPut> buildHadoopMultiAnalyticFiles() throws IOException, Exception {

        if(!isSamplingDone){
            throw new IllegalStateException("Sampling has not done yet for the file of["+hadoopSource.getFileName()+"]");
        }

        List<HadoopMultiAnalyticFileOutPut> theList=new ArrayList<HadoopMultiAnalyticFileOutPut>();
        for(Integer id:randomSamplingConfig.getSampleSize().getSampleIdList()){
			theList.add(generateHadoopOutputForTheGivenFile((null==resultLocaltion?"/":resultLocaltion)+resultsName+"_"+id));

        }
        return theList;
    }


    protected AlpinePigServer acquirePigServerInfo(HadoopAnalyticSource hadoopSource)
            throws AnalysisException {
        String errString;
        try {
            if(null==getContext()){
                if(null==pigServer){
                    throw new AnalysisException("Can not acquire pigserver since hadopsource, hadoopinfo and or context along pigserver itself null for the file of["+hadoopSource.getFileName()+"]");
                }
                return pigServer;
            }
            AlpinePigServer pServer = getContext().getPigServer(hadoopSource.getHadoopInfo());
            if (null == pServer) {
                errString = "Could not acquire Pig Server for the file of["+hadoopSource.getFileName()+"]";
                throw new AnalysisException(errString);
            }
            return pServer;
        } catch (Exception e) {
            errString = "Could not acquire Pig Server for the file of["+hadoopSource.getFileName()+"]";
            throw new AnalysisException(errString, e);
        }
    }


    public String[] getPigScript() {
        return pigScript;
    }


    public void setPigScript(String[] pigScript) {
        this.pigScript = pigScript;
    }


    public HadoopRandomSamplingConfig getRandomSamplingConfig() {
        return randomSamplingConfig;
    }

    public void setRandomSamplingConfig(HadoopRandomSamplingConfig randomSamplingConfig) {
        isVerificationDone=false;
        this.randomSamplingConfig = randomSamplingConfig;
    }


    public AlpinePigServer getPigServer() {
        return pigServer;
    }

    public void setPigServer(AlpinePigServer iAlpinePigServer) {
        this.pigServer = iAlpinePigServer;
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
}
