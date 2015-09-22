/**
 * ClassName LinearRegressionOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.plda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.Resources;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class PLDATrainerOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			
			//contentSchema ,contentTable is from input link 
			OperatorParameter.NAME_contentDocIndexColumn,
			OperatorParameter.NAME_contentWordColumn,
			OperatorParameter.NAME_forceRetrain,
			
			//input config
			OperatorParameter.NAME_dictionarySchema,
			OperatorParameter.NAME_dictionaryTable,
			OperatorParameter.NAME_dicIndexColumn,
			OperatorParameter.NAME_dicContentColumn,
			
			
			
			//outputconfig
			OperatorParameter.NAME_PLDAModelOutputSchema,
			OperatorParameter.NAME_PLDAModelOutputTable,
			OperatorParameter.NAME_PLDAModelOutputTable_StorageParams,
			OperatorParameter.NAME_PLDADropIfExist,
			
			//parameters
			OperatorParameter.NAME_Alpha,
			OperatorParameter.NAME_Beta,
			OperatorParameter.NAME_IterationNumber,
			//topic out
			OperatorParameter.NAME_topicNumber,
			OperatorParameter.NAME_topicOutSchema,
			OperatorParameter.NAME_topicOutTable,	
			OperatorParameter.NAME_topicOutTable_StorageParams,
			OperatorParameter.NAME_topicDropIfExist,
			 
			OperatorParameter.NAME_docTopicOutSchema,
			OperatorParameter.NAME_docTopicOutTable,
			OperatorParameter.NAME_docTopicOutTable_StorageParams,
			OperatorParameter.NAME_docTopicDropIfExist
			
		 
	});
	
	public PLDATrainerOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_PLDA);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.PLDA_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		boolean needValidateDicIndexColumn= true;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_contentDocIndexColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_contentWordColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}
			else if(paraName.equals(OperatorParameter.NAME_dictionarySchema)){
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}
			else if(paraName.equals(OperatorParameter.NAME_dictionaryTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}
			else if(paraName.equals(OperatorParameter.NAME_dicIndexColumn)){
				List<OperatorInputTableInfo> tableInfo = getParentDBTableSet();
				if(tableInfo!=null&&tableInfo.size()>0){
					String system = tableInfo.get(0).getSystem();
					if(system!=null){
						if(system.equals(DataSourceInfoOracle.dBType)
								||system.equals(DataSourceInfoPostgres.dBType)
								||system.equals(DataSourceInfoGreenplum.dBType)){
							//no need 
							needValidateDicIndexColumn= false;
						}else{
							validateNull(invalidParameterList, paraName, paraValue);						 	
						}
					}
				}
			
			}
			else if(paraName.equals(OperatorParameter.NAME_dicContentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
			
			
			
			//outputconfig
			else if(paraName.equals(OperatorParameter.NAME_PLDAModelOutputSchema)){
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}
			else if(paraName.equals(OperatorParameter.NAME_PLDADropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
			else if(paraName.equals(OperatorParameter.NAME_PLDAModelOutputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}
 
			//topic out
			 
			else if(paraName.equals(OperatorParameter.NAME_topicOutTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}
			else if(paraName.equals(OperatorParameter.NAME_topicOutSchema)){
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}
			else if(paraName.equals(OperatorParameter.NAME_topicDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
			 
			else if(paraName.equals(OperatorParameter.NAME_docTopicOutTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}
			else if(paraName.equals(OperatorParameter.NAME_docTopicOutSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
			else if(paraName.equals(OperatorParameter.NAME_docTopicDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
			
			
			else if(paraName.equals(OperatorParameter.NAME_Alpha)||
					paraName.equals(OperatorParameter.NAME_Beta)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue,0,true,Double.POSITIVE_INFINITY,true,variableModel);
			}
			 
			else if(paraName.equals(OperatorParameter.NAME_IterationNumber)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,true,Integer.MAX_VALUE,true,variableModel);
				
			}
			else if(paraName.equals(OperatorParameter.NAME_topicNumber)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,true,Integer.MAX_VALUE,true,variableModel);
			} 			
		}
		
		
		validateDuplicateValue(invalidParameterList,  			
		OperatorParameter.NAME_contentDocIndexColumn, OperatorParameter.NAME_contentWordColumn );
		if(needValidateDicIndexColumn==true){
			validateDuplicateValue(invalidParameterList,  			
				OperatorParameter.NAME_dicIndexColumn , OperatorParameter.NAME_dicContentColumn );
		}
		
		validateDuplicateTableName(invalidParameterList,  				
		  OperatorParameter.NAME_PLDAModelOutputSchema, OperatorParameter.NAME_PLDAModelOutputTable ,
		 OperatorParameter.NAME_docTopicOutSchema, OperatorParameter.NAME_docTopicOutTable  );
		
		
		validateDuplicateTableName(invalidParameterList,  				 
				  OperatorParameter.NAME_docTopicOutSchema, OperatorParameter.NAME_docTopicOutTable ,
				  OperatorParameter.NAME_topicOutSchema,OperatorParameter.NAME_topicOutTable  );
		


		validateDuplicateTableName(invalidParameterList,				
				  OperatorParameter.NAME_PLDAModelOutputSchema, OperatorParameter.NAME_PLDAModelOutputTable ,
			 
				  OperatorParameter.NAME_topicOutSchema,OperatorParameter.NAME_topicOutTable );
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
		
		return operatorParameters;
	}
 	 
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_IterationNumber )){
			return "20";
		} 
		else if (paraName.equals(OperatorParameter.NAME_Alpha)
				||paraName.equals(OperatorParameter.NAME_Beta)){
			return "0.5";
		}else if (paraName.equals(OperatorParameter.NAME_topicDropIfExist)
				||paraName.equals(OperatorParameter.NAME_PLDADropIfExist)
				||paraName.equals(OperatorParameter.NAME_docTopicDropIfExist)){
			return Resources.YesOpt;
		}else if (paraName.equals(OperatorParameter.NAME_PLDAModelOutputSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else if (paraName.equals(OperatorParameter.NAME_topicOutSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else if (paraName.equals(OperatorParameter.NAME_docTopicOutSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
	
}
