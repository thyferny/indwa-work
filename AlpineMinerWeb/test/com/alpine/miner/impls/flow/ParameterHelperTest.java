/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ResourceManagerTest.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.flow;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import com.alpine.miner.ifc.DBResourceManagerFactory;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceItem;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPersistenceModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateField;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowField;
import com.alpine.miner.workflow.operator.parameter.aggregate.WindowFieldsModel;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterFactory;
import com.alpine.miner.workflow.reader.AbstractReaderParameters;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
 

/**
 * @author john zhao
 * 
 */
public class ParameterHelperTest extends AbstractFlowTest{

	static private String flow_name_adaboost = "sample_adaboost.afm";
	static private String flow_name_pca = "sample_principal_component_analysis.afm";

	static private String flow_name_varible = "sample_variable.afm";
	static private String flow_name_aggregate = "sample_aggregate.afm";
	static private String flow_name_kmeans = "sample_K-means.afm";
	static private String flow_name_product_recommendation = "product_recommendation.afm";
	static private String flow_name_cart_tree = "sample_CART_train_normalized.afm";
	static private String flow_name_neural_network = "sample_neural_network_train_normalized.afm";
	static private String flow_name_linear_regression = "sample_linear_regression.afm";
	static private String flow_name_logistic_regression = "sample_logistic_regression_train_normalized.afm";
	static private String flow_name_svm = "sample_svm.afm";
	static private String flow_name_barchart = "sample_bar_chart_preview.afm";
	static private String flow_name_time_series = "sample_time_series.afm";

	static private String flow_name_univariate_ = "sample_univariate.afm";
	static private String flow_name_Correlation = "sample_correlation.afm";
	static private String flow_name_NB = "sample_naive_bayes_train_normalized.afm";
	static private String flow_name_Association="sample_product_recommendation.afm";
	
	static private String flow_name_sample_algorithm_comparison ="sample_algorithm_comparison.afm";
	
	static private String flow_name_sample_house_price_cart ="sample_house_price_cart.afm" ;
	static private String flow_name_sample_svd="sample_svd.afm" ;
	static{
		DBResourceManagerFactory.INSTANCE.registerDBResourceManager(WebDBResourceManager.getInstance());
	}

	
	
	private List<String> kernelList = Arrays.asList(new String[] {
			OperatorParameterFactory.VALUE_DOT_PRODUCT,
			OperatorParameterFactory.VALUE_POLYNOMINAL,
			OperatorParameterFactory.VALUE_GAUSSIAN });

	private OperatorParameter getParameter(String name,
			List<OperatorParameter> parameters) {
		OperatorParameter paramter = null;
		for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
			OperatorParameter operatorParameter = (OperatorParameter) iterator
					.next();
			if (operatorParameter.getName().equals(name)) {
				paramter = operatorParameter;
				break;
			}

		}
		return paramter;

	}

	private List getAvaliableValues(String name,
			List<OperatorParameter> parameters, String username) {
		for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
			OperatorParameter operatorParameter = (OperatorParameter) iterator
					.next();
			if (operatorParameter.getName().equals(name)) {
				OperatorParameter parameter = operatorParameter;
				try {
					return OperatorParameterFactory.INSTANCE.getHelper(parameter)
							.getAvaliableValues(parameter, username, ResourceType.Personal);
				} catch (Exception e) {
					 
					e.printStackTrace();
				}

			}

		}
		return null;

	}

	private Object getParameterValue(String name,
			List<OperatorParameter> parameters) {
		for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
			OperatorParameter operatorParameter = (OperatorParameter) iterator
					.next();
			if (operatorParameter.getName().equals(name)) {
				return operatorParameter.getValue();
			}

		}
		return null;

	}

	private Object getParamValue(String name, Operator operator) {

		return getParameterValue(name, operator.getOperatorParameterList());

	}
 

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 * 
	 * @throws Exception
	 */
    /* Remove product replacement
	@Test
	public void test_product_recommendation() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_product_recommendation);
		Operator productRecommedationOperator = getOperatorByName(workFlow,
				"Product Recommendation");
		List<OperatorParameter> parameters = productRecommedationOperator
				.getOperatorParameterList();
		String username = "guest";
		// values for Product Recommendation
		String customTableName = (String) getParamValue(
				OperatorParameter.NAME_Customer_Table_Name,
				productRecommedationOperator);
		Assert.assertEquals(customTableName, "\"demo\".\"golfnew\"");
		// avaliable values for Product Recommendation

		List<?> avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Customer_Table_Name, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("\"demo\".\"golfnew\""));

		String customIDColumn = (String) getParamValue(
				OperatorParameter.NAME_Customer_ID_Column,
				productRecommedationOperator);
		Assert.assertEquals(customIDColumn, "outlook");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Customer_ID_Column, parameters, username);
		Assert.assertTrue(avaliableVlues.contains("temperature"));
		Assert.assertTrue(avaliableVlues.contains("humidity"));

		String customValueColumn = (String) getParamValue(
				OperatorParameter.NAME_Customer_Value_Column,
				productRecommedationOperator);
		Assert.assertEquals(customValueColumn, "temperature");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Customer_Value_Column, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("temperature"));
		Assert.assertTrue(avaliableVlues.contains("humidity"));
	 

		String NAME_Customer_Product_Column = (String) getParamValue(
				OperatorParameter.NAME_Customer_Product_Column,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Customer_Product_Column, "outlook");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Customer_Product_Column, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("wind"));
		Assert.assertTrue(avaliableVlues.contains("outlook"));

		String NAME_Customer_Product_Count_Column = (String) getParamValue(
				OperatorParameter.NAME_Customer_Product_Count_Column,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Customer_Product_Count_Column, "temperature");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Customer_Product_Count_Column,
				parameters, username);
		Assert.assertTrue(avaliableVlues.contains("temperature"));
		Assert.assertTrue(avaliableVlues.contains("humidity"));
 

		String NAME_Selection_Table_Name = (String) getParamValue(
				OperatorParameter.NAME_Selection_Table_Name,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Selection_Table_Name, "\"demo\".\"golfnew\"");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Selection_Table_Name, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("\"demo\".\"golfnew\""));

		String NAME_Selection_ID_Column = (String) getParamValue(
				OperatorParameter.NAME_Selection_ID_Column,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Selection_ID_Column, "outlook");

		String NAME_SimThreshold = (String) getParamValue(
				OperatorParameter.NAME_SimThreshold,
				productRecommedationOperator);

		Assert.assertEquals(NAME_SimThreshold, "0.9");
		String NAME_Max_Record = (String) getParamValue(
				OperatorParameter.NAME_Max_Record, productRecommedationOperator);

		Assert.assertEquals(NAME_Max_Record, "10");
		String NAME_Min_Product_Count = (String) getParamValue(
				OperatorParameter.NAME_Min_Product_Count,
				productRecommedationOperator);

		Assert.assertEquals(NAME_Min_Product_Count, "10");
		String NAME_Score_Threshold = (String) getParamValue(
				OperatorParameter.NAME_Score_Threshold,
				productRecommedationOperator);

		Assert.assertEquals(NAME_Score_Threshold, "1");
		String NAME_Cohorts = (String) getParamValue(
				OperatorParameter.NAME_Cohorts, productRecommedationOperator);

		Assert.assertEquals(NAME_Cohorts, "1:-Infinity:0;2:0:Infinity");
		String NAME_Above_Cohort = (String) getParamValue(
				OperatorParameter.NAME_Above_Cohort,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Above_Cohort, "1");
		String NAME_Below_Cohort = (String) getParamValue(
				OperatorParameter.NAME_Below_Cohort,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Below_Cohort, "1");

		String NAME_Target_Cohort = (String) getParamValue(
				OperatorParameter.NAME_Target_Cohort,
				productRecommedationOperator);
		Assert.assertEquals(NAME_Target_Cohort, "4");

		// values for Product Recommendation Evaluation
		Operator evaluationOperator = getOperatorByName(workFlow,
				"Product Recommendation Evaluation");
		parameters = evaluationOperator.getOperatorParameterList();
		String recommendationTable = (String) getParamValue(
				OperatorParameter.NAME_Recommendataion_Table,
				evaluationOperator);
		Assert.assertEquals(recommendationTable, "\"demo\".\"golfnew\"");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Recommendataion_Table, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("\"demo\".\"golfnew\""));

		String recommendationIDColumn = (String) getParamValue(
				OperatorParameter.NAME_Recommendataion_ID_Column,
				evaluationOperator);
		Assert.assertEquals(recommendationIDColumn, "outlook");

		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Recommendataion_ID_Column, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("temperature"));
		Assert.assertTrue(avaliableVlues.contains("humidity"));

		String recommendationProductionColumn = (String) getParamValue(
				OperatorParameter.NAME_Recommendataion_Product_Column,
				evaluationOperator);
		Assert.assertEquals(recommendationProductionColumn, "outlook");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Recommendataion_Product_Column,
				parameters, username);
		Assert.assertTrue(avaliableVlues.contains("temperature"));
		Assert.assertTrue(avaliableVlues.contains("humidity"));

		String preRecommendationTable = (String) getParamValue(
				OperatorParameter.NAME_Pre_Recommendataion_Table,
				evaluationOperator);
		Assert.assertEquals(preRecommendationTable, "\"demo\".\"golfnew\"");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Pre_Recommendataion_Table, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("\"demo\".\"golfnew\""));

		String preRecommendationIDColumn = (String) getParamValue(
				OperatorParameter.NAME_Pre_Recommendataion_ID_Column,
				evaluationOperator);
		Assert.assertEquals(preRecommendationIDColumn, "temperature");

		String preRecommendationValueColumn = (String) getParamValue(
				OperatorParameter.NAME_Pre_Recommendataion_Value_Column,
				evaluationOperator);
		Assert.assertEquals(preRecommendationValueColumn, "temperature");
//		avaliableVlues = getAvaliableValues(
//				OperatorParameter.NAME_Pre_Recommendataion_Value_Column,
//				parameters, username);
//		Assert.assertTrue(avaliableVlues.contains("temperature"));
//		Assert.assertTrue(avaliableVlues.contains("humidity"));
//		Assert.assertFalse(avaliableVlues.contains("outlook"));

		String postRecommendationTable = (String) getParamValue(
				OperatorParameter.NAME_Post_Recommendataion_Table,
				evaluationOperator);
		Assert.assertEquals(postRecommendationTable, "\"demo\".\"golfnew\"");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Post_Recommendataion_Table, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("\"demo\".\"golfnew\""));

		String postRecommendationIDColumn = (String) getParamValue(
				OperatorParameter.NAME_Post_Recommendataion_ID_Column,
				evaluationOperator);
		Assert.assertEquals(postRecommendationIDColumn, "outlook");

		String postRecommendationValueColumn = (String) getParamValue(
				OperatorParameter.NAME_Post_Recommendataion_Value_Column,
				evaluationOperator);
		Assert.assertEquals(postRecommendationValueColumn, "temperature");
//		avaliableVlues = getAvaliableValues(
//				OperatorParameter.NAME_Post_Recommendataion_Value_Column,
//				parameters, username);
//		Assert.assertTrue(avaliableVlues.contains("temperature"));
//		Assert.assertTrue(avaliableVlues.contains("humidity"));
//		Assert.assertFalse(avaliableVlues.contains("outlook"));

		String postRecommendationProductColumn = (String) getParamValue(
				OperatorParameter.NAME_Post_Recommendataion_Product_Column,
				evaluationOperator);
		Assert.assertEquals(postRecommendationProductColumn, "outlook");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_Post_Recommendataion_Product_Column,
				parameters, username);
		Assert.assertTrue(avaliableVlues.contains("temperature"));
		Assert.assertTrue(avaliableVlues.contains("humidity"));

	}*/

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_adaboost() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_adaboost);

		Operator adaBoostOperator = getOperatorByName(workFlow, "AdaBoost");
		List<OperatorParameter> parameters = adaBoostOperator
				.getOperatorParameterList();
		String username = "guest";

		// values for Product Recommendation
		String dependent_column = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, adaBoostOperator);
		Assert.assertEquals(dependent_column, "dependent_column");
		// avaliable values for Product Recommendation

		String forceRetrain = (String) getParamValue(
				OperatorParameter.NAME_forceRetrain, adaBoostOperator);
		Assert.assertEquals(forceRetrain, "Yes");

		List avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_columnNames, parameters, username);
		Assert.assertTrue(avaliableVlues.contains("input flow to plant"));
		Assert.assertTrue(avaliableVlues.contains("input pH to plant"));

		AdaboostPersistenceModel adaboostParam = (AdaboostPersistenceModel) getParamValue(
				OperatorParameter.NAME_adaboostUIModel, adaBoostOperator);
		Assert.assertEquals(adaboostParam.getAdaboostUIItems().size(), 6);
		AdaboostPersistenceItem item = adaboostParam.getAdaboostUIItems()
				.get(5);
		Assert.assertEquals(item.getAdaName(), "Naive Bayes5");
 
		Assert.assertTrue(item.getParameterMap().keySet().contains(
				"calculateDeviance"));
		// values for Product Recommendation Evaluation
		Operator evaluationOperator = getOperatorByName(workFlow,
				"AdaBoost Prediction");
		parameters = evaluationOperator.getOperatorParameterList();

		String outputSchema = (String) getParamValue(
				OperatorParameter.NAME_outputSchema, evaluationOperator);
		Assert.assertEquals(outputSchema, "demo");

		String outputTable = (String) getParamValue(
				OperatorParameter.NAME_outputTable, evaluationOperator);
		Assert.assertEquals(outputTable, "ada_output");

		String drop = (String) getParamValue(
				OperatorParameter.NAME_dropIfExist, evaluationOperator);
		Assert.assertEquals(drop, "Yes");

		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_outputSchema, parameters, username);
		Assert.assertTrue(avaliableVlues.contains("public"));

		avaliableVlues = getAvaliableValues(OperatorParameter.NAME_dropIfExist,
				parameters, username);
		Assert.assertTrue(avaliableVlues.contains("No"));

	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_pca() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_pca);
		Operator pcaOperator = getOperatorByName(workFlow,
				"Principal Component Analysis");
		List<OperatorParameter> parameters = pcaOperator
				.getOperatorParameterList();
		String username = "guest";
		String outputSchema = (String) getParamValue(
				OperatorParameter.NAME_PCAQoutputSchema, pcaOperator);
		Assert.assertEquals(outputSchema, "demo");
		outputSchema = (String) getParamValue(
				OperatorParameter.NAME_PCAQvalueOutputSchema, pcaOperator);
		Assert.assertEquals(outputSchema, "demo");

		String outputTable = (String) getParamValue(
				OperatorParameter.NAME_PCAQoutputTable, pcaOperator);
		Assert.assertEquals(outputTable, "pca_output");
		outputTable = (String) getParamValue(
				OperatorParameter.NAME_PCAQvalueOutputTable, pcaOperator);
		Assert.assertEquals(outputTable, "pca_result");

		String drop = (String) getParamValue(
				OperatorParameter.NAME_PCAQDropIfExist, pcaOperator);
		Assert.assertEquals(drop, "Yes");
		drop = (String) getParamValue(
				OperatorParameter.NAME_PCAQvalueDropIfExist, pcaOperator);
		Assert.assertEquals(drop, "Yes");
		String percent = (String) getParamValue(OperatorParameter.NAME_percent,
				pcaOperator);
		Assert.assertEquals(percent, "0.99");
		String aType = (String) getParamValue(
				OperatorParameter.NAME_analysisType, pcaOperator);
		Assert.assertEquals(aType, "COV-SAM");

		List avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_PCAQvalueOutputSchema, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("public"));

		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_PCAQoutputSchema, parameters, username);
		Assert.assertTrue(avaliableVlues.contains("public"));

		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_PCAQDropIfExist, parameters, username);
		Assert.assertTrue(avaliableVlues.contains("No"));
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_PCAQvalueDropIfExist, parameters,
				username);
		Assert.assertTrue(avaliableVlues.contains("No"));

		avaliableVlues = getAvaliableValues(OperatorParameter.NAME_columnNames,
				parameters, username);
		Assert.assertTrue(avaliableVlues.contains("input flow to plant"));
		Assert.assertTrue(avaliableVlues.contains("input pH to plant"));

		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_remainColumns, parameters, username);
		Assert.assertTrue(avaliableVlues.contains("date"));

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static  void tearDownAfterClass() throws Exception {
	}
 
//	@Test
//	public void readWriteAll() {
//		final String DATA_DIR = "test_data";
//		File folder = new File(DATA_DIR);
//		
//		File[] fileList = folder.listFiles();
//		for (File f : fileList) {
//			if (f.isFile() == true && f.getName().endsWith(".afm")) {
//				
//				try {
//					testWorkflowSave(f.getName());
//				} catch (Exception e) {
//					
//					e.printStackTrace();
//				}
//			}			
//		}
//	}
//	

	/**
	 * Test method for read of parameter values
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParameterValue_aggregate() throws Exception {

		XMLWorkFlowReader reader = new XMLWorkFlowReader();

		String filepath = getTestDataDir() + java.io.File.separator
				+ flow_name_aggregate;
		AbstractReaderParameters para = new XMLFileReaderParameters(filepath,"guest",ResourceType.Personal);

		OperatorWorkFlow workFlow = reader.doRead(para,Locale.getDefault());
		Operator operator = workFlow.getChildList().get(0).getOperator();

		List<OperatorParameter> parameters = operator
				.getOperatorParameterList();
		// Connection Demo
		String dbconName = (String) getParameterValue(
				OperatorParameter.NAME_dBConnectionName, parameters);
		Assert.assertEquals(dbconName, "Connection Demo");

		// demo
		String schemaName = (String) getParameterValue(
				OperatorParameter.NAME_schemaName, parameters);
		Assert.assertEquals(schemaName, "demo");

		// aggregatetest
		String tableName = (String) getParameterValue(
				OperatorParameter.NAME_tableName, parameters);
		Assert.assertEquals(tableName, "aggregatetest");

		parameters = workFlow.getChildList().get(1).getOperator()
				.getOperatorParameterList();

		Assert.assertEquals(getParameterValue(
				OperatorParameter.NAME_dropIfExist, parameters), "Yes");

		AggregateFieldsModel aggregateFiedlModel = (AggregateFieldsModel) getParameterValue(
				OperatorParameter.NAME_aggregateFieldList, parameters);
		WindowFieldsModel windowFiedlModel = (WindowFieldsModel) getParameterValue(
				OperatorParameter.NAME_windowFieldList, parameters);

		Assert.assertEquals(getParameterValue(
				OperatorParameter.NAME_outputTable, parameters), "aggt_view");
		Assert.assertEquals(getParameterValue(
				OperatorParameter.NAME_outputSchema, parameters), "demo");
		Assert.assertEquals(getParameterValue(
				OperatorParameter.NAME_outputType, parameters), "VIEW");

		List<String> groupByList = aggregateFiedlModel.getGroupByFieldList();
		Assert.assertEquals(groupByList.get(0), "shop_id");
		Assert.assertEquals(groupByList.get(1), "product_id");

		List<AggregateField> aggregateList = aggregateFiedlModel
				.getAggregateFieldList();
		Assert.assertEquals("sales", aggregateList.get(0).getAlias());
		Assert.assertEquals("sum(\"sales\")", aggregateList.get(0)
				.getAggregateExpression());

		List<WindowField> windowFields = windowFiedlModel.getWindowFieldList();
		Assert.assertEquals(windowFields.get(0).getDataType(), "BIGINT");
		Assert.assertEquals(windowFields.get(0).getResultColumn(), "rank");
		Assert.assertEquals(windowFields.get(0).getWindowFunction(), "rank()");
		Assert.assertEquals(windowFields.get(0).getWindowSpecification(),
				"partition by shop_id order by sum(sales+sales_return) desc");

	}

	/**
	 * Each operator should know his parameter and the order of them
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOperatorParameterNames_aggregate() throws Exception {
		XMLWorkFlowReader reader = new XMLWorkFlowReader();

		String filepath = getTestDataDir() + java.io.File.separator
				+ flow_name_aggregate;
		AbstractReaderParameters para = new XMLFileReaderParameters(filepath,"guest",ResourceType.Personal);

		OperatorWorkFlow workFlow = reader.doRead(para,Locale.getDefault());
		Operator operator = workFlow.getChildList().get(0).getOperator();
		// Each operator know his parameter and the order of parameter
		List<String> parameterNames = operator.getParameterNames();
		Assert.assertEquals(parameterNames.get(0),
				OperatorParameter.NAME_dBConnectionName);
		Assert.assertEquals(parameterNames.get(1),
				OperatorParameter.NAME_schemaName);
		Assert.assertEquals(parameterNames.get(2),
				OperatorParameter.NAME_tableName);

	}

	/**
	 * Each operator should know his parameter and the order of them
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOperatorParameterLabels_en() throws Exception {
		// make sure test the en lable
		// Locale.setDefault(Locale.ROOT);
		 
		XMLWorkFlowReader reader = new XMLWorkFlowReader();

		String filepath = getTestDataDir() + java.io.File.separator
				+ flow_name_aggregate;
		AbstractReaderParameters para = new XMLFileReaderParameters(filepath,"guest",ResourceType.Personal);

		OperatorWorkFlow workFlow = reader.doRead(para,Locale.getDefault());
		Operator operator = workFlow.getChildList().get(0).getOperator();
		// Each operator know his parameter and the order of parameter
		List<String> parameterNames = operator.getParameterNames();
		// this is label name ,can be i18ned
		Assert.assertEquals(OperatorParameterFactory.INSTANCE
				.getHelperByParamName(parameterNames.get(0)).getParameterLabel(
						parameterNames.get(0)).toLowerCase(),
				OperatorParameter.NAME_dBConnectionName.toLowerCase());
		Assert.assertEquals(OperatorParameterFactory.INSTANCE
				.getHelperByParamName(parameterNames.get(1)).getParameterLabel(
						parameterNames.get(1).toLowerCase()),
				OperatorParameter.NAME_schemaName.toLowerCase());
		Assert.assertEquals(OperatorParameterFactory.INSTANCE
				.getHelperByParamName(parameterNames.get(2)).getParameterLabel(
						parameterNames.get(2).toLowerCase()),
				OperatorParameter.NAME_tableName.toLowerCase());

	}

	/**
	 * Each operator should know his parameter and the order of them
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOperatorParameterLabels_zhCN() throws Exception {
		// make sure test the zh_cn lable
		// Locale.setDefault(Locale.CHINESE);
		 

		XMLWorkFlowReader reader = new XMLWorkFlowReader();

		String filepath = getTestDataDir() + java.io.File.separator
				+ flow_name_aggregate;
		AbstractReaderParameters para = new XMLFileReaderParameters(filepath,"guest",ResourceType.Personal);

		OperatorWorkFlow workFlow = reader.doRead(para,Locale.getDefault());
		Operator operator = workFlow.getChildList().get(0).getOperator();
		// Each operator know his parameter and the order of parameter
		List<String> parameterNames = operator.getParameterNames();

		// this is label name ,can be i18ned
//		Assert.assertEquals(OperatorParameterFactory.INSTANCE
//				.getHelperByParamName(parameterNames.get(0)).getParameterLabel(
//						parameterNames.get(0)),"dbConnectionName");
				//"\u6570\u636E\u5E93\u8FDE\u63A5");
//		Assert.assertEquals(OperatorParameterFactory.INSTANCE
//				.getHelperByParamName(parameterNames.get(1)).getParameterLabel(
//						parameterNames.get(1)), "\u6A21\u5F0F\u540D\u79F0");
//		Assert.assertEquals(OperatorParameterFactory.INSTANCE
//				.getHelperByParamName(parameterNames.get(2)).getParameterLabel(
//						parameterNames.get(2)), "\u8868\u540D");

	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFlow_kmeans() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_kmeans);
		List<OperatorParameter> dbTableParameters = workFlow.getChildList()
				.get(0).getOperator().getOperatorParameterList();
		// Connection Demo
		List conns = getAvaliableValues(
				OperatorParameter.NAME_dBConnectionName, dbTableParameters,
				"guest");

		Assert.assertTrue(conns.contains("Connection Demo"));

		// demo
		List schemas = getAvaliableValues(OperatorParameter.NAME_schemaName,
				dbTableParameters, "guest");
		Assert.assertTrue(schemas.contains("demo"));
		Assert.assertTrue(schemas.contains("public"));

		 
		List tableNames = getAvaliableValues(OperatorParameter.NAME_tableName,
				dbTableParameters, "guest");

		Assert.assertTrue(tableNames.contains("iris"));
		Assert.assertTrue(tableNames.contains("water_treatment_sample_0"));
		Assert.assertTrue(tableNames.contains("water_treatment_predict"));
		Operator kmeansOperator = workFlow.getChildList().get( 1).getOperator();
		List<OperatorParameter> kmeansParameters = kmeansOperator.getOperatorParameterList();

		List dropValues = getAvaliableValues(
				OperatorParameter.NAME_dropIfExist, kmeansParameters, "guest");
		Assert.assertEquals(dropValues.get(0), "Yes");
		Assert.assertEquals(dropValues.get(1), "No");

		List idColumns = getAvaliableValues(
				OperatorParameter.NAME_IDColumn_lower, kmeansParameters,
				"guest");
		Assert.assertTrue(idColumns.contains("date"));
		Assert.assertTrue(idColumns.contains("output pH"));
		Assert.assertTrue(idColumns.contains("output sediments"));

		List columnsNames = getAvaliableValues(
				OperatorParameter.NAME_columnNames, kmeansParameters, "guest");
		Assert.assertTrue(columnsNames.contains("date"));
		Assert.assertTrue(columnsNames.contains("output pH"));
		Assert.assertTrue(columnsNames.contains("output sediments"));

		schemas = getAvaliableValues(OperatorParameter.NAME_outputSchema,
				kmeansParameters, "guest");
		Assert.assertTrue(schemas.contains("demo"));
		Assert.assertTrue(schemas.contains("public"));
		validateValue(kmeansOperator,OperatorParameter.NAME_distanse,"Euclidean");
		
		validateValue(kmeansOperator,OperatorParameter.NAME_max_optimization_steps,"50");
 
		
		validateValue(kmeansOperator,OperatorParameter.NAME_max_runs,"10");
		validateValue(kmeansOperator,OperatorParameter.NAME_outputSchema,"demo");
//		validateValue(kmeansOperator,OperatorParameter.NAME_clusterColumnName,"alpine_cluster");
		
		validateValue(kmeansOperator,OperatorParameter.NAME_split_Number,"5");
		
	 
 
		validateValue(kmeansOperator,OperatorParameter.NAME_k,"3");
		validateValue(kmeansOperator,OperatorParameter.NAME_dropIfExist,"Yes");
	 
		validateValue(kmeansOperator,OperatorParameter.NAME_outputTable,"water_treatment_kmeans_result");
		validateValue(kmeansOperator,OperatorParameter.NAME_IDColumn_lower,"date");

	}   

	private void validateValue(Operator operator, String name,
			String value) {

		String paramValue = (String) getParamValue(name, operator);
		Assert.assertEquals(paramValue, value);

		
	}

	private OperatorWorkFlow readFlow(String fileName) throws Exception {
		XMLWorkFlowReader reader = new XMLWorkFlowReader();

		String filepath = getTestDataDir() + java.io.File.separator + fileName;
		AbstractReaderParameters para = new XMLFileReaderParameters(filepath,"guest",ResourceType.Personal);

		OperatorWorkFlow workFlow = reader.doRead(para,Locale.getDefault());
		return workFlow;
	}

	

	/**
	 * @param workFlow
	 * @param string
	 * @return
	 */
	private Operator getOperatorByName(OperatorWorkFlow workFlow, String name) {
		List<UIOperatorModel> list = workFlow.getChildList();
		for (UIOperatorModel uiOperatorModel : list) {
			if (uiOperatorModel.getId().equals(name)) {
				return uiOperatorModel.getOperator();
			}
		}
		return null;
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFlow_svm() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_svm);
		Operator svmPrediction = getOperatorByName(workFlow, "SVM Prediction");
		Operator svmClassification = getOperatorByName(workFlow,
				"SVM Classification");
		Operator svmDetection = getOperatorByName(workFlow,
				"SVM Novelty Detection");
		Operator svmRegression = getOperatorByName(workFlow, "SVM Regression");

		String outPutSchema = (String) getParamValue(
				OperatorParameter.NAME_outputSchema, svmPrediction);
		Assert.assertEquals(outPutSchema, "demo");
		List avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_outputSchema, svmPrediction
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableVlues.contains("demo"));
		Assert.assertTrue(avaliableVlues.contains("public"));

		String outputTable = (String) getParamValue(
				OperatorParameter.NAME_outputTable, svmPrediction);
		Assert.assertEquals(outputTable, "svm_cl_predict");

		String dropIfExist = (String) getParamValue(
				OperatorParameter.NAME_dropIfExist, svmPrediction);
		Assert.assertEquals(dropIfExist, "Yes");

		String dependentColumn = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, svmClassification);
		Assert.assertEquals(dependentColumn, "label");
//		avaliableVlues = getAvaliableValues(
//				OperatorParameter.NAME_dependentColumn, svmClassification
//						.getOperatorParameterList(), "guest");
//		// can not be double
//		Assert.assertTrue(avaliableVlues.contains("id"));
 
 

		String value = (String) getParamValue(OperatorParameter.NAME_gamma,
				svmClassification);
		Assert.assertEquals(value, "0.1");

		value = (String) getParamValue(OperatorParameter.NAME_nu,
				svmClassification);
		Assert.assertEquals(value, "0.2");

		value = (String) getParamValue(OperatorParameter.NAME_eta,
				svmClassification);
		Assert.assertEquals(value, "0.05");
		validateValue(svmClassification, OperatorParameter.NAME_degree, "2") ;

		String kerneltype = (String) getParamValue(
				OperatorParameter.NAME_kernel_type, svmClassification);
		Assert.assertEquals(Integer.parseInt(kerneltype), kernelList
				.indexOf(OperatorParameterFactory.VALUE_DOT_PRODUCT) + 1);
		isKerneltypeParameter(svmClassification,
				OperatorParameter.NAME_kernel_type);

		String forceRetrain = (String) getParamValue(
				OperatorParameter.NAME_forceRetrain, svmClassification);
		Assert.assertEquals(forceRetrain, "Yes");
		isForcereTrainParameter(svmClassification,
				OperatorParameter.NAME_forceRetrain);

		kerneltype = (String) getParamValue(OperatorParameter.NAME_kernel_type,
				svmDetection);
		Assert.assertEquals(Integer.parseInt(kerneltype), kernelList
				.indexOf(OperatorParameterFactory.VALUE_DOT_PRODUCT) + 1);
		isKerneltypeParameter(svmDetection, OperatorParameter.NAME_kernel_type);

		forceRetrain = (String) getParamValue(
				OperatorParameter.NAME_forceRetrain, svmDetection);
		Assert.assertEquals(forceRetrain, "Yes");
		isForcereTrainParameter(svmDetection,
				OperatorParameter.NAME_forceRetrain);

		dependentColumn = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, svmRegression);
		Assert.assertEquals(dependentColumn, "label");
		avaliableVlues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, svmRegression
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableVlues.contains("a"));
		Assert.assertTrue(avaliableVlues.contains("b"));

		kerneltype = (String) getParamValue(OperatorParameter.NAME_kernel_type,
				svmRegression);
		Assert.assertEquals(Integer.parseInt(kerneltype), kernelList
				.indexOf(OperatorParameterFactory.VALUE_DOT_PRODUCT) + 1);
		isKerneltypeParameter(svmRegression, OperatorParameter.NAME_kernel_type);

		forceRetrain = (String) getParamValue(
				OperatorParameter.NAME_forceRetrain, svmRegression);
		Assert.assertEquals(forceRetrain, "Yes");
		isForcereTrainParameter(svmRegression,
				OperatorParameter.NAME_forceRetrain);
		
		validateValue(svmRegression, OperatorParameter.NAME_degree, "2") ;
		validateValue(svmRegression, OperatorParameter.NAME_nu, "0.001") ;
		validateValue(svmRegression, OperatorParameter.NAME_gamma, "0.1") ;
		validateValue(svmRegression, OperatorParameter.NAME_eta, "0.2") ;
		validateValue(svmRegression, OperatorParameter.NAME_lambda, "0.2") ;

	}

	private void isKerneltypeParameter(Operator svmClassification,
			String paramname) {
		List avaliableVlues;
		avaliableVlues = getAvaliableValues(paramname, svmClassification
				.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableVlues
				.contains(OperatorParameterFactory.VALUE_POLYNOMINAL));
		Assert.assertTrue(avaliableVlues
				.contains(OperatorParameterFactory.VALUE_GAUSSIAN));
	}

	private void isForcereTrainParameter(Operator svmClassification,
			String paramName) {
		List avaliableVlues;
		avaliableVlues = getAvaliableValues(paramName, svmClassification
				.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableVlues.contains("Yes"));
		Assert.assertTrue(avaliableVlues.contains("No"));
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFlow_timeseries() throws Exception {

		OperatorWorkFlow workFlow = readFlow(flow_name_time_series);
		Operator timeSeries = getOperatorByName(workFlow, "Time Series");

		String idColumn = (String) getParamValue(
				OperatorParameter.NAME_IDColumn_lower, timeSeries);
		Assert.assertEquals(idColumn, "date");
//		List avaliableVlues = getAvaliableValues(
//				OperatorParameter.NAME_IDColumn_lower, timeSeries
//						.getOperatorParameterList(), "guest");
//		Assert.assertTrue(avaliableVlues.contains("id"));
//		Assert.assertTrue(avaliableVlues.contains("value"));

		String valueColumn = (String) getParamValue(
				OperatorParameter.NAME_ValueColumn, timeSeries);
		Assert.assertEquals(valueColumn, "price");
//		List avaliableVlues = getAvaliableValues(OperatorParameter.NAME_ValueColumn,
//				timeSeries.getOperatorParameterList(), "guest");
//		Assert.assertTrue(avaliableVlues.contains("id"));
//		Assert.assertTrue(avaliableVlues.contains("value"));

		Assert.assertEquals(getParamValue(OperatorParameter.NAME_AR_Order,
				timeSeries), "5");
		Assert.assertEquals(getParamValue(OperatorParameter.NAME_MA_Order,
				timeSeries), "2");
		validateValue(timeSeries, OperatorParameter.NAME_Degree_of_differencing, "0");
		
		Operator timeSeriesPrediction = getOperatorByName(workFlow,
				"Time Series Prediction");

//		Assert.assertEquals(getParamValue(OperatorParameter.NAME_Ahead_Number,
//				timeSeriesPrediction), "10");
 
	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFlow_barchart() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_barchart);
		Operator barChart = getOperatorByName(workFlow, "Bar Chart");

		String value = (String) getParamValue(
				OperatorParameter.NAME_valueDomain, barChart);
		Assert.assertEquals(value, "sales");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_valueDomain, barChart
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("sales"));
		Assert.assertTrue(avaliableValues.contains("quarter"));
		Assert.assertFalse(avaliableValues.contains("region"));

		value = (String) getParamValue(OperatorParameter.NAME_scopeDomain,
				barChart);
		Assert.assertEquals(value, "region");
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_scopeDomain, barChart
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("sales"));
		Assert.assertTrue(avaliableValues.contains("quarter"));

		value = (String) getParamValue(OperatorParameter.NAME_categoryType,
				barChart);
		Assert.assertEquals(value, "quarter");
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_categoryType, barChart
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("sales"));
		Assert.assertTrue(avaliableValues.contains("region"));

	}

	/**
	 * Test method for
	 * {@link com.alpine.miner.impls.web.resource.ResourceManager#getInstance()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFlow_carttree() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_cart_tree);
		Operator rowFilter = getOperatorByName(workFlow, "Row Filter");
		Operator normalization = getOperatorByName(workFlow, "Normalization");
		Operator randomSampling = getOperatorByName(workFlow, "Random Sampling");

		Operator sampleSelector = getOperatorByName(workFlow, "Sample Selector");
		Operator ROC = getOperatorByName(workFlow, "ROC");

		Operator cartTree = getOperatorByName(workFlow, "CART Tree");
		Operator treePrediction = getOperatorByName(workFlow, "Tree Prediction");

		validateRowFilter(rowFilter);
		validateNormalization(normalization);
//		validateRandomSampling(randomSampling);
		validateSampleSelector(sampleSelector);
		validateROC(ROC);
		validateCartTree(cartTree);
		validateCartTreePrediction(treePrediction);

	}

	/**
	 * @param normalization
	 */

	/**
	 * @param treePrediction
	 */
	private void validateCartTreePrediction(Operator operator) {

		String value = (String) getParamValue(
				OperatorParameter.NAME_outputSchema, operator);
		Assert.assertEquals(value, "demo");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_outputSchema, operator
						.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("demo"));
		Assert.assertTrue(avaliableValues.contains("public"));

		value = (String) getParamValue(OperatorParameter.NAME_outputTable,
				operator);
		Assert.assertEquals(value, "water_treatment_cart_result");

		// TODO Auto-generated method stub
		value = (String) getParamValue(OperatorParameter.NAME_dropIfExist,
				operator);
		Assert.assertEquals(value, "Yes");
		isYesNoOption(operator, OperatorParameter.NAME_dropIfExist);
	}

	/**
	 * @param cartTree
	 */
	private void validateCartTree(Operator operator) {
		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, operator);
		Assert.assertEquals(value, "dependent_colum");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, operator
						.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!

		Assert.assertTrue(avaliableValues
				.contains("input suspended solids to primary settler"));

		value = (String) getParamValue(OperatorParameter.NAME_maximal_depth,
				operator);
		Assert.assertEquals(value, "5");

		value = (String) getParamValue(OperatorParameter.NAME_forceRetrain,
				operator);
		Assert.assertEquals(value, "Yes");
		isForcereTrainParameter(operator, OperatorParameter.NAME_forceRetrain);

		value = (String) getParamValue(OperatorParameter.NAME_no_pruning,
				operator);
		Assert.assertEquals(value, "false");
		
		validateValue(operator, OperatorParameter.NAME_minimal_size_for_split, "4");
 
		validateValue(operator, OperatorParameter.NAME_number_of_prepruning_alternatives,"3");
		validateValue(operator, OperatorParameter.NAME_size_threshold_load_data,"10000");
		validateValue(operator, OperatorParameter.NAME_maximal_depth,"5");
		validateValue(operator, OperatorParameter.NAME_confidence,"0.25");
		validateValue(operator, OperatorParameter.NAME_minimal_leaf_size,"2");
		validateValue(operator, OperatorParameter.NAME_no_pre_pruning,"false");

	}

	private void isTrueFalseOption(Operator operator, String paramName) {
		List avaliableValues = getAvaliableValues(paramName, operator
				.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("true"));
		Assert.assertTrue(avaliableValues.contains("false"));
	}

	/**
	 * @param rOC
	 */
	private void validateROC(Operator operator) {
		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, operator);
		Assert.assertEquals(value, "dependent_colum");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, operator
						.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues
				.contains("input suspended solids to primary settler"));

		value = (String) getParamValue(OperatorParameter.NAME_columnValue,
				operator);
		Assert.assertEquals(value, "yes");

		value = (String) getParamValue(OperatorParameter.NAME_useModel,
				operator);
		Assert.assertEquals(value, "true");
//		avaliableValues = getAvaliableValues(OperatorParameter.NAME_useModel,
//				operator.getOperatorParameterList(), "guest");
//		// this is fixed by sam and code not in yet!
//		Assert.assertTrue(avaliableValues.contains("true"));
//		Assert.assertTrue(avaliableValues.contains("false"));

	}

	/**
	 * @param sampleSelector
	 */
	private void validateSampleSelector(Operator operator) {
		String value = (String) getParamValue(
				OperatorParameter.NAME_selectedTable, operator);
		Assert.assertEquals(value, "demo.water_treatment_sample_0");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_selectedTable, operator
						.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		 Assert.assertTrue(avaliableValues.contains("demo.water_treatment_sample_0"));
		 Assert.assertTrue(avaliableValues.contains("demo.water_treatment_sample_1"));

	}

	/**
	 * @param randomSampling
	 */
	private void validateRandomSampling(Operator operator) {
		String value = (String) getParamValue(
				OperatorParameter.NAME_sampleSizeType, operator);
		Assert.assertEquals(value, "Percentage");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_sampleSizeType, operator
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("Percentage"));
		Assert.assertTrue(avaliableValues.contains("Row"));

		value = (String) getParamValue(OperatorParameter.NAME_sampleSize,
				operator);
		Assert.assertEquals(value, "50");
		value = (String) getParamValue(OperatorParameter.NAME_outputType,
				operator);
		Assert.assertEquals(value, "TABLE");
		isOutPutType(operator, OperatorParameter.NAME_outputType);

		value = (String) getParamValue(OperatorParameter.NAME_outputTable,
				operator);
		Assert.assertEquals(value, "water_treatment_sample");

		value = (String) getParamValue(OperatorParameter.NAME_dropIfExist,
				operator);
		Assert.assertEquals(value, "Yes");
		isYesNoOption(operator, OperatorParameter.NAME_dropIfExist);
		validateValue(operator, OperatorParameter.NAME_keyColumnList, "date");
 
		validateValue(operator, OperatorParameter.NAME_sampleCount,"2");
		validateValue(operator, OperatorParameter.NAME_randomSeed,"0.2");
		 
		validateValue(operator, OperatorParameter.NAME_outputSchema,"demo");
		validateValue(operator, OperatorParameter.NAME_disjoint,"true");
		 
		validateValue(operator, OperatorParameter.NAME_consistent,"true");

	}

	/**
	 * DevideByAverage-Transformation",
	 * "Proportion-Transformation","Range-Transformation","Z-Transformation"
	 * */
	private void validateNormalization(Operator operator) {
		String value = (String) getParamValue(OperatorParameter.NAME_method,
				operator);
		Assert.assertEquals(value, "Proportion-Transformation");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_method, operator
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("Range-Transformation"));
		Assert.assertTrue(avaliableValues.contains("Z-Transformation"));

		value = (String) getParamValue(OperatorParameter.NAME_outputType,
				operator);
		Assert.assertEquals(value, "TABLE");

		isOutPutType(operator, OperatorParameter.NAME_outputType);

		value = (String) getParamValue(OperatorParameter.NAME_outputTable,
				operator);
		Assert.assertEquals(value, "water_treatment_normalized");

		value = (String) getParamValue(OperatorParameter.NAME_dropIfExist,
				operator);
		Assert.assertEquals(value, "Yes");
		isYesNoOption(operator, OperatorParameter.NAME_dropIfExist);
		
		validateValue(operator, OperatorParameter.NAME_rangeMin,"");
 
		validateValue(operator, OperatorParameter.NAME_rangeMax,"");
 

	}

	private void validateRowFilter(Operator rowFilter) {
		String value = (String) getParamValue(
				OperatorParameter.NAME_outputType, rowFilter);
		Assert.assertEquals(value, "VIEW");

		isOutPutType(rowFilter, OperatorParameter.NAME_outputType);

		value = (String) getParamValue(OperatorParameter.NAME_outputTable,
				rowFilter);
		Assert.assertEquals(value, "water_treatment_filtered");

		value = (String) getParamValue(OperatorParameter.NAME_dropIfExist,
				rowFilter);
		Assert.assertEquals(value, "Yes");
		isYesNoOption(rowFilter, OperatorParameter.NAME_dropIfExist);
	}

	private void isOutPutType(Operator rowFilter, String paramName) {
		List avaliableValues = getAvaliableValues(paramName, rowFilter
				.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("VIEW"));
		Assert.assertTrue(avaliableValues.contains("TABLE"));
	}

	private void isYesNoOption(Operator operator, String paramName) {
		List avaliableValues;
		avaliableValues = getAvaliableValues(paramName, operator
				.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("Yes"));
		Assert.assertTrue(avaliableValues.contains("No"));
	}

	@Test
	public void testFlow_nueralNetWork() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_neural_network);
		Operator neuralNetWork = getOperatorByName(workFlow, "Neural Network");
		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, neuralNetWork);
		Assert.assertEquals(value, "dependent_column");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, neuralNetWork
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues
				.contains("input Biological demand of oxygen to plant"));
		isYesNoOption(neuralNetWork, OperatorParameter.NAME_forceRetrain);

		value = (String) getParamValue(OperatorParameter.NAME_decay,
				neuralNetWork);
		Assert.assertEquals(value, "false");
		value = (String) getParamValue(OperatorParameter.NAME_normalize,
				neuralNetWork);
		Assert.assertEquals(value, "true");

		value = (String) getParamValue(OperatorParameter.NAME_adjust_per,
				neuralNetWork);
		Assert.assertEquals(value, "ROW");
		avaliableValues = getAvaliableValues(OperatorParameter.NAME_adjust_per,
				neuralNetWork.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("ALL"));

		
		value = (String) getParamValue(OperatorParameter.NAME_fetchsize,
				neuralNetWork);
		Assert.assertEquals(value, "10000");

		value = (String) getParamValue(OperatorParameter.NAME_momentum,
				neuralNetWork);
		Assert.assertEquals(value, "0.2");

		value = (String) getParamValue(OperatorParameter.NAME_training_cycles,
				neuralNetWork);
		Assert.assertEquals(value, "500");

		value = (String) getParamValue(OperatorParameter.NAME_learning_rate,
				neuralNetWork);
		Assert.assertEquals(value, "0.3");

		value = (String) getParamValue(OperatorParameter.NAME_hidden_layers,
				neuralNetWork);
		Assert.assertEquals(value, null);

		value = (String) getParamValue(
				OperatorParameter.NAME_local_random_seed, neuralNetWork);
		Assert.assertEquals(value, "-1");

		value = (String) getParamValue(OperatorParameter.NAME_error_epsilon,
				neuralNetWork);
		Assert.assertEquals(value, "0.00001"); 
		
		
		Operator netWorkPrediction = getOperatorByName(workFlow,
				"Neural Network Prediction");

		value = (String) getParamValue(OperatorParameter.NAME_outputSchema,
				netWorkPrediction);
		Assert.assertEquals(value, "demo");
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_outputSchema, netWorkPrediction
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("demo"));
		Assert.assertTrue(avaliableValues.contains("public"));

		isYesNoOption(netWorkPrediction, OperatorParameter.NAME_dropIfExist);

		
	}

	@Test
	public void testFlow_lieanerRegression() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_linear_regression);
		Operator linearRegression = getOperatorByName(workFlow,
				"Linear Regression");
		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, linearRegression);
		Assert.assertEquals(value, "quality");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, linearRegression
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("pH"));
		isYesNoOption(linearRegression, OperatorParameter.NAME_forceRetrain);
	}

	@Test
	public void testFlow_logisticRegression() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_logistic_regression);
		Operator logisticRegression = getOperatorByName(workFlow,
				"Logistic Regression");

		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, logisticRegression);
		Assert.assertEquals(value, "dependent_column");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, logisticRegression
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues
				.contains("input conductivity to secondary settler"));
		isYesNoOption(logisticRegression, OperatorParameter.NAME_forceRetrain);

		value = (String) getParamValue(OperatorParameter.NAME_goodValue,
				logisticRegression);
		Assert.assertEquals(value, "yes");
		value = (String) getParamValue(OperatorParameter.NAME_max_generations,
				logisticRegression);
		Assert.assertEquals(value, "25");

		value = (String) getParamValue(OperatorParameter.ConstEpsilon_LR,
				logisticRegression);
		Assert.assertNotNull(value);
		Operator logisticRegressionPrediction = getOperatorByName(workFlow,
				"Logistic Regression Preidction");

		value = (String) getParamValue(OperatorParameter.NAME_outputSchema,
				logisticRegressionPrediction);
		Assert.assertEquals(value, "demo");
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_outputSchema,
				logisticRegressionPrediction.getOperatorParameterList(),
				"guest");
		Assert.assertTrue(avaliableValues.contains("demo"));
		Assert.assertTrue(avaliableValues.contains("public"));

		isYesNoOption(logisticRegressionPrediction,
				OperatorParameter.NAME_dropIfExist);

		value = (String) getParamValue(OperatorParameter.NAME_outputTable,
				logisticRegressionPrediction);
		Assert.assertEquals(value,
				"water_treatment_logistic_regression_predict_result");

	}

	@Test
	public void testFlow_correlation() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_Correlation);
		Operator operator = getOperatorByName(workFlow, "Correlation Analysis");

		String value = (String) getParamValue(
				OperatorParameter.NAME_columnNames, operator);
		Assert.assertTrue(value.indexOf("output pH") > 0);
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_columnNames, operator
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues
				.contains("input conductivity to secondary settler"));

	}

	@Test
	public void testFlow_univerate() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_univariate_);
		Operator operaotr = getOperatorByName(workFlow, "Univairate");

		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, operaotr);
		Assert.assertEquals(value, "play");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, operaotr
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains("outlook"));
		Assert.assertTrue(avaliableValues.contains("wind"));

		value = (String) getParamValue(OperatorParameter.NAME_goodValue,
				operaotr);
		Assert.assertEquals(value, "yes");

		value = (String) getParamValue(OperatorParameter.NAME_max_generations,
				operaotr);
		Assert.assertEquals(value, "25");

	}

	@Test
	public void testFlow_nb() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_NB);
		Operator operator = getOperatorByName(workFlow, "Naive Bayes");

		String value = (String) getParamValue(
				OperatorParameter.NAME_dependentColumn, operator);
		Assert.assertEquals(value, "dependent_column");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_dependentColumn, operator
						.getOperatorParameterList(), "guest"); 
		Assert
				.assertTrue(avaliableValues
						.contains("input Biological demand of oxygen to primary settler"));

		value = (String) getParamValue(OperatorParameter.NAME_forceRetrain,
				operator);
		Assert.assertEquals(value, "Yes");
		isForcereTrainParameter(operator, OperatorParameter.NAME_forceRetrain);

		value = (String) getParamValue(
				OperatorParameter.NAME_isCalculateDeviance, operator);
		Assert.assertEquals(value, "false");

	}
	@Test
	public void testFlow_association() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_Association);
		Operator operator = getOperatorByName(workFlow, "Association");

		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_expression, operator
						.getOperatorParameterList(), "guest");
		Assert.assertTrue(avaliableValues.contains(">"));
		Assert.assertTrue(avaliableValues.contains(">="));
		Assert.assertTrue(avaliableValues.contains("<>"));
		validateValue(operator, OperatorParameter.NAME_Use_Array,"false");
		validateValue(operator, OperatorParameter.NAME_tableSizeThreshold,"10000000");
		validateValue(operator, OperatorParameter.NAME_outputSchema,"demo");
		validateValue(operator, OperatorParameter.NAME_minConfidence,"0.8");
		validateValue(operator, OperatorParameter.NAME_dropIfExist,"Yes");
		validateValue(operator, OperatorParameter.NAME_outputTable,"association_output");
		validateValue(operator, OperatorParameter.NAME_minSupport,"0.1");


	}
	
	
	@Test
	public void test_TableJoin() throws Exception{
		
		OperatorWorkFlow workFlow = readFlow(flow_name_sample_house_price_cart);
		Operator operator = getOperatorByName(workFlow, "housing_info");

		validateValue(operator, OperatorParameter.NAME_createSequenceID,"No");
		isYesNoOption(operator, OperatorParameter.NAME_createSequenceID) ;
		isYesNoOption(operator, OperatorParameter.NAME_dropIfExist) ;
		validateValue(operator, OperatorParameter.NAME_outputTable,"housing_info");
		 
	}

	@Test
	public void test_sample_algorithm_comparison() throws Exception {

		OperatorWorkFlow workFlow = readFlow(flow_name_sample_algorithm_comparison);
		Operator operator = getOperatorByName(workFlow, "Lift");

		validateValue(operator, OperatorParameter.NAME_columnValue, "yes");
		validateValue(operator, OperatorParameter.NAME_dependentColumn,"dependent_column");

		validateValue(operator, OperatorParameter.NAME_useModel, "true");
		 

		operator = getOperatorByName(workFlow, "Goodness Of Fit");

		validateValue(operator, OperatorParameter.NAME_useModel, "true");
		 
		 
	}
		
	
	
	@Test
	public void testFlow_SVD() throws Exception {
		OperatorWorkFlow workFlow = readFlow(flow_name_sample_svd);
		Operator svdOperator = getOperatorByName(workFlow, "SVD");
		Operator svdCalculator = getOperatorByName(workFlow, "SVD Calculator");
		Operator pivotOperator  = getOperatorByName(workFlow, "Pivot");
		
		
		Operator numericToText = getOperatorByName(workFlow, "NumericToText");
		Operator nullValueReplace = getOperatorByName(workFlow, "Null Value Replacement");


//		validateSVD(svdOperator);
//		validateSVDCalculator(svdCalculator);
//		validatePivot(pivotOperator);
//		validateNumericToText(numericToText);
//		validateNullValueReplace(nullValueReplace);

	}

	private void validateSVD(Operator operator) {

		// uschema + vschema
		validateValue(operator, OperatorParameter.NAME_UmatrixSchema, "demo");
		validateValue(operator, OperatorParameter.NAME_VmatrixSchema, "demo");

		// udrop + vdrop
		validateValue(operator, OperatorParameter.NAME_UmatrixDropIfExist,
				"Yes");
		validateValue(operator, OperatorParameter.NAME_VmatrixDropIfExist,
				"Yes");

		validateValue(operator, OperatorParameter.NAME_UmatrixTable, "dfg");
		validateValue(operator, OperatorParameter.NAME_VmatrixTable, "dgaa");

		validateValue(operator, OperatorParameter.NAME_dependentColumn, "value");

		// columns name selection
		validateValue(operator, OperatorParameter.NAME_ColName, "item");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_ColName,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("item"));
		Assert.assertTrue(avaliableValues.contains("user_id"));
		validateValue(operator, OperatorParameter.NAME_RowName, "item");
		avaliableValues = getAvaliableValues(OperatorParameter.NAME_RowName,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("item"));
		Assert.assertTrue(avaliableValues.contains("user_id"));
		
		
		validateValue(operator, OperatorParameter.NAME_FastSpeedupConst,"10.0");
		validateValue(operator, OperatorParameter.NAME_SlowdownConst,"0.1");
		validateValue(operator, OperatorParameter.NAME_NumFeatures,"2");
		validateValue(operator, OperatorParameter.NAME_RowName,"item");
		validateValue(operator, OperatorParameter.NAME_SpeedupConst,"1.1");
		validateValue(operator, OperatorParameter.NAME_MinNumIterations,"1");
		validateValue(operator, OperatorParameter.NAME_InitValue,"0.1");
		validateValue(operator, OperatorParameter.NAME_OriginalStep,"10.0");
		validateValue(operator, OperatorParameter.NAME_NumIterations,"10");
		validateValue(operator, OperatorParameter.NAME_MinImprovement,"1.0");
		
		

	}

	private void validateSVDCalculator(Operator operator) {
		validateValue(operator, OperatorParameter.NAME_outputTable, "svd_store_output");

		validateValue(operator, OperatorParameter.NAME_outputSchema, "demo");

		validateValue(operator, OperatorParameter.NAME_dropIfExist, "Yes");
		
		validateValue(operator, OperatorParameter.NAME_CrossProduct, "true");
		//validateValue(operator, OperatorParameter.NAME_KeyValue, "");
		validateValue(operator, OperatorParameter.NAME_KeyColumn, "");
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_KeyColumn,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("item"));
		Assert.assertTrue(avaliableValues.contains("user_id"));
		
		
		//belowing is uneditable in the new release
		validateValue(operator, OperatorParameter.NAME_UmatrixTable,"\"demo\".\"svd_v\"");
		validateValue(operator, OperatorParameter.NAME_VmatrixTable,"\"demo\".\"svd_u\"");
		validateValue(operator, OperatorParameter.NAME_VdependentColumn,"value");
		validateValue(operator, OperatorParameter.NAME_UfeatureColumn,"alpine_feature");
		validateValue(operator, OperatorParameter.NAME_RowName,"item");
		validateValue(operator, OperatorParameter.NAME_VfeatureColumn,"alpine_feature");
		validateValue(operator, OperatorParameter.NAME_UdependentColumn,"value");
		validateValue(operator, OperatorParameter.NAME_ColName,"user_id");
	 
	}

	private void validatePivot(Operator operator) {
		validateValue(operator, OperatorParameter.NAME_outputTable, "xxx");

		validateValue(operator, OperatorParameter.NAME_outputSchema, "demo");

		validateValue(operator, OperatorParameter.NAME_dropIfExist, "Yes");
		validateValue(operator, OperatorParameter.NAME_outputType, "TABLE");

		validateValue(operator, OperatorParameter.NAME_pivotColumn, "item");
		// only one item (category type)
		List avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_pivotColumn,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.size() == 1);

		validateValue(operator, OperatorParameter.NAME_Use_Array, "true");
		isTrueFalseOption(operator, OperatorParameter.NAME_Use_Array);

		// all

		validateValue(operator, OperatorParameter.NAME_groupByColumn, "user_id");
		// only one item (category type)
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_groupByColumn,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("item"));
		Assert.assertTrue(avaliableValues.contains("value"));
		// numeric
		validateValue(operator, OperatorParameter.NAME_aggregateColumn, "value");
		// only one item (category type)
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_aggregateColumn,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("user_id"));
		Assert.assertFalse(avaliableValues.contains("item"));

		validateValue(operator, OperatorParameter.NAME_aggregateType, "sum");
		// only one item (category type)
		avaliableValues = getAvaliableValues(
				OperatorParameter.NAME_aggregateType,
				operator.getOperatorParameterList(), "guest");
		// this is fixed by sam and code not in yet!
		Assert.assertTrue(avaliableValues.contains("avg"));
		Assert.assertTrue(avaliableValues.contains("count"));
		Assert.assertTrue(avaliableValues.contains("max"));
		Assert.assertTrue(avaliableValues.contains("min"));

	}

	private void validateNumericToText(Operator operator) {
		
		validateValue(operator, OperatorParameter.NAME_outputTable,"yyy");
		validateValue(operator, OperatorParameter.NAME_modifyOriginTable,"false");
		isTrueFalseOption(operator,OperatorParameter.NAME_modifyOriginTable) ;
		
		validateValue(operator, OperatorParameter.NAME_outputSchema,"demo");
	 
		validateValue(operator, OperatorParameter.NAME_dropIfExist,"Yes");
		validateValue(operator, OperatorParameter.NAME_outputType,"TABLE");
		
		
		
	}

	private void validateNullValueReplace(Operator operator) {
		
		validateValue(operator, OperatorParameter.NAME_replacement_config, "'',0,0");
		validateValue(operator, OperatorParameter.NAME_outputTable,"zzz");
		validateValue(operator, OperatorParameter.NAME_outputSchema,"demo");
		validateValue(operator, OperatorParameter.NAME_columnNames,"item,value,user_id");
	 
		validateValue(operator, OperatorParameter.NAME_dropIfExist,"Yes");
		validateValue(operator, OperatorParameter.NAME_outputType,"TABLE");
 
		
	}

	private String getTestDataDir() {
		//File f = new File("");
		// f is root of web project, like
		// :home/zhaoyong/dev/workspace/AlpineMinerWeb
		return test_data.getAbsolutePath();//f.getAbsolutePath() + File.separator + "test_data";
	}

}
