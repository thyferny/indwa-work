/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowRunnerManager.js 
 * Author Gary
 * Nov 23, 2012
 */
define(["alpine/flow/WorkFlowManager"], function(workFlowManager){
	var constants = {
		REQUEST_URL: baseURL + "/main/flowRunner.do"
	};
	
	function _run(runUUID, loginName, callback){
		var url = constants.REQUEST_URL + "?method=runFlow" + "&uuid=" + runUUID + "&user=" + loginName;
		ds.post(url, workFlowManager.getEditingFlow(), callback, null);
	}
	
	function _stepRun(stepRunUUID, loginName, operatorUUID, callback, errCallback){
		var url = constants.REQUEST_URL + "?method=stepRunFlow"
			        + "&uuid=" + stepRunUUID
			        + "&user=" + loginName
			        + "&operatorUUID=" + operatorUUID;
	    ds.post(url, workFlowManager.getEditingFlow(), callback, errCallback);
	}
	
	function _stopRunning(runUUID, loginName, callback){
		var url = constants.REQUEST_URL + "?method=stopFlow" + "&uuid=" + runUUID + "&user=" + loginName;
		ds.post(url, workFlowManager.getEditingFlow(), callback);
	}
	
	function _clearStepRun(stepRunUUID, operatorName, paneID){
		var url = constants.REQUEST_URL + "?method=clearStepRunResult&runUUid=" + stepRunUUID + "&operatorName=" + operatorName;
		ds.get(url, null, null, false, paneID);
	}
	
	return {
		run: _run,
		stepRun: _stepRun,
		stopRunning: _stopRunning,
		clearStepRun: _clearStepRun
	};
});