/**
 * ClassName WebOperatorModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.db.attribute.model.customized.COUtility;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.Resources;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.operator.OperatorFactory;
import com.alpine.miner.workflow.operator.customize.UDFManager;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.OperatorParameterImpl;

/**
 * 
 * @author jeff
 *
 */
public class UIOperatorModelImpl extends AbstractUIOperatorModel{

	public UIOperatorModelImpl(){
		
	}
	
	public UIOperatorModelImpl(String className,String id,String udfOperatorName){
		this(className, id, udfOperatorName, System.getProperty("user.name"));
	}
	
	/**
	 * support both Miner and Illuminator
	 * @param className
	 * @param id
	 * @param udfOperatorName
	 * @param userName
	 */
	public UIOperatorModelImpl(String className,String id,String udfOperatorName, String userName){
		setId(id);
		setClassName(className);
		try {
			if(UDFManager.INSTANCE.getRootDir() == null){
				UDFManager.INSTANCE.setRootDir(CustomziedConfig.MODEL_PATH);
			}
			if(UDFManager.INSTANCE.getOperatorRegistryRootDir() == null){
				UDFManager.INSTANCE.setOperatorRegistryRootDir(COUtility.CURRENTDIRECTORY);
			}
			setOperator(OperatorFactory.createOperator(Resources.getOperator(className),udfOperatorName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		getOperator().setOperModel(this);
		getOperator().setResourceType(ResourceType.Personal);
		getOperator().setUserName(userName);

		List<String> paraNames = getOperator().getParameterNames();
		if (paraNames == null)
			return;
		List<OperatorParameter> operatorParameterList = new ArrayList<OperatorParameter>();
		for (String s : paraNames) {
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					getOperator(), s);
			operatorParameterList.add(operatorParameter);
		}
		getOperator().setOperatorParameterList(operatorParameterList);
	}
	
	public UIOperatorModelImpl(String className,String id) {
		this(className,id,Locale.getDefault());
	}
	
	public UIOperatorModelImpl(String className,String id,Locale locale){
		this(className, id, System.getProperty("user.name"), locale);
	}
	
	/**
	 * support both Miner and Illuminator
	 * @param className
	 * @param id
	 * @param userName
	 * @param locale
	 */
	public UIOperatorModelImpl(String className,String id, String userName, Locale locale) {
		setId(id);
		setClassName(className);
		setOperator(OperatorFactory.createOperator(Resources.getOperator(className),locale));	
		getOperator().setOperModel(this);
		getOperator().setResourceType(ResourceType.Personal);
		getOperator().setUserName(userName);

		List<String> paraNames = getOperator().getParameterNames();
		if (paraNames == null)
			return;
		List<OperatorParameter> operatorParameterList = new ArrayList<OperatorParameter>();
		for (String s : paraNames) {
			OperatorParameter operatorParameter = new OperatorParameterImpl(
					getOperator(), s);
			operatorParameterList.add(operatorParameter);
		}
		getOperator().setOperatorParameterList(operatorParameterList);
	}
	

	public void addSourceConnection(UIConnectionModel conn){
		sourceConnection.add(conn);
		firePropertyChange(P_SOURCE_CONNECTION,null,null);
	}
	public void addTargetConnection(UIConnectionModel conn){
		targetConnection.add(conn);
		firePropertyChange(P_TARGET_CONNECTION,null,null);
	}
	public void removeSourceConnection(UIConnectionModel conn){
		sourceConnection.remove(conn);
		firePropertyChange(P_SOURCE_CONNECTION,null,null);
	}
	public void removeTargetConnection(UIConnectionModel conn){
		targetConnection.remove(conn);
		firePropertyChange(P_TARGET_CONNECTION,null,null);
		
	}
	//special for UDF
	@Override
	public void initiateOperator(String udfOperatorName) throws Exception {
		String className=Resources.getOperator(this.getClassName());
		  setOperator(OperatorFactory.createOperator(className,udfOperatorName));
		
	}
 
 
	 
}
