
package com.alpine.miner.workflow.operator;

import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.xml.XmlDocManager;


public interface Operator {

	
	public String validateInputLink(Operator precedingOperator,boolean multiple);
 
	

	public boolean isInputObjectsReady();
 

	public  List<String> getOutputClassList();
	public List<String> getInputClassList();
	
	public UIOperatorModel getOperModel();
	public void setOperModel(UIOperatorModel operModel);

 
	
	public String getToolTipTypeName();
	
	//this two method is completeed in the abstract operator
	public List<Operator> getParentOperators();
	public List<Operator> getChildOperators();
	
	//Please be careful , we weill use this from now
	public List<OperatorParameter> getOperatorParameterList();
	public OperatorParameter getOperatorParameter(String paraName);
	public void setOperatorParameterList(List<OperatorParameter> operatorParameter);
	public List<String> getParameterNames();	
	public String getOperatorParameterDefaultValue(String paraName);
	
 	public boolean isVaild(VariableModel variableModel);
 	public String[] getInvalidParameters();

	public List<Object> getOperatorOutputList();
	public List<Object> getOperatorInputList();
	
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,Node opNode);
	public void toXML(Document xmlDoc,Element element,boolean addSuffixToOutput);

	public String getUserName();

	public void setUserName(String userName);

	public ResourceType getResourceType();

	public void setResourceType(ResourceType resourceType);

	public void saveInputFieldList(Document xmlDoc, Element operator_element, boolean addSuffixToOutput);
	
	public boolean isRunningFlowDirty();
	
	public String getParameterLabel(String parameterName);
	
	public List<Object> getOutputObjectList();



	public void addInputClass(String className);



	public void addOutputClass(String className);

	public String getParameterLabel(String parameterName, Locale locale);
	
	public Locale getLocale();
	
	public void setLocale(Locale locale);
	
	public List<OperatorInputTableInfo> getParentDBTableSet();

	public OperatorWorkFlow getWorkflow();

	public void setWorkflow(OperatorWorkFlow workflow);

	public boolean equals(Object operator);
	
	public static final String AFM_SUFFIX = ".afm";

	String validateInputLink(Operator precedingOperator);
}
