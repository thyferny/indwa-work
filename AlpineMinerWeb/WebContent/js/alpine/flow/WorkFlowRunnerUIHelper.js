/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowRunnerUIHelper.js 
 * Author Gary
 * Nov 23, 2012
 */
define(["alpine/flow/WorkFlowManager",
        "alpine/flow/OperatorManagementManager",
        "alpine/flow/WorkFlowRunnerManager",
        "alpine/flow/WorkFlowEditToolbarHelper"], function(workflowManager, operatorManager, runnerManager, editToolbarHelper){
	var constants = {
		RUN_FLOW_BUTTON: "run_flow",
		STEP_RUN_BUTTON: "step_run_flow",
		STOP_RUN_BUTTON: "stop_flow"
	};
	
	var isRunning = false;
	var runUUID = null;
	var runningOperatorName = null;
	var stepRanOperatorUidSet = [];//key = operator's uid
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.STEP_RUN_BUTTON), "onClick", function(){
			var selectedOperator= alpine.flow.OperatorManagementUIHelper.getSelectedOperator();
			_runToOperator(selectedOperator);
		});
		dojo.connect(dijit.byId(constants.RUN_FLOW_BUTTON), "onClick", _runFlow);
		dojo.connect(dijit.byId(constants.STOP_RUN_BUTTON), "onClick", _stopRunning);
	});
	
	function _runFlow(){
		if(!operatorManager.hasOperator()){
			popupComponent.alert(alpine.nls.running_error_tip_no_operator);
			return;
		}
		var isAllValid = alpine.flow.OperatorManagementUIHelper.validateOperators();
		if (isAllValid == false){
			popupComponent.alert(alpine.nls.invalid_flow);
			return;
		}
		_disableRunFlowButtons();
		if (!workflowManager.isEditing()) {
			return;
		}
		runUUID = Math.random();
		var callbackFn = null;
		if(dojo.isSafari){
			dojo.publish('/opener/callOpen');
		}else{
			callbackFn = _runCallback;
		}
		runnerManager.run(runUUID, alpine.userInfo.login, callbackFn);
	}
	
	function _runToOperator(operator) {
	    if (!workflowManager.isEditing() || operator == null) {
	        return;
	    }
	    var parentOperatorList = operatorManager.getAllParentOperators(operator.uid, true);

	    if (parentOperatorList.length > 0
	    		&& !alpine.flow.OperatorManagementUIHelper.validateOperators(parentOperatorList)){
	        popupComponent.alert(alpine.nls.invalid_flow);
	        return;
	    }
	    if(parentOperatorList.length == 1
	    		&& operator.uid == parentOperatorList[0].uid
	    		&& operator.classname != "SQLExecuteOperator"
	    		&& operator.classname != "SubFlowOperator"){
	    	popupComponent.alert(alpine.nls.step_run_single_operator_error_message);
	    	return;
	    }

	    _disableRunFlowButtons();
	    if(!runUUID){
	    	runUUID = Math.random();
	    }
	    var callbackFn = null;
	    if(dojo.isSafari){
	        dojo.publish('/opener/callOpen');
	    }else{
	        callbackFn = _runCallback;
	    }
	    _storeStepRanOperatorUids(operator.uid, true);
	    runnerManager.stepRun(runUUID, alpine.userInfo.login, operator.uid, callbackFn, function(){
	    	isRunning = false;
			alpine.flow.OperatorManagementManager.forEachOperatorInfo(function(operatorInfo){
				alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid, true);
			});
			_enableStepRunButtons();
	    });
	}
	
	dojo.subscribe('/opener/callOpen',function() {
		var url = baseURL + "/alpine/result/result.jsp?uuid=" + runUUID + "&flowName=" + workflowManager.getEditingFlow().id;
		window.open(encodeURI(url),'_blank', get_open_window_options());
	});
	
	function _runCallback(obj){
		if (obj.error_code && obj.error_code != 0) {
			if (obj.error_code == -1) {
				popupComponent.alert(alpine.nls.no_login, function(){
					window.top.location.pathname = loginURL;	
				});
				return;
			}else {
				var msg = alpine.nls.flow_not_found;
				if (obj.message) {
					msg = obj.message;
				}
				popupComponent.alert(msg);
				alpine.flow.WorkFlowUIHelper.release();
//				get_flow_tree_personal();
				return;
			}			
		}
		var url = baseURL + "/alpine/result/result.jsp?uuid=" + runUUID+"&flowName=" + workflowManager.getEditingFlow().id;
		window.open(encodeURI(url), '_blank', get_open_window_options());
	}
	
	function _stopRunning() {
		if(!runUUID) {
			return;
		}
		_afterRun();
		runnerManager.stopRunning(runUUID, alpine.userInfo.login, function(obj){
			runUUID = null;
			stepRanOperatorUidSet = [];
			if (obj && obj.error_code) {
				handle_error_result(obj);
				return;
			}
		});
	}
	
	function _afterRun(){
		operatorManager.forEachOperatorInfo(function(operatorInfo){
			alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid, true);
		});
		_enableRunFlowButtons();
	}

	function _enableRunFlowButtons(){
		isRunning = false;
		dijit.byId(constants.RUN_FLOW_BUTTON).set("disabled", false);
		dijit.byId(constants.STOP_RUN_BUTTON).set("disabled", true);
	}

	function _disableRunFlowButtons(){
		isRunning = true;
		dijit.byId(constants.RUN_FLOW_BUTTON).set("disabled", true);
		dijit.byId(constants.STEP_RUN_BUTTON).set("disabled", true);
		dijit.byId(constants.STOP_RUN_BUTTON).set("disabled", false);
		editToolbarHelper.release();
//      if (dijit.byId("open_sel_operator_prop")) 
//      	dijit.byId("open_sel_operator_prop").set("disabled", true);
//		dijit.byId(constants.EDIT_OPERATOR_BUTTON).set("disabled", true);
//		if(dijit.byId(constants.DELETE_OPERATOR_BUTTON))
//			dijit.byId(constants.DELETE_OPERATOR_BUTTON).set("disabled", true);
	}
	
	function _enableStepRunButtons() {
		dijit.byId(constants.RUN_FLOW_BUTTON).set("disabled", false);
		dijit.byId(constants.STEP_RUN_BUTTON).set("disabled", false);
		dijit.byId(constants.STOP_RUN_BUTTON).set("disabled", true);
//      if (dijit.byId("open_sel_operator_prop")) dijit.byId("open_sel_operator_prop").set("disabled", false);
//		dijit.byId(constants.EDIT_OPERATOR_BUTTON).set("disabled", false);
//		if(dijit.byId(constants.DELETE_OPERATOR_BUTTON))
//			dijit.byId(constants.DELETE_OPERATOR_BUTTON).set("disabled", false);
	}
	
	function _flashOperator(operatorName){
	    if (runningOperatorName && runningOperatorName != operatorName) {
	    	var operatorUid = operatorManager.getOperatorUidByName(runningOperatorName);
	    	alpine.flow.OperatorManagementUIHelper.focusOperator(operatorUid);
	    	runningOperatorName = operatorName;
	    }
	    var runningOperatorUid = operatorManager.getOperatorUidByName(operatorName);
	    if(runningOperatorUid){
	    	alpine.flow.OperatorManagementUIHelper.toggleOperatorFocus(runningOperatorUid);
	    }
	}
	
	//this function is used to make last ran operator being to focus, or make newly operator being to focus.
	function _switchFlashOperator(operatorName){
		if (runningOperatorName && runningOperatorName != operatorName) {
			//last operator will run here.
	    	var runningOperatorUid = alpine.flow.OperatorManagementManager.getOperatorUidByName(runningOperatorName);
	    	alpine.flow.OperatorManagementUIHelper.focusOperator(runningOperatorUid);
	        LastOperatorName = operatorname;
	    }else{
	    	//newly operator will run here.
	    	var runningOperatorUid = alpine.flow.OperatorManagementManager.getOperatorUidByName(operatorName);
	    	if(runningOperatorUid){
	        	alpine.flow.OperatorManagementUIHelper.focusOperator(runningOperatorUid);
	    	}
	    }
	}
	
	function _isFlowRunning(){
		return isRunning;
	}

	function _clearStepRunResult(operator){
		stepRanOperatorUidSet = [];
		_storeStepRanOperatorUids(operator.uid, false);
		runnerManager.clearStepRun(runUUID, operator.name, "FlowDisplayPanelPersonal");
	}
	
	function _storeStepRanOperatorUids(operatorUid, includeSelf){
		var parentOperatorList = operatorManager.getAllParentOperators(operatorUid, includeSelf);
		stepRanOperatorUidSet = [];
		for(var i = 0;i < parentOperatorList.length;i++){
			stepRanOperatorUidSet[parentOperatorList[i].uid] = true;
    	}
	}
	
	function _canBeCleanRunResult(operatorUid){
		if(stepRanOperatorUidSet == null){
			return false;
		}
		return stepRanOperatorUidSet[operatorUid] == true ? true : false;
	}
	
	function _clean(){
		isRunning = false;
		runUUID = null;
		runningOperatorName = null;
		stepRanOperatorUidSet = [];
	}
	
	function _getRunningID(){
		return runUUID;
	}
	
    return {
    	enableRunFlowButtons: _enableRunFlowButtons,
    	disableRunFlowButtons: _disableRunFlowButtons,
    	runToOperator: _runToOperator,
    	afterRun: _afterRun,
    	flashOperator: _flashOperator,
    	switchFlashOperator: _switchFlashOperator,
    	clearStepRunResult: _clearStepRunResult,
    	isFlowRunning: _isFlowRunning,
    	canBeCleanRunResult: _canBeCleanRunResult,
    	getRunningID: _getRunningID,
    	clean: _clean
    };
});