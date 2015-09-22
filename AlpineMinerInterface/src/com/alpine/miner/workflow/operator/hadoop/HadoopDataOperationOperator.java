package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;

public abstract class HadoopDataOperationOperator extends HadoopOperator {

	protected HadoopDataOperationOperator(List<String> parameterNames) {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(OperatorInputFileInfo.class.getName());
	}

	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputFileInfo());
		return list;
	}


	/**
	 * @param storeResults 
	 * 
	 */
	protected void validateHadoopStorageParameter(String paraName,String paraValue,List<String> invalidParameterList) {
		List<OperatorParameter> paraList=getOperatorParameterList();
		
		String storeResults=null;
		for(OperatorParameter para:paraList){
			if(para.getName().equals(OperatorParameter.NAME_HD_StoreResults)){
				storeResults=(String)para.getValue();
				break;
			}
		}
		if(paraName.equals(OperatorParameter.NAME_HD_StoreResults)){
			validateNull(invalidParameterList, paraName, paraValue);		
		}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
			if(Resources.TrueOpt.equals(storeResults)){
				validateNull(invalidParameterList, paraName, paraValue);
			}	
		}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsName)){
			if(Resources.TrueOpt.equals(storeResults)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
		}	
	}
	
	protected String getOldColumnType(FileStructureModel oldModel,String columnName){
		List<String> columnNameList = oldModel.getColumnNameList();
		List<String> columnTypeList = oldModel.getColumnTypeList();
		if(columnNameList!=null){
			for(int i=0;i<columnNameList.size();i++){
				if(columnNameList.get(i).equals(columnName)){
					return columnTypeList.get(i);
				}
			}
		}
		return null;
	}

    @Override
    public List<Object> getOperatorOutputList() {
        List<Object> operatorInputList = getOperatorInputList();
        if (operatorInputList == null)
            return null;
        List<Object> operatorOutputList = new ArrayList<Object>();
        for (Object obj : operatorInputList) {
            if (obj instanceof OperatorInputFileInfo) {
                OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
                
                operatorInputFileInfo = operatorInputFileInfo.clone();
                String hadoopFileName = getOutputFileName();
                operatorInputFileInfo.setHadoopFileName(hadoopFileName);
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;
                operatorInputFileInfo.getColumnInfo().setIsFirstLineHeader(Resources.FalseOpt);
                operatorOutputList.add(operatorInputFileInfo);
                break;
            }
        }
        return operatorOutputList;
    }
    
	protected OperatorInputFileInfo getInputFileInfo(String uuid) {
		List<OperatorInputFileInfo> parentHadoopFileInfos = getParentHadoopFileInputs();
		if (parentHadoopFileInfos != null && uuid != null) {
			for (int i = 0; i < parentHadoopFileInfos.size(); i++) {
				if (uuid.equals(parentHadoopFileInfos.get(i).getOperatorUUID())) // be
				{
					return parentHadoopFileInfos.get(i);
				}
			}
		}
		return null;
	}
}
