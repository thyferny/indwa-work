/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: VariablesDefine4VarOPHelper
 * Author: Will
 * Date: 13-1-16
 */
define([
    "dojo/dom-attr",
    "dojo/dom-style",
    "dojo/dom-class",
    "dojo/dom",
    "dojo/query",
    "dojo/on",
    "dojo/ready",
    "dijit/registry",
    "dojo/dom-construct",
    "dojo/_base/array",
    "dojo/string"
],
    function (domAttr, domStyle, domClass, dom, query, on, ready, registry, domContruct, array,stringUtil) {

        var domIds = {
            DIALOG:"variableDerivedConfigEditDialog",
            CancelBtn:"btn_close_4defineVariableProperty",
            OKBtn:"btn_save_4defineVariableProperty",
            SCRIPTINPUT:"var_sql_spec",
            VARIABLE_GRID:"variable_define_grid",
            VARIABLE_GRID_CONTAINER:"var_derived_gridpane",
            COLUMN_NAME_GRID:"varDerivedColNamesGrid",
            COLUMN_NAME_GRID_CONTAINER:"var_derived_list_table",
            DATA_TYPE_SELECT:"var_dev_data_type",
            EXPRESSION_TITLE:"sqlExpression_title",
            HELPER_BUTTON:"pigSyntaxHelperButton",
            HELPER_DIALOG:"pigSyntaxHelperDialog",
            BTN_CREATE_4VARIABLE:"var_derived_create_button",
            BTN_UPDATE_4VARIABLE:"var_derived_update_button",
            BTN_DELETE_4VARIABLE:"var_derived_delete_button",
            BTN_CLEAR_4VARIABLE:"var_derived_reset_button",
            INPUT_VARIABLE_NAME:"var_dev_result_column"
        };

        var myCodeMirror = null;
        var derivedFieldsModel = null; //model
        var fullColumns = null;
        var selectedColumns = null;

        ready(function () {
            on(registry.byId(domIds.OKBtn), "click", saveDefineResult);
            on(registry.byId(domIds.CancelBtn), "click", cancelDefineResult);

            on(registry.byId(domIds.BTN_CREATE_4VARIABLE), "click", create_var_derived);
            on(registry.byId(domIds.BTN_UPDATE_4VARIABLE), "click", update_var_derived);
            on(registry.byId(domIds.BTN_DELETE_4VARIABLE), "click", delete_var_derived);
            on(registry.byId(domIds.BTN_CLEAR_4VARIABLE), "click", resetVarDerivedForm);

            myCodeMirror = CodeMirror.fromTextArea(dom.byId(domIds.SCRIPTINPUT), {
                lineNumbers:true,
                matchBrackets:true,
                indentUnit:4,
                mode:"text/x-plsql",
                extraKeys:{"Ctrl-Space":"autocomplete", "Shift-Ctrl-Space":"autocomplete"}
            });
        });

        function showDefineVarDialog(prop) {
            var dlg = registry.byId(domIds.DIALOG);
            if (dlg != null) {
                dlg.titleBar.style.display = 'none';
                dlg.show();
                //init some var value
                var isHadoop = isHadoopOperator()
                initFieldsModel(prop, isHadoop);

                enable_var_derived_create();
                clear_var_derived_from();

                var dataItems = _getVariableDefine4Grid(derivedFieldsModel.derivedFieldsList);
                buildVariableDefineGrid(dataItems, isHadoop);
                buildSelectedColumnGrid(selectedColumns, fullColumns);
                initUIStatus(isHadoop);
            }
        }

        function initFieldsModel(prop, isHadoop) {
            if (prop.derivedFieldsModel == null) {
                prop.derivedFieldsModel = {};
                prop.derivedFieldsModel.derivedFieldsList = [];
                prop.derivedFieldsModel.selectedFieldList = [];
            }
            derivedFieldsModel = prop.derivedFieldsModel;
            selectedColumns = derivedFieldsModel.selectedFieldList;
            fullColumns = prop.fullSelection;
        }

        function initUIStatus(isHadoop) {
            var varDevDataSelect = registry.byId(domIds.DATA_TYPE_SELECT);
            if (isHadoop == true) {
                dom.byId(domIds.EXPRESSION_TITLE).innerHTML = alpine.nls.sqlExpression_title_java;
                registry.byId(domIds.HELPER_BUTTON).set('style', "display:block;");
                registry.byId(domIds.HELPER_DIALOG).titleBar.style.display = 'none';
                //Text-->chararray Numeric-->double Int-->int Float-->
                var dataTypes = alpine.props.HadoopDataTypeUtil.getAllHadoopTypes();
                //var dataTypes = ["int","long","float","double","chararray","bytearray"];
                var items = [];
                for (var i = 0; i < dataTypes.length; i++) {
                    items.push({label:dataTypes[i], value:dataTypes[i]});
                }
                varDevDataSelect.options = items;
                varDevDataSelect.startup();
                varDevDataSelect.set('value', dataTypes[1]);
                myCodeMirror.setOption("mode", "text/x-pig");
                CodeMirror.commands.autocomplete = function (cm) {
                    CodeMirror.simpleHint(cm, CodeMirror.pigHint);
                }
            } else {
                dom.byId(domIds.EXPRESSION_TITLE).innerHTML = alpine.nls.sqlExpression_title_sql;
                registry.byId(domIds.HELPER_BUTTON).set('style', "display:none;");
                var dataTypes = ["BIGINT", "BOOLEAN", "BIT", "BIT VARYING", "CHAR", "DATE", "DOUBLE PRECISION", "NUMERIC", "INTEGER", "VARCHAR"];
                var items = [];
                for (var i = 0; i < dataTypes.length; i++) {
                    items.push({label:dataTypes[i], value:dataTypes[i]});
                }
                varDevDataSelect.options = items;
                varDevDataSelect.startup();
                myCodeMirror.setOption("mode", "text/x-plsql");
                CodeMirror.commands.autocomplete = function (cm) {
                    CodeMirror.simpleHint(cm, CodeMirror.plsqlHint);
                }
            }
            myCodeMirror.setValue("");
        }

        function _getVariableDefine4Grid(lists) {
            var dataItems = [];
            if (null != lists && lists.length > 0) {
                for (var i = 0; i < lists.length; i++) {
                    dataItems.push({
                        id:"dataItem" + i,
                        resultColumnName:lists[i].resultColumnName,
                        dataType:lists[i].dataType,
                        sqlExpression:lists[i].sqlExpression
                    });
                }
            }
            return dataItems;
        }

        function buildVariableDefineGrid(dataItems, isHadoop) {
            var gridStruct = _getDefineVariableStruct(isHadoop);
            var gridStore = _getDefineVariableStroe(dataItems);
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if (null == grid) {
                grid = new dojox.grid.DataGrid({
                    id:domIds.VARIABLE_GRID,
                    store:gridStore,
                    structure:gridStruct,
                    clientSort:true,
                    selectionMode:"single",
                    rowSelector:'10px',
                    onRowDblClick:function (event) {
                        return false;
                    },
                    onRowClick:variableRowClickHandler,
                    onHeaderCellClick:resetVarDerivedForm
                    //canSort:function () {return false;}
                }, domContruct.create("div", null, domIds.VARIABLE_GRID_CONTAINER));
                grid.startup();
            } else {
                grid.setStore(gridStore);
            }
        }

        function _getDefineVariableStruct(isHadoop) {
            var sqlExpressionLabel = alpine.nls.sqlExpression_title_sql;
            if (isHadoop == true) {
                sqlExpressionLabel = alpine.nls.sqlExpression_title_java;
            }
            return [{field:'resultColumnName',name:alpine.nls.var_dev_result_column, width:'100px'},
                {field:'dataType',name:alpine.nls.var_dev_data_type,width:'120px'},
                {field:'sqlExpression',name:sqlExpressionLabel,width:'auto'}];
        }

        function _getDefineVariableStroe(dataItems) {
            return new dojo.data.ItemFileWriteStore({
                data:{identifier:"id",items:dataItems}
            });
        }

        function variableRowClickHandler(event){
             if(null!=event && event.rowIndex!=null && event.rowIndex!=-1){
                 var grid = registry.byId(domIds.VARIABLE_GRID);
                 if(grid!=null){
                     grid.selection.select(event.rowIndex);
                     var dataItems = grid.store._arrayOfAllItems;
                     var selectItem = dataItems[event.rowIndex];
                     if(selectItem!=null){
                         setInputVariableValue(selectItem);
                         enable_var_derived_update();
                         enable_var_derived_delete();
                     }
                 }
             }
        }

        function isHadoopOperator() {
            return CurrentOperatorDTO.classname == "HadoopVariableOperator" ? true : false;
        }

        function setInputVariableValue(selectItem){
            registry.byId(domIds.INPUT_VARIABLE_NAME).set("value",selectItem.resultColumnName[0]);
            myCodeMirror.setValue(selectItem.sqlExpression[0]);
            registry.byId(domIds.DATA_TYPE_SELECT).set('value',selectItem.dataType[0]);
        }

        function buildSelectedColumnGrid(selectedColumns, fullColumns) {
            var gridStruct = _getColumnGridStructure();
            var gridStore = _getColumnGridStore(fullColumns);
            var grid = registry.byId(domIds.COLUMN_NAME_GRID);
            if (null == grid) {
                var grid = new dojox.grid.DataGrid({
                    id:domIds.COLUMN_NAME_GRID,
                    store:gridStore,
                    structure:gridStruct,
                    style:"heigth:100%;width:100%;",
                    query:{"colName":"*"},
                    canSort:function () {return false;},
                    onRowClick:function () {},
                    onDblClick:function (e) {
                        if (e.rowIndex == undefined) { return;}
                        var record = this.getItem(e.rowIndex);
                        var value = toolkit.getValue(record.colName);
                        if (isHadoopOperator() == false) {
                            value = "\"" + value + "\" ";
                        }
                        myCodeMirror.replaceSelection(value.trim() + " ", "end");
                        myCodeMirror.focus();
                    }
                }, domContruct.create("div", null, domIds.COLUMN_NAME_GRID_CONTAINER));
                grid.startup();
            } else {
                grid.setStore(gridStore)
            }
            _setColumnSelectedStatus(selectedColumns);
        }

        function _getColumnGridStructure() {
            return [
                {type:"dojox.grid._CheckBoxSelector"},
                [
                    {name:alpine.nls.var_dev_available_column, field:"colName", width:"100%"}
                ]
            ];
        }

        function _getColumnGridStore(columns) {
            var listObj = _buildListObject(columns);
            return new dojo.data.ItemFileReadStore({
                data:{
                    identifier:"colName",
                    label:"colName",
                    items:listObj
                }
            });
        }

        function _setColumnSelectedStatus(selectedColumns) {
            var grid = registry.byId(domIds.COLUMN_NAME_GRID);
            if (grid != null) {
                var allItems = grid.store._arrayOfAllItems;
                if (allItems != null) {
                    for (var i = 0; i < allItems.length; i++) {
                        if (array.indexOf(selectedColumns, allItems[i].colName[0]) != -1) {
                            grid.selection.setSelected(i, true)
                        }
                    }
                }
            }
        }

        function hideDialog() {
            var dlg = registry.byId(domIds.DIALOG);
            if (dlg != null) {
                dlg.hide();
            }
        }

        function saveDefineResult() {
            //save data tip
            var inputObj = getInputValueModel();
            var varName = inputObj.resultColumnName;
            var expressionValue = inputObj.sqlExpression;
            var btnCreate = registry.byId(domIds.BTN_CREATE_4VARIABLE);
            var btnUpdate = registry.byId(domIds.BTN_UPDATE_4VARIABLE);
            var grid = registry.byId(domIds.VARIABLE_GRID);

            if(stringUtil.trim(varName)!=""
                && stringUtil.trim(expressionValue)!=""){
                var rows = grid.store._arrayOfAllItems;
                if(null!=rows && rows.length>0){
                    var selectIndex = -1;
                    for(var i=0;i<rows.length;i++){
                        if(rows[i].resultColumnName[0]==varName){
                            selectIndex = i;
                            break;
                        }
                    }
                    if(-1!=selectIndex){
                        grid.selection.select(selectIndex);
                        btnCreate.set("disabled",true);
                        btnUpdate.set("disabled",false);
                    }
                }
            }

            if(btnCreate.get("disabled")==false
                && btnUpdate.get("disabled")==true
                && stringUtil.trim(varName)!=""
                && stringUtil.trim(expressionValue)!=""){
                popupComponent.confirm(alpine.nls.var_dev_column_create_tip,alpine.nls.var_dev_column_create_title,{label:alpine.nls.var_dev_column_btn_create,handle:function(){
                    if(create_var_derived()!=false){
                        realSaveDefinVarResult();
                    }
                }},{label:alpine.nls.var_dev_column_btn_cancel,handle:function(){
                    realSaveDefinVarResult();
                }});

            }

            if(btnCreate.get("disabled")==true
                && btnUpdate.get("disabled")==false
                && dojo.trim(varName)!=""
                && dojo.trim(expressionValue)!=""
                && valueDiffWithSelected(varName,expressionValue,dijit.byId("var_dev_data_type").get("value"))==true){
                popupComponent.confirm(alpine.nls.var_dev_column_update_tip,alpine.nls.var_dev_column_update_title,{label:alpine.nls.var_dev_column_btn_update,handle:function(){
                    if(update_var_derived()!=false){
                        realSaveDefinVarResult();
                    }
                }},{label:alpine.nls.var_dev_column_btn_cancel,handle:function(){}});
            }

            if(!(btnCreate.get("disabled")==false
                && btnUpdate.get("disabled")==true
                && dojo.trim(varName)!=""
                && dojo.trim(expressionValue)!="")
                &&
                !(btnCreate.get("disabled")==true
                    && btnUpdate.get("disabled")==false
                    && dojo.trim(varName)!=""
                    && dojo.trim(expressionValue)!=""
                    && valueDiffWithSelected(varName,expressionValue,dijit.byId("var_dev_data_type").get("value"))==true)){
                realSaveDefinVarResult();
            }

        }

        function valueDiffWithSelected(nameValue,expressionValue,selectValue){
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if(grid!=null){
                var gridSelected = grid.selection.selected;
                var rows = grid.store._arrayOfAllItems;
                if(gridSelected!=null && rows != null){
                    var selectIndex =-1;
                    for(var i=0;i<gridSelected.length;i++){
                        if(gridSelected[i]==true){
                            selectIndex = i;
                            break;
                        }
                    }
                    if(selectIndex!=-1){
                        if(rows[selectIndex].resultColumnName[0]!=nameValue
                            || rows[selectIndex].sqlExpression[0]!=expressionValue
                            || rows[selectIndex].dataType[0]!=selectValue){
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        function realSaveDefinVarResult(){
            derivedFieldsModel.derivedFieldsList = [];
            derivedFieldsModel.selectedFieldList = [];
            var columnNameGrid = registry.byId(domIds.COLUMN_NAME_GRID);
            var variableGrid = registry.byId(domIds.VARIABLE_GRID);
            if(columnNameGrid!=null){
               var colSelected = columnNameGrid.selection.selected;
               var colAllItems = columnNameGrid.store._arrayOfAllItems;
               for(var i=0;i<colSelected.length;i++){
                   if(colSelected[i]==true){
                       derivedFieldsModel.selectedFieldList.push(colAllItems[i].colName[0]);
                   }
               }
            }
            if(variableGrid!=null){
               var varAllItems = variableGrid.store._arrayOfAllItems;
               for(i=0;i<varAllItems.length;i++){
                   if(null!=varAllItems[i]){
                       derivedFieldsModel.derivedFieldsList.push({
                           resultColumnName:varAllItems[i].resultColumnName[0],
                           sqlExpression:varAllItems[i].sqlExpression[0],
                           dataType:varAllItems[i].dataType[0]
                       });
                   }
               }
            }

            if(derivedFieldsModel.derivedFieldsList.length>0){
                setButtonBaseClassValid("fieldList"+ID_TAG);
            }else{
                setButtonBaseClassInvalid("fieldList"+ID_TAG);
            }
            _validateVariableBtn();
            hideDialog();
        }

        function cancelDefineResult() {
            _validateVariableBtn();
            hideDialog();
        }

        function _validateVariableBtn(){
            if(CurrentOperatorDTO.classname == "VariableOperator"){
                var variableNum = 0;
                var quantileVariableNum = 0;
                quantileVariableNum =_getQuqntileVariableNumFromPropList();
                variableNum = _getVariableNumFromGrid();
                if(variableNum>0 || quantileVariableNum>0){
                    _setDefVariableBtnValid();
                    _setQuantileVariableBtnValid();
                }else{
                    _setDefVariableBtnInvalid();
                    _setQuantileVariableBtnInalid();
                }
            }
        }

        function _getVariableNumFromGrid(){
            var num = 0;
            var varGrid = registry.byId(domIds.VARIABLE_GRID);
            if(null!=varGrid){
                var items = varGrid.store._arrayOfAllItems;
                if(null!=items){
                    for(var i=0;i<items.length;i++){
                        if(null!=items[i]){
                            num++;
                        }
                    }
                }
            }
            return num;
        }

        //set button status
        function enable_var_derived_create() {
            dijit.byId(domIds.BTN_CREATE_4VARIABLE).set("disabled", false);
            dijit.byId(domIds.BTN_UPDATE_4VARIABLE).set("disabled", true);
            dijit.byId(domIds.BTN_DELETE_4VARIABLE).set("disabled", true);
        }

        function enable_var_derived_update() {
            dijit.byId(domIds.BTN_CREATE_4VARIABLE).set("disabled", true);
            dijit.byId(domIds.BTN_UPDATE_4VARIABLE).set("disabled", false);
            //dijit.byId("var_derived_delete_button").set("disabled", false);
        }

        function enable_var_derived_delete() {
            dijit.byId(domIds.BTN_DELETE_4VARIABLE).set("disabled", false);
        }

        function disable_var_derived_buttons() {
            dijit.byId(domIds.BTN_CREATE_4VARIABLE).set("disabled", true);
            dijit.byId(domIds.BTN_UPDATE_4VARIABLE).set("disabled", true);
            dijit.byId(domIds.BTN_DELETE_4VARIABLE).set("disabled", true);
        }

        function validateInputVARName() {
            var varName = registry.byId(domIds.INPUT_VARIABLE_NAME);
            var varNameValue = varName.get("value");
            if (null == varNameValue || "" == varNameValue) {
                popupComponent.alert("Please input Variable Name.");
                return false;
            }
            if (/^[\w][\w]+/.test(varNameValue) == false) {
                popupComponent.alert("Input value contains illegal characters.");
                return false;
            }
            return true;
        }

        function getInputValueModel() {
            var val = {};
            val.resultColumnName = registry.byId(domIds.INPUT_VARIABLE_NAME).get("value");
            val.sqlExpression = myCodeMirror.getValue();
            val.dataType = registry.byId(domIds.DATA_TYPE_SELECT).get("value");
            return val;
        }

        function hasSameNameInColumGrid(varName) {
            var grid = registry.byId(domIds.COLUMN_NAME_GRID);
            if (null != grid) {
                var selectStatus = grid.selection.selected;
                var dataItems = grid.store._arrayOfAllItems;
                for (var i = 0; i < selectStatus.length; i++) {
                    if (selectStatus[i] == true && dataItems[i].colName[0] == varName) {
                        return true;
                    }
                }
            }
            return false;
        }

        function hasSameNameInVarGrid(varName) {
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if (grid != null) {
                var gridItems = grid.store._arrayOfAllItems;
                if (gridItems != null && gridItems.length > 0) {
                    for (var i = 0; i < gridItems.length; i++) {
                        if (varName == gridItems[i].resultColumnName) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        // crud
        function create_var_derived() {
            //
            var inputObj = getInputValueModel();
            if (validateInputVARName() == false) {
                return false;
            }
            if (hasSameNameInColumGrid(inputObj.resultColumnName) == true) {
                popupComponent.alert(alpine.nls.agg_win_column_exist);
                return false;
            }
            var hasSameName = hasSameNameInVarGrid(inputObj.resultColumnName);
            if (hasSameName == true) {
                //replace exist
                popupComponent.confirm("The name of the item already exists, whether to replace", "Replace?", {
                    handle:function () {
                        replaceExistItemFromVarGrid(inputObj);
                    }
                }, {});
            } else {
                //create variable
                addNewItemToVarGrid(inputObj);
            }
            //
            resetVarDerivedForm();
            return true;
        }

        function replaceExistItemFromVarGrid(inputObj) {
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if (grid != null) {
                var dataItems = getVariableGridItem();
                for (var i = 0; i < dataItems.length; i++) {
                    if (dataItems[i].resultColumnName == inputObj.resultColumnName) {
                        dataItems[i].dataType = inputObj.dataType;
                        dataItems[i].sqlExpression = inputObj.sqlExpression;
                    }
                }
                var newStore = _getDefineVariableStroe(dataItems);
                grid.setStore(newStore);
            }
        }

        function addNewItemToVarGrid(inputObj) {
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if (grid != null) {
                var dataItems = getVariableGridItem();
                dataItems.push({
                    id:("dataItem" + (dataItems.length + 1)),
                    resultColumnName:inputObj.resultColumnName,
                    dataType:inputObj.dataType,
                    sqlExpression:inputObj.sqlExpression
                });

                var newStore = _getDefineVariableStroe(dataItems);
                grid.setStore(newStore);
            }
        }
        //acquire grid data item for new datastore
        function getVariableGridItem() {
            var dataItems = [];
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if (grid != null) {
                var gridStoreItems = grid.store._arrayOfAllItems;
                if (null != gridStoreItems && gridStoreItems.length > 0) {
                    for (var i = 0; i < gridStoreItems.length; i++) {
                        if (gridStoreItems[i] != null) {
                            dataItems.push({
                                id:"dataItem" + i,
                                resultColumnName:gridStoreItems[i].resultColumnName[0],
                                dataType:gridStoreItems[i].dataType[0],
                                sqlExpression:gridStoreItems[i].sqlExpression[0]
                            });
                        }
                    }
                }
            }
            return dataItems;
        }

        function update_var_derived() {
            var inputObj = getInputValueModel();
            if (validateInputVARName() == false) {
                return false;
            }
            //obj{index:int,data:{}}
            var selectItem = _getVariableGridSelectItem();
            if(_haveSameNameExceptSelected(selectItem,inputObj)==true){
                popupComponent.alert("Update the name of the item already exists.Please change the variable name.");
                return false;
            }
            var allVariableItems = getVariableGridItem();
            for(var i=0;i<allVariableItems.length;i++){
                if(i==selectItem.index){
                    allVariableItems[i].resultColumnName = inputObj.resultColumnName;
                    allVariableItems[i].dataType = inputObj.dataType;
                    allVariableItems[i].sqlExpression = inputObj.sqlExpression;
                }
            }

            var newDataStore = _getDefineVariableStroe(allVariableItems);
            var grid = registry.byId(domIds.VARIABLE_GRID);
            grid.setStore(newDataStore);
            resetVarDerivedForm();
        }

        function _getVariableGridSelectItem(){
          var grid = registry.byId(domIds.VARIABLE_GRID);
          if(grid!=null){
              var selectStatus = grid.selection.selected;
              var allItems = grid.store._arrayOfAllItems;
              var selectIndex = -1;
              for(var i=0;i<selectStatus.length;i++){
                  if(selectStatus[i]==true){
                      selectIndex = i;
                      break;
                  }
              }
              if(selectIndex==-1){
                  return null;
              }else{
                  return {
                      index:selectIndex,
                      data:{
                          id:allItems[selectIndex].id[0],
                          resultColumnName:allItems[selectIndex].resultColumnName[0],
                          dataType:allItems[selectIndex].dataType[0],
                          sqlExpression:allItems[selectIndex].sqlExpression[0]
                      }
                  };
              }

          }
        }

        function _haveSameNameExceptSelected(selectItem,inputValue){
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if(null!=grid && selectItem!=null){
                var allItems = grid.store._arrayOfAllItems;
                if(allItems!=null){
                    for(var i=0;i<allItems.length;i++){
                        if(selectItem.index==i){
                            continue;
                        }
                        if(allItems[i].resultColumnName[0]==inputValue.resultColumnName){
                            return true;
                        }
                    }
                }
            }
            return false
        }

        function delete_var_derived() {
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if(grid!=null){
               grid.removeSelectedRows();
           }
            resetVarDerivedForm();
        }

        function resetVarDerivedForm() {
            enable_var_derived_create();
            clear_var_derived_from();
            var grid = registry.byId(domIds.VARIABLE_GRID);
            if (null != grid) {
                grid.selection.clear();
            }
        }

        function clear_var_derived_from() {
            registry.byId(domIds.INPUT_VARIABLE_NAME).set("value", "");
            myCodeMirror.setValue("");
        }

        return {
            showDefineVarDialog:showDefineVarDialog
        }

    });