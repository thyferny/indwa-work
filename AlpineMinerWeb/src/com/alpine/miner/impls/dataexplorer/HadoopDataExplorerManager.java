/**
 * ClassName: HadoopDataExplorerManager
 * <p/>
 * Data: 12-8-29
 * <p/>
 * Author: Will
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.miner.impls.dataexplorer;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.algoconf.BarChartAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.FrequencyAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.TableBoxAndWhiskerConfig;
import com.alpine.datamining.api.impl.algoconf.TableScatterMatrixConfig;
import com.alpine.datamining.api.impl.algoconf.ValueAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopExplorerAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopBarChartAnalyzer;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopBoxPlotAnalyzer;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopFrequencyAnalysisAnalyzer;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopHistogramAnalyzer;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopScatterPlotMatrixAnalyzer;
import com.alpine.datamining.api.impl.hadoop.explorer.HadoopValueAnalysisAnalyzer;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.result.OutPutVisualAdapterFactory;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.util.VisualUtils;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.hadoop.HadoopHDFSFileManager;
import com.alpine.utility.hadoop.fs.HadoopHDFSFileManagerFactoryImpl;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

public class HadoopDataExplorerManager {

    public static HadoopDataExplorerManager INSTANSE = new HadoopDataExplorerManager();

    private HadoopDataExplorerManager(){}

    ResourceInfo.ResourceType getResourceType(String type){
        if(type==null){
            return ResourceInfo.ResourceType.Personal;
        }
        if(type.equals(ResourceInfo.ResourceType.Public.toString())){
            return ResourceInfo.ResourceType.Public;
        }else if(type.equals(ResourceInfo.ResourceType.Group.toString())){
            return ResourceInfo.ResourceType.Group;
        }else{
            return ResourceInfo.ResourceType.Personal;
        }
    }

    public VisualizationModel runAnalyzerForVisual(HadoopAnalyticSource source,
                                                   AbstractHadoopExplorerAnalyzer analyzer,String fileName,
                                                   String resourceType,String userName,Locale locale) throws  Exception {
        VisualizationModel visualModel =null;
        try{

            AnalyticOutPut outPut = analyzer.doAnalysis(source);
            outPut.setDataAnalyzer(analyzer);
            visualModel = OutPutVisualAdapterFactory
                    .getInstance().getAdapter(outPut).toVisualModel(outPut,  locale);
            visualModel.setTitle(fileName);
        }catch (Exception e){
            throw e;
        }
        return visualModel;
    }



    public  VisualizationModel getBoxAndWhiskersVModel(HadoopConnection hadoopInfo,FileStructureModel structureModel,String hadoopFileFormat,String fileName, String analysisValue,
                                                 String analysisSeries, String analysisType, String useApproximation,String resourceType,
                                                 String user,boolean isHDFileOperator, Locale locale) throws Exception {
        AnalyticConfiguration barChartAnalysisConfig =new TableBoxAndWhiskerConfig(analysisValue,analysisSeries,analysisType, useApproximation);

        HadoopBoxPlotAnalyzer boxAnalyzer=new HadoopBoxPlotAnalyzer();
        AnalysisFileStructureModel analysisModel =  structureModel;

        HadoopAnalyticSource source = new HadoopAnalyticSource(hadoopInfo);
        //source.setPath(fileName);

        fileName = VisualUtils.refineFileName(fileName,user,isHDFileOperator);

        source.setFileName(fileName);
        source.setFileFormat(hadoopFileFormat);
        source.setNameAlias("Box and whiskers chart");
        source.setHadoopFileStructureModel(analysisModel);
        source.setAnalyticConfiguration(barChartAnalysisConfig);
        String inputName = "File"+System.currentTimeMillis();
        source.setInputTempName(inputName);

        boxAnalyzer.setAnalyticSource(source);

        AnalyticContext context = new AnalyticContext();
        setPigLocalMode(context,fileName,hadoopInfo) ;
        loadFile4Pig(inputName,fileName,context,hadoopInfo,structureModel);
        boxAnalyzer.setContext(context);

        VisualizationModel visualModel = runAnalyzerForVisual(source, boxAnalyzer,fileName ,resourceType,user,locale);
        visualModel.setTitle(fileName);
        context.dispose();
        return visualModel;
    }


    public  VisualizationModel getBarchartVModel(HadoopConnection hadoopInfo,FileStructureModel structureModel,String hadoopFileFormat,String fileName, String valueDomain,
                                                 String scopeDomain, String categoryType, String resourceType,
                                                 String user,boolean isHDFileOperator, Locale locale) throws Exception {
        AnalyticConfiguration barChartAnalysisConfig =new BarChartAnalysisConfig(valueDomain,scopeDomain,categoryType);

        HadoopBarChartAnalyzer barchartAnalyzer=new HadoopBarChartAnalyzer();
        AnalysisFileStructureModel analysisModel =  structureModel;

        HadoopAnalyticSource source = new HadoopAnalyticSource(hadoopInfo);
        //source.setPath(fileName);
        fileName = VisualUtils.refineFileName(fileName,user,isHDFileOperator);
        source.setFileName(fileName);
        source.setFileFormat(hadoopFileFormat);
        source.setNameAlias("bar chart");
        source.setHadoopFileStructureModel(analysisModel);
        source.setAnalyticConfiguration(barChartAnalysisConfig);
        String inputName = "File"+System.currentTimeMillis();
        source.setInputTempName(inputName);

        barchartAnalyzer.setAnalyticSource(source);

        AnalyticContext context = new AnalyticContext();
        setPigLocalMode(context,fileName,hadoopInfo) ;
        loadFile4Pig(inputName,fileName,context,hadoopInfo,structureModel);
        barchartAnalyzer.setContext(context);

        VisualizationModel visualModel = runAnalyzerForVisual(source, barchartAnalyzer,fileName ,resourceType,user,locale);
        visualModel.setTitle(fileName);
        context.dispose();
        return visualModel;
    }

    private void setPigLocalMode(AnalyticContext context, String filePath,
			HadoopConnection connection) {
    	boolean isLocalMode = true;
		 	
			 try {
				 isLocalMode = HadoopHDFSFileManager.INSTANCE.isLocalModelNeeded( filePath, connection);
					
			}catch (Exception e) {
				e.printStackTrace();			
			}	
			 
			 
			 context.setLocalModelPig(isLocalMode) ;
	}

	public VisualizationModel getScatterPlotMatrixVModel(HadoopConnection hadoopInfo,FileStructureModel structureModel,
                                                         String hadoopFileFormat,String fileName, String columnNameIndex,
                                                         String resourceType,String user,boolean isHDFileOperator,Locale locale) throws Exception {
        TableScatterMatrixConfig config = new TableScatterMatrixConfig(columnNameIndex);
        HadoopScatterPlotMatrixAnalyzer analyzer = new HadoopScatterPlotMatrixAnalyzer();
        AnalysisFileStructureModel analysisModel =  structureModel;

        HadoopAnalyticSource source = new HadoopAnalyticSource(hadoopInfo);
        //source.setPath(fileName);
        fileName = VisualUtils.refineFileName(fileName,user,isHDFileOperator);
        source.setFileName(fileName);
        source.setFileFormat(hadoopFileFormat);
        source.setNameAlias("Scatter Plot Matrix");
        source.setHadoopFileStructureModel(analysisModel);
        source.setAnalyticConfiguration(config);
        String inputName = "File" + System.currentTimeMillis();
        source.setInputTempName(inputName);

        analyzer.setAnalyticSource(source);

        AnalyticContext context = new AnalyticContext();
        setPigLocalMode(context,fileName,hadoopInfo) ;

        loadFile4Pig(inputName, fileName, context, hadoopInfo, structureModel);
        analyzer.setContext(context);

        VisualizationModel visualModel = runAnalyzerForVisual(source, analyzer, fileName,resourceType,user,locale);
        visualModel.setTitle(fileName);
        context.dispose();
        return visualModel;
    }
    public VisualizationModel getHistogramVModel(HadoopConnection hadoopInfo, FileStructureModel structureModel,
                                                 String hadoopFileFormat, String fileName,
                                                 AnalysisColumnBin[] columnBinArray, String resourceType,String user,boolean isHDFileOperator, Locale locale) throws Exception {

        HistogramAnalysisConfig config = new HistogramAnalysisConfig();
        AnalysisColumnBinsModel columnBinModel = new AnalysisColumnBinsModel(Arrays.asList(columnBinArray));
        config.setColumnBinModel(columnBinModel);

        HadoopHistogramAnalyzer analyzer = new HadoopHistogramAnalyzer();

        AnalysisFileStructureModel analysisModel =  structureModel;

        HadoopAnalyticSource source = new HadoopAnalyticSource(hadoopInfo);
        //source.setPath(fileName);
        fileName = VisualUtils.refineFileName(fileName,user,isHDFileOperator);
        source.setFileName(fileName);
        source.setFileFormat(hadoopFileFormat);
        source.setNameAlias("Histogram");
        source.setHadoopFileStructureModel(analysisModel);
        source.setAnalyticConfiguration(config);
        String inputName = "File" + System.currentTimeMillis();
        source.setInputTempName(inputName);

        analyzer.setAnalyticSource(source);

        AnalyticContext context = new AnalyticContext();
        setPigLocalMode(context,fileName,hadoopInfo) ;

        loadFile4Pig(inputName, fileName, context, hadoopInfo, structureModel);
        analyzer.setContext(context);

        VisualizationModel visualModel = runAnalyzerForVisual(source, analyzer, fileName, resourceType,user,locale);
        visualModel.setTitle(fileName);
        context.dispose();
        return visualModel;
    }

    public VisualizationModel getFrequencyVModel(HadoopConnection hadoopInfo, FileStructureModel structureModel,
                                                 String hadoopFileFormat, String fileName,String columnNameIndex,
                                                  String resourceType,String user,boolean isHDFileOperator, Locale locale) throws Exception {

        FrequencyAnalysisConfig config = new FrequencyAnalysisConfig();
        config.setColumnNames(columnNameIndex);
        HadoopFrequencyAnalysisAnalyzer analyzer = new HadoopFrequencyAnalysisAnalyzer();

        AnalysisFileStructureModel analysisModel =  structureModel;

        HadoopAnalyticSource source = new HadoopAnalyticSource(hadoopInfo);
        //source.setPath(fileName);
        fileName = VisualUtils.refineFileName(fileName,user,isHDFileOperator);
        source.setFileName(fileName);
        source.setFileFormat(hadoopFileFormat);
        source.setNameAlias("Frequency");
        source.setHadoopFileStructureModel(analysisModel);
        source.setAnalyticConfiguration(config);
        String inputName = "File" + System.currentTimeMillis();
        source.setInputTempName(inputName);

        analyzer.setAnalyticSource(source);

        AnalyticContext context = new AnalyticContext();
        setPigLocalMode(context,fileName,hadoopInfo) ;

        loadFile4Pig(inputName, fileName, context, hadoopInfo, structureModel);
        analyzer.setContext(context);

        VisualizationModel visualModel = runAnalyzerForVisual(source, analyzer, fileName, resourceType,user,locale);
        visualModel.setTitle(fileName);
        context.dispose();
        return visualModel;
    }

    public VisualizationModel getStatisticsVModel(HadoopConnection hadoopInfo, FileStructureModel structureModel,
                                                 String hadoopFileFormat, String fileName,String columnNameIndex,
                                                  String resourceType,String user,boolean isHDFileOperator, Locale locale) throws Exception {

        ValueAnalysisConfig config = new ValueAnalysisConfig();
        config.setColumnNames(columnNameIndex);
        HadoopValueAnalysisAnalyzer analyzer = new HadoopValueAnalysisAnalyzer();

        AnalysisFileStructureModel analysisModel =  structureModel;

        HadoopAnalyticSource source = new HadoopAnalyticSource(hadoopInfo);
        //source.setPath(fileName);
        fileName = VisualUtils.refineFileName(fileName,user,isHDFileOperator);
        source.setFileName(fileName);
        source.setFileFormat(hadoopFileFormat);
        source.setNameAlias("Summary Statistics");
        source.setHadoopFileStructureModel(analysisModel);
        source.setAnalyticConfiguration(config);
        String inputName = "File" + System.currentTimeMillis();
        source.setInputTempName(inputName);

        analyzer.setAnalyticSource(source);

        AnalyticContext context = new AnalyticContext();
        setPigLocalMode(context,fileName,hadoopInfo) ;

        loadFile4Pig(inputName, fileName, context, hadoopInfo, structureModel);
        analyzer.setContext(context);

        VisualizationModel visualModel = runAnalyzerForVisual(source, analyzer, fileName, resourceType,user,locale);
        visualModel.setTitle(fileName);
        context.dispose();
        return visualModel;
    }

    private void loadFile4Pig(String inputName, String fileName, AnalyticContext context,
                              HadoopConnection hadoopConn,FileStructureModel fileStructureModel) throws Exception {
//        List<String> columnNameList = fileStructureModel.getColumnNameList();
//        List<String> columnTypeList = fileStructureModel.getColumnTypeList();
//        PigServer pigServer = context.getPigServer(hadoopConn);
//        StringBuffer header=new StringBuffer();
//        for(int i=0;i<columnNameList.size();i++){
//            header.append(columnNameList.get(i)).append(":");
//            header.append(HadoopDataType.getTransferDataType(columnTypeList.get(i)));
//            header.append(",");
//        }
//        if(header.length()>0){
//            header=header.deleteCharAt(header.length()-1);
//        }
//        String fileURI="hdfs://"+hadoopConn.getHdfsHostName()+":"+hadoopConn.getHdfsPort()+fileName;
//        //   this is for the filter of the header
//
//        String  realDelimiter = HadoopUtility.getDelimiterValue(fileStructureModel);
//        String script = inputName+" = load '"+fileURI+ "' USING PigStorage('"+  realDelimiter +"') " +
//                " as ("+header+");";
//        if(fileStructureModel.getIncludeHeader().equalsIgnoreCase("true")){
//            String headerLine = HadoopHDFSFileManager.INSTANCE.readHadoopFileToString(fileName, hadoopConn, 1);
//
//            script = inputName+" = load '"+fileURI+ "' USING PigStorage('"+realDelimiter +"','"+headerLine+"') " +
//                    " as ("+header+");";
//        }
//        //load pig file ...
//        pigServer.registerQuery(script);
        
        
        HadoopFileSelectorConfig fileSelectorConfig = new HadoopFileSelectorConfig();
         
        fileSelectorConfig.setHadoopFileName(fileName);
        fileSelectorConfig.setHdfsHostname(hadoopConn.getHdfsHostName());
        fileSelectorConfig.setHadoopFileStructure(fileStructureModel);
        fileSelectorConfig.setConnName(hadoopConn.getConnName());
        fileSelectorConfig.setUserName(hadoopConn.getUserName());
        fileSelectorConfig.setGroupName(hadoopConn.getGroupName());
        fileSelectorConfig.setHdfsHostname(hadoopConn.getHdfsHostName());
        fileSelectorConfig.setHdfsPort(Integer.toString(hadoopConn.getHdfsPort()));
        fileSelectorConfig.setHadoopVersion(hadoopConn.getVersion());
        fileSelectorConfig.setJobHostname(hadoopConn.getJobHostName());
        fileSelectorConfig.setJobPort(Integer.toString(hadoopConn.getJobPort()));
        fileSelectorConfig.setSecurityMode(hadoopConn.getSecurityMode()) ;
        fileSelectorConfig.setHdfsPrincipal(hadoopConn.getHdfsPrincipal()) ;
        fileSelectorConfig.setHdfsKeyTab(hadoopConn.getHdfsKeyTab())  ;
        fileSelectorConfig.setMapredPrincipal(hadoopConn.getMapredPrincipal()) ; 
        fileSelectorConfig.setMapredKeyTab(hadoopConn.getMapredKeyTab()) ;
        
        HadoopFileSelector fileSelector = new HadoopFileSelector();
        fileSelector.setContext(context);
        fileSelector.loadFileToPigServer(fileSelectorConfig, inputName) ;
    }
}
