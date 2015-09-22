/**
 * ClassName SVDOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.svd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;

public class SVDLanczosOperator extends AbstractOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_ColName,
			OperatorParameter.NAME_RowName,
			OperatorParameter.NAME_NumFeatures,
			OperatorParameter.NAME_UmatrixSchema,
			OperatorParameter.NAME_UmatrixTable,
			OperatorParameter.NAME_UmatrixTable_StorageParams,
			OperatorParameter.NAME_UmatrixDropIfExist,
			OperatorParameter.NAME_VmatrixSchema,
			OperatorParameter.NAME_VmatrixTable,
			OperatorParameter.NAME_VmatrixTable_StorageParams,
			OperatorParameter.NAME_VmatrixDropIfExist,
			OperatorParameter.NAME_singularValueSchema,
			OperatorParameter.NAME_singularValueTable,
			OperatorParameter.NAME_singularValueTable_StorageParams,
			OperatorParameter.NAME_singularValueDropIfExist
	});                       
	                          
	public SVDLanczosOperator() {    
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_SVD);
	}
	
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SVD_OPERATOR,locale);
	}


	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String dColumn=null;
		String rowColumn=null;
		String colColumn=null;
		String uSchema=null;
		String uTable=null;
		String vSchema=null;
		String vTable=null;
		String sSchema=null;
		String sTable=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				dColumn=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_ColName)){
				rowColumn=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_RowName)){
				colColumn=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_UmatrixSchema)){
				uSchema=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_UmatrixTable)){
				uTable=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_VmatrixSchema)){
				vSchema=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_VmatrixTable)){
				vTable=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_singularValueSchema)){
				sSchema=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_singularValueTable)){
				sTable=(String)para.getValue();
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)
						&&(dColumn.equals(rowColumn)||
								dColumn.equals(colColumn))){
					invalidParameterList.add(paraName);
				}
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_ColName)){
				validateNull(invalidParameterList, paraName, paraValue);
				if(StringUtil.isEmpty(paraValue)&&!invalidParameterList.contains(paraName)
						&&(colColumn.equals(rowColumn)||
								colColumn.equals(dColumn))){
					invalidParameterList.add(paraName);
				}
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_RowName)){
				validateNull(invalidParameterList, paraName, paraValue);
				if(StringUtil.isEmpty(paraValue)&&!invalidParameterList.contains(paraName)
						&&(rowColumn.equals(dColumn)||
								rowColumn.equals(colColumn))){
					invalidParameterList.add(paraName);
				}
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_NumFeatures)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_UmatrixSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_UmatrixTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
				if(!StringUtil.isEmpty(uSchema)&&!StringUtil.isEmpty(uTable)
						&&!StringUtil.isEmpty(vSchema)&&!StringUtil.isEmpty(vTable)){
					if(uSchema.equals(vSchema)&&uTable.equals(vTable)
							&&!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
				if(!StringUtil.isEmpty(uSchema)&&!StringUtil.isEmpty(uTable)
						&&!StringUtil.isEmpty(sSchema)&&!StringUtil.isEmpty(sTable)){
					if(uSchema.equals(sSchema)&&uTable.equals(sTable)
							&&!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_UmatrixDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_VmatrixSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_VmatrixTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
				if(!StringUtil.isEmpty(uSchema)&&!StringUtil.isEmpty(uTable)
						&&!StringUtil.isEmpty(vSchema)&&!StringUtil.isEmpty(vTable)){
					if(uSchema.equals(vSchema)&&uTable.equals(vTable)
							&&!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
				if(!StringUtil.isEmpty(vSchema)&&!StringUtil.isEmpty(vTable)
						&&!StringUtil.isEmpty(sSchema)&&!StringUtil.isEmpty(sTable)){
					if(vSchema.equals(sSchema)&&vTable.equals(sTable)
							&&!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_VmatrixDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_singularValueSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_singularValueTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
				if(!StringUtil.isEmpty(sSchema)&&!StringUtil.isEmpty(sTable)
						&&!StringUtil.isEmpty(uSchema)&&!StringUtil.isEmpty(uTable)){
					if(sSchema.equals(uSchema)&&sTable.equals(uTable)
							&&!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
				if(!StringUtil.isEmpty(sSchema)&&!StringUtil.isEmpty(sTable)
						&&!StringUtil.isEmpty(vSchema)&&!StringUtil.isEmpty(vTable)){
					if(vSchema.equals(sSchema)&&vTable.equals(sTable)
							&&!invalidParameterList.contains(paraName)){
						invalidParameterList.add(paraName);
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_singularValueDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
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
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=getOperatorInputList();
		return operatorInputList;
	}
	
	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new EngineModel());
		return list;
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_NumFeatures)){
			return "2";
		}else if(paraName.equals(OperatorParameter.NAME_UmatrixDropIfExist)){
			return Resources.YesOpt;
		}else if(paraName.equals(OperatorParameter.NAME_singularValueDropIfExist)){
			return Resources.YesOpt;
		}else if(paraName.equals(OperatorParameter.NAME_VmatrixDropIfExist)){
			return Resources.YesOpt;
		}else if(paraName.equals(OperatorParameter.NAME_UmatrixSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else if(paraName.equals(OperatorParameter.NAME_VmatrixSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else if(paraName.equals(OperatorParameter.NAME_singularValueSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
