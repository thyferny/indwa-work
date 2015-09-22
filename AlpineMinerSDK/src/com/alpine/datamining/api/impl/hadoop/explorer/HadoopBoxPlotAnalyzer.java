/**
 * User: sasher
 * Date: 11/9/12
 * Time: 4:00 PM
 */
package com.alpine.datamining.api.impl.hadoop.explorer;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableBoxAndWhiskerConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.*;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.pigserver.AlpinePigServerUtilitiy;
import org.apache.log4j.Logger;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.util.*;


public class HadoopBoxPlotAnalyzer extends AbstractHadoopExplorerAnalyzer {
    private static Logger itsLogger = Logger
            .getLogger(HadoopBarChartAnalyzer.class);


    private String valueDomain;
    private String series;
    private String categoryType;
    private boolean useApproximation;
    private List<DataRow> dataRows;

    public HadoopBoxPlotAnalyzer() {
    }

    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException {
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Starting Hadoop Box Plot Analyser");
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
            if(null==hdfsFileTableName||"".equals(hdfsFileTableName.trim())){
                String errString="PigDataSource name["+hdfsFileTableName+"] is either empty or null";
                throw new IllegalArgumentException(errString);
            }

            TableBoxAndWhiskerConfig config = (TableBoxAndWhiskerConfig) source
                    .getAnalyticConfig();

            valueDomain = config.getAnalysisValueDomain();
            series = config.getSeriesDomain();
            categoryType = config.getTypeDomain();
            useApproximation = config.getUseApproximation();

            String finalJob = "";
            if (useApproximation)
                finalJob = getQuantiles(hdfsFileTableName,valueDomain,series,categoryType, pigServer);
            else
                finalJob = getExactQuantiles(hdfsFileTableName,valueDomain,series,categoryType, pigServer);

            AnalyzerOutPutBoxWhisker output = new AnalyzerOutPutBoxWhisker();
            output.setApprox(useApproximation);

            output.setAnalyticNodeMetaInfo(createNodeMetaInfo(source));
            handleResults(pigServer, finalJob, output);


            //now we have the jobs, let's see what happens.


            // pigScript = generatePigScript(source, hdfsFileTableName);
            // AlpinePigServerUtilitiy.registerPigQueries(pigServer, pigScript);
            // fetchBoxPlotData(pigServer, hadoopSource.getHadoopInfo());



            if (itsLogger.isDebugEnabled()) {
                itsLogger.debug("Got the result of[outputFileSampleContents.toString()] for the pigScript of:"+ pigScript);
            }
            if(getContext().isLocalModelPig() ==true){
                output.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
            }
            return output;

        } catch (Exception e) {
            String errString = "Could not execute the pig script of[" + Arrays.toString(pigScript)
                    + "due to the exception of"+e.getMessage();
            itsLogger.error(errString, e);
            throw new AnalysisException(errString, e);
        }

        // 2 read the file into a AnalyzerOutPutTableObject and return in ,
        // please refere to TableAnalysisAnalyzer
        //return buildPresentationData(source);
    }

    private AnalyticNodeMetaInfo createNodeMetaInfo(AnalyticSource source) {
        Locale locale= source.getAnalyticConfig().getLocale();
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.BOX_PLOT_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.BOX_PLOT_DESCRIPTION, locale));

        return nodeMetaInfo;
    }

    private void handleResults(AlpinePigServer pigServer, String iteratorName, AnalyzerOutPutBoxWhisker output) throws AnalysisException, IOException {
        try{
    		//this openIterator is ok since it will get all the data
            Iterator<Tuple> it = pigServer.openIterator(iteratorName);

            if (!it.hasNext()) {
                throw new AnalysisException(
                        "Failed to fetch box plot info");
            }
            while (it.hasNext())
            {
                Tuple next = it.next();
                BoxAndWhiskerItem ourOneResult = createBoxAndWhisker(next,valueDomain,series,categoryType);
                output.addItem(ourOneResult);
            }


        }catch(Exception e){
            throw new AnalysisException("Got the exception of["+e.getMessage()+"]",e);
        }
    }

    private String getExactQuantiles(String hdfsFileTableName, String value, String series, String  type,AlpinePigServer pigServer) throws IOException
    {
        String pr="pr"+hdfsFileTableName;
        String gr= "gr"+hdfsFileTableName;
        String freVarName="fr"+hdfsFileTableName;

        String sorted = "sorted = ORDER " + pr + " BY " +  value + "; sum=SUM(sorted." + value + "); count = COUNT(sorted);";


        StringBuilder sb= new StringBuilder(pr);


        sb.append(" = FOREACH "+hdfsFileTableName+" GENERATE ");

        sb.append(value);
        boolean hasSeries = false;
        boolean hasType = false;
        if (series != null && !series.trim().equals(""))
        {
            sb.append(",").append(series);
            hasSeries = true;
        }
        if (type != null && !type.trim().equals(""))
        {
            sb.append(",").append(type);
            hasType = true;
        }
        sb.append(";");

        String inputData=sb.toString();

        String groupString;
        String evalString;

        if (hasSeries && hasType)
        {
            groupString = gr + " = GROUP " + pr + " BY (" +type +  ", " + series + ");";
            evalString = freVarName+" = FOREACH "+gr+ "{ " + sorted + " GENERATE group." +series + ", group." + type+ ", AlpinePiggyBoxPlotExact(sorted." + value + "),(double)sum/(double)count;};";
        } else if (hasSeries)
        {
            groupString = gr + " = GROUP " + pr + " BY (" +series + ");";
            evalString = freVarName+" = FOREACH "+gr+ "{ " + sorted + " GENERATE group, '', AlpinePiggyBoxPlotExact(sorted." + value + "),(double)sum/(double)count;};";
        }  else if (hasType)
        {
            groupString = gr + " = GROUP " + pr + " BY (" +type + ");";
            evalString = freVarName+" = FOREACH "+gr+ "{ " + sorted + " GENERATE '', group, AlpinePiggyBoxPlotExact(sorted." + value + "),(double)sum/(double)count;};";
        }   else
        {
            groupString =  gr+" = GROUP "+pr+"  ALL;";
            evalString = freVarName+" = FOREACH "+gr+ "{ " + sorted + " GENERATE '', '', AlpinePiggyBoxPlotExact(sorted." + value + "),(double)sum/(double)count;};";
        }

        //AlpinePiggyBoxPlotExact(sorted." + value + "),(double)sum/(double)count;}";

        AlpinePigServerUtilitiy.registerPigQueries(pigServer, new String[]{inputData,groupString,evalString});

        return freVarName;
    }

    private String getQuantiles(String hdfsFileTableName, String value, String series, String  type,AlpinePigServer pigServer) throws IOException {

        String pr="pr"+hdfsFileTableName;
        String gr= "gr"+hdfsFileTableName;
        String freVarName="fr"+hdfsFileTableName;

        StringBuilder sb= new StringBuilder(pr);


        sb.append(" = FOREACH "+hdfsFileTableName+" GENERATE ");

        sb.append(value);
        boolean hasSeries = false;
        boolean hasType = false;
        if (series != null && !series.trim().equals(""))
        {
            sb.append(",").append(series);
            hasSeries = true;
        }
        if (type != null && !type.trim().equals(""))
        {
            sb.append(",").append(type);
            hasType = true;
        }
        sb.append(";");

        String inputData=sb.toString();

        String groupString;
        String evalString;

        if (hasSeries && hasType)
        {
            groupString = gr + " = GROUP " + pr + " BY (" +type +  ", " + series + ");";
            evalString = freVarName+" = FOREACH "+gr+" GENERATE group." +series + ", group." + type+ ", AlpinePiggyBoxChart("+pr+"."+ value  + ");";
        } else if (hasSeries)
        {
            groupString = gr + " = GROUP " + pr + " BY (" +series + ");";
            evalString = freVarName+" = FOREACH "+gr+" GENERATE group, '', AlpinePiggyBoxChart("+pr+"."+ value  + ");";
        }  else if (hasType)
        {
            groupString = gr + " = GROUP " + pr + " BY (" +type + ");";
            evalString = freVarName+" = FOREACH "+gr+" GENERATE '', group, AlpinePiggyBoxChart("+pr+"."+ value  + ");";
        }   else
        {
            groupString =  gr+" = GROUP "+pr+"  ALL;";
            evalString = freVarName+" = FOREACH "+gr+" GENERATE '', '', AlpinePiggyBoxChart("+pr+"."+ value  + ");";

        }


        AlpinePigServerUtilitiy.registerPigQueries(pigServer, new String[]{inputData,groupString,evalString});

        return freVarName;
    }

    private BoxAndWhiskerItem createBoxAndWhisker(Tuple oneGroupResult, String valueDomain,String series,String categoryType)  throws ExecException
    {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem();

        Object value = oneGroupResult.get(0);
        item.setSeries((value == null)? "":value.toString().trim());
        value = oneGroupResult.get(1);
        item.setType((value == null)? "":value.toString().trim());


        Tuple results = (Tuple) oneGroupResult.get(2);

        item.setMin(DataType.toDouble(results.get(0)));
        item.setQ1(DataType.toDouble(results.get(1)));
        item.setMedian(DataType.toDouble(results.get(2)));
        item.setQ3(DataType.toDouble(results.get(3)));
        item.setMax(DataType.toDouble(results.get(4)));
        if (useApproximation)
        {
            item.setMean(DataType.toDouble(results.get(5)));

        }   else
        {
            item.setMean(DataType.toDouble(oneGroupResult.get(3)));
        }
        item.setVariableName(valueDomain);
        item.setSeriesName(series);
        item.setTypeName(categoryType);

        return item;
    }



}
