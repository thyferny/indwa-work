package com.alpine.datamining.api.impl.db.attribute.model.association;


public class AnalysisExpressionModel{

	public static final String TAG_NAME="Expression";
	
	private static final String ATTR_EXPRESSION = "expression";

	private static final String ATTR_POSITIVE = "positiveValue";

	private String positiveValue;
	
	private String expression;
	
	public AnalysisExpressionModel(){

	}
	
	public AnalysisExpressionModel(String positiveValue, String expression) {
		this.positiveValue = positiveValue;
		this.expression = expression;
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

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisExpressionModel){
			AnalysisExpressionModel model=(AnalysisExpressionModel)obj;
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
		return new AnalysisExpressionModel(positiveValue, expression);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ATTR_EXPRESSION).append(":").append(expression).append(",");
		sb.append(ATTR_POSITIVE).append(":").append(positiveValue);
		return sb.toString();
	}

}
