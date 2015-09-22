package com.alpine.miner.workflow.operator.parameter.association;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;

public class ExpressionModel extends AbstractParameterObject {

	public static final String TAG_NAME="Expression";

	private static final String ATTR_EXPRESSION = "expression";

	private static final String ATTR_POSITIVE = "positiveValue";
	
	private String positiveValue;
	
	private String expression;
	
	public ExpressionModel(){

	}
	
	public ExpressionModel(String positiveValue, String expression) {
		this.positiveValue = positiveValue;
		this.expression = expression;
	}

	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public String getPositiveValue() {
		return positiveValue;
	}

	public void setPositiveValue(String positiveValue) {
		this.positiveValue = positiveValue;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public static ExpressionModel fromXMLElement(Element element) {
		String expression=element.getAttribute(ATTR_EXPRESSION);
		String positiveValue=element.getAttribute(ATTR_POSITIVE);
		ExpressionModel expressionModel=new ExpressionModel(positiveValue,expression);
		return expressionModel;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ExpressionModel){
			ExpressionModel model=(ExpressionModel)obj;
			if(model.getExpression().equals(expression)
					&&model.getPositiveValue().equals(positiveValue)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new ExpressionModel(positiveValue, expression);
	}

	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_EXPRESSION, getExpression());
		element.setAttribute(ATTR_POSITIVE, getPositiveValue());
		return element;
	}


}
