/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * OperatorRenameUIHelper.js 
 * Author Gary
 * Jul 18, 2012
 */
define(["alpine/flow/OperatorManagementManager",
        "alpine/flow/WorkFlowPainter",
        "alpine/flow/WorkFlowManager"], function(operatorHandler, painter, workFlowHandler){
	
	var constants = {
		EDITOR: "alpine_flow_operator_labe_editor",
		TEXT_BOX: "alpine_flow_operator_labe_textbox",
		DEF_WIDTH: 100
	};
	
	var editorCache = [];
	var eventArray = [];
	var currentPaneId = null;
	var currentOperatorUid = null;
	
	/**
	 * args = {
	 * 		paneId,
	 * 		operatorUid
	 * }
	 */
	function _setupLabelEditor(args){
		currentPaneId = args.paneId;
		currentOperatorUid = args.operatorUid;
		painter.fineOperatorUnit(args.paneId, args.operatorUid, function(operatorUnit){
			_buildEditor(operatorUnit);
		});
	}
	
	function _buildEditor(operatorUnit){
		var editorInfo = _getEditor(currentPaneId);
		_setupContainer(editorInfo, operatorUnit);
		editorInfo.textInput.set("value", operatorUnit.label.shape.text);
		editorInfo.textInput.focus();

		function _setupContainer(editor, operatorUnit){
			var operatorPrimaryInfo = operatorHandler.getOperatorPrimaryInfo(currentOperatorUid);
			var centerOfLabel = operatorPrimaryInfo.x + operatorUnit.icon.shape.width / 2;
			var labelWidth = operatorUnit.label.getTextWidth()*1.3;
			var editorWidth = labelWidth < constants.DEF_WIDTH ? constants.DEF_WIDTH : labelWidth;
			dojo.style(editor.pane, {
				"width": editorWidth + "px",
				"left": centerOfLabel - labelWidth / 2 + "px",
				"top": operatorPrimaryInfo.y + operatorUnit.icon.shape.height + "px",
				"display": ""
            });
			editor.textInput.set("style", "width: " + editorWidth + "px; font-size: 12px");
			_bindEvents(editor.textInput);
		}
	}
	
	function _getEditor(paneId){
		if(editorCache[paneId]){
			return editorCache[paneId];
		}
		var editorContainer = dojo.create("div",null, dojo.byId(paneId));
        dojo.addClass(editorContainer,"operatorRenameContainer");
		var textInput = _createTextbox(editorContainer);
		editorCache[paneId] = {
			pane: editorContainer,
			textInput: textInput
		};
		return editorCache[paneId];
	}
	
	function _createTextbox(container){
		var operatorLabelTextBox = new dijit.form.ValidationTextBox({
			id: constants.TEXT_BOX,
			required: true,
			trim: true,
			selectOnClick: true,
			style: "width: 100px; margin: 0;",
			regExp: "[^\\\&\\\?=\\\"\\\':\\\[\\\]\\\{\\\},]+",//exclude & ? = " ' :  [ { ] } ,
			isValid: function(){
				var val = this.get("value");
				var check = this.validator(val);
				if(!check){
					return false;
				}
				operatorHandler.forEachOperatorInfo(function(operatorInfo){
					if(currentOperatorUid != operatorInfo.uid){
						check &= (val != operatorInfo.name);
					}
				});
				return check;
			}
		}, dojo.create("input", {}, container));
		operatorLabelTextBox.startup();
		return operatorLabelTextBox;
	}
	
	function _bindEvents(textbox){
		eventArray.push(dojo.connect(window, "keydown", window, function(event){
			if(event.keyCode == dojo.keys.ESCAPE){
				hideEditor(currentPaneId);
			}
		}));
		eventArray.push(dojo.connect(textbox, "onKeyDown", textbox, function(event){
			if(event.keyCode == dojo.keys.ENTER){
				_updateText(currentPaneId, currentOperatorUid);
			}
		}));
		eventArray.push(dojo.connect(textbox, "onBlur", textbox, function(event){
			_updateText(currentPaneId, currentOperatorUid);
		}));
	}
	
	function _updateText(paneId, operatorUid){
		if(!dijit.byId(constants.TEXT_BOX).validate()){
			return;
		}
		var newLabel = dijit.byId(constants.TEXT_BOX).get("value");
		if (newLabel !== operatorHandler.getOperatorPrimaryInfo(operatorUid).name) {
			operatorHandler.renameOperator(workFlowHandler.getEditingFlow(), operatorUid, newLabel, function(){
				painter.updateOperatorVisualization({
					paneId: paneId,
					operatorId: operatorUid,
					label: {
						text: newLabel
					}
				});
	            alpine.flow.WorkFlowUIHelper.setDirty(true);
	            operatorHandler.updateOperatorPrimaryInfo([{
	                uid: operatorUid,
	                name: newLabel
	            }]);
			});
        }
		hideEditor(paneId);
	}
	
	/**
	 * move editor
	 * targetPosition = {
	 * 		x,y
	 * }
	 */
	function _moveEditor(operatorUid, targetPosition){
		if(!currentPaneId || !currentOperatorUid || operatorUid != currentOperatorUid){
			return;
		}
		var x = dojo.style(editorCache[currentPaneId].pane, "left");
		var y = dojo.style(editorCache[currentPaneId].pane, "top");
		dojo.style(editorCache[currentPaneId].pane, {
			"left": x + targetPosition.x + "px",
			"top": y + targetPosition.y + "px"
		});
	}
	
	/**
	 * return true if edit new name for operator
	 */
	function _isRenaming(){
		return currentOperatorUid != null;
	}
	
	function _release(){
		editorCache = [];
		eventArray = [];
		if(dijit.byId(constants.TEXT_BOX)){
			dijit.byId(constants.TEXT_BOX).destroyRecursive();
		}
		currentOperatorUid = null;
		var eh = null;
		while((eh = eventArray.pop()) != undefined){
			dojo.disconnect(eh);
		}
	}
	
	function hideEditor(paneId){
		currentPaneId = null;
		currentOperatorUid = null;
		if(dijit.byId(constants.TEXT_BOX)){
			dijit.byId(constants.TEXT_BOX).reset();
		}
		if(editorCache[paneId]){
			dojo.style(editorCache[paneId].pane, "display", "none");
		}
	}
	
	return {
		setupLabelEditor: _setupLabelEditor,
		moveEditor: _moveEditor,
		isRenaming: _isRenaming,
		release: _release
	};
});