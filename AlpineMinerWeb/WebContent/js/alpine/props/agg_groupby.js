/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * agg_groupby.js
 * 
 * Author sam_zang
 * Version 3.0
 * Date Dec 4, 2011
 */

var AggFullGroupByListGrid = null;
var CurrentGroupByFieldsModel = null;
var agg_groupbyGrid = null; 
var agg_groupbyStore = null;
var sourceButtonId = null;
var inputColumns = null;


function showAggregateGroupByEditDialog(prop) {
	if (prop.aggregateFieldsModel == null) {
		var model = {};
		model.groupByFieldList = new Array();
		prop.aggregateFieldsModel = model;
	}
    if (prop.fullSelection == null)
    {
        prop.fullSelection = new Array();
    }
    inputColumns = prop.fullSelection;
	CurrentGroupByFieldsModel = prop.aggregateFieldsModel;
	create_agg_groupbyGrid(prop);
	var id = "agg_groupby_list";
	var func = click_agg_groupby_full_column_list;
	AggFullGroupByListGrid = create_default_column_list(prop.store, id, func);
    sourceButtonId = getSourceButtonId(prop);

    aggregateColumnChecker.initialize(prop.type);
	dijit.byId("aggregateGroupBysConfigEditDialog").titleBar.style.display = 'none';
	dijit.byId("aggregateGroupBysConfigEditDialog").show();
}

function close_agg_groupby_dialog(){ 
	//release resource here...
    _aggValidateBtn();
	dijit.byId('aggregateGroupBysConfigEditDialog').hide();
	
}
function click_agg_groupby_full_column_list(event) {
	var item = AggFullGroupByListGrid.getItem(event.rowIndex);
	if(aggregateColumnChecker.isExists(item.name[0])){
		popupComponent.alert(alpine.nls.agg_column_validation_name_exist);
		return;
	}
	
	var agg = {
		name : item.name[0]
	};
	create_agg_groupby(agg);
}

function update_agg_groupby_data() {
	
	var updateModel = function(items, request) {
		//first run validation to make sure this works.
        if (items.length == 0)
        {
            popupComponent.alert(alpine.nls.agg_groupby_column_one);
            return;
        }

        var success = true;
        for ( var i = 0; i < items.length; i++) {
            var groupByElement = items[i].name[0];
            //need to confirm this element is in the inputColumns
            if (inputColumns.indexOf(groupByElement) == -1)
            {
                success = false;
                break;
            }
        }
        if (!success)
        {
            var text = alpine.nls.agg_groupby_column_invalid.replace("{0}",groupByElement);;
            popupComponent.alert(text);
            return;
        }


        var model = CurrentGroupByFieldsModel;
		model.groupByFieldList = new Array();
		for ( var i = 0; i < items.length; i++) {
			model.groupByFieldList[i] = items[i].name[0];
		}
		aggregateColumnChecker.storeCurrentColumns(model.groupByFieldList);
        setButtonBaseClassValid(sourceButtonId);
        close_agg_groupby_dialog();
	};

	var request = agg_groupbyStore.fetch({
		query : {
			name : "*"
		},
		onComplete : updateModel
	});

}


function create_agg_groupbyGrid(prop) {

	var model = CurrentGroupByFieldsModel;
	var dataList = model.groupByFieldList;

	var agg_groupbyData = new Array();
	for ( var i = 0; i < dataList.length; i++) {
		agg_groupbyData[i] = {
			name : dataList[i]
		};
	}			

	var store = {
		identifier : 'name',
		label : 'name',
		items : agg_groupbyData
	};
	agg_groupbyStore = new dojo.data.ItemFileWriteStore({
		data : store
	});

	
	
	if(agg_groupbyGrid ==null){
		// set the layout structure:
		var layout = [ {
			field : 'name',
			name : alpine.nls.agg_groupby_column,
			width : 'auto'

		} ];
		var panel = dojo.byId("agg_groupby_gridpane");
		if (panel.firstChild) {
			panel.removeChild(panel.firstChild);
		}
		var gridDomNode = document.createElement('div');
		panel.appendChild(gridDomNode);
		// create a new agg_groupby grid:
		agg_groupbyGrid = new dojox.grid.DataGrid({
			query : {
				name : '*'
			},
			store : agg_groupbyStore,
			clientSort : true,
			rowSelector : '10px',
			structure : layout
		}, gridDomNode);
	
	 
		agg_groupbyGrid.startup();
	}else{
		agg_groupbyGrid.setStore(agg_groupbyStore);
		agg_groupbyGrid.render();
	}
}

function create_agg_groupby(agg) {

	var doCreate = function(items, request) {
		if (items.length == 0) {
			agg_groupbyStore.newItem(agg);
			agg_groupbyStore.save();
			agg_groupbyGrid.startup();
			agg_groupbyGrid.render();
		} else {
			popupComponent.alert(alpine.nls.agg_groupby_column_exist);
		}
	};

	var request = agg_groupbyStore.fetch({
		query : {
			name : agg.name
		},
		onComplete : doCreate
	});
}

function delete_agg_groupby() {
	var items = agg_groupbyGrid.selection.getSelected();
	if (items.length) {
		dojo.forEach(items, function(selectedItem) {
			if (selectedItem != null) {
				agg_groupbyStore.deleteItem(selectedItem);
				aggregateColumnChecker.remove(selectedItem.name[0]);
			}
		});
	}
	agg_groupbyStore.save();
}

function move_up_agg_groupby() {
	alpine.props.adaboost.move_grid(agg_groupbyStore, agg_groupbyGrid, 1);
}

function move_down_agg_groupby() {
    alpine.props.adaboost.move_grid(agg_groupbyStore, agg_groupbyGrid, -1);
}