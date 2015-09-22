/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * PropertyController.java
 */
package com.alpine.miner.impls.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.datamining.api.utility.DBDataUtil;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.utility.db.DbConnection;

/**
 * property request handler
 * all requests from property stuff would be moved in this class
 * @author Gary
 * Aug 23, 2012
 */
@Controller
@RequestMapping("/main/property.do")
public class PropertyController extends FlowController {

	/**
	 * @throws Exception
	 */
	public PropertyController() throws Exception {
		super();
	}
	
    @RequestMapping(params = "method=getPropertyData", method = RequestMethod.POST)
    public void getPropertyData(String uuid, String user,
                                HttpServletRequest request, HttpServletResponse response,
                                ModelMap model) throws Exception {

        if (checkUser(user, request, response) == false) {
            return  ;
        }
        FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
        if (info == null) {
            generateErrorDTO(response, BAD_REQUEST, request.getLocale());
            return;
        }
        OperatorWorkFlow flow = null;
        try {
            flow = rmgr.getFlowData(info,request.getLocale());
            if (flow == null) {
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND));
                return;
            }
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response,
                    new ErrorDTO(FLOW_NOT_FOUND, e.getMessage()));
            e.printStackTrace();
        }
        try {
            for (UIOperatorModel op : flow.getChildList()) {
                if (uuid.equals(op.getUUID()) == false) {
                    continue;
                }
                OperatorDTO obj = new OperatorDTO(user, info, op, flow.getVariableModelList(), request.getLocale());
                ProtocolUtil.sendResponse(response, obj);
                break;
            }
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=updatePropertyData", method = RequestMethod.POST)
    public void updatePropertyData(String user, HttpServletRequest request,
                                   HttpServletResponse response, ModelMap model) throws IOException {
        if (checkUser(user, request, response) == false) {
            return  ;
        }
        OperatorDTO opDTO = ProtocolUtil.getRequest(request, OperatorDTO.class);
        if (opDTO == null) {
            generateErrorDTO(response, BAD_REQUEST, request.getLocale());
            return;
        }
        //FIXED MINERWEB-602
        if("RandomSamplingOperator".equals(opDTO.getClassname())
                || "StratifiedSamplingOperator".equals(opDTO.getClassname())
                || "TimeSeriesOperator".equals(opDTO.getClassname())
                || "PivotOperator".equals(opDTO.getClassname())
                || "SQLExecuteOperator".equals(opDTO.getClassname())
                || "ProductRecommendationOperator".equals(opDTO.getClassname())
                || "LogisticRegressionOperator".equals(opDTO.getClassname())
                || "LinearRegressionOperator".equals(opDTO.getClassname())){
            for(PropertyDTO property : opDTO.getPropertyList()){
                if("randomSeed".equals(property.getName())
                        || "groupColumn".equals(property.getName())
                        || "aggregateColumn".equals(property.getName())
                        || "dbConnectionName".equals(property.getName())
                        || "targetCohort".equals(property.getName())
                        || "criterionType".equals(property.getName())//PR
                        ){
                    property.setValue(property.getValue().trim());
                }
            }
        }
        FlowInfo info = opDTO.getFlowInfo();
        String uuid = opDTO.getUuid();
        if (rmgr.hasBeenUpdated(info)) {
            ProtocolUtil.sendResponse(response, new ErrorDTO(HAS_BEEN_UPDATED));
            return;
        }
        try {
        	   OperatorWorkFlow flow =rmgr.getFlowData(info, request.getLocale() );
               boolean isPropertyChanged = false;
            for (UIOperatorModel op : flow.getChildList()) {
                if (uuid.equals(op.getUUID()) == false) {
                    continue;
                }
                OperatorParameter param = null;
                for (PropertyDTO p : opDTO.getPropertyList()) {
                    if (p == null
                            || p.getType() == PropertyDTO.PropertyType.PT_UNKNOWN) {
                        continue;
                    }
                    String undefined = "undefined";
                    //here set the value...
                    Object pValue=PropertyUtil.getValueFromDTO(p) ;
                    if (undefined.equals(pValue)) {
                        continue;
                    }
                    param = OperatorDTO.getParamByName(op, p.getName());
                    if (param != null) {

                        if(param.getValue()!=null&&pValue!=null){
                            if(param.getValue().equals(pValue)==false){
                                if(param.getValue() instanceof ExpressionModel){
                                    ExpressionModel expressionModel = (ExpressionModel)param.getValue();
                                    String positiveValue = null;
                                    for(PropertyDTO pdto : opDTO.getPropertyList()){
                                        if("positiveValue".equals(pdto.getName())){
                                            positiveValue = pdto.getValue();
                                            break;
                                        }
                                    }
                                    if(!expressionModel.getExpression().equals(pValue) ||
                                            !expressionModel.getPositiveValue().equals(positiveValue)){
                                        isPropertyChanged = true;
                                    }
                                }else{
                                    isPropertyChanged=true;
                                }
                            }
                        }else{
                            if ((param.getValue()==null&&pValue!=null&&!"".equals(pValue))||
                                    (param.getValue()!=null&&pValue!=null)){
                                isPropertyChanged=true;
                            }
                        }
                        param.setValue(p.getValue());
                    }
                }
                // now update the custom model data
                opDTO.updateCustomProperty(op, user, info.getResourceType());
                break;
            }
            //actually, as memory curor we don't have to call update function, but call it to make logic clear.
            rmgr.updateFlow(info, flow);
            OperatorWorkFlow newflow =flow;
            //if subflow path, need reload MINERWEB-1010
            //if("SubFlowOperator".equals(opDTO.getClassname())) {
                //newflow = Persistence.INSTANCE.readWorkFlow(info,request.getLocale()) ;
            //}
            FlowDTO  flowDTO = new FlowDTO(info, newflow,
                    getUserName(request));
            flowDTO.setPropertyChanged(isPropertyChanged) ;
            ProtocolUtil.sendResponse(response, flowDTO);
        } catch ( Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }
    
	@RequestMapping(params="method=distinctColumnValues", method=RequestMethod.GET)
	public void distinctColumnValues(String connName, String schema, String table, String columnName, HttpServletRequest request, HttpServletResponse response) throws IOException{
		DBDataUtil dbd;
		DbConnection connInfo = null;
		List<String> columnValueList;
		//get Connection first.
		try {
			connInfo = WebDBResourceManager.getInstance().getDBConnection(getUserName(request), connName, ResourceType.Personal).getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			generateErrorDTO(response, e.getMessage(), request.getLocale());
			return;
		}
		dbd = new DBDataUtil(connInfo);
		try {
			String distinctCount = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_ALG,PreferenceInfo.KEY_DISTINCT_VALUE_COUNT);
			columnValueList = new ArrayList<String>();
			dbd.loadDistinctValue(columnValueList, schema, table, columnName, Integer.parseInt(distinctCount), connInfo.getDbType());
		} catch (Exception e) {
			e.printStackTrace();
			generateErrorDTO(response, e.getMessage(), request.getLocale());
			return;
		}
		ProtocolUtil.sendResponse(response, columnValueList);
	}
}
