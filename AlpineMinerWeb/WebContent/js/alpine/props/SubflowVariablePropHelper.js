/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: SubflowVariablePropHelper
 * Author: Will
 * Date: 13-1-24
 */
define([
    "dojo/dom-attr",
    "dojo/dom-style",
    "dojo/dom-class",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/query",
    "dojo/on",
    "dojo/ready",
    "dijit/registry",
    "dojo/string",
    "dojo/_base/array",
    "alpine/layout/InlineEdit/InlineEdit",
    "dijit/form/ValidationTextBox"
],function(domAttr,domStyle,domClass,dom,domConstruct,query,on,ready,registry,stringUtil,array,InlineEdit,ValidationTextBox){
    var constants = {
        DIALOG:"subFlowPropertyVariableDialog",
        BTN_OK:"subFlowPropertyVariable_button_submit",
        BTN_CANCEL:"subFlowPropertyVariable_button_cancel",
        BUTTON_ADD:"subFlowPropertyVariable_button_add",
        GRID_CONTAINER:"subFlowPropertyVariable_variableGrid_container",
        FORM:"subFlowPropertyVariable_variableForm",
        FIXED_VARIABLE: ["@default_schema", "@default_prefix", "@default_tempdir"]
    };

    var inlineEditor = null;
    var variableModel = null;

    ready(function(){
        on(registry.byId(constants.BTN_OK),"click",saveVariableConfig);
        on(registry.byId(constants.BTN_CANCEL),"click",cancelSaveConfig);
    });

    function showSubFlwoVariableDlg(prop){
        var variableDlg = registry.byId(constants.DIALOG);
        if (variableDlg != null) {
            //variableDlg.set("title", props.displayName);
            variableDlg.titleBar.style.display = "none"
            variableDlg.show();

            inlineEditor = new InlineEdit({
                newRowButtonId: constants.BUTTON_ADD,
                tableContainer: constants.GRID_CONTAINER,
                defaultItemValues: {
                    flowName: ""
                },
                _fillNewRow: _buildRow
            });
            var headerInfo = [
                {innerHTML: alpine.nls.flowvariable_grid_title_variable, style: "width:110px"},
                {innerHTML: alpine.nls.flowvariable_grid_title_value, style: "width:110px"},
                {innerHTML: "", style: "width:40px"}
            ];
            var data = [];
            if(prop.flowVariable==null){
                prop.flowVariable = {}
            }
            if(prop.flowVariable.variables==null){
                prop.flowVariable.variables = [];
            }
            if(prop!=null && prop.flowVariable!=null && prop.flowVariable.variables!=null){
                for(var i=0;i<prop.flowVariable.variables.length;i++){
                    data.push({
                        flowName:"",
                        name:prop.flowVariable.variables[i].name,
                        value:prop.flowVariable.variables[i].value
                    })
                }
            }
            variableModel = prop.flowVariable;
            inlineEditor.createTable(headerInfo, data);

        }
    }

    function _buildRow(item, row, rowIndex){
        _buildVariableNameInput(item, row, rowIndex);
        _buildVariableValueInput(item, row, rowIndex);
        _buildVariableFlowNameCell(item, row, rowIndex);
        if(item.name!="" && dojo.indexOf(constants.FIXED_VARIABLE, item.name) == -1){
            inlineEditor.createDeleteButton(row, rowIndex);
        }else{
            _createEmptyCol(item, row, rowIndex);
        }
    }

    function _buildVariableNameInput(item, row, rowIdx){
        var disabledVariable = dojo.indexOf(constants.FIXED_VARIABLE, item.name) != -1
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
         return true;
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

    function _createEmptyCol(item, row, rowIndex) {
        inlineEditor.putDomInCol(row, domConstruct.create("td"));
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

    function hideDialog(){
        var variableDlg = registry.byId(constants.DIALOG);
        if (variableDlg != null) {
            inlineEditor.clean();
            inlineEditor = null;
            variableDlg.hide();
        }
    }

    function saveVariableConfig(){
        if(!registry.byId(constants.FORM).validate()){
            popupComponent.alert(alpine.nls.flowvariable_editor_validate_failure);
            return;
        }
        var variables = _getVariableData();
        variableModel.variables = [];
        for(var i=0;i<variables.length;i++){
            variableModel.variables.push({
                name:variables[i].name,
                value:variables[i].value
            })
        }

        hideDialog();
    }

    function _getVariableData(){
        var variables = [];
        inlineEditor.returnRows().forEach(function(row){
            var variable = {};
            registry.findWidgets(row).forEach(function(widget){
                if(widget.widgetType == "varName"){
                    variable.name = widget.get("value");
                }else if(widget.widgetType == "varValue"){
                    variable.value = widget.get("value");
                }
            });
            variables.push(variable);
        });
        return variables;
    }

    function cancelSaveConfig(){

        hideDialog();
    }

    return {
        showSubFlwoVariableDlg:showSubFlwoVariableDlg
    }

})