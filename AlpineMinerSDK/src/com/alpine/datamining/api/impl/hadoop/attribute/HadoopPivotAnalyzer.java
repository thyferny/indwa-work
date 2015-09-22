package com.alpine.datamining.api.impl.hadoop.attribute;

import java.io.IOException;
import java.util.ArrayList;
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
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPivotConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAttributeAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.hadoop.pig.AlpinePigServer;
import com.alpine.utility.tools.StringHandler;

public class HadoopPivotAnalyzer extends AbstractHadoopAttributeAnalyzer {

    private static final Logger itsLogger = Logger.getLogger(HadoopPivotAnalyzer.class);

    private static final String GROUPBY_TEMP="groupBy"+System.currentTimeMillis();

    private static final String DISTINCTVALUE_TEMP="distinctValue"+System.currentTimeMillis();

    private static final String ADD_COLUMN_TEMP="addColumn"+System.currentTimeMillis();

    private List<String> distinctValues;

    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source)
            throws AnalysisException {
		 HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;
	    	if(getContext().isEmptyPigVariable(hadoopSource.getInputTempName())){
				throw new AnalysisException(EMPTY_INPUT_MSG);
			}
        init((HadoopAnalyticSource)source);

        try {
            AlpinePigServer pigServer = getContext().getPigServer(hadoopConnection);
            String calDistinctValuePigScript = generateCalculateDistinctValueScript(config, hadoopSource.getInputTempName());

            itsLogger.info(calDistinctValuePigScript);

            runPigScript(pigServer, calDistinctValuePigScript);

            distinctValues = calDistinctValue(pigServer);

            if(distinctValues.size()>Integer.parseInt(AlpineMinerConfig.HADOOP_PIVOT_DISTINCTVALUE_THRESHOLD)){
                itsLogger.error(
                        "Too many distinct value for column "+StringHandler.doubleQ(((HadoopPivotConfig)config).getPivotColumn()));
                throw new AnalysisError(this,AnalysisErrorName.Too_Many_Distinct_value,config.getLocale(),
                        StringHandler.doubleQ(((HadoopPivotConfig)config).getPivotColumn()),AlpineMinerConfig.HADOOP_PIVOT_DISTINCTVALUE_THRESHOLD);
            }

            String pigScript = generateScript(config, hadoopSource.getInputTempName());

            itsLogger.info(pigScript);

            runPigScript(pigServer, pigScript);

            storeOutput(pigServer);

        } catch (Exception e) {
            itsLogger.error(e);
            throw new AnalysisException(e.getLocalizedMessage());
        }

        HadoopMultiAnalyticFileOutPut output = generateHadoopOutput();

        output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));

        return output;
    }

    @Override
    protected AnalysisFileStructureModel getOutPutStructure() {
        AnalysisFileStructureModel oldModel = hadoopSource.getHadoopFileStructureModel();
        AnalysisFileStructureModel newModel = generateNewFileStructureModel(oldModel);

        List<String> newColumnNameList = new ArrayList<String>();
        List<String> newColumnTypeList =new ArrayList<String>();

        newColumnNameList.add(((HadoopPivotConfig)config).getGroupByColumn());
        for(int i=0;i<oldModel.getColumnNameList().size();i++){
            if(oldModel.getColumnNameList().get(i).equals(((HadoopPivotConfig)config).getGroupByColumn())){
                newColumnTypeList.add(oldModel.getColumnTypeList().get(i));
                break;
            }
        }

        if(distinctValues!=null){
            for(String distinctValue:distinctValues){
                newColumnNameList.add(((HadoopPivotConfig)config).getPivotColumn()+"_"+distinctValue);
                newColumnTypeList.add(HadoopDataType.LONG);
            }
        }
        newModel.setColumnNameList(newColumnNameList);
        newModel.setColumnTypeList(newColumnTypeList);

        return newModel;
    }

    protected List<String> calDistinctValue(AlpinePigServer pigServer) throws IOException,
            ExecException {
        List<String> distinctValueList=new ArrayList<String>();
		//this openIterator is ok since it will get all the data
        Iterator<Tuple> iter = pigServer.openIterator(DISTINCTVALUE_TEMP);
        while(iter.hasNext()){
            Tuple tuple = iter.next();
            for(int i=0;i<tuple.size();i++){
                if(tuple.get(i)!=null){
                    String value = DataType.toString(tuple.get(i));
                    distinctValueList.add(value);
                }
            }
        }
        return distinctValueList;
    }

    public String generateCalculateDistinctValueScript(HadoopDataOperationConfig config,String inputTempName) {
        HadoopPivotConfig pConfig=(HadoopPivotConfig)config;
        String pivotColumn = pConfig.getPivotColumn();
        StringBuffer script=new StringBuffer();
        script.append(GROUPBY_TEMP).append(" = GROUP ").append(inputTempName);
        script.append(" BY ").append(pivotColumn).append(";\n");
        script.append(DISTINCTVALUE_TEMP).append(" = foreach ").append(GROUPBY_TEMP).append(" GENERATE group;");
        return script.toString();
    }

    @Override
    public String generateScript(HadoopDataOperationConfig config,String inputTempName) {
        HadoopPivotConfig pConfig=(HadoopPivotConfig)config;
        String groupByColumn = pConfig.getGroupByColumn();
        String pivotColumn = pConfig.getPivotColumn();
        String aggColumn = pConfig.getAggregateColumn();
        String aggType = pConfig.getAggregateType();
        String aggColumnName;
        if(!StringUtil.isEmpty(aggColumn)){
            aggColumnName=aggColumn;
        }else{
            aggColumnName="1";
        }
        StringBuffer script=new StringBuffer();
        script.append(ADD_COLUMN_TEMP).append(" = FOREACH ").append(inputTempName).append(" GENERATE ");
        script.append(groupByColumn).append(",");
        for(String distinctValue:distinctValues){
            String columnName=distinctValue.trim().replaceAll("[^a-zA-Z0-9]","_");
            //pivotal : 41303353
            //	(row_author=='Scott O'Dell'?1:NULL) as row_author_Scott_O_Dell_temp
            distinctValue=distinctValue.replaceAll("'", "\\\\'")		;
//			if(columnName.length()>30){
//				columnName=columnName.substring(0,29);
//			}

            script.append("(").append(pivotColumn).append("=='");
            script.append(distinctValue).append("'?").append(aggColumnName);
            script.append(":NULL) as ").append(pivotColumn).append("_").append(columnName).append("_temp");
            script.append(",");
        }
        script=script.deleteCharAt(script.length()-1);
        script.append(";\n");
        script.append(GROUPBY_TEMP).append(" = GROUP ").append(ADD_COLUMN_TEMP).append(" by ").append(groupByColumn).append(";\n");
        script.append(getOutputTempName()).append(" = FOREACH ").append(GROUPBY_TEMP);
        script.append(" generate group,");
        for(String distinctValue:distinctValues){
            String columnName=distinctValue.trim().replaceAll("[^a-zA-Z0-9]","_");
//			if(columnName.length()>30){
//				columnName=columnName.substring(0,29);
//			}


            script.append(aggType.toUpperCase()).append("(");
            script.append(ADD_COLUMN_TEMP).append(".").append(pivotColumn).append("_").append(columnName).append("_temp");
            script.append(") as ").append(pivotColumn).append("_").append(columnName).append(",");
        }
        script=script.deleteCharAt(script.length()-1);
        script.append(";");
        return script.toString();
    }

    @Override
    protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.PIVOT_NAME,locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.PIVOT_DESRIPTION,locale));

        return nodeMetaInfo;
    }

    public List<String> getDistinctValues() {
        return distinctValues;
    }

    public void setDistinctValues(List<String> distinctValues) {
        this.distinctValues = distinctValues;
    }

}
 