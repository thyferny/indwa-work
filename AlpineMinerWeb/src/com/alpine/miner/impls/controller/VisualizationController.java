/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * VisualizationController.java
 */
package com.alpine.miner.impls.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.miner.impls.result.VisualAdapterLinearRegression;
import com.alpine.miner.impls.result.VisualAdapterLogisticRegression;
import com.alpine.miner.utils.JSONUtility;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

/**
 * @author Gary
 * Jan 21, 2013
 */
@Controller
@RequestMapping("/main/visualization/utility.do")
public class VisualizationController extends AbstractControler {

	/**
	 * @throws Exception
	 */
	public VisualizationController() throws Exception {
		super();
	}

    @RequestMapping(params = "method=getLinearRegressionGroupModelByKey", method = RequestMethod.GET)
    public void getLinearRegressionGroupModelByKey(String key,HttpServletRequest request,
                                                     HttpServletResponse response, ModelMap model) throws IOException{
        VisualizationModel visualModel = null;
        String userName = null;
        userName = getUserName(request);
        Map<String,VisualizationModelComposite> modelCompositeMap = VisualAdapterLinearRegression.linearRegressionGroupByModelMap.get(userName);
        visualModel = modelCompositeMap.get(key);
        if(null!=visualModel){
            String jsonString= JSONUtility.toJSONString(visualModel, request.getLocale());
            try {
                ProtocolUtil.sendResponse(response, jsonString);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @RequestMapping(params = "method=getLinearRegressionGroupMode", method = RequestMethod.GET)
    public void getLinearRegressionGroupMode(HttpServletRequest request,
                                               HttpServletResponse response, ModelMap model) throws IOException{
        int MAX_NUM = 500;

        String userName = null;
        userName = getUserName(request);
        List<String> jsonStrings = new ArrayList<String>();
        Map<String,VisualizationModelComposite> modelDataTableMap = VisualAdapterLinearRegression.linearRegressionGroupByModelMap.get(userName);
        Set<Entry<String,VisualizationModelComposite>> entrySet= modelDataTableMap.entrySet();
        Iterator<Entry<String,VisualizationModelComposite>> entryIterator = entrySet.iterator();
        while (entryIterator.hasNext()){
            Entry<String,VisualizationModelComposite> entry = entryIterator.next();
            VisualizationModel visualModel = entry.getValue();
            if(jsonStrings.size()>MAX_NUM){
                break;
            }
            if(null!=visualModel){
                jsonStrings.add(JSONUtility.toJSONString(visualModel, request.getLocale()));
            }
        }
        try {
            ProtocolUtil.sendResponse(response,jsonStrings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(params = "method=getLogisticRegressionGroupModelByKey", method = RequestMethod.GET)
    public void getLogisticRegressionGroupModelByKey(String key,HttpServletRequest request,
                                                     HttpServletResponse response, ModelMap model) throws IOException{
        VisualizationModel visualModel = null;
        String userName = null;
        userName = getUserName(request);
        Map<String,VisualizationModelDataTable> modelDataTableMap = VisualAdapterLogisticRegression.LogisticRegressionGroupModelMap.get(userName);
        visualModel = modelDataTableMap.get(key);
        if(null!=visualModel){
            String jsonString= JSONUtility.toJSONString(visualModel, request.getLocale());
            try {
                ProtocolUtil.sendResponse(response, jsonString);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @RequestMapping(params = "method=getLogisticRegressionGroupMode", method = RequestMethod.GET)
    public void getLogisticRegressionGroupMode(HttpServletRequest request,
                                               HttpServletResponse response, ModelMap model) throws IOException{
        int MAX_NUM = 500;
        String userName = null;
        userName = getUserName(request);
        List<String> jsonStrings = new ArrayList<String>();
        Map<String,VisualizationModelDataTable> modelDataTableMap = VisualAdapterLogisticRegression.LogisticRegressionGroupModelMap.get(userName);
        Set<Entry<String,VisualizationModelDataTable>> entrySet= modelDataTableMap.entrySet();
        Iterator<Entry<String,VisualizationModelDataTable>> entryIterator = entrySet.iterator();
        while (entryIterator.hasNext()){
            Entry<String,VisualizationModelDataTable> entry = entryIterator.next();
            VisualizationModel visualModel = entry.getValue();
            if(jsonStrings.size()>MAX_NUM){
                break;
            }
            if(null!=visualModel){
                jsonStrings.add(JSONUtility.toJSONString(visualModel, request.getLocale()));
            }
        }
        try {
            ProtocolUtil.sendResponse(response,jsonStrings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
