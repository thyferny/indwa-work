/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowUIHelper.js 
 * Author Gary
 * Jul 20, 2012
 */
define(["alpine/flow/WorkFlowManager",
        "alpine/flow/RecentlyHistoryFlowManager",
        "alpine/flow/FlowCategoryUIHelper",
        "alpine/flow/WorkFlowPainter",
        "alpine/flow/OperatorManagementUIHelper",
        "alpine/flow/OperatorLinkUIHelper",
        "alpine/flow/WorkFlowEditToolbarHelper",
        "alpine/spinner"], function(flowHandler, flowHistoryHandler, flowCategoryUIHelper, painter, operatorManagementUIHelper, operatorLinkUIHelper, editToolbarHelper, spinner){
	
	var constants = {
		CANVAS: "FlowDisplayPanelPersonal",
        WORKBENCH_PANEL: "personalFlowTree",
		CURRENT_FLOW_LABEL: "current_flow_label",//"alpine_flow_editing_label",
		SAVE_BUTTON: "save_flow_button",//"alpine_flow_editing_save_button",
        REVERT_BUTTON:"revert_flow_button",//"alpine_flow_revert_button",
		CLOSE_BUTTON: "cancel_flow_button",//"alpine_flow_editing_close_button",
        PASTE_OPERATOR_BTN: "alpine_flow_operator_paste_btn",
		DIRTY_SIGNATURE: "*",
		SAVE_FORM: {
			DIALOG: "alpine_flow_editing_saveform_dialog",
			COMMENTS: "alpine_flow_editing_saveform_comments",
			SUBMIT_BUTTON: "alpine_flow_editing_saveform_submit"
		}
	};
	
	var eventsTracker = [];
	
	var saveHandler = null;
	
	var deleteOperConnEvent = null;
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.SAVE_FORM.SUBMIT_BUTTON), "onClick", function(){
			_saveEditingFlow();
		});
		
		dojo.connect(dijit.byId(constants.CLOSE_BUTTON), "onClick", function(){
			_closeWorkFlow();
		});
		
		dojo.connect(dijit.byId(constants.SAVE_BUTTON), "onClick", function(){
			_saveWorkFlow();
		});
		dojo.connect(dijit.byId(constants.REVERT_BUTTON), "onClick", function(){
            _revertWorkFlow();
		});
		_resetButtons(true);
		
	});
	
	function _openWorkFlow(flowInfo){
		if(flowHandler.isEditing(flowInfo)){
			return;
		}
		flowHistoryHandler.pushFlow2History(flowInfo);
		
		if(flowHandler.isDirty()){
			popupComponent.saveConfirm(alpine.nls.update_not_saved, {
                handle: function(){
                	_saveWorkFlow(function(){
                		_openWorkFlowHandler(flowInfo);
                	});
                }
            },{
                handle: function(){
                	_closeWorkFlowHandler();
                    _openWorkFlowHandler(flowInfo);
                }
            });
		}else{
			_openWorkFlowHandler(flowInfo);
		}
	}
	
	function _closeWorkFlow(){
		if(!flowHandler.isEditing()){
			return;
		}
		if(flowHandler.isDirty()){
			popupComponent.saveConfirm(alpine.nls.update_not_saved, {
	            handle: function(){
	            	flowCategoryUIHelper.removeWorkFlowEditingTrail();
	            	_saveWorkFlow(_closeWorkFlowHandler);
	            }
	        },{
	            handle: function(){
	            	flowCategoryUIHelper.removeWorkFlowEditingTrail();
	        		_closeWorkFlowHandler();
	            }
	        });
		}else{
        	flowCategoryUIHelper.removeWorkFlowEditingTrail();
			_closeWorkFlowHandler();
		}
	}
	
	function _closeWorkFlowHandler(){
		flowHandler.cancelEditingFlow(function(){
			_release();
		}, constants.CANVAS);
	}
	
	function _openWorkFlowHandler(flowInfo){
		flowHandler.loadFlowData(flowInfo, function(flowDTO){
			_openWorkFlowAsWorkflowDTO(flowInfo, flowDTO);
		}, null, constants.CANVAS);
	}
	
	function _openWorkFlowAsWorkflowDTO(flowInfo, flowDTO){
		_release();
		if (flowDTO.error_code && flowDTO.error_code != 0) {
            var msg =  flowDTO.message || alpine.nls.flow_not_found;
            popupComponent.alert(msg);
            return;
	    }
		flowHandler.storeEditingFlow(flowInfo);
		_setEditingFlowLabel(flowCategoryUIHelper.buildDisplayFlowPath(flowInfo));
        _toggleCanvasBackground(true);
		_buildWorkflow(constants.CANVAS, {
			flowInfo: flowDTO.flowInfo,
			operatorSet: flowDTO.result,
			linkSet: flowDTO.links
		});
        scollToTop();
	    dojo.publish("/operatorExplorer/switchWorkflowEditor", [true]);
	    dijit.byId(constants.CLOSE_BUTTON).set("disabled", false);
        if (dijit.byId(constants.PASTE_OPERATOR_BTN)) {
            dijit.byId(constants.PASTE_OPERATOR_BTN).set("disabled", !alpine.flow.CopyManager.hasCopied());
        }
        _resetButtons(false);
		alpine.flow.OperatorMenuHelper.setupBlankMenu(constants.CANVAS);
		flowCategoryUIHelper.storeFlowPath(flowInfo);//in order to can be reopen when login.
		_bindEvents();
	//	spinner.hideSpinner(constants.CANVAS);
	}

    function scollToTop(){
      if(dojo.byId(constants.CANVAS).scrollTop!=null){
          dojo.byId(constants.CANVAS).scrollTop = 0;
      }
      if(dojo.byId(constants.CANVAS).scrollLeft!=null){
          dojo.byId(constants.CANVAS).scrollLeft = 0;
      }
    }
	
	function _setDirty(isDirty){
		flowHandler.setDirty(isDirty);
	    dijit.byId(constants.SAVE_BUTTON).set("disabled", !isDirty);
	    dijit.byId(constants.REVERT_BUTTON).set("disabled", !isDirty);
	    if(flowHandler.isEditing()){
	        var label = dojo.byId(constants.CURRENT_FLOW_LABEL).innerHTML;
	        if(label.lastIndexOf(constants.DIRTY_SIGNATURE) == label.length - 1){
	        	label = label.substr(0, label.length - 2);
	        }
	        if(isDirty == true){
	        	label += " ";
	        	label += constants.DIRTY_SIGNATURE;
	        }
	        _setEditingFlowLabel(label);
	    }
	}
	
	function _setEditingFlowLabel(label){
		dojo.html.set(dojo.byId(constants.CURRENT_FLOW_LABEL), label);
	}
	
	function _saveWorkFlow(callback){
		_fillSaveForm();
		saveHandler = callback;
	}

    function _revertWorkFlow(){
        var flowInfo = flowHandler.getEditingFlow();
        popupComponent.confirm(alpine.nls.revert_not_revert,"revert",{
            handle: function(){

                //close flow
                flowCategoryUIHelper.removeWorkFlowEditingTrail();
                _closeWorkFlowHandler();
                //reopen flow
                _openWorkFlowHandler(flowInfo)

            }
        },{
            handle: function(){
               //do nothing
            }
        });
    }
	
	function _fillSaveForm(){
		var editingFlow = flowHandler.getEditingFlow();
		dojo.byId(constants.SAVE_FORM.COMMENTS).value = editingFlow.comments;
        dijit.byId(constants.SAVE_FORM.DIALOG).titleBar.style.display='none';
        dijit.byId(constants.SAVE_FORM.DIALOG).show();
	    if (dojo.isIE){
	        dojo.byId(constants.SAVE_FORM.COMMENTS).rows=4;
	        dojo.byId(constants.SAVE_FORM.COMMENTS).cols=38;
	    }
	}
	
	function _saveEditingFlow(){
	    dijit.byId(constants.SAVE_FORM.DIALOG).hide();
	    _setDirty(false);
		flowHandler.saveEditingFlow({
            callbackpanelid: constants.CANVAS,
			comments: dojo.byId(constants.SAVE_FORM.COMMENTS).value,
			callback: function(flowInfo){
//			    var str = flowCategoryUIHelper.buildFlowPath(CurrentFlow);
				flowHandler.storeEditingFlow(flowInfo);
			    _setEditingFlowLabel(flowCategoryUIHelper.buildDisplayFlowPath(flowHandler.getEditingFlow()));
			    //update personal flow tree, in order to ensure other function make sense. e.g. show flow history, open flow. Because they dependence on information of flow in the tree.
			    flowCategoryUIHelper.rebuildCategoryTree();
			    if(saveHandler){
					saveHandler.call();
					saveHandler = null;
			    }
			},
			errCallback: function(){
			}
		});
	}
	
	/**
	 * 
	 * flowDataInfo = {
	 *		flowInfo,
	 *		operatorSet,
	 *		linkSet
	 * }
	 */
	function _buildWorkflow(paneId, flowDataInfo){
		flowHandler.storeEditingFlow(flowDataInfo.flowInfo);
		var operatorConnectionMapping = {};
		for(var i = 0;i < flowDataInfo.linkSet.length;i++){
			operatorConnectionMapping[flowDataInfo.linkSet[i].sourceid] = true;
			operatorConnectionMapping[flowDataInfo.linkSet[i].targetid] = true;
		}
		
		
		for(var i = 0;i < flowDataInfo.operatorSet.length;i++){
			var isconnected = operatorConnectionMapping[flowDataInfo.operatorSet[i].uid] || false;
			operatorManagementUIHelper.fillOperatorInfo(paneId, flowDataInfo.operatorSet[i], isconnected);
		}
		operatorManagementUIHelper.validateOperators();//validate all operators, to make sure all operators display valid status in time.
		for(var i = 0;i < flowDataInfo.linkSet.length;i++){
			operatorLinkUIHelper.fillLinkInfo(paneId, {
				sourceId: flowDataInfo.linkSet[i].sourceid,
		  		targetId: flowDataInfo.linkSet[i].targetid,
		  		startPoint: {
		  			x: flowDataInfo.linkSet[i].x1,
		  			y: flowDataInfo.linkSet[i].y1
		  		},
		  		endPoint: {
		  			x: flowDataInfo.linkSet[i].x2,
		  			y: flowDataInfo.linkSet[i].y2
		  		}
			});
		}
	}
	
	function _unselectAll(){
		operatorLinkUIHelper.resetSelectedLink(constants.CANVAS);
		dojo.forEach(operatorManagementUIHelper.getSelectOperatorUidArray(), function(item){
			operatorManagementUIHelper.resetOperator(item);
		});
	}
	
	function _release(){
		painter.release(constants.CANVAS);
		operatorLinkUIHelper.release();
		operatorManagementUIHelper.release();
		editToolbarHelper.release();
		
		var svgCanvas = dojo.byId(constants.CANVAS);
	    while(svgCanvas.firstChild) {
	    	svgCanvas.removeChild(svgCanvas.firstChild);
	    }
	    _resetButtons(true);
        _setDirty(false);
        _toggleCanvasBackground(false);
        alpine.flow.WorkFlowRunnerUIHelper.clean();
        latestStepRanOperatorUid = null;
	    
	    dijit.byId(constants.CLOSE_BUTTON).set("disabled", true);
	    flowHandler.release();
	    _setEditingFlowLabel("");
		alpine.flow.OperatorMenuHelper.releaseBlankMenu();
		var event = null;
		while((event = eventsTracker.pop()) != null){
			dojo.disconnect(event);
		}
	    dojo.publish("/operatorExplorer/switchWorkflowEditor", [false]);
	}
	
	function _resetButtons(isDisable){
		dijit.byId("export_flow_button").set("disabled", isDisable);
		dijit.byId("export_exec_flow_button").set("disabled", isDisable);
		dijit.byId("btn_show_flow_history").set("disabled", isDisable);
		dijit.byId("rename_flow_action_button").set("disabled", isDisable);
		dijit.byId("delete_flow_action_button").set("disabled", isDisable);
	    dijit.byId("share_flow_button").set("disabled", isDisable);
	    dijit.byId("publish_chorus_button").set("disabled", isDisable);
	    dijit.byId("btn_tree_duplicate_flow").set("disabled", isDisable);
	    dijit.byId("flowVariable_setting_button").set("disabled", isDisable);
        dijit.byId("clear_temp_table").set("disabled", isDisable);
	    //dijit.byId("find_replace_parameter_value_btn").set("disabled", !flag);
	    dijit.byId(constants.CLOSE_BUTTON).set("disabled", isDisable);

	    if (isDisable == false) {
	    	alpine.flow.WorkFlowRunnerUIHelper.enableRunFlowButtons();
	    }
	    else {
	    	alpine.flow.WorkFlowRunnerUIHelper.disableRunFlowButtons();
	        dijit.byId("stop_flow").set("disabled", true);
	        _setDirty(false);
	    }

		dojo.disconnect(deleteOperConnEvent);
		deleteOperConnEvent = null;
	}

    function _toggleCanvasBackground(isOpeningFlow) {
        isOpeningFlow |= false;
        if (isOpeningFlow) {
            dojo.setStyle(dojo.byId(constants.CANVAS),{"background": "#FFFFFF"});
            dojo.setStyle(dojo.byId(constants.WORKBENCH_PANEL),{"background-color": "#FFFFFF"});
        } else {
            dojo.setStyle(dojo.byId(constants.CANVAS),{"background": "#E7E7E9 url(../../images/interface/noflow.png) center no-repeat"});
            dojo.setStyle(dojo.byId(constants.WORKBENCH_PANEL),{"background-color": "#E7E7E9"});
        }
    }
    
    function _bindEvents(){
    	eventsTracker.push(dojo.connect(dojo.byId("rootContainer"), "onclick", function(e){
			//unselect operator when click everywhere.
    		console.log("called unbind delete event...");
			_unselectAll();
			_bindDeleteKeyBoardEvent(false);
		}));
    	eventsTracker.push(dojo.connect(dojo.byId("alpine_operator_toolbar_pane"), "click", function(e){
	    	// To prevent event bubble up.
	    	dojo.stopEvent(e);
	    }));
    }
    
    function _bindDeleteKeyBoardEvent(isBind){
    	console.log("called  _bindDeleteKeyBoardEvent isBind-->" + isBind);
    	if(isBind == true){
    		//if deleteConnEvent exist, disconnect it first
    		if(deleteOperConnEvent){
    			dojo.disconnect(deleteOperConnEvent);
    		}
        	deleteOperConnEvent = dojo.connect(window, "onkeydown", function(evt){
    			if(dojo.keys.DELETE == evt.keyCode){
    				dojo.disconnect(deleteOperConnEvent);
    				operatorManagementUIHelper.deleteSelectedOperators();
    				operatorLinkUIHelper.deleteSelectedLink(constants.CANVAS);
    			}
    		});
    	}else if(deleteOperConnEvent){
    		dojo.disconnect(deleteOperConnEvent);
    		deleteOperConnEvent = null;
    	}
    }
	
	return{
		openWorkFlowAsWorkflowDTO: _openWorkFlowAsWorkflowDTO,
		setEditingFlowLabel: _setEditingFlowLabel,
		openWorkFlow: _openWorkFlow,
		setDirty: _setDirty,
		release: _release,
        saveWorkFlow:_saveWorkFlow,
        bindDeleteKeyBoardEvent: _bindDeleteKeyBoardEvent
	};
});