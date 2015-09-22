package com.alpine.datamining.api.impl.hadoop.runner;


import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.HadoopVariableSelectionConfig;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionResult;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopRunner;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.VariableSelectionKeySet;
import com.alpine.hadoop.utily.type.DoubleArrayWritable;
import com.alpine.hadoop.variableselection.*;
import com.alpine.utility.exception.EmptyFileException;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopDataType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HadoopVariableSelectionRunner extends AbstractHadoopRunner {

    private static Logger itsLogger = Logger.getLogger(HadoopVariableSelectionRunner.class);
    private long objectTimeStamp=System.currentTimeMillis();
    private String betaTemp=null;
    private String statTemp=null;

    protected String resultsName;

    protected HadoopVariableSelectionConfig config;
    Configuration baseConf = new Configuration();
    VariableSelectionResult resultSet = null;

    Map<String, String> resultMap = new HashMap<String, String>();

    private List<String> resultColumnsList=new ArrayList<String>();


    public HadoopVariableSelectionRunner(AnalyticContext context,String operatorName) {
        super(context,operatorName);

    }

    @Override
    public int run(String[] args) throws Exception {

        String selectedColumnNames = config.getColumnNames();
        StringBuffer distinctColumnNames = new StringBuffer();
        StringBuffer sb=new StringBuffer();
        if (selectedColumnNames != null) {
            int flag=0;
            for (String columnName : selectedColumnNames.split(",")) {
                int id = fileStructureModel.getColumnNameList().indexOf(
                        columnName);
                if (false == HadoopDataType.isNumberType(fileStructureModel
                        .getColumnTypeList().get(id))) {
                    if(flag==0){
                        distinctColumnNames.append(columnName);
                        flag=1;
                    }
                    else{
                        distinctColumnNames.append(",").append(columnName);
                    }
                }
                else{
                    sb.append(columnName).append(",");
                }
            }
        }

        Map<String, String[]> distinctColumnMap = initDistinctMap(distinctColumnNames.toString());// if
        // distinctColumnNames
        // not null
        List<String> shouldReMoveList=new ArrayList<String>();
        for (String key : distinctColumnMap.keySet()) {
            if(distinctColumnMap.get(key).length<=1){
                shouldReMoveList.add(key);
                itsLogger.warn("column "+key+" is constant value");
            }
            else{
                if(key.equals(config.getDependentColumn())==false){
                    sb.append(key).append(",");
                }
            }
        }
        distinctColumnMap.keySet().removeAll(shouldReMoveList);
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        else{
            throw new Exception("Can not perform calculation since each selected column only contains one distinct value.");
        }
        config.setColumnNames(sb.toString());
//		if(distinctColumnMap.size()<=1){
//			throw new Exception("")
//		}
        initResultColumn(distinctColumnMap);

        callBetaJob(distinctColumnMap);

        tmpFileToDelete.add(betaTemp);
        readFileToKeyMap(betaTemp);

        String beta = trimArrayString(resultMap.get(VariableSelectionKeySet.beta));
        String alpha = trimArrayString(resultMap.get(VariableSelectionKeySet.alpha));
        String dependent_avg = trimArrayString(resultMap.get(VariableSelectionKeySet.dependent_avg));

        callR2Job(alpha, beta, dependent_avg);

        String[] columns = resultColumnsList.toArray(new String[0]);


        tmpFileToDelete.add(statTemp);
        readFileToKeyMap(statTemp);

        double[] scores = stringToDoubleArray(resultMap.get(VariableSelectionKeySet.r2));
        resultSet = new VariableSelectionResult(columns,scores,0,0);

        return 0;
    }

    private void readFileToKeyMap(String dirPath) throws Exception {
    	List<String> fileInfos = null;
        try{
        	fileInfos = hdfsManager.readHadoopPathToLineList4All(dirPath,
                    hadoopConnection);
            if(fileInfos==null||fileInfos.size()==0){
            	throw new EmptyFileException("");
            }	
        }
        catch(Exception e){
            if(e instanceof EmptyFileException){
                throw new Exception("Train failed,check the CLEAN data");
            }
            else{
                throw e;
            }
        }
        for (String line : fileInfos) {
            resultMap.put(line.split("\t")[0], line.split("\t")[1]);
        }
    }

    private void callR2Job(String alpha,String beta,String dependent_avg) throws Exception {
        Configuration statConf=new Configuration(baseConf);
        super.initHadoopConfig(statConf, HadoopConstants.JOB_NAME.VariableSelection_R2);
        betaTemp=tmpPath+"VarSelBetaTemp"+objectTimeStamp;
        statTemp=tmpPath+"VarSelR2Temp"+objectTimeStamp;
        statConf.set(VariableSelectionKeySet.beta, beta);
        baseConf.set(VariableSelectionKeySet.beta, beta); //does this need to be here?
        statConf.set(VariableSelectionKeySet.alpha, alpha);
        statConf.set(VariableSelectionKeySet.dependent_avg, dependent_avg);
        Job computerStatisticsJob = createJob(HadoopConstants.JOB_NAME.VariableSelection_R2, statConf,
                R2Mapper.class, R2Reducer.class, Text.class, DoubleArrayWritable.class, inputFileFullName, statTemp);

        computerStatisticsJob.setCombinerClass(R2Combiner.class);
        super.setInputFormatClass(computerStatisticsJob) ;
        runMapReduceJob(computerStatisticsJob,true);
    }

    private void callBetaJob(Map<String, String[]> distinctColumnMap) throws Exception {
        initConf("", true,distinctColumnMap);
        Configuration betaConf=new Configuration(baseConf);
        super.initHadoopConfig(betaConf,HadoopConstants.JOB_NAME.VariableSelection_Beta);
        Job computerBetaJob = createJob(HadoopConstants.JOB_NAME.VariableSelection_Beta, betaConf,
                AlphaBetaMapper.class, AlphaBetaReducer.class, Text.class, DoubleArrayWritable.class, inputFileFullName, betaTemp);

        computerBetaJob.setCombinerClass(AlphaBetaCombiner.class);
        super.setInputFormatClass(computerBetaJob) ;
        runMapReduceJob(computerBetaJob,true);
		badCounter=computerBetaJob.getCounters().findCounter(AlpineHadoopConfKeySet.ALPINE_BAD_COUNTER, AlpineHadoopConfKeySet.TYPE_NOT_MATCH).getValue();

    }

    @Override
    public VariableSelectionResult runAlgorithm(AnalyticSource source) throws  Exception {
        init((HadoopAnalyticSource) source);
        try {
            ToolRunner.run(this, null);
        } catch (Exception e) {
            throw new AnalysisException(e);
        }
        finally{
            deleteTemp();
        }
        return resultSet;
    }

    private void initResultColumn(Map<String, String[]> distinctColumnMap) {
        if(config.getColumnNames()!=null){
            for(String column:config.getColumnNames().split(",")){
                if(distinctColumnMap.get(column)==null){
                    resultColumnsList.add(column);
                }
                else{
                    String[] distinctArray = distinctColumnMap.get(column);
                    for(int i=0;i<distinctArray.length-1;i++){
                        resultColumnsList.add(column+"_"+distinctArray[i].trim());
                    }
                }
            }
        }

    }

    private void initConf(String beta, boolean firstJob,Map<String,String[]> distinctColumnMap) throws AnalysisException {
        if(firstJob){
            baseConf.set(VariableSelectionKeySet.dependent,config.getDependentColumn());
                baseConf.set(VariableSelectionKeySet.interactionItems,"");
            if(config.getColumnNames()!=null){
                baseConf.set(VariableSelectionKeySet.columns, config.getColumnNames());

                for(String columnName:config.getColumnNames().split(",")){
                    String[] distinctArray = distinctColumnMap.get(columnName);
                    if(distinctArray!=null&&distinctArray.length>0){
                        String distincts=genereateDistinctValueString(distinctColumnMap.get(columnName));
                        baseConf.set(AlpineHadoopConfKeySet.ALPINE_PREFIX+columnName, distincts);
                    }
                }
            }
        }
        else{
            baseConf.set(VariableSelectionKeySet.beta, beta);
        }
    }

    protected void init(HadoopAnalyticSource hadoopSource) throws Exception {
        super.init(hadoopSource) ;
        betaTemp=tmpPath+"VarSelBetaTemp"+objectTimeStamp;
        statTemp=tmpPath+"VarSelR2Temp"+objectTimeStamp;
        config = (HadoopVariableSelectionConfig) hadoopSource.getAnalyticConfig();
    }

    public   String listToString(List<AnalysisInterActionItem> list,String seperator){
        StringBuffer sb = new StringBuffer();
        if(list!=null&&seperator!=null){
            for (int i = 0; i < list.size(); i++) {
                if(i>0){
                    sb.append(seperator) ;
                }
                sb.append(list.get(i).toString()) ;
            }
        }
        return sb.toString();
    }

    public   double[] stringToDoubleArray(String value){
        value=trimArrayString(value);
        String[] valueArray=value.split(",");
        double[] result = new double[valueArray.length];
        for (int i = 0; i < valueArray.length; i++)
        {
            result[i] = Double.parseDouble(valueArray[i]);
        }
        return result;
    }

    public   String trimArrayString(String arrayString){
        if(arrayString.startsWith("[")){
            arrayString=arrayString.substring(1);
        }
        if(arrayString.endsWith("]")){
            arrayString=arrayString.substring(0,arrayString.length()-1);
        }
        return arrayString;
    }

}
