/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * OperatorLinkUIHelper.js 
 * Author Gary
 * Jul 12, 2012
 */
define(["alpine/flow/OperatorLinkManager",
        "alpine/flow/OperatorManagementManager",
        "alpine/flow/WorkFlowPainter",
        "alpine/system/PermissionUtil",
        "alpine/flow/WorkFlowRunnerUIHelper",
        "alpine/flow/WorkFlowEditToolbarHelper"
        ], function(linkManagement, operatorHandler, painter, permissionUtil, runnerUIHelper, editToolbarHelper){
	
	/**
	 * A Map of publisher and its subscribers
	 * key = publisher's uid
	 * value = Array of publisher's subscribers.
	 */
	var currentSubscriberMap = [];
	
	/**
	 * Array, the item  = {
	 * 		sourceId, targetId
	 * }
	 */
	var selectedLink = null;
	
	/**
	 * make Operator ready to connect
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * 		onConnectComplete(connectionInfo)
	 * 			connectionInfo = {
	 *				sourceOperatorUid,
	 *				targetOperatorUid,
	 *				operatorInfoList
	 * 			}
	 * 		onConnectFail function
	 * }
	 */
	function _activeOperator(args){
		painter.setupPublish({
			paneId: args.paneId,
			operatorUid: args.operatorUid,
            className:args.className,
			onStartConnect: function(event){
				_displaySubscribers({
					paneId: args.paneId,
					operatorUid: args.operatorUid
				});
				//create activity arrow
				painter.paintActivityLink({
					paneId: args.paneId,
					link: {
						sourceId: args.operatorUid,
						endPointX: event.clientX,
						endPointY: event.clientY
					}
				});
				// reset selected links
				_resetLink(args.paneId, selectedLink);
				// remove ears before dragging
//				painter.discardConnectState({
//					paneId: args.paneId,
//					operatorUid: args.operatorUid
//				});
			},
			onEndConnect: function(sourceId, targetId){
				_inactiveOperator({
					paneId: args.paneId,
					operatorUid: sourceId
				});
				if(!targetId){
					args.onConnectFail();
					return;
				}
				_connectOperator(args.paneId, args.operatorUid, targetId, args.onConnectComplete);
			}
		});
	}

	/**
	 * make Operator and its subscribers to ordinary
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * }
	 */
	function _inactiveOperator(args){
		var subscribers = currentSubscriberMap[args.operatorUid];
		painter.discardConnectState(args);
		if(!subscribers){
			return;
		}
		_resetOperatorIcon(args.paneId, args.operatorUid);
	}
	
	function _resetOperatorIcon(paneId, operatorUid){
		operatorHandler.forEachOperatorInfo(function(operatorPrimaryInfo){
			painter.resetOperatorIcon({
				paneId: paneId,
				operatorUid: operatorPrimaryInfo.uid,
				className: operatorPrimaryInfo.classname
			});
		});
		delete currentSubscriberMap[operatorUid];
	}
	
	/**
	 * appear all subscriber's ears
	 * 
	 * args = {
	 * 		paneId
	 * 		flowInfo: current flow Info
	 * 		sourceId,	--optional
	 * 		operatorUid,
	 * 		isConnectReverse: true is reconnect source, default is false 
	 * }
	 */
	function _displaySubscribers(args){
		var isConnectReverse = args.isConnectReverse || false;
		if(currentSubscriberMap[args.operatorUid]){
			return;// avoid multiple click on publisher
		}
		var callback = function(subscribeUidArray){
			_buildSubscriber(args.paneId, subscribeUidArray, args.operatorUid);
			currentSubscriberMap[args.operatorUid] = subscribeUidArray;
		};
		var errCallback = function(msg){
			console.error(msg);
		};
		if(isConnectReverse){
			linkManagement.getAvailablePublishers({
				operatorUid: args.operatorUid,
				originalSourceId: args.sourceId,
				callback: callback,
				errCallback: errCallback
			});
		}else{
			linkManagement.getAvailableSubscribes({
				operatorUid: args.operatorUid,
				callback: callback,
				errCallback: errCallback
			});
		}
	}
	
	function _buildSubscriber(paneId, subscribeUidArray, sourceOperatorUid){
		for(var i = 0;i < subscribeUidArray.length;i++){
			painter.setupSubscribe({
				paneId: paneId,
				operatorUid: subscribeUidArray[i],
				className: operatorHandler.getOperatorPrimaryInfo(subscribeUidArray[i]).classname
			});
		}
		
		//fade out operator which cannot be connect
		operatorHandler.forEachOperatorInfo(function(operatorPrimaryInfo){
			if(dojo.indexOf(subscribeUidArray, operatorPrimaryInfo.uid) == -1 && sourceOperatorUid != operatorPrimaryInfo.uid){
				painter.setupUnableConnectOperator({
					paneId: paneId,
					operatorUid: operatorPrimaryInfo.uid,
					className: operatorPrimaryInfo.classname
				});
			}
		});
	}
	
	function _connectOperator(paneId, sourceOperatorUid, targetOperatorUid, onConnectComplete){
		linkManagement.connectOperators({
			sourceId: sourceOperatorUid,
			targetId: targetOperatorUid,
			callback: function(operatorInfoSet){
				var sourceOperator = operatorHandler.getOperatorPrimaryInfo(sourceOperatorUid);
				var targetOperator = operatorHandler.getOperatorPrimaryInfo(targetOperatorUid);
				_fillLinkInfo(paneId, {
					sourceId: sourceOperatorUid,
			  		targetId: targetOperatorUid,
			  		startPoint: {
			  			x: sourceOperator.x,
			  			y: sourceOperator.y
			  		},
			  		endPoint: {
			  			x: targetOperator.x,
			  			y: targetOperator.y
			  		}
				});
				onConnectComplete.call(null, {
					sourceOperatorUid: sourceOperatorUid,
					targetOperatorUid: targetOperatorUid,
					operatorInfoList: operatorInfoSet
				});
			}
		});
		
	}
	
	/**
	 * invoked when loaded flow information, to build link and set it up.
	 * linkInfo = {
	 * 		sourceId,
	 * 		targetId,
	 * 		startPoint: {
	 * 			x,y
	 * 		},
	 * 		endPoint: {
	 * 			x,y
	 * 		}
	 * }
	 */
	function _fillLinkInfo(paneId, linkInfo){
		linkManagement.pushLinkInfo(linkInfo);
		painter.paintLink({
    		paneId: paneId,
    		link: linkInfo,
    		onSelect: _selectLink
    	});
	}
	
	function _reconnectCompleteHandler(operatorInfoSet, paneId, newSourceId, newTargetId){
		var sourceOperator = operatorHandler.getOperatorPrimaryInfo(newSourceId);
		var targetOperator = operatorHandler.getOperatorPrimaryInfo(newTargetId);
		_fillLinkInfo(paneId, {
			sourceId: newSourceId,
	  		targetId: newTargetId,
	  		startPoint: {
	  			x: sourceOperator.x,
	  			y: sourceOperator.y
	  		},
	  		endPoint: {
	  			x: targetOperator.x,
	  			y: targetOperator.y
	  		}
		});
		operatorHandler.updateOperatorPrimaryInfo(operatorInfoSet);
		alpine.flow.OperatorManagementUIHelper.validateOperators(operatorInfoSet);
		alpine.flow.WorkFlowUIHelper.setDirty(true);
		selectedLink = null;
		editToolbarHelper.resetLinkToolbar();
	}
	
	function _deleteLinkHandler(paneId, sourceId, targetId){
		linkManagement.deleteLink({
			sourceId: sourceId,
			targetId: targetId,
			callback: function(operatorInfoSet){
				painter.removeLink({
					paneId: paneId,
					judgementAbleToRemove: function(sid, tid){
						return sid == sourceId && tid == targetId;
					}
				});
				operatorHandler.updateOperatorPrimaryInfo(operatorInfoSet);
				alpine.flow.OperatorManagementUIHelper.validateOperators(operatorInfoSet);
				alpine.flow.WorkFlowUIHelper.setDirty(true);
			}
		});
	}
	
	function _batchDeleteLinkHandler(paneId, linkArray){
		if(!linkArray || linkArray.length == 0){
			return;
		}
		linkManagement.batchDeleteLinks({
			deleteLinks: linkArray,
			callback: function(operatorInfoSet){
				for(var i = 0;i < linkArray.length;i++){
					painter.removeLink({
						paneId: paneId,
						judgementAbleToRemove: function(sid, tid){
							return sid == linkArray[i].sourceId && tid == linkArray[i].targetId;
						}
					});
				}
				operatorHandler.updateOperatorPrimaryInfo(operatorInfoSet);
				alpine.flow.OperatorManagementUIHelper.validateOperators(operatorInfoSet);
				alpine.flow.WorkFlowUIHelper.setDirty(true);
			}
		});
	}
	

	/**
	 * build link status select.
	 * 
	 * paneId,
	 * sourceId,
	 * targetId
	 */
	function _selectLink(args){
		if((selectedLink && selectedLink.sourceId == args.sourceId && selectedLink.targetid == args.targetId) 
				|| runnerUIHelper.isFlowRunning()
				|| !permissionUtil.checkPermission("CONNECT_EDIT")){
			return;
		}
		//unselect all operators.
		alpine.flow.OperatorManagementUIHelper.resetSelectedOperators();
		
		_resetLink(args.paneId, selectedLink);
		selectedLink = {
			sourceId: args.sourceId,
			targetId: args.targetId
		};
		painter.selectLink(args.paneId, args.sourceId, args.targetId);
		editToolbarHelper.setupLinkToolbar();
		
		painter.attachControlPoints({
			paneId: args.paneId,
			sourceId: args.sourceId,
			targetId: args.targetId,
			onDelete: function(){
				_deleteLinkHandler(args.paneId, args.sourceId, args.targetId);
				selectedLink = null;
				editToolbarHelper.resetLinkToolbar();
			},
			onStartSourceMove: function(currentOperatorId, linkInfo, event){
				_displaySubscribers({
					paneId: args.paneId,
					sourceId: linkInfo.sourceId,
					operatorUid: currentOperatorId,
					isConnectReverse: true
				});
				painter.makeLinkMoveable({
					paneId: args.paneId,
					isReverse: true,
					sourceId: linkInfo.sourceId,
					targetId: linkInfo.targetId,
					latestPoint: {
						x: event.clientX,
						y: event.clientY
					}
				});
    		},
			onStartTargetMove: function(currentOperatorId, linkInfo, event){
				_displaySubscribers({
					paneId: args.paneId,
					operatorUid: currentOperatorId,
					isConnectReverse: false
				});
				painter.makeLinkMoveable({
					paneId: args.paneId,
					isReverse: false,
					sourceId: linkInfo.sourceId,
					targetId: linkInfo.targetId,
					latestPoint: {
						x: event.clientX,
						y: event.clientY
					}
				});
    		},
    		onEndSourceMove: function(currentOperatorId, targetId, originalSourceId, originalTargetId){
    			_resetOperatorIcon(args.paneId, currentOperatorId);
    			if(targetId == null){
    				var sourceOperator = operatorHandler.getOperatorPrimaryInfo(originalSourceId);
    				var targetOperator = operatorHandler.getOperatorPrimaryInfo(originalTargetId);
    				_fillLinkInfo(args.paneId, {
    					sourceId: originalSourceId,
    			  		targetId: originalTargetId,
    			  		startPoint: {
    			  			x: sourceOperator.x,
    			  			y: sourceOperator.y
    			  		},
    			  		endPoint: {
    			  			x: targetOperator.x,
    			  			y: targetOperator.y
    			  		}
    				});
    				return;
    			}
    			linkManagement.reconnectOperators({
    				originalSourceId: originalSourceId,
    				originalTargetId: originalTargetId,
    				sourceId: targetId,
    				targetId: currentOperatorId,
    				callback: function(operatorInfoSet){
    					_reconnectCompleteHandler(operatorInfoSet, args.paneId, targetId, currentOperatorId);
    				}
    			});
    		},
    		onEndTargetMove: function(currentOperatorId, targetId, originalSourceId, originalTargetId){
    			_resetOperatorIcon(args.paneId, currentOperatorId);
    			if(targetId == null){
    				var sourceOperator = operatorHandler.getOperatorPrimaryInfo(originalSourceId);
    				var targetOperator = operatorHandler.getOperatorPrimaryInfo(originalTargetId);
    				_fillLinkInfo(args.paneId, {
    					sourceId: originalSourceId,
    			  		targetId: originalTargetId,
    			  		startPoint: {
    			  			x: sourceOperator.x,
    			  			y: sourceOperator.y
    			  		},
    			  		endPoint: {
    			  			x: targetOperator.x,
    			  			y: targetOperator.y
    			  		}
    				});
    				return;
    			}
    			linkManagement.reconnectOperators({
    				originalSourceId: originalSourceId,
    				originalTargetId: originalTargetId,
    				sourceId: currentOperatorId,
    				targetId: targetId,
    				callback: function(operatorInfoSet){
    					_reconnectCompleteHandler(operatorInfoSet, args.paneId, currentOperatorId, targetId);
    				}
    			});
    		}
		});
		alpine.flow.WorkFlowUIHelper.bindDeleteKeyBoardEvent(true);
	}
	
	function _deleteSelectedLink(paneId){
		if(!selectedLink){
			return;
		}
		_deleteLinkHandler(paneId, selectedLink.sourceId, selectedLink.targetId);
		selectedLink = null;
		editToolbarHelper.resetLinkToolbar();
	}
	
	/**
	 * release link resources and reset it all status to normal.
	 * link = {
	 * 		sourceId,
	 * 		targetId
	 * }
	 */
	function _resetLink(paneId, link){
		if(link){
			painter.resetLink(paneId, link.sourceId, link.targetId);
		}
	}
	
	function _getSelectedLink(){
		return selectedLink;
	}
	
	function _resetSelectedLink(paneId){
		_resetLink(paneId, selectedLink);
		selectedLink = null;
		editToolbarHelper.resetLinkToolbar();
	}
	
	function _release(){
		selectedLink = null;
	}
	
	return {
		activeOperator: _activeOperator,
		inactiveOperator: _inactiveOperator,
		fillLinkInfo: _fillLinkInfo,
		batchDeleteLinkHandler: _batchDeleteLinkHandler,
		deleteSelectedLink: _deleteSelectedLink,
		getSelectedLink: _getSelectedLink,
		resetSelectedLink: _resetSelectedLink,
		release: _release
	};
});