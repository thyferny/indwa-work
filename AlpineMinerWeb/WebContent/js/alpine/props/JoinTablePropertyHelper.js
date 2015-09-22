define([
    "dojo/dom-construct", "dojo/query",       "dojo/_base/lang", "dojo/on", "dijit/registry","alpine/flow/OperatorManagementManager",
    "alpine/operatorexplorer/OperatorUtil","alpine/props/JoinPropertyHelper", "alpine/flow/WorkFlowVariableReplacer","alpine/props/MultiFileUtil"
],

    function (domConstruct,query,lang, on, registry, operatorManager, operatorUtil,joinHelper, variableReplacer,multiFileUtil) {
    var tableAliasStore = null;
    var tableAliasTable;

    var joinRuleStore;
    var joinRulesTable = null;

    var current_join_condition;

    var tableJoinDialog_ID = "tableJoinDialog";
    var blank_str_label = "&nbsp;";
    var blank_str_value = " ";

    var column_type_array = ["array", "VARRAY", "FLOATARRAY", "INTEGERARRAY",
        "VARCHAR2ARRAY", "FLOATARRAYARRAY", "INTEGERARRAYARRAY",
        "VARCHAR2ARRAYARRAY"];


    var aliasArray = new Array();


    var columnDataStore = null;

    var constants = {
        SELECTED_COLUMNS_HOLDER:"table_join_selected_columns_holder",
        COLUMNS_GRID:"columns_grid",
        COLUMN_DISPLAY_BUTTON_PANEL:"table_join_tables_toggle",
        COLUMN_DISPLAY_BUTTON_PANEL_INNER:"table_join_toggle_inner",
        PREFIX_INPUT_BUTTON:"input_button_",
        PREFIX_ALIAS:"tj_alias_",
        DIV_NUM_SEL_COLUMNS: "div_number_sel_columns",
        ALL_PREFIX:"all_",
        NONE_PREFIX:"none_",
        CREATE_EDIT_CONDITIONS: "join_conditions_edit_title"
    };
    var generatedWidgets = [];


    var tableAliasByUUID;

    function getTableAliasByTableUUID(uuid)
    {
        return tableAliasByUUID[uuid];
    }

    function numFieldsSelectedChanged()
    {
        if(columnDataStore)
        {

            columnDataStore.fetch( { query: { selected: true },
                onComplete: function(items, request){
                    if (items)
                    {
                        fillSelectedFieldsHTML(items.length);
                    }
                }
            });

        }
    }
    function fillSelectedFieldsHTML(numSelFields)
    {
        var text = alpine.nls.Table_Join_Fields.replace("{0}",numSelFields);
        if (numSelFields == 1) text = alpine.nls.Table_Join_Fields_One_Field;
        dojo.byId(constants.DIV_NUM_SEL_COLUMNS).innerHTML = text;
    }

    function filterColumns(whichFilter, output) {
        var showSelected = (output) ? "true" : "*";
        var filterByUUID = "*";
        if (!output)
        {
            var aliasindex = +whichFilter.substring(13);
            filterByUUID = aliasArray[aliasindex].tableUUID[0];
        }

        dijit.byId(constants.COLUMNS_GRID).filter({
            "selected": showSelected,
            "operatorUUID": filterByUUID

        }, false);

        dijit.byId(constants.COLUMNS_GRID).sort();
    }

    function setInputTableSelected(aliasindex, allSelected)
    {
        var filterByUUID = aliasArray[aliasindex].tableUUID[0];
        var currentId = constants.PREFIX_INPUT_BUTTON + aliasindex;

            columnDataStore.fetch( { query: { operatorUUID: filterByUUID },
            onComplete: function(items, request){
                dojo.forEach(items,function(item){
                        columnDataStore.setValue(item, "selected", allSelected);

               });
                dijit.byId(constants.COLUMNS_GRID).filter({
                    "selected": "*",
                    "operatorUUID": filterByUUID

                }, false);
            }
        });
        selectedTable(currentId);
    }

    function createTableSelectObject(currentTableJoin,holder, outer, index, operatorInfo,inputTables)
    {
        dojo.addClass(holder, "selectableDiv");
        dojo.addClass(holder, "thisgroup");

        var imgKey = "TableJoinOperator";
        var tableName =  current_tableJoin.opInfo.name;
        if (!outer)
        {
            imgKey = operatorInfo.classname;
            tableName = operatorInfo.name;
        }
       var img =  domConstruct.create("img", { src:operatorUtil.getStandardImageSourceByKey(imgKey), style:{"float":"left"} }, holder);
       dojo.addClass(img,"small");
       var anotherDiv =  domConstruct.create("div", { innerHTML:tableName }, holder);
       dojo.addClass(anotherDiv,"tablejointablename"  ) ;

        if (outer)
        {
            var numbSelColDiv = domConstruct.create("div", { id:constants.DIV_NUM_SEL_COLUMNS}, holder);
            dojo.addClass(numbSelColDiv,"tablejoinsubfield" )
            fillSelectedFieldsHTML(currentTableJoin.tableJoinModel.joinColumns.length);
        }  else
        {
            var aliasLabel = domConstruct.create("label", {"for":constants.PREFIX_ALIAS + index, innerHTML:alpine.nls.Table_Join_Alias}, holder);
            dojo.addClass(aliasLabel,"tablejoinsubfield" )
            var textBox = new dijit.form.TextBox({
                name:constants.PREFIX_ALIAS + index,
                value:inputTables[index].alias,
                style:"width: 15em;",
                id:constants.PREFIX_ALIAS + index

            });
            var textBoxHolder = domConstruct.create("span",{ style:"font-size: 13px;"}, holder);
            dojo.addClass(textBoxHolder,"subtleTextbox" )
            textBoxHolder.appendChild(textBox.domNode);

            var allNoneHolder = domConstruct.create("span",{},holder);
            dojo.addClass(allNoneHolder,"allNoneHolder" )

            var allButton =  new dijit.form.Button({
                id: constants.ALL_PREFIX +index,
                baseClass: "linkButton",
                onClick: function(event)
            {
                dojo.stopEvent(event); //we stop the event so we don't have two filters firing at the same time (one for selecting elements, one for choosing which input table)
                var currentId = +this.id.substring(4);
                setInputTableSelected(currentId, true);
            },

                label: alpine.nls.Table_Join_All_Button
            });


            allNoneHolder.appendChild(allButton.domNode);

           var dividerSpan =  domConstruct.create("span", { style:"padding-right:0; color:#949599;" , innerHTML:"|"}, allNoneHolder);
           dojo.addClass(dividerSpan, "tablejoinsubfield");
            var noneButton =  new dijit.form.Button({
                id: constants.NONE_PREFIX +index,
                baseClass: "linkButton",
                onClick: function(event)
                {
                    dojo.stopEvent(event);   //we stop the event so we don't have two filters firing at the same time (one for selecting elements, one for choosing which input table)
                    var currentId = +this.id.substring(5);
                    setInputTableSelected(currentId, false);
                },

                label: alpine.nls.Table_Join_None_Button
            });

            allNoneHolder.appendChild(noneButton.domNode);

            on(textBox, "blur", function()
            {
                _updateAliasForInputTable(this);
            });

            dojo.connect(textBox, "onMouseDown", function(event)
            {
                if (!dojo.hasClass(holder,"selectableDivSelected"))
                {
                    dojo.stopEvent(event);
                    holder.click();
                }
            });
            generatedWidgets.push(textBox);

        }
        domConstruct.create("div", {style:{clear:"both"} }, holder);

        dojo.query(holder).onclick(function (event) {
//            if (event.target.nodeName == "INPUT" && event.target.nodeName == "")   //NOTE: input elements in dojo send off two events - this is just removing the extra event.
//            {
//                console.log("eating the input event");
//                return true;
//            }
            var currentId = this.id;
            selectedTable(currentId, true, outer);
        });

    }

    function selectedTable(currentId, shouldFilter, outer)
    {
        dojo.query(".thisgroup").forEach(function (node) {

            if (node.id == currentId) {

                //if this is already selected, then nothing should happen...
                if (dojo.hasClass(currentId,"selectableDivSelected"))
                {
                    //console.log("already has the focus");
                } else
                {
                    dojo.removeClass(node.id, "selectableDiv");
                    dojo.addClass(currentId, "selectableDivSelected");
                    if (shouldFilter) filterColumns(currentId, outer);

                }

            } else {
                dojo.addClass(node.id, "selectableDiv");
                dojo.removeClass(node.id, "selectableDivSelected");

            }
        });
    }

        function createButtonsAndStackForAllOperators(currentTableJoin) {
            var inputTables = currentTableJoin.tableJoinModel.joinTables;
            var inner = domConstruct.create("div", {id:constants.COLUMN_DISPLAY_BUTTON_PANEL_INNER }, dijit.byId(constants.COLUMN_DISPLAY_BUTTON_PANEL).domNode);

            //First we need to list the output info
        var outerTableTitle = domConstruct.create("div", { innerHTML:alpine.nls.Table_Join_Output_Table}, inner);
            dojo.addClass(outerTableTitle, "tablejointableheader");
        var outputButtonHolder = domConstruct.create("div", { id:"OutputHolder"}, inner);
        createTableSelectObject(currentTableJoin,outputButtonHolder, true);


        //now do all the input tables
        var innerTableTitle = domConstruct.create("div", { innerHTML:alpine.nls.Table_Join_Input_Tables}, inner);
            dojo.addClass(innerTableTitle, "tablejointableheader");
            //Then for all inputs, create one.
        for (var i = 0; i < inputTables.length; i++) {
            var operatorInfo = operatorManager.getOperatorPrimaryInfo(inputTables[i].operatorModelID);
            var inputButtonHolder = domConstruct.create("div", { id:constants.PREFIX_INPUT_BUTTON + i }, inner);
            createTableSelectObject(currentTableJoin,inputButtonHolder,false, i, operatorInfo,inputTables);
        }

            if (inputTables.length == 0)
            {
                var emptyDiv = domConstruct.create("div", { innerHTML:alpine.nls.Table_Join_Error_No_Input_Tables }, inner);
                dojo.addClass(emptyDiv, "tablejoinempty");
                dijit.byId("tablejoin_dialog_ok_id").set('disabled',true);
            }  else
            {
                dijit.byId("tablejoin_dialog_ok_id").set('disabled',false);

            }

            //Now we need the selectedColumnsArea
        var grid = dijit.byId(constants.COLUMNS_GRID);
         if (grid)
         {
             dijit.byId(constants.COLUMNS_GRID).destroyRecursive();

         }
        grid = new dojox.grid.EnhancedGrid({
            id: constants.COLUMNS_GRID,
            store: columnDataStore,
            structure: getColumnDataStoreStructure(),
            plugins:{}
       }, dojo.create("div", {}, constants.SELECTED_COLUMNS_HOLDER));

        grid.startup();



    }


    /**
     * Sets up the input table field data in a way that it can be used both in the
     * "Select output columns" area and in the dropdowns for choosing columns to join on.
     *
     * @param joinColumns - already selected columns for the output table
     */
    function initColumnData(joinColumns)
    {
        var newColumnArray = new Array();
        var inputTableInfos = CurrentOperatorDTO.inputTableInfos;
        if (!inputTableInfos) inputTableInfos = new Array();
        for (var i = 0; i < inputTableInfos.length; i++)
        {
            var fieldColumns = inputTableInfos[i].fieldColumns;
            var tableName = inputTableInfos[i].table;
            var tableAlias = getTableAliasByTableUUID(inputTableInfos[i].operatorUUID);
            for (var j=0; j < fieldColumns.length;j++)
            {
                var colData = {};
                colData.columnName = fieldColumns[j][0];
                colData.newColumnName = fieldColumns[j][0];
                colData.columnType = fieldColumns[j][1];
                colData.tableName = tableName;
                colData.tableAlias = tableAlias;
                colData.operatorUUID =  inputTableInfos[i].operatorUUID;
                colData.name = tableAlias + "." + fieldColumns[j][0];       //for the combo boxes
                colData.label = tableAlias + "." + fieldColumns[j][0];      //for the combo boxes
                colData.value = "\"" + tableAlias + "\".\"" + fieldColumns[j][0] + "\"";  //this is with the quotes
                colData.strippedValue = tableAlias + "." + fieldColumns[j][0];     //without quotes, so we can compare with old joins that don't have quotes

                //this field will tell us whether this column can be used in a join (can't be an array)
                colData.joinable = false;
                if (dojo.indexOf(column_type_array, fieldColumns[j][1]) < 0) {
                    colData.joinable = true;
                }


                //whether it's selected for output tables is stored in the joinColumns object
                colData.selected = false;
                for (var k=0; k < joinColumns.length; k++)
                {
                    if (joinColumns[k].tableAlias == tableAlias && colData.columnName ==joinColumns[k].columnName )
                    {
                        colData.selected = true;
                    }
                }


                newColumnArray.push(colData);
            }
       }

        var colDataItems = {
            items:newColumnArray
        };
        //all the data is of the right form, we think.
        //So now, put it in the store.
        columnDataStore = new dojo.data.ItemFileWriteStore({
            data:colDataItems
        });
        dojo.connect(columnDataStore, "onSet", function(item, attribute, oldValue, newValue){
            if (attribute && attribute == "selected")
            {
                numFieldsSelectedChanged();
            }
        });
    }

    /**
     * This tells us what data should look like in the "select output columns" area.
     * Right now, it give a checkbox indicating selection and a label that looks like [table_alias] colName.
     * @return {Array}
     */
    function getColumnDataStoreStructure()
    {
        var structure = [

            [ { field: "selected",
                name: " ",
                width: "16px",
                editable: true,
                type:joinHelper.getDojoCheckbox("tjpropinput_")

    },
            {
                name: "",
                field: "_item",
                width: "100%",
                formatter: function(value) {
                    var theName = value.columnName;
                    return "<div class=\"tablejointablecolumn\">[" + value.tableAlias + "] " + "<span class=\"tablejointablecolumnname\">" + theName +  "</span></div>";
                }
            }
        ]];
        return structure;
    }

    function initAliasTables(joinTables) {
        tableAliasByUUID = {};
        aliasArray = new Array();
        for (var i = 0; i < joinTables.length; i++) {
            aliasArray[i] = {
                tableName:joinTables[i].table,
                tableAlias:joinTables[i].alias,
                tableUUID: joinTables[i].operatorModelID
            };
            tableAliasByUUID[joinTables[i].operatorModelID] = joinTables[i].alias;
        }

        var dataTable = {
            items:aliasArray
        };
        // our test data store for this example:
        tableAliasStore = new dojo.data.ItemFileWriteStore({
            data:dataTable
        });

        if (tableAliasTable == null) {
            tableAliasTable = dijit.byId("aliasTablesTable");
           // dojo.connect(tableAliasTable, "onRowClick", select_join_table_alias);
        }
        //this will make the edit ok
        tableAliasTable.setStore(tableAliasStore);

        // Call startup, in order to render the grid:

        tableAliasTable.render();
        var items = tableAliasTable.selection.getSelected();
        if (!items || items.length != 1) {
           // dijit.byId("tablejoin_alias_update_btn").set("disabled", true);
           // dijit.byId("tablejoin_alias_edit").set("disabled", true);
        }
        else {
            //dijit.byId("tablejoin_alias_update_btn").set("disabled", false);
            //dijit.byId("tablejoin_alias_edit").set("disabled", false);
        }

        var options = getTableAliasOptions();
        dijit.byId("join_left_table_combo").set("options", options);
        dijit.byId("join_left_table_combo").startup();//xxx
        var optionsx = getTableAliasOptions();
        optionsx.push({
            label:blank_str_label, value:" "});

        dijit.byId("edit_join_right_table").set("options", optionsx);
        dijit.byId("edit_join_right_table").startup();//xxx
        options = [
            {label:"JOIN", value:"JOIN"          },
            {label:"LEFT JOIN", value:"LEFT JOIN"         },
            {label:"RIGHT JOIN", value:"RIGHT JOIN"},
            {label:"FULL OUTER JOIN", value:"FULL OUTER JOIN"},
            {label:"CROSS JOIN", value:"CROSS JOIN"} ,
            {label:blank_str_label, value:" "}
        ];

        dijit.byId("edit_join_type").set("options", options);


        options = [
            {      label:"AND", value:"AND"       },
            {      label:"OR", value:"OR"      },
            {      label:blank_str_label, value:" "}
        ];

        dijit.byId("edit_join_andor").set("options", options);


        options = [
            {           label:"=", value:"="  },
            {              label:"&gt;", value:">"  },
            {              label:"&lt;", value:"<"  },
            {           label:">=", value:">=" },
            {           label:"<=", value:"<=" } ,
            {            label:"<>", value:"<>" },
            {            label:"LIKE", value:"LIKE" },
            {            label:"NOT LIKE", value:"NOT LIKE" },
            {            label:"IS NULL", value:"IS NULL" },
            {            label:"IS NOT NULL", value:"IS NOT NULL" },
            {            label:"BETWEEN", value:"BETWEEN" },
            {            label:"NOT BETWEEN", value:"NOT BETWEEN" },
            {            label:"IN", value:"IN" },
            {            label:"NOT IN", value:"NOT IN" },
            {            label:"IS", value:"IS" },
            {           label:blank_str_label, value:" "}
        ];

        dijit.byId("edit_join_condition").set("options", options);

    }

    function select_join_rule(event) {
        //event is no use now
        //ename delete button and remove button ...
        var items = joinRulesTable.selection.getSelected();
        if (items && items[0]) { //single select
            dijit.byId("updateJoinRuleBtn").set("disabled", false);
            dijit.byId("removeJoinRuleBtn").set("disabled", false);
            dijit.byId("addJoinRuleBtn").set("disabled", true);

            if (event) {//make sure last one
                current_join_condition = joinRulesTable.getItem(event.rowIndex);
                if (dojo.indexOf(items, current_join_condition) < 0) {
                    //ctrl to cacel the select
                    current_join_condition = items[0];
                }
            } else if (items && items[0]) {
                current_join_condition = items[0];
            }

            dijit.byId("edit_join_column1").set("value", convertSpaceString(current_join_condition.JoinColumn1[0]));
            dijit.byId("edit_join_column2").set("value", convertSpaceString(current_join_condition.JoinColumn2[0]));

            dijit.byId("edit_join_type").set("value", convertSpaceString(current_join_condition.JoinType[0]));
            dijit.byId("edit_join_right_table").set("value", convertSpaceString(current_join_condition.RightTable[0]));

            dijit.byId("edit_join_condition").set("value", convertSpaceString(current_join_condition.JoinCondition[0]));
            dijit.byId("edit_join_andor").set("value", convertSpaceString(current_join_condition.AndOr[0]));
            dojo.byId(constants.CREATE_EDIT_CONDITIONS).innerHTML = alpine.nls.Table_Join_Join_Edit_Title;
        } else {//no select
            dijit.byId("updateJoinRuleBtn").set("disabled", true);
            dijit.byId("removeJoinRuleBtn").set("disabled", true);
            current_join_condition = null;
        }

    }


    function convertSpaceString(str) {
        if (!str || dojo.trim(str).length == 0 || str.length == 0) {
            return " ";
        } else {
            return str;
        }

    }

    function getTableAliasOptions() {
        var options = new Array();
        if (tableAliasStore) {
            var items = tableAliasStore._arrayOfAllItems;
            if (items) {
                for (var x = 0; x < items.length; x++) {
                    var item = items[x];
                    var alias = item.tableAlias[0];
                    options.push({ label:alias, value:alias});
                }
            }
        }
        return options;
    }

    function getConditionStr(condition) {
        return condition;

    }

    function clearEditingArea()
    {
        current_join_condition = null;

        dijit.byId("updateJoinRuleBtn").set("disabled", true);
        dijit.byId("removeJoinRuleBtn").set("disabled", true);
        dijit.byId("addJoinRuleBtn").set("disabled", false);

        dijit.byId("edit_join_column1").set("value", "");
        dijit.byId("edit_join_column2").set("value", "");

        dijit.byId("edit_join_type").set("value", "");
        dijit.byId("edit_join_right_table").set("value", " ");

        dijit.byId("edit_join_condition").set("value", "");
        dijit.byId("edit_join_andor").set("value", " ");

        dojo.byId(constants.CREATE_EDIT_CONDITIONS).innerHTML = alpine.nls.Table_Join_Join_Create_Title;

        _clearSelection();
    }

    function _clearSelection() {
        if (joinRulesTable) {joinRulesTable.selection.clear();}
    }

    function initJoinConditionTables(joinConditions, avialableColumns) {
        //clear the edit area..
        clearEditingArea();

        if (joinConditions && joinConditions.length > 0) {
            dijit.byId("join_left_table_combo").set("value", joinConditions[0].tableAlias1);
        }
        var conditionArray = new Array();
        for (var i = 0; i < joinConditions.length; i++) {
            var conditionStr = getConditionStr(joinConditions[i].condition);
            conditionArray[i] = {

                JoinType:joinConditions[i].joinType,
                RightTable:joinConditions[i].tableAlias2,
                JoinColumn1:joinConditions[i].column1,
                JoinCondition:conditionStr,
                JoinColumn2:joinConditions[i].column2,
                AndOr:joinConditions[i].andOr
            };
        }

        var dataTable = {
            items:conditionArray
        };
        // our test data store for this example:
        joinRuleStore = new dojo.data.ItemFileWriteStore({
            data:dataTable
        });

        if (joinRulesTable == null) {
            joinRulesTable = dijit.byId("joinRulesTable");
            dojo.connect(joinRulesTable, "onRowClick", select_join_rule);
        }


        var potentialJoinColumns =  { joinable: true} ;

        dijit.byId("edit_join_column1").set("store", columnDataStore);
        dijit.byId("edit_join_column2").set("store", columnDataStore);

        dijit.byId('edit_join_column1').set('query', potentialJoinColumns);
        dijit.byId('edit_join_column2').set('query', potentialJoinColumns);

        //dijit.byId("joinRulesTable").structure.cells[0][0].name
        //this will make the edit ok
        joinRulesTable.setStore(joinRuleStore);
        joinRulesTable.render();
        // append the new grid to the div "gridContainer4":
        //     dojo.byId("gridContainer4").appendChild(grid4.domNode);

        // Call startup, in order to render the grid:

    }

    function removeJoinRuleBtn_Click() {
        // Get all selected items from the Grid:
        var items = joinRulesTable.selection.getSelected();
        if (items.length) {
            dojo.forEach(items, function (selectedItem) {
                if (selectedItem != null) {
                    joinRulesTable.store.deleteItem(selectedItem);
                    //joinRulesTable.setStore(joinRuleStore);
                }
            });
        } // end if
        joinRulesTable.render();
//	joinRulesTable.setStore(joinRuleStore);
        clearEditingArea();
        if (joinRuleStore._arrayOfTopLevelItems &&
            joinRuleStore._arrayOfTopLevelItems.length > 0) {

            joinRulesTable.selection.select(0);

            clearEditingArea();
        } else {
            joinRulesTable.selection.select(-1);
            clearEditingArea();
            dijit.byId("updateJoinRuleBtn").set("disabled", true);
            dijit.byId("removeJoinRuleBtn").set("disabled", true);
        }
    }

    function clearJoinConditionEditValue() {
        current_join_condition = null;

        dijit.byId("edit_join_column1").set("value", "");
        dijit.byId("edit_join_column2").set("value", " ");

        dijit.byId("edit_join_type").set("value", " ");
        dijit.byId("edit_join_right_table").set("value", " ");

        dijit.byId("edit_join_condition").set("value", " ");
        dijit.byId("edit_join_andor").set("value", " ");

    }

    function readValidateAndHandleCondition(isUpdate)
    {
        if (isUpdate && !current_join_condition) return;  //can't update if nothing to update, so return
        var joinColumn1 = convertEmptyString(dijit.byId("edit_join_column1").get("value"));
        var joinColumn2 = convertEmptyString(dijit.byId("edit_join_column2").get("value"));

          //if we switch to filteringselect, use this to get the value
//        var colOneItem = dijit.byId("edit_join_column1").get("item");
//        var colTwoItem = dijit.byId("edit_join_column2").get("item");
//        var joinColumn1 = "";
//        var joinColumn2 = "";
//        if (colOneItem)
//        {
//            joinColumn1 = convertEmptyString(colOneItem.value[0]);
//        }
//        if (colTwoItem)
//        {
//            joinColumn2 = convertEmptyString(colTwoItem.value[0]);
//        }


        var joinType = convertEmptyString(dijit.byId("edit_join_type").get("value"));
        var rightTable = convertEmptyString(dijit.byId("edit_join_right_table").get("value"));


        if (joinType.replace(/^\s+|\s+$/g, '') != "" && rightTable.replace(/^\s+|\s+$/g, '') == "") {
            popupComponent.alert(alpine.nls.right_table_empty);
            return false;
        }

        var joinCondition = convertEmptyString(dijit.byId("edit_join_condition").get("value"));
        var andOr = convertEmptyString(dijit.byId("edit_join_andor").get("value"));


        if (joinColumn1.length == 0 && joinColumn2.length == 0
            && joinType.length == 0 && rightTable.length == 0
            && joinCondition.length == 0 && andOr.length == 0) {
            popupComponent.alert(alpine.nls.cant_empty_condition, function () {
                if (isUpdate) joinRulesTable.render();
            });
            return;
        }


        //Now do Update

         if (isUpdate)
         {
            current_join_condition.JoinColumn1[0] = joinColumn1;
            current_join_condition.JoinColumn2[0] = joinColumn2;

            current_join_condition.JoinType[0] = joinType;
            current_join_condition.RightTable[0] = rightTable;

            current_join_condition.JoinCondition[0] = joinCondition;
            current_join_condition.AndOr[0] = andOr;
            joinRulesTable.render();

             clearEditingArea();
             return;
         }

         //Now do create
            if (joinRuleStore._arrayOfTopLevelItems &&
                joinRuleStore._arrayOfTopLevelItems.length > 0) {
                var items = joinRuleStore._arrayOfTopLevelItems;
                for (var x = 0; x < items.length; x++) {
                    var item = items[x];
                    if ((item.JoinType == joinType)
                        && (item.RightTable == rightTable)
                        && (item.AndOr == andOr)
                        && (item.JoinColumn1 == joinColumn1)
                        && (item.JoinColumn2 == joinColumn2)
                        && (item.JoinCondition == joinCondition)
                        ) {
                        popupComponent.alert(alpine.nls.condition_exists);
                        return;
                    }
                }
            }


            var myNewItem = {
                JoinType:joinType,
                RightTable:rightTable,
                AndOr:andOr,
                JoinColumn1:joinColumn1,
                JoinCondition:joinCondition,
                JoinColumn2:joinColumn2
            };
            joinRulesTable.store.newItem(myNewItem);
            joinRulesTable.render();
//		joinRulesTable.setStore(joinRuleStore);
            var allItems = joinRuleStore._arrayOfTopLevelItems;
            joinRulesTable.selection.select(allItems.length - 1);


        clearEditingArea();

    }

    function updateJoinRuleBtn_Click() {
        readValidateAndHandleCondition(true);
   }

    function convertEmptyString(str) {
        if (!str || str == " " || str == blank_str_label) {
            return "";
        } else {
            return dojo.trim(str);
        }

    }

    function addJoinRuleBtn_Click() {
        readValidateAndHandleCondition(false);
    }

    function clearJoinRuleBtn_Click() {
        clearEditingArea();
    }


    function isJoinColumnAdded(column, items) {
        var added = false;
        if (column && items) {
            for (var x = 0; x < items.length; x++) {
                if (items[x].joinColumn == column) {
                    added = true;
                    break;
                }
            }
        }
        return added;
    }

    function getNewAvialableColumns(avialableColumns) {
        var newAvialableColumns = new Array();
        if (avialableColumns) {
            for (var i = 0; i < avialableColumns.length; i++) {
                var newJoinColumn = avialableColumns[i];
                if (newJoinColumn.indexOf("\"") == 0
                    && newJoinColumn.indexOf("\".\"") >= 0) {
                    newJoinColumn = newJoinColumn.substring(1, newJoinColumn.length - 1);

                    newJoinColumn = newJoinColumn.replace("\".\"", ".");
                }
                newAvialableColumns.push(newJoinColumn);
            }
        }
        return newAvialableColumns;
    }

    dojo.addOnLoad(function () {
        dojo.connect(dijit.byId(tableJoinDialog_ID), "onHide", dijit.byId(tableJoinDialog_ID), function () {
            var len = generatedWidgets.length;
            for (i = 0; i < len; i++) {
                generatedWidgets[i].destroyRecursive();
            }
            generatedWidgets = [];
            dojo.forEach(dijit.findWidgets(dojo.byId(constants.COLUMN_DISPLAY_BUTTON_PANEL_INNER)), function (w) {
                w.destroyRecursive();
            });
            dojo.forEach(dijit.findWidgets(dojo.byId(constants.SELECTED_COLUMNS_HOLDER)), function (w) {
                w.destroyRecursive();
            });
            dojo.empty(constants.COLUMN_DISPLAY_BUTTON_PANEL);
            dojo.empty(constants.SELECTED_COLUMNS_HOLDER);


        });

//	var grid= dijit.byId("aliasTablesTable");
//	dojo.connect(grid, "onApplyCellEdit",  onApplyAliasCellEditHandler );

        //var grid= dijit.byId("joinColumnTable");
        //dojo.connect(grid, "onApplyCellEdit",  onApplyColumnCellEditHandler );


    });


//the array to save the prious alias for the rename use
    var previousAliasArray;

    function onApplyAliasCellEditHandler(inValue, inRowIndex) {
//	if(!inValue||inValue.replace(/\ /g,"").length==0)
//	{
//		dijit.byId("tablejoin_dialog_ok_id").setAttribute("disabled",true);
//		popupComponent.alert(alpine.nls.table_alias_empty);
//	}else{
        //the name changed successfully, so need update the join condition and columns
        dijit.byId("tablejoin_dialog_ok_id").set("disabled", false);

        var inputTableInfos = CurrentOperatorDTO.inputTableInfos;
        var previousAlias = previousAliasArray[inRowIndex];
        var newAlias = inValue;

//		if(tableAliasStore){
//			var items = tableAliasStore._arrayOfAllItems;
//			if(items){
//				for ( var x = 0; x < items.length; x++) {
//					var item = items[x] ;
//					var alias=item.tableAlias[0];
//					if(inRowIndex!=x&&alias==newAlias){
//						dijit.byId("tablejoin_dialog_ok_id").setAttribute("disabled",true);
//						popupComponent.alert(alpine.nls.table_alias_same);
//						return;
//					}
//				 
//				}
//			}
//		}


        previousAliasArray[inRowIndex] = newAlias;





        items = joinRuleStore._arrayOfTopLevelItems;

        for (var i = 0; i < items.length; i++) {
            var joinColumn1 = items[i].JoinColumn1[0];
            if (joinColumn1.indexOf("\"" + previousAlias + "\".") == 0) {
                items[i].JoinColumn1[0] = joinColumn1.replace(previousAlias + "\".", newAlias + "\".");
            } else if (joinColumn1.indexOf(previousAlias + ".") == 0) {
                items[i].JoinColumn1[0] = joinColumn1.replace(previousAlias + ".", newAlias + ".");
            }
            var joinColumn2 = items[i].JoinColumn2[0];
            if (joinColumn2.indexOf("\"" + previousAlias + "\".") == 0) {
                items[i].JoinColumn2[0] = joinColumn2.replace(previousAlias + "\".", newAlias + "\".");
            } else if (joinColumn2.indexOf(previousAlias + ".") == 0) {
                items[i].JoinColumn2[0] = joinColumn2.replace(previousAlias + ".", newAlias + ".");
            }
            var rightTable = items[i].RightTable[0];
            if (previousAlias == rightTable) {
                items[i].RightTable[0] = newAlias;
            }
        }

        joinRulesTable.setStore(joinRuleStore);
        //and update the combo options...


//        var structure = getTableStructure(joinColumnTable);
//        var previousOptions = structure.cells[0][0].options;
//        var newOptions = new Array();
//        var storeOptions = new Array();
//        for (var i = 0; i < previousOptions.length; i++) {
//            if (previousOptions[i].indexOf("\"" + previousAlias + "\".") == 0) {
//                newOptions[i] = previousOptions[i].replace("\"" + previousAlias + "\".", "\"" + newAlias + "\".");
//            } else if (previousOptions[i].indexOf(previousAlias + ".") == 0) {
//                newOptions[i] = previousOptions[i].replace(previousAlias + ".", newAlias + ".");
//            }
//            else {
//                newOptions[i] = previousOptions[i];
//            }
//            storeOptions.push({
//                label:newOptions[i],
//                name:newOptions[i]
//            });
//
//        }

      //  structure.cells[0][0].options = newOptions;
      //  joinColumnTable.setStructure(structure);
//
//        var columnStore = new dojo.data.ItemFileReadStore({
//            data:{items:storeOptions}
//        });
//
//        dijit.byId("edit_join_column1").set("store", columnDataStore);
//
//        storeOptions = dojo.clone(storeOptions);
//        columnStore = new dojo.data.ItemFileReadStore({
//            data:{items:storeOptions}
//        });
//        dijit.byId("edit_join_column2").set("store", columnDataStore);


        var options = getTableAliasOptions();
        var lefetTableValue = dijit.byId("join_left_table_combo").get("value");

        dijit.byId("join_left_table_combo").set("options", options);
        dijit.byId("join_left_table_combo").startup();
        //value //


        if (lefetTableValue == previousAlias) {
            dijit.byId("join_left_table_combo").set("value", newAlias);

        } else {
            if (lefetTableValue == "") {
                lefetTableValue = " ";
            }
            dijit.byId("join_left_table_combo").set("value", lefetTableValue);
        }

        var optionsx = getTableAliasOptions();
        optionsx.push({
            label:blank_str_label, value:" "});

        lefetTableValue = dijit.byId("edit_join_right_table").get("value");
        dijit.byId("edit_join_right_table").set("options", optionsx);
        dijit.byId("edit_join_right_table").startup();


        if (lefetTableValue == previousAlias) {
            dijit.byId("edit_join_right_table").set("value", newAlias);
        } else {
            if (lefetTableValue == "") {
                lefetTableValue = " ";
            }
            dijit.byId("edit_join_right_table").set("value", lefetTableValue);
        }


        var joinColumn1 = dijit.byId("edit_join_column1").get("value");
        if (joinColumn1.indexOf("\"" + previousAlias + "\".") == 0) {
            dijit.byId("edit_join_column1").set("value", joinColumn1.replace(previousAlias + "\".", newAlias + "\"."));
        } else if (joinColumn1.indexOf(previousAlias + ".") == 0) {
            dijit.byId("edit_join_column1").set("value", joinColumn1.replace(previousAlias + ".", newAlias + "."));
        }

        var joinColumn2 = dijit.byId("edit_join_column2").get("value");
        if (joinColumn2.indexOf("\"" + previousAlias + "\".") == 0) {
            dijit.byId("edit_join_column2").set("value", joinColumn2.replace(previousAlias + "\".", newAlias + "\"."));
        } else if (joinColumn2.indexOf(previousAlias + ".") == 0) {
            dijit.byId("edit_join_column2").set("value", joinColumn2.replace(previousAlias + ".", newAlias + "."));
        }

        select_join_rule();

        /////////////////////Handle new column display//////////////////////////////
        if (previousAlias != newAlias)
        {
            columnDataStore.fetch({
            onComplete: function(items, request){
                dojo.forEach(items,function(item){
                    if (item.tableAlias == previousAlias)
                    {
                        columnDataStore.setValue(item, "tableAlias", newAlias);
                        columnDataStore.setValue(item, "name", newAlias + "." + item.columnName);
                        columnDataStore.setValue(item, "label", newAlias + "." + item.columnName);
                    }

                });
            }
        });
        }




//	}
    }

    function getDBInfoFromDTO(operator) {
        var dbinfo = {};
        if (operator.classname == "StratifiedSamplingOperator"
            || operator.classname == "RandomSamplingOperator") {
            return dbinfo;
        }
        dbinfo.opuid = operator.uid;
        var inputTableInfo = CurrentOperatorDTO.inputTableInfos;
        if (!operator.operatorDTO || !operator.operatorDTO.propertyList || operator.classname == "SubFlowOperator" || operator.classname == "SampleSelectorOperator") {
            //if the operator has none of DTO information. use output fields instead | MINERWEB:1200 get SampleSelector Properties
            for (var i = 0; i < inputTableInfo.length; i++) {
                if (inputTableInfo[i].operatorUUID == dbinfo.opuid) {
                    dbinfo.schema = inputTableInfo[i].schema;
                    dbinfo.table = inputTableInfo[i].table;
                    break;
                }
            }
            return dbinfo;
        }
        var propertyArray = operator.operatorDTO.propertyList;
        for (var x = 0; x < propertyArray.length; x++) {
            var prop = propertyArray[x];
            //prop could be null
            if (prop && prop.name == "dbConnectionName") {
                dbinfo.connection = prop.value;
            } else if (prop && (prop.name == "schemaName" || prop.name == "outputSchema")) {
                dbinfo.schema = prop.value;
            } else if (prop && (prop.name == "tableName" || prop.name == "outputTable" || prop.name == "selectedTable")) {
                dbinfo.table = prop.value;
            }
        }
        return dbinfo;
    }

    function filterTableJointModel(tableJoinModel) {

//	var parents = getParanetOperators(current_op);
        //var parents = alpine.flow.OperatorManagementManager.getPreviousOperators(current_op.uid);
        var parents = CurrentOperatorDTO.inputTableInfos;

        if (!parents) {
            return null;
        }
        var inputTables = new Array();
        var modifiedInputTables = new Array();
        for (var x = 0; x < parents.length; x++) {
           // var dbinfo = getDBInfo(parents[x]);
            if (parents[x].table) {
                inputTables[inputTables.length] = parents[x];
                modifiedInputTables[modifiedInputTables.length] = parents[x];
//			inputTable_schemas[inputTable_schemas.length] = dbinfo.schema; 
//			inputTables_opuids[inputTables_opuids.length] = dbinfo.opuid; 
            }
        }
        var joinTables = tableJoinModel.joinTables;
        var removedTables = new Array();
        for (var i = 0; i < joinTables.length; i++) {
            var joinTable = joinTables[i];
            var isInclude = false;
            for (var ii = 0; ii < inputTables.length; ii++) {
                if (inputTables[ii].operatorUUID == joinTable.operatorModelID) {
                    isInclude = true;
                    break;
                }
            }
            if (!isInclude) {
                //if current joinTable is not exist in inputTables means it was removed or reconnect. remove it.
                removedTables[removedTables.length] = joinTable.operatorModelID;
            }

            for (var j = 0; j < inputTables.length; j++) {
                if (inputTables[j].operatorUUID == joinTable.operatorModelID
                    && variableReplacer.replaceVariable(inputTables[j].schema) == variableReplacer.replaceVariable(joinTable.schema)
                    && variableReplacer.replaceVariable(inputTables[j].table) != variableReplacer.replaceVariable(joinTable.table)) {
                    //input changed .need refresh the model...
                    removedTables[removedTables.length] = joinTable.operatorModelID;
                }

                //use input table schema to instead join table schema.
                //joinTable.schema = inputTables[j].schema;
            }
        }

        var addTables = new Array();
        for (var i = 0; i < inputTables.length; i++) {
            var inputTable = inputTables[i];
            var found = false;
            for (var j = 0; j < joinTables.length; j++) {
                if (joinTables[j].operatorModelID == inputTable.operatorUUID
                    && variableReplacer.replaceVariable(joinTables[j].table) == variableReplacer.replaceVariable(inputTable.table)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                addTables[addTables.length] = {
                    table:modifiedInputTables[i].table,
                    uid:modifiedInputTables[i].operatorUUID,
                    schema:modifiedInputTables[i].schema
                };
            }
        }

        var msg = "";
        if (addTables.length > 0) {
            msg = msg + alpine.nls.join_table_added + "\n";
            for (var i = 0; i < addTables.length; i++) {
                msg = msg + addTables[i].table + "\n";
                addJoinTable(addTables[i].table, tableJoinModel, addTables[i].uid, addTables[i].schema);
            }
        }


        if (removedTables.length > 0) {
            msg = msg + alpine.nls.join_table_removed + "\n";
            for (var i = 0; i < removedTables.length; i++) {
                msg = msg + removedTables[i] + "\n";
                removeTableFromJoinModel(tableJoinModel, removedTables[i]);

            }
            msg = msg + alpine.nls.table_join_config_removed;

        }
//do we really neesd this?	
//	if(msg && msg.length>0){
//		popupComponent.alert(msg);
//	}


    }

    function addJoinTable(tableName, tableJoinModel, uid, schema) {
        var joinTables = tableJoinModel.joinTables;
        joinTables[joinTables.length] = {
            alias:randomAlias(),
            operatorModelID:uid,
            schema:schema,
            table:tableName
        };

    }

    function randomAlias() {
        var seed1 = Math.round(Math.random() * 100 % 25) + 97;
        var seed2 = Math.round(Math.random() * 100 % 25) + 97;
        return String.fromCharCode(seed1, seed2);
    }

    function removeTableFromJoinModel(tableJoinModel, removedTableID) {
        if (removedTableID && tableJoinModel) {
            var joinTables = tableJoinModel.joinTables;
            var removedTableAlias = null;
            if (joinTables) {
                for (var i = 0; i < joinTables.length; i++) {
                    if (joinTables[i].operatorModelID == removedTableID) {
                        removedTableAlias = joinTables[i].alias;
                        RemoveArrayElement(joinTables, joinTables[i]);
                        break;
                    }
                }
            }

            var joinColumns = tableJoinModel.joinColumns;
            var removedColumns = new Array;
            if (removedTableAlias) {
                for (var i = 0; i < joinColumns.length; i++) {
                    if (joinColumns[i].tableAlias == removedTableAlias) {
                        removedColumns[removedColumns.length] = joinColumns[i];
                        //RemoveArrayElement(joinColumns,joinColumns[i]) ;

                    }
                }
                for (var i = 0; i < removedColumns.length; i++) {
                    RemoveArrayElement(joinColumns, removedColumns[i]);
                }
            }


            var removedConditions = new Array;
            var joinConditions = tableJoinModel.joinConditions;
            if (removedTableAlias) {
                for (var i = 0; i < joinConditions.length; i++) {
                    if (joinConditions[i].tableAlias1 == removedTableAlias
                        || joinConditions[i].tableAlias2 == removedTableAlias) {
                        removedConditions[removedConditions.length] = joinConditions[i];


                    }
                }
                for (var i = 0; i < removedConditions.length; i++) {
                    RemoveArrayElement(joinConditions, removedConditions[i]);
                }
            }

        }


    }

//CurrentOperatorDTO it the operator...
    function initTableJoinDialog(current_tableJoin) {
        //once the input table has been changed, the model need refresh auto
        filterTableJointModel(current_tableJoin.tableJoinModel);


        var joinTables = current_tableJoin.tableJoinModel.joinTables;
        var joinColumns = current_tableJoin.tableJoinModel.joinColumns;
        var joinConditions = current_tableJoin.tableJoinModel.joinConditions;


        previousAliasArray = new Array();
        for (var i = 0; i < joinTables.length; i++) {
            previousAliasArray[i] = joinTables[i].alias;
        }

        var inputTableInfos = CurrentOperatorDTO.inputTableInfos;
        var avaliableColumns = new Array();
        var avaliableColumnsWithOutArray = new Array();
        var index = 0;


        //nothing to do ...
        if (!inputTableInfos) {
            //clean the table
            var emptyArray = new Array();
            initAliasTables(emptyArray);
            initColumnData(emptyArray);
            initJoinConditionTables(emptyArray, emptyArray);
            createButtonsAndStackForAllOperators(current_tableJoin);
            return;
        }
//	var joinTableAliasRefer = dojo.clone(joinTables);
        // make sure sequence of joinTableAliasRefer is consistance with inputTableInfos. In order to fix MINERWEB-729
        var joinTableAliasRefer = getTableAliasRefer(inputTableInfos, joinTables);
        console.log("here we are!");
        for (var i = 0; i < inputTableInfos.length; i++) {
            var columnNames = inputTableInfos[i].fieldColumns;
            if (i > 0) {
                joinTableAliasRefer.splice(0, 1);//remove first element from joinTables. Avoid to join same table.
            }
            for (var j = 0; j < columnNames.length; j++) {
                var alias = getJoinTableAlias(joinTableAliasRefer, inputTableInfos[i].table);
                var column = alias + "." + columnNames[j][0];
                avaliableColumns[index++] = column;
                //avoid the array in the condition...
                if (dojo.indexOf(column_type_array, columnNames[j][1]) < 0) {
                    avaliableColumnsWithOutArray.push(column);
                }


            }
        }

        var removedColumns = new Array();
        if (joinColumns) {
            for (var x = 0; x < joinColumns.length; x++) {
                var col = joinColumns[x];
                if (joinColumnExists(col, inputTableInfos, joinTables) == false) {
                    removedColumns[removedColumns.length] = col;
                }
            }
        }
        //some columns are not in the avaliable columns...
        for (var y = 0; y < removedColumns.length; y++) {
            RemoveArrayElement(joinColumns, removedColumns[y]);
        }

        initAliasTables(joinTables);
        initColumnData(joinColumns);
        initJoinConditionTables(joinConditions, avaliableColumnsWithOutArray);
        createButtonsAndStackForAllOperators(current_tableJoin);


    }

    function getTableAliasRefer(inputTableInfos, joinTableList) {
        var joinTableArray = new Array(),
            joinTables = dojo.clone(joinTableList);
        for (var i = 0; i < inputTableInfos.length; i++) {
            var inputTable = variableReplacer.replaceVariable(inputTableInfos[i].table);
            for (var j = 0; j < joinTables.length; j++) {
                var joinTableName = variableReplacer.replaceVariable(joinTables[j].table);
                if (joinTableName == inputTable) {
                    joinTableArray[i] = joinTables[j];
                    joinTables.splice(j, 1);
                    break;
                }
            }
        }
        return joinTableArray;
    }


    function joinColumnExists(col, inputTableInfos, joinTables) {
        var tableAliasRefer = getTableAliasRefer(inputTableInfos, joinTables);
        for (var i = 0; i < inputTableInfos.length; i++) {
            var columnNames = inputTableInfos[i].fieldColumns;
            if (i > 0) {
                tableAliasRefer.splice(0, 1);//remove first element from joinTables. Avoid to join same table.
            }
            var alias = getJoinTableAlias(tableAliasRefer, inputTableInfos[i].table);
            for (var j = 0; j < columnNames.length; j++) {

                if (col.columnName == columnNames[j][0]
                    && col.tableAlias == alias) {
                    return true;
                }
            }
        }
        return false;
    }

    function getJoinTableAlias(joinTables, tableName) {

        for (var i = 0; i < joinTables.length; i++) {
            if (joinTables[i].table == tableName) {
                return  joinTables[i].alias;
            }
        }
    }


    function _showTableJoinDialog(op_info) {

        if (!multiFileUtil.validateDBInputFiles(CurrentOperatorDTO.inputTableInfos)) {return false;}
        if(multiFileUtil.validateDBInputFromSame(CurrentOperatorDTO.inputTableInfos)==false){return false;}

        dojo.query("#addJoinColumnBtn>span").style("width", "45px");
        dojo.query("#removeJoinColumnBtn>span").style("width", "45px");
        dijit.byId(tableJoinDialog_ID).titleBar.style.display='none';

        dijit.byId(tableJoinDialog_ID).show();
        dijit.byId(tableJoinDialog_ID).resize(500, 701);
        //avoid the nullpoint
        if (!current_tableJoin.tableJoinModel) {
            current_tableJoin.tableJoinModel = {joinTables:[

            ],
                joinColumns:[],
                joinConditions:[]
            };
        }
        current_tableJoin.opInfo = op_info

        initTableJoinDialog(current_tableJoin);


    }

    function validate_table_join() {

        if(null==columnDataStore){
            return false;
        }
        //validate new column store
        var numColumnsSelected = 0;
        columnDataStore.fetch( { query: { selected: true },
            onComplete: function(items, request){
                if (items) numColumnsSelected = items.length;
            }
        });

        //validate same column name

        if (numColumnsSelected == 0 )
        {
            popupComponent.alert(alpine.nls.Table_Join_Error_Join_Column_Empty);
            return false;
        }
//
//        if (null == joinColumnStore) {
//            return false;
//        }


//        if (!joinColumnStore || !joinColumnStore._arrayOfTopLevelItems || joinColumnStore._arrayOfTopLevelItems.length < 1) {
//            popupComponent.alert(alpine.nls.join_column_empty);
//            return false;
//        } else {
//
//            var items = joinColumnStore._arrayOfTopLevelItems;
//            for (var i = 0; i < items.length; i++) {
//                var item_i = items[i];
//                for (var j = 0; j < items.length; j++) {
//                    if (i != j) {
//                        var item_j = items[j];
//                        if (item_j.joinColumn[0] == item_i.joinColumn[0]) {
//                            popupComponent.alert(alpine.nls.join_column_same + item_j.joinColumn[0]);
//                            return false;
//                        }
//                    }
//
//                }
//            }
//
//        }
        if (!joinRuleStore || !joinRuleStore._arrayOfTopLevelItems || joinRuleStore._arrayOfTopLevelItems.length < 1) {
            popupComponent.alert(alpine.nls.join_condition_empty);
            return false;
        }

        return validate_join_rules();
    }

    function validate_join_rules() {
        var conditions = joinRuleStore._arrayOfTopLevelItems;

        for (var i = 0; i < conditions.length; i++) {


            if (conditions[i].JoinType[0].replace(/^\s+|\s+$/g, '') != "" && conditions[i].RightTable[0].replace(/^\s+|\s+$/g, '') == "") {
                popupComponent.alert(alpine.nls.right_table_empty);
                return false;
            }
        }


        return true;
    }

    function update_table_join_data() {
        if (validate_table_join() == false) {
            return;
        }
        //modify later
        var newTableJoinModel = {joinTables:[], joinColumns:[], joinConditions:[]};
        var oldTableJoinModel = current_tableJoin.tableJoinModel;


        //modify later
        if (tableAliasStore && tableAliasStore._arrayOfTopLevelItems) {
            var alias = tableAliasStore._arrayOfTopLevelItems;
            for (var i = 0; i < alias.length; i++) {
                newTableJoinModel.joinTables[i] = {
                    table:alias[i].tableName[0],
                    alias:alias[i].tableAlias[0],
                    operatorModelID:oldTableJoinModel.joinTables[i].operatorModelID,
                    schema:oldTableJoinModel.joinTables[i].schema
                };
            }
        }


        var newColumnsNameList = new Array();
        columnDataStore.fetch( { query: { selected: true },
            onComplete: function(items, request){
                dojo.forEach(items,function(item){
                    var alias = item.tableAlias[0];
                    var columnName = item.columnName[0];
                    var newColumnName = columnName;
                    if (newColumnsNameList.indexOf(newColumnName) > -1) {
                        newColumnName = alias + "." + newColumnName;

                    }
                    newColumnsNameList.push(newColumnName);
                    newTableJoinModel.joinColumns.push(
                        {
                            tableAlias:alias,
                            columnName:columnName,
                            newColumnName:newColumnName,
                            columnType:item.columnType[0]
                        }
                    )
                });
            }
        });


        /**
         * JoinType : joinConditions[i].joinType,
         JoinColumn1 : joinConditions[i].tableAlias1 + "."
         + joinConditions[i].column1,
         JoinCondition : conditionStr,
         JoinColumn2 : joinConditions[i].tableAlias2 + "."
         + joinConditions[i].column2
         * */
        if (joinRuleStore && joinRuleStore._arrayOfTopLevelItems) {
            var conditions = joinRuleStore._arrayOfTopLevelItems;

            for (var i = 0; i < conditions.length; i++) {

                newTableJoinModel.joinConditions[i] = {
                    joinType:conditions[i].JoinType[0],
                    tableAlias1:dijit.byId("join_left_table_combo").get("value"),
                    column1:conditions[i].JoinColumn1[0],
                    condition:conditions[i].JoinCondition[0],
                    tableAlias2:conditions[i].RightTable[0],
                    column2:conditions[i].JoinColumn2[0],
                    andOr:conditions[i].AndOr[0]
                };
            }
        }

        //update the operator parameter...
        current_tableJoin.tableJoinModel = newTableJoinModel;
        setButtonBaseClassValid(getSourceButtonId(current_tableJoin));
        close_table_join_dialog();
    }

    function close_table_join_dialog() {
        tableAliasStore = null;

//        joinColumnStore = null;

        joinRuleStore = null;

        current_join_condition = null;

        dijit.byId(tableJoinDialog_ID).hide();

    }

    var update_join_column_lock = false;

    function update_join_column1() {

        var value = dijit.byId("edit_join_column1").get("value");
        var optionStore = dijit.byId("edit_join_column1").store._arrayOfTopLevelItems;
        var isInOptionArray = false;
        for (var i = 0; i < optionStore.length; i++) {
            if (value == optionStore[i].name[0]) {
                isInOptionArray = true;
                break;
            }
        }
        if (!isInOptionArray) {
            return;// if user typing in select then attached nothing.
        }
        if (value.indexOf("\"") != 0) {
            var newValue = value.replace("\.", "\".\"");
            newValue = "\"" + newValue + "\"";
            dijit.byId("edit_join_column1").set("value", newValue);
        }
    }

    function update_join_column2(value) {
        var value = dijit.byId("edit_join_column2").get("value");
        var optionStore = dijit.byId("edit_join_column1").store._arrayOfTopLevelItems;
        var isInOptionArray = false;
        for (var i = 0; i < optionStore.length; i++) {
            if (value == optionStore[i].name[0]) {
                isInOptionArray = true;
                break;
            }
        }
        if (!isInOptionArray) {
            return;// if user typing in select then attached nothing.
        }
        if (value.indexOf("\"") != 0) {
            var newValue = value.replace("\.", "\".\"");
            newValue = "\"" + newValue + "\"";
            dijit.byId("edit_join_column2").set("value", newValue);
        }
    }

    var tableJoinAliasSelectIdx;

//    function select_join_table_alias(event) {
//        //1 mutiple select is disable
//        //2 single select will enable the button
//        //3 single select will fill the text input
//        var currentRowIndex;
//        if (event.rowIndex == undefined) {
//            currentRowIndex = tableAliasTable.focus.rowIndex;
//        } else {
//            currentRowIndex = event.rowIndex;
//        }
//        var item = tableAliasTable.getItem(currentRowIndex);
//        tableAliasTable.selection.select(currentRowIndex);
//        if (!item) {
//          //  dijit.byId("tablejoin_alias_edit").set("value", "");
//            //dijit.byId("tablejoin_alias_update_btn").set("disabled", true);
//           // dijit.byId("tablejoin_alias_edit").set("disabled", true);
//            tableJoinAliasSelectIdx = -1;
//            return;
//        }
//
//
//        tableJoinAliasSelectIdx = currentRowIndex;
//
//    }

    function _updateAliasForInputTable(item) {
        var allRight = true;

        var aliasindex = +item.name.substring(9); //remove name prefix
        var editVal = item.value;
        //now get the new alias value.
        var regExp=/^[\w]+$/;
        if (!regExp.test(editVal))
        {
            popupComponent.alert(alpine.nls.Table_Join_Error_Invalid_Alias);
            return;
        }

//    allRight &= item.validate();


        if (aliasArray) {
            if (aliasArray) {
                for (var x = 0; x < aliasArray.length; x++) {
                    var alias = aliasArray[x].tableAlias[0];
                    if (aliasindex != x && alias == editVal) {
                        dijit.byId("tablejoin_dialog_ok_id").set("disabled", true);
                        popupComponent.alert(alpine.nls.table_alias_same);
                        allRight &= false;
                    }

                }
            }
        }

        if (!allRight) {
            return;
        }
        item._hasBeenBlurred = true;
        aliasArray[aliasindex].tableAlias[0] = editVal;
        var UUID = aliasArray[aliasindex].tableUUID[0];

        tableAliasByUUID[UUID] = editVal;

        onApplyAliasCellEditHandler(editVal, aliasindex);

    }



    return {
        showTableJoinDialog:_showTableJoinDialog,
       // updateTableJoinTableAlias:_updateTableJoinTableAlias,
        addJoinRuleBtn_Click:addJoinRuleBtn_Click,
        clearJoinRuleBtn_Click:clearJoinRuleBtn_Click,
        //addJoinColumnBtn_Click:addJoinColumnBtn_Click,
        close_table_join_dialog:close_table_join_dialog,
        update_table_join_data:update_table_join_data,
        //removeJoinColumnBtn_Click:removeJoinColumnBtn_Click,
        updateJoinRuleBtn_Click:updateJoinRuleBtn_Click,
        removeJoinRuleBtn_Click:removeJoinRuleBtn_Click,
        update_join_column1:update_join_column1,
        update_join_column2:update_join_column2

    };
});