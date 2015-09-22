/**
 * User: robbie
 * Date: 12/14/12
 * 2012
 */

define([
    "dojo/ready",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/query",
    "dojo/on",
    "dojo/_base/lang",
    "dojo/_base/array",
    "dojo/store/Memory",
    "dijit/registry",
    "dijit/form/Button",
    "dijit/form/Select",
    "dijit/form/FilteringSelect",
    "dijit/form/ValidationTextBox",
    "dijit/form/CheckBox",
    "alpine/layout/HistogramTypeWidget/HistogramTypeWidget",
    "alpine/layout/InlineEdit/InlineEditTTDialog",
    "alpine/layout/InlineEdit/InlineEditDropDown",
    "alpine/layout/InlineEdit/InlineEdit",
    "alpine/props/HadoopDataTypeUtil"
],
    function(ready, dom, domConstruct, domClass, query, on, lang, array, Memory, registry, Button, Select, FilteringSelect, ValidationTextBox, CheckBox, HistogramTypeWidget, InlineTTDialog, InlineDropDown, inlineEdit, hadoopTypeUtil) {

        var constants = {
            DIALOG_ID: "nvrConfigurationDialog",
            BTN_SEL_ALL: "nvrConfigurationAllBtn",
            BTN_SEL_NONE: "nvrConfigurationNoneBtn",
            BTN_OK: "nvrConfigurationDialog_ok",
            BTN_CANCEL: "nvrConfigurationDialog_cancel",
            GROUP_BY: "nvrGroupBySelect",
            FORM: "nvrConfigurationForm",
            TABLE_CONTAINER: "nvrInlineEdit",
            DEFAULT_ITEM_VALUES: {},
            COL_IDS: {
                CHECK: "colCheck",
                NAME: "colName",
                TYPE: "colType",
                VALUE: "colValue"
            },
            STYLE : {
                LABEL: "inlineLabel",
                D_LABEL: "inlineLabelDisabled"
            },
            REP_NAME: "columnName",
            REP_COLTYPE: "columnType",
            REP_VALUE: "value",
            REP_TYPE: "type",
            REP_SEL: "selected",
            REP_TYPE_AVG: "AVG",
            REP_TYPE_VAL: "value",
            REP_TYPE_AGG: "agg",
            AVAIL_TYPES: ["AVG","MIN","MAX","value"]
        };

        var sourceButtonId;
        var availTypes;

        var returnProp;

        var inlineEditor;

        ready(function(){
            on(registry.byId(constants.BTN_OK), "click", _saveAndClose);
            on(registry.byId(constants.BTN_CANCEL), "click", _releaseAndClose);
            on(registry.byId(constants.DIALOG_ID), "hide", _cleanDialog);
            lang.mixin(constants, {AVAIL_TYPES_LABEL: [alpine.nls.nvr_avg,alpine.nls.nvr_min,alpine.nls.nvr_max,alpine.nls.nvr_with_value]})
        });

        /***************** Init Functions ****************************/
        function _showNVRDialog(prop, _sourceButtonID) {
            //console.log("----- show nvr dialog -----");
            sourceButtonId = _sourceButtonID;
            _initNVRDialog(prop);
            registry.byId(constants.DIALOG_ID).show();
        }

        function _initNVRDialog(prop) {
            inlineEditor = new inlineEdit({
                tableContainer: constants.TABLE_CONTAINER,
                defaultItemValues: constants.DEFAULT_ITEM_VALUES,
                _fillNewRow: _fillNewRow
            });
            returnProp = prop;
            _setupAllNoneButtons();
            if(!CurrentOperatorDTO.inputFileInfos){
            	return;
            }
            var columnInfo = CurrentOperatorDTO.inputFileInfos[0].columnInfo;
            var typeMap = _getHadoopMetadataList(columnInfo);
            registry.byId(constants.GROUP_BY).set('store',_getGroupByColumnMemory(columnInfo));
            var fullSelection = lang.clone(prop.fullSelection);
            var nullReplacements = [];
            if (prop.nullReplacementModel) {
                nullReplacements = lang.clone(prop.nullReplacementModel.nullReplacements);
                registry.byId(constants.GROUP_BY).set('value',prop.nullReplacementModel.groupBy);
            }
            var selected = lang.clone(prop.selected);
            availTypes = _getTypesForSelect();
            var dataItems = _buildItems(fullSelection, nullReplacements, selected, typeMap);
            var headerInfo = [
                {innerHTML:"",style:"width:5%"},
                {innerHTML:alpine.nls.nvr_column_name,style:"width:45%"},
                {innerHTML:alpine.nls.nvr_method,style:"width:30%"},
                {innerHTML:"",style:"width:20%"}
            ];

            inlineEditor.createTable(headerInfo, dataItems);
        }

        function _getHadoopMetadataList(columnInfo) {
            var colNames = columnInfo.columnNameList;
            var colTypes = columnInfo.columnTypeList;
            var typeMap = {};
            for ( var k=0; k<colNames.length; k++ ) {
                typeMap[colNames[k]] = colTypes[k];
            }
            return typeMap;
        }

        function _getGroupByColumnMemory(columnInfo) {
            var colNames = columnInfo.columnNameList;
            var colTypes = columnInfo.columnTypeList;
            var data = [];
            var acceptableTypes = [
                hadoopTypeUtil.hadoopDatatype.CHARARRAY,
                hadoopTypeUtil.hadoopDatatype.LONG,
                hadoopTypeUtil.hadoopDatatype.INT
            ];
            data.push({id: "", name:alpine.nls.nvr_placeholder_no_group});
            for (var k=0; k<colNames.length; k++ ) {
                if (array.indexOf(acceptableTypes, colTypes[k]) != -1) {
                    data.push({
                        id: colNames[k],
                        name: colNames[k] + " [" + colTypes[k] + "]"
                    });
                }
            }
            return new Memory({data: data});
        }

        function _buildItems(fullSelection, nullReplacements, selected, typeMap) {
            var rowObjects = [];
            var hasMatch;
            array.forEach(fullSelection, function(col){
                var rowObject = {};
                rowObject[constants.REP_NAME] = col;
                rowObject[constants.REP_COLTYPE] = typeMap[col];
                rowObject[constants.REP_SEL] = (array.indexOf(selected,col) != -1);
                hasMatch = array.some(nullReplacements, function(aRep){
                    if (aRep[constants.REP_NAME] == col) {
                        if (!aRep[constants.REP_TYPE] || aRep[constants.REP_TYPE] == constants.REP_TYPE_VAL ) {
                            rowObject[constants.REP_VALUE] = aRep[constants.REP_VALUE];
                            rowObject[constants.REP_TYPE] = constants.REP_TYPE_VAL;
                        } else {
                            rowObject[constants.REP_TYPE] = (typeMap[col] == hadoopTypeUtil.hadoopDatatype.CHARARRAY) ? constants.REP_TYPE_VAL : aRep[constants.REP_VALUE];
                            rowObject[constants.REP_VALUE] = (typeMap[col] == hadoopTypeUtil.hadoopDatatype.CHARARRAY) ? "''" : "";
                        }
                        return true;
                    } else {
                        return false;
                    }
                });
                if (hasMatch) {
                    rowObjects.push(rowObject);
                } else {
                    rowObject[constants.REP_VALUE] = (typeMap[col] == hadoopTypeUtil.hadoopDatatype.CHARARRAY) ? "''" : "0";
                    rowObject[constants.REP_TYPE] = constants.REP_TYPE_VAL;
                    rowObjects.push(rowObject);
                }
            });
            return rowObjects;
        }

        function _getTypesForSelect() {
            var ops = [];
            for ( var k=0; k < constants.AVAIL_TYPES.length; k++ ) {
                ops.push({
                    label: constants.AVAIL_TYPES_LABEL[k],
                    value: constants.AVAIL_TYPES[k]
                });
            }
            return ops;
        }

        function _setupAllNoneButtons() {
            inlineEditor.addToListeners(on(registry.byId(constants.BTN_SEL_ALL), "click", function(){
                _selectAllNone(true);
            }));
            inlineEditor.addToListeners(on(registry.byId(constants.BTN_SEL_NONE), "click", function(){
                _selectAllNone(false);
            }));
        }

        function _fillNewRow(item, row, rowIndex) {
            _createCheckColumn(item, row, rowIndex);
            _createColumnNameColumn(item, row, rowIndex);
            _createTypeColumn(item, row, rowIndex);
            _createValueColumn(item, row, rowIndex);
        }

        function _createCheckColumn(item, row, rowIndex) {
            var selCB = new CheckBox({
                uniqueType: constants.COL_IDS.CHECK,
                value: item[constants.REP_NAME],
                checked: item[constants.REP_SEL],
                dataType: item[constants.REP_COLTYPE]
            });
            inlineEditor.putWidgetInCol(row, selCB);
            var handle = on(selCB,"change", function(val){
                var typeWidget, valueWidget;
                array.forEach(registry.findWidgets(row),function(widget){
                    switch (widget.get('uniqueType')) {
                        case constants.COL_IDS.TYPE:
                            typeWidget = widget;
                            widget.set('disabled', !val);
                            break;
                        case constants.COL_IDS.VALUE:
                            valueWidget = widget;
                            break;
                        default:
                            break;
                    }
                });
                if (typeWidget) {
                    var type = typeWidget.get('value');
                } else {
                    type = constants.REP_TYPE_VAL;
                }
                if (valueWidget) {valueWidget.set('disabled',isValueDisabled(val, type));}

                // toggle the disabled class on labels (column name & type label if data type is chararray)
                query("."+constants.STYLE.LABEL,row).forEach(function(domNode){
                    domClass.toggle(domNode,constants.STYLE.D_LABEL,!val);
                });
            });
            inlineEditor.addToListeners(handle);
        }

        function isValueDisabled(isSelected, type) {
            return !isSelected || (type != constants.REP_TYPE_VAL);
        }

        function _createColumnNameColumn(item, row, rowIndex) {
            var colName = item[constants.REP_NAME]  + " [" + item[constants.REP_COLTYPE] + "]";
            var label = domConstruct.create('div',{innerHTML:colName,title:colName,className:constants.STYLE.LABEL});
            domClass.toggle(label, constants.STYLE.D_LABEL, !item[constants.REP_SEL]);
            inlineEditor.putDomInCol(row, label);
        }

        function _createTypeColumn(item, row, rowIndex) {
            if (item[constants.REP_COLTYPE].toLowerCase() == hadoopTypeUtil.hadoopDatatype.CHARARRAY) {
                var label = domConstruct.create('label',{innerHTML:alpine.nls.nvr_with_string,className:constants.STYLE.LABEL});
                domClass.toggle(label, constants.STYLE.D_LABEL, !item[constants.REP_SEL]);
                inlineEditor.putDomInCol(row, label);
            } else {
                var sel = new Select({
                    uniqueType: constants.COL_IDS.TYPE,
                    baseClass: "inlineDropdownButton",
                    style: "width:100%;",
                    options: availTypes,
                    value: item[constants.REP_TYPE],
                    disabled: !item[constants.REP_SEL]
                });
                inlineEditor.putWidgetInCol(row,sel);
                var handle = on(sel, "change", function(evt){
                    var disableValue = evt != constants.REP_TYPE_VAL;
                    array.forEach(registry.findWidgets(row),function(widget){
                        if (widget.get('uniqueType') == constants.COL_IDS.VALUE) {
                            widget.set('disabled', disableValue);
                        }
                    });
                });
                inlineEditor.addToListeners(handle);
            }
        }

        function _createValueColumn(item, row, rowIndex) {
            var validTB = new ValidationTextBox({
                uniqueType: constants.COL_IDS.VALUE,
                baseClass: "inlineTextbox",
                style: "width:100%;",
                value: item[constants.REP_VALUE],
                disabled: isValueDisabled(item[constants.REP_SEL], item[constants.REP_TYPE]),
                isValid: function() {
                    var val = this.get('value');
                    if (item[constants.REP_COLTYPE] == hadoopTypeUtil.hadoopDatatype.CHARARRAY) {
                        return true
                    } else {
                        return val.length > 0 && !isNaN(val);
                    }
                }
            });
            inlineEditor.putWidgetInCol(row,validTB);
        }

        function _selectAllNone(allOrNone) {
            var allRows = inlineEditor.returnRows();
            allRows.forEach(function(doms){
                array.forEach(registry.findWidgets(doms),function(widget){
                    switch (widget.get('uniqueType')) {
                        case constants.COL_IDS.CHECK:
                            widget.set('checked', allOrNone);
                            break;
                        default:
                            break;
                    }
                });
            })
        }

        function _ensureStringHasQuotes(value) {
            if (value.slice(0,1) != "'" ) {value = "'" + value;}
            if (value.slice(-1) != "'" ) {value += "'";}
            if (value == "'" ) {value += "'";}
            return value;
        }

        function _buildReturnObj() {
            var allRows = inlineEditor.returnRows();
            var returnArray = [];
            var selected = [];
            var rowObject;
            var isSelected, dataType;
            var failToSave = false;
            var reuseGroupBy = false;
            var groupBy = registry.byId(constants.GROUP_BY).get('value');
            allRows.forEach(function(doms){
                isSelected = false;
                rowObject = {};
                array.forEach(registry.findWidgets(doms),function(widget){
                    switch (widget.get('uniqueType')) {
                        case constants.COL_IDS.CHECK:
                            dataType = widget.get('dataType');
                            if (widget.get('checked')) {
                                var thisColName = widget.get('value');
                                isSelected = true;
                                selected.push(thisColName);
                                rowObject[constants.REP_NAME] = thisColName;
                                if (thisColName == groupBy) {reuseGroupBy = true;}
                            }
                            break;
                        case constants.COL_IDS.TYPE:
                            if (widget.get('value') == constants.REP_TYPE_VAL) {
                                rowObject[constants.REP_TYPE] = constants.REP_TYPE_VAL;
                            } else {
                                rowObject[constants.REP_VALUE] = widget.get('value');
                                rowObject[constants.REP_TYPE] = constants.REP_TYPE_AGG;
                            }
                            break;
                        case constants.COL_IDS.VALUE:
                            if (rowObject[constants.REP_VALUE] == null) { //chararray
                                rowObject[constants.REP_TYPE] = constants.REP_TYPE_VAL;
                                if (dataType == hadoopTypeUtil.hadoopDatatype.CHARARRAY) {
                                    // if dataType is chararray make sure it starts and ends with '
                                    rowObject[constants.REP_VALUE] = _ensureStringHasQuotes(widget.get('value'));
                                } else {
                                    rowObject[constants.REP_VALUE] = widget.get('value');
                                }
                            }
                            break;
                        case constants.COL_IDS.NAME:
                            break;
                        default:
                            break;
                    }
                });
                if (reuseGroupBy) {
                    popupComponent.alert(alpine.nls.nvr_cannot_replace_groupby);
                    failToSave = true;
                }
                if (isSelected) {
                    returnArray.push(rowObject);
                    isSelected = false;
                }
            });

            //need one or more columns
            if (returnArray.length == 0) {
                popupComponent.alert(alpine.nls.nvr_select_one_or_more);
                return false;
            }
            // failed to validate in building row objects
            if (failToSave) {return false;}

            return {
                selected: selected,
                nullReplacements: returnArray,
                groupBy: groupBy
            }
        }

        function _saveAndClose() {
            if (!registry.byId(constants.FORM).validate()) {return;}
            var returnObj = _buildReturnObj();
            if (returnObj === false) {return;}
            returnProp.selected = returnObj.selected;
            if (!returnProp.nullReplacementModel) {returnProp.nullReplacementModel={};}
            returnProp.nullReplacementModel.nullReplacements = returnObj.nullReplacements;
            returnProp.nullReplacementModel.groupBy = returnObj.groupBy;
            setButtonBaseClassValid(sourceButtonId);
            _releaseAndClose();
        }

        function _releaseAndClose() {
            registry.byId(constants.DIALOG_ID).hide();
        }

        function _cleanDialog() {
            sourceButtonId = null;
            inlineEditor.clean();
            inlineEditor = {};
            registry.byId(constants.GROUP_BY).set('value',"");
        }

        function _getNVRModel(){
            return returnProp;
        }

        function _destroyNVRModel(){
            returnProp = null;
        }

        return {
            showNVRDialog: _showNVRDialog,
            getNVRModel: _getNVRModel,
            destroyNVRModel: _destroyNVRModel
        }

    });
