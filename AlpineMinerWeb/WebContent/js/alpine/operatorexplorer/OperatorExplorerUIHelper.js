/**
 * User: sasher
 * Date: 7/3/12
 * Time: 11:47 AM
 */

define(["alpine/operatorexplorer/FrequentOperatorsManager",
        "alpine/operatorexplorer/OperatorUtil",
        "alpine/spinner",
        "alpine/flow/OperatorManagementUIHelper",
    "alpine/layout/SelectPlusCheckBox",
    "dijit/focus" ], function(freqHandler, opUtil, spinner, operatorManagement,SelectPlusCheckBox,  focusUtil){

    var constants = {
            OPERATOR_PANE: "alpine_layout_navigation_operator_pane",
            FILTER_INPUT: "alpine_operatorexplorer_filter",
            DISPLAY_PANE: "alpine_operatorexplorer_display_pane",
            OPERATOR_GRID: "alpine_operatorexplorer_display_grid",
            OPERATOR_FREQ_GRID: "alpine_operatorexplorer_freq_grid",
            OPERATOR_FILTER_SELECT: "alpine_operatorexplorer_filterselect",
            OPERATOR_FILTER_SELECT_CBOX: "alpine_operatorexplorer_filterselect_menu_cbox",
            OPERATOR_FILTER_SELECT_MENU: "alpine_operatorexplorer_filterselect_menu",
            OPERATOR_GRID_HEADER: "alpine_operatorexplorer_display_grid_header",
            OPERATOR_FREQ_GRID_HEADER: "alpine_operatorexplorer_display_freq_grid_header"
    };

    var textFilter = "*";
    var typeFilter = -1;
    var freqs = [];
    var customOps = [];

    var canBeDragging = false;

    //listen from open/close workflow editor.
    dojo.subscribe("/operatorExplorer/switchWorkflowEditor", null, function(isOpen){
    	canBeDragging = isOpen;
    	_setAbleToDrag(constants.OPERATOR_GRID, isOpen);
    	_setAbleToDrag(constants.OPERATOR_FREQ_GRID, isOpen);
    });

    function _setAbleToDrag(gridId, ableToDrag){
    	if(dijit.byId(gridId)){
    		dijit.byId(gridId).setupDnDConfig({
    			"out": {
    				"row": ableToDrag
    			}
    		});
    	}
    }

    dojo.ready(function(){

        constants.HADOOP_ENABLED_TAG = " (" + alpine.nls.hadoop_enabled + ")";

        var filterSelect = new SelectPlusCheckBox({
            baseClass:"transparentDropdownButton",
            options: [
                { label: alpine.nls.workbench_op_category_all, value: "-1" },
                { label: alpine.nls.workbench_op_category_extraction, value: "0" },
                { label: alpine.nls.workbench_op_category_exploration, value: "1" },
                { label: alpine.nls.workbench_op_category_transformation, value: "2" },
                { label: alpine.nls.workbench_op_category_sampling, value: "3" },
                { label: alpine.nls.workbench_op_category_modeling, value: "4" },
                { label: alpine.nls.workbench_op_category_scoring, value: "5" },
                { label: alpine.nls.workbench_op_category_other, value: "6" }
            ]
        }, constants.OPERATOR_FILTER_SELECT);

        dojo.connect(dijit.byId(constants.OPERATOR_PANE), "onShow", function(){
            freqHandler.getRecentAndCustomOperators(handleFreqOperators, handleCustomizedOperators, dataAllLoaded,dataLoadFailed) ;
            dojo.byId(constants.FILTER_INPUT).focus();
            dojo.byId(constants.FILTER_INPUT).select();
        });

        dojo.connect(dijit.byId(constants.FILTER_INPUT), "onChange", function(){
            textFilter = this.get("value") + "*";
            filterOperators();
        });

        dojo.connect(dijit.byId(constants.OPERATOR_FILTER_SELECT_MENU), "onBlur", function(){
            filterOperators();
        });

        dojo.connect(dijit.byId(constants.OPERATOR_FILTER_SELECT), "onChange", function(evt){
            console.log("changed selection");
            typeFilter = 1*evt;
            filterOperators();
        });
    });


function handleCustomizedOperators(custops)
{
       if (custops) console.log("got the custom: " + custops);
    customOps = custops;
    //so if we have the custom operators, we need to add them to the operator hash in operatorUtils.

}
function handleFreqOperators(newfreqs)
{
	freqs = newfreqs;
}

function dataAllLoaded()
{
   console.log("all the data is loaded - now deal with it!");
    buildDataGrid();
    }

function dataLoadFailed()
{
    customOps = [];
    freqs = [];
}

function buildDataGrid(){
        console.log("building op grid");
    var gridArgs = buildOperatorGrid(opUtil.getAllOperatorsIncludingCustom(customOps), " ");
    var freqGridArgs = buildOperatorGrid(opUtil.getOperatorObjectsByKeys(freqs), alpine.nls.workbench_op_recent_used_label.toUpperCase());

        var grid = dijit.byId(constants.OPERATOR_GRID);
        var freqgrid = dijit.byId(constants.OPERATOR_FREQ_GRID);

    //whenever back to this tab, recreate the operators and frequently used operators
    if(freqgrid || grid){
        destroyOperatorsGrid();
    }


    if (!dojo.byId(constants.OPERATOR_FREQ_GRID_HEADER))
    {
        var gridFreqHeader = dojo.create("div", {id: constants.OPERATOR_FREQ_GRID_HEADER}, constants.DISPLAY_PANE);
        dojo.addClass(gridFreqHeader,"operatortableheader");
        gridFreqHeader.innerHTML = alpine.nls.workbench_op_recent_used_label.toUpperCase();
    }


        //only show frequently if there are some to show!
    var freqDataStore = new dojo.data.ItemFileReadStore({
        data: {
            identifier: "key",
            items: freqGridArgs.gridData
        }
    });
    freqgrid = new dojox.grid.EnhancedGrid({
            id: constants.OPERATOR_FREQ_GRID,
            store: freqDataStore,
            query: {"key": "*"},
            autoHeight: 6,
            structure: freqGridArgs.structure,
            canSort:function(){return false;},
            onRowClick: function(e){
                if(e.rowIndex == undefined){
                    return;
            }
                          },
        //below are for dnd
        selectionMode: "single",
		plugins: {
			dndop: {
				copyOnly: true,
				dndConfig: {
					"within": {
						"row": false,
						"col": false,
						"cell": false
					},
					"in": {
						"row": false,
						"cell": false
					},
					"out": {
						"row": canBeDragging,
						"col": false,
						"cell": false
					}
				}
			},
			selector: {
				col: false,
				row: true,
				cell: false
			}
		}
    }, dojo.create("div", {style: "width: 100%"}, constants.DISPLAY_PANE));
    var dataStore = new dojo.data.ItemFileReadStore({
        data: {
            identifier: "key",
            items: gridArgs.gridData
        }
    });

    if (!dojo.byId(constants.OPERATOR_GRID_HEADER))
    {
        var gridHeader = dojo.create("div", {id: constants.OPERATOR_GRID_HEADER}, constants.DISPLAY_PANE);
        dojo.addClass(gridHeader,"operatortableheader");
        gridHeader.innerHTML = " ";
    }
    grid = new dojox.grid.EnhancedGrid({
            id: constants.OPERATOR_GRID,
            store: dataStore,
            query: {"key": "*"},
            structure: gridArgs.structure,
            autoHeight: true,
            canSort:function(){return false;},
            onRowClick: function(e){
                if(e.rowIndex == undefined){
                    return;
                }
        },
        //below are for dnd
        selectionMode: "single",
		plugins: {
			dndop: {
				copyOnly: true,
				dndConfig: {
					"within": {
						"row": false,
						"col": false,
						"cell": false
					},
					"in": {
						"row": false,
						"cell": false
					},
					"out": {
						"row": canBeDragging,
						"col": false,
						"cell": false
            }
				}
			},
			selector: {
				col: false,
				row: true,
				cell: false
			}
		}
        }, dojo.create("div", {style: "width: 100%; height: 50%"}, constants.DISPLAY_PANE));

    grid.queryOptions = {ignoreCase: true};  //so filtering is case-insensitive
    freqgrid.queryOptions = {ignoreCase: true};   //so filtering is case-insensitive

    freqgrid.plugin("dndop")._source.generateText = false;
    grid.plugin("dndop")._source.generateText = false;
    
    freqgrid.onCellMouseDown = operatorManagement.gridDragAdaptor;// overwrite to avoid drag on folder will be moved.
    grid.onCellMouseDown = operatorManagement.gridDragAdaptor;// overwrite to avoid drag on folder will be moved.
//    dojo.connect(freqgrid, "onCellMouseDown", operatorManagement.gridDragAdaptor);
//    dojo.connect(grid, "onCellMouseDown", operatorManagement.gridDragAdaptor);

    freqgrid.startup();
    grid.startup();
    filterOperators();

    }

    function buildOperatorGrid(data, thetitle){
        return {
            gridData: data,
            structure: [
                {name: thetitle,field: "_item", width: "100%",
                    formatter: function(value) {
                        var type = value.imgtype[0];
                        var src = opUtil.getImageSourceByImageType(type);
                    return "<img class=\"oplistIcon\" src=\"" + src + "\" /><div class=\"oplistValue\">" + value.label[0].toLowerCase()+ "</div>";
                    }
                }
            ]
        };
    }
    function filterOperators() {
        var hadoopChecked = dijit.byId(constants.OPERATOR_FILTER_SELECT_CBOX).get('checked');
        var gridHeaderDisplay = dijit.byId(constants.OPERATOR_FILTER_SELECT).get('displayedValue');
        if (dojo.byId(constants.OPERATOR_GRID_HEADER)) {
            if (hadoopChecked) {gridHeaderDisplay += constants.HADOOP_ENABLED_TAG;}
            dojo.byId(constants.OPERATOR_GRID_HEADER).innerHTML =  gridHeaderDisplay.toUpperCase();
        }
        var showType = (typeFilter == opUtil.OP_ALL) ? "*" : typeFilter;
        var showHadoopEnabledOnly = hadoopChecked ? true : "*";
        dijit.byId(constants.OPERATOR_GRID).setQuery({
            "label": textFilter,
            "optype": showType,
            "showhadoop": showHadoopEnabledOnly
        }, false);
        dijit.byId(constants.OPERATOR_FREQ_GRID).setQuery({
            "label": textFilter,
            "optype": showType,
            "showhadoop": showHadoopEnabledOnly
        }, false);
    }


    function destroyOperatorsGrid(){
        dijit.byId(constants.OPERATOR_FREQ_GRID).destroyRecursive();
        dijit.byId(constants.OPERATOR_GRID).destroyRecursive();
        dojo.destroy(constants.OPERATOR_GRID_HEADER);
        dojo.destroy(constants.OPERATOR_FREQ_GRID_HEADER);
    }

    function _refreshOperatorPane(){
    	freqHandler.getRecentAndCustomOperators(handleFreqOperators, handleCustomizedOperators, dataAllLoaded,dataLoadFailed) ;
    }
    
    return {
    	refreshPane: _refreshOperatorPane
    };
});
