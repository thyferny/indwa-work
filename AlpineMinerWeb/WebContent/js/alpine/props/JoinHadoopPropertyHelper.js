/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopJoinHelper
 * Author: Will
 * Date: 12-7-17
 */
define(["alpine/flow/OperatorManagementManager",
    "alpine/operatorexplorer/OperatorUtil",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/flow/HadoopOperatorsDataExplorerManager",
    "dijit/form/FilteringSelect",
    "dojo/data/ObjectStore",
    "dojo/store/Memory",
    "dojo/on",
    "dojo/has",
    "dijit/registry",
    "dojo/dom",
    "dojo/dom-style",
    "dojo/dom-construct",
    "dojo/string",
    "dojo/dom-class",
    "dojo/ready",
    "dojo/_base/lang",
    "dojo/_base/event",
    "dojo/_base/array",
    "dojo/_base/declare",
    "dojo/query",
    "dojox/grid/EnhancedGrid",
    "alpine/props/MultiFileUtil",
    "alpine/props/JoinPropertyHelper"

],
    function (operatorManager, operatorUtil, hdDataTypeUtil, dataExplorerMgmt, Select, ObjectStore, Memory, on, has, registry, dom, domStyle, domConstruct, string, domClass, ready, lang, event, array, declare, query, EnhancedGrid, multiFileUtil, joinHelper) {
        //dom ids
        var condition_grid = "condition_grid";
        var hadoop_select_join_column_grids_container = "hadoop_select_join_column_grids_container";
        var selectJoinType = "hadoop_join_config_choose_condition_type";

        //variable
        var inputFileInfos = null;
        var hadoopJoinModel = null;
        var sourceButtonId = null;

        //actual objects
        var newColumnSelectGrid = null;
        var columnDataStore = null;


        var constants = {
            DIALOG_ID:"hadoopJoinDialog",
            BTN_OK:"hadoopJoin_dlg_btn_OK",
            BTN_CANCEL:"hadoopJoin_dlg_btn_Cancel",
            joinSelectorsDiv:"joinSelectors",
            selectorIdPrefix:"hj_sel_",
            selected_columns_holder:"hadoop_join_selected_columns_holder",
            hadoop_join_helper_text:"hadoop_join_helper_text",
            first_table_join_option:"hj_left",
            second_table_join_option:"hj_right",
            first_table_join_option_label:"hj_left_label",
            second_table_join_option_label:"hj_right_label",
            tableFilterPanel:"hadoop_join_tables_toggle",
            PREFIX_INPUT_BUTTON:"input_button_",
            COLUMN_DISPLAY_BUTTON_PANEL_INNER:"hadoop_join_tables_toggle_inner",
            DIV_NUM_SEL_COLUMNS:"div_number_sel_columns",
            ALL_PREFIX:"all_",
            NONE_PREFIX:"none_"
        }

        var joins = joinHelper.joins;


        ready(function () {
            on(registry.byId(constants.BTN_OK), "click", saveJoinConfig);
            on(registry.byId(constants.BTN_CANCEL), "click", cancelJoinDlg);
            on(registry.byId(constants.DIALOG_ID), "hide", _destroySthOnDlgHide);

        });


        function showHadoopJoinDialog() {
            var dlg = registry.byId(constants.DIALOG_ID);
            if (!multiFileUtil.validateHadoopInputFiles(inputFileInfos)) {return false;}
            if(multiFileUtil.validateHadoopInputFiles(inputFileInfos)==false){return false;}
            if (dlg != null) {
                dlg.show();
                _buildSelectColumnGrids();
                _buildColumnFilterPanel();
                _buildSelectors(hadoopJoinModel.joinConditions);


                if (columnDataStore) {
                    var total = columnDataStore.query({selected:true}).total;
                    if (typeof total == "object") total = 0;

                    fillSelectedFieldsHTML(total);
                }

                if (inputFileInfos.length != 2) {
                    domStyle.set("hj_join_options", "display", "none");
                    hadoopJoinModel.joinType = joins.INNER;

                } else {
                    dom.byId(constants.first_table_join_option_label).innerHTML = string.substitute(alpine.nls.hadoop_join_option, {
                        tableName:alpine.flow.OperatorManagementManager.getOperatorPrimaryInfo(inputFileInfos[0].operatorUUID).name});
                    dom.byId(constants.second_table_join_option_label).innerHTML = string.substitute(alpine.nls.hadoop_join_option, {
                        tableName:alpine.flow.OperatorManagementManager.getOperatorPrimaryInfo(inputFileInfos[1].operatorUUID).name});

                    domStyle.set("hj_join_options", "display", "block");
                    setJoinTypeStatus(hadoopJoinModel.joinType);
                }

            }
        }


        function getJoinType() {
            if (inputFileInfos.length != 2)
                return joins.INNER;


            var firstTableChecked = registry.byId(constants.first_table_join_option).get("checked");
            var secondTableChecked = registry.byId(constants.second_table_join_option).get("checked");

            if (firstTableChecked && secondTableChecked) return joins.OUTER;
            if (firstTableChecked) return joins.LEFT;
            if (secondTableChecked) return joins.RIGHT;
            return joins.INNER;
        }

        function setJoinTypeStatus(joinType) {
            var leftCheckbox = registry.byId(constants.first_table_join_option);
            var rightCheckbox = registry.byId(constants.second_table_join_option);

            leftCheckbox.set("checked", false);
            rightCheckbox.set("checked", false);
            if (inputFileInfos.length != 2) return;
            var joinConditions = hadoopJoinModel.joinConditions;
            var primaryOnLeft = true;
            if (joinConditions != null && joinConditions.length > 0) {
                //left is the first one:
                var LeftOperatorUUID = joinConditions[0].fileId;
                if (inputFileInfos[0].operatorUUID != LeftOperatorUUID) {
                    primaryOnLeft = false;
                }
            } else {
                return;   //haven't chosen left or right
            }


            var joinTypeWidget = registry.byId(selectJoinType);
            if (joinType == joins.OUTER) {
                leftCheckbox.set("checked", true);
                rightCheckbox.set("checked", true);
            } else if (joinType == joins.LEFT) {
                if (primaryOnLeft)  leftCheckbox.set("checked", true);
                else rightCheckbox.set("checked", true);
            } else if (joinType == joins.RIGHT) {
                if (primaryOnLeft)  rightCheckbox.set("checked", true);
                else leftCheckbox.set("checked", true);

            }
        }

        function initHadoopJoinModelAndInputFilesVariable(prop) {
            hadoopJoinModel = lang.clone(prop.hadoopJoinModel);
            inputFileInfos = CurrentOperatorDTO.inputFileInfos;
            sourceButtonId = getSourceButtonId(prop);
        }

        function getHadoopJoinModel() {
            return hadoopJoinModel;
        }

        function clearHadoopJoinModel() {
            hadoopJoinModel = null;
        }

        function saveJoinConfig() {

            if (false == _validateColumnSelected()) {
                popupComponent.alert(alpine.nls.hadoop_join_select_grid_tip);
                return false;
            }

            if (true == _validateConditionColumnStartWith()) {
                return false;
            }

            if (true == _validateOutputColumnSame()) {
                //have same
                popupComponent.alert(alpine.nls.hadoop_join_condition_key_column_same_error_tip);
                return false;
            }
            if (false == _validateJoinConditionGrid()) {
                popupComponent.alert(alpine.nls.hadoop_join_condition_tip);
                return false;
            }

            if (false == _validateConditionColumnType()) {
                popupComponent.alert(alpine.nls.hadoop_join_condition_column_type_error_tip);
                return false;
            }

            hadoopJoinModel.joinType = getJoinType();


            _saveJoinTable4Model(hadoopJoinModel);
            _saveCondition4Model(hadoopJoinModel);
            _saveJoinColumn4Model(hadoopJoinModel);
            setButtonBaseClassValid(sourceButtonId);
            hideJoinConfigDlg();
        }

        function _saveJoinTable4Model(joinModel) {
            var joinTables = [];
            if (null != inputFileInfos) {
                for (var i = 0; i < inputFileInfos.length; i++) {
                    var inputFile = inputFileInfos[i];
                    if (null != inputFile) {

                        var parentFileName = inputFile.hadoopFileName;
                        var operatorPrimaryInfo = operatorManager.getOperatorPrimaryInfo(inputFile.operatorUUID);
                        if (operatorPrimaryInfo.classname != "HadoopFileOperator") {
                            if (null != parentFileName) {
                                parentFileName = parentFileName.substring((parentFileName.lastIndexOf("/") + 1));
                            }
                        }
                        joinTables.push({
                                file:parentFileName,
                                operatorModelID:inputFile.operatorUUID
                            }
                        );
                    }

                }
            }
            joinModel.joinTables = joinTables;

        }

        /**
         * Saves the join conditions
         * @param joinModel
         * @private
         */
        function _saveCondition4Model(joinModel) {
            var joinConditions = [];

            for (var i = 0; i < inputFileInfos.length; i++) {
                var selectObject = dijit.byId(constants.selectorIdPrefix + inputFileInfos[i].operatorUUID);
                var conditionItem = selectObject.get("item");
                joinConditions.push({
                    fileId:conditionItem.fileId,
                    keyColumn:conditionItem.columnName,
                    keyColumnAlias:conditionItem.columnAlias
                });
            }

            joinModel.joinConditions = joinConditions;

        }

        /**
         * Saves the selected output columns
         * @param joinModel
         * @private
         */
        function _saveJoinColumn4Model(joinModel) {
            var joinColumns = [];
            columnDataStore.query({ selected:true }).forEach(function (item) {
                joinColumns.push({
                    columnName:item.columnName,
                    columnType:item.columnType,
                    fileId:item.fileId,
                    newColumnName:item.columnAlias
                });
            });
            joinModel.joinColumns = joinColumns;
        }


        /**
         * Confirms that one column per input table has been chosen for the join
         * @return {Boolean}
         * @private
         */
        function _validateJoinConditionGrid() {
            for (var i = 0; i < inputFileInfos.length; i++) {
                var selectObject = dijit.byId(constants.selectorIdPrefix + inputFileInfos[i].operatorUUID);
                if (!selectObject.validate()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Confirms that the columns chosen for the join are of the same datatype.
         * @return {Boolean}
         * @private
         */
        function _validateConditionColumnType() {
            var firstDataType = "";

            for (var i = 0; i < inputFileInfos.length; i++) {
                var selectObject = registry.byId(constants.selectorIdPrefix + inputFileInfos[i].operatorUUID);
                var item = selectObject.get("item");
                if (i == 0) {
                    firstDataType = item.columnType;
                } else {
                    if (!hdDataTypeUtil.isSimilarType(firstDataType, item.columnType)) {
                        return false;
                    }
                }
            }
            return true;
        }

        function _validateConditionColumnStartWith() {
            var keyColumns = [];

            var storeItems = columnDataStore.objectStore.data;
            if (null != storeItems) {
                for (var i = 0; i < storeItems.length; i++) {
                    var item = storeItems[i];
                    if (null != item) {
                        if (item.columnAlias == null || item.columnAlias.trim() == "") {
                            popupComponent.alert(alpine.nls.hadoop_join_need_alias);
                            return true; //can't have empty aliases
                        }
                        keyColumns.push(item.columnAlias);
                    }
                }
            }

            keyColumns.sort();
            /*
             for(i=0;i<keyColumns.length-1;i++){
             if(keyColumns[i]==keyColumns[i+1]){
             popupComponent.alert(alpine.nls.hadoop_join_condition_key_column_same_error_tip);
             return true;
             }
             }
             */
            var reg = /^[a-zA-Z][\w]*/;
            for (i = 0; i < keyColumns.length; i++) {
                if (reg.test(keyColumns[i]) == false) {
                    popupComponent.alert(alpine.nls.hadoop_join_condition_key_column_start_number_error_tip);
                    return true;
                }
            }
            return false;

        }


        function _validateOutputColumnSame() {
            var aliasNames = [];
            columnDataStore.query({selected:true}).forEach(function (item) {
                aliasNames.push(item.columnAlias);
            });
            aliasNames.sort();
            for (i = 0; i < (aliasNames.length - 1); i++) {
                if (aliasNames[i] == aliasNames[i + 1]) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Confirms that user has selected at least one output table
         * @return {Boolean}
         * @private
         */
        function _validateColumnSelected() {
            //validate new column store
            if (columnDataStore) {
                var numColumnsSelected = columnDataStore.query({selected:true}).total;
                if (typeof numColumnsSelected == "object") numColumnsSelected = 0;

            }
            if (numColumnsSelected < 1) {
                popupComponent.alert(alpine.nls.Table_Join_Error_Join_Column_Empty);
                return false;
            }
            else
                return true;
        }


        function fillColumnGridDataStoreWithFileInfo(fileInfo, dataItems, joinColumns) {
            if (null != fileInfo && fileInfo.columnInfo != null && null != fileInfo.columnInfo.columnNameList) {
                var nameList = fileInfo.columnInfo.columnNameList;
                var typeList = fileInfo.columnInfo.columnTypeList;
                for (var i = 0; i < nameList.length; i++) {
                    var colAlias = _getColumnAlias(fileInfo, nameList[i]);
                    if (colAlias == null) {
                        colAlias = nameList[i];
                    }
                    var selected = false;
                    for (var j = 0; j < joinColumns.length; j++) {
                        if (joinColumns[j].columnName == nameList[i] && joinColumns[j].fileId == fileInfo.operatorUUID) {
                            selected = true;
                            break;
                        }
                    }

                    dataItems.push({
                        selected:selected,
                        id:fileInfo.operatorUUID + nameList[i], //this needs to be unique across all columns in all tables
                        columnName:nameList[i],
                        columnType:typeList[i],
                        columnAlias:colAlias,
                        fileInfo:fileInfo.hadoopFileName,
                        fileId:fileInfo.operatorUUID,
                        operatorName:alpine.flow.OperatorManagementManager.getOperatorPrimaryInfo(fileInfo.operatorUUID).name,
                        label:nameList[i],
                        value:fileInfo.operatorUUID + nameList[i]
                    });
                }

            }
            ;
        }

        function _buildColumnFilterPanel() {
            var inner = domConstruct.create("div", {id:constants.COLUMN_DISPLAY_BUTTON_PANEL_INNER }, dijit.byId(constants.tableFilterPanel).domNode);

            //First we need to list the output info
            var outerTableTitle = domConstruct.create("div", { innerHTML:alpine.nls.Table_Join_Output_Table}, inner);
            domClass.add(outerTableTitle, "tablejointableheader");
            var outputButtonHolder = domConstruct.create("div", { id:"OutputHolder"}, inner);
            createTableSelectObject(outputButtonHolder, true);


            //now do all the input tables
            var innerTableTitle = domConstruct.create("div", { innerHTML:alpine.nls.Table_Join_Input_Tables}, inner);
            domClass.add(innerTableTitle, "tablejointableheader");
            //Then for all inputs, create one.
            for (var i = 0; i < inputFileInfos.length; i++) {
                var operatorInfo = operatorManager.getOperatorPrimaryInfo(inputFileInfos[i].operatorUUID);
                var inputButtonHolder = domConstruct.create("div", { id:constants.PREFIX_INPUT_BUTTON + i}, inner);
                createTableSelectObject(inputButtonHolder, false, i, operatorInfo, inputFileInfos);
            }
        }


        function createTableSelectObject(holder, outer, index, operatorInfo, inputTables) {
            domClass.add(holder, "selectableDiv");
            domClass.add(holder, "thisgroup");

            var imgKey = "TableJoinOperator";
            var tableName = "outer";
            if (current_op) tableName = current_op.name;
            // var tableName =  current_tableJoin.opInfo.name;
            if (!outer) {
                imgKey = operatorInfo.classname;
                tableName = operatorInfo.name;
            }
            var theImag = domConstruct.create("img", { src:operatorUtil.getStandardImageSourceByKey(imgKey), style:{"float":"left"} }, holder);
            domClass.add(theImag, "small");
            var tableNameDiv =  domConstruct.create("div", { innerHTML:tableName }, holder);
            domClass.add(tableNameDiv, "tablejointablename");

            if (outer) {
               var numSelCol =  domConstruct.create("div", { id:constants.DIV_NUM_SEL_COLUMNS}, holder);
                domClass.add(numSelCol, "tablejoinsubfield");

            } else {

                var allNoneHolder = domConstruct.create("span", {}, holder);
                domClass.add(allNoneHolder, "allNoneHolder")
                var allButton = new dijit.form.Button({
                    id:constants.ALL_PREFIX + index,
                    baseClass:"linkButton",
                    onClick:function (e) {
                        event.stop(e); //we stop the event so we don't have two filters firing at the same time (one for selecting elements, one for choosing which input table)
                        var currentId = +this.id.substring(4);
                        setInputTableSelected(currentId, true);
                    },

                    label:alpine.nls.Table_Join_All_Button
                });


                allNoneHolder.appendChild(allButton.domNode);

                var divider = domConstruct.create("span", { style:"padding-right:0; color:#949599;", innerHTML:"|"}, allNoneHolder);
                domClass.add(divider, "tablejoinsubfield")

                var noneButton = new dijit.form.Button({
                    id:constants.NONE_PREFIX + index,
                    baseClass:"linkButton",
                    onClick:function (e) {
                        event.stop(e);   //we stop the event so we don't have two filters firing at the same time (one for selecting elements, one for choosing which input table)
                        var currentId = +this.id.substring(5);
                        setInputTableSelected(currentId, false);
                    },

                    label:alpine.nls.Table_Join_None_Button
                });

                allNoneHolder.appendChild(noneButton.domNode);

            }
            domConstruct.create("div", {style:{clear:"both"} }, holder);

            query(holder).onclick(function (e) {
              //  if (e.srcElement.nodeName == "INPUT" && e.srcElement.nodeName == "")   //NOTE: input elements send off two events - this is just removing the extra event.
              //  {
              //      console.log("eating the input event");
              //      return true;
              //  }
                var currentId = this.id;
                selectedTable(currentId, true, outer);
            });

        }

        function setInputTableSelected(aliasindex, allSelected) {
            var filterByUUID = inputFileInfos[aliasindex].operatorUUID;
            var currentId = constants.PREFIX_INPUT_BUTTON + aliasindex;
            columnDataStore.query({fileId:filterByUUID}).forEach(function (item) {
                columnDataStore.setValue(item, "selected", allSelected);
            });

            newColumnSelectGrid.filter({
                "selected":"*",
                "fileId":filterByUUID

            }, false);
            selectedTable(currentId);
        }

        function selectedTable(currentId, shouldFilter, outer) {
            query(".thisgroup").forEach(function (node) {

                if (node.id == currentId) {

                    //if this is already selected, then nothing should happen...
                    if (domClass.contains(currentId, "selectableDivSelected")) {
                        //console.log("already has the focus");
                    } else {
                        domClass.remove(node.id, "selectableDiv");
                        domClass.add(currentId, "selectableDivSelected");
                        if (shouldFilter) filterColumns(currentId, outer);

                    }

                } else {
                    domClass.add(node.id, "selectableDiv");
                    domClass.remove(node.id, "selectableDivSelected");

                }
            });
        }


        function filterColumns(whichFilter, output) {
            var showSelected = (output) ? "true" : "*";
            var filterByUUID = "*";
            if (!output) {
                var aliasindex = +whichFilter.substring(13);
                filterByUUID = inputFileInfos[aliasindex].operatorUUID;
            }

            newColumnSelectGrid.filter({
                "selected":showSelected,
                "fileId":filterByUUID

            }, false);

            newColumnSelectGrid.sort();
        }

        function fillSelectedFieldsHTML(numSelFields) {
            var text = alpine.nls.Table_Join_Fields.replace("{0}", numSelFields);
            if (numSelFields == 1) text = alpine.nls.Table_Join_Fields_One_Field;
            dom.byId(constants.DIV_NUM_SEL_COLUMNS).innerHTML = text;
        }

        function _buildSelectColumnGrids() {
            if (null != inputFileInfos && inputFileInfos.length > 0) {
                var dataItems = [];
                for (var i = 0; i < inputFileInfos.length; i++) {
                    var _legend = null;
                    var _fieldset = null;
                    var operatorInfo = operatorManager.getOperatorPrimaryInfo(inputFileInfos[i].operatorUUID);
                    dataExplorerMgmt.getCurrentOperatorPropertys4hdJoin_Set(operatorInfo, function (data) {
                        var status = _isStoreResults(data);
                        if (status == false && data.classname != "HadoopFileOperator") {
                            inputFileInfos[i].hadoopFileName = operatorInfo.name;
                        }
                    });

                    fillColumnGridDataStoreWithFileInfo(inputFileInfos[i], dataItems, hadoopJoinModel.joinColumns);
                }

                var store = new Memory({
                    idProperty:"value",
                    data:dataItems
                });

                columnDataStore = new ObjectStore({
                    objectStore:store,
                    onSet:  function (item, attribute, oldValue, newValue) {
                         if (attribute && attribute == "selected") {
                             numFieldsSelectedChanged();
                         }
                     }
                });

                newColumnSelectGrid = new EnhancedGrid({
                        store:columnDataStore,
                        structure:getSelectColumnGridLayout(),
                        //selectionMode:"single",
                        singleClickEdit:true,
                        canSort:function () {
                            return false;
                        },
                        onRowClick:function (e) {
                            return false;
                        },
                        onSelectionChanged:function () {
                            //this.selection.toggleSelect(event.rowIndex);
                        },
                        doclick:function (e)          //prevent alias validation textbox from appearing if not selected.
                        {
                            if (e.cellNode) {
                                var currentItem = this.getItem(e.rowIndex);
                                if (currentItem.selected || e.cellIndex != 2)
                                    this.onCellClick(e);
                                else
                                    this.onRowClick(e);
                            } else {
                                this.onRowClick(e);
                            }
                        }

                    },
                    domConstruct.create('div', {}, constants.selected_columns_holder));
                var selectedItems = [];
                newColumnSelectGrid.startup();

            }
        }

        function numFieldsSelectedChanged() {
            if (columnDataStore) {
                var total = columnDataStore.query({selected:true}).total;
                if (typeof total == "object") total = 0;

                fillSelectedFieldsHTML(total);
            }
        }

        function _buildSelectors(joinConditions) {
            array.forEach(dijit.findWidgets(dom.byId(constants.joinSelectorsDiv)), function (w) {
                w.destroyRecursive();
            });
            domConstruct.empty(constants.joinSelectorsDiv);

            var table = domConstruct.create("table", {}, constants.joinSelectorsDiv);
            var tableBody = domConstruct.create("tbody", {}, table);
            for (var i = 0; i < inputFileInfos.length; i++) {
                var accRow = domConstruct.create("tr", {}, tableBody);

                var operatorInfo = operatorManager.getOperatorPrimaryInfo(inputFileInfos[i].operatorUUID);
                var opNameDiv = domConstruct.create("td", { innerHTML:operatorInfo.name}, accRow);
                domClass.add(opNameDiv,"listHeader");

                var selCell = domConstruct.create("td", {}, accRow);
                domClass.add(selCell, "listValueTd");
                var selectID = constants.selectorIdPrefix + inputFileInfos[i].operatorUUID;
                var s = new Select({
                    id:selectID,
                    baseClass:"inlineDropdownButton",
                    name:"select2",
                    searchAttr:"columnName", //tells you what shows up in the dropdown.
                    //placeHolder:"Please select a column",
                    store:columnDataStore,
                    query:{fileId:inputFileInfos[i].operatorUUID}

                }).placeAt(selCell, "last");
                s.startup();

                //s.set( 'value', " ");
                for (var j = 0; j < joinConditions != null && j < joinConditions.length; j++) {
                    if (joinConditions[j].fileId == inputFileInfos[i].operatorUUID) {
                        console.log('setting value to: ' + joinConditions[j].keyColumn);
                        s.set('value', inputFileInfos[i].operatorUUID + joinConditions[j].keyColumn);
                    }
                }
            }


        }


        function _isStoreResults(data) {
            var isStoreResults = false;
            if (null != data && null != data.propertyList) {
                for (var i = 0; i < data.propertyList.length; i++) {
                    var prop = data.propertyList[i];
                    if (prop.name == "storeResults" && prop.value == "true") {
                        isStoreResults = true;
                        break;
                    } else if (prop.name == "storeResults" && prop.value == "false") {
                        isStoreResults = false;
                        break;
                    }
                }
            } else {
                isStoreResults = false;
            }
            return isStoreResults
        }


        function getSelectColumnGridLayout() {
            var checkType;
            if (has("mozilla")) {
                checkType = dojox.grid.cells.Bool;
            } else {
                checkType = joinHelper.getDojoCheckbox("hjpropinput_");
            }

            return [
               [
                   {
                       field:"selected",
                       name:" ",
                       width:"16px",
                       editable:true,
                       type: checkType
                   },
                   {
                       name:alpine.nls.hadoop_select_grid_column_availible_column,
                       field:'_item',
                       width:'60%',
                       formatter:function (value) {
                           var theName = value.columnName;
                           var shortOpName = (value.operatorName.length > 20) ? value.operatorName.substring(0, 20) + '...' : value.operatorName;
                           return "<div class=\"tablejointablecolumn\">[" + shortOpName + "] " + "<span class=\"tablejointablecolumnname\">" + theName + "</span></div>";
                       }
                   },
                   {
                       'name':alpine.nls.hadoop_select_grid_column_alias,
                       'field':'columnAlias',
                       editable:function (test) {
                           console.log("test");
                           return true;
                       },
                       type:dojox.grid.cells._Widget,
                       widgetClass:dijit.form.ValidationTextBox,
                       widgetProps:{
                           required:false,
                           regExp:"[a-zA-Z][\\w]*"
                       },
                       formatter:function (value, index, y) {
                           var item = newColumnSelectGrid.getItem(index);
                           if (item.selected) {
                               return "<span class='tablejointablecolumn'>Alias: " + value + "</span>";
                           } else {
                               return "<div style='display: none;'></div>";
                           }
                       },
                       width:'40%'
                   }
               ]
            ];
        }


        function _getColumnAlias(fileInfo, columnName) {
            if (hadoopJoinModel != null && hadoopJoinModel.joinColumns != null) {
                var joinColumns = hadoopJoinModel.joinColumns;
                for (var i = 0; i < joinColumns.length; i++) {
                    var columnObj = joinColumns[i];
                    if (fileInfo.operatorUUID == columnObj.fileId && columnName == columnObj.columnName) {
                        return columnObj.newColumnName != null ? columnObj.newColumnName : columnName;
                    }
                }
            }
        }


        function cancelJoinDlg() {
            hideJoinConfigDlg();
        }

        function hideJoinConfigDlg() {
            var dlg = registry.byId(constants.DIALOG_ID);
            if (dlg != null) {
                dlg.hide();
            }
        }

        function _destroyInside(dojoId) {
            array.forEach(registry.findWidgets(dom.byId(dojoId)), function (w) {
                w.destroyRecursive();
            });
            domConstruct.empty(dojoId);
        }

        function _destroySthOnDlgHide() {
            _destroyInside(constants.joinSelectorsDiv);
            _destroyInside(constants.selected_columns_holder);
            _destroyInside(constants.tableFilterPanel);
        }

        return {
            showHadoopJoinDialog:showHadoopJoinDialog,
            initHadoopJoinModelAndInputFilesVariable:initHadoopJoinModelAndInputFilesVariable,
            getHadoopJoinModel:getHadoopJoinModel,
            clearHadoopJoinModel:clearHadoopJoinModel
        }

    });