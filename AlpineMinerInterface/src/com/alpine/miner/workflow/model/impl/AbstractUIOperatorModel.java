/**
 * ClassName AbstractOperatorModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-3-31
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.inter.resources.Resources;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorFactory;

public abstract class AbstractUIOperatorModel extends AbstractModel implements UIOperatorModel{
	public static final String P_CONSTRAINT = "_constraint";
	public static final String P_TEXT = "_text";
	public static final String P_SOURCE_CONNECTION = "_source_connection";
	public static final String P_TARGET_CONNECTION="_target_connection";
	
	protected List<UIConnectionModel> sourceConnection = new ArrayList<UIConnectionModel>();
	public List<UIConnectionModel> targetConnection = new ArrayList<UIConnectionModel>();
 	
	private String newName = "";

	private String uuid=String.valueOf(System.currentTimeMillis());	//operatorID //default value
 
	private String className;
	private String imgName = "";
	private String id=""; 
	
	private OperatorPosition position;
	private Operator oper;
	
	
	
	public void initiateOperator(Locale locale){
		String operatorName=Resources.getOperator(this.getClassName());
		this.setOperator(OperatorFactory.createOperator(operatorName,locale));
	}
	
	abstract public void addSourceConnection(UIConnectionModel conn);
	abstract public void addTargetConnection(UIConnectionModel conn);
	abstract public void removeSourceConnection(UIConnectionModel conn);
	abstract public void removeTargetConnection(UIConnectionModel conn);
	
	 

	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}


	public Operator getOperator() {
		return oper;
	}


	public void setOperator(Operator oper) {
		this.oper = oper;
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
		
	}
	
 
	public List<UIConnectionModel> getSourceConnection(){
		return sourceConnection;
	}
	public List<UIConnectionModel> getTargetConnection(){
		return targetConnection;
	}
	
	public void setPosition(OperatorPosition pos){
		this.position=pos;
		firePropertyChange(P_CONSTRAINT,null,pos);
	}

	public OperatorPosition getPosition() {
		return position;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
		firePropertyChange(P_TEXT,null,newName);
	}

	public String getImgName() {
		return imgName;
	}


	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	
	public String getUUID()  {
 
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}
	public boolean containTarget(UIOperatorModel model){
		boolean result = false;
		for (Iterator<UIConnectionModel> iterator = targetConnection.iterator(); iterator.hasNext();) {
			UIConnectionModel connModel =  iterator.next();
			if(connModel.getTarget().getId().equals(model.getId())){
				result = true;
				break;
			}
		}
		return result;
	}
}
