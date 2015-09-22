package com.alpine.miner.impls.controller;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.license.validator.illuminator.LicenseInfomation;
import com.alpine.miner.impls.license.LicenseManager;
import com.alpine.miner.impls.systemupdate.BuildInformation;
import com.alpine.miner.impls.systemupdate.SystemUpdateService;
import com.alpine.miner.impls.taskmanager.TaskKeyInfo;
import com.alpine.miner.impls.taskmanager.TaskManagerStore;
import com.alpine.miner.impls.web.resource.UpdateVersionInfo;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import com.alpine.miner.workflow.runner.FlowRunningHelper;

/**
 * ClassName: SystemUpdateControler
 * <p/>
 * Data: 12-12-7
 * <p/>
 * Author: Will
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
@Controller
@RequestMapping("/main/systemUpdate.do")
public class SystemUpdateControler extends AbstractControler {

    /**
	 * @throws Exception
	 */
	public SystemUpdateControler() throws Exception {
		super();
	}

	@RequestMapping(params="method=getUpdateFileInfos", method= RequestMethod.GET)
    public void getUpdateFileInfos(HttpServletRequest request, HttpServletResponse response) throws IOException {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, request.getLocale());
    	UpdateVersionInfo info = null;
        try {
           BuildInformation information = SystemUpdateService.getInstance().readBuildInformation();
           if(information!=null){
               info = new UpdateVersionInfo(information.getApplicationName(),
            		   information.getVersion(),
            		   dateFormat.format(information.getReleaseDate()),
            		   information.getDescription());
           }
        } catch (Exception e) {
        	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
        	return;
        }

        ProtocolUtil.sendResponse(response, info);
    }

    @RequestMapping(params="method=getCurrentRunFlowInfo", method= RequestMethod.GET)
    public void getCurrentRunFlowInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String,Map<String, WebWorkFlowRunner>> runnerMap = FlowRunningHelper.RUNNER_MAP;
//        HashMap<String,WebWorkFlowStepRunner> stepRunnerMap = FlowController.stepRunnerMap;
        Map<String,String> stepRunnerMap = FlowRunningHelper.STEP_RUNNER_MAP_FOR_SYSTEM_UPDATE;
        Map<String,ArrayList<String>> currentRunInfo = new HashMap<String,ArrayList<String>>();
        List<TaskKeyInfo> runningScheduleTasks = TaskManagerStore.SCHEDULER.getInstance().getRunningTaskKeys();
        if (runnerMap.size() > 0) {
            Set<Map.Entry<String, Map<String, WebWorkFlowRunner>>> runnerUsers = runnerMap.entrySet();
            Iterator<Map.Entry<String, Map<String, WebWorkFlowRunner>>> userItor = runnerUsers.iterator();
            while (userItor.hasNext()) {
                Map.Entry<String, Map<String, WebWorkFlowRunner>> userEntry = userItor.next();
                String userName = userEntry.getKey();
                Map<String, WebWorkFlowRunner> runFlowInfos = userEntry.getValue();

                if (currentRunInfo.get(userName) == null) {
                    currentRunInfo.put(userName, new ArrayList<String>());
                }
                Set<Map.Entry<String, WebWorkFlowRunner>> runflowSet = runFlowInfos.entrySet();
                Iterator<Map.Entry<String, WebWorkFlowRunner>> runflowItor = runflowSet.iterator();
                while (runflowItor.hasNext()) {
                    WebWorkFlowRunner runner = runflowItor.next().getValue();
//                    currentRunInfo.get(userName).add(runner.getListener().getFilePath());
                    currentRunInfo.get(userName).add(runner.getFlowName());
                }
            }
        }
        if(stepRunnerMap.size()>0){
            Set<Map.Entry<String,String>> stepRunnerSet = stepRunnerMap.entrySet();
            Iterator<Map.Entry<String,String>> stepRunItor = stepRunnerSet.iterator();
            while (stepRunItor.hasNext()){
                Map.Entry<String,String> stepEntry = stepRunItor.next();
                String userName = stepEntry.getKey();
                String stepRunnFlowName = stepEntry.getValue();
                if (currentRunInfo.get(userName) == null) {
                    currentRunInfo.put(userName, new ArrayList<String>());
                }
                currentRunInfo.get(userName).add(stepRunnFlowName);
            }
        }
        for(TaskKeyInfo taskKey : runningScheduleTasks){
        	ArrayList<String> taskNames = currentRunInfo.get(taskKey.getUserName());
        	if(taskNames == null){
        		taskNames = new ArrayList<String>();
        		currentRunInfo.put(taskKey.getUserName(), taskNames);
        	}
        	taskNames.add(taskKey.getTaskName() + "(schedule)");
        }
        ProtocolUtil.sendResponse(response, currentRunInfo);
    }

    @RequestMapping(params="method=execuSystemUpdate", method= RequestMethod.POST)
    public void execuSystemUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //IUpdateService[] infos = {new UpdateVersionInfo("aaa","1.1","2012-12-01","aaaaaa"), new UpdateVersionInfo("bbb","1.2","2012-12-02","bbbbb"), new UpdateVersionInfo("ccc","1.3","2012-12-03","ccccc"), new UpdateVersionInfo("ddd","1.4","2012-12-01","ddddd")};
        UpdateVersionInfo updateInfo = ProtocolUtil.getRequest(request, UpdateVersionInfo.class);
        String version = updateInfo.getVersion();
        String contextName = request.getContextPath().substring(1);
        try {
            String deployPath = new File(this.getClass().getResource("/").toURI().getPath()).getParentFile().getParentFile().getParent();
            SystemUpdateService.getInstance().deploy(version,deployPath, contextName);
            ProtocolUtil.sendResponse(response, "{info:'success'}");
        } catch (Exception e) {
        	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
        }
    }

    @RequestMapping(params="method=haveNewUpdateVersion", method= RequestMethod.GET)
    public void haveNewUpdateVersion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //IUpdateService[] infos = {new UpdateVersionInfo("aaa","1.1","2012-12-01","aaaaaa"), new UpdateVersionInfo("bbb","1.2","2012-12-02","bbbbb"), new UpdateVersionInfo("ccc","1.3","2012-12-03","ccccc"), new UpdateVersionInfo("ddd","1.4","2012-12-01","ddddd")};

        BuildInformation information = null;
        try {
            LicenseInfomation licenseInfo = LicenseManager.getLicenseInfo();
            information = SystemUpdateService.getInstance().readBuildInformation();
            if(null != information.getVersion() && !licenseInfo.getProductID().equals(information.getVersion())){
                ProtocolUtil.sendResponse(response,"{hasNewVersion:true}");
            }else{
                ProtocolUtil.sendResponse(response,"{hasNewVersion:false}");
            }
        } catch (Exception e) {
            ProtocolUtil.sendResponse(response,"{hasNewVersion:false}");
        }

    }

    @RequestMapping(params="method=checkScriptVersion", method= RequestMethod.GET)
    public void checkScriptVersion(String scriptVersion, HttpServletRequest request, HttpServletResponse response) throws IOException{
    	try {
			LicenseInfomation licenseInfo = LicenseManager.getLicenseInfo();
			String latestRelVerion = licenseInfo.getProductID();
			boolean isLatestRelVersion = latestRelVerion.trim().equals(scriptVersion.trim());
			ProtocolUtil.sendResponse(response, isLatestRelVersion);
		} catch (Exception e) {
        	ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
		}
    }

}
