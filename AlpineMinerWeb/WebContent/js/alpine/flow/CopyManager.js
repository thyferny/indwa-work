/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * CopyManager.js 
 * Author Gary
 * Aug 3, 2012
 */
define(["alpine/flow/WorkFlowManager",
        "alpine/flow/OperatorManagementUIHelper",
        "alpine/flow/OperatorManagementManager",
        "alpine/flow/OperatorLinkUIHelper"], function(workFlowManager, operatorUIHelper, operatorManager, linkUIHelper){

	var constants = {
		REQUEST_URL: baseURL + "/main/operatorManagement.do",
		CANVAS: "FlowDisplayPanelPersonal",
        PASTE_OPERATOR_BTN: "alpine_flow_operator_paste_btn"
    };
	
	/**
	 * copyData = {
	 * 		copiedFlowInfo,
	 * 		copyOperatorSet: {
	 * 			uuid,
	 * 			name
	 * 		},
	 * 		copyConnectionSet: {
	 * 			sourceId,
	 * 			targetId
	 * 		}
	 * }
	 */
	var copyData = null;
	var pasteCounter = 1;
	var pasting = false;
	
	var hasCopied = false;
	
//	dojo.ready(function(){
//		dojo.connect(window, "onkeydown", _handlEvent);
//	});
	
//	function _handlEvent(event){
//		var copyKey;
//		if(dojo.isMac){
//			copyKey = event.metaKey;
//		}else{
//			copyKey = event.ctrlKey;
//		}
//		if(copyKey == false){
//			return;
//		}
//		if(event.keyCode == 67){//ctrl + c
//			_onCopy();
//		}else if(event.keyCode == 86){//ctrl + v
//			_paste();
//		}
//	}
	
	function _onCopy(){
		var selectedOperatorIdArray = operatorUIHelper.getSelectOperatorUidArray();
		if(selectedOperatorIdArray.length == 0){
			return;
		}
		_copy(selectedOperatorIdArray);
        hasCopied = true;
        if (dijit.byId(constants.PASTE_OPERATOR_BTN)) {
            dijit.byId(constants.PASTE_OPERATOR_BTN).set("disabled", !hasCopied);
        }
	}
	
	/**
	 * copy operators and connections
	 * operatorUUIDArray is an Array, e.g. [uuid1, uuid2, uuid3]
	 */
	function _copy(operatorUUIDArray){
		var flowInfo = workFlowManager.getEditingFlow();
		pasteCounter = 1;
		var copyOperatorSet = new Array();
		for(var i = 0;i < operatorUUIDArray.length;i++){
			var operatorInfo = operatorManager.getOperatorPrimaryInfo(operatorUUIDArray[i]);
			copyOperatorSet.push({
				uuid: operatorUUIDArray[i],
				name: operatorInfo.name
			});
		}
		copyData = {
			copiedFlowInfo: flowInfo,
			copyOperatorSet: copyOperatorSet
		};
	}
	
	function _paste(){
		if(!copyData || pasting == true){
			return;
		}
		pasting = true;
		var flowInfo = dojo.clone(workFlowManager.getEditingFlow());
		copyData.flowInfo = flowInfo;
		for(var i = 0;i < copyData.copyOperatorSet.length;i++){
			copyData.copyOperatorSet[i].newUUID =  new Date().getTime() + "_" + i;
			copyData.copyOperatorSet[i].newName = operatorManager.generateOperatorLabel(copyData.copyOperatorSet[i].name);
		}
		copyData.offset = pasteCounter * 30;
		pasteCounter++;
		var _url = constants.REQUEST_URL + "?method=copyOperators";
		ds.post(_url, copyData, _pasteHandler);
	}
	
	function _pasteHandler(data){
		var operatorList = data.operatorPrimaryInfoSet;
		var connectionList = data.connectionInfoSet;
		var operConnMapping = {};
		//first figure out which operator has been connected.
		for(var i = 0;i < connectionList.length;i++){
			operConnMapping[connectionList[i].sourceId] = true;
			operConnMapping[connectionList[i].targetId] = true;
		}
		//second draw operators icon
		for(var i = 0;i < operatorList.length;i++){
			var isConnected = operConnMapping[operatorList[i].uid] || false;
			operatorUIHelper.fillOperatorInfo(constants.CANVAS, operatorList[i], isConnected);
		}
		//finally draw link 
		for(var i = 0;i < connectionList.length;i++){
			operConnMapping[connectionList[i].sourceId] = true;
			operConnMapping[connectionList[i].targetId] = true;
			
			linkUIHelper.fillLinkInfo(constants.CANVAS, {
				sourceId: connectionList[i].sourceId,
		  		targetId: connectionList[i].targetId,
		  		startPoint: {
		  			x: connectionList[i].x1,
		  			y: connectionList[i].y1
		  		},
		  		endPoint: {
		  			x: connectionList[i].x2,
		  			y: connectionList[i].y2
		  		}
			});
		}
		alpine.flow.WorkFlowUIHelper.setDirty(true);
		pasting = false;
	}
	
	function _hasCopied(){
		return hasCopied;
	}
	
	return {
		copy: _onCopy,
		paste: _paste,
		hasCopied: _hasCopied
	};
});