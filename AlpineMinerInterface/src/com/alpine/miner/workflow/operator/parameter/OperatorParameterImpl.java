/**
 * ClassName OperatorParameterImpl.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter;

import java.util.Locale;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.hadoop.CopyToDBOperator;
import com.alpine.miner.workflow.operator.hadoop.CopytoHadoopOperator;

/**
 * @author zhaoyong
 *
 */
public class OperatorParameterImpl implements OperatorParameter {
 

	String name ;
	private Object value ;
	private ParameterDataType dataType;
	private Operator operator;;
	//this is for the customized parameters
	public OperatorParameterImpl( Operator operator,   String name, ParameterDataType dataType ) {
 this.operator=operator;
		
		if(dataType==null){
			
		}else{
			this.dataType=dataType;
		}
		 
		this.name=name;
	}
	
	public OperatorParameterImpl( Operator operator,   String name   ) {
		 
		this(operator,name,null);
	}
	
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public Object getValue() {
		return value;
	}
	@Override
	public void setValue(Object value) {
		this.value = value;
	}
	@Override
	public ParameterDataType getDataType() {
		return dataType;
	}
	@Override
	public void setDataType(ParameterDataType type) {
		this.dataType = type;
	}

	@Override
	public Operator getOperator() {
		
		return this.operator;
	}
	@Override
	public void setOperator(Operator operator) {
		this.operator=operator;
		
	}
 
	@Override
	public String getParameterLabel(Locale locale,Operator operator){
		
		String label=LanguagePack.getMessage(name, locale);
		if(operator instanceof CopytoHadoopOperator && name.equals(OperatorParameter.NAME_HD_connetionName)){
			label= LanguagePack.getMessage(LanguagePack.CopyTo_Label, locale);
				//copy to 
			
		}else if(operator instanceof CopytoHadoopOperator && name.equals(OperatorParameter.NAME_HD_ResultsLocation)){
			//destination
			label = LanguagePack.getMessage(LanguagePack.Destination_Lable, locale);
		}else if(operator instanceof CopyToDBOperator && name.equals(OperatorParameter.NAME_dBConnectionName)){
			label= LanguagePack.getMessage(LanguagePack.CopyTo_Label, locale);
		}
		else if(operator instanceof CopyToDBOperator && name.equals(OperatorParameter.NAME_schemaName)){
			label= LanguagePack.getMessage(LanguagePack.Destination_Lable, locale);
		}
		if(label==null){
			label=name;
		}
		return label;
	}
	@Override
	public   String getParameterLabel(Operator operator){
		return getParameterLabel(Locale.getDefault(),  operator);
	}
	
	public String toString(){
		return "name:"+getName()+" value:"+getValue()+"\n";
	} 
 
}
