package com.alpine.miner.workflow.operator.customize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.db.attribute.model.customized.COUtility;
import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedOperatorModel;
import com.alpine.datamining.api.impl.db.attribute.model.customized.ParameterModel;
import com.alpine.miner.model.uitype.AbstractUIControlType;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.storageparam.StorageParameterModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.xml.XmlDocManager;

public class CustomizedOperator extends DataOperationOperator {
	private CustomizedOperatorModel coModel;
	private String operatorName;

	public CustomizedOperator(String operatorName) throws Exception {
		coModel = UDFManager.INSTANCE
				.getCustomizedOperatorModelByOperatorName(operatorName);
		if (coModel == null) {
			coModel = UDFManager.INSTANCE.getCustomizedOperatorModelByUDFName(operatorName);
			if(coModel == null){
				throw new Exception(
						"Customized OperatorModel not found . \nPlease make sure the customized operator are registed.");
			}
		}

		List<String> paramNames = new ArrayList<String>();
		HashMap<String, ParameterModel> paraMaps = coModel.getParaMap();
		Set<String> keys = paraMaps.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			ParameterModel model = paraMaps.get(it.next());
			paramNames.add(model.getParaName());
		}

		// ----default ------
		paramNames.add(OperatorParameter.NAME_outputType);
		paramNames.add(OperatorParameter.NAME_outputSchema);
		paramNames.add(OperatorParameter.NAME_outputTable);
		paramNames.add(OperatorParameter.NAME_outputTable_StorageParams);
		paramNames.add(OperatorParameter.NAME_dropIfExist);
		paramNames.add(OperatorParameter.NAME_remainColumns);// /same as
		// NAME_columnNames)
		// ;
		super.setParameterNames(paramNames);
		this.operatorName = operatorName;
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	public CustomizedOperatorModel getCoModel() {
		return coModel;
	}

	public void setCoModel(CustomizedOperatorModel coModel) {
		this.coModel = coModel;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	@Override
	public String getToolTipTypeName() {
		return operatorName.substring(0, operatorName.lastIndexOf("_"));
	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		HashMap<String, String>  outMap=coModel.getOutputColumnMap();
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				operatorInputTableInfo.setSchema((String) getOperatorParameter(
						OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String) getOperatorParameter(
						OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo
						.setTableType((String) getOperatorParameter(
								OperatorParameter.NAME_outputType).getValue());
				
				String dataSource=operatorInputTableInfo.getSystem();
				
				List<String[]> oldFieldColumns = operatorInputTableInfo
				.getFieldColumns();
				List<String[]> newFieldColumns = new ArrayList<String[]>();
				
				Iterator<Entry<String, String>>  iter=outMap.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String, String> entry=iter.next();
					String columnName=entry.getKey();
					String type=entry.getValue();
					newFieldColumns.add(new String[]{columnName,AlpineUtil.converterDateType(type, dataSource)});
				}
				
				String remainColumns = (String)getOperatorParameter(OperatorParameter.NAME_remainColumns).getValue();
				
				if(!StringUtil.isEmpty(remainColumns)){
					String[] temp=remainColumns.split(",");
					for(int i=0;i<temp.length;i++){
						for(String[] ss:oldFieldColumns){
							if(temp[i].equals(ss[0])){
								newFieldColumns.add(new String[]{ss[0],ss[1]});
								break;
							}					
						}
					}
				}
				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(operatorInputTableInfo);
				break;
			}
		}
		return operatorInputList;
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		HashMap<String, ParameterModel> paraMap = coModel.getParaMap();
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			if(paraName.equals(OperatorParameter.NAME_remainColumns)){
				validateColumnNames(fieldList,invalidParameterList, paraName, (String)para.getValue());
				continue;
			}
			Object paraValue = para.getValue();
			if (paraValue != null) {
				if (paraValue instanceof String) {
					validateNull(invalidParameterList, paraName,
							(String) paraValue);
					ParameterModel pModel = paraMap.get(paraName);
					if(pModel!=null){
						String paraType = pModel.getParaType();
						List<String> optionalValues = pModel.getOptionalValue();
						if(paraType.equals(AbstractUIControlType.COMBO_CONTROL_TYPE)
								&&optionalValues==null
								){
							validateContainColumns(fieldList,invalidParameterList, paraName, (String)paraValue);
						}
					}
				}
			} else {
				if(paraName.equals(OperatorParameter.NAME_outputTable_StorageParams)==false){ 
					invalidParameterList.add(paraName);
				}
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

	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = new ArrayList<OperatorParameter>();

		ArrayList<Node> parameterNodeList = opTypeXmlManager.getNodeList(
				opNode, "Parameter");
		HashMap<String, OperatorParameter> paraMap = new HashMap<String, OperatorParameter>();
		for (Node parameterNode : parameterNodeList) {
			String paraName = ((Element) parameterNode).getAttribute("key");

			String paraValue = null;
			if (paraName.equals("password")) {
				paraValue = XmlDocManager
						.decryptedPassword(((Element) parameterNode)
								.getAttribute("value"));
			} else {
				if (this.getCoModel().hasParamPosition(paraName) == true) {
					paraName = this.getCoModel().getParamNameByPosition(
							paraName);
				}
				paraValue = ((Element) parameterNode).getAttribute("value");
			}
			setSimpleParametersValue(this, operatorParameters, paraName,
					paraValue, paraMap);
		}
		setParameters(this, operatorParameters, paraMap);
		String[] paramName = StorageParameterModel.getPossibleTags();
		if(paramName!=null){
			for(int i = 0 ;i<paramName.length;i++){
				fillStorageParameters(opTypeXmlManager, opNode, paramName[i], operatorParameters);
			}
		}
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {

		String operatorName = this.getCoModel().getOperatorName();
		String udfschema = this.getCoModel().getUdfSchema();
		String udfName = this.getCoModel().getUdfName();
		element.setAttribute(COUtility.OPERATOR_NAME, operatorName);
		element.setAttribute("udfschema", udfschema);
		element.setAttribute("udfName", udfName);

		HashMap<String, String> outputMap = this.getCoModel()
				.getOutputColumnMap();
		Iterator<Map.Entry<String, String>> iter = outputMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			Element parameter = xmlDoc.createElement("outputColumns");
			parameter.setAttribute("column", entry.getKey());
			parameter.setAttribute("type", entry.getValue());
			element.appendChild(parameter);
		}

		List<OperatorParameter> parameterList = getOperatorParameterList();
		Iterator<OperatorParameter> iter_para = parameterList.iterator();
		while (iter_para.hasNext()) {
			OperatorParameter parameter = iter_para.next();
			String paraName = parameter.getName();
			// /name -> index
			HashMap<String, ParameterModel> paraMap = this.getCoModel()
					.getParaMap();
			if (paraMap.keySet().contains(paraName)) {
				paraName = paraMap.get(paraName).getPosition();
			}

			Object value = parameter.getValue();
			if (value instanceof String) {
				createSimpleElements(xmlDoc, element, value, paraName,
						addSuffixToOutput);
			}else if (value instanceof StorageParameterModel){
				
				StorageParameterModel model = (StorageParameterModel)ParameterUtility.getParameterByName(this,paraName).getValue();
				Element ele = model.toXMLElement(xmlDoc,paraName); 
 
				element.appendChild(ele);
				
			}
		}
	}
	
	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
}
