/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DataSourceExplorerUIHelper
 * Author Robbie & Gary
 */
define(["alpine/datasourceexplorer/DataSourceExplorerManager", 
        "alpine/datasourceexplorer/DatabaseExplorerUIHelper", 
        "alpine/datasourceexplorer/HadoopExplorerUIHelper", 
        "alpine/flow/OperatorManagementUIHelper",
        "alpine/spinner", 
        "alpine/AnimUtil"], function(dataSourceManager, databaseHandler, hadoopHandler, operatorManagement, spinner, animUtil){
    var constants = {
        DATASOURCE_PANE: "alpine_layout_navigation_datasource_pane",
        LINK_DATA_SOURCES: "alpine_datasourceexplorer_datasource",
        CURRENT_LOCATION: "alpine_datasourceexplorer_current_location",
        UPLOAD_BUTTON: "alpine_datasourceexplorer_import_btn",
        CREATE_SUB_FOLDER: "alpine_datasourceexplorer_create_btn",
        REFRESH_BUTTON: "alpine_datasourceexplorer_refresh_btn",
        FILTER_INPUT: "alpine_datasourceexplorer_filter",
        DISPLAY_PANE: "alpine_datasourceexplorer_display_pane",
        DISPLAY_PANE_HOLDER: "alpine_datasourceexplorer_display_pane_holder",
        DATASOURCE_GRID: "alpine_datasourceexplorer_display_grid",
        BREADCRUMB: "alpine_datasourceexplorer_breadcrumb",
        //for toolbutton create connection
        CREATE_BUTTON: "alpine_datasourceexplorer_create_connection_btn"
    };


    var accessTrack = [null];// need to be cleared when back to root

    var connectionType = {
        DATABASE: "DATABASE",
        HADOOP: "HADOOP"
    };
    
    var isEveryReady = {
		editor: false,
		explorer: false
    };
    //listen from open/close workflow editor.
    dojo.subscribe("/operatorExplorer/switchWorkflowEditor", null, function(isOpen){
    	isEveryReady["editor"] = isOpen;
    	var everyIsReady = true;
    	for(var source in isEveryReady){
    		everyIsReady &= isEveryReady[source];
    	}
    	switchDragging(everyIsReady);
    });
    dojo.subscribe("/operatorExplorer/switchDatasourceExplorer", null, function(canBeDragging){
    	isEveryReady["explorer"] = canBeDragging;
    	var everyIsReady = true;
    	for(var source in isEveryReady){
    		everyIsReady &= isEveryReady[source];
    	}
    	switchDragging(everyIsReady);
    });
    
    
    function switchDragging(ableToDragging){
    	if(dijit.byId(constants.DATASOURCE_GRID)){
    		dijit.byId(constants.DATASOURCE_GRID).setupDnDConfig({
    			"out": {
    				"row": ableToDragging
    			}
    		});
    	}
    }

    dojo.ready(function(){
        dojo.connect(dijit.byId(constants.DATASOURCE_PANE), "onShow", function(){
            if(!dijit.byId(constants.DATASOURCE_GRID)){
               // spinner.showSpinner(constants.DISPLAY_PANE);
                dataSourceManager.getAvailableConnections(buildDataGridFromBuildConnectionGridArgs);
                new alpine.layout.BreadcrumbNavigation({
                    rootLabel: alpine.nls.Data_Sources,
                    rootClickFn: function(node){
                        accessTrack = [null];

                        databaseHandler.release();
                        hadoopHandler.release();

                        dojo.byId(constants.CURRENT_LOCATION).innerHTML = "&nbsp;";
//                        _setUploadDisabled(true);
                        dataSourceManager.getAvailableConnections(buildDataGridFromBuildConnectionGridArgs);
                    }
                }, constants.BREADCRUMB);
            }
            dojo.byId(constants.FILTER_INPUT).focus();
            dojo.byId(constants.FILTER_INPUT).select();
        });

        dojo.connect(dijit.byId(constants.FILTER_INPUT), "onChange", function(){
            var val = this.get("value");
            dijit.byId(constants.DATASOURCE_GRID).filter({
                "label": val + "*"
            }, true);
        });
        
        dojo.connect(dijit.byId(constants.REFRESH_BUTTON), "onClick", function(){
        	_refreshCurrentLevel();
        });

    });
    
    function _fillCurrentLable(label){
    	if(label.length > 20){
            dojo.byId(constants.CURRENT_LOCATION).innerHTML = label.substr(0, 20) + "...";
            dojo.byId(constants.CURRENT_LOCATION).title = label;
    	}else{
            dojo.byId(constants.CURRENT_LOCATION).innerHTML = label;
    	}
    }

    function clickOnRow(currentItem, buildDisplayInfoFn){
//        _setUploadDisabled(true);
       // spinner.showSpinner(constants.DISPLAY_PANE);
        buildDisplayInfoFn.call(null, currentItem, function(displayInfo){//use anonymous function in order to use 'currentItem' out of function.
            _fillCurrentLable(toolkit.getValue(currentItem.label));
            if(accessTrack.length > 1){
                var parent = accessTrack[accessTrack.length - 1];
                var navigation = dijit.byId(constants.BREADCRUMB);
                navigation.appendCrumb({
                    label: toolkit.getValue(parent.label),
                    onCrumbClick: function(node, i){
                        navigation.removeCrumb(node);
                        _fillCurrentLable(accessTrack[i].label);
                        buildDataGrid(accessTrack[i].displayInfo);
                        accessTrack[i].displayInfo.saveCurrentKey(accessTrack[i].key);
                        accessTrack.splice(i + 1, accessTrack.length - i);
//                        _setUploadDisabled(true);	As Hadoop would able to import data in any folder
                    }
                });
            }
            var currentIteTrackInfo = {
            	key: toolkit.getValue(currentItem.key),
                label: toolkit.getValue(currentItem.label),
                displayInfo: displayInfo
            };
            if(toolkit.getValue(currentItem.configType)){
            	currentIteTrackInfo.configType = toolkit.getValue(currentItem.configType);// just for connection item to demarcate db and hadoop
            }
            accessTrack.push(currentIteTrackInfo);
//            animUtil.slideToLeft(constants.DATASOURCE_PANE,constants.DISPLAY_PANE, function() {
//                buildDataGrid(displayInfo);
//                animUtil.slideFromRight(constants.DATASOURCE_PANE,constants.DISPLAY_PANE);
//               // spinner.hideSpinner(constants.DATASOURCE_PANE);
//            });
     //       animUtil.slideToLeft(constants.DATASOURCE_PANE,constants.DISPLAY_PANE);
            buildDataGrid(displayInfo);
            animUtil.slideFromRight(constants.DISPLAY_PANE_HOLDER);

        });  //async call

//      spinner.showSpinner(constants.DISPLAY_PANE);
//      dojo.byId(constants.CURRENT_LOCATION).innerHTML = toolkit.getValue(currentItem.label);
//
//          buildDisplayInfoFn.call(null, currentItem, displayInfoCallback);
    }

    function buildConnectionGridArgs(data){
        return {
            gridData: data,
            structure: [
                {name: "",field: "label", width: "40px",
                    formatter: function(value) {
                        var src = baseURL + "/images/workbench_icons/directory.png";
                        return "<img class=\"listIcon\" src=\"" + src + "\" />";
                    }
                },
                {name: "",field: "label", width: "100%",
                    formatter: function(value) {
                        return "<div class=\"selectablelistValue\">" + value + "</div>";
                    }
                }
            ],
            buildDisplayInfoFn: switchHanderDisplayInfo,
            isOpenable: function(currentItem){
                return true;
            },
			ableToImport: function(){
				return false;
			},
			ableToCreateSubFolder: function(){
				return false;
			},
            ableToCreateConnection:function(){
                return true;
            }
        };
    }

    function switchHanderDisplayInfo(currentItem, callbackfunction){
        switch(toolkit.getValue(currentItem.configType)){
            case connectionType.DATABASE:
                databaseHandler.getSchemaDisplayInfo.call(null, currentItem, callbackfunction);
                break;
            case connectionType.HADOOP:
                hadoopHandler.getNextDisplayInfo.call(null, currentItem, callbackfunction);
                break;
        }
    }

    function buildDataGridFromBuildConnectionGridArgs(result){
        buildDataGrid(buildConnectionGridArgs(result));
      //  spinner.hideSpinner(constants.DISPLAY_PANE);
    }

    function buildDataGrid(gridArgs){
        var grid = dijit.byId(constants.DATASOURCE_GRID);
        var dataStore = new dojo.data.ItemFileWriteStore({
            data: {
                identifier: "key",
                items: gridArgs.gridData
            }
        });
        if(grid){
            destoryDataSourceGrid();
        }
        grid = new dojox.grid.EnhancedGrid({
            id: constants.DATASOURCE_GRID,
            store: dataStore,
            query: {
            	"label": dijit.byId(constants.FILTER_INPUT).get("value") + "*"
            },
            structure: gridArgs.structure,
            sortInfo: 2,
            canSort:function(){return false;},
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
							"row": false,
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
        }, dojo.create("div", {style: "width: 100%; height: 100%"}, constants.DISPLAY_PANE));
        grid.queryOptions = {ignoreCase: true};  //so filtering is case-insensitive
        grid.onCellMouseDown = operatorManagement.gridDragAdaptor;// overwrite to avoid drag on folder will be moved.


    	dojo.connect(grid, "onCellClick", function(e){
    		switch(e.cell.index){
    		case 0:
    		case 1:
                if(e.rowIndex == undefined){
                    return;
                }
                var currentItem = this.getItem(e.rowIndex);
                if(gridArgs.isOpenable(currentItem)){
                    clickOnRow(currentItem, gridArgs.buildDisplayInfoFn);
                }
                break;
            default:
            	if(gridArgs.onCellClick){
            		gridArgs.onCellClick.call(this, e);
            	}
    		}
    	});
        if(gridArgs.onRowMouseOver){
        	grid.onRowMouseOver = function(e){
        		gridArgs.onRowMouseOver.call(this, e);
        	};
        }
        if(gridArgs.onRowMouseOut){
        	grid.onRowMouseOut = function(e){
        		gridArgs.onRowMouseOut.call(this, e);
        	};
        }
        
        grid.startup();
        var dndPlugin = grid.plugin("dndop");
        dndPlugin._source.generateText = false;
        dojo.publish("/operatorExplorer/switchDatasourceExplorer", [gridArgs.dragging || false]);
        _setUploadDisabled(gridArgs.ableToImport());
        _changeCreateDisabled(gridArgs.ableToCreateSubFolder());
        _changeCreateConnectBtnStatus(gridArgs.ableToCreateConnection());
    }

    function destoryDataSourceGrid(){
        dijit.byId(constants.DATASOURCE_GRID).destroyRecursive();
    }

    function _setUploadDisabled(display) {
    	display = display || false;
        dijit.byId(constants.UPLOAD_BUTTON).set('disabled', !display);
    }
    
    function _changeCreateDisabled(display){
    	display = display || false;
        dijit.byId(constants.CREATE_SUB_FOLDER).set('disabled', !display);
    }

   function _changeCreateConnectBtnStatus(display){
       display = display || false;
       dijit.byId(constants.CREATE_BUTTON).set('disabled', !display);
    }
    
    /**
     * refresh current level data
     */
    function _refreshCurrentLevel(){
    	var parentLevel = accessTrack[accessTrack.length - 2],
    		currentLevel = accessTrack[accessTrack.length - 1];
    	if(!currentLevel){// at connection level
    		dataSourceManager.getAvailableConnections(function(result){
    			buildDataGridFromBuildConnectionGridArgs(result);
        		animUtil.slideFromRight(constants.DISPLAY_PANE_HOLDER);
    		});
    		return;
    	}
		var refreshItem = {
        		key: currentLevel.key,
        		label: currentLevel.label
    		};
    	if(parentLevel){
    		parentLevel.displayInfo.buildDisplayInfoFn.call(null, refreshItem, _callback);
    	}else{
    		if(currentLevel.configType == connectionType.DATABASE){
    			databaseHandler.getSchemaDisplayInfo.call(null, refreshItem, _callback);
    		}else if(currentLevel.configType == connectionType.HADOOP){
        		//if parentLevel == null means it mush be at root of hadoop, because db has schema level.
        		delete refreshItem.key;// load data in root don't need key attribute.
        		hadoopHandler.getNextDisplayInfo.call(null, refreshItem, _callback);
    		}
    	}
    	
    	function _callback(displayInfo){
    		currentLevel.displayInfo = displayInfo;//refresh cache in access track.
    		buildDataGrid(displayInfo);
    		animUtil.slideFromRight(constants.DISPLAY_PANE_HOLDER);
    	}
    }

    return {
        refreshCurrentLevel: _refreshCurrentLevel
    };
});