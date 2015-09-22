/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * adaboost.js
 * 
 * Author sam_zang
 * Version 3.0
 * Date Dec 3, 2011
 */


 define([], function()
 {


var CurrentAboostModel = null;
var CurrentAboostDataList = null;
var CurrentAdaboostOp = null;
var isNum = false;
var iconpath = baseURL + "/images/icons/";

var adaboost_paramGrid = null;
var adaboost_paramStore = null ;
var sourceButtonId = null;


function showAdaboostDialog(prop) {
	var id = "dependentColumn" + ID_TAG;
	var columnName = dojo.byId(id).value;
	if (columnName == null || columnName == "") {
		popupComponent.alert(alpine.nls.select_dep_column_first);
		return;
	}
	CurrentAboostModel = null;
	CurrentAboostDataList = null;
	CurrentAdaboostOp = null;
	isNum = false;
	
	if (prop.adaboostModel == null) {
		var model = {};
		model.adaboostDataList = new Array();
		prop.adaboostModel = model;
	}
	CurrentAboostModel = prop.adaboostModel;
	
	adaboostCleanDojoItemAttrs(CurrentAboostModel.adaboostDataList);
	
	CurrentAboostDataList = dojo.clone(CurrentAboostModel.adaboostDataList);
	create_adaboost_paramGrid(prop);

	// get dependent column type.
	if ( get_quantile_type(columnName) == "Numeric") {
		 dijit.byId("adaboost_cart_tree").set("disabled", true);                               
		 dijit.byId("adaboost_neural_network").set("disabled", true); 
		 isNum = true;  
	}
	else {
		 dijit.byId("adaboost_cart_tree").set("disabled", false);                               
		 dijit.byId("adaboost_neural_network").set("disabled", false);   
	}

	clear_adaboost_property_table();
    sourceButtonId = getSourceButtonId(prop);
    dijit.byId("adaboostDialog").titleBar.style.display = "none";
    dijit.byId("adaboostDialog").show();
	
}

function adaboostCleanDojoItemAttrs(adaboostDataList){
	for(var i = 0;i < adaboostDataList.length;i++){
		if(adaboostDataList[i].classname == "SVMClassificationOperator"){
			var props = adaboostDataList[i].propertyList;
			for(var j = 0;j < props.length;j++){
				var prop = props[j];
				if(prop.type == "PT_SINGLE_SELECT"){//maybe even more.
					for(var a = 0;a < prop.store.items.length;a++){
						prop.store.items[a] = {
							name: dojo.isArray(prop.store.items[a].name) ? prop.store.items[a].name[0] : prop.store.items[a].name
						};
					}
				}
			}
		}
	}
}

function clear_adaboost_property_table() {
	var tbl = dojo.byId("adaboostTable");
	while (tbl.firstChild) {
		tbl.removeChild(tbl.firstChild);
	}
	CurrentAdaboostOp = null;
	var title = alpine.nls.adaboost_param_title;
	dojo.html.set(dojo.byId("adaboost_operator_name"), title);
	dojo.byId("adaboost_operator_icon").src = alpine.operatorexplorer.OperatorUtil.getStandardImageSourceByKey("AdaboostOperator");
	
}

function update_adaboost_param_data() {
	if (CurrentAdaboostOp != null) {
		if (!dijit.byId("adaboostForm").validate()) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		get_current_operator_data(CurrentAdaboostOp.propertyList,false);
	}
	if (isNum == true) {
		var items = adaboost_paramStore._arrayOfTopLevelItems;
		for ( var i = 0; i < items.length; i++) {
			var data = get_adaboost_data(items[i].name[0]);
			if (data.classname == "NeuralNetworkOperator" ||
					data.classname == "CartOperator") {
				popupComponent.alert(alpine.nls.ConnectionValid);
				return;
			}
		}
	}
    setButtonBaseClassValid(sourceButtonId);
    dijit.byId('adaboostDialog').hide();
			
	var updateModel = function(items, request) {
		var model = CurrentAboostModel;
		model.adaboostDataList = new Array();
		for ( var i = 0; i < items.length; i++) {
			var name = items[i].name[0];
			model.adaboostDataList[i] = get_adaboost_data(name);
		}
	}

	var request = adaboost_paramStore.fetch({
		query : {
			name : "*"
		},
		onComplete : updateModel
	});
}

function get_adaboost_data(name) {	
	for ( var i = 0; i < CurrentAboostDataList.length; i++) {
		if (CurrentAboostDataList[i].adaName == name) {
			return CurrentAboostDataList[i];
		}
	}
	return null;			
}

function copy_adaboost_template(name) {	
	var items = CurrentAboostModel.adaboostTemplateList;
	for ( var i = 0; i < items.length; i++) {
		if (items[i].adaName == name) {
			var data = make_copy(items[i]);
			//data.adaName = items[i].adaName;
			//data.adaType = items[i].adaType;
			//data.icon = items[i].icon;
			//data.propertyList = items[i].propertyList.clone();
			return data;
		}
	}
	return null;			
}

function make_copy(obj) {
    // Handle the 3 simple types, and null or undefined
    if (null == obj || "object" != typeof obj) return obj;

    // Handle Date
    if (obj instanceof Date) {
        var copy = new Date();
        copy.setTime(obj.getTime());
        return copy;
    }

    // Handle Array
    if (obj instanceof Array) {
        var copy = [];
        for(var i = 0; i < obj.length; i++) {
            copy[i] = make_copy(obj[i]);
        }
        return copy;
    }

    // Handle Object
    if (obj instanceof Object) {
        var copy = {};
        for(var attr in obj) {
            if (obj.hasOwnProperty(attr)) copy[attr] = make_copy(obj[attr]);
        }
        return copy;
    }

    throw new Error("Unable to copy obj! Its type isn't supported.");
}
function create_adaboost_paramGrid(prop) {

	var model = CurrentAboostModel;
	var dataList = model.adaboostDataList;

	var adaboost_paramData = new Array();
	if (dataList != null || dataList.length > 0) {							
		for ( var i = 0; i < dataList.length; i++) {
			adaboost_paramData[i] = {
				name : dataList[i].adaName
			};
		}
	}
	var store = {
		identifier : 'name',
		label : 'name',
		items : adaboost_paramData
	};
	adaboost_paramStore = new dojo.data.ItemFileWriteStore({
		data : store
	});
	
	if( adaboost_paramGrid == null ){

		// set the layout structure:
		var layout = [ {
			field : 'name',
			name : alpine.nls.cohorst_selected_operators,
			width : 'auto'
	
		} ];
	
		var panel = dojo.byId("adaboost_param_gridpane");
		if (panel.firstChild) {
			panel.removeChild(panel.firstChild);
		}
		var adaboostDomNode=document.createElement('div');
		panel.appendChild(adaboostDomNode);
		// create a new adaboost_param grid:
		adaboost_paramGrid = new dojox.grid.DataGrid({
			query : {
				name : '*'
			},
			store : adaboost_paramStore,
			clientSort : true,
			rowSelector : '10px',		
			structure : layout
		}, adaboostDomNode);
	
		
		dojo.connect(adaboost_paramGrid, "onRowClick", adaboost_paramGrid, select_adaboost_operator);
		adaboost_paramGrid.startup();
	}
	else{
		adaboost_paramGrid.setStore(adaboost_paramStore);
		adaboost_paramGrid.render();
	}
	
	
}

var adaboostEditRowIdx = -1;
function select_adaboost_operator(event) {
	if (!dijit.byId("adaboostForm").validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		if(adaboostEditRowIdx != -1){
			adaboost_paramGrid.selection.select(adaboostEditRowIdx);
		}
		return;
	}
	var currentRowIdx = this.focus.rowIndex;
	if(event.rowIndex == undefined){
		this.selection.select(currentRowIdx);
	}
	var items = adaboost_paramGrid.selection.getSelected();
	if((!items||!items[0]) && event.rowIndex != undefined) {//no selected or cancel select
		clear_adaboost_property_table();
		return;
	}else{
 
		var op = adaboost_paramGrid.getItem(currentRowIdx);
		
		if(dojo.indexOf(items,op)<0){
			//ctrl to cancel the select
			op = items[0];
			var allitems = adaboost_paramStore._arrayOfAllItems;
			adaboostEditRowIdx= dojo.indexOf(allitems,op);
			
		}else{
			adaboostEditRowIdx = currentRowIdx;
		}
	
	
		var data = get_adaboost_data(op.name[0]);
		edit_adaboost_property(data);
	}
}

function add_adaboost_operator(name) {
	if (!dijit.byId("adaboostForm").validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	
	var newName = get_unique_name(name);
	var newItem = { name : newName };
	create_adaboost_param(newItem);

	var data = get_adaboost_data(newName);
	if (data == null) {
		data = copy_adaboost_template(name);
		data.adaName = newName;
		CurrentAboostDataList[CurrentAboostDataList.length] = data;
	}
	edit_adaboost_property(data);
}

function edit_adaboost_property(data) {
	if (CurrentAdaboostOp != null) {
		get_current_operator_data(CurrentAdaboostOp.propertyList,false);
	}
	CurrentAdaboostOp = data;
	var img = dojo.byId("adaboost_operator_icon");
    //img.src = iconpath + data.icon;
    img.src = alpine.operatorexplorer.OperatorUtil.getStandardImageSourceByKey(data.classname);
	var title = data.adaName;
	dojo.html.set(dojo.byId("adaboost_operator_name"), title);
	
	//clean the table content
	var tbl = dojo.byId("adaboostTable");
	while (tbl.firstChild) {
		tbl.removeChild(tbl.firstChild);
	}
	for(var i = 0;i < data.propertyList.length;i++){
		var _widget = dijit.byId(data.propertyList[i].name + ID_TAG);
		if(_widget){
			dijit.registry.remove(data.propertyList[i].name + ID_TAG);
			_widget.destroyRecursive();
		}
	}
	
	initialize_property_table("adaboostTable", data.propertyList);
	applyCustomeRules(data);
	
}

function get_unique_name (name) {
	var exists = true;
	var count = 1;
	var tmpname = name;
	
	while (exists) {
		if (adaboost_paramStore
			&& adaboost_paramStore._arrayOfTopLevelItems) {
			items = adaboost_paramStore._arrayOfTopLevelItems;
			var found = false;
			for (var i = 0; i < items.length; i++) {
				if (items[i] && items[i].name[0] == tmpname) {
					tmpname = name + count;
					count ++;
					found = true;
					break;
				}
			}
			exists = found;
		}
		else { 
			return name; 
		}
	}
	return tmpname;
}

function create_adaboost_param(adaItem) {

	var doCreate = function(items, request) {
		if (items.length == 0) {
			adaboost_paramStore.newItem(adaItem);
			adaboost_paramStore.save();
			adaboost_paramGrid.render();
			adaboostEditRowIdx = adaboost_paramGrid.rowCount - 1;
			adaboost_paramGrid.selection.select(adaboostEditRowIdx);
		} else {
			popupComponent.alert(alpine.nls.adaboost_param_column_exist);
		}
	};

	var request = adaboost_paramStore.fetch({
		query : {
			name : adaItem.name
		},
		onComplete : doCreate
	});
}

function delete_adaboost_param() {
	var items = adaboost_paramGrid.selection.getSelected();
	if (items.length) {
		dojo.forEach(items, function(selectedItem) {
			if (selectedItem !== null) {
				adaboost_paramStore.deleteItem(selectedItem);
			}
		});
	}
	adaboost_paramStore.save();
	clear_adaboost_property_table();
}

function move_up_adaboost_param() {
	move_grid(adaboost_paramStore, adaboost_paramGrid, 1);
}

function move_down_adaboost_param() {
	move_grid(adaboost_paramStore, adaboost_paramGrid, -1);
}

function move_grid(store, grid, delta) {
	if (!dijit.byId("adaboostForm").validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	// delta should be 1 (up) or -1 (down)
	var selectedItems = grid.selection.getSelected();
	if (selectedItems.length == 0) {
		return;
	}
	items = store._arrayOfTopLevelItems;
	if (items.length < 2) {
		return;
	}

	var idxList = new Array();
	for (var i = 0; i < selectedItems.length; i++) {
		idxList[i] = grid.getItemIndex(selectedItems[i]);
	}

	if (idxList[0] == 0 && delta > 0) {
		return;
	}	
	if (idxList[selectedItems.length - 1] == items.length - 1 && delta < 0) {
		return;
	}	

	// move it.

	if (delta > 0) {
		for ( var i = 0; i < idxList.length; i++) {
			var item = items[idxList[i]];
			items[idxList[i]] = items[idxList[i] - delta];
			items[idxList[i] - delta] = item;
			grid.selection.setSelected(idxList[i], 0);
			grid.selection.setSelected(idxList[i] - delta, 1);
		}
	}
	else {
		for ( var i = idxList.length - 1; i >= 0; i--) {
			var item = items[idxList[i]];
			items[idxList[i]] = items[idxList[i] - delta];
			items[idxList[i] - delta] = item;
			grid.selection.setSelected(idxList[i], 0);
			grid.selection.setSelected(idxList[i] - delta, 1);
		}
	}

	store.save();
	grid.startup();
	grid.render();
}
   return {
       showAdaboostDialog: showAdaboostDialog,
       update_adaboost_param_data:update_adaboost_param_data,
       add_adaboost_operator:add_adaboost_operator,
       delete_adaboost_param:delete_adaboost_param,
       move_up_adaboost_param:move_up_adaboost_param,
       move_down_adaboost_param:move_down_adaboost_param,
       move_grid:move_grid

   }

 });
