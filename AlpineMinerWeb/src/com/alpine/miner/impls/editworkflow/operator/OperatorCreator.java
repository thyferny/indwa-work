/**
 * 
 */
package com.alpine.miner.impls.editworkflow.operator;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorModelImpl;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.xml.XmlDocManager;

/**
 * ClassName: OperatorCreator.java
 * <p/>
 * for read operator's properties when edit operator property 
 * <p/>
 * Data: 2012-7-7
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class OperatorCreator {

	private static OperatorCreator INSTANCE = new OperatorCreator();
	
	public static OperatorCreator getInstance(){
		return INSTANCE;
	}
	
	private OperatorCreator(){}
	
	public UIOperatorModel newOperatorModel(String simpleClassName, String name, OperatorWorkFlow workFlow, String userName, Locale locale){
		
		return newOperatorModel(simpleClassName,name,workFlow,userName,locale,null);
	}
	public UIOperatorModel newOperatorModel(String simpleClassName, String name, OperatorWorkFlow workFlow, String userName, Locale locale,FlowInfo flowInfo){
		
		UIOperatorModel opModel = createOperatorModel(simpleClassName, name, userName, locale,flowInfo);
		opModel.getOperator().setWorkflow(workFlow);
        //moved to this location so that when we switch something to the hadoop version, it will get appropriate default values
        //since this creates a new operator, we need to populate the default values; //PIV-32953019
        Operator operator = opModel.getOperator();
        List<OperatorParameter> parameters = operator.getOperatorParameterList();
        if(parameters == null){
            return opModel;//for model save operator it have no property.
        }
        Iterator<OperatorParameter> paramIter = parameters.iterator();
        while (paramIter.hasNext()) {
            OperatorParameter parameter = paramIter.next();
            String paraName = parameter.getName();
            String defaultValue = operator.getOperatorParameterDefaultValue(paraName);
            if (defaultValue != null && !defaultValue.isEmpty()){
                operator.getOperatorParameter(paraName).setValue(defaultValue);
            }  else if (XmlDocManager.OUTPUTTABLElIST.contains(paraName)){      //this is an output table, so need to generate unique output table name
                operator.getOperatorParameter(paraName).setValue(OperatorUtility.getOperatorOutputTableName(operator.getClass().getCanonicalName(), paraName, workFlow.getChildList()));
            }
        }
		return opModel;
	}
	
	private UIOperatorModel createOperatorModel(String simpleClassName, String name, String userName, Locale locale,FlowInfo flowInfo){
		if(simpleClassName != null && simpleClassName.startsWith("CustomizedOperator")){
            //need to pull correct info from parameters.  For customized operators, simpleClassName is: CustomizedOperator_udfName
            String udfName = simpleClassName.substring(19);

			return new UIOperatorModelImpl("CustomizedOperator", name, udfName, userName);
		}else if(simpleClassName != null && simpleClassName.startsWith("SubFlowOperator")
				&&flowInfo!=null){
			UIOperatorModelImpl opModel= new UIOperatorModelImpl(simpleClassName, name, userName, locale);
			SubFlowOperator subFlowOperator=(SubFlowOperator)opModel.getOperator();
			String filePath=flowInfo.getKey();
			String pathPrefix=filePath.substring(0,filePath.lastIndexOf(File.separator));
			subFlowOperator.setPathPrefix(pathPrefix);
			return opModel;
		}else{
			return new UIOperatorModelImpl(simpleClassName, name, userName, locale);
		}
	}
}
