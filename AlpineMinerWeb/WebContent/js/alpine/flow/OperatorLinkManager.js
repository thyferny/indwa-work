/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * OperatorLinkManager.js 
 * Author Gary
 * Jul 12, 2012
 */
define(function(){
	
	var constants = {
		REQUEST_URL: baseURL + "/main/linkManagement.do"
	};

	/**
	 * 
	 * String sourceId;
	 * String targetId;
	 * startPoint: {
	 * 		x,y
	 * };
	 * endPoint: {
	 * 		x,y
	 * }
	 */
	var workflowLinkSet = [];
	
	
	/**
	 * get useable to connect operator's uuid for giving operator
	 * 
	 * args = {
	 * 		operatorUid, 
	 * 		callback(Array<operatorUid>), 
	 * 		errCallback
	 * }
	 */
	function _getAvailableSubscribes(args){
		ds.post(constants.REQUEST_URL + "?method=getAvailableSubscribers&publisherOperatorUid=" + args.operatorUid, 
				alpine.flow.WorkFlowManager.getEditingFlow(),
				args.callback, args.errCallback);
	}

	/**
	 * get useable to connect source operator's uuid for giving operator
	 * 
	 * args = {
	 * 		operatorUid, 
	 * 		originalSourceId
	 * 		callback(Array<operatorUid>), 
	 * 		errCallback
	 * }
	 */
	function _getAvailablePublishers(args){
		ds.post(constants.REQUEST_URL + "?method=getAvailablePublishers&subscriberOperatorUid=" + args.operatorUid + "&originalSourceId=" + args.originalSourceId, 
				alpine.flow.WorkFlowManager.getEditingFlow(),
				args.callback, args.errCallback);
	}
	
	/**
	 * create a connection to connect two Operators.
	 * 
	 * args = {
	 * 		sourceId,
	 * 		targetId,
	 * 		callback,
	 * 		errCallback		--optional
	 * }
	 */
	function _connectOperators(args){
		ds.post(constants.REQUEST_URL + "?method=connectOperator",{
			flowInfo: alpine.flow.WorkFlowManager.getEditingFlow(),
			sourceOperatorUid: args.sourceId,
			targetOperatorUid: args.targetId
		}, function(operatorInfoSet){
//			_deleteInvalidOperatorInfo(operatorInfoSet);
			args.callback.call(null, operatorInfoSet);
		}, args.errCallback);
	}

	/**
	 * reconnect Operators.
	 * args = {
	 * 		originalSourceId,
	 * 		originalTargetId,
	 * 		sourceId,
	 * 		targetId,
	 * 		callback,
	 * 		errCallback		--optional
	 * }
	 */
	function _reconnectOperators(args){
		ds.post(constants.REQUEST_URL + "?method=reconnectOperator",{
			flowInfo: alpine.flow.WorkFlowManager.getEditingFlow(),
			originalSourceOperatorUid: args.originalSourceId,
			originalTargetOperatorUid: args.originalTargetId,
			sourceOperatorUid: args.sourceId,
			targetOperatorUid: args.targetId
		}, function(operatorInfoSet){
//			_deleteInvalidOperatorInfo(operatorInfoSet);
			args.callback.call(null, operatorInfoSet);
		}, args.errCallback);
	}

	/**
	 * delete a connection.
	 * 
	 * args = {
	 * 		sourceId,
	 * 		targetId,
	 * 		callback,
	 * 		errCallback		--optional
	 * }
	 */
	function _deleteLink(args){
		ds.post(constants.REQUEST_URL + "?method=deleteLink",{
			flowInfo: alpine.flow.WorkFlowManager.getEditingFlow(),
			sourceOperatorUid: args.sourceId,
			targetOperatorUid: args.targetId
		}, function(operatorInfoSet){
//			_deleteInvalidOperatorInfo(operatorInfoSet);
			args.callback.call(null, operatorInfoSet);
		}, args.errCallback);
	}

	/**
	 * delete connections.
	 * 
	 * args = {
	 * 		deleteLinks	[]
	 * 		callback,
	 * 		errCallback		--optional
	 * }
	 */
	function _batchDeleteLinks(args){
		ds.post(constants.REQUEST_URL + "?method=batchDeleteLink",{
			flowInfo: alpine.flow.WorkFlowManager.getEditingFlow(),
			connectionModels: args.deleteLinks
		}, function(operatorInfoSet){
//			_deleteInvalidOperatorInfo(operatorInfoSet);
			args.callback.call(null, operatorInfoSet);
		}, args.errCallback);
	}
	
	function _deleteInvalidOperatorInfo(operatorInfoSet){
		// delete invalid position info.
		for(var i = 0;i < operatorInfoSet.length;i++){
			delete operatorInfoSet[i].x;
			delete operatorInfoSet[i].y;
		}
	}

	/**
	 * 
	 * linkInfo: {
	 * 		String sourceId;
	 * 		String targetId;
	 * 		startPoint: {
	 * 			x,y
	 * 		};
	 * 		endPoint: {
	 * 			x,y
	 * 		}
	 * }
	 */
	function pushLinkInfo(linkInfo){
		workflowLinkSet.push(linkInfo);
	}
	
	function removeLinkInfo(sourceId, targetId){
		for(var i = 0;i < workflowLinkSet.length;i++){
			var linkInfo = workflowLinkSet[i];
			if(linkInfo.sourceId == sourceId && linkInfo.targetId == targetId){
				workflowLinkSet.splice(i, 1);
				break;
			}
		}
	}
	
	function forEachLink(fn){
		for(var i = 0;i < workflowLinkSet.length;i++){
			fn.call(workflowLinkSet, workflowLinkSet[i], i);
		}
	}
	
	function release(){
		workflowLinkSet = [];
	}
	
	return {
		getAvailableSubscribes: _getAvailableSubscribes,
		getAvailablePublishers: _getAvailablePublishers,
		connectOperators: _connectOperators,
		reconnectOperators: _reconnectOperators,
		deleteLink: _deleteLink,
		pushLinkInfo: pushLinkInfo,
		forEachLink: forEachLink,
		removeLinkInfo: removeLinkInfo,
		batchDeleteLinks: _batchDeleteLinks,
		release: release
	};
});