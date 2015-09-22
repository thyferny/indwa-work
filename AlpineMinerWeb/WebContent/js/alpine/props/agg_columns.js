/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * agg_columns.js
 * 
 * Author sam_zang
 * Version 3.0
 * Date Dec 4, 2011
 */

var AggProperty = null;
var AggAvailableColumnGrid = null;
var CurrentAggregateFieldsModel = null;
var current_aggregator = "sum";
var agg_columnGrid;
var agg_columnStore;

function showAggregateColumnEditDialog(prop) {
	//aggeregate columns 
	AggProperty = prop;
	if (prop.aggregateFieldsModel == null) {
		var model = {};
		model.aggregateFieldList = new Array();
		prop.aggregateFieldsModel = model;
	}
	CurrentAggregateFieldsModel = prop.aggregateFieldsModel;
	create_agg_columnGrid(prop);
	
	
	//avaliable columns 
	set_available_column_list(prop, current_aggregator);

	disable_agg_column_buttons();
	clear_agg_column_from();
	var colName = dijit.byId("agg_column_alias");
	var handler =dojo.connect(colName, "onKeyUp", function() {
		agg_column_button_mode(colName.get("value"));
	});
	sp_property_event_handlers.push(handler);
	aggregateColumnChecker.initialize(prop.type);
	dijit.byId("aggregateColumnsConfigEditDialog").titleBar.style.display = 'none';
	dijit.byId("aggregateColumnsConfigEditDialog").show();
}

function close_agg_Columns_dialog(){
    _aggValidateBtn();
	dijit.byId('aggregateColumnsConfigEditDialog').hide();
    dojo.byId("agg_column_alias").disabled = "";
}

function _aggValidateBtn(){
    var fieldNum = aggregateFieldNum();;
    var windowNum = windowFieldListNum();
    var groupbyNum = aggGroupbyNum();

    var btnField = dijit.byId("aggregateFieldList"+ID_TAG);
    var btnWindow = dijit.byId("windowFieldList"+ID_TAG);
    var btnGroup = dijit.byId("groupByColumn"+ID_TAG);

    if(fieldNum>0){
        btnField.set("baseClass","workflowButton");
        btnField.focus();
    }else{
        btnField.set("baseClass","workflowButtonInvalid");
        btnField.focus();
    }
    if(windowNum>0){
        btnWindow.set("baseClass","workflowButton");
        btnWindow.focus();
    }else{
        btnWindow.set("baseClass","workflowButtonInvalid");
        btnWindow.focus();
    }

    if(groupbyNum>0 && (windowNum>0 || fieldNum>0)){
       // btnGroup.set("baseClass","workflowButton");
       // btnGroup.focus();
        //group property will take care of itself - its validation is independent of field and window

        btnField.set("baseClass","workflowButton");
        btnField.focus();

        btnWindow.set("baseClass","workflowButton");
        btnWindow.focus();
    }else{
         if(groupbyNum==0){
             btnGroup.set("baseClass","workflowButtonInvalid");
             btnGroup.focus();
             if(windowNum==0 && fieldNum==0){
                 btnField.set("baseClass","workflowButtonInvalid");
                 btnField.focus();

                 btnWindow.set("baseClass","workflowButtonInvalid");
                 btnWindow.focus();

             }else{
                 btnField.set("baseClass","workflowButton");
                 btnField.focus();

                 btnWindow.set("baseClass","workflowButton");
                 btnWindow.focus();
             }
         }
    }

}

function aggregateFieldNum(){
    var num = 0;
    var propAggField =getPropertyByName("aggregateFieldList");
    if(null!=propAggField
        && null!=propAggField.aggregateFieldsModel
        && null!=propAggField.aggregateFieldsModel.aggregateFieldList){
        num = propAggField.aggregateFieldsModel.aggregateFieldList.length;
    }
    return num;
}

function aggGroupbyNum(){
    var num = 0;
    var groupByColumn = getPropertyByName("groupByColumn");
    if(null!=groupByColumn
        && null!=groupByColumn.aggregateFieldsModel
        && null!=groupByColumn.aggregateFieldsModel.groupByFieldList){
        num = groupByColumn.aggregateFieldsModel.groupByFieldList.length;
    }
    return num;
}

function windowFieldListNum(){
    var num = 0;
    var windowFieldList = getPropertyByName("windowFieldList");
    if(null!=windowFieldList
        && null!=windowFieldList.windowFieldsModel
        && null!=windowFieldList.windowFieldsModel.windowFieldList ){
        num = windowFieldList.windowFieldsModel.windowFieldList.length;
    }
    return num;
}

function getPropertyByName(name){
    if(null!=CurrentOperatorDTO && null!=CurrentOperatorDTO.propertyList){
        for(var i=0;i<CurrentOperatorDTO.propertyList.length;i++){
            if(CurrentOperatorDTO.propertyList[i].name==name){
                return CurrentOperatorDTO.propertyList[i];
            }
        }
    }
    return null;
}


function click_agg_full_column_list(event) {
    resetAggColumnForm();
	AggAvailableColumnGrid.selection.select(AggAvailableColumnGrid.focus.rowIndex);
	var item = AggAvailableColumnGrid.getItem(AggAvailableColumnGrid.focus.rowIndex);
	// item.name is the column name.
    var buildName = current_aggregator + "_" + item.name;
	dojo.byId("agg_column_alias").value = buildName;
	dojo.byId("agg_column_expression").value = aggregateExpression = current_aggregator + "(\"" + item.name +"\")";
	dojo.byId("agg_column_dataType").value = getColumnOriginalType(item.name);
    agg_column_button_mode(buildName);
}


function set_aggregator_name(value) {
	current_aggregator = value;
	set_available_column_list(AggProperty, current_aggregator);
}

function set_available_column_list(prop, aggregator) {
	var id = "agg_column_list";
	var func = click_agg_full_column_list;
	var store = prop.store;
	
	switch (aggregator) {
	case "sum":
	case "avg":
	case "variance":
	case "stddev":
		store = filter_aggregate_column_store(prop, "Numeric");
		break;
	case "min":
	case "max":
		store = filter_aggregate_column_store(prop, "Numeric_DateTime");
		break;
	case "count":
	default:
		break;
	}
	AggAvailableColumnGrid = create_default_column_list(store, id, func);
}

function filter_aggregate_column_store(prop, type) {
	var store = {};
	store.identifier = "name";
	store.label = "name";
	store.items = new Array();

	var list = prop.fullSelection;
	var idx = 0;
	for (var i = 0; i < list.length; i++) {
		var cty = get_column_type(list[i]);
		if (type == "Numeric" && cty == "Numeric") {
			store.items[idx++] = { "name" : list[i]};
		}
		else if (type == "Numeric_DateTime" && 
				(cty == "Numeric" || cty == "DateTime")) {
			store.items[idx++] = { "name" : list[i]};
		}
	}
	return store;
}

//get original type with column
function getColumnOriginalType(columnName){
	var data = CurrentOperatorDTO.inputTableInfos;
	if (!data || data.length == 0) {
		return null;
	}
	var list = data[0].fieldColumns;
	var type = null;
	for (var i = 0; i < list.length; i++) {
		if (columnName == list[i][0]) {
			type = list[i][1];
			break;
		}
	}

	if (type == null) {
		return null;
	}else{
		return type;
	}
}

function get_column_type(columnName) {
	var data = CurrentOperatorDTO.inputTableInfos;
	if (!data || data.length == 0) {
		return null;
	}
	var list = data[0].fieldColumns;
	var type = null;
	for (var i = 0; i < list.length; i++) {
		if (columnName == list[i][0]) {
			type = list[i][1];
			break;
		}
	}

	if (type == null) {
		return null;
	}

    var reg = /^CHAR\w*/;
	if (reg.test(type) == true) {
		type = "CHAR";
	}

	switch (type) {
	case "DATE":
	case "TIME":
	case "TIMESTAMP":
		return "DateTime";
		break;

	case "TEXT":
	case "VARCHAR":
	case "BOOLEAN":
	case "CHAR":
		return "String";
		break;

	default:
		return "Numeric";
	}
	return null;
}
	 
function update_agg_column_data() {
	
	var updateModel = function(items, request) {				
		var model = CurrentAggregateFieldsModel;
		model.aggregateFieldList = new Array();
		for (var i = 0; i < items.length; i++) {
			var agg = {};
			agg.alias = items[i].alias[0];
			agg.aggregateExpression = items[i].aggregateExpression[0];
			agg.dataType = items[i].dataType[0];
			model.aggregateFieldList[i] = agg;
		}
		aggregateColumnChecker.storeCurrentColumns(model.aggregateFieldList);
	};
	
	var request = agg_columnStore.fetch({
			 query: { alias : "*"}, 
			 onComplete: updateModel});
    setButtonBaseClassValid(getSourceButtonId(AggProperty));
	close_agg_Columns_dialog();
}


function create_agg_columnGrid(prop) {

	var model = CurrentAggregateFieldsModel;
	var agg_columnData = dojo.clone(model.aggregateFieldList);

	var store = {
		identifier : 'alias',
		label : 'alias',
		items : agg_columnData
	};
	agg_columnStore = new dojo.data.ItemFileWriteStore({
		data : store
	});

	
	
	if(agg_columnGrid==null){
		// set the layout structure:
		var layout = [ {
			field : 'alias',
			name : alpine.nls.agg_column_alias,
			width : '120px'

		}, {
			field : 'aggregateExpression',
			name : alpine.nls.agg_column_expression,
			width : 'auto'
		} ];
		var panel = dojo.byId("agg_column_gridpane");
		if (panel.firstChild) {
			panel.removeChild(panel.firstChild);
		}
		var gridDomNode = document.createElement('div');
		panel.appendChild(gridDomNode);
		// create a new agg_column grid:
		agg_columnGrid = new dojox.grid.DataGrid({
			query : {
				alias : '*'
			},
			store : agg_columnStore,
			clientSort : true,
			rowSelector : '10px',
			selectionMode: "single",
			structure : layout
		}, gridDomNode);
	
	
		 dojo.connect(agg_columnGrid, "onRowClick", click_agg_column);
		agg_columnGrid.startup();
	
	}else{
		agg_columnGrid.setStore(agg_columnStore);
		agg_columnGrid.render();
	}
}

function click_agg_column(event) {

	var currentRowIdx = this.focus.rowIndex;
	//in case of entry click to select
	if(event.rowIndex == undefined){
		this.selection.select(currentRowIdx);
	}
	var agg = agg_columnGrid.getItem(currentRowIdx);
   
	var items = agg_columnGrid.selection.getSelected();
	if((!items||!items[0]) && event.rowIndex != undefined) {//no selected or cancel select
		dojo.byId("agg_column_alias").value = "";
		dojo.byId("agg_column_expression").value = "";
		dojo.byId("agg_column_dataType").value = "";
		dijit.byId("agg_column_update_button").set("disabled", true);
		dijit.byId("agg_column_delete_button").set("disabled", true);
		return;
	}else{
		if(dojo.indexOf(items,agg)<0){
			//ctrl to cacel the select
			agg = items[0];
		}
		dojo.byId("agg_column_alias").value = agg.alias;
        dojo.byId("agg_column_alias").disabled = "disabled";
		dojo.byId("agg_column_expression").value = agg.aggregateExpression;
		dojo.byId("agg_column_dataType").value = agg.dataType;
		enable_agg_column_update();
        enable_agg_column_delete();
	}
	

}
	


function clear_agg_column_from() {
	dojo.byId("agg_column_alias").value = "";
	dojo.byId("agg_column_expression").value = "";
	dojo.byId("agg_column_dataType").value = "";
}

function create_agg_column() {
	if (!agg_columnForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	if(aggregateColumnChecker.isExists(dijit.byId("agg_column_alias").get("value"))){
		popupComponent.alert(alpine.nls.agg_column_validation_name_exist);
		return;
	}

	var agg = get_agg_column_data();
	var doCreate = function(items, request) {
		if (items.length == 0) {
			agg_columnStore.newItem(agg);
			agg_columnStore.save();
			agg_columnGrid.startup();
			agg_columnGrid.render();	
			resetAggColumnForm();			  
		}
		else {
			popupComponent.alert(alpine.nls.agg_column_alias_exist);
		}
	};
	
	var request = agg_columnStore.fetch({
			 query: { alias : agg.alias }, 
			 onComplete: doCreate});							
}

function update_agg_column() {
	if (!agg_columnForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	var item = get_agg_column_data();
    var items = agg_columnGrid.store._arrayOfAllItems;
    if (null!=items && items.length>0) {
        for (var i = 0; i < items.length; i++) {
            if(items[i]==null) {continue;}
            if(items[i].alias[0]==item.alias) {
                agg_columnStore.deleteItem(items[i]);
            }
        }
    }
    agg_columnStore.save();
	agg_columnStore.newItem(item);
	agg_columnStore.save();
	
	agg_columnGrid.startup();
	agg_columnGrid.render();
	resetAggColumnForm();
}

function delete_agg_column() {
	if (!agg_columnForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	var d = get_agg_column_data();
    var items = agg_columnGrid.selection.getSelected();
    if (items.length) {         
        dojo.forEach(items, function(selectedItem) {
            if (selectedItem !== null) {                      
            	agg_columnStore.deleteItem(selectedItem);
            	aggregateColumnChecker.remove(selectedItem.alias[0]);
            }                  
        });               
    }
    agg_columnStore.save();
	resetAggColumnForm();
}

function resetAggColumnForm(){
	disable_agg_column_buttons();
	clear_agg_column_from();
    dojo.byId("agg_column_alias").disabled = "";
    var items = agg_columnGrid.selection.clear();
}

function get_agg_column_data() {
	var agg = {};
	agg.aggregateExpression = dojo.byId("agg_column_expression").value;
	agg.alias = dojo.byId("agg_column_alias").value;
	agg.dataType = dojo.byId("agg_column_dataType").value;
	return agg;
}

function agg_column_button_mode(value) {
	var list = agg_columnStore._arrayOfTopLevelItems;
	if (list && list.length > 0) {
		for (var i = 0; i < list.length; i++) {
			if (list[i].alias[0] == value) {
				enable_agg_column_update();
				return;
			}
		}
	}

	enable_agg_column_create();
}

function enable_agg_column_create() {
	dijit.byId("agg_column_create_button").set("disabled", false);
	dijit.byId("agg_column_update_button").set("disabled", true);
	dijit.byId("agg_column_delete_button").set("disabled", true);
}

function enable_agg_column_update() {
	dijit.byId("agg_column_create_button").set("disabled", true);
	dijit.byId("agg_column_update_button").set("disabled", false);
	//dijit.byId("agg_column_delete_button").set("disabled", false);
}

function enable_agg_column_delete() {
	dijit.byId("agg_column_delete_button").set("disabled", false);
}

function disable_agg_column_buttons() {
	dijit.byId("agg_column_create_button").set("disabled", true);
	dijit.byId("agg_column_update_button").set("disabled", true);
	dijit.byId("agg_column_delete_button").set("disabled", true);
}