/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * 
 * flowcontrol.js
 * 
 * Author sam_zang
 * 
 * Version 3.0
 * 
 * Date Oct 29, 2011
 */

function showCohortsDialog(prop) {
	CurrentCohortProperty = prop;
      
	dojo.byId("new_max_cohort").value = "";
    init_cohorts_grid(prop);
    dijit.byId("cohortDialog").show();
    
	dojo.addOnLoad(function() {
		var bt = dijit.byId("new_max_cohort");

		dojo.connect(bt, 'onKeyPress', function(evt) {
			key = evt.keyCode;
			if (key == dojo.keys.ENTER) {
				add_cohort();
			}
		});

	});
}

var CohortNumberList = null;
var CurrentCohortProperty = null;
var cohortsGrid = null;

function init_cohorts_grid(prop) {
	CohortNumberList = cohort_value_to_list(prop.value);
	var cohortsStore = make_cohort_store(CohortNumberList);
	// create a new cohorts grid:
	// set the layout structure:
	var layout = [ {
		field : 'index',
		name : alpine.nls.cohorts_index,
		width : '100px'

	}, {
		field : 'min',
		name : alpine.nls.cohorts_min,
		width : '120px'

	}, {
		field : 'max',
		name : alpine.nls.cohorts_max,
		width : 'auto'

	} ];
	
	cohortsGrid = new dojox.grid.DataGrid({
		query : {
			index : '*'
		},
		store : cohortsStore,
		clientSort : false,
		rowSelector : '10px',
		structure : layout
	}, document.createElement('div'));

	var panel = dojo.byId("cohorts_grid_pane");
	if (panel.firstChild) {
		panel.removeChild(panel.firstChild);
	}
	panel.appendChild(cohortsGrid.domNode);

	cohortsGrid.startup();
}

function make_cohort_store(numlist) {
	var list = make_cohort_item_list(numlist);

//	Test data.	
//	var list = new Array();
//	list[0] = { index: 1, min: "-Infinity", max: 0};
//	list[1] = { index: 2, min: "0", max: 10};
//	list[2] = { index: 3, min: 10, max: "Infinity"};
	
	var store = {
			identifier : 'index',
			label : alpine.nls.cohorts_index,
			items : list
		};
	var cohortsStore = new dojo.data.ItemFileWriteStore({
		data : store
	});

	return cohortsStore;
}

// value format: "1:-Infinity:0;2:0:10;3:10:Infinity"
function cohort_value_to_list(value) {
	var list = new Array();
	var itemList = value.split(";");
	var listIdx = 0;
	for (var i = 0; i < itemList.length; i++) {
		var item = itemList[i].split(":");
		if (item[2] != "Infinity") {
			list[list.length] = item[2];
		}
	}
	
	return list;
}

function list_to_cohort_value(numlist) {
	
	if (numlist.length == 0) {
		return "";
	}
	var count = 2;
	var value = "1:-Infinity:";
	for (var i = 0; i < numlist.length; i++) {
		if(numlist[i]!= undefined){
			value += numlist[i] + ';' + count + ":" + numlist[i] + ":";
			count ++;
		}
	}
	value += "Infinity";
	
	return value;	
}

function make_cohort_item_list(numlist) {
	var list = new Array();
	if(numlist.length == 1&&numlist[0]==undefined){
		numlist=new Array();
	}
	if (numlist.length == 0) {
		return "";
	}
	var count = 1;
	var min = "-Infinity";
	for (var i = 0; i < numlist.length; i++) {
		if(numlist[i]!=undefined){
			list[list.length] = {
					index: count,
					min: min,
					max: numlist[i]
			};
			count ++;
			min = numlist[i];
		}
	}
	list[list.length] = {
			index: count,
			min: min,
			max: "Infinity"
	}
	
	return list;
}

function add_cohort() {
	// update number list.
	var new_max = dojo.byId("new_max_cohort").value;
	if (!new_max || new_max == "") {
		return;
	}
	for (var i = 0; i < CohortNumberList.length; i++) {
		if (CohortNumberList[i] == new_max) {
			popupComponent.alert(alpine.nls.already_exist );
			return;
		}
	}
	CohortNumberList[CohortNumberList.length] = new_max;
	CohortNumberList.sort(function(a, b) {	
			return (a - b); 
	});

	var cohortsStore = make_cohort_store(CohortNumberList);
	cohortsGrid.setStore(cohortsStore);
	cohortsGrid.startup();
}

function remove_selected_cohorts() {
	// update number list.
	var items = cohortsGrid.selection.getSelected();
	if (items.length) {
		var new_list = new Array(); 
		for ( var i = 0; i < CohortNumberList.length; i++) {
			var found = false;
			for ( var k = 0; k < items.length; k++) {
				if (!items[k]) {
					continue;
				}
				var old_max = items[k].max[0];
				if (CohortNumberList[i] == old_max) {
					found = true;
					break;
				}
				if (old_max == "Infinity" && (i == CohortNumberList.length - 1)) {
					found = true;
				}
			}
			
			if (found == false&&CohortNumberList[i]!=undefined) { 
				new_list[new_list.length] = CohortNumberList[i];
			}
		}
		CohortNumberList = new_list;
		var cohortsStore = make_cohort_store(CohortNumberList);
		cohortsGrid.setStore(cohortsStore);
		cohortsGrid.startup();
	}
}

function update_cohorst_data() {
	CurrentCohortProperty.value = list_to_cohort_value(CohortNumberList);
    setButtonBaseClassValid(getSourceButtonId(CurrentCohortProperty));
	dijit.byId('cohortDialog').hide();
}

