/**
 * ClassName HadoopTimeSeriesPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-9
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.miner.ifc.HadoopConnectionManagerFactory;
import com.alpine.miner.ifc.HadoopConnectionManagerIfc;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.AlpineConncetionException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.log4j.Logger;

/**
 * @author Jeff Dong
 *
 */
public class HadoopTimeSeriesPredictOperator extends HadoopPredictOperator {
    private static final Logger itsLogger=Logger.getLogger(HadoopTimeSeriesPredictOperator.class);

    public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_connetionName,
			OperatorParameter.NAME_Ahead_Number,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
	});

	public HadoopTimeSeriesPredictOperator() {
        super(false);
		setParameterNames(parameterNames);
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_ARIMA);
		addOutputClass(OperatorInputFileInfo.class.getName());
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		 
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();

		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_HD_connetionName)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_Ahead_Number)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,10000,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_HD_StoreResults)){
				validateNull(invalidParameterList, paraName, paraValue);		
			}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsName)){
				validateNull(invalidParameterList, paraName, paraValue);
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
	public void saveInputFieldList(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		HadoopConnectionManagerIfc hdManager = HadoopConnectionManagerFactory.INSTANCE.getManager();
		
		String connName = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_connetionName).getValue();
		
		String location = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_ResultsLocation).getValue();
		String result = (String) getOperatorParameter(
				OperatorParameter.NAME_HD_ResultsName).getValue();
		if(StringUtil.isEmpty(connName)){
			return;
		}
		try {
			HadoopConnection hadoopConn = hdManager.readHadoopConnection(connName, userName);
				
			Element ele = xmlDoc.createElement("InPutFieldList");
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_connetionName, connName);
			
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsHostname , hadoopConn.getHdfsHostName());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsPort , String.valueOf(hadoopConn.getHdfsPort()));
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_version, hadoopConn.getVersion());


			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_jobHostname , hadoopConn.getJobHostName());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_jobPort , String.valueOf(hadoopConn.getJobPort()));
			
			 
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_user , hadoopConn.getUserName());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_group  , hadoopConn.getGroupName());
		 	
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_format , HadoopOperator.File_Formats.get(0));
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_fileName , location+HadoopFile.SEPARATOR+result);
		 
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_securityMode  , hadoopConn.getSecurityMode());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsPrincipal , hadoopConn.getHdfsPrincipal());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_hdfsKeyTab  , hadoopConn.getHdfsKeyTab ());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_mapredPrincipal  , hadoopConn.getMapredPrincipal());
			setAttribute(xmlDoc, ele, HadoopFileSelectorConfig.NAME_HD_mapredKeyTab  , hadoopConn.getMapredKeyTab ());

			
			element.appendChild(ele);
			
		}catch (AlpineConncetionException e) {
			itsLogger.error(e.getMessage(),e);
			throw new RuntimeException("1019", e);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		}
	}
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TIMESERIES_PREDICTION_OPERATOR,locale);
	}

}
