/**
 * ClassName LinearRegressionOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.linearregression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class LinearRegressionOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_Interaction_Columns,
			OperatorParameter.NAME_columnNames,
			OperatorParameter.NAME_isStepWise,
			OperatorParameter.NAME_stepWiseType,
			OperatorParameter.NAME_criterionType,
			OperatorParameter.NAME_checkValue,
			OperatorParameter.NAME_groupByTrainerColumn,
			OperatorParameter.NAME_AddResidualPlot
	});
	
	public LinearRegressionOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_LIR);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LINEARREGRESSION_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		InterActionColumnsModel interModel=null;
		String columnName=null;
		String isStepWise=null;
		String dependColumn = null;
		for(OperatorParameter para:paraList){
			if(para.getName().equals(OperatorParameter.NAME_columnNames)){
				columnName=(String)para.getValue();
			}else if(para.getValue() instanceof InterActionColumnsModel){
				interModel=(InterActionColumnsModel)para.getValue();
			}else if(para.getName().equals(OperatorParameter.NAME_isStepWise)){
				isStepWise=(String)para.getValue();
			}else if(para.getName().equals(OperatorParameter.NAME_dependentColumn)){
				dependColumn=(String)para.getValue();
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Interaction_Columns)){
				if((interModel==null||interModel.getInterActionItems().size()==0)
						&&StringUtil.isEmpty(columnName)){
					invalidParameterList.add(paraName);
					continue;
				}
				validateInteractionColumns(fieldList,invalidParameterList, paraName, interModel);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				if((interModel==null||interModel.getInterActionItems().size()==0)
						&&StringUtil.isEmpty(columnName)){
					invalidParameterList.add(paraName);
					continue;
				}
				if(!StringUtil.isEmpty(isStepWise)&&isStepWise.equals(Resources.TrueOpt)){
					validateNull(invalidParameterList, paraName, paraValue);
				}
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_checkValue)){
				if(!StringUtil.isEmpty(isStepWise)&&isStepWise.equals(Resources.TrueOpt)){
					validateNull(invalidParameterList, paraName, paraValue);
					validateNumber(invalidParameterList, paraName, paraValue, 0, false, 1, false,variableModel);
				}	
			}else if(paraName.equals(OperatorParameter.NAME_groupByTrainerColumn)){
				if(!StringUtil.isEmpty(columnName)){
					String[] columnNames = columnName.split(",");
					for(String s:columnNames){
						if(s.equals(paraValue)){
							invalidParameterList.add(paraName);
							break;
						}
					}
				}
				if(!StringUtil.isEmpty(dependColumn)){
					if(dependColumn.equals(paraValue)
							&& !invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
				if(!invalidParameterList.contains(paraName)){
					String dbType = OperatorUtility.getDbType(this);
					if(StringUtil.isEmpty(dbType)==false
							&&(dbType.equals(DataSourceInfoOracle.dBType)
							||dbType.equals(DataSourceInfoDB2.dBType)
							||dbType.equals(DataSourceInfoNZ.dBType))){
//						invalidParameterList.add(paraName);
						para.setValue(null);
					}
				}
			}
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}	
	}

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> interActionNodeList=opTypeXmlManager.getNodeList(opNode, InterActionColumnsModel.TAG_NAME);
		if(interActionNodeList!=null&&interActionNodeList.size()>0){
			Element interActionElement=(Element)interActionNodeList.get(0);
			InterActionColumnsModel interActionModel=InterActionColumnsModel.fromXMLElement(interActionElement);
			getOperatorParameter(OperatorParameter.NAME_Interaction_Columns).setValue(interActionModel);
		}
		
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		
		OperatorParameter interModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_Interaction_Columns);
		
		Object value = interModelParameter.getValue();
		if (! (value instanceof InterActionColumnsModel)) {
			return;
		}
		
		InterActionColumnsModel interActionModel=(InterActionColumnsModel)value;
		if(interActionModel!=null
				&&interActionModel.getInterActionItems()!=null
				&&interActionModel.getInterActionItems().size()>0){
			element.appendChild(interActionModel.toXMLElement(xmlDoc));	
		}
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_isStepWise)){
			return Resources.FalseOpt;
		}else if (paraName.equals(OperatorParameter.NAME_stepWiseType)){
			return "FORWARD";
		}else if (paraName.equals(OperatorParameter.NAME_AddResidualPlot)){
			return Resources.FalseOpt;
		}else if (paraName.equals(OperatorParameter.NAME_checkValue)){
			return "0.05";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
	
}
