
var VquantileCGrid = null;
var VquantileCurrentModel = null;
var ID_TAG_CHECK = "quantile_bin_use_range_id__";

var ID_TAG_NUM_START = "quantile_bin_num_start_id__";
var ID_TAG_TIMESTAMP_START = "quantile_bin_timestamp_start_id__";
var ID_TAG_DATE_START = "quantile_bin_date_start_id__";

var ID_TAG_NUM_END = "quantile_bin_num__end_id__";
var ID_TAG_TIMESTAMP_END = "quantile_bin_timestamp_end_id__";
var ID_TAG_DATE_END = "quantile_bin_date_end_id__";

var ID_TAG_NUM_VALUES = "quantile_bin_num_values_id__";
var ID_TAG_CATEGORY_VALUES = "quantile_bin_category_values_id__";

var ID_TAG_START_T = "quantile_bin_start_t_id__";
var ID_TAG_END_T = "quantile_bin_end_t_id__";
var Column_id = "var_quan_result_column__prop_form_value__";

var sourceButtonId = null;

function showVariableQuantileEditDialog(prop) {
	if (prop.quantileFieldsModel == null) {
		var model = {};
		model.quantileItems = new Array();
		prop.quantileFieldsModel = model;
	}
	VquantileCurrentModel = prop.quantileFieldsModel;
	//remove item which is in current columns any more. MINERWEB-750
	clearInvalidData(prop.store.items, VquantileCurrentModel.quantileItems);
	create_var_quantileGrid();

	var obj = dijit.byId(Column_id);
	if (obj) {
		obj.destroyRecursive(false);
		dijit.registry.remove(Column_id);
	}
	var btn = dojo.byId("var_quan_result_column");
	var data = {};
	data.name = "var_quan_result_column";
	data.displayName = "var_quan_result_column";
	data.value = null;
	data.store = prop.store;
	var fsel = generate_filtering_select(btn, data);
	if (fsel) {
		var handler = dojo.connect(fsel, "onChange", function(value) {
			set_button_mode(var_quantileStore, value);
		});
		
		sp_property_event_handlers.push(handler);
	}
	
	clear_var_quantile_from();
    sourceButtonId = getSourceButtonId(prop);
    dijit.byId("variableQuantileConfigEditDialog").titleBar.style.display = 'none';
    dijit.byId("variableQuantileConfigEditDialog").show();
}

function clearInvalidData(referenceColumns, columnList){
	checkColumn:
	for(var i = 0;i < columnList.length;i++){
		for(var j = 0;j < referenceColumns.length;j++){
			if(getVal(columnList[i].columnName == getVal(referenceColumns[j].name))){
				continue checkColumn;
			}
		}
		columnList.splice(i--, 1);
	}

	function getVal(val){
		return dojo.isArray(val) ? val[0] : val;
	};
}

function close_var_quantile_dialog() {
    _validateQuantileBtn();
	dijit.byId("variableQuantileConfigEditDialog").hide();
}
function update_var_quantile_data() {
	var updateModel = function(items, request) {
		var model = VquantileCurrentModel;
		model.quantileItems = new Array();
		for ( var i = 0; i < items.length; i++) {
			var val = {};
			val.columnName = items[i].columnName[0];
			val.numberOfBin = items[i].numberOfBin[0];
			val.isCreateNewColumn = items[i].isCreateNewColumn[0];
			val.quantileTypeLabel = items[i].quantileTypeLabel[0];
			model.quantileItems[i] = val;
		}
	};

	var request = var_quantileStore.fetch({
		query : {
			columnName : "*"
		},
		onComplete : updateModel
	});

    if (CurrentOperatorDTO.classname == "VariableOperator") {
        for (var i = 0; i < CurrentOperatorDTO.propertyList.length; i++) {
            if (CurrentOperatorDTO.propertyList[i].name == "fieldList") {
                if (CurrentOperatorDTO.propertyList[i].derivedFieldsModel == null) {
                    CurrentOperatorDTO.propertyList[i].derivedFieldsModel = {};
                }
                if (CurrentOperatorDTO.propertyList[i].derivedFieldsModel != null
                    && CurrentOperatorDTO.propertyList[i].derivedFieldsModel.selectedFieldList == null) {
                    CurrentOperatorDTO.propertyList[i].derivedFieldsModel.selectedFieldList = [];
                }
                if (CurrentOperatorDTO.propertyList[i].derivedFieldsModel != null
                    && CurrentOperatorDTO.propertyList[i].derivedFieldsModel.derivedFieldsList == null) {
                    CurrentOperatorDTO.propertyList[i].derivedFieldsModel.derivedFieldsList = [];
                }
            }
        }
    }

     close_var_quantile_dialog();
}

function _setDefVariableBtnValid(){
    setButtonBaseClassValid("fieldList"+ID_TAG);
    dijit.byId("fieldList"+ID_TAG).focus();
}
function _setDefVariableBtnInvalid(){
    dijit.byId("fieldList"+ID_TAG).set('baseClass', "workflowButtonInvalid");
    dijit.byId("fieldList"+ID_TAG).focus();
}

function _setQuantileVariableBtnValid(){
    setButtonBaseClassValid("quantileFieldList"+ID_TAG);
    dijit.byId("quantileFieldList"+ID_TAG).focus();
}
function _setQuantileVariableBtnInalid(){
    dijit.byId("quantileFieldList"+ID_TAG).set('baseClass', "workflowButtonInvalid");
    dijit.byId("quantileFieldList"+ID_TAG).focus();
}

function _validateQuantileBtn(){
    if(CurrentOperatorDTO.classname == "VariableOperator"){
        var variableNum = 0;
        var quantileVariableNum = 0;
        variableNum = _getVariableNum();
        quantileVariableNum = _getQuqntileVariableNum();
        if(variableNum>0 || quantileVariableNum>0){
            _setDefVariableBtnValid();
            _setQuantileVariableBtnValid();
        }else{
            _setDefVariableBtnInvalid();
            _setQuantileVariableBtnInalid();
        }
    }
}

function _getQuqntileVariableNum(){
    var num = 0;
    if(null!=var_quantileStore){
          var allItems = var_quantileStore._arrayOfAllItems;
        for(var i=0;i<allItems.length;i++){
            if(null!=allItems[i]){
                num++;
            }
        }
    }
    return num;

}

function _getVariableNum(){
    var num = 0;
    if(CurrentOperatorDTO!=null && CurrentOperatorDTO.propertyList!=null){
         var plist = CurrentOperatorDTO.propertyList;
        for(var i=0;i<plist.length;i++){
            if(plist[i].name=="fieldList"){
               if(null!=plist[i].derivedFieldsModel && null!=plist[i].derivedFieldsModel.derivedFieldsList){
                   num = plist[i].derivedFieldsModel.derivedFieldsList.length;
                   break;
               }
            }
        }
    }
    return num;
}

function _getQuqntileVariableNumFromPropList(){
    var num = 0;
    if(CurrentOperatorDTO!=null && CurrentOperatorDTO.propertyList!=null){
        var plist = CurrentOperatorDTO.propertyList;
        for(var i=0;i<plist.length;i++){
            if(plist[i].name=="quantileFieldList"){
                if(null!=plist[i].quantileFieldsModel && null!=plist[i].quantileFieldsModel.quantileItems){
                    num = plist[i].quantileFieldsModel.quantileItems.length;
                    break;
                }
            }
        }
    }
    return num;
}


var var_quantileGrid;
var var_quantileStore;
function create_var_quantileGrid() {

	var model = VquantileCurrentModel;
	var var_quantileData = dojo.clone(model.quantileItems);

	var store = {
		identifier : 'columnName',
		label : 'columnName',
		items : var_quantileData
	};
	var_quantileStore = new dojo.data.ItemFileWriteStore({
		data : store
	});

	
	// create a new var_quantile grid:
	if(var_quantileGrid==null){
		
		// set the layout structure:
		var layout = [ {
			field : 'columnName',
			name : alpine.nls.var_quan_result_column,
			width : '200px'

		}, {
			field : 'quantileTypeLabel',
			name : alpine.nls.var_quan_data_type,
			width : '120px'
		}, {
			field : 'isCreateNewColumn',
			name : alpine.nls.var_quan_create_new,
			width : '120px'
		}, {
			field : 'numberOfBin',
			name : alpine.nls.var_quantile_bins,
			width : 'auto'
		} ];

		
		var panel = dojo.byId("var_quantile_gridpane");
		if (panel.firstChild) {
			panel.removeChild(panel.firstChild);
		}
		
		var gridDomNode =document.createElement('div');
		panel.appendChild(gridDomNode);
		
		var_quantileGrid = new dojox.grid.DataGrid({
			query : {
				columnName : '*'
			},
			store : var_quantileStore,
			clientSort : true,
			rowSelector : '10px',
			selectionMode: "single",
			structure : layout
		}, gridDomNode);
 

	  dojo.connect(var_quantileGrid, "onRowClick", click_var_quantile);
 
	  var_quantileGrid.startup();
	}else{
		var_quantileGrid.setStore(var_quantileStore);
		var_quantileGrid.render();
	}
}

function click_var_quantile(event) {
	
	var items = [];	
	   if(event && event.rowIndex==undefined){
			var currentRowIndex = var_quantileGrid.focus.rowIndex;		
			items[0] = var_quantileGrid.getItem(currentRowIndex);
			var_quantileGrid.selection.select(currentRowIndex);
		}else{
			items = var_quantileGrid.selection.getSelected();
		}
	
	var val = var_quantileGrid.getItem(var_quantileGrid.focus.rowIndex);
	
	if(!items||!items[0]) {//no selected or cancel select
		//dijit.byId(Column_id).set("value", "");
		clear_var_quantile_from();
		return;
	}else{
		if(dojo.indexOf(items,val)<0){
			//ctrl to cacel the select
			val = items[0];
		}
		dijit.byId(Column_id).set("value", val.columnName);
	}
	
	
}

function clear_var_quantile_from() {
	dijit.byId(Column_id).reset();
	dojo.byId("var_quantile_bins").value = "";
	disable_var_quantile_buttons();
	clear_bin_edit_table();
}

function create_var_quantile() {
	if (!var_quantileForm.validate() ||
			!var_quantileBinForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}

	var val = get_var_quantile_data();
	if (valide_quatile_bin_values(val) == false) {
		popupComponent.alert(alpine.nls.QuantileBinInvalid);
		return;
	}
				
	var doCreate = function(items, request) {
		if (items.length == 0) {
			var_quantileStore.newItem(val);
			var_quantileStore.save();
			var_quantileGrid.startup();
			var_quantileGrid.render();
			clear_var_quantile_from();
			
		} else {
			popupComponent.alert(alpine.nls.var_dev_column_exist);
		}
	};

	var request = var_quantileStore.fetch({
		query : {
			columnName : val.columnName
		},
		onComplete : doCreate
	});

}

function update_var_quantile() {
	if (!var_quantileForm.validate() ||
			!var_quantileBinForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}

	var val = get_var_quantile_data();
	if (valide_quatile_bin_values(val) == false) {
		popupComponent.alert(alpine.nls.QuantileBinInvalid);
		return;
	}
	
	var list = var_quantileStore._arrayOfTopLevelItems;
	if (list && list.length > 0) {
		for (var i = 0; i < list.length; i++) {
			if (list[i].columnName[0] == val.columnName) {
				var_quantileStore.deleteItem(list[i]);
			}
		}
	}
	var_quantileStore.save();
	var_quantileStore.newItem(val);
	var_quantileStore.save();

	var_quantileGrid.startup();
	var_quantileGrid.render();
	clear_var_quantile_from();
	
}

function delete_var_quantile() {

	var d = get_var_quantile_data();
	var list = var_quantileStore._arrayOfTopLevelItems;
	if (list && list.length > 0) {
		for (var i = 0; i < list.length; i++) {
			if (list[i].columnName[0] == d.columnName) {
				var_quantileStore.deleteItem(list[i]);
			}
		}
	}

	var_quantileStore.save();
	clear_var_quantile_from();
	
}

// String columnName = null; 
// int quantileTypeLabel = null;
// int numberOfBin = null;
function get_var_quantile_data() {
	var val = {};
	val.columnName = dojo.byId(Column_id).value;
	val.numberOfBin = dojo.byId("var_quantile_bins").value;
	var btn = dojo.byId("var_quan_data_type_customize");
	if (btn.checked == true) {
		val.quantileTypeLabel = btn.value;
	} else {
		val.quantileTypeLabel = dojo.byId("var_quan_data_type_aa").value;
	}

	btn = dojo.byId("var_quan_new_column_one");
	if (btn.checked == true) {
		val.isCreateNewColumn = btn.value;
	} else {
		val.isCreateNewColumn = dojo.byId("var_quan_new_column_two").value;
	}

	return val;
}

function get_quantile_type(columnName) {
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

	if (/^CHAR/.test(type)) {
		type = "CHAR";
	}

	switch (type) {
	case "DATE":
	case "TIME":
	case "TIMESTAMP":
		return type;
		break;

	case "VARCHAR":
	case "BOOLEAN":
	case "TEXT":
	case "CHAR":
		return "Category";
		break;

	default:
		return "Numeric";
	}
	return null;
}

function get_default_quantile_bin_list(columnName, type) {
	var model = VquantileCurrentModel;
	var list;
	
	switch (type) {
	case "Numeric":
		list = model.numericBins;
		break;
	case "Category":
		list = model.categoryBins;
		break;
	case "DATE":
	case "TIME":
	case "TIMESTAMP":
		list = model.datetimeBins;
		break;
	default:			
		return null;
	}

	if (!list || list.length == 0) {
		return null;
	}
	
	for (var i = 0; i < list.length; i++) {
		if (columnName == list[i].columnName) {
			return list[i].binList;
		}
	}
	return null;
}

function set_quantile_bin_list(columnName, type, binList) {
	var model = VquantileCurrentModel;
	var list;
	
	switch (type) {
	case "Numeric":
		if (!model.numericBins) {
			model.numericBins = new Array();
		}
		list = model.numericBins;
		break;
	case "Category":
		if (!model.categoryBins) {
			model.categoryBins = new Array();
		}
		list = model.categoryBins;
		break;
	case "DATE":
	case "TIME":
	case "TIMESTAMP":
		if (!model.datetimeBins) {
			model.datetimeBins = new Array();
		}
		list = model.datetimeBins;
		break;
	default:	
		// unknown type		
		return;
	}

	for (var i = 0; i < list.length; i++) {
		if (columnName == list[i].columnName) {
			list[i].binList = binList;
			return;
		}
	}
	var binItem = {};
	
	binItem.columnName = columnName;
	binItem.binList = binList;
	list[list.length] = binItem;
}

var CurrentType = null;
var CurrentBin = null;
function force_update_bin_editor() {
	CurrentType = null;
	check_bin_editor();
}
function check_bin_editor() {

	// check column name exist.
	// check bin number > 0
	// get bin type and default values
	// then create edit table.
	var columnName = dojo.byId(Column_id).value;
	if (columnName == null || columnName == "") {
		return;
	}
	var type = get_quantile_type(columnName);
	if (type == "Numeric") {				
		// enable average ascend	
		enable_average_ascend();			
	}
	else {				
		disable_average_ascend();			
	}

	if (dojo.byId("var_quan_data_type_aa").checked == true) {
		clear_bin_edit_table();
		return;
	}
	
	var len = dojo.byId("var_quantile_bins").value;
	var def = get_default_quantile_bin_list(columnName, type);
	create_bin_edit_table(type, len, def);				
	
}

function valide_quatile_bin_values(item) {

	var type = get_quantile_type(item.columnName);	
	var len = item.numberOfBin;
	if (len == 0) {				
		return false;
	}			
	
	if (type == "Numeric") {				
		if (dojo.byId("var_quan_data_type_aa").checked == true)	{
			var binList = new Array();
			for (var i = 0; i < len; i++) {
				var bin = {};
				bin.binIndex = i + 1;
				bin.binType = 0;
				binList[i] = bin;
			}
			set_quantile_bin_list(item.columnName, type, binList);
			return true;
		}	
	}
			
	var binListData = get_quantile_bin_list_from_table(type, len);
	if (valide_quatile_bin_values_from_table(type, binListData) == false) {
		return false;
	}

	set_quantile_bin_list(item.columnName, type, binListData);
	return true;
}

function get_quantile_bin_list_from_table(type, len) {
	var list = new Array();
	for (var i = 0; i < len; i++) {
		var starTimeId = ID_TAG_START_T + i;
		var endTimeId = ID_TAG_END_T + i;

		var item = {};
		item.binIndex = i + 1;
		item.binType = 0;
		
		switch (type) {
		case "Numeric":
			if (dojo.byId(ID_TAG_CHECK + i).checked == true) {
				item.startFrom = dojo.byId(ID_TAG_NUM_START + i).value;
				item.endTo = dojo.byId(ID_TAG_NUM_END + i).value;
			}
			else {
				item.binType = 1;
				item.values = dojo.byId(ID_TAG_NUM_VALUES + i).value.split(",");
			}					
			break;
			
		case "Category":
			var values = dijit.byId(ID_TAG_CATEGORY_VALUES + i).get("value");
			if (values.length == 1 && values[0] == "Remaining Values") {
				item.binType = 2;
			}
			else {
				item.binType = 1;
				item.values = values;
			}		
			break;
			
		case "TIMESTAMP":
			item.startDate = dojo.byId(ID_TAG_TIMESTAMP_START + i).value;
			item.endDate = dojo.byId(ID_TAG_TIMESTAMP_END + i).value;
			item.startTime = dojo.byId(starTimeId).value;					
			item.endTime = dojo.byId(endTimeId).value;
			break;
			
		case "DATE":
			item.startDate = dojo.byId(ID_TAG_DATE_START + i).value;
			item.endDate = dojo.byId(ID_TAG_DATE_END + i).value;
			break;
			
		case "TIME":					
			item.startTime = dojo.byId(starTimeId).value;					
			item.endTime = dojo.byId(endTimeId).value;									
			break;
		}	

		list[i] = item;	
	}
	return list;
}

function valide_quatile_bin_values_from_table(type, list) {
	if (type == "TIMESTAMP" || list.length == 1) {
		var item = list[0];
		var start = new Date(item.startDate + " " + item.startTime);
		var end = new Date(item.endDate + " " + item.endTime);
		if (start >= end) {
			return false;
		}
	}
	if (type == "Category" || list.length < 2) {
		return true;
	}
	var intervalList = new Array();
	var delta = 0.00000001;
	var idx = 0;
	for (var i = 0; i < list.length; i++) {
		var item = list[i];
		switch (type) {
		case "Numeric":
			// range or values
			if (item.binType == 0) {
				if (Number(item.startFrom) >= Number(item.endTo)) {
					return false;
				}
				intervalList[idx ++] = { start : Number(item.startFrom), end : Number(item.endTo)};
			}
			else {
				var len = item.values.length;
				if (len == 0) {
					return false;
				}
				for (var j = 0; j < len; j++) {
					var val = item.values[j];
					intervalList[idx ++] = { start : Number(val), end : delta + Number(val)};
				}
			}
			break;

		case "TIMESTAMP":
			var start = new Date(item.startDate + " " + item.startTime);
			var end = new Date(item.endDate + " " + item.endTime);
			intervalList[idx ++] = { start : start, end : end};
			break;
			
		case "DATE":
			var start = new Date(item.startDate);
			var end = new Date(item.endDate);
			intervalList[idx ++] = { start : start, end : end};
			break;
			
		case "TIME":
			var start = new Date("1970/1/1 " + item.startTime);
			var end = new Date("1970/1/1 " + item.endTime);
			intervalList[idx ++] = { start : start, end : end};					
			break;
		}		
	}

	// sort
	intervalList.sort(function(a, b) {
		if (a.start < b.start) { 
			return -1;
		}
		else if (a.start > b.start) {
		 	return 1; 
		} 
		else { 
		 	return 0; 
		}
	});

	for (var i = 0; i < intervalList.length - 1; i++) { 
		if (intervalList[i].end > intervalList[i+1].start) { 
		 	return false; 
		}
		if(type != "Numeric" && intervalList[i].start.getTime() == intervalList[i+1].start.getTime() && intervalList[i].end.getTime() == intervalList[i + 1].end.getTime()){
		 	return false; 
		}
	}
	// compare
	return true;
}

function enable_average_ascend() {
	dijit.byId("var_quan_data_type_aa").set("disabled", false);
}

function disable_average_ascend() {
	dijit.byId("var_quan_data_type_customize").set("checked", true);
	dijit.byId("var_quan_data_type_aa").set("disabled", true);
}

var bin_edit_table_in_use = false;
function clear_bin_edit_table() {
	var id = "var_quantile_bins_table";
	var tbl = dojo.byId(id);
	while (tbl.firstChild) {
		tbl.removeChild(tbl.firstChild);
	}
	return tbl;
}

function create_bin_edit_table(type, nrows, valList) {

	var tbl = clear_bin_edit_table();
	tbl = dojo.create("tbody", {style:"font-size:10pt;"}, tbl);
	
	switch (type) {
	case "Numeric":
		create_Numeric_bin_edit_table(tbl, nrows, valList);
		break;
	case "Category":
		create_Category_bin_edit_table(tbl, nrows, valList);
		break;
	case "TIMESTAMP":
	case "DATE":
	case "TIME":
		create_Datetime_bin_edit_table(tbl, nrows, valList, type);
		break;
	}		
}

function create_Numeric_bin_edit_table(tbl, nrows, valList) {

	var row = dojo.create("tr", {}, tbl);

	// columns
	// 1. index, 2, checkbox value range
	// 3. from, 4. to, 5 values		
	var th = dojo.create("th", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_index
	}, th);

	th = dojo.create("th", {style: {width: "80px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_value_range
	}, th);

	th = dojo.create("th", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_value_from
	}, th);

	th = dojo.create("th", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_value_to
	}, th);

	th = dojo.create("th", {style: {width: "100px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_values
	}, th);

	for (var i = 0; i < nrows; i++) {
		var def = null;
		if (valList != null && i < valList.length) {
			def = valList[i];
		}
		
		create_Numeric_bin_edit_table_addrow(tbl, i, def);
	}
}

function create_Numeric_bin_edit_table_addrow(tbl, i, def) {
	row = dojo.create("tr", {}, tbl);
	var td = dojo.create("td", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : i + 1
	}, td);

	td = dojo.create("td", {style: {width: "40px"}}, row);
	td = dojo.create("div", {}, td);

	var id;
	var checked = false;
	var valList = "";
	var fromDef = "";
	var endDef = "";
	if (def) {
		if (def.values && def.values.length > 0) {
			valList = get_csv_value(def.values);
		}
		else { 
			checked = true;
			fromDef = def.startFrom;
			endDef = def.endTo;
		}
	}

	id = ID_TAG_CHECK + i;
	check_dijit_id(id);
	var checkBox = new dijit.form.CheckBox({
		id : id,
		name : "",
		value : "",
		checked : checked
	}, td);

	td = dojo.create("td", {style: {width: "40px"}}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_NUM_START + i;
//	check_dijit_id(id);
	var fromValue = dijit.byId(id);
	if(!fromValue){
		fromValue = new dijit.form.ValidationTextBox({
			id : id,
			style : { width : "40px"},
			trim : true,
			value : fromDef,
			required: true,
			isValid: function(){
				var val = alpine.flow.WorkFlowVariableReplacer.replaceVariable(this.getValue());
				var check = this.validator(val);
				check &= /^[\d]*$/.test(val);
				if(!check){
					this.invalidMessage = this.messages.invalidMessage;
					return false;
				}
				var toVal = alpine.flow.WorkFlowVariableReplacer.replaceVariable(toValue.getValue());
				if(toValue != undefined && toVal != "" && val > toVal){
					this.invalidMessage = this.messages.rangeMessage;
					return false;
				}
				return true;
			}
		});
	}else{
		fromValue.set("value",fromDef);
	}
	td.appendChild(fromValue.domNode);
	
	td = dojo.create("td", {style: {width: "40px"}}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_NUM_END + i;
//	check_dijit_id(id);
	var toValue = dijit.byId(id);
	if(!toValue){
		toValue = new dijit.form.ValidationTextBox({
			id : id,
			style : { width : "40px"},
			trim : true,
			value : endDef,
			required: true,
			isValid: function(){
				var val = alpine.flow.WorkFlowVariableReplacer.replaceVariable(this.getValue());
				var check = this.validator(val);
				check &= /^[\d]*$/.test(val);
				if(!check){
					this.invalidMessage = this.messages.invalidMessage;
					return false;
				}
				var fromVal = alpine.flow.WorkFlowVariableReplacer.replaceVariable(fromValue.getValue());
				if(fromVal == "" || val <= fromVal){
					this.invalidMessage = this.messages.rangeMessage;
					return false;
				}
				return true;
			}
		});
	}else{
		toValue.set("value",endDef);
	}
	td.appendChild(toValue.domNode);

	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_NUM_VALUES + i;
//	check_dijit_id(id);
	var valueList = dijit.byId(id);
	if(!valueList){
		valueList = new dijit.form.NumberTextBox({
			id : id,
			required : true,
			trim : true,
			regExp : "[\.,\-0123456789]+", // somehow 0-9 is not working. dojo?!
			value : valList
		});
	}else{
		valueList.set("value",valList);
	}
	td.appendChild(valueList.domNode);

//	dojo.connect(fromValue, "onBlur", {}, function() { 
//		toValue.constraints.min = fromValue.get("value");
//	});
//	dojo.connect(toValue, "onBlur", {}, function() { 
//		fromValue.constraints.max = toValue.get("value");
//	});
	
	dojo.connect(checkBox, "onClick", {}, function(value) { 
		var ch = checkBox.checked;
		fromValue.set("disabled", !ch);
		toValue.set("disabled", !ch);
		valueList.set("disabled", ch);
	});
	
	fromValue.set("disabled", !checked);
	toValue.set("disabled", !checked);
	valueList.set("disabled", checked);
}

function get_csv_value(list) {
//	var value = list[0];
//	for ( var i = 1; i < list.length; i++) {
//		value = value + "," + list[i];				
//	}
//	return value;
	return list.join(",");
}


function create_Category_bin_edit_table(tbl, nrows, valList) {
	var row = dojo.create("tr", {}, tbl);

	// columns
	// 1. index, 
	// 2. values, 			
	var th = dojo.create("th", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_index
	}, th);
	th = dojo.create("th", {style: {width: "340px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_values
	}, th);
    row = dojo.create("tr", {}, tbl);
    td = dojo.create("td", {style: {width: "40px"}}, row);
    td = dojo.create("td", {style: {width: "340px", "font-style": "italic" }, innerHTML: " (" + alpine.nls.quantile_listinput_hint +")"}, row);
	for (var i = 0; i < nrows; i++) {
		var def = null;
		if (valList != null && i < valList.length) {
			def = valList[i];
		}
		
		create_Category_bin_edit_table_addrow(tbl, i, def);
	}
}

function create_Category_bin_edit_table_addrow(tbl, i, def) {
	row = dojo.create("tr", {}, tbl);
	var td = dojo.create("td", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : i + 1
	}, td);

	var value;
	if (def) {
		if (def.values && def.values.length > 0) {
			value = get_csv_value(def.values);
		}
		else { 
			value = "Remaining Values";
		}
	}
	
	td = dojo.create("td", {style: {width: "430px"}}, row);
	var id = ID_TAG_CATEGORY_VALUES + i;
//	check_dijit_id(id);
	var fromValue = dijit.byId(id);
	if(!fromValue){
		fromValue = new dojox.form.ListInput({
			id : id,
			readOnlyItem : true,
			style: "width: 430px; overflow-x:hidden;",
			title: alpine.nls.quantile_listinput_hint
		});
		fromValue.startup();
		dojo.connect(fromValue, "onDblClick", fromValue, function(){
			showQuantileDistinctValues(this);
		});
	}else{
		fromValue.reset();
	}
	td.appendChild(fromValue.domNode);
	if(def != null && def.values != null){
		for(var i = 0;i < def.values.length;i++){
			fromValue.add(def.values[i]);
		}
	}
}

function create_Datetime_bin_edit_table(tbl, nrows, valList, type) {
	var row = dojo.create("tr", {}, tbl);

	// columns
	// 1. index, 
	// 2. values, 			
	var th = dojo.create("th", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : alpine.nls.bin_index
	}, th);

	if (type == "TIMESTAMP") {
		th = dojo.create("th", {}, row);
		var th2 = dojo.create("th", {}, row);
		dojo.create("label", {
			style : { colspan : "2" },
			innerHTML : alpine.nls.bin_value_from
		}, th);

		th = dojo.create("th", {}, row);
		th2 = dojo.create("th", {}, row);
		dojo.create("label", {
			style : { colspan : "2" },
			innerHTML : alpine.nls.bin_value_to
		}, th);
	}
	else {			
		th = dojo.create("th", {}, row);
		dojo.create("label", {
			innerHTML : alpine.nls.bin_value_from
		}, th);

		th = dojo.create("th", {}, row);
		dojo.create("label", {
			innerHTML : alpine.nls.bin_value_to
		}, th);
	}
	
	for (var i = 0; i < nrows; i++) {
		var def = null;
		if (valList != null && i < valList.length) {
			def = valList[i];
		}
		if (type == "DATE") {
			add_date_row_Datetime_bin_edit_table(tbl, i, def, type);
		}
		else if (type == "TIME") {		
			add_time_row_Datetime_bin_edit_table(tbl, i, def, type);
		}
		else {		
			add_timestamp_row_Datetime_bin_edit_table(tbl, i, def, type);
		}
	}
}

function add_date_row_Datetime_bin_edit_table(tbl, i, def, type) {
	row = dojo.create("tr", {}, tbl);
	var td = dojo.create("td", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : i + 1
	}, td);

	var id;
	var today = new Date();//make the data text to null, force user change date values.
	var value = null;
	if (def) {
		value = new Date(def.startDate);
	}
	else {
		value = today;
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_DATE_START + i;
//	check_dijit_id(id);
	var fromValue = dijit.byId(id);
	if(!fromValue){
		fromValue = new dijit.form.DateTextBox({
			id : id,
			required : true,
			style : { width: "120px" },
			constraints: {
				datePattern:"yyyy/MM/dd", 
				strict:true
			},
			value : value
		});
	}else{
		fromValue.set("value",value);
	}
	td.appendChild(fromValue.domNode);

	if (def) {
		value = new Date(def.endDate);
	}
	else {
		value = today;
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_DATE_END + i;
//	check_dijit_id(id);
	var toValue = dijit.byId(id);
	if(!toValue){
		toValue = new dijit.form.DateTextBox({
			id : id,
			required : true,
			style : { width: "120px" },
			constraints: {
				datePattern:"yyyy/MM/dd", 
				strict:true
			},
			value : value
		});
	}else{
		toValue.set("value",value);
	}
	td.appendChild(toValue.domNode);
	
	dojo.connect(fromValue, "onChange", {}, function(value) { 
		toValue.constraints.min = value;
	});
	dojo.connect(toValue, "onChange", {}, function(value) { 
		fromValue.constraints.max = value;
	});

}

function add_time_row_Datetime_bin_edit_table(tbl, i, def, type) {
	row = dojo.create("tr", {}, tbl);
	var td = dojo.create("td", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : i + 1
	}, td);

	var id;
	var value = null;
	if (def) {
		value = new Date("1970/1/1 " + def.startTime);
	}
	else {
		value = new Date();//make the data text to null, force user change date values.
	}
	td = dojo.create("td", {}, row);
//	var td = dojo.create("div", null, td);
	id = ID_TAG_START_T + i;
	//check_dijit_id(id);
	var fromValue = dijit.byId(id);
	if(!fromValue){
		fromValue = new dijit.form.TimeTextBox({
			id : id,
	        value: value,
	        required : true,
	        style : { width: "120px" },
	        constraints: {
	            timePattern: 'HH:mm:ss',
	            clickableIncrement: 'T00:15:00',
	            visibleIncrement: 'T00:15:00',
	            visibleRange: 'T01:00:00'
	        }
		});
	}else{
		fromValue.set("value",value);
	}
	td.appendChild(fromValue.domNode);

	if (def) {
		value = new Date("1970/1/1 " + def.endTime);
	}
	else {
		value = new Date();//make the data text to null, force user change date values.
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_END_T + i;
//	check_dijit_id(id);
	var toValue = dijit.byId(id);
	if(!toValue){
		toValue = new dijit.form.TimeTextBox({
			id : id,
	        value: value,
	        required : true,
	        style : { width: "120px" },
	        constraints: {
	            timePattern: 'HH:mm:ss',
	            clickableIncrement: 'T00:15:00',
	            visibleIncrement: 'T00:15:00',
	            visibleRange: 'T01:00:00'
	        }
		});
	}else{
		toValue.set("value",value);
	}
	td.appendChild(toValue.domNode);

	dojo.connect(fromValue, "onChange", {}, function(value) { 
		toValue.constraints.min = value;
	});
	dojo.connect(toValue, "onChange", {}, function(value) { 
		fromValue.constraints.max = value;
	});

}

function add_timestamp_row_Datetime_bin_edit_table(tbl, i, def, type) {
	row = dojo.create("tr", {}, tbl);
	var td = dojo.create("td", {style: {width: "40px"}}, row);
	dojo.create("label", {
		innerHTML : i + 1
	}, td);

	var id;
	var today = new Date();//make the data text to null, force user change date values.
	var value = null;
	if (def) {
		value = new Date(def.startDate);
	}
	else {
		value = today;
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_TIMESTAMP_START + i;
//	check_dijit_id(id);
	var fromValue = dijit.byId(id);
	if(!fromValue){
		fromValue = new dijit.form.DateTextBox({
			id : id,
			required : true,
			style : { width: "90px" },
			constraints: {
				datePattern:"yyyy/MM/dd", 
				strict:true
			},
			value : value
		});
	}else{
		fromValue.set("value",value);
	}
	td.appendChild(fromValue.domNode);
	
	if (def) {
		value = new Date("1970/1/1 " + def.startTime);
	}
	else {
		value = new Date();//make the data text to null, force user change date values.
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_START_T + i;
//	check_dijit_id(id);
	var fromTimeValue = dijit.byId(id);
	if(!fromTimeValue){
		fromTimeValue = new dijit.form.TimeTextBox({
			id : id,
	        value: value,
	        required : true,
	        style : { width: "90px" },
	        constraints: {
	            timePattern: 'HH:mm:ss',
	            clickableIncrement: 'T00:15:00',
	            visibleIncrement: 'T00:15:00',
	            visibleRange: 'T01:00:00'
	        }
		});
	}else{
		fromTimeValue.set("value",value);
	}
	td.appendChild(fromTimeValue.domNode);
	
	if (def) {
		value = new Date(def.endDate);
	}
	else {
		value = today;
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_TIMESTAMP_END + i;
//	check_dijit_id(id);
	var toValue = dijit.byId(id);
	if(!toValue){
		toValue = new dijit.form.DateTextBox({
			id : id,
			required : true,
			style : { width: "90px" },
			constraints: {
				datePattern:"yyyy/MM/dd", 
				strict:true
			},
			value : value
		});
	}else{
		toValue.set("value",value);
	}
	td.appendChild(toValue.domNode);
	
	if (def) {
		value = new Date("1970/1/1 " + def.endTime);
	}
	else {
		value =new Date();//make the data text to null, force user change date values.
	}
	td = dojo.create("td", {}, row);
	td = dojo.create("div", {}, td);
	id = ID_TAG_END_T + i;
//	check_dijit_id(id);
	var toTimeValue = dijit.byId(id); 
	if(!toTimeValue){
		toTimeValue = new dijit.form.TimeTextBox({
			id : id,
	        value: value,
	        required : true,
	        style : { width: "90px" },
	        constraints: {
	            timePattern: 'HH:mm:ss',
	            clickableIncrement: 'T00:15:00',
	            visibleIncrement: 'T00:15:00',
	            visibleRange: 'T01:00:00'
	        }
		});
	}else{
		toTimeValue.set("value",value);
	}
	td.appendChild(toTimeValue.domNode);
	
	dojo.connect(fromValue, "onChange", {}, function(value) { 
		toValue.constraints.min = value;
	});
	dojo.connect(toValue, "onChange", {}, function(value) { 
		fromValue.constraints.max = value;
	});

}

function set_button_mode(store, value) {
	var list = store._arrayOfTopLevelItems;
	if (list && list.length > 0) {
		for (var i = 0; i < list.length; i++) {
			if (list[i].columnName[0] == value) {
				dojo.byId("var_quantile_bins").value = list[i].numberOfBin[0];
				if (list[i].quantileTypeLabel[0] == "Customize") {
					dijit.byId("var_quan_data_type_customize").set("checked", true);
				} else {
					dijit.byId("var_quan_data_type_aa").set("checked", true);
				}

				if (list[i].isCreateNewColumn[0] == true) {
					dijit.byId("var_quan_new_column_one").set("checked", true);
				} else {
					dijit.byId("var_quan_new_column_two").set("checked", true);
				}
				enable_var_quantile_update();
				check_bin_editor();
				return;
			}
		}
	}

	dojo.byId("var_quantile_bins").value = "1";
	enable_var_quantile_create();
	check_bin_editor();
}

function enable_var_quantile_create() {
	dijit.byId("var_quantile_create_button").set("disabled", false);
	dijit.byId("var_quantile_update_button").set("disabled", true);
	dijit.byId("var_quantile_delete_button").set("disabled", true);
}

function enable_var_quantile_update() {
	dijit.byId("var_quantile_create_button").set("disabled", true);
	dijit.byId("var_quantile_update_button").set("disabled", false);
	dijit.byId("var_quantile_delete_button").set("disabled", false);
}

function disable_var_quantile_buttons() {
	dijit.byId("var_quantile_create_button").set("disabled", true);
	dijit.byId("var_quantile_update_button").set("disabled", true);
	dijit.byId("var_quantile_delete_button").set("disabled", true);

}

function check_dijit_id(id) {
	var obj = dijit.byId(id);
	if (obj) {
		obj.destroyRecursive(true);
		dijit.registry.remove(id);
	}
}

var variableQuantileEditingWidget = null;
function showQuantileDistinctValues(editingWidget){
	dijit.byId("alpine_prop_variable_quantile_distinctValues_dialog").show();
	variableQuantileEditingWidget = editingWidget;
	var usedValues = new Array();
	var binNumber = dijit.byId("var_quantile_bins").get("value");
	for(var i = 0;i < binNumber;i++){
		var columnValues = dijit.byId(ID_TAG_CATEGORY_VALUES + i).get("value");
		usedValues = usedValues.concat(columnValues);
	}
	getDistinctValues(function(distinctValues){
		var availableValues = new Array();
		for(var i = 0;i < distinctValues.length;i++){
			if(dojo.indexOf(usedValues, distinctValues[i]) == -1){
				availableValues.push({
					columnName: distinctValues[i]
				});
			}
		}
		var dataStore = new dojo.data.ItemFileReadStore({
			data: {
				identifier: "columnName",
				items: availableValues
			}
		});
		var availableValuesGrid = dijit.byId("alpine_prop_variable_quantile_distinctValues_grid");
		if(!availableValuesGrid){
			availableValuesGrid = new dojox.grid.DataGrid({
				store: dataStore,
				query: {"columnName": "*"},
				style: "height: 100%",
				structure: [
					{type: "dojox.grid._CheckBoxSelector"},
					[
			            {name: alpine.nls.var_quan_result_column, field: "columnName",width: "100%"}
					]
				]
			}, "alpine_prop_variable_quantile_distinctValues_grid");
			availableValuesGrid.startup();
		}else{
			availableValuesGrid.selection.clear();
			availableValuesGrid.setStore(dataStore);
		}
	});
}

var distinctValueCache = [];
function getDistinctValues(callback){
    var columnName = dijit.byId(Column_id).get("value");
	var connName = CurrentOperatorDTO.inputTableInfos[0].connectionName;
	var schemaName = CurrentOperatorDTO.inputTableInfos[0].schema;
	var tableName = CurrentOperatorDTO.inputTableInfos[0].table;
	if(distinctValueCache[columnName]){
		callback.call(null, distinctValueCache[columnName]);
		return;
	}
    //MINERWEB-1206 replace workflow variables to get distinct values and close dialog if error
    var realSchema = alpine.flow.WorkFlowVariableReplacer.replaceVariable(schemaName);
    var realTable = alpine.flow.WorkFlowVariableReplacer.replaceVariable(tableName);

	ds.get(alpine.baseURL + "/main/property.do?method=distinctColumnValues" +
			"&connName=" + connName + "&schema=" + realSchema + "&table=" + realTable + "&columnName=" + columnName,
			function(data){
		distinctValueCache[columnName] = data;
		callback.call(null, distinctValueCache[columnName]);
	}, function(data) {
            var distinctValueDialog = dijit.byId("alpine_prop_variable_quantile_distinctValues_dialog")
            if (distinctValueDialog) {distinctValueDialog.hide();}
        }, false, "alpine_prop_variable_quantile_distinctValues_dialog");
}

function fillbackQuantileColumnValues(){
	var selectedValues = dijit.byId("alpine_prop_variable_quantile_distinctValues_grid").selection.getSelected();
	dijit.byId("alpine_prop_variable_quantile_distinctValues_grid").scrollToRow(selectedValues.length - 1);
	for(var i = 0;i < selectedValues.length;i++){
        var colName;
        if (selectedValues[i])  colName = selectedValues[i].columnName;
		variableQuantileEditingWidget.add(colName);
	}
	variableQuantileEditingWidget = null;
	dijit.byId("alpine_prop_variable_quantile_distinctValues_dialog").hide();
	
}

function cancelQuantileColumnValue(){
	variableQuantileEditingWidget = null;
	dijit.byId('alpine_prop_variable_quantile_distinctValues_dialog').hide();
}