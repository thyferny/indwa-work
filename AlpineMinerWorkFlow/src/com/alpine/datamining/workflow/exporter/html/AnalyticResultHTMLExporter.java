/**
 * ClassName  AnalyticResultHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jfree.chart.JFreeChart;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticFlowMetaInfo;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticResult;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.AbstractSVMConfig;
import com.alpine.datamining.api.impl.algoconf.AdaboostConfig;
import com.alpine.datamining.api.impl.algoconf.AggregateConfig;
import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.algoconf.FPGrowthConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopAggregaterConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPigExecuteConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopVariableConfig;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.LinearRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.ReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.SQLAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.TableJoinConfig;
import com.alpine.datamining.api.impl.algoconf.TableSetConfig;
import com.alpine.datamining.api.impl.algoconf.VariableConfig;
import com.alpine.datamining.api.impl.algoconf.VariableSelectionConfig;
import com.alpine.datamining.api.impl.db.DBTableSelector;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.model.association.AnalysisExpressionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionFile;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModelItem;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionSourceColumn;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayersModel;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinTable;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisTableJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisColumnMap;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisTableSetModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBin;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.ClusterAllVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DataTableVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DataTextAndTableListVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.JFreeChartImageVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.LinearRegressionNormalProbabilityPlotVisualization;
import com.alpine.datamining.api.impl.visual.LinearRegressionResidualPlotVisualization;
import com.alpine.datamining.api.impl.visual.LinearRegressionTableVisualizationType;
import com.alpine.datamining.api.impl.visual.LogisticRegressionTextAndTableVisualizationType;
import com.alpine.datamining.api.impl.visual.TreeVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguageConfig;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceItem;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceModel;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.operator.regressions.LogisticRegressionGroupModel;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.NLSUtility;


/**
 * @author John Zhao
 * 
 */
public class AnalyticResultHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(AnalyticResultHTMLExporter.class);
    private static final String COLUMN = "column";

	private static final String ul_ID = "category";

	//url.....
	public static HashMap<String , Connection> connectionMap =new HashMap<String , Connection>();
	
	private final static String recFolderName ="resource";
	private final static String contentFileName ="content";
	private final static String indexFileName ="index";
	private final static String suffix =".html";
	private final static String allContentFile =contentFileName+suffix;
	private final static String allIndexFile =indexFileName+suffix;
	private final static String overview ="overview";
	
	private final static Locale locale=Locale.getDefault();
	
	public static void exportToFile(AnalyticResult result, String outputFile  
		 ) throws Exception{
		List<String> tempFileList = new ArrayList<String>();
		itsLogger.info("Export the result in to file:"+outputFile); //$NON-NLS-1$
		try {		
			String contentFile=getContentPath(outputFile);
			String indexFile=getIndexPath(contentFile);
			
			createContentHtml(result, outputFile, tempFileList, contentFile);
			
			createIndexHtml(result,indexFile);
			
			createFrame(outputFile,contentFile);
			
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
 			throw new RuntimeException(e);
		} finally {
			closeConenctions();
		}
	}



	private static void createIndexHtml(AnalyticResult result,String outputFile) throws IOException {
//		createImgFolder(outputFile);	
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		htmlWriter.writeDoctype();
		htmlWriter.writeBeginHtml();
		htmlWriter.writeHead();
		htmlWriter.writeBeginBody();
		
		String overViewIndex=addLi(addSpanWithA("content.html#"+overview,WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.RPT_Overview_chapter,locale)));
		StringBuilder sb_ol=new StringBuilder();
		sb_ol.append(overViewIndex);
	
		int i = 1;
		List<AnalyticOutPut> outPuts = result.getOutPuts();

		for (Iterator<AnalyticOutPut> iterator = outPuts.iterator(); iterator.hasNext();) {
			AnalyticOutPut analyticOutPut = iterator
					.next();		
			String nodeName = analyticOutPut.getAnalyticNode().getName();
			String nodeNum=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node,locale) + i;
			String rootName=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node,locale) + i + ":" + nodeName;
			String rootOverview=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node,locale) + i + overview;
			String rootOverviewName=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OverView,locale);
			String input=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Input,locale);
			String output=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OutPut,locale);
			
			String nodeNameIndex=addSpanWithA("content.html#"+nodeNum,rootName);
//			sb_ol.append(addSpanWithA("content.html#"+nodeNum,rootName));
			ToHtmlWriter ulNodeWriter=new ToHtmlWriter();
			String nodeOverViewIndex=createIndexLI("content.html#"+rootOverview,rootOverviewName,4);
			String nodeInputIndex="";
			String nodeOutputIndex="";
			if (needShowInPut(analyticOutPut)){
				nodeInputIndex=createIndexLI("content.html#"+input+"_"+i,input,4);
			}
			if (needShowoutPut(analyticOutPut)){
				nodeOutputIndex=addSpanWithA("content.html#"+output+"_"+i,output);
			}		
			ToHtmlWriter ulOutputWriter=new ToHtmlWriter();
			if (needShowoutPut(analyticOutPut) == false)
				continue;
			
			List<VisualizationOutPut> vOuts = getAllVisualizationOutPuts(analyticOutPut);
			
			splitModelHandling(analyticOutPut, htmlWriter, vOuts,true);
			
			StringBuilder sb=new StringBuilder();
			int[] j = {1};
			genetateOutputIndex(i,j, output, vOuts, sb);
			ulOutputWriter.writeUL(sb.toString());
			ulNodeWriter.writeUL(nodeOverViewIndex+nodeInputIndex+addLi(nodeOutputIndex+ulOutputWriter.toString()));	
			sb_ol.append(addLi(nodeNameIndex+ulNodeWriter.toString()));
			i++;
		}
		htmlWriter.writeULWithId(sb_ol.toString(), ul_ID);
		htmlWriter.writeEndBody();
		htmlWriter.writeEndHtml();
		writeFile(outputFile,htmlWriter.toString());
	}



	private static int[] genetateOutputIndex(int i, int[] j,String output,
			List<VisualizationOutPut> vOuts, StringBuilder sb) {
		for (Iterator<VisualizationOutPut> iter= vOuts.iterator(); iter.hasNext();) {				
			VisualizationOutPut visualizationOutPut =  iter
					.next();
			if (visualizationOutPut instanceof CompositeVisualizationOutPut){
				List<VisualizationOutPut> vChildsOuts=((CompositeVisualizationOutPut) visualizationOutPut).getChildOutPuts();
				genetateOutputIndex(i,j, output, vChildsOuts, sb);	
			}else if(visualizationOutPut instanceof ClusterAllVisualizationOutPut){
				j[0]++;
			}
			else if (visualizationOutPut != null) {
				String outputName=visualizationOutPut.getName();
				String outputIndex=createIndexLI("content.html#"+output+"_"+i+"_"+j[0],outputName,5);
				sb.append(outputIndex);
				j[0]++;
			} 		
		}
		return j;
	}

	private static String createIndexLI(String str,String str1,int h) {
		ToHtmlWriter liWriter=new ToHtmlWriter();
//		ToHtmlWriter hWriter=new ToHtmlWriter();
		ToHtmlWriter aWriter=new ToHtmlWriter();
		aWriter.writeA(str,str1, contentFileName);
//		switch (h){
//		case 3:hWriter.writeH3(aWriter.toString());break;
//		case 4:hWriter.writeH4(aWriter.toString());break;
//		case 5:hWriter.writeH5(aWriter.toString());break;
//		}	
		liWriter.writeLI(aWriter.toString());
		return liWriter.toString();	
	}

	private static String addSpanWithA(String str,String str1){
		ToHtmlWriter spanWriter=new ToHtmlWriter();
		ToHtmlWriter aWriter=new ToHtmlWriter();
		aWriter.writeA(str,str1, contentFileName);
		spanWriter.writeSpan(aWriter.toString());
		return spanWriter.toString();	
	}
	private static String addLi(String str){
		ToHtmlWriter LiWriter=new ToHtmlWriter();
		LiWriter.writeLI(str);
		return LiWriter.toString();	
	}

	private static void createContentHtml(AnalyticResult result,
			String outputFile, List<String> tempFileList, String contentFile)
			throws Exception, IOException {
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		htmlWriter.writeDoctype();
		htmlWriter.writeBeginHtml();
		htmlWriter.setTitle(contentFile);
		htmlWriter.writeHead();
		htmlWriter.writeBeginBody();
		
		createOverViewChapter( result.getAnalyticMetaInfo(), htmlWriter);

		int i = 1;
		List<AnalyticOutPut> outPuts = result.getOutPuts();

		for (Iterator<AnalyticOutPut> iterator = outPuts.iterator(); iterator.hasNext();) {
			AnalyticOutPut analyticOutPut =  iterator
					.next();
			createCharpter4Node(htmlWriter, i, analyticOutPut,tempFileList,contentFile);		
			i++;
		}
		
		htmlWriter.writeEndBody();
		htmlWriter.writeEndHtml();

		writeFile(contentFile, htmlWriter.toString());
	}



	private static void writeFile(String fileName, String content)
			throws IOException {
		File file= new File(fileName);
		FileOutputStream iStream=null;
		try {
			iStream = new FileOutputStream(file);
			iStream.write(content.getBytes(ToHtmlWriter.getCharSet()));
		} catch ( Exception e) {
			e.printStackTrace();
		}finally{
			iStream.close();
		}
		
	}



	private static void createFrame(String outputFile,String contentFile) throws IOException {
		int i = contentFile.lastIndexOf(File.separator);
		String curdir = contentFile.substring(0, i);
		i = curdir.lastIndexOf(File.separator);
		String folderName=contentFile.substring(i+1, curdir.length());
		
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		htmlWriter.writeDoctype();
		htmlWriter.writeBeginHtml();
		String title=outputFile.substring(outputFile.lastIndexOf(File.separator)+1, outputFile.length());
		htmlWriter.setTitle(title);
		htmlWriter.writeHead();
		
		String[] percent={"15%","85%"};
		ToHtmlWriter frameIndexWriter=new ToHtmlWriter();
		frameIndexWriter.writeFrame(indexFileName, folderName+Resources.SLASH+allIndexFile);
		ToHtmlWriter frameContentWriter=new ToHtmlWriter();
		frameContentWriter.writeFrame(contentFileName, folderName+Resources.SLASH+allContentFile);
		String[] frame={frameIndexWriter.toString(),frameContentWriter.toString()};
		htmlWriter.writeFrameSet(percent, frame);
		
		htmlWriter.writeEndHtml();
		
		writeFile(outputFile,htmlWriter.toString());
		
	}

	


	private static String getContentPath(String outputFile) {
		String curdir = createRecFolder(outputFile);
		return curdir+File.separator+contentFileName+suffix;
	}
	private static String getIndexPath(String outputFile) {
		int i = outputFile.lastIndexOf(File.separator);
		String curdir = outputFile.substring(0, i);
		return curdir+File.separator+indexFileName+suffix;
	}


	private static String createRecFolder(String outputFile) {
		int i = outputFile.lastIndexOf(File.separator);
		String curdir = outputFile.substring(0, i);
		String fileName=outputFile.substring(i+1,outputFile.lastIndexOf("."));
		curdir=curdir+File.separator+fileName+"_"+recFolderName;
		File file= new File(curdir);
		if(file.exists()){
			curdir=curdir+"_"+System.currentTimeMillis();
			File fileNew= new File(curdir);
			fileNew.mkdir();
		}else{
			file.mkdir();
		}
		return curdir;
	}
//	private static String createImgFolder(String outputFile) {
//		int i = outputFile.lastIndexOf(File.separator);
//		String curdir = outputFile.substring(0, i);
//		String fileName=outputFile.substring(i+1,outputFile.lastIndexOf("."));
//		curdir=curdir+File.separator+fileName+"_"+imgFolderName;
//		File file= new File(curdir);
//		if(!file.exists()){
//			file.mkdir();
//			
//		}
//		return curdir;
//	}


	/**
	 * 
	 */
	private static void closeConenctions() {
		Collection<Connection> conns = AnalyticResultHTMLExporter.connectionMap.values();
		for (Iterator<Connection> iterator = conns.iterator(); iterator.hasNext();) {
			Connection connection =  iterator.next();
			try {
				if(connection!=null&&connection.isClosed()==false){
				
					connection.close();
				} 
			}catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
			}
		}
		AnalyticResultHTMLExporter.connectionMap.clear();
	 
		
	}


	/**
	 * @param result
	 * @param outputDir
	 */
	public static void exportToDir(AnalyticResult result, String outputDir) throws Exception{
		AnalyticFlowMetaInfo metaInfo = result.getAnalyticMetaInfo();
		String flowName = metaInfo.getFlowFileName();


		File file=new File(outputDir);
		if(file.exists()==false){
			file.mkdir();
			itsLogger.info("Output directory \""+outputDir+"\" does not exist.");//, create it:"+outputDir); //$NON-NLS-1$
			
		}
		String outputFileName=outputDir+File.separator
			+ flowName.substring(0, flowName.lastIndexOf("."))+ ".html"; //$NON-NLS-1$ //$NON-NLS-2$
		
		exportToFile(  result,   outputFileName );

		 

	}


	private static void createOverViewChapter(AnalyticFlowMetaInfo metaInfo,
			ToHtmlWriter htmlWriter)  {

		try{
			ToHtmlWriter h1Writer=new ToHtmlWriter();
			h1Writer.writeH1(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.RPT_Overview_chapter,locale));
			htmlWriter.writeAnchor(overview, h1Writer.toString());
			writeFlowOverView(metaInfo,htmlWriter);
			htmlWriter.writePageSplit();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private static void writeFlowOverView(AnalyticFlowMetaInfo metaInfo,ToHtmlWriter htmlWriter) {
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Flow_Name,locale) + metaInfo.getFlowFileName());
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Flow_Owner,locale) + StringUtil.filterEmptyString( //$NON-NLS-1$
				metaInfo.getFlowOwnerUser()));
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Flow_Description,locale) + StringUtil.filterEmptyString( //$NON-NLS-1$
				metaInfo.getFlowDescription()));
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Execute_User,locale) + metaInfo.getExecuteUserName());
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Start_From,locale) + metaInfo.getStartTime());
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_End_to,locale) + metaInfo.getEndTime());
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Engine_Name,locale) + StringUtil.filterEmptyString( //$NON-NLS-1$
				metaInfo.getAnalyticApplicationName()));
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_CopyRight,locale) + StringUtil.filterEmptyString( //$NON-NLS-1$
				metaInfo.getCopyRightInfo()));
		Properties props=metaInfo.getAnalyticServerConfig();
		if(props!=null){
			String blank="         "; //$NON-NLS-1$
			htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Server_Configuration,locale));
			Set<Object> keys = props.keySet();
			for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value=(String)props.get(key);
				htmlWriter.writeP(blank+key+" = "+value);
			}
		}
	}

	private static void writeNodeOverView(AnalyticNodeMetaInfo nodeInfo,ToHtmlWriter htmlWriter) {
		if (nodeInfo == null)
			return ; //$NON-NLS-1$

		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node_name,locale) + nodeInfo.getName()); 
		if (nodeInfo.getAlgorithmName() != null) {
			htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Algorithm_name,locale) + nodeInfo.getAlgorithmName());
		}
		if (nodeInfo.getAlgorithmDescription() != null) {

			htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Algorithm_Description,locale) //$NON-NLS-1$
					+ nodeInfo.getAlgorithmDescription());
		}
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Start_From,locale) + nodeInfo.getStartTime());
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_End_to,locale) + nodeInfo.getEndTime());
		
		Properties props = nodeInfo.getProperties();
		if (props!=null){
			 Set<Object> keys = props.keySet();
			 for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				Object key = (Object) iterator.next();
				Object value=props.get(key);
				htmlWriter.writeP(key+": " +value);
			} 
		}
	}

	private static void createCharpter4Node(ToHtmlWriter htmlWriter, int i,
			AnalyticOutPut analyticOutPut, List<String> tempFileList,String rootPath ) throws  Exception  {
		String nodeName = analyticOutPut.getAnalyticNode().getName();
		itsLogger.debug("createCharpter4Node:nodeNmae="+nodeName); //$NON-NLS-1$
		ToHtmlWriter h2Writer=new ToHtmlWriter();
		String rootName=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node,locale) + i;
		h2Writer.writeH2(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node,locale) + i + ":" + nodeName);
		htmlWriter.writeAnchor(rootName, h2Writer.toString());

		createNodeOverViewSection(analyticOutPut, htmlWriter,i);
		createNodeInPutSection(analyticOutPut, htmlWriter,i);
		createNodeOutPutSection(analyticOutPut, htmlWriter,tempFileList ,rootPath,i);
		htmlWriter.writePageSplit();

	}

	private static void createNodeOutPutSection(AnalyticOutPut analyticOutPut,
			ToHtmlWriter htmlWriter, List<String> tempFileList, String rootPath,int i )   throws  Exception {
		if (needShowoutPut(analyticOutPut) == false)
			return;
		
		ToHtmlWriter h2Writer=new ToHtmlWriter();
		String output=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OutPut,locale);
		h2Writer.writeH2(output);
		htmlWriter.writeAnchor(output+"_"+i, h2Writer.toString());
		List<VisualizationOutPut> vOuts=new ArrayList<VisualizationOutPut>();
		getAllVisualizationOutPuts(analyticOutPut.getVisualizationOutPut(),vOuts);
		
		splitModelHandling(analyticOutPut, htmlWriter, vOuts,false);
		
		int j=1;
		for (Iterator<VisualizationOutPut> iterator = vOuts.iterator(); iterator.hasNext();) {
			
			VisualizationOutPut visualizationOutPut =  iterator
					.next();
			if (visualizationOutPut instanceof CompositeVisualizationOutPut){

			}else if(visualizationOutPut instanceof ClusterAllVisualizationOutPut){
				
			}
			else if (visualizationOutPut != null) {
				createNodeOutPutSection(visualizationOutPut, htmlWriter,tempFileList ,rootPath,i,j);
			} 
			j++;
		}

	}



	private static void splitModelHandling(AnalyticOutPut analyticOutPut,
			ToHtmlWriter htmlWriter, List<VisualizationOutPut> vOuts,boolean isInIndex) {
		if(analyticOutPut instanceof AnalyzerOutPutTrainModel){
			EngineModel engineModel = ((AnalyzerOutPutTrainModel)analyticOutPut).getEngineModel();
			String modelType = engineModel.getModelType();
			if(modelType.equals(EngineModel.MPDE_TYPE_LR_SPLITMODEL)){
				Model model = engineModel.getModel();
				lrSplitModelHandling(htmlWriter, vOuts, model,isInIndex);
			}else if(modelType.equals(EngineModel.MPDE_TYPE_LIR_SPLITMODEL)){
				Model model = engineModel.getModel();
				lirSplitModelHandling(htmlWriter, vOuts, model,isInIndex);
			}
		}
	}



	private static void lirSplitModelHandling(ToHtmlWriter htmlWriter,
			List<VisualizationOutPut> vOuts, Model model, boolean isInIndex) {
		if(model instanceof LinearRegressionGroupGPModel){
			vOuts.remove(1);//remove the first one,then add all
			vOuts.remove(1);
			vOuts.remove(1);
			Map<String, LinearRegressionModelDB> modelList = ((LinearRegressionGroupGPModel)model).getModelList();
			Iterator<Entry<String, LinearRegressionModelDB>> iter = modelList.entrySet().iterator();
			int count=0;
			while(iter.hasNext()){
				if(count>=Integer.parseInt(VisualLanguageConfig.SPLITMODEL_GROUP_LIMIT)){
					if(isInIndex==false){
						String warning = NLSUtility.bind(WorkFlowLanguagePack.getMessage(
								WorkFlowLanguagePack.SPLITMODEL_TOOMANYGROUP_WARNING,locale), VisualLanguageConfig.SPLITMODEL_GROUP_LIMIT);
						htmlWriter.writeH3(warning);
					}			
					break;
				}
				VisualizationOutPut vOut = null;
				VisualizationOutPut rChartVOut=null;
				VisualizationOutPut nChartVOut=null;
				Entry<String, LinearRegressionModelDB> entry = iter.next();
				String distinctValue = entry.getKey();
				LinearRegressionModelDB splitModel = entry.getValue();
				String dependentColumn = splitModel.getLabel().getName();
				if(isInIndex==true){		
					vOut=new DataTableVisualizationOutPut();
					rChartVOut=new DataTableVisualizationOutPut();
					nChartVOut=new DataTableVisualizationOutPut();
				}else{
					TextAndTableListEntity te = LinearRegressionTableVisualizationType.getVTextTable(splitModel);
					vOut=new DataTextAndTableListVisualizationOutPut(te);
					
					JFreeChart rChart = LinearRegressionResidualPlotVisualization.
							generateResidualPlot(dependentColumn, splitModel.getResiduals());
					rChartVOut=new JFreeChartImageVisualizationOutPut(rChart);
					
					JFreeChart nChart = LinearRegressionNormalProbabilityPlotVisualization.
							generateNormalProbabilityPlot(dependentColumn, splitModel.getResiduals(), splitModel.getS());
					nChartVOut=new JFreeChartImageVisualizationOutPut(nChart);
				}
				vOut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+distinctValue
						+":"+VisualLanguagePack.getMessage(VisualLanguagePack.DATA_TITLE,locale));
				rChartVOut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+distinctValue
						+":"+VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUALPLOT_TITLE,locale));
				nChartVOut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+distinctValue
						+":"+VisualLanguagePack.getMessage(VisualLanguagePack.Q_Q_PLOT_TITLE,locale));
				
				vOuts.add(vOut);
				vOuts.add(rChartVOut);
				vOuts.add(nChartVOut);
				
				count++;
			}
		}
		
	}



	private static void lrSplitModelHandling(ToHtmlWriter htmlWriter,
			List<VisualizationOutPut> vOuts, Model model, boolean isInIndex) {
		if(model instanceof LogisticRegressionGroupModel){
			vOuts.remove(1);//remove the first one,then add all
			Map<String, LogisticRegressionModelDB> modelList = 
					((LogisticRegressionGroupModel)model).getModelList();
			Iterator<Entry<String, LogisticRegressionModelDB>> iter = modelList.entrySet().iterator();
			int count=0;
			while(iter.hasNext()){
				if(count>=Integer.parseInt(VisualLanguageConfig.SPLITMODEL_GROUP_LIMIT)){
					if(isInIndex==false){
						String warning = NLSUtility.bind(WorkFlowLanguagePack.getMessage(
								WorkFlowLanguagePack.SPLITMODEL_TOOMANYGROUP_WARNING,locale), VisualLanguageConfig.SPLITMODEL_GROUP_LIMIT);
						htmlWriter.writeH3(warning);
					}			
					break;
				}
				DataTableVisualizationOutPut vOut = null;
				Entry<String, LogisticRegressionModelDB> entry = iter.next();
				if(isInIndex==true){		
					vOut=new DataTableVisualizationOutPut();					
				}else{
					TableEntity te = LogisticRegressionTextAndTableVisualizationType.createTable(entry.getValue());
					vOut=new DataTableVisualizationOutPut(te);
				}
				vOut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+entry.getKey());
				vOuts.add(vOut);
				count++;
			}
		}
	}

 

	/**
	 * @param visualizationOutPut
	 * @param rootPath 
	 * @param htmlWriter
	 * @param tempFileList
	 */
	private static void createNodeOutPutSection (VisualizationOutPut visualizationOutPut,
			ToHtmlWriter htmlWriter, List<String> tempFileList, String rootPath,int i,int j )   throws  Exception {
		ToHtmlWriter h2Writer=new ToHtmlWriter();
		String output=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OutPut,locale);
		h2Writer.writeH2(visualizationOutPut.getName());
		htmlWriter.writeAnchor(output+"_"+i+"_"+j, h2Writer.toString());
		if(visualizationOutPut instanceof JFreeChartImageVisualizationOutPut
				||visualizationOutPut instanceof TreeVisualizationOutPut){
		}

		// then put image or table here... csv
		VisualOutPutHTMLExporter  vExporter=getVisulExporter(visualizationOutPut);
		//can be paragraphy and table...
		StringBuffer contents=vExporter.export(visualizationOutPut,tempFileList,rootPath);
		htmlWriter.writeP(contents.toString());
	}

	/**
	 * @param vOutPut
	 * @return
	 */
	private static VisualOutPutHTMLExporter getVisulExporter(
			VisualizationOutPut vOutPut) {

		return HTMLExporterFactory.getInstance().getVisualOutPutExporter(vOutPut );
	}

	/**
	 * @param analyticOutPut
	 * @return
	 */
	private static List<VisualizationOutPut> getAllVisualizationOutPuts(
			AnalyticOutPut analyticOutPut) {

		VisualizationOutPut out = analyticOutPut.getVisualizationOutPut();
		if (out instanceof CompositeVisualizationOutPut) {
			return ((CompositeVisualizationOutPut) out).getChildOutPuts();
		} else {
			List<VisualizationOutPut> outPuts = new ArrayList<VisualizationOutPut>();
			outPuts.add(out);
			return outPuts;
		}

	}

	private static void getAllVisualizationOutPuts(
			VisualizationOutPut out,List<VisualizationOutPut> outPuts) {
		if (out instanceof CompositeVisualizationOutPut) {
			List<VisualizationOutPut>  compositeOutPuts=((CompositeVisualizationOutPut) out).getChildOutPuts();
			for(VisualizationOutPut output:compositeOutPuts){
				getAllVisualizationOutPuts(output,outPuts);
			}
		} else {
			outPuts.add(out);
		}

	}
	
	/**
	 * @param analyticOutPut
	 * @return
	 */
	private static boolean needShowoutPut(AnalyticOutPut analyticOutPut) {
		return true;
	}

	private static void createNodeInPutSection(AnalyticOutPut analyticOutPut,
			ToHtmlWriter htmlWriter,int i) {
		if (needShowInPut(analyticOutPut) == false)
			return;
		ToHtmlWriter h2Writer=new ToHtmlWriter();		
		String input=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Input,locale);
		h2Writer.writeH2(input);
		htmlWriter.writeAnchor(input+"_"+i, h2Writer.toString());

		writeNodeInputString(analyticOutPut.getAnalyticNode(),htmlWriter);
			

	}

	/**
	 * @param analyticNode
	 * @param htmlWriter
	 * @return
	 */
	private static void writeNodeInputString(AnalyticNode analyticNode,ToHtmlWriter htmlWriter) {	
		List<AnalyticSource> sourceList=analyticNode.getAllSources();
		if(sourceList==null||sourceList.size()==0)return;
		if(sourceList.size()>1){
			Iterator<AnalyticSource>  iter=sourceList.iterator();
			while(iter.hasNext()){
				AnalyticSource source=iter.next();
				writeSourceInfo(htmlWriter, source);
			}
		}else{
			writeSourceInfo(htmlWriter, analyticNode.getSource());
		}
		AnalyticSource  analyticSource =analyticNode.getSource();			
		writeParaInfo(htmlWriter, analyticSource);
	}



	private static void writeParaInfo(ToHtmlWriter htmlWriter,
			AnalyticSource analyticSource) {
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Param_information,locale));
		AnalyticConfiguration config=analyticSource.getAnalyticConfig();
		String[] configArray = setSimpleConfig(htmlWriter, config);
		
		if(config!=null&&config instanceof LogisticRegressionConfigGeneral
					&&((LogisticRegressionConfigGeneral)config).getInterActionModel()!=null){
			htmlWriter.writeP(InterActionColumnsModel.TAG_NAME+" = "+
					((LogisticRegressionConfigGeneral)config).getInterActionModel().toString());
		}else if(config!=null&&config instanceof LinearRegressionConfig
				&&((LinearRegressionConfig)config).getInterActionModel()!=null){
			htmlWriter.writeP(InterActionColumnsModel.TAG_NAME+" = "+
					((LinearRegressionConfig)config).getInterActionModel().toString());
		}else if(config!=null&&config instanceof CustomziedConfig){
			outputCustomizedColumn(htmlWriter, config, configArray);
		}else if(config!=null&&config instanceof VariableConfig){
			if(((VariableConfig)config).getQuantileModel()!=null){
				outputQuantileModel(htmlWriter, config);
			}
			if(((VariableConfig)config).getDerivedModel()!=null){
				htmlWriter.writeP(((VariableConfig)config).getDerivedModel().toString());
			}
		}else if(config!=null&&config instanceof AggregateConfig){
			if(((AggregateConfig)config).getAggregateFieldsModel()!=null){
				htmlWriter.writeP(((AggregateConfig)config).getAggregateFieldsModel().toString());
			}
			if(((AggregateConfig)config).getWindowFieldsModel()!=null){
				htmlWriter.writeP(((AggregateConfig)config).getWindowFieldsModel().toString());
			}
		}else if(config!=null&&config instanceof HadoopAggregaterConfig ){
			if(((HadoopAggregaterConfig)config).getAggregateFieldsModel()!=null){
				htmlWriter.writeP(((HadoopAggregaterConfig)config).getAggregateFieldsModel().toString());
			}
		}else if(config!=null&&config instanceof HadoopVariableConfig ){
			if(((HadoopVariableConfig)config).getDerivedModel()!=null){
				htmlWriter.writeP(((HadoopVariableConfig)config).getDerivedModel().toString());
			}
		}else if(config!=null&&config instanceof HistogramAnalysisConfig){
			if(((HistogramAnalysisConfig)config).getColumnBinModel()!=null){
				htmlWriter.writeP(AnalysisColumnBinsModel.TAG_NAME+" = "+
						((HistogramAnalysisConfig)config).getColumnBinModel().toString());
			}
		}else if(config!=null&&config instanceof ReplaceNullConfig){
			if(((ReplaceNullConfig)config).getNullReplacementModel()!=null){
				htmlWriter.writeP(AnalysisNullReplacementModel.TAG_NAME+" = "+
						((ReplaceNullConfig)config).getNullReplacementModel().toString());
			}
		}else if(config!=null&&config instanceof HadoopReplaceNullConfig){
			if(((HadoopReplaceNullConfig)config).getNullReplacementModel()!=null){
				htmlWriter.writeP(AnalysisNullReplacementModel.TAG_NAME+" = "+
						((HadoopReplaceNullConfig)config).getNullReplacementModel().toString());
			}
		}
		else if(config!=null&&config instanceof FPGrowthConfig){
			if(((FPGrowthConfig)config).getExpressionModel()!=null){
				htmlWriter.writeP(AnalysisExpressionModel.TAG_NAME+" = "+
						((FPGrowthConfig)config).getExpressionModel().toString());
			}
		}else if(config!=null&&config instanceof NeuralNetworkConfig){
			if(((NeuralNetworkConfig)config).getHiddenLayersModel()!=null){
				htmlWriter.writeP(AnalysisHiddenLayersModel.TAG_NAME+" = "+
						((NeuralNetworkConfig)config).getHiddenLayersModel().toString());
			}
		}else if(config!=null&&config instanceof TableJoinConfig
				&&((TableJoinConfig)config).getTableJoinDef()!=null){
			outputTableJointDefinition(htmlWriter, config);
		}else if(config!=null&&config instanceof HadoopJoinConfig
				&&((HadoopJoinConfig)config).getJoinModel()!=null){
			outputHadoopJoinDefinition(htmlWriter, config);
		} 
		else if(config!=null&&config instanceof TableSetConfig
				&&((TableSetConfig)config).getTableSetModel()!=null){
			outputTableSetDefinition(htmlWriter, (TableSetConfig)config);
		}else if(config!=null&&config instanceof HadoopUnionConfig
				&&((HadoopUnionConfig)config).getUnionModel()!=null){
			outputHadoopUnionDefinition(htmlWriter, (HadoopUnionConfig)config);
		} 
		else if(config!=null&&config instanceof HadoopPigExecuteConfig
				&&((HadoopPigExecuteConfig)config).getPigScriptModel()!=null){
			outputPigScriptnDefinition(htmlWriter, (HadoopPigExecuteConfig)config);
		} 
		else if(config!=null&&config instanceof AdaboostConfig&&
				!((AdaboostConfig)config).isListEmpty()){
			AdaboostConfig adaboostConfig=(AdaboostConfig)config;
			AnalysisAdaboostPersistenceModel adaboostPersistenceModel = adaboostConfig.getAdaboostUIModel();
			List<AnalysisAdaboostPersistenceItem> items = adaboostPersistenceModel.getAdaboostUIItems();
			for(AnalysisAdaboostPersistenceItem item:items){
				AbstractModelTrainerConfig singleConfig=adaboostConfig.getNameConfigMap().get(item.getAdaName());
				htmlWriter.writeH3(item.getAdaName()+":");
				setSimpleConfig(htmlWriter, singleConfig);
				if(config instanceof NeuralNetworkConfig){
					if(((NeuralNetworkConfig)config).getHiddenLayersModel()!=null){
						htmlWriter.writeP(AnalysisHiddenLayersModel.TAG_NAME+" = "+
								((NeuralNetworkConfig)config).getHiddenLayersModel().toString());
					}
				}
			}
		}
	}

	private static void outputPigScriptnDefinition(ToHtmlWriter htmlWriter,
			HadoopPigExecuteConfig config) {
 
		if(config.getPigScriptModel()!=null){
	 
			htmlWriter.writeP(AnalysisPigExecutableModel.TAG_NAME + " = ");
			htmlWriter.writeP(config.getPigScriptModel().getPigScript());
			 
		}
		
	}



	private static void outputHadoopUnionDefinition(ToHtmlWriter htmlWriter,
			HadoopUnionConfig config) {
		 
			htmlWriter.writeP(HadoopUnionModel.TAG_NAME+" = ");
	 
			  AnalysisHadoopUnionModel unionModel = config.getUnionModel();
			
		//	htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.TableSet_Source_Info,locale));
		//	htmlWriter.writeTable(writeUnionFIlesTable(unionModel.getUnionFiles()));
			htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.TableSet_Column_Info,locale));
			htmlWriter.writeTable(writeUnionColumnTable(unionModel.getOutputColumns(),unionModel.getUnionFiles()));

	}



	private static String writeUnionColumnTable(
			List<AnalysisHadoopUnionModelItem> outputColumns, List<AnalysisHadoopUnionFile> files)  {
		List<AnalysisHadoopUnionSourceColumn> sourceColumns = outputColumns.get(0).getMappingColumns();
		int columnNumber = sourceColumns.size()+1;
		String[] columnModelHeader=new String[columnNumber];
				
		columnModelHeader[0] =
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_NEW_COLUMN_NAME,locale);
		for(int i = 0 ;i <columnNumber-1;i++){
			columnModelHeader[i+1] =files.get(i).getFile();
		}
		
		 
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(columnModelHeader, tableWriter);
		for(AnalysisHadoopUnionModelItem cModel:outputColumns){
			String[] row=new String[columnNumber];
			row[0]=cModel.getColumnName();
			
			for(int i = 0 ;i <columnNumber-1;i++){
				row[i+1] =cModel.getMappingColumns().get(i).getColumnName();
			}
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}



//	private static String writeUnionFIlesTable(
//			List<AnalysisHadoopUnionFile> unionFiles) {
//		String[] tableModelHeader=new String[]{
//				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Name ,locale)
//			 
//				 };	
//		ToHtmlWriter tableWriter=new ToHtmlWriter();
//		addColumnHeader(tableModelHeader, tableWriter);
//		for(AnalysisHadoopUnionFile tModel:unionFiles){
//			String[] row=new String[1];
//			row[0]=tModel.getFile();
//		 
//			addRow(row, tableWriter);
//		}
//		return tableWriter.toString();
//	}



	private static void outputTableSetDefinition(ToHtmlWriter htmlWriter,
			TableSetConfig config) {
		 
			htmlWriter.writeP(TableJoinModel.TAG_NAME+" = ");
	 
			  AnalysisTableSetModel unionMode = config.getTableSetModel();  
	 
			htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.TableSet_Column_Info,locale));
			htmlWriter.writeTable(writeUnionColumnTable(unionMode.getColumnMapList()  ));
		
	}



	private static String writeUnionColumnTable(
			List<AnalysisColumnMap> columnMapList) {
		int columnNumber= columnMapList.size();
		int rowNumber = columnMapList.get(0).getTableColumns().size();
		String[][] rowDatas = new String [rowNumber][columnNumber];
		int i = 0; 
		String[] tableModelHeader=new String[columnNumber];
		for (AnalysisColumnMap analysisColumnMap : columnMapList) {
	 
			List<String> columns = analysisColumnMap.getTableColumns();
			tableModelHeader[i]=analysisColumnMap.getTableName();
			for (int j = 0; j < columns.size(); j++) {
				rowDatas[j][i] = columns.get(j) ;
			}
			i++;
		}
		
	
		
ToHtmlWriter tableWriter=new ToHtmlWriter();
addColumnHeader(tableModelHeader, tableWriter);

for (int j = 0; j < rowNumber ; j++) {
 
	String[] row=rowDatas[j];
	 
	addRow(row, tableWriter);
}
return tableWriter.toString();
	}



	private static String[] setSimpleConfig(ToHtmlWriter htmlWriter,
			AnalyticConfiguration config) {
		String configString=config.toString();
		if(StringUtil.isEmpty(configString))return new String[0];
		String[] configArray=configString.split("\n");
		for(int i=0;i<configArray.length;i++){
			if(config!=null&&config instanceof AbstractSVMConfig
					&&configArray[i].split("=")[0].trim().equals(AbstractSVMConfig.ConstKernelType)){			
				htmlWriter.writeP(configArray[i].split("=")[0]+" = "+configArray[i].split("=")[1].trim());
			}else if(config!=null&&config instanceof SQLAnalysisConfig
					&&configArray[i].split("=")[0].trim().equals(SQLAnalysisConfig.Const_PASSWORD)){
				continue;
			}else if(config!=null&&config instanceof VariableSelectionConfig
					&&configArray[i].split("=")[0].trim().equals(VariableSelectionConfig.PARAMETER_scoreType)){
				htmlWriter.writeP(configArray[i].split("=")[0]+" = "+configArray[i].split("=")[1].trim());
			}else{
				htmlWriter.writeP(configArray[i]);
			}		
		}
		return configArray;
	}

	private static void outputHadoopJoinDefinition(ToHtmlWriter htmlWriter,
			AnalyticConfiguration config) {
		htmlWriter.writeP(TableJoinModel.TAG_NAME+" = ");
		
		AnalysisHadoopJoinModel tjd = ((HadoopJoinConfig)config).getJoinModel();
		
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_INFO,locale));
		htmlWriter.writeTable(writeHadoopJoinModelTable(tjd));
		
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_COLUMN_INFO,locale));
		htmlWriter.writeTable(writeHadoopColumnModelTable(tjd));
		
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_CONDITION_INFO,locale));
		htmlWriter.writeTable(writeHadoopJoinConditionModelTable(tjd));
	}

	private static void outputTableJointDefinition(ToHtmlWriter htmlWriter,
			AnalyticConfiguration config) {
		htmlWriter.writeP(TableJoinModel.TAG_NAME+" = ");
		
		AnalysisTableJoinModel tjd = ((TableJoinConfig)config).getTableJoinDef();
		
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_INFO,locale));
		htmlWriter.writeTable(writeTableModelTable(tjd));
		
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_COLUMN_INFO,locale));
		htmlWriter.writeTable(writeColumnModelTable(tjd));
		
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_CONDITION_INFO,locale));
		htmlWriter.writeTable(writeJoinConditionModelTable(tjd));
	}


	private static String writeHadoopJoinConditionModelTable(AnalysisHadoopJoinModel tjd) {
		String[] joinConditionModelHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_LEFT,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TYPE,locale)};
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(joinConditionModelHeader, tableWriter);
		List<AnalysisHadoopJoinCondition> conditionModelList = tjd.getJoinConditions();
		for(AnalysisHadoopJoinCondition cModel:conditionModelList){
			String[] row=new String[2];
			row[0]=cModel.getKeyColumn();
			row[1]=tjd.getJoinType();
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}

	private static String writeJoinConditionModelTable(AnalysisTableJoinModel tjd) {
		String[] joinConditionModelHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_LEFT,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_RIGHT,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TYPE,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_CONDITION,locale)};
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(joinConditionModelHeader, tableWriter);
		List<AnalysisJoinCondition> conditionModelList = tjd.getJoinConditions();
		for(AnalysisJoinCondition cModel:conditionModelList){
			String[] row=new String[4];
			row[0]=cModel.getTableAlias1();
			row[1]=cModel.getTableAlias2();
			row[2]=cModel.getJoinType();
			row[3]=cModel.getCondition();
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}

	private static String writeHadoopColumnModelTable(AnalysisHadoopJoinModel tjd) {
		String[] columnModelHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_ALIAS,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_COLUMN_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_NEW_COLUMN_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_NEW_COLUMN_TYPE,locale)};
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(columnModelHeader, tableWriter);
		List<AnalysisHadoopJoinColumn> columnModelList = tjd.getJoinColumns();
		for(AnalysisHadoopJoinColumn cModel:columnModelList){
			String[] row=new String[4];
			row[0]=cModel.getFileName();
			row[1]=cModel.getColumnName();
			row[2]=cModel.getNewColumnName();
			row[3]=cModel.getColumnType();
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}

	private static String writeColumnModelTable(AnalysisTableJoinModel tjd) {
		String[] columnModelHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_ALIAS,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_COLUMN_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_NEW_COLUMN_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_NEW_COLUMN_TYPE,locale)};
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(columnModelHeader, tableWriter);
		List<AnalysisJoinColumn> columnModelList = tjd.getJoinColumns();
		for(AnalysisJoinColumn cModel:columnModelList){
			String[] row=new String[4];
			row[0]=cModel.getTableAlias();
			row[1]=cModel.getColumnName();
			row[2]=cModel.getNewColumnName();
			row[3]=cModel.getColumnType();
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}


	private static String writeHadoopJoinModelTable(AnalysisHadoopJoinModel tjd) {
		String[] tableModelHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_NAME,locale)};	
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(tableModelHeader, tableWriter);
		List<AnalysisHadoopJoinFile> tableModelList = tjd.getJoinTables();
		for(AnalysisHadoopJoinFile tModel:tableModelList){
			String[] row=new String[1];
			row[0]=tModel.getFile();
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}

	private static String writeTableModelTable(AnalysisTableJoinModel tjd) {
		String[] tableModelHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_TABLE_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_SCHEMA_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.JOIN_ALIAS,locale)};	
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(tableModelHeader, tableWriter);
		List<AnalysisJoinTable> tableModelList = tjd.getJoinTables();
		for(AnalysisJoinTable tModel:tableModelList){
			String[] row=new String[3];
			row[0]=tModel.getTable();
			row[1]=tModel.getSchema();
			row[2]=tModel.getAlias();
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}



	private static void outputCustomizedColumn(ToHtmlWriter htmlWriter,
			AnalyticConfiguration config, String[] configArray) {
		ArrayList<String> columnList=new ArrayList<String>();
		for(String s:configArray){
			String[] temp=s.split("=");
			columnList.add(temp[0].trim());
		}
		HashMap<String, String>  paramap=((CustomziedConfig)config).getParametersMap();
		Iterator<Entry<String, String>>  iter=paramap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry=iter.next();
			if(columnList.contains(entry.getKey()))continue;
			htmlWriter.writeP(COLUMN+entry.getKey()+" = "+entry.getValue());
		}
	}



	private static void outputQuantileModel(ToHtmlWriter htmlWriter,
			AnalyticConfiguration config) {
		htmlWriter.writeP(QuantileFieldsModel.TAG_NAME+" = ");
		String[] ColumnHeader=new String[]{
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.QUANTILE_COLUMN_NAME,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.NO_OF_BIN,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.QUANTILE_TYPE,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.CREATE_NEW_COLUMN,locale),
				WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.BIN,locale)};
		ToHtmlWriter tableWriter=new ToHtmlWriter();
		addColumnHeader(ColumnHeader,tableWriter);
		AnalysisQuantileFieldsModel qModel = ((VariableConfig)config).getQuantileModel();
		 for (Iterator<AnalysisQuantileItem> iterator = qModel.getQuantileItems().iterator(); iterator.hasNext();) {
			 AnalysisQuantileItem item = iterator.next();
			 String[] row=new String[5];
			 if(item.isCreateNewColumn()==true){
				 row[0]=item.getNewColumnName();
				 row[3]=Resources.TrueOpt;
			 }else{
				 row[0]=item.getColumnName();
				 row[3]=Resources.FalseOpt;
			 }
			 row[1]=String.valueOf(item.getNumberOfBin());
			 row[2]=item.getQuantileType()==0?
					 WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.TYPE_CUSTIMZE_LABEL,locale):WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.TYPE_AVG_ASC_LABEL,locale);
			 List<AnalysisQuantileItemBin> qBins = item.getBins();
			 StringBuilder sb=new StringBuilder();
			 if(item.getQuantileType()==0){
				 for(AnalysisQuantileItemBin bin:qBins){
						sb.append(bin.toString()).append(","); 
					 } 
				 if(sb.length()>1){
					 sb=sb.deleteCharAt(sb.length()-1);
				 }
			 }else{
				 sb.append("");
			 }		 
			 row[4]=sb.toString();
			 addRow(row,tableWriter);
		}	 
		 htmlWriter.writeTable(tableWriter.toString());
	}
	private static void addColumnHeader(String[] columns, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		for (int i = 0; i < columns.length; i++) {
			tdWriter.writeTD(columns[i]);
		}
		htmlWriter.writeTR(tdWriter.toString());
	}
	private static void addRow(String[] row, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		for(String s:row){
			if(s!=null){
				tdWriter.writeTD(s);
			}else{
				tdWriter.writeTD("");
			}
		}
		htmlWriter.writeTR(tdWriter.toString());
	}


	private static void writeSourceInfo(ToHtmlWriter htmlWriter,
			AnalyticSource analyticSource) {
		htmlWriter.writeP(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Source_Information,locale));
		String source=analyticSource.toString();
		String[] sourceArray=source.split("\n");
		if(sourceArray.length>1){
			for(int i=0;i<sourceArray.length-2;i=i+2){
				htmlWriter.writeP(sourceArray[i]+sourceArray[i+1]);
			}
		}
		if(analyticSource instanceof DataBaseAnalyticSource
				&&((DataBaseAnalyticSource)analyticSource).getTableInfo()!=null){
			String columnNames=((DataBaseAnalyticSource)analyticSource).getTableInfo().getColumnNameString();
			htmlWriter.writeP(sourceArray[sourceArray.length-2]+columnNames);
		}
		else if(analyticSource instanceof HadoopAnalyticSource){
			htmlWriter.writeP(((HadoopAnalyticSource)analyticSource).toReportString());

		}
	}

	/**
	 * @param analyticOutPut
	 * @return
	 */
	private static boolean needShowInPut(AnalyticOutPut analyticOutPut) {
		 if(analyticOutPut.getAnalyticNode().getAnalyzerClass().equals(
				 DBTableSelector.class.getName())
				 ||analyticOutPut.getAnalyticNode().getAnalyzerClass().equals(
						 EngineModelWrapperAnalyzer.class.getName())
							 ){
			 return false;
		 }
		 else{
			 return true;
		 }
	 
	}

	private static void createNodeOverViewSection(
			AnalyticOutPut analyticOutPut, ToHtmlWriter htmlWriter,int i) {
		ToHtmlWriter h2Writer=new ToHtmlWriter();
		h2Writer.writeH2(WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OverView,locale));
		
		String rootOverview=WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Node,locale) + i + overview;	
		htmlWriter.writeAnchor(rootOverview, h2Writer.toString());
		
		writeNodeOverView(analyticOutPut.getAnalyticNodeMetaInfo(),htmlWriter);
	}

	

	public static void main(String args[]) {

	}

}
