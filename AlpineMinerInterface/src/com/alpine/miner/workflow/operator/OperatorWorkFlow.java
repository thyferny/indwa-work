package com.alpine.miner.workflow.operator;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;

public class OperatorWorkFlow { 
	private String name; //the real file name without .afm
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	private String userName;
	private String description;
	private String version;
	private List<UIOperatorModel> operatorModelList = new ArrayList<UIOperatorModel>();
	private List<UIOperatorConnectionModel> connModelList = new ArrayList<UIOperatorConnectionModel>();
	private List<VariableModel> variableModelList;


	public List<UIOperatorModel> getChildList() {
		return operatorModelList;
	}
	public void setChildList(List<UIOperatorModel> list) {
		this.operatorModelList = list;
	}
	public void addChild(UIOperatorModel om){
		this.operatorModelList.add(om);
	}
	public void clear(){
		operatorModelList.clear();
	}
	 
	
	public List<UIOperatorConnectionModel> getConnModelList() {
		return connModelList;
	}
	public void setConnModelList(List<UIOperatorConnectionModel> connModelList) {
		this.connModelList = connModelList;
	}
	public void add(UIOperatorConnectionModel connModel){
		connModelList.add(connModel);
	}
	
	

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}


	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public void initVariableModelList(){
		if(variableModelList==null){
			variableModelList=new ArrayList<VariableModel>();
			VariableModel first=new VariableModel();
			variableModelList.add(first);
		}
	}
	public List<VariableModel> getVariableModelList() {
		return variableModelList;
	}
	
	public VariableModel getParentVariableModel(){
		if(variableModelList!=null){
			return variableModelList.get(0);
		}
		return null;
	}
	
	public void setParentVariableModel(VariableModel variableModel){
		if(variableModelList!=null){
			variableModelList.clear();
		}else{
			variableModelList=new ArrayList<VariableModel>();
		}
		variableModelList.add(variableModel);
	}
	
	
	public void setVariableModelList(List<VariableModel> variableModelList) {
		this.variableModelList = variableModelList;
	}
	
	public void addVariableModel(VariableModel variableModel) {
		if(variableModelList==null){
			variableModelList=new ArrayList<VariableModel>();
		}	
		variableModelList.add(variableModel);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OperatorWorkFlow [name=");
		builder.append(name);
		builder.append(", userName=");
		builder.append(userName);
		builder.append(", description=");
		builder.append(description);
		builder.append(", version=");
		builder.append(version);
		builder.append(", operatorModelList=");
		builder.append(operatorModelList);
		builder.append(", connModelList=");
		builder.append(connModelList);
		builder.append(", variableModelList=");
		builder.append(variableModelList);
		builder.append("]");
		return builder.toString();
	}
	
}
