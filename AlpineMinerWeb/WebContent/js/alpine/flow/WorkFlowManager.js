/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowManager.js 
 * Author Gary
 * Jul 20, 2012
 */
define(["alpine/flow/OperatorManagementManager"], function(operatorHandler){
	
	var constants = {
		REQUEST_URL: baseURL + "/main/flow.do"
	};
	/**
	 * stored editing workflow information
	 * reflect to FlowInfo class
	 * editingFlow = {
	 * 		id,
	 * 		version,
	 * 		type,
	 * 		categories--Array,
	 * 		groupName
	 * 		createUser
	 * 		createTime--time millise
	 * 		modifiedUser
	 * 		modifiedTime
	 * 		comments
	 * 
	 * 		xmlString
	 * 		tmpPath
	 * }
	 */
	var editingFlow = null;
	
	var isDirty = false;
	
	function _storeEditingFlow(flowInfo){
		editingFlow = toolkit.getValue(flowInfo);
	}
	
	/**
	 * check whether is editing a flow 
	 * flowInfo		-- optional, if passed will check it is editing flow.
	 * 							 else only check is any flow editing. 
	 */
	function _isEditing(flowInfo){
		var isEditing = editingFlow != null;
		if(isEditing && flowInfo){
			isEditing &= (toolkit.getValue(flowInfo.categories) == toolkit.getValue(editingFlow.categories));
			isEditing &= (flowInfo.id == editingFlow.id);
		}
		return isEditing;
	}
	
	function _getEditingFlow(){
		return editingFlow;
	}
	
	function _setDirty(/*boolean*/dirty){
		isDirty = dirty;
	}
	
	function _isDirty(){
		return isDirty;
	}
	
	function _release(){
		editingFlow = null;
		isDirty = false;
	}
	
	/**
	 * save flow info
	 * args = {
	 * 		comments,
	 * 		callback,
	 * 		errCallback
	 * }
	 */
	function _saveEditingFlow(args){
        console.log("save editing flow");
		var url = constants.REQUEST_URL + "?method=completeUpdate" + "&user=" + alpine.USER;
		error_msg = "";
		var flowInfo = _getEditingFlow();
		var operatorInfoList = [];
		flowInfo.comments = args.comments;
		
		operatorHandler.forEachOperatorInfo(function(operatorInfo){
			operatorInfoList.push(operatorInfo);
		});
		
		ds.post(url, {
			flowInfo: flowInfo,
			result: operatorInfoList
		}, function(data){
			args.callback.call(null, data);
		}, function(){
			if(args.errCallback){
				args.errCallback.call(null, arguments);
			}
		}, false, args.callbackpanelid);
	}
	
	function _cancelEditingFlow(callback, errCallback, callbackpanelid){
		var url = constants.REQUEST_URL + "?method=cancelUpdate" + "&user=" + alpine.USER;
	    ds.post(url, _getEditingFlow(), callback, errCallback, false, callbackpanelid);
	}
	
	function _loadFlowData(flowInfo, callback, errCallback, callbackpanelid){
		var url = constants.REQUEST_URL + "?method=openWorkFlow" + "&user=" + alpine.USER;
		ds.post(url, flowInfo, callback, errCallback, false, callbackpanelid);
	}

    function _renameFlow(newName, flowInfo, callback, errorCallback, callbackpanelid){
        var url = constants.REQUEST_URL + "?method=renameFlow" + "&newFlowName=" + newName;
        ds.post(url, flowInfo, function(obj) {
            if (obj.error_code&&obj.error_code!=0){
                if (obj.error_code == 3) {
                    popupComponent.alert(alpine.nls.duplicateflow_alert_newflowisexist, function(){
                       // progressBar.closeLoadingBar();
                    });
                    return;
                } else if (obj.error_code == 4) {
                    popupComponent.alert(alpine.nls.duplicateflow_alert_copyerror, function(){
                       // progressBar.closeLoadingBar();
                    });
                    return;
                }else{
                    popupComponent.alert(obj.message, function(){
                        //progressBar.closeLoadingBar();
                    });
                    return;
                }
            }else {
               if (callback) callback.call(null,obj);
            }
        }, errorCallback, callbackpanelid);
    }

	
	return {
		storeEditingFlow: _storeEditingFlow,
		getEditingFlow: _getEditingFlow,
		loadFlowData: _loadFlowData,
		saveEditingFlow: _saveEditingFlow,
		cancelEditingFlow: _cancelEditingFlow,
        renameFlow: _renameFlow,
		setDirty: _setDirty,
		isEditing: _isEditing,
		isDirty: _isDirty,
		release: _release
	};
});