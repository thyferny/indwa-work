/**
 * ClassName  JoinCondition.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.tablejoin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author John Zhao
 *
 */
public class JoinCondition implements    XMLFragment {

 
	private static final String ATTR_TABLE_ALIAS1 = "tableAlias1";

	private static final String ATTR_TABLE_ALIAS2 = "tableAlias2";

	private static final String ATTR_JOIN_TYPE = "joinType";

	private static final String ATTR_JOIN_COND = "condition";
	
	private static final String ATTR_AND_OR = "andOr";

	public static final String TAG_NAME = "JoinConditionModel";
	
	public static final String COLUMN1 = "column1";
	
	public static final String COLUMN2 = "column2";
	
	public static final String JOINCONDITION = "joinCondition";

	String tableAlias1;

	String tableAlias2;
	String joinType;
	String condition;
	String andOr;
	
	String column1;
	String column2;


	/**
	 * @param alias1
	 * @param alias2
	 * @param joinType
	 * @param condition
	 */
	public JoinCondition(String alias1,  String joinType, String alias2,
			String condition,String andOr) {
		this.tableAlias1 = alias1;
		this.tableAlias2 = alias2;
		this.joinType = joinType;
		this.condition = condition;
		this.andOr=andOr;
	}
	public String getTableAlias1() {
		return tableAlias1;
	}

	public void setTableAlias1(String tableAlias1) {
		this.tableAlias1 = tableAlias1;
	}

	public String getTableAlias2() {
		return tableAlias2;
	}

	public void setTableAlias2(String tableAlias2) {
		this.tableAlias2 = tableAlias2;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	public String getCondition() {
		return condition;
	}
	@Deprecated
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getColumn1() {
		return column1;
	}
	public void setColumn1(String column1) {
		this.column1 = column1;
	}
	public String getColumn2() {
		return column2;
	}
	public void setColumn2(String column2) {
		this.column2 = column2;
	}
	
	public String getAndOr() {
		return andOr;
	}
	public void setAndOr(String andOr) {
		this.andOr = andOr;
	}
	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_TABLE_ALIAS1, getTableAlias1());
		element.setAttribute(ATTR_TABLE_ALIAS2, getTableAlias2());
		element.setAttribute(ATTR_JOIN_TYPE, getJoinType());
		element.setAttribute(ATTR_JOIN_COND, getCondition());
		element.setAttribute(ATTR_AND_OR, getAndOr());
		element.setAttribute(COLUMN1, getColumn1());
		element.setAttribute(COLUMN2, getColumn2());
		return element;
	}
	/**
	 * @param item
	 * @return
	 */
	public static JoinCondition fromXMLElement(Element item) {
		String tableAlias1=	item.getAttribute(ATTR_TABLE_ALIAS1);
		String tableAlias2=	item.getAttribute(ATTR_TABLE_ALIAS2);
		String joinType=	item.getAttribute(ATTR_JOIN_TYPE);
		String joinCondition=	item.getAttribute(ATTR_JOIN_COND);
		String column1=item.getAttribute(COLUMN1);
		String column2=item.getAttribute(COLUMN2);
		String andOr = item.getAttribute(ATTR_AND_OR);
		JoinCondition model=new JoinCondition(tableAlias1,joinType,tableAlias2,joinCondition,andOr);
		model.setColumn1(column1);
		model.setColumn2(column2);
		return model;
	}

	 public boolean equals(Object obj) {
		 if(obj==null||obj instanceof JoinCondition ==false){
			 return false;
		 }
		 JoinCondition target=(JoinCondition) obj ;
		 //avoid the the null point
		 
		 return ParameterUtility.nullableEquales( target.getAndOr(),getAndOr())
		 			&&ParameterUtility.nullableEquales( target.getCondition(),getCondition())
		 			&&ParameterUtility.nullableEquales( target.getJoinType(),getJoinType())
				 	&&ParameterUtility.nullableEquales( target.getTableAlias1(),getTableAlias1())
				 	&&ParameterUtility.nullableEquales( target.getTableAlias2(),getTableAlias2())
				 	&&ParameterUtility.nullableEquales( target.getColumn1(),getColumn1())
				 	&&ParameterUtility.nullableEquales( target.getColumn2(),getColumn2())
				;
	 }
	 
		@Override
		public Object clone() throws CloneNotSupportedException {
			JoinCondition model=new JoinCondition(tableAlias1,joinType,tableAlias2,condition,andOr);
			model.setColumn1(column1);
			model.setColumn2(column2);
			return model;
		}
		
		
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		this.setTableAlias1(element.getAttribute(ATTR_TABLE_ALIAS1));
		this.setTableAlias2(element.getAttribute(ATTR_TABLE_ALIAS2));
		this.setJoinType(element.getAttribute(ATTR_JOIN_TYPE));
		this.setCondition(element.getAttribute(ATTR_JOIN_COND));
		String column1=element.getAttribute(COLUMN1);
		String column2=element.getAttribute(COLUMN2);
		this.setAndOr(element.getAttribute(ATTR_AND_OR));
	 
		this.setColumn1(column1);
		this.setColumn2(column2);
		
	}
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public  String getXMLTagName() {
		return TAG_NAME;
	}
} 
