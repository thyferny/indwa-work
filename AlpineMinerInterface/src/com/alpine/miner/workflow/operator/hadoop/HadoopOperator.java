/**
 * ClassName HadoopOperator.java
 *
 * Version information:1.00
 *
 * Date:Jun 11, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.tools.NLSUtility;

public abstract class HadoopOperator extends AbstractOperator {
	public static final List<String> File_Formats = Arrays.asList(new String[]{		
				 "Text File", "XML", "JSON","Log File"});
	//private static final Logger itsLogger=Logger.getLogger(HadoopOperator.class);
	protected HadoopOperator(List<String> parameterNames) {
		super(parameterNames);
	}
	
	public List<OperatorInputFileInfo> getParentHadoopFileInputs() {
		List<OperatorInputFileInfo> dbTableSets = new ArrayList<OperatorInputFileInfo>();

		List<Object> inputList = this.getOperatorInputList();
		if(inputList==null){
			return dbTableSets;
		}
		for (Object obj : inputList) {
			if (obj instanceof OperatorInputFileInfo) {
				dbTableSets.add((OperatorInputFileInfo) obj);
			}
		}
		return dbTableSets;
 
	}

	
	public void saveInputFieldList(Document xmlDoc, Element operator_element,
			boolean addSuffixToOutput) {
		if(getOperatorInputList()!=null){
			for (Object obj : getOperatorInputList()) {
				if (obj instanceof OperatorInputFileInfo) {
					OperatorInputFileInfo info = (OperatorInputFileInfo) obj;
					Element ele = xmlDoc.createElement("InPutFieldList");
					fillInputFieldList4Hadoop(xmlDoc, info, ele);
					
					if(info.getColumnInfo()!=null){
						ele.appendChild(info.getColumnInfo().toXMLElement(xmlDoc));
					}
					
					operator_element.appendChild(ele);
					
				}
			 
			}
		}
	}

	protected void fillInputFieldList4Hadoop(Document xmlDoc,
			OperatorInputFileInfo info, Element ele) {
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_connetionName, info.getConnectionName());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsHostname , info.getHdfsHostname() );
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsPort , info.getHdfsPort() );
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_version, info.getVersion());

		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_jobHostname , info.getJobHostname() );
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_jobPort , info.getJobPort() );
		
		 
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_user , info.getUser());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_group  , info.getGroup());
		
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_format , info.getHadoopFileFormat());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_fileName , info.getHadoopFileName());
		
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_securityMode  , info.getSecurityMode());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsPrincipal , info.getHdfsPrincipal());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsKeyTab  , info.getHdfsKeyTab ());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_mapredPrincipal  , info.getMapredPrincipal());
		setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_mapredKeyTab  , info.getMapredKeyTab ());
		
	}
	
	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputFileInfo> dbList = new ArrayList<OperatorInputFileInfo>();
		if (list != null) {
			for (Object obj : list) {
				if (obj instanceof OperatorInputFileInfo) {
					dbList.add((OperatorInputFileInfo) obj);
				}
			}
		}
		if (dbList.size() == 1) {
			return true;
		} else {
			return false;
		}
	}
	



	protected String getOutputFileName() {
		String folder = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_ResultsLocation).getValue(); 
		String file =  (String) getOperatorParameter(
				OperatorParameter.NAME_HD_ResultsName ).getValue();
		String hadoopFileName= folder
				+  HadoopFile.SEPARATOR+ file ;
		return hadoopFileName;
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator ,boolean multiple) {
		String message = super.validateInputLink(precedingOperator,multiple);
			if(!StringUtil.isEmpty(message)){
				return message;
			}
			List<String> precedingOutputList = precedingOperator.getOutputClassList();
			if(precedingOperator instanceof ModelOperator && (this instanceof HadoopPredictOperator )){
				if(precedingOperator instanceof ModelOperator && precedingOutputList==null){
					precedingOutputList = fillModelType(precedingOperator);
				 
				}
			}
			/**
			 * check only one table output operator linked
			 */
			if(multiple==false){
				return validateMultipleInput(precedingOperator);
			}
			return "";
			
		}

    @Override
	protected String validateMultipleInput(Operator precedingOperator){
		List<String> precedingOutputList = precedingOperator.getOutputClassList();

		boolean sourceTable = false;
		for (String str : precedingOutputList) {
			if (str.equals(OperatorInputFileInfo.class.getName())) {
				sourceTable = true;
			}
		}
		boolean targetTable = false;

		List<UIOperatorModel> parentList = OperatorUtility
				.getParentList(getOperModel());
		for (UIOperatorModel om : parentList) {
			if(om.getOperator().getOutputClassList()!=null){
				for (String str : om.getOperator().getOutputClassList()) {
					if (str.equals(OperatorInputFileInfo.class.getName())
							||str.equals(OperatorInputTableInfo.class.getName())) {
						targetTable = true;
						break;
					}
				}
			}
		}
		if (sourceTable && targetTable) {
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.CANT_LINK_MUTIL_DATASOURCE,locale),
					this.getToolTipTypeName());
		}
		return "";
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_HD_StoreResults)) {
			return Resources.FalseOpt;
		} else if (paraName.equals(OperatorParameter.NAME_HD_Override)) {
			return Resources.YesOpt;
		} else if (paraName.equals(OperatorParameter.NAME_HD_format)) {
			return File_Formats.get(0) ;
		} else if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
			VariableModel variableModel = getWorkflow().getParentVariableModel();
			if(variableModel!=null&&variableModel.containsKey(VariableModel.DEFAULT_TMPDIR)){
				String tmpDir = variableModel.getVariable(VariableModel.DEFAULT_TMPDIR);
				return tmpDir;
			}
			return "";
		}
		return "";
	}
}
