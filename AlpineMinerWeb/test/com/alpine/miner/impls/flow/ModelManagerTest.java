/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ModelManagerTest.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.flow;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ModelInfo;
import com.alpine.miner.interfaces.AnalysisModelManager;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.reader.AbstractReaderParameters;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;

public class ModelManagerTest extends AbstractFlowTest {
	private static final String TEMP_MODEL_NAME = "tempmodel";

	private static final String MODEL_TYPE_DECISION_TREE = "DecisionTreeModel";

	private static final String DIR_MODEL = "model";

	private static final String TEST_MODEL_NAME_TREE = "CART Tree"; 
	
	private String predict_carttree="predict_carttree" ;
	private String train_cart_tree="train_cart_tree" ; 

	

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static  void setUpBeforeModelManagerTestClass() throws Exception {
		//prepaire the data...
		String guestFlowDir = FilePersistence.FLOWPREFIX+ResourceType.Personal+File.separator+USER_GUEST;
		//clear guest' flow
		FileUtils.deleteDirectory(new File (guestFlowDir)) ;
		//copy the tested model flow ..
        String modelFlowDir = new File(test_data +File.separator+ DIR_MODEL+File.separator+"flow").getAbsolutePath();
		FileUtils.copyDirectory(new File(modelFlowDir), new File(guestFlowDir)) ;
		String modelDir = new File(test_data +File.separator+ DIR_MODEL+File.separator+"model").getAbsolutePath();
		FileUtils.copyDirectory(new File(modelDir), new File(FilePersistence.MODELPRFIX)) ;
		
	}
	



	@Test
	public void testSaveModel() throws Exception {
		ModelInfo modelInfo = new ModelInfo(USER_GUEST, ResourceType.Personal, String.valueOf(System.currentTimeMillis()),
				TEMP_MODEL_NAME, MODEL_TYPE_DECISION_TREE, train_cart_tree) ;
		EngineModel engineModel = new EngineModel(); 
		AnalysisModelManager.INSTANCE.saveEngineModel(modelInfo, engineModel) ;
		
		List<ModelInfo> modelList = AnalysisModelManager.INSTANCE.getModelInfoList(USER_GUEST,
				train_cart_tree, TEMP_MODEL_NAME,Locale.getDefault());
		
		Assert.assertTrue(modelList.size()>0) ;
	}
	
//	@Test
	public void testReplaceModel() throws Exception {
		List<ModelInfo> modelList = AnalysisModelManager.INSTANCE.getModelInfoList(USER_GUEST,
				train_cart_tree, TEST_MODEL_NAME_TREE,Locale.getDefault());
		
//		FlowInfo result = AnalysisModelManager.INSTANCE.replaceModel(USER_GUEST, predict_carttree, "train_cart_treeModel", modelList.get(1),
//				ResourceType.Personal,Locale.getDefault());
//
//		 Assert.assertTrue(result!=null) ;
//		 result = AnalysisModelManager.INSTANCE.replaceModel(USER_GUEST, predict_carttree, "xxxxxxxx", modelList.get(1),
//					ResourceType.Personal,Locale.getDefault());
//
			// Assert.assertTrue(result==null) ;
	}
	
	@Test
	public void testGetModelList() throws Exception {
		List<ModelInfo> modelList = AnalysisModelManager.INSTANCE.getModelInfoList(USER_GUEST,
				train_cart_tree, TEST_MODEL_NAME_TREE,Locale.getDefault());
		Assert.assertTrue(modelList.size()>0) ;
	}
 
	@Test
	public void testSaveModelInFlowFile() throws Exception {
		

		ModelInfo modelInfo = new ModelInfo(USER_GUEST, ResourceType.Personal, String.valueOf(System.currentTimeMillis()),
				TEMP_MODEL_NAME, MODEL_TYPE_DECISION_TREE, train_cart_tree) ;
 
		EngineModel engineModel = new EngineModel(); 
		AnalysisModelManager.INSTANCE.saveEngineModel(modelInfo, engineModel) ;
		
		String tempDir = System.getProperty( "java.io.tmpdir"); 
		String filePath = tempDir + File.separator + System.currentTimeMillis()+".afm";
		AnalysisModelManager.INSTANCE.saveModelInFlowFile(filePath, modelInfo,Locale.getDefault()) ;		
	 		
		XMLWorkFlowReader reader = new XMLWorkFlowReader();
 		AbstractReaderParameters para = new XMLFileReaderParameters(filePath,USER_GUEST,ResourceType.Personal);

		OperatorWorkFlow workFlow = reader.doRead(para,Locale.getDefault());
		Operator modelOperator = workFlow.getChildList().get(0).getOperator();
		
		Assert.assertTrue(modelOperator instanceof ModelOperator) ;
	}
	

	@Test 
	public void testGetModelVisualization() throws Exception{
		List<ModelInfo> modelList = AnalysisModelManager.INSTANCE.getModelInfoList(USER_GUEST,
				train_cart_tree, TEST_MODEL_NAME_TREE, Locale.getDefault());
		if(modelList!=null){
			for(int i=0;i<modelList.size();i++){
				String vModel = AnalysisModelManager.INSTANCE.getModelVisualization(modelList.get(i), Locale.getDefault()); 
			if(modelList.get(i)!=null&&modelList.get(i).getModelName().endsWith(TEMP_MODEL_NAME)){
				Assert.assertNull(vModel) ;
			}else{
				Assert.assertNotNull(vModel) ;
			}
		
		}
		
		}
				
			//test null model
		ModelInfo modelInfo = new ModelInfo(USER_GUEST, ResourceType.Personal, "xx",
				"xx", "xx", "xx") ;
		 AnalysisModelManager.INSTANCE.getModelVisualization(modelInfo, Locale.getDefault());
	}
	
	
	
	@Test
	public void testDeleteModel() throws Exception {
		String id = String.valueOf(System.currentTimeMillis());
		ModelInfo modelInfo = new ModelInfo(USER_GUEST, ResourceType.Personal, id,
				TEMP_MODEL_NAME, MODEL_TYPE_DECISION_TREE, train_cart_tree) ;
		EngineModel engineModel = new EngineModel(); 
		AnalysisModelManager.INSTANCE.saveEngineModel(modelInfo, engineModel) ;
		
		 AnalysisModelManager.INSTANCE.deleteModel( modelInfo); 
			
			List<ModelInfo> modelList = AnalysisModelManager.INSTANCE.getModelInfoList(USER_GUEST,
					train_cart_tree, TEMP_MODEL_NAME,Locale.getDefault());
			
			for(int i = 0 ;i<modelList.size();i++){
				ModelInfo model = modelList.get(i);
				if(model.getId().equals(id)){
					//failed
					Assert.assertTrue(false) ;
					return;
				}
			}
			//success
		Assert.assertTrue(true) ;
	}
	
	
}
