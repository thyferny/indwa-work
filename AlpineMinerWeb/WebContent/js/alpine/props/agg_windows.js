/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * agg_windows.js
 * 
 * Author sam_zang
 * Version 3.0
 * Date Dec 4, 2011
 */

var AggFullWindowListGrid = null;
var CurrentWindowFieldsModel = null;
var AggColumnList = null;
var agg_windowGrid = null; 
var agg_windowStore= null;
var sourceButtonId = null;


function showAggregateWindowEditDialog(prop) {
	if (prop.windowFieldsModel == null) {
		var model = {};
		model.windowFieldList = new Array();
		prop.windowFieldsModel = model;
	}
	CurrentWindowFieldsModel = prop.windowFieldsModel;
	create_agg_windowGrid(prop);
	
	var id = "agg_window_list";
	var func = click_agg_window_full_column_list;
	AggFullWindowListGrid = create_default_column_list(prop.store, id, func);

	AggColumnList = prop.fullSelection;
	//disable_agg_window_buttons();
	clear_agg_window_from();
	
	var colName = dijit.byId("agg_win_result_column");
	var handler = dojo.connect(colName, "onKeyUp", function() {
		agg_window_button_mode(colName.get("value"));
	});
	sp_property_event_handlers.push(handler);

    sourceButtonId = getSourceButtonId(prop);
	aggregateColumnChecker.initialize(prop.type);
	dijit.byId("aggregateWindowsConfigEditDialog").titleBar.style.display = 'none';
	dijit.byId("aggregateWindowsConfigEditDialog").show();
}

function close_agg_window_dialog(){ 
	//release resource here...
    _aggValidateBtn();
	dijit.byId('aggregateWindowsConfigEditDialog').hide();
    dojo.byId("agg_win_result_column").disabled = "";
}

function click_agg_window_full_column_list(event) {
	var item = AggFullWindowListGrid.getItem(event.rowIndex);
	// item.name is the column name.
	var val = dojo.byId("agg_win_spec").value;
	val = val + " " + item.name + " ";
	dojo.byId("agg_win_spec").value = val;						
}

function update_agg_window_data() {
	

	var updateModel = function(items, request) {
		var model = CurrentWindowFieldsModel;
		model.windowFieldList = new Array();
		for (var i = 0; i < items.length; i++) {
			var agg = {};
			agg.resultColumn = items[i].resultColumn[0];
			agg.windowFunction = items[i].windowFunction[0];
			agg.windowSpecification = items[i].windowSpecification[0];
			agg.dataType = items[i].dataType[0];
			model.windowFieldList[i] = agg;
		}
		aggregateColumnChecker.storeCurrentColumns(model.windowFieldList);
	};
	
	var request = agg_windowStore.fetch({
			 query: { resultColumn : "*"}, 
			 onComplete: updateModel});

    setButtonBaseClassValid(sourceButtonId);
	close_agg_window_dialog();
}

function create_agg_windowGrid(prop) {

	var model = CurrentWindowFieldsModel;
	var agg_windowData = dojo.clone(model.windowFieldList);
	var store = {
		identifier : 'resultColumn',
		label : 'resultColumn',
		items : agg_windowData
	};
	agg_windowStore = new dojo.data.ItemFileWriteStore({
		data : store
	});

	// set the layout structure:

	if(agg_windowGrid==null){
		var layout = [ {
			field : 'resultColumn',
			name : alpine.nls.agg_win_result_column,
			width : '100px'

		}, {
			field : 'dataType',
			name : alpine.nls.agg_win_data_type,
			width : '120px'
		}, {
			field : 'windowFunction',
			name : alpine.nls.agg_win_function,
			width : '100px'
		}, {
			field : 'windowSpecification',
			name : alpine.nls.agg_win_spec,
			width : 'auto'
		} ];

		var panel = dojo.byId("agg_window_gridpane");
		if (panel.firstChild) {
			panel.removeChild(panel.firstChild);
		}
		var gridDomNode =document.createElement('div');
		panel.appendChild(gridDomNode);
		// create a new agg_window grid:
		agg_windowGrid = new dojox.grid.DataGrid({
			query : {
				resultColumn : '*'
			},
			store : agg_windowStore,
			clientSort : true,
			rowSelector : '10px',
			selectionMode: "single",
			structure : layout
		}, gridDomNode);
	
	
	
		  dojo.connect(agg_windowGrid, "onRowClick", click_agg_window);
	 
		agg_windowGrid.startup();
	}else{
		agg_windowGrid.setStore(agg_windowStore);
		agg_windowGrid.render();
	}
}

function click_agg_window(event) {
	var items = [];
	if(event && event.rowIndex==undefined){
		var currentRowIndex = agg_windowGrid.focus.rowIndex;		
		items[0] = agg_windowGrid.getItem(currentRowIndex);
		agg_windowGrid.selection.select(currentRowIndex);
	}else{
		 
		items = agg_windowGrid.selection.getSelected();
	}


	
	if(!items||!items[0]) {//no selected or cancel select
		dojo.byId("agg_win_result_column").value = "";
		dojo.byId("agg_win_function").value = "";
		dojo.byId("agg_win_spec").value = "";
		dojo.byId("agg_win_data_type").value = "";
		
		dijit.byId("agg_window_create_button").set("disabled", true);
		dijit.byId("agg_window_update_button").set("disabled", true);
		dijit.byId("agg_window_delete_button").set("disabled", true);
		
		return;
	}else{
		var agg =  items[0];
	 
		dojo.byId("agg_win_result_column").value = agg.resultColumn;
        dojo.byId("agg_win_result_column").disabled = "disabled";
		dojo.byId("agg_win_function").value = agg.windowFunction;
		dojo.byId("agg_win_spec").value = agg.windowSpecification;
		dojo.byId("agg_win_data_type").value = agg.dataType;
		
		enable_agg_window_update();
        enable_agg_window_delete();
	}
	
}

function clear_agg_window_from() {
	dojo.byId("agg_win_result_column").value = "";
	dojo.byId("agg_win_function").value = "";
	dojo.byId("agg_win_spec").value = "";
    dojo.byId("agg_win_result_column").disabled = "";
    agg_windowGrid.selection.clear();
    disable_agg_window_buttons();
}

function create_agg_window() {
	if (!agg_windowForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
    var specifiedName = dijit.byId("agg_win_result_column").get("value");
	if(aggregateColumnChecker.isExists(specifiedName)){
		popupComponent.alert(alpine.nls.agg_column_validation_name_exist);
		return;
	}
    if (is_in_column_list(AggColumnList, specifiedName) == true) {
        popupComponent.alert(alpine.nls.agg_win_column_exist);
        return;
    }
	var agg = get_agg_window_data();
    clear_agg_window_from();
	var doCreate = function(items, request) {
		if (items.length == 0) {
			agg_windowStore.newItem(agg);
			agg_windowStore.save();
			agg_windowGrid.startup();
			agg_windowGrid.render();			  
		}
		else {
			popupComponent.alert(alpine.nls.agg_win_column_exist);
		}
	};
	
	var request = agg_windowStore.fetch({
			 query: { resultColumn : agg.resultColumn }, 
			 onComplete: doCreate});

}

function is_in_column_list(list, value) {	
	var result = false;	
	if (list == null) {
		return result;
	}
	for (var i = 0; i < list.length; i++) {
		if (value == list[i]) {
			result = true;
			break;
		}
	}
	return result;
}

function update_agg_window() {
	if (!agg_windowForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	var item = get_agg_window_data();
	var items = agg_windowGrid.store._arrayOfAllItems;
    if (null!=items && items.length>0) {
        for (var i = 0; i < items.length; i++) {
            if(items[i]==null) {continue;}
            if(items[i].resultColumn[0]==item.resultColumn) {
                agg_windowStore.deleteItem(items[i]);
            }
        }
    }
	agg_windowStore.save();
	agg_windowStore.newItem(item);
	agg_windowStore.save();

	agg_windowGrid.startup();
	agg_windowGrid.render();

    clear_agg_window_from();
}

function delete_agg_window() {
	if (!agg_windowForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	var items = agg_windowGrid.selection.getSelected();
	if (items.length) {
		dojo.forEach(items, function(selectedItem) {
			if (selectedItem !== null) {
				agg_windowStore.deleteItem(selectedItem);
				aggregateColumnChecker.remove(selectedItem.resultColumn[0]);
			}
		});
	}
	agg_windowStore.save();
    clear_agg_window_from();
}

function get_agg_window_data() {
	var agg = {};
	agg.resultColumn = dojo.byId("agg_win_result_column").value;
	agg.windowFunction = dojo.byId("agg_win_function").value;
	agg.windowSpecification = dojo.byId("agg_win_spec").value;
	agg.dataType = dojo.byId("agg_win_data_type").value;
	return agg;
}


function agg_window_button_mode(value) {
	var list = agg_windowStore._arrayOfTopLevelItems;
	if (list && list.length > 0) {
		for (var i = 0; i < list.length; i++) {
			if (list[i].resultColumn[0] == value) {
				enable_agg_window_update();
				return;
			}
		}
	}

	enable_agg_window_create();
}
function enable_agg_window_create() {
	dijit.byId("agg_window_create_button").set("disabled", false);
	dijit.byId("agg_window_update_button").set("disabled", true);
	dijit.byId("agg_window_delete_button").set("disabled", true);
}

function enable_agg_window_update() {
	dijit.byId("agg_window_create_button").set("disabled", true);
	dijit.byId("agg_window_update_button").set("disabled", false);
	//dijit.byId("agg_window_delete_button").set("disabled", false);
}

function enable_agg_window_delete() {
    dijit.byId("agg_window_delete_button").set("disabled", false);
}

function disable_agg_window_buttons() {
	dijit.byId("agg_window_create_button").set("disabled", true);
	dijit.byId("agg_window_update_button").set("disabled", true);
	dijit.byId("agg_window_delete_button").set("disabled", true);
}
