/**
 * User: robbie
 * Date: 10/19/12
 * (c) Alpine Data Labs 2012
 */

define([
    "dojo/ready",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/query",
    "dojo/dom-style",
    "dojo/on",
    "dojo/_base/lang",
    "dojo/_base/array",
    "dojo/store/Memory",
    "dijit/registry",
    "dijit/form/Button",
    "dijit/form/Select",
    "dijit/form/FilteringSelect",
    "dijit/form/ValidationTextBox",
    "dijit/form/DropDownButton",
    "alpine/layout/InlineEdit/InlineEdit",
    "alpine/layout/InlineEdit/InlineEditTTDialog",
    "alpine/flow/OperatorManagementManager",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/props/MultiFileUtil",
    "alpine/util/ValidationHelper",
    "alpine/flow/WorkFlowVariableReplacer"
],
    function(ready, dom, domConstruct, domClass, query, domStyle, on, lang, array, Memory, registry, Button, Select, FilteringSelect, ValidationTextBox, DropDownButton, inlineEdit, inlineEditTTDialog, opManager, hdTypeUtil, multiFileUtil, validationHelper, variableReplacer) {
        var constants = {
            DIALOG_ID: "setOperatorDialog",
            BTN_ADD_NEW: "setOperatorDialogNewBtn",
            BTN_OK: "setOperatorDialog_ok",
            BTN_CANCEL: "setOperatorDialog_cancel",
            MAGIC: {
                dialog: "magicMappingDialog",
                dialogCancel: "magicMappingDialog_cancel",
                dialogOpen: "setOperatorMagicBtn",
                order: "magicBtn_order",
                name: "magicBtn_name",
                nameStrict: "magicBtn_nameStrict"
            },
            SEL_TYPE: "setOperatorUnionTypeSelect",
            SEL_TYPE_CONTAINTER: "setOperatorUnionTypeSelectCont",
            FIRST_TABLE: "setOperatorFirstTableSelect",
            FIRST_TABLE_CONTAINER: "setOperatorFirstTableSelectCont",
            SEL_DESC: "setOpTypeDescription",
            FORM: "setOperatorDialogForm",
            TABLE_CONTAINER: "setOpInlineEdit",
            COL_IDS: {
                COL_NAME: "colNameCol",
                ALIAS: "outputAlias"
            },
            COL_NAME_CLASS: "setAliasClass",
            GRID_ROW_ID: "_id#",
            ALIAS: "_customize_name#",
            HD_OUT: {
                NAME: "columnName",
                TYPE: "columnType",
                MAPPING: "mappingColumns",
                FILE: "file",
                UUID: "operatorModelID"
            }
        };

        var inlineEditor;

        var returnProp;
        var datasetMemories = [];
        var datasetArray = [];
        var aliasMap = [];
        var hdUnionFiles = [];

        var isHadoopSet = false;

        ready(function(){
            on(registry.byId(constants.BTN_OK), "click", _saveAndClose);
            on(registry.byId(constants.BTN_CANCEL), "click", _releaseAndClose);
            on(registry.byId(constants.DIALOG_ID), "hide", _cleanDialog);
        });

        /***************** Init Functions ****************************/
        function _showSetOpDialog(prop) {
            //console.log("===============SET OP DIALOG================");
            if (_initSetOpDialog(prop)) {
                registry.byId(constants.DIALOG_ID).show();
            } else {
                inlineEditor.clean();
                inlineEditor = {};
            }
        }

        function _initSetOpDialog(prop) {
            inlineEditor = new inlineEdit({
                newRowButtonId: constants.BTN_ADD_NEW,
                tableContainer: constants.TABLE_CONTAINER,
                _fillNewRow: _fillNewRow,
                _getDefaultItem: _getDefaultItem,
                _customClean: _customClean
            });

            returnProp = prop;
            var dataItems = lang.clone(prop.tableSetModel);
            if(!multiFileUtil.validateDBInputFiles(dataItems.inputTables)) {return false;}
            aliasMap = dataItems.aliasMap;
            var tableColumnsList = _getMetadataList(dataItems.inputTables);
            _setupFirstTableSelect(tableColumnsList, dataItems.firstTableName);
            _setupTypeSelect(dataItems.type);
            var headerInfo = _createHeaderInfo(tableColumnsList);
            var rowObjects = dataItems.datasetList;
            _setupColumnListMemories(tableColumnsList);
            inlineEditor.createTable(headerInfo, rowObjects);
            _enableMagicButton();
            return true;
        }

        function _setupColumnListMemories(tableColumnsList) {
            array.forEach(tableColumnsList, function(inputTable) {
                var datas = [];
                for (var i=0;i<inputTable.columns.length;i++) {
                    var cols = inputTable.columns[i];
                    datas.push({
                        name: cols[0] + " [" + cols[1] + "]",
                        id: cols[0],
                        type: cols[1]
                    });
                }
                datasetMemories[inputTable.tableName] = new Memory({
                    data: datas
                });
                datasetArray.push(inputTable.tableName);
            });
        }

        function _createHeaderInfo(tableColumnsList) {
            var headerInfo = [
                {innerHTML: "Alias",style:"width:150px;"}
            ];
            for (var i=0;i<tableColumnsList.length;i++) {
                var colObj = {
                    //innerHTML: variableReplacer.replaceVariable(tableColumnsList[i].schema + "." + tableColumnsList[i].pureTableName),
                    innerHTML: tableColumnsList[i].displayName,
                    title: variableReplacer.replaceVariable(tableColumnsList[i].schema + "." + tableColumnsList[i].pureTableName),
                    style: "width:170px;"
                };
                headerInfo.push(colObj);
            }
            headerInfo.push({innerHTML:"",style:"width:55px;"});
            return headerInfo;
        }

        function _getMetadataList(inputTables){
            var metadataInfoList = [];
            for(var i=0;i < inputTables.length;i++){
                var inputTableInfo = inputTables[i];
                var columnList = [];
                for(var j=0;j < inputTableInfo.fieldColumns.length;j++){
                    columnList.push(inputTableInfo.fieldColumns[j]);
                }
                var dbOpPrimaryInfo = opManager.getOperatorPrimaryInfo(inputTableInfo.operatorUUID);
                metadataInfoList.push({
                    schema: inputTableInfo.schema,
                    pureTableName: aliasMap[inputTableInfo.table],
                    tableName: inputTableInfo.schema + "." + inputTableInfo.table,//in order to macth with server side.
                    displayName: dbOpPrimaryInfo.name,
                    //displayName: variableReplacer.replaceVariable(inputTableInfo.schema + "." + inputTableInfo.table),
                    columns: columnList
                });
            }
            return metadataInfoList;
        }

        function _setupHadoopFirstTableSelect(tableMetaData, firstTable) {
            var firstTableOptions = [];
            array.forEach(tableMetaData, function(tableObj) {
                firstTableOptions.push({
                    value: "\"" + tableObj.pureTableName+ "\"",
                    label: tableObj.displayName
                });
            });
            if(!firstTable){
            	firstTable="" ;
            }
            var firstTableSelect = new Select({
                id: constants.FIRST_TABLE,
                baseClass:"greyDropdownButton",
                style: "width:auto;",
                options: firstTableOptions,
                value:firstTable
            },domConstruct.create("div",{},dom.byId(constants.FIRST_TABLE_CONTAINER)));
            domStyle.set(dom.byId(constants.FIRST_TABLE_CONTAINER),"visibility","hidden");
            inlineEditor.addToTracker(firstTableSelect);
        }
        
        function _setupFirstTableSelect(tableMetaData, firstTable) {
            var firstTableOptions = [];
            array.forEach(tableMetaData, function(tableObj) {
                firstTableOptions.push({
                    value: "\"" + tableObj.schema + "\".\"" + tableObj.pureTableName + "\"",
                    label: tableObj.displayName
                });
            });
            var firstTableSelect = new Select({
                id: constants.FIRST_TABLE,
                baseClass:"greyDropdownButton",
                style: "width:auto;",
                options: firstTableOptions,
                value:firstTable
            },domConstruct.create("div",{},dom.byId(constants.FIRST_TABLE_CONTAINER)));
            domStyle.set(dom.byId(constants.FIRST_TABLE_CONTAINER),"visibility","hidden");
            inlineEditor.addToTracker(firstTableSelect);
        }

        function _setupTypeSelect(setType) {
            var unionTypeSel = new Select({
                id: constants.SEL_TYPE,
                baseClass:"greyDropdownButton",
                style: "width:auto;",
                options:[
                    {label:"UNION",value:"UNION", selected:true},
                    {label:"UNION ALL",value:"UNION ALL"},
                    {label:"INTERSECT",value:"INTERSECT"},
                    {label:"EXCEPT",value:"EXCEPT"}
                ]
            },domConstruct.create("div",{},dom.byId(constants.SEL_TYPE_CONTAINTER)));
            _setTypeDescHelper(unionTypeSel.get('value'));
            on(unionTypeSel, "change", _setTypeDescHelper);
            unionTypeSel.set('value',setType);
            inlineEditor.addToTracker(unionTypeSel);
        }

        /***************** Set Functions **********************************/

        function _setTypeDescHelper(newVal) {
            var msg;
            var showFirstTable = false;
            switch (newVal) {
                case "UNION":
                    msg = alpine.nls.set_op_union_msg;
                    break;
                case "UNION ALL":
                    msg = alpine.nls.set_op_unionall_msg;
                    break;
                case "INTERSECT":
                    msg = alpine.nls.set_op_intersect_msg;
                    break;
                case "EXCEPT":
                    msg = alpine.nls.set_op_except_msg;
                    showFirstTable = true;
                    break;
                default:
                    msg = alpine.nls.set_op_union_msg;
                    break;
            }
            dom.byId(constants.SEL_DESC).innerHTML = msg;
            if (showFirstTable) {
                domStyle.set(dom.byId(constants.FIRST_TABLE_CONTAINER), "visibility", "visible");
            } else {
                domStyle.set(dom.byId(constants.FIRST_TABLE_CONTAINER), "visibility", "hidden");
            }
        }

        /***************** End Init Functions ****************************/


        /*
         *  _getDefaultItem
         *  utility to retrieve an object for filling a new row
         *
         * */
        function _getDefaultItem() {
            var defaultObj = {};
            defaultObj[constants.ALIAS] = "";
            for (var k=0;k<datasetArray.length;k++) {
                defaultObj[datasetArray[k]] = ""
            }
            return defaultObj;
        }

        /*
         *  _customClean
         *  utility to empty the table container and destroy any widgets in widgetTracker
         *
         * */
        function _customClean() {
            datasetMemories = [];
            datasetArray = [];
            aliasMap = [];
            hdUnionFiles = [];

            isHadoopSet = false;
            domStyle.set(dom.byId(constants.FIRST_TABLE_CONTAINER), "visibility", "hidden");
            domConstruct.empty(dom.byId(constants.SEL_TYPE_CONTAINTER));
        }

        /*
         *  _fillNewRow
         *  tell _addRow how to create a new row, e.g. a single function for each column type
         *  @param item: row property object
         *  @param row: row dom
         *  @param rowIndex: unique row id
         *
         * */
        function _fillNewRow(item, row, rowIndex) {
            /* create alias textbox */
            _createAliasColumn(item, row, rowIndex);
            for (var k=0;k<datasetArray.length;k++) {
                _createColumnListDropdown(item[datasetArray[k]],datasetArray[k],row,rowIndex);
            }
            inlineEditor.createDeleteButton(row, rowIndex);
        }

        function _createAliasColumn(item, row, rowIndex) {
            var validTB = new ValidationTextBox({
                uniqueType: constants.COL_IDS.ALIAS,
                baseClass: "inlineTextbox",
                placeHolder: "Alias",
                value: item[constants.ALIAS],
                required: true,
                regExp: validationHelper.columnName,
                isValid: function() {
                    var val = this.get('value');
                    if (!this.validator(val)) {
                        this.invalidMessage = alpine.nls.set_op_alias_validate_msg_invalid;
                        return false;
                    }
                    var colNames = _currentlyConfiguredCols();
                    var counter = 0;
                    for (var k=0;k<colNames.length;k++) {
                        if (colNames[k] == val) {counter++;}
                    }
                    this.invalidMessage = alpine.nls.set_op_alias_validate_msg_duplicate;
                    return (counter < 2);
                }
            });
            inlineEditor.putWidgetInCol(row,validTB);
            domClass.add(validTB.domNode,constants.COL_NAME_CLASS);
        }

        function _createColumnListDropdown(columnName, tableName, row, rowIndex) {
            var filterSel = new FilteringSelect({
                uniqueType: constants.COL_IDS.COL_NAME,
                baseClass: "inlineDropdownButton",
                style: "width:170px;",
                placeHolder: alpine.nls.inline_edit_choose_column,
                store: datasetMemories[tableName],
                tableName: tableName,
                value: columnName
            });
            filterSel.startup();
            inlineEditor.putWidgetInCol(row,filterSel);
            on.once(filterSel, "change", function(val) {
                query("."+constants.COL_NAME_CLASS, dom.byId(inlineEditor.tableRow + rowIndex)).forEach(function(node) {
                    var alias = registry.getEnclosingWidget(node);
                    if(alias.get('value')=="") {alias.set('value', val);}
                });
            });
        }

        /*
         *  _currentlyConfiguredCols
         *  utility to get current aliases for validation
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
            var setObj = {};
            var rowCounter = 0;
            allRows.forEach(function(doms){
                setObj = {};
                setObj[constants.GRID_ROW_ID] = rowCounter;
                rowCounter++;
                array.forEach(registry.findWidgets(doms),function(widget){
                    switch (widget.get('uniqueType')) {
                        case constants.COL_IDS.ALIAS:
                            setObj[constants.ALIAS] = widget.get('value');
                            break;
                        case constants.COL_IDS.COL_NAME:
                            setObj[widget.get('tableName')] = widget.get('value');
                            break;
                        default:
                            break;
                    }

                });
                returnArray.push(setObj);
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
                popupComponent.alert(alpine.nls.set_op_form_error);
                return;
            }
            if (isHadoopSet) {
                var outputColumns = _buildHadoopReturnArray();
                if (!outputColumns||outputColumns.length==0) {
                	return;
                }
                returnProp.hadoopUnionModel = {
                    outputColumns: outputColumns,
                    unionFiles: _buildUnionFiles(),
                    firstTable: registry.byId(constants.FIRST_TABLE).get('value'),
                    setType: registry.byId(constants.SEL_TYPE).get('value') 
                };
                if (returnProp.hadoopUnionModel.outputColumns.length < 1) {
                    popupComponent.alert(alpine.nls.histogram_alert_nocolumn);
                    return;
                }
            } else {
                returnProp.tableSetModel = {
                    datasetList: _buildReturnArray(),
                    firstTableName: registry.byId(constants.FIRST_TABLE).get('value'),
                    type: registry.byId(constants.SEL_TYPE).get('value'),
                    aliasMap: returnProp.tableSetModel.aliasMap,
                    inputTables: returnProp.tableSetModel.inputTables
                };
                if (returnProp.tableSetModel.datasetList.length < 1) {
                    popupComponent.alert(alpine.nls.histogram_alert_nocolumn);
                    return;
                }
            }
            setButtonBaseClassValid(getSourceButtonId(returnProp));
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
            inlineEditor.clean();
            inlineEditor = {};
        }

        /**********************Hadoop Init*********************************/
        function _showHadoopSetOpDialog(prop) {
            //console.log("===============SET OP DIALOG for HADOOP================");

            if (_initHadoopSetOpDialog(prop)) {
                registry.byId(constants.DIALOG_ID).show();
            } else {
                inlineEditor.clean();
                inlineEditor = {};
            }
        }

        function _initHadoopSetOpDialog(prop) {
            inlineEditor = new inlineEdit({
                newRowButtonId: constants.BTN_ADD_NEW,
                tableContainer: constants.TABLE_CONTAINER,
                _fillNewRow: _fillNewRow,
                _getDefaultItem: _getDefaultItem,
                _customClean: _customClean
            });

            isHadoopSet = true;
            returnProp = prop;
            var dataItems = lang.clone(prop.hadoopUnionModel);
            if (!dataItems.outputColumns) {
            	dataItems.outputColumns = [];
            }
            var inputFilesInfo = CurrentOperatorDTO.inputFileInfos;
            if (!multiFileUtil.validateHadoopInputFiles(inputFilesInfo)) {return false;}
            var tableColumnsList = _getHadoopMetadataList(inputFilesInfo);
            _setupHadoopType(dataItems.setType);

            _setupHadoopFirstTableSelect(tableColumnsList, dataItems.firstTableName);
            var headerInfo = _createHeaderInfo(tableColumnsList);
            var fileSwap = _inplaceHadoopMapping(tableColumnsList,dataItems.outputColumns);
            var rowObjects = _buildHadoopRowObjects(dataItems.outputColumns,fileSwap);
     
            _setupColumnListMemories(tableColumnsList);
            inlineEditor.createTable(headerInfo, rowObjects);
            _enableMagicButton();
            return true;
        }

        function _inplaceHadoopMapping(tableColumnsList, outputColumns) {
            if (outputColumns.length < 1) {return null;}
            var inputSet = [];
            var configSet = [];
            array.forEach(tableColumnsList, function(datasetInfo){
                inputSet.push(datasetInfo.tableName);
            });
            array.forEach(outputColumns[0].mappingColumns,function(inputs){
                configSet.push(inputs.operatorModelID);
            });

            var configNotFound = _findMissing(configSet, inputSet);
            var inputNotFound = _findMissing(inputSet, configSet);

            if (configNotFound.length == 1 && inputNotFound.length == 1) {
                return [
                    configNotFound[0],
                    inputNotFound[0]
                ];
            } else {
                return null;
            }

            function _findMissing(setOne, setTwo) {
                var found = false;
                var output = [];
                array.forEach(setOne, function(oneUid){
                    array.forEach(setTwo, function(twoUid){
                        if (oneUid == twoUid) {
                            found = true;
                        }
                    });
                    if (!found) {output.push(oneUid);}
                    found = false;
                });
                return output;
            }

        }

        function _getHadoopMetadataList(inputFiles) {
            var metadataInfoList = [];
            for(var i=0;i<inputFiles.length;i++){
                var inputFileInfo = inputFiles[i];
                var columnNames = inputFileInfo.columnInfo.columnNameList;
                var columnTypes = inputFileInfo.columnInfo.columnTypeList;
                var columnList = [];
                for (var k=0;k<columnNames.length;k++) {
                    columnList.push([columnNames[k],columnTypes[k]]);
                }
                var hdOpPrimaryInfo = opManager.getOperatorPrimaryInfo(inputFileInfo.operatorUUID);
                metadataInfoList.push({
                    schema: "",
                    pureTableName: inputFileInfo.hadoopFileName,
                    tableName: hdOpPrimaryInfo.uid,
                    displayName: hdOpPrimaryInfo.name,
                    columns: columnList
                });
                hdUnionFiles.push({
                    file: inputFileInfo.hadoopFileName,
                    operatorModelID: hdOpPrimaryInfo.uid
                });
            }
            return metadataInfoList;
        }

        function _buildHadoopRowObjects(outputColumns,fileSwap) {
      
            var rowObjs = [];
            for (var i=0;i<outputColumns.length;i++) {
                var rowObj = {};
                rowObj[constants.GRID_ROW_ID] = "";
                rowObj[constants.ALIAS] = outputColumns[i].columnName;
                var mapping = outputColumns[i].mappingColumns;
                for (var k=0;k<outputColumns[i].mappingColumns.length;k++) {
                    var identifier;
                    if (fileSwap && fileSwap[0] == mapping[k].operatorModelID) {
                        identifier = fileSwap[1];
                    } else {
                        identifier = mapping[k].operatorModelID;
                    }
                    rowObj[identifier] = mapping[k].columnName;
                }
                rowObjs.push(rowObj);
            }
            return rowObjs;
        }

        function _setupHadoopType( setType) {
            dom.byId(constants.SEL_DESC).innerHTML = alpine.nls.set_op_unionall_msg;
            //Maybe later, doesn't look great...
            //domConstruct.create("span",{innerHTML:"UNION",style:"vertical-align:middle;color:#999999;"},dom.byId(constants.SEL_TYPE_CONTAINTER));
            var unionTypeSel = new Select({
                id: constants.SEL_TYPE,
                baseClass:"greyDropdownButton",
                style: "width:auto;",
                options:[
                    {label:"UNION",value:"UNION", selected:true},
                    {label:"UNION ALL",value:"UNION ALL"},
                    {label:"INTERSECT",value:"INTERSECT"},
                    {label:"EXCEPT",value:"EXCEPT"}
                ]
            },domConstruct.create("div",{},dom.byId(constants.SEL_TYPE_CONTAINTER)));
            _setTypeDescHelper(unionTypeSel.get('value'));
            on(unionTypeSel, "change", _setTypeDescHelper);
            if(setType){
            	unionTypeSel.set('value',setType);
            }else{
            	unionTypeSel.set('value',"UNION ALL");
            }
            inlineEditor.addToTracker(unionTypeSel);
 
        
        }
       
    //	type and first table...
        function _buildHadoopReturnArray() {
            var allRows = inlineEditor.returnRows();
            var returnArray = [];
            var setObj = {};
            var mappingColumns = [];
            var dataTypeArray = [];
            var compatibilityErrors = [];
            var compatibilityRows = [];
            allRows.forEach(function(doms){
                setObj = {};
                mappingColumns = [];
                dataTypeArray = [];
                array.forEach(registry.findWidgets(doms),function(widget){
                    switch (widget.get('uniqueType')) {
                        case constants.COL_IDS.ALIAS:
                            setObj[constants.HD_OUT.NAME] = widget.get('value');
                            setObj[constants.HD_OUT.TYPE] = "";
                            break;
                        case constants.COL_IDS.COL_NAME:
                            var tempObj = {};
                            var myUUID = widget.get('tableName');
                            var myName = widget.get('value');
                            var myType = datasetMemories[myUUID].query({id:myName})[0]['type'];
                            dataTypeArray.push(myType);
                            tempObj[constants.HD_OUT.NAME] = myName;
                            tempObj[constants.HD_OUT.TYPE] = myType;
                            tempObj[constants.HD_OUT.UUID] = myUUID;
                            mappingColumns.push(tempObj);
                            break;
                        default:
                            break;
                    }
                });
                if(!hdTypeUtil.areHDCompatible(dataTypeArray)) {
                    compatibilityErrors.push(setObj[constants.HD_OUT.NAME]);
                    compatibilityRows.push(doms);
                }
                setObj[constants.HD_OUT.TYPE] = _computeTypeFromArray(dataTypeArray);
                setObj[constants.HD_OUT.MAPPING] = mappingColumns;
       //don't need this any more ...        
//                for(var i = 0 ;i < mappingColumns.length;i++){
//                 	for(var j =0;j<mappingColumns.length;j++){
//                		if(i!=j&&_haveSameInputFile(mappingColumns[i].operatorModelID,mappingColumns[j].operatorModelID)){
////                			if(mappingColumns[i][constants.HD_OUT.NAME] != mappingColumns[j][constants.HD_OUT.NAME]){
////                				popupComponent.alert("Same file can not have different column in same output column: "+mappingColumns[i][constants.HD_OUT.NAME]+" and " + mappingColumns[j][constants.HD_OUT.NAME]);
////                				//     _highlightErrorRows(compatibilityRows);
////                	                return null;
////                			}
//                		}
//                	}
//                }
                 
                
                returnArray.push(setObj);
            });
            if (compatibilityErrors.length > 0) {
                _raiseCompatibilityErrors(compatibilityErrors);
                _highlightErrorRows(compatibilityRows);
                return null;
            }
             
             
            
            return returnArray;
        }
        function _haveSameInputFile(modelID1,modelID2){
        	var inputFile1 = null;
        	var inputFile2 = null;
        	
        	for(var i =0;i<hdUnionFiles.length;i++){
        		if(modelID1==hdUnionFiles[i].operatorModelID){
        			inputFile1 = hdUnionFiles[i].file;
        		}
        		if(modelID2==hdUnionFiles[i].operatorModelID){
        			inputFile2 = hdUnionFiles[i].file;
        		}
        	}
        	return inputFile1==inputFile2;
        }

        function _buildUnionFiles() {
            return hdUnionFiles;
        }

        function _computeTypeFromArray(dataTypes) {
            /*console.log("new:" + hdTypeUtil.getResultColumnType(dataTypes));
            console.log("old:" + hdTypeUtil.guessColumnType(dataTypes));*/
            return hdTypeUtil.getResultColumnType(dataTypes);
        }

        function _highlightErrorRows(errors) {
            for (var i=0;i<errors.length;i++) {
                domClass.add(errors[i], 'inlineRowError');
                on.once(errors[i],'click',function(evt){
                    domClass.remove(evt.currentTarget,'inlineRowError');
                });
            }
        }

        function _raiseCompatibilityErrors(errors) {
            popupComponent.alert(alpine.nls.set_op_datatype_error);
        }

        /************************ Magic Button ************************/

        function _enableMagicButton() {

            var buttons = [
                {
                    id: constants.MAGIC.order,
                    label: alpine.nls.set_op_magic_order,
                    type:"order"
                },
                {
                    id: constants.MAGIC.nameStrict,
                    label: alpine.nls.set_op_magic_name_all,
                    type:"nameStrict"
                }
            ];
            if (datasetArray.length > 2) {
                buttons.push({
                    id: constants.MAGIC.name,
                    label: alpine.nls.set_op_magic_name,
                    type:"name"
                });
            }
            var ttContent = domConstruct.create("div");
            array.forEach(buttons,function(btn){
                var line = domConstruct.create("div",{style:"padding:2px;"},ttContent);
                var btnWidget = new Button({
                    id: btn.id,
                    baseClass: "lightLinkBtn",
                    label: btn.label
                },domConstruct.create("div",{},line));
                inlineEditor.addToListeners(on(btnWidget, "click", function(){
                    _doMagicColumnMapping(btn.type);
                }));
            });
            var magicTTDialog = new inlineEditTTDialog({
                content: ttContent
            });
            var magicDD = new DropDownButton({
                baseClass: "linkButton",
                dropDown: magicTTDialog,
                label: alpine.nls.set_op_magic_label
            },domConstruct.create("div",{},constants.MAGIC.dialogOpen));
            inlineEditor.addToTracker(magicDD);
        }

        function _doMagicColumnMapping(type) {
            var minLength = Infinity;
            var maxLength = -1;
            var minKey, maxKey, tempLength;
            array.forEach(datasetArray,function(name){
                tempLength = datasetMemories[name].query().length;
                if (tempLength < minLength) {
                    minLength = tempLength;
                    minKey = name;
                }
                if (tempLength > maxLength) {
                    maxLength = tempLength;
                    maxKey = name;
                }
            });
            var rowObjects;
            switch (type) {
                case "order":
                    rowObjects = _matchByOrder(minLength);
                    break;
                case "name":
                    rowObjects = _matchByNameInTwo();
                    break;
                case "nameStrict":
                    rowObjects = _matchByName(maxKey);
                    break;
                default:
                    break;
            }
            inlineEditor.rebuildRows(rowObjects);
        }

        function _matchByOrder(minLength) {
            var rowObjects = [];
            for (var k=0;k<minLength;k++) {
                var rowObject = {};
                rowObject[constants.GRID_ROW_ID] = "";
                rowObject[constants.ALIAS] = "";
                array.forEach(datasetArray,function(name){
                    var colName = datasetMemories[name].query()[k]["id"];
                    if(rowObject[constants.ALIAS] == "") {rowObject[constants.ALIAS] = colName}
                    rowObject[name] = colName;
                });
                rowObjects.push(rowObject);
            }
            return rowObjects;
        }

        function _matchByName(minOrMaxKey) {
            var minMem = datasetMemories[minOrMaxKey].query();
            var rowObjects = [];
            array.forEach(minMem,function(col){
                var rowObject = {};
                var colName = col["id"];
                var containsANoMatch = false;
                rowObject[constants.ALIAS] = colName;
                rowObject[constants.GRID_ROW_ID] = "";
                rowObject[minOrMaxKey] = colName;
                array.forEach(datasetArray,function(name){
                    if (minOrMaxKey != name) {
                        var matchMem = datasetMemories[name].query();
                        var memName = name;
                        var localMatch = false;
                        array.some(matchMem,function(mem){
                            if(mem["id"].toLowerCase() == colName.toLowerCase()) {
                                rowObject[memName] = mem["id"];
                                localMatch = true;
                                return true;
                            }
                            return false;
                        });
                        if (!localMatch) {containsANoMatch = true;}
                    }
                });
                if (!containsANoMatch) {
                    rowObjects.push(rowObject);
                }
            });
            return rowObjects;
        }

        function _matchByNameInTwo() {
            var allCols = [];
            array.forEach(datasetArray,function(name){
                var keyMem = datasetMemories[name].query();
                array.forEach(keyMem,function(uCol){
                    allCols.push(uCol['id']);
                });
            });
            var uniqueCols = _getUniqueArray(allCols);
            var rowObjects = [];
            array.forEach(uniqueCols,function(colName){
                var rowObject = {};
                rowObject[constants.ALIAS] = colName;
                rowObject[constants.GRID_ROW_ID] = "";
                var counter = 0;
                array.forEach(datasetArray,function(key){
                    var datasetMem = datasetMemories[key].query();
                    array.some(datasetMem,function(col){
                        var checkName = col['id'];
                        if (checkName.toLowerCase()==colName.toLowerCase()) {
                            rowObject[key] = checkName;
                            counter++;
                            return true;
                        }
                        return false;
                    });
                });
                if (counter > 1) {rowObjects.push(rowObject);}
            });
            return rowObjects;


            function _getUniqueArray(arr){
                var test = {};
                return array.filter(arr, function(val){
                    return test[val.toLowerCase()] ? false : (test[val.toLowerCase()] = true);
                });
            }
        }

        return {
            showDialog: _showSetOpDialog,
            showHadoopDialog: _showHadoopSetOpDialog
        }
    });
