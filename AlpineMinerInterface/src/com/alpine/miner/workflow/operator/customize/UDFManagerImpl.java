/**
 * ClassName :UDFManagerImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-10-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.customize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.db.attribute.model.customized.COUtility;
import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;
import com.alpine.datamining.api.impl.db.attribute.model.customized.ParameterModel;
import com.alpine.miner.model.uitype.AbstractUIControlType;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterFactory;
import com.alpine.miner.workflow.operator.parameter.helper.SingleSelectParameterHelper;
import org.apache.log4j.Logger;

/**
 * make sure it can be reused by eclipse
 * 
 * @author zhaoyong
 * 
 */
public class UDFManagerImpl implements UDFManager {
    private static final Logger itsLogger=Logger.getLogger(UDFManagerImpl.class);

    // udf model info --default for web
	protected String rootDir = null;
	protected String operatorRegistryRootDir = null;
	boolean init=false;

	List<CustomizedOperatorModel> allModels=null;

	public String getOperatorRegistryRootDir() {
		return operatorRegistryRootDir;
	}

	public void setOperatorRegistryRootDir(String operatorRegistryRootDir) {
		this.operatorRegistryRootDir = operatorRegistryRootDir;
		File file = new File(operatorRegistryRootDir);
		if(file.exists()==false){
			file.mkdir();
		}
	}

	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
		//fileter the last file.separater
		CustomziedConfig.setPath(rootDir.substring(0,rootDir.length()-1));
		File file = new File(rootDir);
		if(file.exists()==false){
			file.mkdir();
		}
	}

	public UDFManagerImpl() {

	}

	@Override
	public CustomizedOperatorModel getCustomizedOperatorModelByUDFName(String udfName)
			throws Exception {
		List<CustomizedOperatorModel> models = getAllCustomizedOperatorModels();
		if (models != null) {

			for (int i = 0; i < models.size(); i++) {

				CustomizedOperatorModel model = models.get(i);
				if (model.getUdfName().equals(udfName)) {
					return model;
				}
			}
		}

		return null;
	}

	@Override
	public boolean deleteCustomizedOperatorModels(String operatorName) throws Exception {
		//here is full name 
 
		
		String configPath =operatorRegistryRootDir+COUtility.OPERATOR_FILE;
		File cmFile=new File(rootDir+operatorName+CustomziedConfig.MODEL_SUFFIX);
		if(cmFile.exists()&&cmFile.isFile()){
			if(!cmFile.delete()){
				return true;
			}		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document xmlDoc = db.parse(new File(configPath));
			// we have change to save each operator in to separater file
			Element root = xmlDoc.getDocumentElement();

			NodeList definitions = root.getElementsByTagName("operator");
			for (int i = 0; i < definitions.getLength(); i++) {
				String nodeName=((Element)definitions.item(i)).getAttribute(COUtility.OPERATOR_NAME);
				if(nodeName.equals(operatorName)){
					root.removeChild(definitions.item(i));
					break;
				}
			}
			saveConfigXML(configPath, xmlDoc);
		}
		CustomizedOperatorModel model = getCustomizedOperatorModelByOperatorName(operatorName);
		//2 delete the cm file
		String path = getRootDir()+model.getOperatorName()+CustomziedConfig.MODEL_SUFFIX;
		File file= new File(path);
		file.delete();
		//3 unreguistry the parameter
		unRegistryModel(model) ;
		return true;
	}

	private void saveConfigXML(String configPath, Document xmlDoc)
			throws TransformerFactoryConfigurationError, Exception {
		File file = new File(configPath);
		BufferedWriter writer = null;
		try {
			
			writer = new BufferedWriter(new FileWriter(file));
			String out = COUtility.xmlToString(xmlDoc);
			
			writer.write(out);
	 		} catch (Exception e) {
			itsLogger.error(UDFManagerImpl.class.getName()+"\n"+e.toString());
			throw e;
		}finally{
			if(writer!=null){
			 
				writer.close();
			}
			
		}
	}

	@Override
	public List<CustomizedOperatorModel> getAllCustomizedOperatorModels()
			throws Exception {
		if(allModels==null){
			initModelsFromPersistence();
		}
		return allModels;
	}

	private void initModelsFromPersistence() throws Exception { 
		File dir = new File(rootDir);
		if(rootDir==null){
			throw new Exception("Please set the root directory for CustomizedOperatorModel!");
		}
		allModels = new ArrayList<CustomizedOperatorModel>();
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				//.cm file
			 if(files[i].getName().endsWith(CustomziedConfig.MODEL_SUFFIX)==false){
				 continue;
			 }
				ObjectInputStream ois = null;
				try {
					FileInputStream fis = new FileInputStream(files[i]);
					ois = new ObjectInputStream(fis);
					CustomizedOperatorModel coModel = (CustomizedOperatorModel) ois
							.readObject();
					registryNewModel(coModel);
				} catch (Exception e) {
					itsLogger.error(
							UDFManagerImpl.class.getName() + "\n"
									+ e.toString());
					throw e;
				} finally {
					if (ois != null) {
						try {
							ois.close();
						} catch (IOException e) {
							itsLogger.error(
									UDFManagerImpl.class.getName() + "\n"
											+ e.toString());
						}
					}
				}
			}
		}else{
			dir.mkdir();
		}
	 
		
	}

	@Override
	public boolean importUDFFile(String udfFilePath) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xmlDoc = db.parse(new File(udfFilePath));
		String configPath =operatorRegistryRootDir+COUtility.OPERATOR_FILE;
		// we have change to save each operator in to separater file
		Element root = xmlDoc.getDocumentElement();

		NodeList definitions = root.getElementsByTagName("operator");

		Document customizedXMLDoc = generateCustomizedXML();
		Element customizedXML = (Element) customizedXMLDoc.getFirstChild();

	 
		for (int i = 0; i < definitions.getLength(); i++) {
			if (definitions.item(i) instanceof Element) {
				CustomizedOperatorModel model = CustomizedOperatorModel
						.fromXMLElement((Element) definitions.item(i),
								udfFilePath);
			 
				try{
					registryNewModel(model) ;
					serlizeModel(model);
					Element operatorNode = customizedXMLDoc
							.createElement(COUtility.OPERATOR);
					operatorNode.setAttribute(COUtility.OPERATOR_NAME, model
							.getOperatorName());
					customizedXML.appendChild(operatorNode);
				}catch(Exception e){
					// handle operator.xml		
					saveConfigXML(configPath, customizedXMLDoc);
					throw e;
				}
			}
		}
		// handle operator.xml		
		saveConfigXML(configPath, customizedXMLDoc);

		return true;
	}


	
	private void serlizeModel(CustomizedOperatorModel model) throws Exception {
		String binartFileName=rootDir+model.getOperatorName()+CustomziedConfig.MODEL_SUFFIX;
	
		
		   ObjectOutputStream os = null; 
		try {
			FileOutputStream fs = new FileOutputStream(new File(binartFileName));
              os =  new ObjectOutputStream(fs);   
            os.writeObject(model);   
            os.close(); 
		} catch ( Exception e) {
			itsLogger.error(UDFManagerImpl.class.getName()+"\n"+e.toString());
			throw e;
		}   finally{
			if(os!=null){
				os.close();
		 
			
			}
		}	
		
	}

	private Document generateCustomizedXML() {
		File customizedXMLFile = new File(operatorRegistryRootDir+  COUtility.OPERATOR_FILE);

		Document xmlDoc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			if (!customizedXMLFile.exists()) {
				xmlDoc = docBuilder.newDocument();
				Element root = xmlDoc.createElement(COUtility.CUMSTOMIZED);
				xmlDoc.appendChild(root);
			} else {
				xmlDoc = docBuilder.parse( customizedXMLFile);
			}
		} catch (ParserConfigurationException e) {
			itsLogger.error(
					UDFManagerImpl.class.getName() + "\n" + e.toString());
		} catch (SAXException e) {
			itsLogger.error(
					UDFManagerImpl.class.getName() + "\n" + e.toString());
		} catch (IOException e) {
			itsLogger.error(
					UDFManagerImpl.class.getName() + "\n" + e.toString());
		}
		return xmlDoc;
	}

	@Override
	public CustomizedOperatorModel getCustomizedOperatorModelByOperatorName(
			String operatorName) throws Exception {
		List<CustomizedOperatorModel> models = getAllCustomizedOperatorModels();
		if (models != null) {

			for (int i = 0; i < models.size(); i++) {

				CustomizedOperatorModel model = models.get(i);
				if (model.getOperatorName().equals(operatorName)) {
					return model;
				}
			}
		}

		return null;
	}
	private void registryNewModel(CustomizedOperatorModel model) throws Exception{
		if(allModels==null){
			initModelsFromPersistence();
		}
	
		HashMap<String, ParameterModel> parameterMap = model.getParaMap();
		Set<String> keys = parameterMap.keySet();
		for(Iterator<String> it= keys.iterator();it.hasNext();){
			String parameterName= it.next();
			ParameterModel paramModel =  parameterMap.get(parameterName) ;
			//AbstractUIControlType.BUTTON_CONTROL_TYPE -- column name
			if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.BUTTON_CONTROL_TYPE)){
 
				OperatorParameterFactory.INSTANCE.registryHelper(parameterName,
						OperatorParameterFactory.columnNamesParameterHelper) ; 
			}else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.UDF_COLUMNNAME_CONTROL_TYPE)){
				OperatorParameterFactory.INSTANCE.registryHelper(parameterName,
						OperatorParameterFactory.columnNamesParameterHelper) ; 
			} //true false type
			else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.CHECK_CONTROL_TYPE)){
				OperatorParameterFactory.INSTANCE.registryHelper(parameterName,
						OperatorParameterFactory.trueFalseHelper) ; 
			}
			else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.TEXT_CONTROL_TYPE)){
				OperatorParameterFactory.INSTANCE.registryHelper(parameterName,
						OperatorParameterFactory.simpleStringHelper) ;
			} //true false type
			else if(paramModel.getParaType().equalsIgnoreCase(AbstractUIControlType.COMBO_CONTROL_TYPE)){
				List<String> optionValues = paramModel.getOptionalValue();
				//this means get the column names
				if(optionValues==null||optionValues.size()==0){
					OperatorParameterFactory.INSTANCE.registryHelper(parameterName,
							OperatorParameterFactory.singleColumnParameterHelper) ; 
				}else{
					OperatorParameterFactory.INSTANCE.registryHelper(parameterName, new SingleSelectParameterHelper(optionValues)) ;
				} 
			}
	
		} 
		//make sure the parameter name is OK
		if(allModels.contains(model)==false){
			allModels.add(model);
		}
		
	}
	private void unRegistryModel(CustomizedOperatorModel model) throws Exception{
		 
	
		HashMap<String, ParameterModel> parameterMap = model.getParaMap();
		Set<String> keys = parameterMap.keySet();
		for(Iterator<String> it= keys.iterator();it.hasNext();){
			String parameterName= it.next();
			 
			OperatorParameterFactory.INSTANCE.unRegistryHelper(parameterName) ; 
	 
	
		} 
		//make sure the parameter name is OK
		if(allModels!=null&&allModels.contains(model)){
			allModels.remove(model);
		}
		
	}
 
}
