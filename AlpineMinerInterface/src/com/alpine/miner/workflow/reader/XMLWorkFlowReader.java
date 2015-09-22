/**
 * ClassName XMLWorkFlowReader.java
 *
 * Version information: 1.00
 *
 * Data: 2011/04/02
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.db.attribute.model.customized.COUtility;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.Resources;
import com.alpine.miner.util.SerializeUtil;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.model.impl.UIOperatorModelImpl;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.structual.RecursiveException;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

public class XMLWorkFlowReader implements IWorkFlowReader {
	
	public static final String XML_TAG_UUID="uuid";

	public static Logger itsLogger=Logger.getLogger(XMLWorkFlowReader.class);
	
	private HashMap<String,List<HashMap<String,HashMap<String,String>>>> inputFieldMap=new HashMap<String,List<HashMap<String,HashMap<String,String>>>>();



	private HashSet<String> readedSubFlowNames  =new HashSet<String> (); 

	public HashSet<String> getReadedSubFlowNames() {
		return readedSubFlowNames;
	}


	public void setReadedSubFlowNames(HashSet<String> readedSubFlowNames) {
		this.readedSubFlowNames = readedSubFlowNames;
	}


	@Override
	public OperatorWorkFlow doRead(AbstractReaderParameters para,Locale locale) 	throws   Exception {
		
		String filePath=((XMLFileReaderParameters)para).getFilePath();
		String workFlowName= null;
		if(filePath.contains(Operator.AFM_SUFFIX )){
			workFlowName=filePath.substring(filePath.lastIndexOf(File.separator) +1, filePath.lastIndexOf(Operator.AFM_SUFFIX));
		} 
		if(readedSubFlowNames.contains(workFlowName)==true){
			throw new RecursiveException("Recursive Flow Found!");
		}
		String loginUserName=((XMLFileReaderParameters)para).getUserName();
		ResourceType resourceType=((XMLFileReaderParameters)para).getResourceType();
		OperatorWorkFlow ow = new OperatorWorkFlow();
		Hashtable<String,UIOperatorModel> htModel = new Hashtable<String, UIOperatorModel>();
		XmlDocManager opTypeXmlManager = new XmlDocManager();
		

		opTypeXmlManager.parseXMLFile(filePath);

		
		

		/**
		 * read operator model ,show in diagram
		 */
		String userName = opTypeXmlManager.getRootNode().getAttributes().getNamedItem("UserName").getNodeValue();
		String description = opTypeXmlManager.getRootNode().getAttributes().getNamedItem("Description").getNodeValue();
		String version = opTypeXmlManager.getRootNode().getAttributes().getNamedItem("Version").getNodeValue();
		ow.setUserName(userName);
		ow.setDescription(description);
		ow.setVersion(version);
		ow.setName(workFlowName);
		
		HashMap<String,Operator> operatorMap=new HashMap<String,Operator>();
		
		ArrayList<Node> opNodes = opTypeXmlManager.getNodeListByTag("Operator"); // Get a list of Operator Type; 
		
		UIOperatorModel[] child = new UIOperatorModel[opNodes.size()];
		int i=0;
		for (Node opNode:opNodes) {
			child[i] = new UIOperatorModelImpl();
			String operName = opNode.getAttributes().getNamedItem("name").getNodeValue();
			child[i].setId(operName);

			if(opNode.getAttributes().getNamedItem(XML_TAG_UUID)!=null){
				String uuid = opNode.getAttributes().getNamedItem(XML_TAG_UUID).getNodeValue();
				child[i].setUUID(uuid);
			}
		
			String operClass = opNode.getAttributes().getNamedItem("type").getNodeValue();
			String className = operClass.substring(operClass.lastIndexOf(".")+1,operClass.length());

			child[i].setClassName(className);
			
			if(className.equals("CustomizedOperator")){
				String operatorName=opNode.getAttributes().getNamedItem(COUtility.OPERATOR_NAME).getNodeValue();
 
				try {
					child[i].initiateOperator(operatorName);
				} catch (Exception e) {
					itsLogger.error(e.getMessage(),e);
					e.printStackTrace();
					throw new RuntimeException("1076");
				}
 
			}else{
				child[i].initiateOperator(locale);
			}
			
			
			String operX = opNode.getAttributes().getNamedItem("X").getNodeValue();
			int x = Integer.parseInt(operX);
			String operY = opNode.getAttributes().getNamedItem("Y").getNodeValue();
			int y = Integer.parseInt(operY); 
			child[i].setPosition(new OperatorPosition(x, y,-1,-1));//.setRect(new Rectangle(x,y,-1,-1));

			/**
			 * read parameter into operatorParameter
			 */

			Operator operator=child[i].getOperator();
			
			if(!StringUtil.isEmpty(loginUserName)){
				operator.setUserName(loginUserName);
			}
			if(resourceType!=null){
				operator.setResourceType(resourceType);
			}
			
			operatorMap.put(operName, operator);

			operator.setOperModel(child[i]);
			
			operator.setWorkflow(ow);
			
			List<OperatorParameter> operatorParameters =null;
			if(version.equals(FlowMigrator.CURRENT_READ_VERION)==false){//Migration
				FlowMigrator migerator = FlowMigrationFactory.getMigrator(version,FlowMigrator.CURRENT_READ_VERION);
				if(migerator==null){
					throw new ParserConfigurationException("Flow version not supported : version = '"+version+"'"); 
				}
				operatorParameters = migerator.doReadOperatorMigrator(operator,opTypeXmlManager,(Element)opNode);
			}else{
				if(operator instanceof SubFlowOperator){
					String pathPrefix=filePath.substring(0,filePath.lastIndexOf(File.separator));
					((SubFlowOperator)operator).setPathPrefix(pathPrefix);
					if(readedSubFlowNames==null){  
						readedSubFlowNames = new HashSet<String>();
					}
					readedSubFlowNames.add(workFlowName) ;
					operatorParameters =((SubFlowOperator)operator).fromXML(opTypeXmlManager,opNode,readedSubFlowNames);
					readedSubFlowNames.remove(workFlowName) ;
				}else{
					operatorParameters =operator.fromXML(opTypeXmlManager,opNode);	
				}
				
			}
			child[i].getOperator().setOperatorParameterList(operatorParameters);
				
			htModel.put(child[i].getId(), child[i]);
//			/**
//			 * read model in cache
//			 */
			ArrayList<Node> modelNodeList = opTypeXmlManager.getNodeList(opNode, "Model");
			for(Node node:modelNodeList){
				String modelString = node.getTextContent();
				EngineModel model = (EngineModel) SerializeUtil.stringToObject(modelString);
				if(child[i].getOperator() instanceof ModelOperator){
					((ModelOperator)child[i].getOperator()).setModel(model);
				}
				if(opNode.getAttributes().getNamedItem("uuid")!=null){
					String uuid = opNode.getAttributes().getNamedItem("uuid").getNodeValue();
					Resources.retrainHash.put(uuid, model);
				}else{
					String uuid = System.currentTimeMillis()+"";
					child[i].getOperator().getOperModel().setUUID(uuid);
					Resources.retrainHash.put(uuid, model);
				}
			}		
			i++;
		}
		

		/**
		 * read link in diagram
		 */
		ArrayList<Node> linkNodes = opTypeXmlManager.getNodeListByTag("Link"); // Get a list of Operator Type;
		UIOperatorConnectionModel[] connection = new UIOperatorConnectionModel[linkNodes.size()];
		int j=0;
		for (Node linkNode:linkNodes) {
			connection[j] = new UIOperatorConnectionModel();
			String source = linkNode.getAttributes().getNamedItem("source").getNodeValue();
			String target = linkNode.getAttributes().getNamedItem("target").getNodeValue();
			if(null!=operatorMap.get(source)&&null!=operatorMap.get(source).getOperModel()){
				operatorMap.get(source).getOperModel().addTargetConnection(connection[j]);
			}else{
				itsLogger.error("Operator map is missing the input for the source["+source+"]");
			}
			
			if(null!=operatorMap.get(target)&&null!=operatorMap.get(target).getOperModel()){
				operatorMap.get(target).getOperModel().addSourceConnection(connection[j]);
			}else{
				itsLogger.error("Operator map is missing the input for the target["+target+"]");
			}
			
			
			
			
			for(int k=0;k<child.length;k++){
				if(child[k].getId().equals(source)){
					connection[j].setSource(child[k]);
				}else if(child[k].getId().equals(target)){
					connection[j].setTarget(child[k]);
				}
			}
			ow.add(connection[j]);
			j++;
		}
		for(UIOperatorModel om:child){
			ow.addChild(om);
		}
		
		/**
		 * read variable
		 */
		
		ArrayList<Node> variableModelNodes = opTypeXmlManager.getNodeList(opTypeXmlManager.getRootNode(),VariableModel.MODEL_TAG_NAME); 
		for (Node variableModelNode:variableModelNodes){
			Element variableElement=(Element)variableModelNode;
			VariableModel variableModel = VariableModel.fromXMLElement(variableElement);
			ow.addVariableModel(variableModel);
		}
		
		if(ow.getVariableModelList()==null){
			ow.initVariableModelList();
		}
		
		ow.setVersion(FlowMigrator.CURRENT_READ_VERION);
		return ow;
	}


	public HashMap<String,List<HashMap<String,HashMap<String,String>>>> getInputFieldList(AbstractReaderParameters para){
		return inputFieldMap;
	}

	 
}
