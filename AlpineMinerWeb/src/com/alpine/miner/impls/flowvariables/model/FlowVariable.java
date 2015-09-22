/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowVariable
 * Apr 9, 2012
 */
package com.alpine.miner.impls.flowvariables.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.alpine.miner.workflow.operator.VariableModel;

/**
 * @author Gary
 *
 */
public class FlowVariable {

	private List<Variable> variables = new ArrayList<Variable>();
	
	private String flowName;
	
	/**
	 * 
	 */
	public FlowVariable() {}
	
	public FlowVariable(VariableModel model, String flowName){
		this.flowName = flowName;
		if(model == null)
			return;
		for(Iterator<Entry<String, String>> iter = model.getIterator(); iter.hasNext();){
			Entry<String, String> variable = iter.next();
			variables.add(new Variable(variable.getKey(), variable.getValue()));
		}
	}
	
	public VariableModel getModel(){
		VariableModel model = new VariableModel();
		for(Variable variable: variables){
			model.addVariable(variable.getName(), variable.getValue());
		}
		return model;
	}

	public List<Variable> getVariables() {
		return variables;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}
	
	
	public static class Variable{
		private String 	name,
						value;

		public Variable(){}
		
		public Variable(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
}
