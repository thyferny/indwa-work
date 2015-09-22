package com.alpine.miner.impls.report;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticFlowMetaInfo;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.algoconf.AbstractSVMConfig;
import com.alpine.datamining.api.impl.algoconf.AdaboostConfig;
import com.alpine.datamining.api.impl.algoconf.AggregateConfig;
import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.algoconf.FPGrowthConfig;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.LinearRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.ReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.SQLAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.TableJoinConfig;
import com.alpine.datamining.api.impl.algoconf.VariableConfig;
import com.alpine.datamining.api.impl.algoconf.VariableSelectionConfig;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.db.DBTableSelector;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.association.AnalysisExpressionModel;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBin;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayersModel;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinTable;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisTableJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItem;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileItemBin;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceItem;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceModel;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOENode;
import com.alpine.datamining.operator.woe.AnalysisWOENominalNode;
import com.alpine.datamining.operator.woe.AnalysisWOENumericNode;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.impls.report.html.HTMLReportGenerator;
import com.alpine.miner.impls.report.html.HTMLTD;
import com.alpine.miner.impls.report.html.HTMLTR;
import com.alpine.miner.impls.report.html.HTMLTable;
import com.alpine.miner.impls.report.html.HTMLTextNode;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.tablejoin.TableJoinModel;
import com.alpine.miner.workflow.operator.parameter.woe.WOETable;
import com.alpine.utility.file.StringUtil;

public class FlowResultGenerator {

	public static final String COLUMN = "Column_";

	private static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private static final String  WEB_ENCODING = "UTF-8"; 


	private static List<String[]> toSourceProps(AnalyticSource analyticSource,Locale locale) {
		List<String[]> sourceProps=new ArrayList<String[]> ();
		sourceProps.add( new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Source_Information ,locale),""});
		String source=analyticSource.toString();
		String[] sourceArray=source.split("\n");
		if(sourceArray.length>1){
			for(int i=0;i<sourceArray.length-2;i=i+2){
				sourceProps.add( new String[]{sourceArray[i],sourceArray[i+1]});
			}
		}
		
		if(analyticSource instanceof DataBaseAnalyticSource
				&&((DataBaseAnalyticSource)analyticSource).getTableInfo()!=null){
			String columnNames=((DataBaseAnalyticSource)analyticSource).getTableInfo().getColumnNameString();
			sourceProps.add( new String[]{sourceArray[sourceArray.length-2],columnNames});
		}

		return sourceProps;
	}
	
	/**
	 * @param nodeMetaInfo
	 * @return
	 */
	public static String[][] getNodeMetaInfoProperties(
			AnalyticNodeMetaInfo nodeMetaInfo,Locale locale) {
		if(nodeMetaInfo==null){
			return null;
		}
		int n = 5; 
		Properties nodeProps = nodeMetaInfo.getProperties();
		if(nodeProps!=null){
			Set<Object> keys = nodeProps.keySet();
			n = n+keys.size();
		}
 
		String[][] props = new String[n][2];
		
		props[0]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Node_name,locale), nodeMetaInfo.getName()};
		props[1]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Algorithm_name,locale), nodeMetaInfo.getAlgorithmName()};
		props[2]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Algorithm_Description,locale), nodeMetaInfo.getAlgorithmDescription()};

		props[3]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Start_From,locale), StringUtil
				.filterEmptyString(DATE_FORMATTER.format(nodeMetaInfo.getStartTime()))};
		props[4]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_End_to,locale), StringUtil.filterEmptyString(DATE_FORMATTER.format(nodeMetaInfo.getEndTime()))};
		
		if(nodeProps!=null){
			 
			addPropertyArray(nodeProps, props, 4);
		}
		 
		return props;
	}

	public static void addPropertyArray(Properties flowProps,
			String[][] props, int startIndex) {
		Set<Object> keys = flowProps.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			startIndex= startIndex +1;
			String key = (String) iterator.next();
			props[startIndex]=new String[]{ key, flowProps.getProperty(key)};

		}
	}
	
	/**
	 * @param analyticMetaInfo
	 * @return
	 */
	public static String[][] toFlowMetaInfoProperties(
			AnalyticFlowMetaInfo analyticMetaInfo,String userName,Locale locale) {
		Properties flowProps = analyticMetaInfo.getAnalyticServerConfig();
		int n = 7; 
		if(flowProps!=null){
			Set<Object> keys = flowProps.keySet();
			n = n+keys.size();
		}
		
		
		String[][] props = new String[n][2];

		props[0]= new String[]{WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Flow_Owner,locale), StringUtil
				.filterEmptyString(analyticMetaInfo.getFlowOwnerUser())};
		
		props[1]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Flow_Description,locale), analyticMetaInfo.getFlowDescription()};
		props[2]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Execute_User,locale), userName};

		props[3]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Start_From ,locale), StringUtil
				.filterEmptyString(DATE_FORMATTER.format(analyticMetaInfo.getStartTime()))};
		props[4]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_End_to,locale), StringUtil
				.filterEmptyString(DATE_FORMATTER.format(analyticMetaInfo.getEndTime()))};

		props[5]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Engine_Name,locale), StringUtil
				.filterEmptyString(analyticMetaInfo
						.getAnalyticApplicationName())};
		props[6]=new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_CopyRight,locale), StringUtil
				.filterEmptyString(analyticMetaInfo.getCopyRightInfo())};

		if(flowProps!=null){
			 
			addPropertyArray(flowProps, props, 6);
		}
		return props;
	}
	
	/**
	 * @param outPut
	 * @return
	 * @throws Exception 
	 */
	public static  String[][] getOperatorInputs(AnalyticOutPut outPut,Locale locale) throws Exception {


		if(needShowInPut(outPut) ==false){
			return null;
		}
		List<AnalyticSource> sourceList=outPut.getAnalyticNode().getAllSources();
		if(sourceList==null||sourceList.size()==0) {
			return null;
		}
		//1*4
		//could have multiple sources
		//each source is a input...
		List<String[]> sourcesProps =new ArrayList<String[]>();
		if(sourceList.size()>1){
			Iterator<AnalyticSource>  iter=sourceList.iterator();
			while(iter.hasNext()){
				AnalyticSource source=iter.next();
				sourcesProps.addAll(toSourceProps(source,locale));
			}
		}else{
			sourcesProps.addAll(toSourceProps( outPut.getAnalyticNode().getSource(),locale));
		}

//		private static void writeParaInfo(ToHtmlWriter htmlWriter,
//		AnalyticSource analyticSource) {
		sourcesProps.addAll(toParameterProps( outPut.getAnalyticNode().getSource(),locale));
		String[][] sourcesPropsArray = new String[sourcesProps.size()][];
		int i = 0;
		for (Iterator<String[]> iterator = sourcesProps.iterator(); iterator.hasNext();) {
			String[] strings = (String[]) iterator.next();
			sourcesPropsArray[i]=strings;
			i++;
		}
			return sourcesPropsArray;
	}

	
	private static List<String[]> toGeneralProps(AnalyticConfiguration config,Locale locale){
		if(config instanceof SQLAnalysisConfig ){
			
			return Arrays.asList(new String[][]{{"SqlClause",((SQLAnalysisConfig)config).getSqlClause()}});
		}
			String configString=config.toString();
			List<String[]> paramProps = new ArrayList<String[]>(); 
			if(StringUtil.isEmpty(configString)){
				return paramProps;
			 }
			paramProps.add(new String[]{WorkFlowLanguagePack.getMessage(
				WorkFlowLanguagePack.AnalyticResultExporter_Param_information,locale)
				,""});
		
		
			String[] configArray=configString.split("\n");
			for(int i=0;i<configArray.length;i++){
	//no need any more , jeff has save the value directly, no more index MINERWEB-429
				//			if(config!=null&&config instanceof AbstractSVMConfig
//						&&configArray[i].split("=")[0].trim().equals(AbstractSVMConfig.ConstKernelType)){
//					
//					paramProps.add(new String[]{configArray[i].split("=")[0],
//							AbstractSVMConfig.kernelTypeArray[Integer.parseInt(configArray[i].split("=")[1].trim())-1]});
//					
//				}else 
					if(config!=null&&config instanceof SQLAnalysisConfig
						&&configArray[i].split("=")[0].trim().equals(SQLAnalysisConfig.Const_PASSWORD)){
					continue;
					
				}else if(config!=null&&config instanceof VariableSelectionConfig
						&&configArray[i].split("=")[0].trim().equals(VariableSelectionConfig.PARAMETER_scoreType)){
					
					paramProps.add(new String[]{configArray[i].split("=")[0],configArray[i].split("=")[1].trim()
							/*VariableSelectionConfig.scoreTypeArray[Integer.parseInt(configArray[i].split("=")[1].trim())-1]*/});
				}else{
					paramProps.add(new String[]{configArray[i].split("=")[0],configArray[i].split("=")[1]});
				}		
			}
			return paramProps;
	}
	
	
	private static List< String[]> toParameterProps(	AnalyticSource analyticSource,Locale locale) throws Exception {

		AnalyticConfiguration config=analyticSource.getAnalyticConfig();
		List<String[]> paramProps = toGeneralProps(config, locale);
	 
	 
		if(config!=null&&config instanceof LogisticRegressionConfigGeneral
					&&((LogisticRegressionConfigGeneral)config).getInterActionModel()!=null){
			paramProps.add(new String[]{InterActionColumnsModel.TAG_NAME,
					((LogisticRegressionConfigGeneral)config).getInterActionModel().toString()});
		}else if(config!=null&&config instanceof LinearRegressionConfig
				&&((LinearRegressionConfig)config).getInterActionModel()!=null){
			paramProps.add(new String[]{InterActionColumnsModel.TAG_NAME,
					((LinearRegressionConfig)config).getInterActionModel().toString()});
		}else if(config!=null&&config instanceof CustomziedConfig){
			outputCustomizedColumn(paramProps, config, config.toString().split("\n"));
		}else if(config!=null&&config instanceof VariableConfig){
			if(((VariableConfig)config).getQuantileModel()!=null){
				outputQuantileModel(paramProps, config,locale);
			}
			
			if(((VariableConfig)config).getDerivedModel()!=null){
				AnalysisDerivedFieldsModel derivedModel = ((VariableConfig)config).getDerivedModel();
				outputDerivedMoel(paramProps, derivedModel,locale);

				List<String> selectedField = derivedModel.getSelectedFieldList();
				if(selectedField!=null&&selectedField.size()>0){
					String selectedFields = mergeStringList(selectedField);
					paramProps.add(new String[]{VisualNLS.getMessage(VisualNLS.Selected_Columns,locale), 	selectedFields});

				}
			 
			}
		}else if(config!=null&&config instanceof AggregateConfig){
			if(((AggregateConfig)config).getAggregateFieldsModel()!=null){

				
				outputAggregateFieldsModel(config, paramProps,locale);
				
			}
			if(((AggregateConfig)config).getWindowFieldsModel()!=null){
				outputWindowFieldsModel(config, paramProps,locale);
			}
			
		}else if(config!=null&&config instanceof HistogramAnalysisConfig){
			if(((HistogramAnalysisConfig)config).getColumnBinModel()!=null){
				outputHistogramDefinition(paramProps, ((HistogramAnalysisConfig)config).getColumnBinModel(), locale);
//				paramProps.add(new String[]{AnalysisColumnBinsModel.TAG_NAME,
//						((HistogramAnalysisConfig)config).getColumnBinModel().toString()});
			}
		}else if(config!=null&&config instanceof ReplaceNullConfig){
			if(((ReplaceNullConfig)config).getNullReplacementModel()!=null){
				paramProps.add(new String[]{AnalysisNullReplacementModel.TAG_NAME,
						((ReplaceNullConfig)config).getNullReplacementModel().toString()});
			}
		}else if(config!=null&&config instanceof FPGrowthConfig){
			if(((FPGrowthConfig)config).getExpressionModel()!=null){
				paramProps.add(new String[]{AnalysisExpressionModel.TAG_NAME,
						((FPGrowthConfig)config).getExpressionModel().toString()});
			}
		}else if(config!=null&&config instanceof NeuralNetworkConfig){
			if(((NeuralNetworkConfig)config).getHiddenLayersModel()!=null){
				paramProps.add(new String[]{AnalysisHiddenLayersModel.TAG_NAME,
						((NeuralNetworkConfig)config).getHiddenLayersModel().toString()});
			}
		}
		else if(config!=null&&config instanceof TableJoinConfig
				&&((TableJoinConfig)config).getTableJoinDef()!=null){
			outputTableJointDefinition(paramProps, config);
		}
		else if(config!=null&&config instanceof WeightOfEvidenceConfig
				&&((WeightOfEvidenceConfig)config).getWOETableInfor()!=null){
			outputWOEDefinition(paramProps, (WeightOfEvidenceConfig)config,locale);
		}
		else if(config!=null&&config instanceof AdaboostConfig&&
				!((AdaboostConfig)config).isListEmpty()){
			AdaboostConfig adaboostConfig=(AdaboostConfig)config;
			AnalysisAdaboostPersistenceModel adaboostPersistenceModel=adaboostConfig.getAdaboostUIModel();
			List<AnalysisAdaboostPersistenceItem> items=adaboostPersistenceModel.getAdaboostUIItems();
			for(AnalysisAdaboostPersistenceItem item:items){
				AbstractModelTrainerConfig singleConfig=adaboostConfig.getNameConfigMap().get(item.getAdaName());
//				htmlWriter.writeH3(+":");
				paramProps.add(new String[]{item.getAdaName(),""});
				
				setSimpleConfig(paramProps, singleConfig,locale);
				if(config instanceof NeuralNetworkConfig){
					if(((NeuralNetworkConfig)config).getHiddenLayersModel()!=null){
						paramProps.add(new String[]{AnalysisHiddenLayersModel.TAG_NAME,
								((NeuralNetworkConfig)config).getHiddenLayersModel().toString()});
					}
				}
			}
		}
		return paramProps;
	}
	
	private static void outputHistogramDefinition(List<String[]> paramProps,
			AnalysisColumnBinsModel model, Locale locale) {
		paramProps.add(new String[]{AnalysisColumnBinsModel.TAG_NAME,
				toTableHTML(writeHistogramInput(model.getColumnBins(), locale))});
	}
	
	private static String writeHistogramInput(List<AnalysisColumnBin> bins, Locale locale){
		String[] inputTableHeader=new String[]{
				VisualNLS.getMessage(VisualNLS.Column_Name, locale ) ,
				VisualNLS.getMessage(VisualNLS.HISTOGRAM_BY_NUMBER, locale ) ,
				VisualNLS.getMessage(VisualNLS.HISTOGRAM_BY_WIDTH, locale ) ,
				VisualNLS.getMessage(VisualNLS.HISTOGRAM_MINIMUM, locale ) ,
				VisualNLS.getMessage(VisualNLS.HISTOGRAM_MAXIMUM, locale ) 
		};
		ToHtmlWriter tableWriter = new ToHtmlWriter(WEB_ENCODING);
		addColumnHeader(inputTableHeader, tableWriter);
		
		for(AnalysisColumnBin columnBin : bins){
			String[] row = {"","","","",""};
			row[0] = columnBin.getColumnName();
			switch(columnBin.getType()){
			case AnalysisColumnBin.TYPE_BY_NUMBER:
				row[1] = columnBin.getBin().toString();
				row[2] = "";
				break;
			case AnalysisColumnBin.TYPE_BY_WIDTH:
				row[1] = "";
				row[2] = Double.toString(columnBin.getWidth());
				break;
			}
			if(columnBin.isMin()){
				row[3] = Double.toString(columnBin.getMin());
			}
			if(columnBin.isMax()){
				row[4] = Double.toString(columnBin.getMax());
			}
			addRow(row, tableWriter);
		}
		return tableWriter.toString();
	}

	private static void outputWOEDefinition(List<String[]> paramProps,
			WeightOfEvidenceConfig config, Locale locale) {
 
		paramProps.add(new String[]{WOETable.TAG_NAME,
				toTableHTML(writeJoinConditionModelTable(config.getWOETableInfor(), locale))});
	}

	private static String writeJoinConditionModelTable(
			AnalysisWOETable woeTableInfor, Locale locale) {
		
		String[] joinConditionModelHeader=new String[]{
				VisualNLS.getMessage(VisualNLS.Column_Name, locale ) ,
				VisualNLS.getMessage(VisualNLS.ID, locale ) ,
				VisualNLS.getMessage(VisualNLS.WOE_VALUE, locale ) ,
				VisualNLS.getMessage(VisualNLS.WOE_OPTION_VALUE, locale ) ,
				VisualNLS.getMessage(VisualNLS.WOE_Bottom, locale ) ,
				VisualNLS.getMessage(VisualNLS.WOE_Upper, locale ) 
				};
		ToHtmlWriter tableWriter=new ToHtmlWriter(WEB_ENCODING);
		addColumnHeader(joinConditionModelHeader, tableWriter);
		
		List<AnalysisWOEColumnInfo> woeList = woeTableInfor.getDataTableWOE();
		for(AnalysisWOEColumnInfo woeParam:woeList){
			
		
			List<AnalysisWOENode> infoList = woeParam.getInforList();
		 
			for (Iterator iterator = infoList.iterator(); iterator.hasNext();) {
				AnalysisWOENode analysisWOENode = (AnalysisWOENode) iterator
						.next();
				String[] row = new String[6];
				row[0] =	woeParam.getColumnName();
				row[1] = analysisWOENode.getGroupInfror();
				row[2] =analysisWOENode.getWOEValue()+"";
				if(analysisWOENode instanceof AnalysisWOENominalNode ){
					row[3] =	((AnalysisWOENominalNode)analysisWOENode).getChoosedList().toString();
					row[4] = "";
					row[5] = "";
				}
				else if(analysisWOENode instanceof AnalysisWOENumericNode ){
					row[3] ="";
					row[4] = ((AnalysisWOENumericNode)analysisWOENode).getBottom()+"";
					row[5] = ((AnalysisWOENumericNode)analysisWOENode).getUpper()+"";
					
				}
				 
				addRow(row, tableWriter);
			}
			
			
		 
			
		}
		return tableWriter.toString();
		
	 
	}

	/**
	 * @param paramProps
	 * @param config
	 * @param derivedModel 
	 */
	private static void outputDerivedMoel(List<String[]> paramProps,  AnalysisDerivedFieldsModel derivedModel,Locale locale) {
		
		
		 
		String[] ColumnHeader=new String[]{
				 VisualNLS.getMessage(VisualNLS.Column_Name,locale),
					 VisualNLS.getMessage(VisualNLS.Data_Type,locale),
						 VisualNLS.getMessage(VisualNLS.Expression,locale)};
		ToHtmlWriter tableWriter=new ToHtmlWriter(WEB_ENCODING);
		addColumnHeader(ColumnHeader,tableWriter);
 
		 for (Iterator<AnalysisDerivedFieldItem> iterator = derivedModel.getDerivedFieldsList().iterator(); iterator.hasNext();) {
			 AnalysisDerivedFieldItem item = iterator.next();
			 String[] row=new String[3];
			 
		 
				 row[0]=item.getResultColumnName();
				 
			 row[1]=item.getDataType();
			 row[2]=item.getSqlExpression();
			 
			 addRow(row,tableWriter);
		}	 
		 paramProps.add(new String[]{AnalysisDerivedFieldItem.TAG_NAME,toTableHTML(tableWriter.toString())}); 
 
		
	}

	private static void outputAggregateFieldsModel(AnalyticConfiguration config,
			List<String[]> paramProps,Locale locale) throws Exception {
		AnalysisAggregateFieldsModel aggregateFieldModel = ((AggregateConfig)config).getAggregateFieldsModel();
//		aggregateFieldModel.get
//		""
//		""
		HTMLTable ptable = new HTMLTable(null, null);
		ptable.setCssClass(HTMLReportGenerator.CSS_PROPERTY_TABLE) ;
		
 

		ptable.appendChild(createTR( new String[] {VisualNLS.getMessage(VisualNLS.Groupby_Fields,locale),
				mergeStringList(aggregateFieldModel.getGroupByFieldList())}));
		 
		ptable.appendChild(createTR( new String[]{ VisualNLS.getMessage(VisualNLS.Parent_Fields,locale), 
				mergeStringList(aggregateFieldModel.getParentFieldList())}));
	 
		
 
		 
		List<AnalysisAggregateField> aggFieldList = aggregateFieldModel.getAggregateFieldList();
		
		if (aggFieldList != null) {
			HTMLTable table = new HTMLTable(null, null);
			table.setCssClass(HTMLReportGenerator.CSS_PROPERTY_TABLE) ;
			String[] ColumnHeader = new String[] {  VisualNLS.getMessage(VisualNLS.Alias,locale),
					 VisualNLS.getMessage(VisualNLS.Data_Type,locale),  VisualNLS.getMessage(VisualNLS.Expression,locale) };
			table.appendChild(createTR(ColumnHeader)) ;
			ToHtmlWriter tableWriter = new ToHtmlWriter(WEB_ENCODING);
			addColumnHeader(ColumnHeader, tableWriter);

			for (Iterator iterator = aggFieldList.iterator(); iterator
					.hasNext();) {
				AnalysisAggregateField field = (AnalysisAggregateField) iterator
						.next();

				String[] row = new String[3];

				row[0] = field.getAlias();
				row[1] = field.getDataType();
				row[2] = field.getAggregateExpression();
				
				table.appendChild(createTR(row)) ;
			}

			ptable.appendChild(createTR(new String[] { AnalysisAggregateField.TAG_NAME,
					table.toString() }));
			
			paramProps.add(new String[] { AnalysisAggregateFieldsModel.TAG_NAME,
					ptable.toString() });
		}
	}
	private static void outputWindowFieldsModel(AnalyticConfiguration config,
			List<String[]> paramProps, Locale locale) throws Exception {
		AnalysisWindowFieldsModel windowFieldModel = ((AggregateConfig) config)
				.getWindowFieldsModel();
		List<AnalysisWindowField> windowFieldList = windowFieldModel
				.getWindowFieldList();
		if (windowFieldList != null) {
			HTMLTable table = new HTMLTable(null, null);
			table.setCssClass(HTMLReportGenerator.CSS_PROPERTY_TABLE) ;
			String[] ColumnHeader = new String[] {  VisualNLS.getMessage(VisualNLS.Result_Column,locale),
					VisualNLS.getMessage(VisualNLS.Data_Type,locale) ,
							 VisualNLS.getMessage(VisualNLS.Window_Function,locale), 
							 VisualNLS.getMessage(VisualNLS.Window_Specification,locale) };
			table.appendChild(createTR(ColumnHeader)) ;
			ToHtmlWriter tableWriter = new ToHtmlWriter(WEB_ENCODING);
			addColumnHeader(ColumnHeader, tableWriter);

			for (Iterator iterator = windowFieldList.iterator(); iterator
					.hasNext();) {
				AnalysisWindowField field = (AnalysisWindowField) iterator
						.next();

				String[] row = new String[4];

				row[0] = field.getResultColumn();
				row[1] = field.getDataType();

				row[2] = field.getWindowFunction();
				row[3] = field.getWindowSpecification();
				table.appendChild(createTR(row)) ;
			}

			paramProps.add(new String[] { AnalysisWindowField.TAG_NAME,
					table.toString() });
		}
	}

 
	
	private static HTMLTR  createTR(String[] items) throws Exception { 
		HTMLTR  tr= new HTMLTR(); 
	 if(items!=null){
		 for (int i = 0; i < items.length; i++) {
			HTMLTD td = new HTMLTD();
			td.appendChild(new HTMLTextNode(items[i])) ;
			 tr.appendChild(td) ;
		}
	 }
		return tr;
	}



	private static String mergeStringList(List<String> selectedField) {
		if(selectedField!=null){
			StringBuffer sb = new StringBuffer ();
			for (int i = 0; i < selectedField.size(); i++) {
				
			 
				String field = (String) selectedField.get(i);
				sb.append(field);
				if(i<selectedField.size()-1){
					sb.append(",") ;
				}
				
			}
			return sb.toString();
		}
		return "";
	}

 
	

	private static void outputCustomizedColumn(List<String[]> paramProps,
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
			paramProps.add(new String[]{COLUMN+entry.getKey(),entry.getValue()}); 
		}
	}



	private static void outputQuantileModel(List<String[]> paramProps,
			AnalyticConfiguration config,Locale locale) {
		 
		String[] ColumnHeader=new String[]{
				WorkFlowLanguagePack.QUANTILE_COLUMN_NAME,
				WorkFlowLanguagePack.NO_OF_BIN,
				WorkFlowLanguagePack.QUANTILE_TYPE,
				WorkFlowLanguagePack.CREATE_NEW_COLUMN,
				WorkFlowLanguagePack.BIN};
		ToHtmlWriter tableWriter=new ToHtmlWriter(WEB_ENCODING);
		addColumnHeader(ColumnHeader,tableWriter);
		AnalysisQuantileFieldsModel qModel = ((VariableConfig)config).getQuantileModel();
		 for (Iterator<AnalysisQuantileItem> iterator = qModel.getQuantileItems().iterator(); iterator.hasNext();) {
			 AnalysisQuantileItem item = iterator.next();
			 String[] row=new String[5];
			 if(item.isCreateNewColumn()==true){
				 row[0]=item.getNewColumnName();
				 row[3]="True";
			 }else{
				 row[0]=item.getColumnName();
				 row[3]="False";
			 }
			 row[1]=String.valueOf(item.getNumberOfBin());
			 row[2]=item.getQuantileType()==0?
					 WorkFlowLanguagePack.TYPE_CUSTIMZE_LABEL:WorkFlowLanguagePack.TYPE_AVG_ASC_LABEL;
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
		 paramProps.add(new String[]{VisualNLS.getMessage(VisualNLS.QuantileModel,locale),toTableHTML(tableWriter.toString())}); 
	 
	}
	private static void addColumnHeader(String[] columns, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter(WEB_ENCODING);
		for (int i = 0; i < columns.length; i++) {
			tdWriter.writeTD(columns[i]);
		}
		htmlWriter.writeTR(tdWriter.toString());
	}
	private static void addRow(String[] row, ToHtmlWriter htmlWriter) {
		ToHtmlWriter tdWriter=new ToHtmlWriter(WEB_ENCODING);
		for(String s:row){
			if(s!=null){
				tdWriter.writeTD(s);
			}else{
				tdWriter.writeTD("");
			}
		}
		htmlWriter.writeTR(tdWriter.toString());
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
	
	private static String[] setSimpleConfig(List<String[]> paramProps,
			AnalyticConfiguration config,Locale locale) {
		String configString=config.toString();
		if(StringUtil.isEmpty(configString))return new String[0];
		String[] configArray=configString.split("\n");
		for(int i=0;i<configArray.length;i++){
			if(config!=null&&config instanceof AbstractSVMConfig
					&&configArray[i].split("=")[0].trim().equals(AbstractSVMConfig.ConstKernelType)){
//				paramProps.add(new String[]{configArray[i].split("=")[0],
//						AbstractSVMConfig.kernelTypeArray[Integer.parseInt(configArray[i].split("=")[1].trim())-1]});

				paramProps.add(new String[]{configArray[i].split("=")[0],configArray[i].split("=")[1].trim()});
			}else if(config!=null&&config instanceof SQLAnalysisConfig
					&&configArray[i].split("=")[0].trim().equals(SQLAnalysisConfig.Const_PASSWORD)){
				continue;
			}else if(config!=null&&config instanceof VariableSelectionConfig
					&&configArray[i].split("=")[0].trim().equals(VariableSelectionConfig.PARAMETER_scoreType)){
				paramProps.add(new String[]{configArray[i].split("=")[0],
						VariableSelectionConfig.getScoreTypeArray(locale)[Integer.parseInt(configArray[i].split("=")[1].trim())-1]});
			}else{
				paramProps.add(new String[]{"",configArray[i]});
			}		
		}
		return configArray;
	}



	private static void outputTableJointDefinition(List<String[]> paramProps,
			AnalyticConfiguration config) {
		paramProps.add(new String[]{TableJoinModel.TAG_NAME," "});
		
		AnalysisTableJoinModel tjd = ((TableJoinConfig)config).getTableJoinDef();
		
		paramProps.add(new String[]{WorkFlowLanguagePack.JOIN_TABLE_INFO,
		toTableHTML(writeTableModelTable(tjd))});
		
		paramProps.add(new String[]{WorkFlowLanguagePack.JOIN_COLUMN_INFO,
		toTableHTML(writeColumnModelTable(tjd))});
		
		paramProps.add(new String[]{WorkFlowLanguagePack.JOIN_CONDITION_INFO,
				toTableHTML(writeJoinConditionModelTable(tjd))});
	}

//TODO: this is special case... 

	private static String toTableHTML(String tableTRTD) { 
		return"<table class =\"" +HTMLReportGenerator.CSS_PROPERTY_TABLE+
			 		" \">"+tableTRTD+"</table>"; 
//		return "<table>"+writeTableModelTable+"</table>"; 
	}

	private static String writeJoinConditionModelTable(AnalysisTableJoinModel tjd) {
		String[] joinConditionModelHeader=new String[]{
				WorkFlowLanguagePack.JOIN_TABLE_LEFT,
				WorkFlowLanguagePack.JOIN_TABLE_RIGHT,
				WorkFlowLanguagePack.JOIN_TYPE,
				WorkFlowLanguagePack.JOIN_CONDITION};
		ToHtmlWriter tableWriter=new ToHtmlWriter(WEB_ENCODING);
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



	private static String writeColumnModelTable(AnalysisTableJoinModel tjd) {
		String[] columnModelHeader=new String[]{
				WorkFlowLanguagePack.JOIN_ALIAS,
				WorkFlowLanguagePack.JOIN_COLUMN_NAME,
				WorkFlowLanguagePack.JOIN_NEW_COLUMN_NAME,
				WorkFlowLanguagePack.JOIN_NEW_COLUMN_TYPE};
		ToHtmlWriter tableWriter=new ToHtmlWriter(WEB_ENCODING);
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



	private static String writeTableModelTable(AnalysisTableJoinModel tjd) {
		String[] tableModelHeader=new String[]{
				WorkFlowLanguagePack.JOIN_TABLE_NAME,
				WorkFlowLanguagePack.JOIN_SCHEMA_NAME,
				WorkFlowLanguagePack.JOIN_ALIAS};	
		ToHtmlWriter tableWriter=new ToHtmlWriter(WEB_ENCODING);
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
	
}
