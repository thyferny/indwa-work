/**
 * ClassName ContentsSqlModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alpine.miner.workflow.model.impl.AbstractModel;
import com.alpine.miner.workflow.operator.VariableModel;
/**
 * 
 * @author jimmy
 *
 */
public class ContentsOperatorModel extends AbstractModel{
	private static final Logger itsLogger=Logger.getLogger(ContentsOperatorModel.class);
	private List<UIOperatorModel> children;
	private List<String[]> listConnection;
	private List<VariableModel> parentVariableModelList;
	private Map<String,VariableModel> subFlowVariableModelMap;
	public ContentsOperatorModel(){
		children = new ArrayList<UIOperatorModel>();
		listConnection = new ArrayList<String[]>();
	}
	
	/**
	 * define mark for this
	 */
	public static final String P_CHILDREN= "_children";
	public List<UIOperatorModel> getChildren() {
		return children;
	}

	public void addChildren(Object children) {
		this.children.add((UIOperatorModel)children);
		firePropertyChange(P_CHILDREN,null,null);
	}
	
	public void removeChild(Object child){
		children.remove(child);
		firePropertyChange(P_CHILDREN,null,null);
	}
	
	/**
	 * link list operation
	 */
	
	private static String[] st;

	public void clearSt(){
		st = new String[2];
	}
	public void setSource(String source){
		st[0] = source;
	}
	public void setTarget(String target){
		st[1] = target;
	}
	public List<String[]> getListConnection() {
		return listConnection;
	}

	public void addListConnection() {
		listConnection.add(st);
	}
	public void clearConnection(){
		listConnection.clear();
	}

	public void delListConnection(Object conn) {
		UIConnectionModel con = (UIConnectionModel) conn;
		for(int i=0;i<listConnection.size();i++){
			if(null==listConnection.get(i)||
					listConnection.get(i).length<2||null==listConnection.get(i)[0]||null==listConnection.get(i)[1]){
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("The connections are["+
							((null==listConnection.get(i)?null:Arrays.toString(listConnection.get(i))))+"]");
				}
				continue;
			}
			if(null==con.getTarget()||null==con.getSource()){
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("Either target["+con.getTarget()+"] or source["+con.getSource()+"] is null");
				}
				continue;
			}
			
			if(listConnection.get(i)[0].equals(con.getSource().getId())&&listConnection.get(i)[1].equals(con.getTarget().getId())){
				listConnection.remove(i);
				break;
			}
		}
	}
	public VariableModel getParentVariableModel(){
		if(parentVariableModelList!=null){
			return parentVariableModelList.get(0);
		}
		return null;
	}
	public List<VariableModel> getVariableModelList() {
		return parentVariableModelList;
	}
	public void setVariableModelList(List<VariableModel> variableModelList) {
		this.parentVariableModelList = variableModelList;
	}
	
	public void addVariableModel(VariableModel variableModel) {
		if(parentVariableModelList==null){
			parentVariableModelList=new ArrayList<VariableModel>();
		}
		parentVariableModelList.add(variableModel);
	}
	
	public void removeVariableModel(VariableModel variableModel) {
		if(parentVariableModelList!=null){
			if(parentVariableModelList.contains(variableModel)){
				parentVariableModelList.remove(variableModel);
			}
		}
	}
	
	public void clearVariableModels(){
		if(parentVariableModelList!=null){
			parentVariableModelList.clear();
		}
	}

	public void addSubFlowVariableModel(String operatorId,VariableModel variableModel){
		if(subFlowVariableModelMap==null){
			subFlowVariableModelMap=new LinkedHashMap<String, VariableModel>();
		}
		subFlowVariableModelMap.put(operatorId, variableModel);
	}
	
	public VariableModel getSubFlowVariableModel(String operatorId){
		if(subFlowVariableModelMap!=null){
			return subFlowVariableModelMap.get(operatorId);
		}
		return null;
	}
	
	public Iterator<Entry<String, VariableModel>> getSubFlowVariableModelIterator(){
		if(subFlowVariableModelMap!=null){
			return subFlowVariableModelMap.entrySet().iterator();
		}
		return null;
	}
}
