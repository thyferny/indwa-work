/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowVariableHelper.js 
 * Author Gary
 * Dec 25, 2012
 */
define(["dojo/ready",
        "dojo/dom",
        "dojo/dom-class",
        "dojo/on",
        "dijit/registry",
        "dojo/dom-construct",
        "alpine/layout/InlineEdit/InlineEdit",
        "dijit/form/ValidationTextBox",
        "alpine/flow/WorkFlowVariableManager",
        "alpine/flow/WorkFlowManager"], function(ready, dom, domClass, on, registry, domConstruct, InlineEdit, ValidationTextBox, workFlowVariableManager, workflowManager){
	
	var constants = {
		MENU: "flowVariable_setting_button",
		DIALOG: "flow_flowvariable_dialog",
		FORM: "flow_flowvariable_variableForm",
		BUTTON_ADD: "flow_flowvariable_button_add",
		BUTTON_REMOVE: "flow_flowvariable_button_remove",
		BUTTON_SUBMIT: "flow_flowvariable_button_submit",
		BUTTON_CLOSE: "flow_flowvariable_button_cancel",
		GRID_CONTAINER: "flow_flowvariable_variableGrid_container",
		FIXED_VARIABLE: ["@default_schema", "@default_prefix", "@default_tempdir"],
		CURRENT_FLOW_SIGN: ""
	};
	
	ready(function(){
		on(registry.byId(constants.BUTTON_CLOSE), "click", function(){
			registry.byId(constants.DIALOG).hide();
		});
		on(registry.byId(constants.DIALOG), "hide", _clean);
		on(registry.byId(constants.MENU), "click", _showVariableDialog);
		on(registry.byId(constants.BUTTON_SUBMIT), "click", _saveVariables);
		
	});
	
	var inlineEditor = null;
	var eventTracker = [];
	var duplicatWidgetMapping = [];
	
	function _showVariableDialog(){
 		if(!workflowManager.isEditing()){
			return;
		}
 		registry.byId(constants.DIALOG).titleBar.style.display = "none";
 		registry.byId(constants.DIALOG).show();
		
		inlineEditor = new InlineEdit({
            newRowButtonId: constants.BUTTON_ADD,
            tableContainer: constants.GRID_CONTAINER,
            defaultItemValues: {
            	flowName: constants.CURRENT_FLOW_SIGN
            },
            _fillNewRow: _buildRow
        });
        var headerInfo = [
            {innerHTML: alpine.nls.flowvariable_grid_title_variable, style: "width:110px"},
            {innerHTML: alpine.nls.flowvariable_grid_title_value, style: "width:110px"},
            {innerHTML: ""},
            {innerHTML: "", style: "width:40px"}
        ];
        workFlowVariableManager.getFlowVariable(workflowManager.getEditingFlow(), constants.CURRENT_FLOW_SIGN, function(data){
        	inlineEditor.createTable(headerInfo, data);
        }, constants.DIALOG);
	}
	
	function _buildRow(item, row, rowIndex){
		_buildVariableNameInput(item, row, rowIndex);
		_buildVariableValueInput(item, row, rowIndex);
		_buildVariableFlowNameCell(item, row, rowIndex);
		if(item.flowName == constants.CURRENT_FLOW_SIGN
				&& dojo.indexOf(constants.FIXED_VARIABLE, item.name) == -1){
	        inlineEditor.createDeleteButton(row, rowIndex, function(row, rowIdx){
	    		_foreachWidget(function(itemWidget){
	    			if(registry.findWidgets(row)[0].get("value") == itemWidget.get("value")){
		    			domClass.remove(itemWidget.focusNode, "strikethrough");
	    			}
	    		});
	        });
		} else {
            _createEmptyCol(item, row, rowIndex);
        }
	}
	
	function _buildVariableFlowNameCell(item, row, rowIndex){
		var flowName = item.flowName.length > 25 ? item.flowName.substring(0, 25) + "..." : item.flowName;
		var flowNameLabel = new ValidationTextBox({
			widgetType: "flowName",
			title: item.flowName,
			value: flowName,
            baseClass: "inlineTextboxV",
            disabled: true
		});
		inlineEditor.putWidgetInCol(row, flowNameLabel);
	}
	
	function _buildVariableNameInput(item, row, rowIdx){
		var disabledVariable = dojo.indexOf(constants.FIXED_VARIABLE, item.name) != -1 || !item.flowName == constants.CURRENT_FLOW_SIGN;
		var varNameInput = new ValidationTextBox({
			widgetType: "varName",
			value: item.name,
			title: item.name == null ? "" : item.name,
            baseClass: "inlineTextboxV",
            style: "width:100px;",
            required: true,
            trim: true,
            disabled: disabledVariable,
            isValid: _validateVariableName
		});
		inlineEditor.putWidgetInCol(row, varNameInput);
		
		// initialize the override status for variable name.
		var rows = inlineEditor.returnRows();
		for(var i = 0;i < rows.length;i++){
			var rowWidgets = registry.findWidgets(rows[i]);
			if(rowWidgets[2] == null){
				continue;
			}
			if(rowWidgets[2].get("value") != constants.CURRENT_FLOW_SIGN){
				break;
			}
			var foundDuplicate = _handleDuplicateVariable(rowWidgets[0], varNameInput, true);
			if(foundDuplicate){
				break;
			}
		}
	}

    function _createEmptyCol(item, row, rowIndex) {
        inlineEditor.putDomInCol(row, domConstruct.create("td"));
    }
	
	function _validateVariableName(){
		this.set("title", this.get("value"));
		var val = this.getValue();
		var isDuplicate = false;
		var check = this.validator(val);
		//if start with @alpine.hadoop or @alpine.mapred then use same validation as operator label's.
		var result = /^@alpine\.[hadoop|mapred].*$/.test(val) ? /[\&\?=\"\':\[\]\{\},]+/.test(val) : !/^@[\w][A-Za-z0-9_\.]*$/.test(val);
		if(!check || result){
			this.invalidMessage = this.messages.invalidMessage;
			return false;
		}
		_foreachWidget(function(widget){
			if(widget.widgetType == "flowName" && widget.get("value") != constants.CURRENT_FLOW_SIGN){ //only validate the variables which in current flow
				return false;
			}
			if(this == widget //filter itself
					|| widget.widgetType != "varName"
					|| isDuplicate){
				return;//to next widget
			}
			isDuplicate = _isDuplicateVariable(this, widget);
		}, this);
		if(isDuplicate == true){
			this.invalidMessage = alpine.nls.flowvariable_editor_variable_check_msg;
			return false;
		}
		_changeStatus4DuplicateVariable(this);
		return true;
	}
	
	function _buildVariableValueInput(item, row, rowIdx){
		var varValInput = new ValidationTextBox({
			widgetType: "varValue",
			value: item.value,
            baseClass: "inlineTextboxV",
            required: true,
            trim: true,
            style: "width:100px;"
		});
		inlineEditor.putWidgetInCol(row, varValInput);
	}
	
	function _changeStatus4DuplicateVariable(refWidget){
		//first clear all of widget's status
		_foreachWidget(function(itemWidget){
			domClass.remove(itemWidget.focusNode, "strikethrough");
		});
		_foreachWidget(function(itemWidget, i, cols){
			// only check variables of current flow.
			if(cols[2].get("value") != constants.CURRENT_FLOW_SIGN){
				return false;
			}
			if(itemWidget.widgetType != "varName"){
				return true;
			}
			_foreachWidget(function(widget, i, cols){
				//filter current variables.
				if(cols[2].get("value") == constants.CURRENT_FLOW_SIGN){
					return true;
				}
				_handleDuplicateVariable(itemWidget, widget, false);
			});
		});
		
		var rows = inlineEditor.returnRows();
		loopRows:
		for(var i = 0;i < rows.length;i++){
			var widgets = registry.findWidgets(rows[i]);
			for(var j = 0;j < widgets.length;j++){
				
			}
		}
		domClass.remove(refWidget.focusNode, "strikethrough");
	}
	
	function _isDuplicateVariable(refWidget, checkWidget){
		var isDuplicate = false;
		if(checkWidget == refWidget){
			return isDuplicate;
		}
		if(checkWidget.widgetType == "varName"
			&& checkWidget.get("value") == refWidget.get("value")){
			isDuplicate = true;
		}
		return isDuplicate;
	}
	
	function _handleDuplicateVariable(refWidget, checkWidget, forceChange){
		if(_isDuplicateVariable(refWidget, checkWidget)){
			if(!domClass.contains(checkWidget.focusNode, "strikethrough")){
				domClass.add(checkWidget.focusNode, "strikethrough");
			}
			return true;
		}else{
			if(forceChange == true){
				domClass.remove(checkWidget.focusNode, "strikethrough");
			}
			return false;
		}
	}
	
	function _foreachWidget(onWidget, scope){
		var rows = inlineEditor.returnRows();
		loopRows:
		for(var i = 0;i < rows.length;i++){
			var widgets = registry.findWidgets(rows[i]);
			for(var j = 0;j < widgets.length;j++){
				var isContinue = onWidget.call(scope, widgets[j], i, widgets);
				if(isContinue != null && !isContinue){
					break loopRows;
				}
			}
		}
	}
	
	function _getVariableData(){
		var variables = new Array();
		var rowIndex = 0;
		var currentFlowName = workflowManager.getEditingFlow().id;
		inlineEditor.returnRows().forEach(function(row){
			variable = {};
			registry.findWidgets(row).forEach(function(widget){
				if(widget.widgetType == "varName"){
					variable.name = widget.get("value");
				}else if(widget.widgetType == "varValue"){
					variable.value = widget.get("value");
				}else if(widget.widgetType == "flowName"){
					var flowName = widget.get("title");
					variable.flowName = flowName == constants.CURRENT_FLOW_SIGN ? currentFlowName : flowName;
				}
			});
			rowIndex++;
			variables.push(variable);
		});
		return variables;
	}
	
	function _saveVariables(){
		if(!registry.byId(constants.FORM).validate()){
			 popupComponent.alert(alpine.nls.flowvariable_editor_validate_failure);
             return;
		}
		var variableRecords = _getVariableData();
		workFlowVariableManager.saveFlowVariables(variableRecords, workflowManager.getEditingFlow(), function(flowInfo){
			alpine.flow.OperatorManagementManager.updateOperatorPrimaryInfo(flowInfo.result);
			alpine.flow.OperatorManagementUIHelper.validateOperators();
			alpine.flow.WorkFlowUIHelper.setDirty(true);
			registry.byId(constants.DIALOG).hide();
		}, constants.DIALOG);
	}
	
	function _clean(){
		inlineEditor.clean();
        inlineEditor = null;
        var event = null;
        while((event = eventTracker.pop()) != null){
        	event.remove();
        }
	}
	
	return {
		showVariableDialog: _showVariableDialog
	};
});