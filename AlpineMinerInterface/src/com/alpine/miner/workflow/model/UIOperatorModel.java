/**
 * ClassName UIOperatorModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model;

import java.util.List;
import java.util.Locale;

import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.operator.Operator;

public interface UIOperatorModel extends UIModel{
	
	//id is name now
	public String getId();
	public void setId(String operName);
	
	public List<UIConnectionModel> getSourceConnection();
	public void addSourceConnection(UIConnectionModel connectionModel);
	public void removeSourceConnection(UIConnectionModel connectionModel);
	

	public List<UIConnectionModel> getTargetConnection();
	public void addTargetConnection(UIConnectionModel connectionModel); 
	public void removeTargetConnection(UIConnectionModel connectionModel);
 	

	public void setUUID(String uuid);

	public void setPosition(OperatorPosition operatorPosition);

	public Operator getOperator(); 
	public void setOperator(Operator operator);

	public void initiateOperator(Locale locale); 

 
	public String getUUID();

	public OperatorPosition getPosition();
	
	//class is type...
	public void setClassName(String className);
 	public String getClassName();

 	public String getNewName();
 	public void setNewName(String newName);
	public void initiateOperator(String operatorName) throws Exception;
	public boolean containTarget(UIOperatorModel model);

}
 