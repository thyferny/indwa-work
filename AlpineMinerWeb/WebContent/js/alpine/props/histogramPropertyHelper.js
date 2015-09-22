/**
 * Date: 10/10/2012
 * User: robbie
 * (C) 2012 Alpine Data Labs
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
    "dijit/form/FilteringSelect",
    "dijit/form/ValidationTextBox",
    "alpine/layout/HistogramTypeWidget/HistogramTypeWidget",
    "alpine/layout/InlineEdit/InlineEditTTDialog",
    "alpine/layout/InlineEdit/InlineEditDropDown",
    "alpine/layout/InlineEdit/InlineEdit",
    "alpine/flow/WorkFlowVariableReplacer"
],
    function(ready, dom, domConstruct, domClass, query, on, lang, array, Memory, registry, Button, FilteringSelect, ValidationTextBox, HistogramTypeWidget, InlineTTDialog, InlineDropDown, inlineEdit, variableReplace) {
        var constants = {
            DIALOG_ID: "histogramConfigurationDialog",
            BTN_ADD_NEW: "histogramConfigurationNewBtn",
            BTN_OK: "histogramConfigurationDialog_ok",
            BTN_CANCEL: "histogramConfigurationDialog_cancel",
            FORM: "histogramConfigurationForm",
            TABLE_CONTAINER: "histogramInlineEdit",
            DEFAULT_ITEM_VALUES: {
                bin: 10,
                isMax: false,
                isMin: false,
                max: 100,
                min: 0,
                type: 0,
                width: 1
            },
            COL_IDS: {
                COL_NAME: "colNameCol",
                TYPE: "typeBinCol",
                MIN: "minCol",
                MAX: "maxCol"
            },
            MIN_ID: "histMin_id_",
            MAX_ID: "histMax_id_",
            COL_NAME_CLASS: "histColNameClass"
        };

        var colNameStore;
        var callbackFn;
        var sourceButtonId;

        var inlineEditor;

        ready(function(){
            on(registry.byId(constants.BTN_OK), "click", _saveAndClose);
            on(registry.byId(constants.BTN_CANCEL), "click", _releaseAndClose);
            on(registry.byId(constants.DIALOG_ID), "hide", _cleanDialog);
        });

        /***************** Init Functions ****************************/
        function _showHistogramDialog(prop, _sourceButtonID, _callback) {
            sourceButtonId = _sourceButtonID;
            callbackFn = _callback;
            _initHistogramDialog(prop);
            registry.byId(constants.DIALOG_ID).show();
        }

        function _initHistogramDialog(prop) {
            inlineEditor = new inlineEdit({
                newRowButtonId: constants.BTN_ADD_NEW,
                tableContainer: constants.TABLE_CONTAINER,
                defaultItemValues: constants.DEFAULT_ITEM_VALUES,
                _fillNewRow: _fillNewRow
            });
            var dataItems = lang.clone(prop.columnBinsModel.columnBins);
            colNameStore = new Memory({
                data: lang.clone(prop.store)
            });
            var headerInfo = [
                {innerHTML:alpine.nls.histogram_header_for,style:"width:30%"},
                {innerHTML:alpine.nls.histogram_header_type,style:"width:25%"},
                {innerHTML:alpine.nls.histogram_header_min,style:"width:18%"},
                {innerHTML:alpine.nls.histogram_header_max,style:"width:18%"},
                {innerHTML:"",style:"width:9%"}
            ];

            inlineEditor.createTable(headerInfo, dataItems);
        }
        /***************** End Init Functions ****************************/

        /***************** Histogram Specific Functions **************************************
        *
        *  Most of these functions should be interfaces:
        *  _fillNewRow
        *  _createDeleteButton (use as is)
        *  _buildReturnArray
        *  _saveAndClose
        *  _releaseAndClose
        *
        * */

        /*
         *  _fillNewRow
         *  tell _addRow how to create a new row, e.g. a single function for each column type
         *  @param item: row property object
         *  @param row: row dom
         *  @param rowIndex: unique row id
         *
         * */
        function _fillNewRow(item, row, rowIndex) {
            _createColNameCol(colNameStore, item, row, rowIndex);
            _createTypeCol(item, row, rowIndex);
            _createMinAndMaxCols(item, row, "min", rowIndex);
            _createMinAndMaxCols(item, row, "max", rowIndex);
            inlineEditor.createDeleteButton(row, rowIndex);
        }

        /*
         *  _createColNameCol
         *  for histogram - create column name column
         *
         * */
        function _createColNameCol(colNameStore, item, row, rowIndex) {
            var comboBox = new FilteringSelect({
                uniqueType: constants.COL_IDS.COL_NAME,
                baseClass: "inlineDropdownButton",
                store: colNameStore,
                placeHolder: alpine.nls.inline_edit_choose_column,
                value: item.columnName ? item.columnName : "",
                isValid: function() {
                    var val = this.get('value');
                    if (!this.validator(val)) {
                        this.invalidMessage="The entered value is invalid.";
                        return false;
                    }
                    var colNames = _currentlyConfiguredCols();
                    var counter = 0;
                    for (var k=0;k<colNames.length;k++) {
                        if (colNames[k] == val) {counter++;}
                    }
                    this.invalidMessage="Each column can only be specified once.";
                    return (counter < 2) ? true : false;
                }
            });
            comboBox.startup();
            inlineEditor.putWidgetInCol(row,comboBox);
            domClass.add(comboBox.domNode,constants.COL_NAME_CLASS);
        }

        /*
         *  _createTypeCol
         *  for histogram - create bin type column
         *
         * */
        function _createTypeCol(item, row, rowIndex) {
            var histTypeWidget = new HistogramTypeWidget({
                binType:item.type,
                binWidth:item.width,
                binNumber:item.bin
            });

            var ttDialog = new InlineTTDialog({
                content:histTypeWidget
            });

            var dropdown = new InlineDropDown({
                uniqueType: constants.COL_IDS.TYPE,
                dropDown: ttDialog,
                baseClass: "inlineEditCustom"
            });
            inlineEditor.putWidgetInCol(row,dropdown);
        }

        /*
         *  _createMinAndMaxCols
         *  for histogram - create min and max columns
         *
         * */
        function _createMinAndMaxCols(item,row,type,rowIndex) {
            var minmaxValue = "";
            var placeHolderValue;
            var idPrefix;
            var widgetID;
            switch (type) {
                case "min":
                    if (item.isMin == true) {minmaxValue = item.min}
                    placeHolderValue = alpine.nls.histogram_inline_nomin;
                    widgetID = constants.COL_IDS.MIN;
                    idPrefix = constants.MIN_ID + rowIndex;
                    break;
                case "max":
                    if (item.isMax == true) {minmaxValue = item.max}
                    placeHolderValue = alpine.nls.histogram_inline_nomax;
                    widgetID = constants.COL_IDS.MAX;
                    idPrefix = constants.MAX_ID + rowIndex;
                    break;
                default:
                    break;
            }
            var tdTextBox = new ValidationTextBox({
                id: idPrefix,
                uniqueType: widgetID,
                value: minmaxValue,
                placeHolder: placeHolderValue,
                baseClass: "inlineTextbox",
                style: "width:100px;",
                isValid: function() {
                    var val = variableReplace.replaceVariable(this.get('value'));
                    var basicValidate = /^[-\+]?\d+(\.\d+)?((e|E)\+\d+)?$/.test(val) || /^\d*(\.\d+)?$/.test(val);
                    if (!basicValidate) {return false;}
                    var minBox = registry.byId(constants.MIN_ID + rowIndex);
                    var maxBox = registry.byId(constants.MAX_ID + rowIndex);
                    if (minBox && maxBox) {
                        var minSet = variableReplace.replaceVariable(minBox.get('value'));
                        var maxSet = variableReplace.replaceVariable(maxBox.get('value'));
                        if (maxSet && minSet) {
                            basicValidate = parseFloat(maxSet) > parseFloat(minSet);
                        }
                    }
                    return basicValidate;
                }
            });
            inlineEditor.putWidgetInCol(row,tdTextBox);
        }

        /*
         *  _currentlyConfiguredCols
         *  utility to get currently selected columns for validation
         *
         * */
        function _currentlyConfiguredCols() {
            var currentlyConfiguredCols = [];
            query("."+constants.COL_NAME_CLASS, dom.byId(constants.TABLE_CONTAINER)).forEach(function(node) {
                currentlyConfiguredCols.push(registry.getEnclosingWidget(node).get('value'));
            });
            return currentlyConfiguredCols;
        }

        /*
         *  _buildReturnArray
         *  from the table, build the array to return the configured rows
         *
         * */
        function _buildReturnArray() {
            var allRows = inlineEditor.returnRows();
            var returnArray = [];
            var histObj = {};
            allRows.forEach(function(doms){
                histObj = {};
                array.forEach(registry.findWidgets(doms),function(widget){
                    switch(widget.get('uniqueType')) {
                        case constants.COL_IDS.COL_NAME:
                            histObj.columnName = widget.get('value');
                            break;
                        case constants.COL_IDS.TYPE:
                            histObj.type = widget.dropDown.content.binType;
                            histObj.bin = widget.dropDown.content.binNumber;
                            histObj.width = widget.dropDown.content.binWidth;
                            /* histogram has backend bug
                             *  Even if not used, bin/width must be set (arbitrarily)...*/
                            if (histObj.bin=="") {histObj.bin = "1"}
                            if (histObj.width=="") {histObj.width = "1"}
                            break;
                        case constants.COL_IDS.MIN:
                            var val = widget.get('value');
                            if (val && val!="") {
                                histObj.isMin = true;
                                histObj.min = val;
                            } else {
                                histObj.isMin = false;
                                histObj.min = "0";
                            }
                            break;
                        case constants.COL_IDS.MAX:
                            var val = widget.get('value');
                            if (val && val!="") {
                                histObj.isMax = true;
                                histObj.max = val;
                            } else {
                                histObj.isMax = false;
                                histObj.max = "100";
                            }
                            break;
                    }
                });
                returnArray.push(histObj);
            });

            return returnArray;
        }

        /*
         *  _saveAndClose
         *  check if form is valid
         *  + build return array, release and close dialog
         *
         * */
        function _saveAndClose() {
            if(!registry.byId(constants.FORM).validate()){
                popupComponent.alert(alpine.nls.histogram_form_error);
                return;
            }
            var returnArray = _buildReturnArray();
            if(returnArray.length < 1){
                popupComponent.alert(alpine.nls.histogram_alert_nocolumn);
                return;
            }
            if(sourceButtonId){
                setButtonBaseClassValid(sourceButtonId);
            }
            callbackFn.call(null,returnArray);
            _releaseAndClose();
        }

        /*
         *  _releaseAndClose
         *  release and close dialog (discard changes)
         *
         * */
        function _releaseAndClose() {
            registry.byId(constants.DIALOG_ID).hide();
        }

        function _cleanDialog() {
            callbackFn = null;
            sourceButtonId = null;
            inlineEditor.clean();
            inlineEditor = {};
        }

        return {
            showHistogramDialog: _showHistogramDialog
        }
    });