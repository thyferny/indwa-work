/**
 * ClassName HadoopFowFilterAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.explorer.helpers.HadoopHistogramSnapShot;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.attributeanalysisresult.BinHistogramAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;
import com.alpine.utility.file.AlpineMapUtility;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.pigserver.AlpinePigServerUtilitiy;

public class HadoopHistogramAnalyzer extends AbstractHadoopExplorerAnalyzer {
    private static Logger itsLogger = Logger
            .getLogger(HadoopHistogramAnalyzer.class);

    private AlpinePigServer pigServer;
    private String    loadedVariableName;
    private Locale    locale;
    private Map<String,AnalysisColumnBin> binsMap;
    private Map<String,String> columnNameTablePairs;
    private Map<String,List<Double>> binRelatedInfo;//List first column is begin, end, delta and second column is stepsize
    private Map<String,Boolean> filteringMap;
    private static final double TILT = 1;
    private Map<String,HadoopHistogramSnapShot> hadoopHistogramSnapShots;
    private HadoopHistogramScriptGenerator scriptGen;
    public HadoopHistogramAnalyzer() {
        hadoopHistogramSnapShots = new HashMap<String,HadoopHistogramSnapShot>();
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
            return acquireHistogramInfo(source.getAnalyticConfig());

        } catch (Exception e) {
            String errString = "Got the exception so could not generate the histogram result due to the exception of:"+e.getMessage();
            itsLogger.error(errString, e);
            throw new AnalysisException(errString, e);
        }
    }
    public void initBinRelatedMap(){
        binRelatedInfo = new HashMap<String,List<Double>>();
    }
    private void init(AnalyticSource source) throws AnalysisException {

        setUpBinsMap(source);
        initBinRelatedMap();
        HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;

        String errString;
        //Setting purefilename
        String pureFileName =hadoopSource.getFileName();
        if(null==pureFileName||"".equals(pureFileName.trim())){
            errString="File name["+pureFileName+"] is either empty or null";
            throw new IllegalArgumentException(errString);
        }
        //Setting pig server
        setPigServer(acquirePigServerInfo(hadoopSource));
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
        scriptGen=new HadoopHistogramScriptGenerator(loadedVariableName, binsMap);

    }

    private void setUpBinsMap(AnalyticSource source) {
        HistogramAnalysisConfig config = (HistogramAnalysisConfig) source
                .getAnalyticConfig();
        AnalysisColumnBinsModel binModel = config.getColumnBinModel();
        List<AnalysisColumnBin> bins = binModel.getColumnBins();

        binsMap = new TreeMap<String, AnalysisColumnBin>();
        for(AnalysisColumnBin bin:bins){
            if(binsMap.containsKey(bin.getColumnName())){
                itsLogger.error("We have more than one bin with the same" +
                        " column name that is["+bin.getColumnName()+"]we will ignore this and move on ");
            }
            binsMap.put(bin.getColumnName(), bin);
        }

    }

    public AnalyticOutPut acquireHistogramInfo(AnalyticConfiguration config) throws IOException, AnalysisException {

        HistogramAnalysisResult histogramAnalysisResult = fetchHistogramDataThrougPig();
        AnalyzerOutPutObject outPut = new AnalyzerOutPutObject(histogramAnalysisResult);
        outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(locale));
        if(getContext().isLocalModelPig() ==true){
            outPut.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
        }
        return outPut;
    }

    private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.HISTOGRAM_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.HISTOGRAM_DESCRIPTION, locale));

        return nodeMetaInfo;
    }

    private HistogramAnalysisResult fetchHistogramDataThrougPig()
            throws IOException, AnalysisException {

        List<HadoopHistogramSnapShot> hbins=new ArrayList<HadoopHistogramSnapShot>();
        columnNameTablePairs = new HashMap<String,String>();

        HistogramAnalysisResult histogramAnalysisResult = new HistogramAnalysisResult(loadedVariableName);
        Collection<AnalysisColumnBin> binColumns = binsMap.values();
        acquireMaxMinBins(binColumns);
        generateScriptForEachColumnAndRegisterIt();
        Map<Integer, Map<Integer, Integer>> binsMapOrderByColumnNumber = fethcTheBins();
        int iColumnOrder=0;
        for (AnalysisColumnBin projectedColumnBean : binColumns) {
            //HadoopHistogramSnapShot snapShot = fetchTheSnapShot(projectedColumnBean);
            HadoopHistogramSnapShot snapShot = hadoopHistogramSnapShots.get(projectedColumnBean.getColumnName());
            double delta=(snapShot.getMax())-snapShot.getMin();
            double numberOfTheBins = getNumberOfTheBins(projectedColumnBean,delta);
            delta= delta*TILT;
            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("Delta is[" + delta + "]");
            }
            //In case delta is zero than all of them same ...
            double stepSizeOnTheBin = (0==delta?snapShot.getMax():
                    getStepSize(projectedColumnBean, delta));

            injectBinInfo(projectedColumnBean,delta,stepSizeOnTheBin,snapShot.getMin(),snapShot.getMax());
            snapShot.setStepSize(stepSizeOnTheBin);


            SortedMap<Integer, Integer> pigBinMap = new TreeMap<Integer,Integer>(binsMapOrderByColumnNumber.get(iColumnOrder++));
            mergeMinusOneIntoZero(pigBinMap);
            List<BinHistogramAnalysisResult> binHistogramResult = convertBinMapToHistogramAnalysisResult(projectedColumnBean, pigBinMap,numberOfTheBins);

            fixtheResultSetIfNeccessary(binHistogramResult,projectedColumnBean);
            makeTheBinsDojoComp(binHistogramResult);
            histogramAnalysisResult.addSetOfBinResult(binHistogramResult);
            hbins.add(snapShot);
            //storeTheBin(binHistogramResult);

        }

        return histogramAnalysisResult;

    }



    private void generateScriptForEachColumnAndRegisterIt() throws IOException {
        StringBuilder result=new StringBuilder();
        SortedSet<String> sortedColumnNames = fetchedSortedColumnNames();
        for (String cn:sortedColumnNames) {
            AnalysisColumnBin projectedColumnBean = binsMap.get(cn);
            //HadoopHistogramSnapShot snapShot = fetchTheSnapShot(projectedColumnBean);
            HadoopHistogramSnapShot snapShot = new HadoopHistogramSnapShot(projectedColumnBean,loadedVariableName);
            hadoopHistogramSnapShots.put(projectedColumnBean.getColumnName(),snapShot);
            double delta=(snapShot.getMax())-snapShot.getMin();

            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("Delta is[" + delta + "]");
            }
            //In case delta is zero than all of them same ...
            double stepSizeOnTheBin = (0==delta?snapShot.getMax():
                    getStepSize(projectedColumnBean, delta));

            injectBinInfo(projectedColumnBean,delta,stepSizeOnTheBin,snapShot.getMin(),snapShot.getMax());
            snapShot.setStepSize(stepSizeOnTheBin);

            String binScript = getBinsScript(projectedColumnBean.getColumnName(), stepSizeOnTheBin,snapShot.getMin(),snapShot.getMax());
            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("TheBinScript is[" + binScript + "]");
            }



            result.append(",").append(binScript);
        }
        String withComma=result.toString();
        String withoutComma = result.substring(1,withComma.length());
        String pigScriptForTheBins=scriptGen.genPigScriptForBins(withoutComma);
        AlpinePigServerUtilitiy.registerPigQueries(pigServer,  pigScriptForTheBins);

    }


    private void mergeMinusOneIntoZero(
            SortedMap<Integer,Integer> pigBinMap) {
        Integer minusOneCount = pigBinMap.get(-1);
        if(null==minusOneCount)return;


        Integer zeroCount=pigBinMap.get(0);
        if(null==zeroCount){
            pigBinMap.put(0,minusOneCount);
        }
        else{
            pigBinMap.put(0,zeroCount+minusOneCount);
        }
        pigBinMap.remove(-1);

    }

    private void makeTheBinsDojoComp(
            List<BinHistogramAnalysisResult> binHistogramResult) {
        for(BinHistogramAnalysisResult bin:binHistogramResult){
            bin.setBin(bin.getBin()+1);
        }

    }

    private void acquireMaxMinBins(
            Collection<AnalysisColumnBin> binColumns) throws IOException, AnalysisException {
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Will be calling to get max and min");
        }
        List<String> genScript = scriptGen.genPigScriptForNumbers();
        filteringMap=new HashMap<String,Boolean>();
        AlpinePigServerUtilitiy.registerPigQueries(getPigServer(),genScript.toArray(new String[]{}));
        Tuple itForNumber = acquireIterator(scriptGen.getAggregatorVariableNameForNumber());

        SortedSet<String> sortedColumnNames = fetchedSortedColumnNames();

        for(String columnName:sortedColumnNames){
            fetchNumericalValue(itForNumber,columnName,scriptGen);
        }
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Completed to fetch max min values");
        }
    }

    private SortedSet<String> fetchedSortedColumnNames() {
        Set<String> colNames=binsMap.keySet();
        SortedSet<String> sortedColumnNames = new TreeSet<String>();
        for(String name:colNames){
            sortedColumnNames.add(name);
        }
        return sortedColumnNames;
    }


    private void fetchNumericalValue(Tuple next,
                                     String columnName, HadoopHistogramScriptGenerator scriptGenerator)
            throws IOException, AnalysisException, ExecException {
        Integer columnOrder = 2 * scriptGenerator.getColumnOrder(columnName);

        AnalysisColumnBin bm = binsMap.get(columnName);


        setValuesForTheColumn(next, columnOrder,bm);

    }
    private void setValuesForTheColumn(Tuple next, Integer columnOrder,AnalysisColumnBin bin) throws ExecException, AnalysisException {

        Double max=DataType.toDouble(next.get(columnOrder));
        Double min=DataType.toDouble(next.get(columnOrder+1));

        if(null==max||null==min){
            throw new AnalysisException("Could not fetch max["+max+"]or min["+min+"] for the column of["+bin+"]");
        }

        boolean isFilterationRequired = false;
        if(bin.isMax()&&bin.getMax()<max){
            isFilterationRequired = true;
        }else{
            bin.setMax(max);
        }

        if(bin.isMin()||bin.getMin()>min){
            isFilterationRequired = true;
        }else{
            bin.setMin(min);
        }

        filteringMap.put(bin.getColumnName(),isFilterationRequired);

    }

    private Tuple acquireIterator(String iteratorName) throws AnalysisException, IOException {
        Iterator<Tuple> it = pigServer.openIterator(iteratorName);
        if (!it.hasNext()) {
            throw new AnalysisException(
                    "Failed to fetch any aggregated iterator");
        }
        Tuple next = it.next();
        return  (Tuple) next.get(0);
    }
    private void fixtheResultSetIfNeccessary(
            List<BinHistogramAnalysisResult> bins,
            AnalysisColumnBin column) {
        List<Double> list = binRelatedInfo.get(column.getColumnName());
        double expectedLength=Math.ceil(list.get(2)/list.get(3));
        if(bins.size()==expectedLength){
            return;
        }

        if(bins.size()!=expectedLength+1){
            itsLogger.error("Do not know what to do");
            return;
        }
        //Need to combine the last with the previous of the last
        BinHistogramAnalysisResult last = bins.get(bins.size()-1);
        BinHistogramAnalysisResult previousBeforeTheLast = bins.get(bins.size()-2);
        previousBeforeTheLast.setCount(previousBeforeTheLast.getCount()+last.getCount());
        previousBeforeTheLast.setAccumCount(last.getAccumCount());
        previousBeforeTheLast.setPercentage(previousBeforeTheLast.getPercentage()+last.getPercentage());
        previousBeforeTheLast.setAccumPercentage(last.getAccumPercentage());
        bins.remove(bins.size()-1);

        rearangeTheBeginsAndEnds(bins,column);


    }

    private void rearangeTheBeginsAndEnds(
            List<BinHistogramAnalysisResult> bins, AnalysisColumnBin column) {
        List<Double> list = binRelatedInfo.get(column.getColumnName());
        double begin=list.get(0);
        double stepSize = list.get(3);
        for(BinHistogramAnalysisResult b:bins){
            b.setBegin((float)begin);
            b.setEnd((float)(begin+stepSize));
            begin+=stepSize;
        }
    }

    private void injectBinInfo(AnalysisColumnBin projectedColumnBean,double delta, double stepSizeOnTheBin,double begin,double end) {

        List<Double> binList=new ArrayList<Double>();
        binList.add(begin);
        binList.add(end);
        binList.add(delta);
        binList.add(stepSizeOnTheBin);

        binRelatedInfo.put(projectedColumnBean.getColumnName(), binList);

    }

    private double getNumberOfTheBins(AnalysisColumnBin bin,double delta){
        if (bin.getType() == AnalysisColumnBin.TYPE_BY_NUMBER) {
            return bin.getBin();
        }
        return  delta / bin.getWidth();

    }
    private double getStepSize(AnalysisColumnBin bin, double delta) {

        double stepSizeOnTheBin;
        if (bin.getType() == AnalysisColumnBin.TYPE_BY_NUMBER) {
            stepSizeOnTheBin = delta / bin.getBin();

            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("It is is[TYPE_BY_NUMBER]and has the step size of["+ stepSizeOnTheBin + "]");
            }

        }else{
            stepSizeOnTheBin = bin.getWidth();
            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("It is is[ATTR_WIDTH]and has the step size of["
                        + stepSizeOnTheBin + "]");
            }
        }


        if ( Math.ceil(delta/stepSizeOnTheBin) > 100) {
            String errString = "Number of the bins[" + Math.ceil(delta/stepSizeOnTheBin)
                    + "] would be generated is bigger than max[" + 100
                    + "] allowed";
            itsLogger.error(errString);
            throw new RuntimeException(errString);
        }


        return stepSizeOnTheBin;

    }

    public List<BinHistogramAnalysisResult> convertBinMapToHistogramAnalysisResult(
            AnalysisColumnBin bin, SortedMap<Integer, Integer> pigBinMap2, double desiredNumberOfBeans) {

        //Will start from one
        SortedMap<Integer,Integer> finalBinCounts = new TreeMap<Integer,Integer>();
        for(int i=0;i<desiredNumberOfBeans;i++){
            finalBinCounts.put(i, 0);
        }

        Set<Integer> binNumbers = pigBinMap2.keySet();
        for(Integer key:binNumbers){

            if(!finalBinCounts.containsKey(key)){
                itsLogger.error("The bin for the bin id of["+key+"] does not seem to exist we will add it now");
                finalBinCounts.put(key, 0);
            }
            Integer currentCount=finalBinCounts.get(key);
            if(!currentCount.equals(0)){
                itsLogger.error("It seems we get result for the bin[]more than once. We will add it up");
            }
            finalBinCounts.put(key, currentCount+pigBinMap2.get(key));
        }



        SortedMap<Integer,BinHistogramAnalysisResult> result = new TreeMap<Integer,BinHistogramAnalysisResult>();
        HadoopHistogramSnapShot snapShot = hadoopHistogramSnapShots.get(bin.getColumnName());
        if(null==snapShot){
            throw new IllegalArgumentException("SnapShot of the hadoop histogram for the column["+bin.getColumnName()+"]is not");
        }

        Set<Integer> binNumber = finalBinCounts.keySet();
        float totalNumberOfItems= 0;
        for(Integer binId:binNumber){
            if(null==binId){
                if(itsLogger.isDebugEnabled()){
                    itsLogger.debug("Got an null bin");
                }
                continue;
            }
            totalNumberOfItems+=finalBinCounts.get(binId);
        }
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Total number of elements in the bin for the column of["+bin.getColumnName()+"]is["+totalNumberOfItems+"]");
        }


        binNumber = finalBinCounts.keySet();
        float accCount=0;
        for(Integer binId:binNumber){
            BinHistogramAnalysisResult hist = new BinHistogramAnalysisResult();
            hist.setColumnName(bin.getColumnName());
            accCount+=finalBinCounts.get(binId);
            hist.setAccumCount(accCount);
            hist.setAccumPercentage(accCount/totalNumberOfItems);
            hist.setBin(binId);
            hist.setPercentage(finalBinCounts.get(binId)/totalNumberOfItems);
            hist.setCount(finalBinCounts.get(binId));
            result.put(binId,hist);
        }

        ifRequiredCloseTheGap(result,accCount);
        return getTheList(result,snapShot,bin);
    }

    private List<BinHistogramAnalysisResult> getTheList(
            SortedMap<Integer, BinHistogramAnalysisResult> result, HadoopHistogramSnapShot snapShot, AnalysisColumnBin bin) {
        List<BinHistogramAnalysisResult> theResult = new ArrayList<BinHistogramAnalysisResult>(result.size());
        Set<Integer> keys=result.keySet();
        double begin = snapShot.getMin();
        boolean isWidth = (bin.getType() == AnalysisColumnBin.TYPE_BY_WIDTH);
        double stepSize = isWidth? bin.getWidth(): ( (snapShot.getMax()-snapShot.getMin())/result.size());
        boolean startsFromZero=false;
        for(Integer key:keys){
            if(key.equals(0)){
                startsFromZero=true;
            }
            Integer s=startsFromZero?key:key-1;
            BinHistogramAnalysisResult inst = result.get(key);
            inst.setBegin((float)(begin+(s*stepSize)));
            inst.setEnd(snapShot.getMax()<(float)(inst.getBegin()+stepSize)?(float)snapShot.getMax():(float)(inst.getBegin()+stepSize));
            theResult.add(result.get(key));
        }
        return theResult;
    }

    private void ifRequiredCloseTheGap(SortedMap<Integer,BinHistogramAnalysisResult> result, float accCount) {
        int iExcpected=1;
        List<BinHistogramAnalysisResult> missings = new ArrayList<BinHistogramAnalysisResult>();
        for(Integer binId:result.keySet()){
            missings.addAll(generateTheMissing(iExcpected,result,binId));
            iExcpected=binId+1;
        }
        for(BinHistogramAnalysisResult m:missings){
            result.put(m.getBin(),m);
        }
    }

    private List<BinHistogramAnalysisResult> generateTheMissing(int iExpectedBinNumber,Map<Integer,BinHistogramAnalysisResult> result,int binId){
        List<BinHistogramAnalysisResult> gaps= new ArrayList<BinHistogramAnalysisResult>();
        BinHistogramAnalysisResult inst = result.get(binId);
        BinHistogramAnalysisResult prevInst = getPreviousFilledBinID(result,binId);
        for(int i=iExpectedBinNumber;i<inst.getBin();i++){
            BinHistogramAnalysisResult hist = new BinHistogramAnalysisResult();
            hist.setAccumCount(null==prevInst?0:prevInst.getAccumCount());
            hist.setAccumPercentage(null==prevInst?0:prevInst.getAccumPercentage());
            hist.setColumnName(inst.getColumnName());
            hist.setBin(i);
            hist.setPercentage(0);
            hist.setCount(0);
            gaps.add(hist);
        }
        return gaps;
    }


    private BinHistogramAnalysisResult getPreviousFilledBinID(
            Map<Integer, BinHistogramAnalysisResult> result, int binId) {
        Set<Integer> keys=result.keySet();
        BinHistogramAnalysisResult previous=null;
        for(Integer key:keys){
            if(key>=binId){
                break;
            }
            previous = result.get(key);
        }
        return previous;
    }


    private Map<Integer, Map<Integer, Integer>> fethcTheBins()
            throws IOException {

        try {
            Iterator<Tuple> it = pigServer.openIterator(scriptGen.getHistogramBinDistributionName());

            if (it.hasNext()) {
                Tuple next = it.next();
                next = (Tuple)next.get(0);
                if (next.size() != 1) {
                    String stringErrorMsg = "Received the tuple with some missing or extra columns.. Tuple has["+ next.size() + "]columns";

                    itsLogger.error(stringErrorMsg);
                    throw new RuntimeException(stringErrorMsg);
                }
                String mapString = DataType.toString(next.get(0));
                return AlpineMapUtility.desiralizeTheMap(mapString);

            } else {
                throw new RuntimeException("There has been no bin Map");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }



    public StringBuilder generatePigScript(AnalysisColumnBin bin) {
        StringBuilder pigScript = new StringBuilder();
        boolean isFiltered = bin.isMin() || bin.isMax();
        if (isFiltered) {
            /*
                     file165080751 = load 'hdfs://supportserver.local:8200/s/cleanSalaries.txt' USING PigStorage(',')  as (salary:int,age:int,edu:int);
                     records_group_filtered = FILTER file165080751 BY edu >  10.0;
                   records_group = GROUP records_group_filtered ALL;
                   record_max_min = foreach records_group generate MAX(records_group_filtered.edu) as maxEdu,MIN(records_group_filtered.edu) as minEdu;
                *
                */
            pigScript.append("records_group_columnName_filtered = FILTER loadedVariableName BY ");
            if (bin.isMin() && !bin.isMax()) {// only min
                pigScript.append("columnName >  ");
                pigScript.append(bin.getMin());
            } else if (!bin.isMin() && bin.isMax()) {// only max
                pigScript.append("columnName <= ");
                pigScript.append(bin.getMax());
            } else {// max and min
                pigScript.append("columnName >");
                pigScript.append(bin.getMin());
                pigScript.append(" and ");
                pigScript.append("columnName <= ");
                pigScript.append(bin.getMax());
            }
            pigScript.append(";\n").
                    append("records_group_columnName = GROUP records_group_columnName_filtered ALL;\n").
                    append("record_max_min_columnName = foreach records_group_columnName generate MAX(records_group_columnName_filtered.columnName) as maxEdu,MIN(records_group_columnName_filtered.columnName) as minEdu;\n");
            pigScript = injectFileNameAndColumnName(bin, pigScript);
            columnNameTablePairs.put(bin.getColumnName(), "records_group_columnName_filtered");
        }else{
            /*
                    records_group = GROUP file165080751 ALL;
                    record_max_min = foreach records_group generate MAX(file165080751.edu) as maxEdu,MIN(file165080751.edu) as minEdu;
                    */
            pigScript.
                    append("records_group_columnName = GROUP loadedVariableName ALL;\n").
                    append("record_max_min_columnName = foreach records_group_columnName generate MAX(loadedVariableName.columnName) as maxcolumnName,MIN(loadedVariableName.columnName) as mincolumnName;\n");
            pigScript = injectFileNameAndColumnName(bin, pigScript);
            columnNameTablePairs.put(bin.getColumnName(), loadedVariableName);
        }
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Generated pig script to fetch max min is\n"+pigScript);
        }
        return pigScript;
    }

    private StringBuilder injectFileNameAndColumnName(AnalysisColumnBin bin,
                                                      StringBuilder pigScript) {
        String nameFixed = pigScript.toString().replace("loadedVariableName", loadedVariableName);
        String columnNameFixed = nameFixed.replace("columnName", bin.getColumnName());
        pigScript = new StringBuilder(columnNameFixed);
        return pigScript;
    }

    private String getBinsScript(String columnName,double stepSizeOnTheBin,double tupleMin, double tupleMax){
        double MINs=tupleMin;
        double MAXs=tupleMax;
        Boolean isFilteringRequired = filteringMap.get(columnName);
        if(null!=isFilteringRequired&&isFilteringRequired){
            AnalysisColumnBin bin = binsMap.get(columnName);
            if (bin.isMin() && !bin.isMax()) {// only min
                MINs=bin.getMin();

            } else if (!bin.isMin() && bin.isMax()) {// only max
                MAXs=bin.getMax();
            } else {// max and min
                MINs=bin.getMin();
                MAXs=bin.getMax();
            }

        }
        return MINs+","+MAXs+","+stepSizeOnTheBin;
    }

    public void setPigServer(AlpinePigServer pigServer){
        this.pigServer=pigServer;
    }



    public AlpinePigServer getPigServer() {
        return pigServer;
    }
    public Map<String, HadoopHistogramSnapShot> getHadoopHistogramSnapShots() {
        return hadoopHistogramSnapShots;
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