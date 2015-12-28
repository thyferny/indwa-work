
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateField;
import com.alpine.miner.workflow.operator.parameter.aggregate.AggregateFieldsModel;
import com.alpine.utility.db.Resources;
import com.alpine.utility.xml.XmlDocManager;

public class HadoopAggregateOperator extends HadoopDataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{

			OperatorParameter.NAME_aggregateFieldList,
			//OperatorParameter.NAME_windowFieldList,

			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
	});
	
	public HadoopAggregateOperator() {
		super(parameterNames);
	}

	

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.AGGREGATE_OPERATOR,locale);
	}

	

	@Override
	public boolean isVaild(VariableModel variableModel) {
		 
		
		AggregateFieldsModel fieldModel = null;
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		for (OperatorParameter para : paraList) {
			if (para.getValue() instanceof String)
				continue;
			Object paraObj = para.getValue();
			if (paraObj instanceof AggregateFieldsModel) {
				fieldModel = (AggregateFieldsModel) paraObj;
			}  
		}
		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if (paraName.equals(OperatorParameter.NAME_aggregateFieldList)) {
				validateAggregateModel(fieldModel, invalidParameterList,paraName);
			}
			else {				
				validateHadoopStorageParameter(paraName,paraValue,invalidParameterList);
			}
		}
		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputFileInfo) {
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
				operatorInputFileInfo =operatorInputFileInfo.clone();
			 	String hadoopFileName = getOutputFileName();
				operatorInputFileInfo.setHadoopFileName(hadoopFileName);
				operatorInputFileInfo.setIsDir(true) ;
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());
                //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

                FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				if(columnInfo!=null){
					fillColumnInfo2NewOutput(columnInfo);
				}
				
				
			 operatorInputList.add(operatorInputFileInfo);
				break;
			}
		}
		return operatorInputList;
	}

	
	private void fillColumnInfo2NewOutput(FileStructureModel columnInfo) {
		List<String> oldColumns =columnInfo.getColumnNameList() ;
		List<String> oldColumnTypes =columnInfo.getColumnTypeList() ;
		
		List<String> newColumns = new ArrayList<String>();
		List<String> newColumnTypes = new ArrayList<String>();

		AggregateFieldsModel aggModel = (AggregateFieldsModel) getOperatorParameter(
				OperatorParameter.NAME_aggregateFieldList).getValue();
		if (aggModel != null) {
			List<String> groupByFieldList = aggModel
					.getGroupByFieldList();
			for (String s : groupByFieldList) {
				for (String fieldColumn : oldColumns) {
					if (fieldColumn.equals(s)) {
				 
						newColumns.add(fieldColumn);
						newColumnTypes.add(oldColumnTypes.get(oldColumns.indexOf(fieldColumn)));
						break;
					}
				}
			}
			List<AggregateField> aggField = aggModel
					.getAggregateFieldList();
			for (AggregateField field : aggField) {
 
				newColumns.add(field.getAlias());
				newColumnTypes.add(field.getDataType() );
			
			}
		}
		columnInfo.setIsFirstLineHeader(Resources.FalseOpt);
		columnInfo.setColumnNameList(newColumns);
		columnInfo.setColumnTypeList(newColumnTypes) ;
		
		
	}



	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(
				opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);

		readAggregateModel(opTypeXmlManager, opNode);

 
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);

		storeAggregateFielsdModel(xmlDoc, element);

	}

 

}
