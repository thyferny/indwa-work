/**
 *
 * Version information: 1.00
 *
 * Data: 2011-7-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.saver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.Resources;
import com.alpine.miner.util.SerializeUtil;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.reader.FlowMigrator;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

public class XMLWorkFlowSaver {
    private static final Logger itsLogger=Logger.getLogger(XMLWorkFlowSaver.class);

    public static final String XML_TAG_UUID="uuid";
	public static final String connString=com.alpine.utility.db.Resources.FieldSeparator;

	public void doSave(String path,OperatorWorkFlow ow,boolean addSuffixToOutput) throws Exception{
		doSave(path,ow,null,addSuffixToOutput);
	}
		
	public void doSave(String path,OperatorWorkFlow ow,String userName,boolean addSuffixToOutput) throws Exception{
		Document xmlDoc = createDocument(ow,userName,addSuffixToOutput);
		File file = new File(path);
		XmlDocManager xmlDocManager = new XmlDocManager();
		try {
			BufferedWriter writer = null;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),XmlDocManager.ENCODING_UTF8));
//			writer = new BufferedWriter(new FileWriter(file));
			writer.write(xmlDocManager.xmlToLocalString(xmlDoc)); 
			writer.close();
		} catch (Exception e) {
			itsLogger.error(SerializeUtil.class.getName()+"\n"+e.toString());
			throw e;
		}
	}
	public Document createDocument(OperatorWorkFlow ow,String userName,boolean addSuffixToOutput) throws Exception {
		Document xmlDoc = null;
		Element root = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
            itsLogger.error(SerializeUtil.class.getName()+"\n"+e.toString());
		}
		root = xmlDoc.createElement("Process");
		root.setAttribute("UserName", ow.getUserName());
		root.setAttribute("Description", ow.getDescription());
		root.setAttribute("Version", FlowMigrator.CURRENT_WRITE_VERION);
		
		xmlDoc.appendChild(root);
		
		List<UIOperatorModel> children = ow.getChildList();
		Iterator<UIOperatorModel> it = children.iterator();
		while (it.hasNext()) {
			UIOperatorModel model = it.next();
			Element operator_element = xmlDoc.createElement("Operator");
			String name = model.getId();
			String uuid=model.getUUID();
				
			operator_element.setAttribute(XML_TAG_UUID,uuid);
			operator_element.setAttribute("name", name);
			//
			String modelName=model.getOperator().getClass().getName();
			if(modelName.contains(".workflow.operator.")){
				modelName=modelName.replace(".workflow.operator.", ".gef.runoperator.");
			}
			operator_element.setAttribute("type", modelName);
			OperatorPosition op=model.getPosition();

			int x = op.getStartX();
			if(x<0)x=0;
			operator_element.setAttribute("X", "" + x);
			int y = op.getStartY();
			if(y<0)y=0;
			operator_element.setAttribute("Y", "" + y);
			
			/**
			 * save parameter
			 */
			model.getOperator().toXML(xmlDoc, operator_element,addSuffixToOutput);
			
			
			/**
			 * write field list parameters
			 */
			model.getOperator().saveInputFieldList(xmlDoc, operator_element,addSuffixToOutput);
			
			/**
			 * save retrain model
			 */
			if(model.getOperator() instanceof ModelOperator
					&&((ModelOperator)model.getOperator()).getModel()!=null
					&&(
							ParameterUtility.getParameterValue(model.getOperator(), OperatorParameter.NAME_Model_File_Path)==null
							||StringUtil.isEmpty(ParameterUtility.getParameterValue(model.getOperator(), OperatorParameter.NAME_Model_File_Path).toString())==true)
					){
				ModelOperator modelOperator = (ModelOperator)model.getOperator() ;
				EngineModel engienModel = modelOperator.getModel();
				if(engienModel!=null){
					Element modelElement = xmlDoc.createElement("Model");
					modelElement.setTextContent(AlpineUtil.objectToString(engienModel));
					operator_element.appendChild(modelElement);
				}
			}else if( Resources.retrainHash.containsKey(uuid)&&
					Resources.retrainHash.get(uuid)!=null){
				if(model.getOperator() instanceof ModelOperator ==false
						||((ModelOperator)model.getOperator() ).isSaveModelFromCache() == true){
					Element modelElement = xmlDoc.createElement("Model");
					EngineModel engienModel=Resources.retrainHash.get(uuid); 
					engienModel.setName(name);
					modelElement.setTextContent(AlpineUtil.objectToString(engienModel));
					operator_element.appendChild(modelElement);
				}
			} 
			
			root.appendChild(operator_element);
		}
		/**
		 * save link node
		 */
		List<UIOperatorConnectionModel>  connModelList=ow.getConnModelList();
		for(UIOperatorConnectionModel connModel:connModelList){
			Element line = xmlDoc.createElement("Link");
			if(null!=connModel&&null!=connModel.getSource()){
				line.setAttribute("source", connModel.getSource().getId());
			}else{
				line.setAttribute("source", null);	
			}
			if(null!=connModel&&null!=connModel.getTarget()){
				line.setAttribute("target", connModel.getTarget().getId());
			}else{
				line.setAttribute("target",null);
			}
			
			root.appendChild(line);
		}
		
		/**
		 * save parent variable model
		 */
		
		if(ow.getVariableModelList()!=null){
			VariableModel variableModel = ow.getVariableModelList().get(0);
			if(variableModel!=null){	
				root.appendChild(variableModel.toXMLElement(xmlDoc));
			}
		}
		return xmlDoc;
	}

}
